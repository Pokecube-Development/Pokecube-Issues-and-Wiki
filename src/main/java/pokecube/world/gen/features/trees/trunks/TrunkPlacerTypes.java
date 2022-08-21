package pokecube.world.gen.features.trees.trunks;

import java.util.Random;
import java.util.function.BiConsumer;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.RegistryObject;
import pokecube.world.PokecubeWorld;

public class TrunkPlacerTypes
{
    public static final RegistryObject<TrunkPlacerType<PalmTrunkPlacer>> PALM_TRUNK_PLACER = PokecubeWorld.TRUNK_PLACERS
            .register("palm_trunk_placer", () -> new TrunkPlacerType<>(PalmTrunkPlacer.CODEC));
    public static final RegistryObject<TrunkPlacerType<StraightTrunkPlacerNoDirt>> STRAIGHT_TRUNK_PLACER_NO_DIRT = PokecubeWorld.TRUNK_PLACERS
            .register("straight_trunk_placer_no_dirt", () -> new TrunkPlacerType<>(StraightTrunkPlacerNoDirt.CODEC));

    public static void init()
    {};

    public static boolean isDirtOrSand(LevelSimulatedReader level, BlockPos pos)
    {
        return level.isStateAtPosition(pos, (state) ->
        {
            return Feature.isDirt(state) || isSand(state) && !state.is(Blocks.GRASS_BLOCK) && !state.is(Blocks.MYCELIUM);
        });
    }

    public static void setDirtAt(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter, Random random,
                                 BlockPos pos, TreeConfiguration treeConfig)
    {
        if (treeConfig.forceDirt || !isDirtOrSand(level, pos)) {
            blockSetter.accept(pos, treeConfig.dirtProvider.getState(random, pos));
        }
    }

    public static void requireForceDirtAt(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter, Random random,
                                 BlockPos pos, TreeConfiguration treeConfig)
    {
        if (treeConfig.forceDirt && !isDirtOrSand(level, pos)) {
            blockSetter.accept(pos, treeConfig.dirtProvider.getState(random, pos));
        }
    }

    public static boolean isSand(BlockState state)
    {
        return state.is(BlockTags.SAND) || state.is(Tags.Blocks.SAND);
    }
}
