package thut.api.block.flowing;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
import thut.core.common.ThutCore;

public class MoltenBlock extends DustBlock
{
    private static final Map<ResourceLocation, RegistryObject<DustBlock>> REGMAP = Maps.newHashMap();

    public static RegistryObject<DustBlock>[] makeMolten(DeferredRegister<Block> BLOCKS, String modid, String layer,
            String block, BlockBehaviour.Properties layer_props, BlockBehaviour.Properties block_props,
            ResourceLocation solid_layer, ResourceLocation solid_block)
    {
        ResourceLocation layer_id = new ResourceLocation(modid, layer);
        ResourceLocation block_id = new ResourceLocation(modid, block);

        @SuppressWarnings("unchecked")
        RegistryObject<DustBlock>[] arr = (RegistryObject<DustBlock>[]) Array.newInstance(RegistryObject.class, 2);

        RegistryObject<DustBlock> layer_reg = BLOCKS.register(layer,
                () -> new MoltenBlock(layer_props).solidBlock(() -> SolidBlock.REGMAP.get(solid_layer).get())
                        .alternateBlock(() -> REGMAP.get(block_id).get()));
        REGMAP.put(layer_id, layer_reg);

        RegistryObject<DustBlock> block_reg = BLOCKS.register(block,
                () -> new FullMolten(block_props).solidBlock(() -> SolidBlock.REGMAP.get(solid_block).get())
                        .alternateBlock(() -> REGMAP.get(layer_id).get()));
        REGMAP.put(block_id, block_reg);

        arr[0] = layer_reg;
        arr[1] = block_reg;

        return arr;
    }

    Supplier<Block> solid;
    float hardenRate = 1;

    BlockState solid_full = null;
    BlockState solid_layer = null;

    public MoltenBlock(Properties properties)
    {
        super(properties);
        this.slope = 7;
        hardenRate = 0.5f;
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
        return Fluids.FLOWING_LAVA.defaultFluidState();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState p_60572_, BlockGetter p_60573_, BlockPos p_60574_,
            CollisionContext p_60575_)
    {
        return Shapes.empty();
    }

    @Override
    protected boolean canReplace(BlockState state, BlockPos pos, ServerLevel level)
    {
        if (state.isFlammable(level, pos, Direction.UP)) return true;
        return super.canReplace(state, pos, level);
    }

    @Override
    protected BlockState getMergeResult(BlockState mergeFrom, BlockState mergeInto, BlockPos posTo, ServerLevel level)
    {
        checkSolid();
        if (solid_full.isAir()) return mergeInto;

        if (mergeInto.getBlock() == solid_layer.getBlock()) return mergeFrom;

        // for now, magma overrides other blocks.
        if (mergeInto.getBlock() instanceof DustBlock) return mergeFrom;

        return super.getMergeResult(mergeFrom, mergeInto, posTo, level);
    }

    @Override
    protected void onStableTick(BlockState state, ServerLevel level, BlockPos pos, Random random)
    {
        double rng = random.nextDouble();
        harden:
        if (rng < hardenRate && !isFalling(state))
        {
            checkSolid();

            if (solid_full.isAir()) break harden;

            boolean stableBelow = false;

            for (int i = 1; i < 2; i++)
            {
                BlockPos p2 = pos.below(i);
                BlockState b2 = level.getBlockState(p2);
                stableBelow = this.isStableBelow(b2, p2, level);
                if (stableBelow) break;
            }
            if (!stableBelow) break harden;

            int dust = getExistingAmount(state, pos, level);
            // 16 dust, we need a full block
            if (dust == 16)
            {
                level.setBlock(pos, solid_full, 2);
            }
            else
            {
                level.setBlock(pos, solid_layer.setValue(LAYERS, dust), 2);
            }
            return;
        }
        else if (rng > hardenRate)
        {
            level.scheduleTick(pos.immutable(), this, tickRateFall);
            return;
        }
        super.onStableTick(state, level, pos, random);
    }

    private void checkSolid()
    {
        if (solid_layer == null)
        {
            Block block = this.solid.get();
            if (!(block instanceof DustBlock b))
            {
                solid_full = Blocks.AIR.defaultBlockState();
                return;
            }
            BlockState s = b.defaultBlockState();
            if (s.hasProperty(LAYERS))
            {
                solid_layer = s;
                solid_full = b.getAlternate().get().defaultBlockState();
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
                solid_layer = b.getAlternate().get().defaultBlockState();
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

        protected int getExistingAmount(BlockState state, BlockPos pos, ServerLevel level)
        {
            if (state.getBlock() == this) return 16;
            if (state.getBlock() instanceof DustBlock && state.hasProperty(LAYERS)) return state.getValue(LAYERS);
            if (state.canBeReplaced(Fluids.FLOWING_WATER)) return 0;
            if (state.isAir()) return 0;
            return -1;
        }

        protected BlockState makeFalling(BlockState state, boolean falling)
        {
            if (!falling) return this.defaultBlockState();
            return getAlternate().get().defaultBlockState().setValue(LAYERS, 16).setValue(FALLING, falling);
        }

        protected BlockState setAmount(BlockState state, int amt)
        {
            if (amt == 16) return this.defaultBlockState();
            return getAlternate().get().defaultBlockState().setValue(LAYERS, amt);
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
}
