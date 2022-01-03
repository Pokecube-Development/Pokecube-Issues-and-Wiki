package thut.concrete.block;

import java.lang.reflect.Array;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import thut.api.block.flowing.FlowingBlock;
import thut.api.block.flowing.MoltenBlock;
import thut.concrete.Concrete;

public class WetConcreteBlock extends MoltenBlock
{
    public static final Map<ResourceLocation, RegistryObject<FlowingBlock>> REGMAP = Maps.newHashMap();

    public static RegistryObject<FlowingBlock>[] makeWet(DeferredRegister<Block> BLOCKS, String modid, String layer,
            String block, BlockBehaviour.Properties layer_props, BlockBehaviour.Properties block_props,
            ResourceLocation solid_layer, ResourceLocation solid_block)
    {
        ResourceLocation layer_id = new ResourceLocation(modid, layer);
        ResourceLocation block_id = new ResourceLocation(modid, block);

        @SuppressWarnings("unchecked")
        RegistryObject<FlowingBlock>[] arr = (RegistryObject<FlowingBlock>[]) Array.newInstance(RegistryObject.class,
                2);

        RegistryObject<FlowingBlock> layer_reg = BLOCKS.register(layer,
                () -> new WetConcreteBlock(layer_props).solidBlock(() -> ConcreteBlock.REGMAP.get(solid_layer).get())
                        .alternateBlock(() -> REGMAP.get(block_id).get()));
        REGMAP.put(layer_id, layer_reg);

        RegistryObject<FlowingBlock> block_reg = BLOCKS.register(block,
                () -> new FullWet(block_props).solidBlock(() -> ConcreteBlock.REGMAP.get(solid_block).get())
                        .alternateBlock(() -> REGMAP.get(layer_id).get()));
        REGMAP.put(block_id, block_reg);

        arr[0] = layer_reg;
        arr[1] = block_reg;

        return arr;
    }

    private final DyeColor colour;

    public WetConcreteBlock(Properties properties)
    {
        super(properties);
        this.tickRateFall = 1;
        this.tickRateFlow = 1;
        this.colour = DyeColor.LIGHT_GRAY;
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
    {
        builder.add(LAYERS, WATERLOGGED, FALLING);
    }

    @Override
    protected void initStateDefinition()
    {
        this.registerDefaultState(this.stateDefinition.any().setValue(LAYERS, Integer.valueOf(1))
                .setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(FALLING, Boolean.valueOf(false)));
    }

    @Override
    public RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.MODEL;
    }

    @Override
    public FluidState getFluidState(BlockState state)
    {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState();
    }

    @Override
    public BlockState getMergeResult(BlockState mergeFrom, BlockState mergeInto, BlockPos posTo, ServerLevel level)
    {
        if (mergeInto.getBlock() instanceof RebarBlock)
        {
            BlockState ret = Concrete.REBAR_BLOCK.get().defaultBlockState().setValue(RebarBlock.LEVEL,
                    this.getExistingAmount(mergeFrom, posTo, level));
            return ret;
        }
        return super.getMergeResult(mergeFrom, mergeInto, posTo, level);
    }

    public DyeColor getColour()
    {
        return colour;
    }

    public static class FullWet extends WetConcreteBlock
    {
        public FullWet(Properties properties)
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
            builder.add(WATERLOGGED);// , DryConcrete.COLOUR);
        }

        protected void initStateDefinition()
        {
            this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.valueOf(false)));
//                    .setValue(DryConcrete.COLOUR, DyeColor.LIGHT_GRAY));
        }

        @Override
        public VoxelShape getShape(BlockState state, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_)
        {
            return Shapes.block();
        }
    }

}
