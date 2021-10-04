package pokecube.core.world.gen.jigsaw;

import java.util.Random;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

public class CustomJigsawFeature extends Feature<JigsawConfig>
{

    public CustomJigsawFeature(final Codec<JigsawConfig> codec)
    {
        super(codec);
    }

    @Override
    public boolean place(final WorldGenLevel reader, final ChunkGenerator generator, final Random rand, final BlockPos pos,
            final JigsawConfig config)
    {
        return false;
    }


}
