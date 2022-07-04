package pokecube.legends.worldgen.features;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.valueproviders.ClampedNormalFloat;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.DripstoneClusterConfiguration;
import pokecube.legends.Reference;
import pokecube.legends.init.BlockInit;
import pokecube.legends.worldgen.utils.AquamarineUtils;

public class AquamarineClusterFeature extends Feature<DripstoneClusterConfiguration>
{
    // Block Tag
    public static final TagKey<Block> BASE_STONE_ULTRASPACE = TagKey.create(Registry.BLOCK_REGISTRY,
            new ResourceLocation(Reference.ID, "base_stone_ultraspace"));

    public AquamarineClusterFeature(final Codec<DripstoneClusterConfiguration> config)
    {
        super(config);
    }

    public boolean place(FeaturePlaceContext<DripstoneClusterConfiguration> context)
    {
        WorldGenLevel world = context.level();
        BlockPos pos = context.origin();
        DripstoneClusterConfiguration aquamarineConfig = context.config();
        Random random = context.random();
        if (!AquamarineUtils.isEmptyOrWater(world, pos))
        {
            return false;
        }
        else
        {
            int i = aquamarineConfig.height.sample(random);
            float f = aquamarineConfig.wetness.sample(random);
            float f1 = aquamarineConfig.density.sample(random);
            int j = aquamarineConfig.radius.sample(random);
            int k = aquamarineConfig.radius.sample(random);

            for (int l = -j; l <= j; ++l)
            {
                for (int i1 = -k; i1 <= k; ++i1)
                {
                    double d0 = this.getChanceOfStalagmiteOrStalactite(j, k, l, i1, aquamarineConfig);
                    BlockPos blockpos1 = pos.offset(l, 0, i1);
                    this.placeColumn(world, random, blockpos1, l, i1, f, d0, i, f1, aquamarineConfig);
                }
            }
            return true;
        }
    }

    public void placeColumn(WorldGenLevel world, Random random, BlockPos pos, int x, int y, float a, double d, int z,
            float b, DripstoneClusterConfiguration config)
    {
        Optional<Column> optional = Column.scan(world, pos, config.floorToCeilingSearchRange,
                AquamarineUtils::isEmptyOrWater, AquamarineUtils::isAquamarineBaseOrLava);
        if (optional.isPresent())
        {
            OptionalInt intOptional = optional.get().getCeiling();
            OptionalInt intOptional1 = optional.get().getFloor();
            if (intOptional.isPresent() || intOptional1.isPresent())
            {
                boolean flag = random.nextFloat() < a;
                Column column;
                if (flag && intOptional1.isPresent() && this.canPlacePool(world, pos.atY(intOptional1.getAsInt())))
                {
                    int i = intOptional1.getAsInt();
                    column = optional.get().withFloor(OptionalInt.of(i - 1));
                    world.setBlock(pos.atY(i), Blocks.WATER.defaultBlockState(), 2);
                }
                else
                {
                    column = optional.get();
                }

                OptionalInt intOptional2 = column.getFloor();
                boolean flag1 = random.nextDouble() < d;
                int j;
                if (intOptional.isPresent() && flag1 && !this.isLava(world, pos.atY(intOptional.getAsInt())))
                {
                    int k = config.dripstoneBlockLayerThickness.sample(random);
                    this.replaceBlocksWithAquamarineBlocks(world, pos.atY(intOptional.getAsInt()), k, Direction.UP);
                    int l;
                    if (intOptional2.isPresent())
                    {
                        l = Math.min(z, intOptional.getAsInt() - intOptional2.getAsInt());
                    }
                    else
                    {
                        l = z;
                    }

                    j = this.getDripstoneHeight(random, x, y, b, l, config);
                }
                else
                {
                    j = 0;
                }

                boolean flag2 = random.nextDouble() < d;
                int i3;
                if (intOptional2.isPresent() && flag2 && !this.isLava(world, pos.atY(intOptional2.getAsInt())))
                {
                    int i1 = config.dripstoneBlockLayerThickness.sample(random);
                    this.replaceBlocksWithAquamarineBlocks(world, pos.atY(intOptional2.getAsInt()), i1, Direction.DOWN);
                    if (intOptional.isPresent())
                    {
                        i3 = Math.max(0, j + Mth.randomBetweenInclusive(random,
                                -config.maxStalagmiteStalactiteHeightDiff, config.maxStalagmiteStalactiteHeightDiff));
                    }
                    else
                    {
                        i3 = this.getDripstoneHeight(random, x, y, b, z, config);
                    }
                }
                else
                {
                    i3 = 0;
                }

                int j1;
                int j3;
                if (intOptional.isPresent() && intOptional2.isPresent()
                        && intOptional.getAsInt() - j <= intOptional2.getAsInt() + i3)
                {
                    int k1 = intOptional2.getAsInt();
                    int l1 = intOptional.getAsInt();
                    int i2 = Math.max(l1 - j, k1 + 1);
                    int j2 = Math.min(k1 + i3, l1 - 1);
                    int k2 = Mth.randomBetweenInclusive(random, i2, j2 + 1);
                    int l2 = k2 - 1;
                    j3 = l1 - k2;
                    j1 = l2 - k1;
                }
                else
                {
                    j3 = j;
                    j1 = i3;
                }

                boolean flag3 = random.nextBoolean() && j3 > 0 && j1 > 0 && column.getHeight().isPresent()
                        && j3 + j1 == column.getHeight().getAsInt();
                if (intOptional.isPresent())
                {
                    AquamarineUtils.growAquamarineCrystal(world, pos.atY(intOptional.getAsInt() - 1), Direction.DOWN,
                            j3, flag3);
                }

                if (intOptional2.isPresent())
                {
                    AquamarineUtils.growAquamarineCrystal(world, pos.atY(intOptional2.getAsInt() + 1), Direction.UP, j1,
                            flag3);
                }
            }
        }
    }

