package thut.concrete.block;

import java.lang.reflect.Array;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import thut.api.block.flowing.FlowingBlock;
import thut.api.block.flowing.MoltenBlock;
import thut.api.item.ItemList;
import thut.concrete.Concrete;

public abstract class WetConcreteBlock extends MoltenBlock
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
                () -> new PartialWet(layer_props).solidBlock(() -> ConcreteBlock.REGMAP.get(solid_layer).get())
                        .alternateBlock(() -> REGMAP.get(block_id).get()));
        REGMAP.put(layer_id, layer_reg);

        RegistryObject<FlowingBlock> block_reg = BLOCKS.register(block, // ConcreteBlock.REGMAP.get(solid_block).get()
                () -> new FullWet(block_props).solidBlock(() -> ConcreteBlock.VANILLA.get(DyeColor.LIGHT_GRAY))
                        .alternateBlock(() -> REGMAP.get(layer_id).get()));
        REGMAP.put(block_id, block_reg);

        arr[0] = layer_reg;
        arr[1] = block_reg;

        return arr;
    }

    public static final ResourceLocation WETCONCRETEREPLACEABLE = new ResourceLocation("concrete:wet_concrete_replace");

    private final DyeColor colour;

    public WetConcreteBlock(Properties properties)
    {
        super(properties);
        this.hardenRate = 0.025f;
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
        if (state.hasProperty(FALLING) && state.getValue(FALLING) || !state.hasProperty(LAYERS)
                || state.getValue(WATERLOGGED))
            return RenderShape.MODEL;
        return RenderShape.INVISIBLE;
    }

    @Override
    public FluidState getFluidState(BlockState state)
    {
        if (state.getValue(WATERLOGGED)) return Fluids.WATER.getSource(false);
        if (isFalling(state)) return Fluids.EMPTY.defaultFluidState();
        int amt = this.getAmount(state) + 1;
        if (amt < 2) amt = 2;
        if (amt > 16) amt = 16;
        return Concrete.CONCRETE_FLUID_FLOWING.get().defaultFluidState().setValue(FlowingFluid.LEVEL, amt / 2);
    }

    @Override
    public ItemStack pickupBlock(LevelAccessor level, BlockPos pos, BlockState state)
    {
        int amt = this.getAmount(state);
        if (amt == 16)
        {
            // We are linked to the concrete fluid normally, so we let that be
            // picked up instead.
            level.setBlock(pos, this.empty(state), 3);
            if (!state.canSurvive(level, pos))
            {
                level.destroyBlock(pos, true);
            }
            return new ItemStack(Concrete.BUCKET.get());
        }
        // Below is super from SimpleWaterloggedBlock
        if (state.getValue(BlockStateProperties.WATERLOGGED))
        {
            level.setBlock(pos, state.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(false)), 3);
            if (!state.canSurvive(level, pos))
            {
                level.destroyBlock(pos, true);
            }

            return new ItemStack(Items.WATER_BUCKET);
        }
        else
        {
            return ItemStack.EMPTY;
        }
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

    @Override
    protected void checkSolid()
    {
        if (solid_full != null) return;
        this.solid_full = ConcreteBlock.VANILLA.get(DyeColor.LIGHT_GRAY).defaultBlockState();
        this.solid_layer = Concrete.DRY_LAYER[DyeColor.LIGHT_GRAY.ordinal()].get().defaultBlockState();
    }

    @Override
    public boolean canReplace(BlockState state, BlockPos pos, ServerLevel level)
    {
        return canReplace(state);
    }

    @Override
    public boolean canReplace(BlockState state)
    {
        if (state.isAir()) return true;
        if (state.canBeReplaced(Fluids.FLOWING_WATER)) return true;
        return ItemList.is(WETCONCRETEREPLACEABLE, state);
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
            builder.add(WATERLOGGED);
        }

        protected void initStateDefinition()
        {
            this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.valueOf(false)));
        }

        @Override
        public VoxelShape getShape(BlockState state, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_)
        {
            return Shapes.block();
        }
    }

    public static class PartialWet extends WetConcreteBlock
    {

        public PartialWet(Properties properties)
        {
            super(properties);
        }

    }
}
