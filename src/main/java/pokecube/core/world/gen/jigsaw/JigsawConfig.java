package pokecube.core.world.gen.jigsaw;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import pokecube.core.database.worldgen.WorldgenHandler.JigSawConfig;

public class JigsawConfig implements FeatureConfiguration
{
    public static final Codec<JigsawConfig> CODEC = RecordCodecBuilder.create((builder) ->
    {
        return builder.group(JigSawConfig.CODEC.fieldOf("struct_config").forGetter((config) ->
        {
            return config.struct_config;
        })).apply(builder, JigsawConfig::new);
    });

    public final JigSawConfig struct_config;

    public JigsawConfig(final JigSawConfig struct)
    {
        this.struct_config = struct;
    }
}
