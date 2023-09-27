package thut.concrete.block;

import java.util.function.Supplier;

import org.joml.Vector3f;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import thut.api.block.flowing.IFlowingBlock;
import thut.concrete.Concrete;

public class RebarBlock extends PipeBlock implements SimpleWaterloggedBlock, IFlowingBlock
{
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 16);
    public static final BooleanProperty RUSTY = BooleanProperty.create("rusty");

    protected int tickRateFall = 150;
    protected int tickRateFlow = 10;
    public float hardenRate = 0.025f;

    Supplier<Block> wetConcrete;
    Supplier<Block> dry_layer;
    Supplier<Block> dry_full;

    protected DyeColor colour;

    public RebarBlock(Properties properties)
    {
        super(0.1875f, properties);
        initStateDefinition();
        this.tickRateFall = 1;
        this.tickRateFlow = 1;
        this.colour = DyeColor.LIGHT_GRAY;
    }

    protected void initStateDefinition()
    {
        registerDefaultState(getStateDefinition().any().setValue(IFlowingBlock.WATERLOGGED, false).setValue(LEVEL, 0)
                .setValue(RUSTY, false).setValue(UP, false).setValue(DOWN, false).setValue(NORTH, false)
                .setValue(SOUTH, false).setValue(EAST, false).setValue(WEST, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(NORTH, EAST, WEST, SOUTH, UP, DOWN, IFlowingBlock.WATERLOGGED, LEVEL, RUSTY);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state)
    {
        return getAmount(state) > 0;
    }

    @Override
    public void handlePrecipitation(BlockState p_152450_, Level p_152451_, BlockPos p_152452_, Precipitation p_152453_)
    {
        // TODO Auto-generated method stub
        super.handlePrecipitation(p_152450_, p_152451_, p_152452_, p_152453_);
    }

    @Override
    @Deprecated
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos)
    {
        int amt = getAmount(state);
        if (amt > 0) return IFlowingBlock.SHAPES[amt - 1];
        return super.getOcclusionShape(state, level, pos);
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return this.getShape(state, level, pos, context);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        Level blockgetter = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        BlockState prev = super.getStateForPlacement(context).setValue(IFlowingBlock.WATERLOGGED,
                Boolean.valueOf(fluidstate.getType() == Fluids.WATER));
        {
            var dir = context.getClickedFace().getOpposite();

            prev = prev.setValue(PROPERTY_BY_DIRECTION.get(dir), true);
            dir = dir.getOpposite();
            BlockPos pos = blockpos.relative(dir);
            BlockState clicked = blockgetter.getBlockState(pos);
            if (clicked.getBlock() instanceof RebarBlock)
            {
                prev = prev.setValue(PROPERTY_BY_DIRECTION.get(dir), true);
                dir = dir.getOpposite();
                blockgetter.setBlockAndUpdate(pos, clicked.setValue(PROPERTY_BY_DIRECTION.get(dir), true));
            }
        }

        for (Direction dir : Direction.values())
        {
            dir = dir.getOpposite();
            BlockPos pos = blockpos.relative(dir);
            BlockState clicked = blockgetter.getBlockState(pos);
            if (clicked.getBlock() instanceof RebarBlock)
            {
                prev = prev.setValue(PROPERTY_BY_DIRECTION.get(dir), true);
                dir = dir.getOpposite();
                blockgetter.setBlockAndUpdate(pos, clicked.setValue(PROPERTY_BY_DIRECTION.get(dir), true));
            }
        }

        return prev;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hitresult)
    {
        if (!player.getItemInHand(hand).isEmpty()) return InteractionResult.PASS;
        var relPos = hitresult.getLocation().subtract(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        var dir = hitresult.getDirection();
        float size = 0.1875f;// This is in the constructor for this class.
        var r = new Vector3f((float) relPos.x, (float) relPos.y, (float) relPos.z);
        var r_hat = dir.step().mul(size);
        // Get location along the face
        r_hat.absolute();
        r.setComponent(r_hat.maxComponent(), 0);
        // If it is inside the central square, we are adding a segment.
        var r_abs = new Vector3f();
        r.absolute(r_abs);
        if (r_abs.get(r_abs.maxComponent()) < size)
        {
            if (player.isShiftKeyDown()) dir = dir.getOpposite();
            var prop = PROPERTY_BY_DIRECTION.get(dir);
            boolean value = state.getValue(prop);
            level.setBlockAndUpdate(pos, state.setValue(prop, !value));
            return InteractionResult.SUCCESS;
        }
        Direction max = dir;
        float max_dot = 0;

        // Otherwise find which face we are
        for (Direction d : Direction.values())
        {
            if (d == dir || d == dir.getOpposite()) continue;
            float dot = r.dot(d.step());
            if (dot > max_dot)
            {
                max_dot = dot;
                max = d;
            }
        }
        if (max != dir)
        {
            if (player.isShiftKeyDown()) max = max.getOpposite();
            var prop = PROPERTY_BY_DIRECTION.get(max);
            boolean value = state.getValue(prop);
            level.setBlockAndUpdate(pos, state.setValue(prop, !value));
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState otherState, LevelAccessor level,
            BlockPos pos_1, BlockPos pos_2)
    {
        return state;
    }

    @Override
    public Block getAlternate()
    {
        return this;
    }

    @Override
    public int getFlowRate()
    {
        return tickRateFlow;
    }

    @Override
    public int getFallRate()
    {
        return tickRateFall;
    }

    @Override
    public FluidState getFluidState(BlockState state)
    {
        if (isFalling(state)) return Fluids.EMPTY.defaultFluidState();
        if (state.hasProperty(IFlowingBlock.WATERLOGGED) && state.getValue(IFlowingBlock.WATERLOGGED))
            return Fluids.WATER.getSource(false);
        if (!flows(state)) return Fluids.EMPTY.defaultFluidState();
        int amt = this.getAmount(state);
        if (amt == 0) return Fluids.EMPTY.defaultFluidState();
        if (amt < 2) amt = 2;
        if (amt > 16) amt = 16;
        return Concrete.CONCRETE_FLUID_FLOWING.get().defaultFluidState().setValue(FlowingFluid.LEVEL, amt / 2);
    }

    @Override
    public boolean flows(BlockState state)
    {
        if (state.hasProperty(LEVEL)) return state.getValue(LEVEL) > 0;
        return IFlowingBlock.super.flows(state);
    }

    @Override
    public int getAmount(BlockState state)
    {
        if (state.hasProperty(LEVEL)) return state.getValue(LEVEL);
        return IFlowingBlock.super.getAmount(state);
    }

    @Override
    public boolean isFalling(BlockState state)
    {
        if (this.getAmount(state) <= 0) return false;
        return IFlowingBlock.super.isFalling(state);
    }

    @Override
    public BlockState setAmount(BlockState state, int amt)
    {
        if (state.hasProperty(LEVEL)) return state.setValue(LEVEL, amt);
        return IFlowingBlock.super.setAmount(state, amt);
    }

    @Override
    public BlockState getFlowResult(BlockState flowState, BlockState destState, BlockPos posTo, ServerLevel level)
    {
        if (!(destState.getBlock() instanceof IFlowingBlock))
        {
            var newFlowState = Concrete.WET_LAYER.get().defaultBlockState();
            newFlowState = IFlowingBlock.copyValidTo(flowState, newFlowState);
            flowState = this.setAmount(newFlowState, this.getExistingAmount(flowState, posTo, level));
        }
        else if (flowState.getBlock() instanceof RebarBlock rebar)
        {
            destState = this.setAmount(destState, this.getExistingAmount(flowState, posTo, level));
            return destState;
        }
        BlockState ret = IFlowingBlock.super.getFlowResult(flowState, destState, posTo, level);
        if (destState.getBlock() instanceof RebarBlock rebar)
        {
            ret = IFlowingBlock.copyValidTo(flowState, ret);
            ret = rebar.setAmount(ret, this.getExistingAmount(flowState, posTo, level));
        }
        return ret;
    }

    @Override
    public BlockState empty(BlockState state)
    {
        if (!(state.getBlock() instanceof RebarBlock)) return IFlowingBlock.super.empty(state);
        BlockState empty = Concrete.REBAR_BLOCK.get().defaultBlockState();
        empty = IFlowingBlock.copyValidTo(state, empty);
        empty = empty.setValue(LEVEL, 0);
        return empty;
    }

    @Override
    public void neighborChanged(BlockState us, Level level, BlockPos here, Block other, BlockPos changed, boolean bool)
    {
        if (level instanceof ServerLevel slevel) reScheduleTick(us, slevel, here);
    }

    @Override
    public boolean canReplace(BlockState state)
    {
        if (state.getBlock() instanceof RebarBlock) return true;
        return IFlowingBlock.super.canReplace(state);
    }

    @Override
    public BlockState makeFalling(BlockState state, boolean falling)
    {
        if (state.getBlock() == this) return state;
        return IFlowingBlock.super.makeFalling(state, falling);
    }

    @Override
    public void onStableTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random)
    {
        if (random.nextDouble() > hardenRate)
        {
            reScheduleTick(state, level, pos);
            return;
        }
        int index = colour.ordinal();
        int amt = this.getAmount(state);
        Block blockTo = amt == 16 ? Concrete.REF_BLOCK[index].get() : Concrete.REF_LAYER[index].get();
        BlockState stateTo = IFlowingBlock.copyValidTo(state, blockTo.defaultBlockState());
        if (amt != 16) stateTo = setAmount(stateTo, amt);
        level.setBlock(pos, stateTo, 3);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random)
    {
        this.doTick(state, level, pos, random);
    }

    @Override
    public boolean isScaffolding(BlockState state, LevelReader world, BlockPos pos, LivingEntity entity)
    {
        return true;
    }

    @Override
    public boolean isLadder(BlockState state, LevelReader world, BlockPos pos, LivingEntity entity)
    {
        int i = this.getAABBIndex(state);
        if (i == 0) return false;
        return true;
    }
}
