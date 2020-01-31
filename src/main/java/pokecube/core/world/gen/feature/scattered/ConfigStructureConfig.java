package pokecube.core.world.gen.feature.scattered;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

import net.minecraft.world.gen.feature.IFeatureConfig;
import pokecube.core.database.worldgen.WorldgenHandler.JsonStructure;

public class ConfigStructureConfig implements IFeatureConfig
{
    public final JsonStructure struct;

    public ConfigStructureConfig(final JsonStructure struct)
    {
        this.struct = struct;
    }

    @Override
    public <T> Dynamic<T> serialize(final DynamicOps<T> ops)
    {
        return new Dynamic<>(ops, ops.createMap(ImmutableMap.of(ops.createString("struct"), ops.createString(this.struct
                .serialize()))));
    }

    public static <T> ConfigStructureConfig deserialize(final Dynamic<T> ops)
    {
        final String structstring = ops.get("struct").asString("");
        return new ConfigStructureConfig(JsonStructure.deserialize(structstring));
    }
}
