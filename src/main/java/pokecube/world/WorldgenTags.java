package pokecube.world;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;

public class WorldgenTags
{
    public static void initTags()
    {}

    public static TagKey<Structure> NO_LAKES = TagKey.create(Registry.STRUCTURE_REGISTRY,
            new ResourceLocation("pokecube_world", "mixin_restrictions/no_lakes"));

    public static TagKey<Structure> LESS_JUNGLE_BUSHES = TagKey.create(Registry.STRUCTURE_REGISTRY,
            new ResourceLocation("pokecube_world", "mixin_restrictions/less_jungle_bushes"));

    public static TagKey<Structure> NO_FLUIDFALLS = TagKey.create(Registry.STRUCTURE_REGISTRY,
            new ResourceLocation("pokecube_world", "mixin_restrictions/no_fluidfalls"));

    public static TagKey<Structure> NO_BASALT = TagKey.create(Registry.STRUCTURE_REGISTRY,
            new ResourceLocation("pokecube_world", "mixin_restrictions/no_basalt"));
}
