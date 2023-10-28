package pokecube.legends.blocks.flowing;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import thut.api.block.flowing.FlowingBlock;

public class AshBlock extends FlowingBlock
{
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

    public static final BooleanProperty WET = BooleanProperty.create("wet");

    public int dustColour = 3816264;

    protected AshBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random random)
    {
        if (state.getBlock() != this) return;
        boolean wet = state.getValue(WET);
        Map<Direction, FluidState> nearFluids = getAdjacentFluids(level, pos);

        int water = 0;
        int lava = 0;

        int amt = getAmount(state);

        // First check for fluid above us, if that is the case, we wash away if
        // not a full block.
        FluidState f = nearFluids.get(Direction.UP);
        boolean isWater = f.is(FluidTags.WATER);
        boolean isLava = f.is(FluidTags.LAVA);
        if ((isWater || isLava) && amt < 16)
        {
            BlockState newState = f.createLegacyBlock();
            if (newState.hasProperty(LiquidBlock.LEVEL))
                level.setBlock(pos, newState.setValue(LiquidBlock.LEVEL, amt), 3);
            else level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            return;
        }

        for (Direction dir : Direction.Plane.HORIZONTAL)
        {
            // This loops in order down-up-etc, we want up-down-etc
            Direction d = dir.getOpposite();
            f = nearFluids.get(d);
            isWater = f.is(FluidTags.WATER);
            isLava = f.is(FluidTags.LAVA);
            if (isWater)
            {
                water++;
            }
            if (isLava)
            {
                lava++;
            }
            // If the fluid level is greater than us, we break.
            if (f.getAmount() - 2 > amt)
            {
                BlockState newState = f.createLegacyBlock();
                if (newState.hasProperty(LiquidBlock.LEVEL))
                    level.setBlock(pos, newState.setValue(LiquidBlock.LEVEL, amt), 3);
                else level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                return;
            }
        }
        boolean nearWater = level.isRainingAt(pos.above()) || water > 0;
        boolean shouldBeWet = nearWater;
        boolean nearLava = lava > 0;
        shouldBeWet = shouldBeWet && !nearLava;
        if (wet != shouldBeWet)
        {
            level.setBlock(pos, state = state.setValue(WET, shouldBeWet), 2);
            return;
        }
        super.tick(state, level, pos, random);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean canBeReplaced(BlockState state, Fluid fluid)
    {
        if (fluid == Fluids.LAVA && this.getAmount(state) < 16) return true;
        return super.canBeReplaced(state, fluid);
    }

    @Override
    public boolean flows(BlockState state)
    {
        if (state.getBlock() != this) super.flows(state);
        return super.flows(state);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state)
    {
        return true;
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, Random random)
    {
        if (random.nextInt(16) == 0 && state.getValue(WET) == false)
        {
            BlockPos posBelow = pos.below();
            if (FallingBlock.isFree(world.getBlockState(posBelow)))
            {
                double d0 = pos.getX() + random.nextDouble();
                double d1 = pos.getY() - 0.05D;
                double d2 = pos.getZ() + random.nextDouble();

                // TODO falling dust particle needs a mixin to use this!
                world.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, state), d0, d1, d2, 0.0D, 0.0D,
                        0.0D);
            }
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
    {
        if (!this.flows(state)) return true;

        BlockState stateBelow = world.getBlockState(pos.below());
        if (!stateBelow.is(Blocks.BARRIER))
        {
            if (!stateBelow.is(Blocks.HONEY_BLOCK))
            {
                return Block.isFaceFull(stateBelow.getCollisionShape(world, pos.below()), Direction.UP)
                        || stateBelow.is(this) && stateBelow.getValue(LAYERS) == 16;
            }
            else
            {
                return true;
            }
        }
        else
        {
            return false;
        }
    }

    private static Map<Direction, FluidState> getAdjacentFluids(LevelReader world, BlockPos pos)
    {
        Map<Direction, FluidState> map = Maps.newHashMap();
        for (Direction dir : Direction.values())
        {
            map.put(dir, world.getFluidState(pos.relative(dir)));
        }
        return map;
    }

    @Override
    protected void initStateDefinition()
    {
        this.registerDefaultState(this.stateDefinition.any().setValue(LAYERS, Integer.valueOf(1))
                .setValue(VISCOSITY, Integer.valueOf(7)).setValue(WATERLOGGED, Boolean.valueOf(false))
                .setValue(FALLING, Boolean.valueOf(false)).setValue(WET, false));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(WET);
    }

    public static class FullDust extends AshBlock
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
        public boolean flows(BlockState state)
        {
            if (state.getBlock() != this) super.flows(state);
            return !state.getValue(WET);
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
        {
            builder.add(WATERLOGGED);
            builder.add(VISCOSITY);
            builder.add(WET);
        }

        protected void initStateDefinition()
        {
            this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.valueOf(false))
                    .setValue(VISCOSITY, Integer.valueOf(7)).setValue(WET, false));
        }

        @Override
        public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
        {
            // Let the full block always report true here, it will then fall
            // when placed!
            return true;
        }

        @Override
        public VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos)
        {
            return Shapes.block();
        }
    }

    public static class PartialDust extends AshBlock implements SimpleWaterloggedBlock
    {

        public PartialDust(Properties properties)
        {
            super(properties);
        }

    }
}
