package thut.api.block.flowing;

import java.lang.reflect.Array;
import java.util.Random;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import thut.api.item.ItemList;
import thut.core.common.ThutCore;

public abstract class MoltenBlock extends FlowingBlock implements SimpleWaterloggedBlock
{
    public static RegistryObject<FlowingBlock>[] makeMolten(DeferredRegister<Block> BLOCKS, String modid, String layer,
            String block, BlockBehaviour.Properties layer_props, BlockBehaviour.Properties block_props,
            ResourceLocation solid_layer, ResourceLocation solid_block)
    {
        ResourceLocation layer_id = new ResourceLocation(modid, layer);
        ResourceLocation block_id = new ResourceLocation(modid, block);

        @SuppressWarnings("unchecked")
        RegistryObject<FlowingBlock>[] arr = (RegistryObject<FlowingBlock>[]) Array.newInstance(RegistryObject.class,
                2);

        RegistryObject<FlowingBlock> layer_reg = BLOCKS.register(layer,
                () -> new PartialMolten(layer_props).solidBlock(() -> SolidBlock.REGMAP.get(solid_layer).get())
                        .alternateBlock(() -> REGMAP.get(block_id).get()));
        REGMAP.put(layer_id, layer_reg);

        RegistryObject<FlowingBlock> block_reg = BLOCKS.register(block,
                () -> new FullMolten(block_props).solidBlock(() -> SolidBlock.REGMAP.get(solid_block).get())
                        .alternateBlock(() -> REGMAP.get(layer_id).get()));
        REGMAP.put(block_id, block_reg);

        arr[0] = layer_reg;
        arr[1] = block_reg;

        return arr;
    }

    public static final ResourceLocation LAVAREPLACEABLE = new ResourceLocation("thutcore:lava_replace");

    public static final BooleanProperty HEATED = BooleanProperty.create("heated");

    Supplier<Block> solid;
    public float hardenRate;

    protected BlockState solid_full = null;
    protected BlockState solid_layer = null;

    protected MoltenBlock(Properties properties)
    {
        super(properties);
        hardenRate = 1f;
        tickRateFall = 5;
        tickRateFlow = 5;
    }

    @Override
    protected void initStateDefinition()
    {
        this.registerDefaultState(this.stateDefinition.any().setValue(LAYERS, Integer.valueOf(16))
                .setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(FALLING, Boolean.valueOf(false))
                .setValue(HEATED, Boolean.valueOf(false)).setValue(VISCOSITY, 4));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(HEATED);
    }

    @Override
    public RenderShape getRenderShape(BlockState state)
    {
        if (isFalling(state)) return RenderShape.MODEL;
        return RenderShape.INVISIBLE;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state)
    {
        // This one ticks so it can harden
        return true;
    }

    @Override
    public ItemStack pickupBlock(LevelAccessor level, BlockPos pos, BlockState state)
    {
        int amt = this.getAmount(state);
        if (amt > 12)
        {
            // We are linked to the concrete fluid normally, so we let that be
            // picked up instead.
            level.setBlock(pos, this.empty(state), 3);
            if (!state.canSurvive(level, pos))
            {
                level.destroyBlock(pos, true);
            }
            return new ItemStack(Items.LAVA_BUCKET);
        }
        return SimpleWaterloggedBlock.super.pickupBlock(level, pos, state);
    }

    public MoltenBlock solidBlock(Supplier<Block> supplier)
    {
        this.solid = supplier;
        return this;
    }

    @Override
    public FluidState getFluidState(BlockState state)
    {
        if (isFalling(state)) return Fluids.EMPTY.defaultFluidState();
        int amt = this.getAmount(state);
        if (amt < 2) amt = 2;
        return Fluids.FLOWING_LAVA.defaultFluidState().setValue(FlowingFluid.LEVEL, amt / 2);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState p_60572_, BlockGetter p_60573_, BlockPos p_60574_,
            CollisionContext p_60575_)
    {
        return Shapes.empty();
    }

    @Override
    public boolean canReplace(BlockState state, BlockPos pos, ServerLevel level)
    {
        if (super.canReplace(state, pos, level)) return true;
        if (state.isFlammable(level, pos, Direction.UP)) return true;
        return false;
    }

    @Override
    public boolean canReplace(BlockState state)
    {
        if (ItemList.is(LAVAREPLACEABLE, state)) return true;
        @SuppressWarnings("deprecation")
        boolean burns = ((FireBlock) Blocks.FIRE).getBurnOdd(state) > 0;
        if (burns) return true;
        return super.canReplace(state);
    }

