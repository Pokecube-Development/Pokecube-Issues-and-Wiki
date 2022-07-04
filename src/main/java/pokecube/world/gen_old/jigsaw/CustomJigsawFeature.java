package pokecube.world.gen_old.jigsaw;

import com.mojang.serialization.Codec;

import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class CustomJigsawFeature extends Feature<JigsawConfig>
{

    public CustomJigsawFeature(final Codec<JigsawConfig> codec)
    {
        super(codec);
    }

    @Override
    public boolean place(final FeaturePlaceContext<JigsawConfig> context)
    {
        return false;
    }


}
