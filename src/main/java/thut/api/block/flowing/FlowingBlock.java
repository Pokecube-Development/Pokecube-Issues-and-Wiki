package thut.api.block.flowing;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public abstract class FlowingBlock extends Block implements IFlowingBlock
{
    public static final Map<ResourceLocation, RegistryObject<FlowingBlock>> REGMAP = Maps.newHashMap();

    public static RegistryObject<FlowingBlock>[] makeDust(DeferredRegister<Block> BLOCKS, String modid, String layer,
            String block, BlockBehaviour.Properties layer_props, BlockBehaviour.Properties block_props)
    {
        ResourceLocation layer_id = new ResourceLocation(modid, layer);
        ResourceLocation block_id = new ResourceLocation(modid, block);

        @SuppressWarnings("unchecked")
        RegistryObject<FlowingBlock>[] arr = (RegistryObject<FlowingBlock>[]) Array.newInstance(RegistryObject.class,
                2);

        RegistryObject<FlowingBlock> layer_reg = BLOCKS.register(layer,
                () -> new PartialDust(layer_props).alternateBlock(() -> REGMAP.get(block_id).get()));
        REGMAP.put(layer_id, layer_reg);
        RegistryObject<FlowingBlock> block_reg = BLOCKS.register(block,
                () -> new FullDust(block_props).alternateBlock(() -> REGMAP.get(layer_id).get()));
        REGMAP.put(block_id, block_reg);

        arr[0] = layer_reg;
        arr[1] = block_reg;

        return arr;
    }

    public int tickRateFall = 150;
    public int tickRateFlow = 10;
    protected boolean flows = true;
    private Supplier<Block> convert;

    protected FlowingBlock(Properties properties)
    {
        super(properties);
        initStateDefinition();
    }

    protected void initStateDefinition()
    {
        this.registerDefaultState(
                this.stateDefinition.any().setValue(LAYERS, Integer.valueOf(1)).setValue(VISCOSITY, Integer.valueOf(4))
                        .setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(FALLING, Boolean.valueOf(false)));
    }

    public FlowingBlock alternateBlock(Supplier<Block> supplier)
    {
        this.convert = supplier;
        return this;
    }

    @Override
    public Block getAlternate()
    {
        return convert.get();
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
    public boolean flows()
    {
        return flows;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(LAYERS);
        builder.add(WATERLOGGED);
        builder.add(FALLING);
        builder.add(VISCOSITY);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state)
    {
        return flows(state) && isFalling(state);
    }

    @Override
    public void neighborChanged(BlockState us, Level level, BlockPos here, Block other, BlockPos changed, boolean bool)
    {
        if (level instanceof ServerLevel slevel) reScheduleTick(us, slevel, here);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState state2, boolean bool)
    {
        level.scheduleTick(pos, this, tickRateFlow);
        if (level instanceof ServerLevel slevel) updateNearby(pos, slevel, tickRateFlow);
    }

    @Override
    public FluidState getFluidState(BlockState state)
    {
        if (!state.hasProperty(WATERLOGGED) || !state.getValue(WATERLOGGED)) return Fluids.EMPTY.defaultFluidState();;
        return Fluids.WATER.getSource(false);
    }

    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean flag)
    {
        if (level instanceof ServerLevel slevel && flag) updateNearby(pos, slevel, tickRateFlow);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random random)
    {
        if (this.flows(state)) this.doTick(state, level, pos, random);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return getBlockSupportShape(state, level, pos);
    }

    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return getBlockSupportShape(state, level, pos);
    }

    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos)
    {
        if (!state.hasProperty(LAYERS)) return Shapes.block();
        int amt = state.getValue(LAYERS);
        boolean falling = state.hasProperty(FALLING) && state.getValue(FALLING);
        if (falling) return Shapes.empty();
        return SHAPES[amt - 1];
    }

    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return getBlockSupportShape(state, level, pos);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos());
        if (blockstate.is(this) && blockstate.hasProperty(LAYERS))
        {
            int i = blockstate.getValue(LAYERS);
            return blockstate.setValue(LAYERS, Integer.valueOf(Math.min(16, i + 1)));
        }
        else
        {
            return super.getStateForPlacement(context);
        }
    }

    public boolean canBeReplaced(BlockState state, BlockPlaceContext context)
    {
        if (!this.flows(state)) return false;
        int i = this.getAmount(state);
        return i < 3;
    }

    public boolean useShapeForLightOcclusion(BlockState p_56630_)
    {
        return true;
    }

    public static class FullDust extends FlowingBlock
    {
        public FullDust(Properties properties)
        {
            super(properties);
        }

        @Override
        public boolean isFullBlock()
        {
            return true;
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
        {
            builder.add(WATERLOGGED);
            builder.add(VISCOSITY);
        }

        protected void initStateDefinition()
        {
            this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.valueOf(false)));
        }

        @Override
        public VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos)
        {
            return Shapes.block();
        }
    }

    public static class PartialDust extends FlowingBlock implements SimpleWaterloggedBlock
    {

        public PartialDust(Properties properties)
        {
            super(properties);
        }

    }
}