    public boolean isLava(LevelReader world, BlockPos pos)
    {
        return world.getBlockState(pos).is(Blocks.LAVA);
    }

    public int getDripstoneHeight(Random random, int x, int y, float a, int z, DripstoneClusterConfiguration config)
    {
        if (random.nextFloat() > a)
        {
            return 0;
        }
        else
        {
            int i = Math.abs(x) + Math.abs(y);
            float f = (float) Mth.clampedMap((double) i, 0.0D, (double) config.maxDistanceFromCenterAffectingHeightBias,
                    (double) z / 2.0D, 0.0D);
            return (int) randomBetweenBiased(random, 0.0F, (float) z, f, (float) config.heightDeviation);
        }
    }

    public boolean canPlacePool(WorldGenLevel world, BlockPos pos)
    {
        BlockState state = world.getBlockState(pos);
        if (!state.is(Blocks.WATER) && !state.is(BlockInit.UNREFINED_AQUAMARINE.get())
                && !state.is(BlockInit.AQUAMARINE_CRYSTAL.get()))
        {
            for (Direction direction : Direction.Plane.HORIZONTAL)
            {
                if (!this.canBeAdjacentToWater(world, pos.relative(direction)))
                {
                    return false;
                }
            }

            return this.canBeAdjacentToWater(world, pos.below());
        }
        else
        {
            return false;
        }
    }

    public boolean canBeAdjacentToWater(LevelAccessor world, BlockPos pos)
    {
        BlockState state = world.getBlockState(pos);
        return state.is(BASE_STONE_ULTRASPACE) || state.getFluidState().is(FluidTags.WATER);
    }

    public void replaceBlocksWithAquamarineBlocks(WorldGenLevel world, BlockPos pos, int j, Direction direction)
    {
        BlockPos.MutableBlockPos mutablePos = pos.mutable();

        for (int i = 0; i < j; ++i)
        {
            if (!AquamarineUtils.placeAquamarineBlockIfPossible(world, mutablePos))
            {
                return;
            }
            mutablePos.move(direction);
        }

    }

    public double getChanceOfStalagmiteOrStalactite(int x, int y, int z, int a, DripstoneClusterConfiguration config)
    {
        int i = x - Math.abs(z);
        int j = y - Math.abs(a);
        int k = Math.min(i, j);
        return (double) Mth.clampedMap((float) k, 0.0F,
                (float) config.maxDistanceFromEdgeAffectingChanceOfDripstoneColumn,
                config.chanceOfDripstoneColumnAtMaxDistanceFromCenter, 1.0F);
    }

    public static float randomBetweenBiased(Random random, float a, float b, float c, float d)
    {
        return ClampedNormalFloat.sample(random, c, d, a, b);
    }
}