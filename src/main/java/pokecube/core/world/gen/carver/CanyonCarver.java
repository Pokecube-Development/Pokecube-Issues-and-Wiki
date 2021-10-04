package pokecube.core.world.gen.carver;

import com.mojang.serialization.Codec;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.carver.CanyonWorldCarver;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import pokecube.core.world.terrain.PokecubeTerrainChecker;

public class CanyonCarver extends CanyonWorldCarver
{

    public CanyonCarver(final Codec<ProbabilityFeatureConfiguration> codec)
    {
        super(codec);
    }

    @Override
    protected boolean canReplaceBlock(final BlockState state)
    {
        return PokecubeTerrainChecker.isCave(state);
    }
}
