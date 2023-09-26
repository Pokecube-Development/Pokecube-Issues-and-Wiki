package thut.concrete.block;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import thut.api.block.IDyedBlock;
import thut.api.block.flowing.IFlowingBlock;

public abstract class ReinforcedConcreteBlock extends RebarBlock implements IDyedBlock
{
    public static final Map<ResourceLocation, RegistryObject<Block>> REGMAP = Maps.newHashMap();

    private static final Map<DyeColor, ReinforcedConcreteBlock> BYCOLOR = Maps.newHashMap();

    public static RegistryObject<Block>[] makeDry(DeferredRegister<Block> BLOCKS, String modid, String layer,
            String block, BlockBehaviour.Properties layer_props, BlockBehaviour.Properties block_props, DyeColor colour)
    {
        ResourceLocation layer_id = new ResourceLocation(modid, layer);
        ResourceLocation block_id = new ResourceLocation(modid, block);

        @SuppressWarnings("unchecked")
        RegistryObject<Block>[] arr = (RegistryObject<Block>[]) Array.newInstance(RegistryObject.class, 2);

        RegistryObject<Block> layer_reg = BLOCKS.register(layer,
                () -> new PartialDry(layer_props, colour).alternateBlock(() -> REGMAP.get(block_id).get()));
        REGMAP.put(layer_id, layer_reg);
        RegistryObject<Block> block_reg = BLOCKS.register(block, () -> new FullDry(block_props, colour));
        REGMAP.put(block_id, block_reg);

        arr[0] = layer_reg;
        arr[1] = block_reg;

        return arr;
    }

    private Supplier<Block> convert;

    protected ReinforcedConcreteBlock(Properties properties, DyeColor colour)
    {
        super(properties);
        this.colour = colour;
        BYCOLOR.put(colour, this);
    }

    protected void initStateDefinition()
    {
        registerDefaultState(getStateDefinition().any().setValue(IFlowingBlock.WATERLOGGED, false).setValue(LAYERS, 1)
                .setValue(RUSTY, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(NORTH, EAST, WEST, SOUTH, UP, DOWN, IFlowingBlock.WATERLOGGED, LAYERS, RUSTY);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        int amt = getAmount(state);
        if (amt < 16) return Shapes.or(this.shapeByIndex[this.getAABBIndex(state)], IFlowingBlock.SHAPES[amt - 1]);
        else return Shapes.block();
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return this.getShape(state, level, pos, context);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState otherState, LevelAccessor level,
            BlockPos pos_1, BlockPos pos_2)
    {
//        return super.updateShape(state, direction, otherState, level, pos_1, pos_2);
        return state;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state)
    {
        return false;
    }

    public ReinforcedConcreteBlock alternateBlock(Supplier<Block> supplier)
    {
        this.convert = supplier;
        return this;
    }

    @Override
    public BlockState getMergeResult(BlockState mergeFrom, BlockState mergeInto, BlockPos posTo, ServerLevel level)
    {
        return mergeInto;
    }

    @Override
    public boolean flows(BlockState state)
    {
        return false;
    }

    @Override
    public Block getAlternate()
    {
        return convert.get();
    }

    @Override
    public int getFlowRate()
    {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getFallRate()
    {
        return Integer.MAX_VALUE;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random)
    {
        // NO OP
    }

    @Override
    public Block getFor(DyeColor c)
    {
        return BYCOLOR.get(c);
    }

    @Override
    public DyeColor getColour()
    {
        return colour;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if (state.is(this)) {
            if (context.getPlayer() != null && context.getPlayer().isCreative())
            {
                int i = state.getValue(LAYERS);
                return state.setValue(LAYERS, Integer.valueOf(Math.min(16, i + 1)));
            }
        } else {
            return super.getStateForPlacement(context);
        }
        return super.getStateForPlacement(context);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        int i = state.getValue(LAYERS);
        if (context.getItemInHand().is(this.asItem()) && i < 16 && context.getPlayer() != null && context.getPlayer().isCreative()) {
            if (context.replacingClickedOnBlock()) {
                return context.getClickedFace() == Direction.UP;
            } else {
                return true;
            }
        } else {
            return i == 1;
        }
    }

    public static class FullDry extends Block implements IDyedBlock
    {
        private static final Map<DyeColor, FullDry> BYCOLOR = Maps.newHashMap();

        private final DyeColor colour;

        public FullDry(Properties properties, DyeColor colour)
        {
            super(properties);
            this.colour = colour;
            BYCOLOR.put(colour, this);
        }

        @Override
        public DyeColor getColour()
        {
            return colour;
        }

        @Override
        public Block getFor(DyeColor c)
        {
            return BYCOLOR.get(c);
        }
    }

    public static class PartialDry extends ReinforcedConcreteBlock
    {

        protected PartialDry(Properties properties, DyeColor colour)
        {
            super(properties, colour);
        }

    }
}
