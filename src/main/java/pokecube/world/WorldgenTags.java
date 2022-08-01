package pokecube.world;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;

public class WorldgenTags
{
    public static void initTags()
    {}

    public static TagKey<ConfiguredStructureFeature<?, ?>> NO_LAKES = TagKey
            .create(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, new ResourceLocation("pokecube_world", "mixin_restrictions/no_lakes"));

    public static TagKey<ConfiguredStructureFeature<?, ?>> LESS_JUNGLE_BUSHES = TagKey.create(
            Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY,
            new ResourceLocation("pokecube_world", "mixin_restrictions/less_jungle_bushes"));

    public static TagKey<ConfiguredStructureFeature<?, ?>> NO_LAVAFALLS = TagKey.create(
            Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, new ResourceLocation("pokecube_world", "mixin_restrictions/no_lavafalls"));

    public static TagKey<ConfiguredStructureFeature<?, ?>> NO_BASALT = TagKey.create(
            Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, new ResourceLocation("pokecube_world", "mixin_restrictions/no_basalt"));
}
