package pokecube.core.world.gen.carver;

import com.mojang.serialization.Codec;

import net.minecraft.block.BlockState;
import net.minecraft.world.gen.carver.CaveWorldCarver;
import net.minecraft.world.gen.feature.ProbabilityConfig;
import pokecube.core.world.terrain.PokecubeTerrainChecker;

public class CaveCarver extends CaveWorldCarver
{

    public CaveCarver(final Codec<ProbabilityConfig> codec, final int maxHeight)
    {
        super(codec, maxHeight);
    }

    @Override
    protected boolean isCarvable(final BlockState state)
    {
        return PokecubeTerrainChecker.isCave(state);
    }
}
