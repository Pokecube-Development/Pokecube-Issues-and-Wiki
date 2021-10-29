package pokecube.core.world.gen.carver;

import com.mojang.serialization.Codec;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.carver.CanyonCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CanyonWorldCarver;
import net.minecraft.world.level.levelgen.carver.UnderwaterCanyonWorldCarver;
import pokecube.core.world.terrain.PokecubeTerrainChecker;

public class UnderwaterCanyonCarver extends UnderwaterCanyonWorldCarver
{

    public UnderwaterCanyonCarver(final Codec<CanyonCarverConfiguration> codec)
    {
        super(codec);
    }

    @Override
    protected boolean canReplaceBlock(final BlockState state)
    {
        return PokecubeTerrainChecker.isCave(state);
    }
}
