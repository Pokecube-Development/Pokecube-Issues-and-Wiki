package pokecube.core.world.gen.jigsaw;

import java.util.Random;

import com.mojang.serialization.Codec;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;

public class CustomJigsawFeature extends Feature<JigsawConfig>
{

    public CustomJigsawFeature(final Codec<JigsawConfig> codec)
    {
        super(codec);
    }

    @Override
    public boolean generate(final ISeedReader reader, final ChunkGenerator generator, final Random rand, final BlockPos pos,
            final JigsawConfig config)
    {
        return false;
    }


}
