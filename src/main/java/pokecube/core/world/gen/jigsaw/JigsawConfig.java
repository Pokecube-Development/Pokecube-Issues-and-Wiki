package pokecube.core.world.gen.jigsaw;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.gen.feature.IFeatureConfig;
import pokecube.core.database.worldgen.WorldgenHandler.JigSawConfig;

public class JigsawConfig implements IFeatureConfig
{
    public static final Codec<JigsawConfig> CODEC = RecordCodecBuilder.create((builder) ->
    {
        return builder.group(Codec.STRING.fieldOf("struct").forGetter((config) ->
        {
            return config.struct.name;
        })).apply(builder, JigsawConfig::new);
    });

    public final JigSawConfig struct;

    public JigsawConfig(final String struct)
    {
        this.struct = JigSawConfig.deserialize(struct);
    }
}
