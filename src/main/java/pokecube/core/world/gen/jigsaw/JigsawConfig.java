package pokecube.core.world.gen.jigsaw;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

import net.minecraft.world.gen.feature.IFeatureConfig;
import pokecube.core.database.worldgen.WorldgenHandler.JigSawConfig;

public class JigsawConfig implements IFeatureConfig
{
    public final JigSawConfig struct;

    public JigsawConfig(final JigSawConfig struct)
    {
        this.struct = struct;
    }

    @Override
    public <T> Dynamic<T> serialize(final DynamicOps<T> ops)
    {
        return new Dynamic<>(ops, ops.createMap(ImmutableMap.of(ops.createString("struct"), ops.createString(this.struct
                .serialize()))));
    }

    public static <T> JigsawConfig deserialize(final Dynamic<T> ops)
    {
        final String structstring = ops.get("struct").asString("");
        return new JigsawConfig(JigSawConfig.deserialize(structstring));
    }
}
