package pokecube.core.world.gen.carver;

import com.mojang.serialization.Codec;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CaveWorldCarver;
import net.minecraft.world.level.levelgen.carver.UnderwaterCaveWorldCarver;
import pokecube.core.world.terrain.PokecubeTerrainChecker;

public class UnderwaterCaveCarver extends UnderwaterCaveWorldCarver
{

    public UnderwaterCaveCarver(final Codec<CaveCarverConfiguration> codec)
    {
        super(codec);
    }

    @Override
    protected boolean canReplaceBlock(final BlockState state)
    {
        return PokecubeTerrainChecker.isCave(state);
    }
}
