package pokecube.world;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import thut.lib.RegHelper;

public class WorldgenTags
{
    public static void initTags()
    {}

    public static TagKey<Structure> NO_LAKES = TagKey.create(RegHelper.STRUCTURE_REGISTRY,
            new ResourceLocation("pokecube_world", "mixin_restrictions/no_lakes"));

    public static TagKey<Structure> LESS_JUNGLE_BUSHES = TagKey.create(RegHelper.STRUCTURE_REGISTRY,
            new ResourceLocation("pokecube_world", "mixin_restrictions/less_jungle_bushes"));

    public static TagKey<Structure> NO_FLUIDFALLS = TagKey.create(RegHelper.STRUCTURE_REGISTRY,
            new ResourceLocation("pokecube_world", "mixin_restrictions/no_fluidfalls"));

    public static TagKey<Structure> NO_BASALT = TagKey.create(RegHelper.STRUCTURE_REGISTRY,
            new ResourceLocation("pokecube_world", "mixin_restrictions/no_basalt"));

    public static TagKey<Biome> IS_ERODED = TagKey.create(RegHelper.BIOME_REGISTRY,
            new ResourceLocation("pokecube_world", "is_eroded_biome"));

    public static TagKey<Biome> IS_ICEBERG = TagKey.create(RegHelper.BIOME_REGISTRY,
            new ResourceLocation("pokecube_world", "is_iceberg_biome"));
}
