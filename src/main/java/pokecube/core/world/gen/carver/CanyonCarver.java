package pokecube.core.world.gen.carver;

import com.mojang.serialization.Codec;

import net.minecraft.block.BlockState;
import net.minecraft.world.gen.carver.CanyonWorldCarver;
import net.minecraft.world.gen.feature.ProbabilityConfig;
import pokecube.core.world.terrain.PokecubeTerrainChecker;

public class CanyonCarver extends CanyonWorldCarver
{

    public CanyonCarver(final Codec<ProbabilityConfig> codec)
    {
        super(codec);
    }

    @Override
    protected boolean isCarvable(final BlockState state)
    {
        return PokecubeTerrainChecker.isCave(state);
    }
}
