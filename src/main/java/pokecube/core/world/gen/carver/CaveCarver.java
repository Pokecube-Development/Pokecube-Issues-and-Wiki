package pokecube.core.world.gen.carver;

import com.mojang.serialization.Codec;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.carver.CaveWorldCarver;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import pokecube.core.world.terrain.PokecubeTerrainChecker;

public class CaveCarver extends CaveWorldCarver
{

    public CaveCarver(final Codec<ProbabilityFeatureConfiguration> codec, final int maxHeight)
    {
        super(codec, maxHeight);
    }

    @Override
    protected boolean canReplaceBlock(final BlockState state)
    {
        return PokecubeTerrainChecker.isCave(state);
    }
}