    @Override
    public BlockState setAmount(BlockState state, int amt)
    {
        boolean hot = state.hasProperty(HEATED) && state.getValue(HEATED);
        BlockState ret = super.setAmount(state, amt);
        if (hot && ret.hasProperty(HEATED)) ret = ret.setValue(HEATED, hot);
        return ret;
    }

    @Override
    public BlockState getMergeResult(BlockState mergeFrom, BlockState mergeInto, BlockPos posTo, ServerLevel level)
    {
        BlockState ret = super.getMergeResult(mergeFrom, mergeInto, posTo, level);
        // The result from the merge won't be heated, even if we are!
        if (ret != mergeInto && ret.hasProperty(HEATED)) ret = ret.setValue(HEATED, false);
        return ret;
    }

    @Override
    public boolean canMergeInto(BlockState here, BlockState other, BlockPos posTo, ServerLevel level)
    {
        checkSolid();
        if (solid_full.isAir()) return false;

        IFlowingBlock to = (other.getBlock() instanceof FlowingBlock) ? (IFlowingBlock) other.getBlock() : null;

        if (other.getBlock() == solid_layer.getBlock() || to != null && to.getAlternate() == solid_layer.getBlock())
        {
            int amt_from = getAmount(here);
            int amt_to = getAmount(other);
            if (amt_from != amt_to) return true;
        }

        return super.canMergeInto(here, other, posTo, level);
    }

    @Override
    public void onStableTick(BlockState state, ServerLevel level, BlockPos pos, Random random)
    {
        if (!FMLEnvironment.production)
        {
            tickRateFall = 1;
            tickRateFlow = 1;
        }
        double rng = random.nextDouble();

        harden:
        if ((!state.hasProperty(HEATED) || !state.getValue(HEATED)) && rng < hardenRate && !isFalling(state))
        {
            checkSolid();

            if (solid_full.isAir()) break harden;

            boolean stableBelow = false;

            BlockPos p2 = pos.below();
            BlockState b2 = level.getBlockState(p2);
            stableBelow = this.isStableBelow(b2, p2, level);
            if (!stableBelow) break harden;

            int dust = getExistingAmount(state, pos, level);
            BlockState solidTo = solid_full;
            // not 16 dust, we need a layer block
            if (dust != 16)
            {
                solidTo = solid_layer;
            }
            solidTo = IFlowingBlock.copyValidTo(state, solidTo);
            level.setBlock(pos, solidTo, 2);
            return;
        }
        else if (rng > hardenRate && !isFalling(state))
        {
            reScheduleTick(state, level, pos);
            return;
        }
        super.onStableTick(state, level, pos, random);
    }

    protected void checkSolid()
    {
        if (solid_layer == null)
        {
            Block block = this.solid.get();
            if (!(block instanceof FlowingBlock b))
            {
                solid_full = Blocks.AIR.defaultBlockState();
                return;
            }
            BlockState s = b.defaultBlockState();
            if (s.hasProperty(LAYERS))
            {
                solid_layer = s;
                solid_full = b.getAlternate().defaultBlockState();
                if (solid_full.hasProperty(LAYERS))
                {
                    ThutCore.LOGGER.error(new IllegalStateException("This should not be the case!"));
                    solid_full = Blocks.AIR.defaultBlockState();
                    return;
                }
            }
            else
            {
                solid_full = s;
                solid_layer = b.getAlternate().defaultBlockState();
                if (!solid_layer.hasProperty(LAYERS))
                {
                    ThutCore.LOGGER.error(new IllegalStateException("This should not be the case!"));
                    solid_full = Blocks.AIR.defaultBlockState();
                    return;
                }
            }
        }
    }

    public static class FullMolten extends MoltenBlock
    {

        public FullMolten(Properties properties)
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
            builder.add(HEATED);
            builder.add(VISCOSITY);
        }

        protected void initStateDefinition()
        {
            this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.valueOf(false))
                    .setValue(HEATED, Boolean.valueOf(false)).setValue(VISCOSITY, 4));
        }

        @Override
        public VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos)
        {
            return Shapes.block();
        }
    }

    public static class PartialMolten extends MoltenBlock
    {
        public PartialMolten(Properties properties)
        {
            super(properties);
        }
    }
}
