package pokecube.legends.init;

import java.util.Locale;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PlaceOnWaterBlockItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.RegistryObject;
import pokecube.api.entity.pokemob.Nature;
import pokecube.api.utils.PokeType;
import pokecube.core.entity.boats.GenericBoat;
import pokecube.core.init.ItemGenerator;
import pokecube.core.client.render.mobs.RenderPokecube;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.Reference;
import pokecube.legends.items.DistortedMirror;
import pokecube.legends.items.ItemBase;
import pokecube.legends.items.ItemTiers;
import pokecube.legends.items.LegendsSword;
import pokecube.legends.items.RainbowSword;
import pokecube.legends.items.TemporalBambooBlockItem;
import pokecube.legends.items.UltraKey;
import pokecube.legends.items.armor.UltraBootsEffect;
import pokecube.legends.items.armor.UltraHelmetEffect;
import pokecube.legends.items.natureedit.ItemNature;
import pokecube.legends.items.tools.ZamazentaShieldItem;
import pokecube.legends.items.zmove.ItemZCrystal;

public class ItemInit
{
    // Materials
    public static final Tier MATERIAL_RAINBOW = Tiers.DIAMOND;
    public static final Tier MATERIAL_JUSTISE = Tiers.DIAMOND;

    // Keys
    // Orbs
    public static final RegistryObject<Item> BLUE_ORB;
    public static final RegistryObject<Item> GREEN_ORB;
    public static final RegistryObject<Item> RED_ORB;
    public static final RegistryObject<Item> GRAY_ORB;
    public static final RegistryObject<Item> RAINBOW_ORB;
    public static final RegistryObject<Item> LUSTROUS_ORB;
    public static final RegistryObject<Item> ADAMANT_ORB;
    public static final RegistryObject<Item> OCEAN_ORB;
    public static final RegistryObject<Item> LIFE_ORB;
    public static final RegistryObject<Item> DESTRUCT_ORB;
    public static final RegistryObject<Item> REGIS_ORB;
    public static final RegistryObject<Item> KOKO_ORB;
    public static final RegistryObject<Item> BULU_ORB;
    public static final RegistryObject<Item> LELE_ORB;
    public static final RegistryObject<Item> FINI_ORB;
    public static final RegistryObject<Item> SOUL_HEART;
    public static final RegistryObject<Item> SOUL_DEW;

    // Cores
    public static final RegistryObject<Item> ROCK_CORE;
    public static final RegistryObject<Item> ICE_CORE;
    public static final RegistryObject<Item> STEEL_CORE;
    public static final RegistryObject<Item> THUNDER_CORE;
    public static final RegistryObject<Item> DRAGO_CORE;
    public static final RegistryObject<Item> MAGMA_CORE;
    public static final RegistryObject<Item> KYUREM_CORE;
    public static final RegistryObject<Item> STEAM_CORE;

    // Misc
    public static final RegistryObject<Item> LIGHT_STONE;
    public static final RegistryObject<Item> DARK_STONE;
    public static final RegistryObject<Item> EMBLEM;
    public static final RegistryObject<Item> ORANGE_RUNE;
    public static final RegistryObject<Item> BLUE_RUNE;
    public static final RegistryObject<Item> GREEN_RUNE;
    public static final RegistryObject<Item> ICE_CARROT;
    public static final RegistryObject<Item> SHADOW_CARROT;
    public static final RegistryObject<Item> ANCIENT_STONE;
    public static final RegistryObject<Item> IMPRISIONMENT_HELMET;

    public static final RegistryObject<Item> FIRE_WING;
    public static final RegistryObject<Item> DARK_FIRE_WING;
    public static final RegistryObject<Item> ELECTRIC_WING;
    public static final RegistryObject<Item> STATIC_WING;
    public static final RegistryObject<Item> ICE_WING;
    public static final RegistryObject<Item> ICE_DARK_WING;

    public static final RegistryObject<Item> FLAME_GEM;
    public static final RegistryObject<Item> WATER_GEM;
    public static final RegistryObject<Item> THUNDER_GEM;
    public static final RegistryObject<Item> DIAMOND_GEM;
    public static final RegistryObject<Item> AZELF_GEM;
    public static final RegistryObject<Item> MESPRIT_GEM;
    public static final RegistryObject<Item> UXIE_GEM;

    public static final RegistryObject<Item> NIGHTMARE_BOOK;
    public static final RegistryObject<Item> LUNAR_WING;

    public static final RegistryObject<Item> MANAPHY_NECKLACE;

    public static final RegistryObject<Item> MELOETTA_OCARINA;

    public static final RegistryObject<Item> METEOR_SHARD;

    public static final RegistryObject<Item> STAR_CORE;

    public static final RegistryObject<Item> COSMIC_ORB;
    public static final RegistryObject<Item> LIGHTING_CRYSTAL;

    public static final RegistryObject<Item> KUBFU_SCARF;
    public static final RegistryObject<Item> WOODEN_CROWN;

    // Gems
    public static final RegistryObject<Item> AQUAMARINE;
    public static final RegistryObject<Item> RUBY;
    public static final RegistryObject<Item> SAPPHIRE;

    // Forms
    public static final RegistryObject<Item> SILVER_WING;
    public static final RegistryObject<Item> RAINBOW_WING;
    public static final RegistryObject<Item> AQUAMARINE_SHARD;
    public static final RegistryObject<Item> GRISEOUS_ORB;
    public static final RegistryObject<Item> ZYGARDE_CUBE;
    public static final RegistryObject<Item> PRISION_BOTTLE;
    public static final RegistryObject<Item> REVEAL_GLASS;
    public static final RegistryObject<Item> DNA_SPLICER_A;
    public static final RegistryObject<Item> DNA_SPLICER_B;
    public static final RegistryObject<Item> GRACIDEA;
    public static final RegistryObject<Item> METEORITE;
    public static final RegistryObject<Item> N_SOLARIZER;
    public static final RegistryObject<Item> N_LUNARIZER;
    public static final RegistryObject<Item> AZURE_FLUTE;
    public static final RegistryObject<Item> RUSTED_SHIELD;
    public static final RegistryObject<Item> RUSTED_SWORD;
    public static final RegistryObject<Item> REINS_UNITY;

    // Evolutions
    public static final RegistryObject<Item> CHIPPED_POT;
    public static final RegistryObject<Item> CRACKED_POT;
    public static final RegistryObject<Item> GALARCUFF;
    public static final RegistryObject<Item> PARCHMENT_DARK;
    public static final RegistryObject<Item> PARCHMENT_WATER;
    public static final RegistryObject<Item> GALARWREATH;

    // Raids/Dynamax/Gigantamax
    public static final RegistryObject<Item> WISHING_PIECE;
    public static final RegistryObject<Item> GIGANTIC_SHARD;

    // Tools
    public static final RegistryObject<Item> RAINBOW_SWORD;
    public static final RegistryObject<Item> KELDEO_SWORD;
    public static final RegistryObject<Item> VIRIZION_SWORD;
    public static final RegistryObject<Item> COBALION_SWORD;
    public static final RegistryObject<Item> TERRAKION_SWORD;
    public static final RegistryObject<Item> ZAMAZENTA_SHIELD;
    public static final RegistryObject<Item> ZACIAN_SWORD;

    // Dimensions
    public static final RegistryObject<Item> PILE_OF_ASH;
    public static final RegistryObject<Item> SPECTRUM_SHARD;
    public static final RegistryObject<Item> ULTRA_KEY;
    public static final RegistryObject<Item> COSMIC_DUST;
    public static final RegistryObject<Item> FRACTAL_SHARD;
    public static final RegistryObject<Item> DISTORTIC_WATER_BUCKET;

    // Giratina
    public static final RegistryObject<Item> GIRATINA_MIRROR;
    public static final RegistryObject<Item> HEAD_MIRROR;
    public static final RegistryObject<Item> BODY_MIRROR;
    public static final RegistryObject<Item> GLASS_MIRROR;

    // Armor
    public static final RegistryObject<ArmorItem> ULTRA_HELMET;
    public static final RegistryObject<ArmorItem> ULTRA_CHESTPLATE;
    public static final RegistryObject<ArmorItem> ULTRA_LEGGINGS;
    public static final RegistryObject<ArmorItem> ULTRA_BOOTS;

    // Torch
    public static final RegistryObject<Item> INFECTED_TORCH;

    // Plants
    public static final RegistryObject<Item> DISTORTIC_VINES;
    public static final RegistryObject<Item> GOLDEN_SHROOM;
    public static final RegistryObject<Item> GOLDEN_SWEET_BERRIES;
    public static final RegistryObject<Item> PINK_TAINTED_LILY_PAD;
    public static final RegistryObject<Item> TAINTED_LILY_PAD;
    public static final RegistryObject<Item> TEMPORAL_BAMBOO;

    // Foods
    public static final RegistryObject<Item> ASPEAR_POKEPUFF;
    public static final RegistryObject<Item> CHERI_POKEPUFF;
    public static final RegistryObject<Item> CHESTO_POKEPUFF;
    public static final RegistryObject<Item> CORNN_POKEPUFF;
    public static final RegistryObject<Item> ENIGMA_POKEPUFF;
    public static final RegistryObject<Item> GREPA_POKEPUFF;
    public static final RegistryObject<Item> HONDEW_POKEPUFF;
    public static final RegistryObject<Item> JABOCA_POKEPUFF;
    public static final RegistryObject<Item> KELPSY_POKEPUFF;
    public static final RegistryObject<Item> LEPPA_POKEPUFF;
    public static final RegistryObject<Item> LUM_POKEPUFF;
    public static final RegistryObject<Item> NANAB_POKEPUFF;
    public static final RegistryObject<Item> NULL_POKEPUFF;
    public static final RegistryObject<Item> ORAN_POKEPUFF;
    public static final RegistryObject<Item> PECHA_POKEPUFF;
    public static final RegistryObject<Item> PERSIM_POKEPUFF;
    public static final RegistryObject<Item> PINAP_POKEPUFF;
    public static final RegistryObject<Item> POMEG_POKEPUFF;
    public static final RegistryObject<Item> QUALOT_POKEPUFF;
    public static final RegistryObject<Item> RAWST_POKEPUFF;
    public static final RegistryObject<Item> ROWAP_POKEPUFF;
    public static final RegistryObject<Item> SITRUS_POKEPUFF;
    public static final RegistryObject<Item> TAMATO_POKEPUFF;

    // Signs
    public static final RegistryObject<Item> AGED_SIGN;
    public static final RegistryObject<Item> CONCRETE_SIGN;
    public static final RegistryObject<Item> CONCRETE_DENSE_SIGN;
    public static final RegistryObject<Item> CORRUPTED_SIGN;
    public static final RegistryObject<Item> DISTORTIC_SIGN;
    public static final RegistryObject<Item> INVERTED_SIGN;
    public static final RegistryObject<Item> MIRAGE_SIGN;
    public static final RegistryObject<Item> TEMPORAL_SIGN;

    // Hanging Signs
    public static final RegistryObject<Item> AGED_HANGING_SIGN;
    public static final RegistryObject<Item> CONCRETE_HANGING_SIGN;
    public static final RegistryObject<Item> CORRUPTED_HANGING_SIGN;
    public static final RegistryObject<Item> DISTORTIC_HANGING_SIGN;
    public static final RegistryObject<Item> INVERTED_HANGING_SIGN;
    public static final RegistryObject<Item> MIRAGE_HANGING_SIGN;
    public static final RegistryObject<Item> TEMPORAL_HANGING_SIGN;

    public static Map<Nature, RegistryObject<Item>> mints = Maps.newHashMap();
    public static Map<PokeType, RegistryObject<Item>> zCrystals = Maps.newHashMap();

    static
    {
        // Legends Creative Tab - Sorting depends on the order the items are
        // listed in
        // Orbs
        BLUE_ORB = PokecubeLegends.ITEMS.register("blueorb",
                () -> new ItemBase("blueorb", 2, 1));
        COSMIC_ORB = PokecubeLegends.ITEMS.register("cosmic_orb",
                () -> new ItemBase("cosmic_orb", 2, 1));
        DESTRUCT_ORB = PokecubeLegends.ITEMS.register("destructorb",
                () -> new ItemBase("destructorb", 2, 1));
        GRAY_ORB = PokecubeLegends.ITEMS.register("grayorb",
                () -> new ItemBase("grayorb", 1, 1));
        GREEN_ORB = PokecubeLegends.ITEMS.register("greenorb",
                () -> new ItemBase("greenorb", 2, 1));
        LIFE_ORB = PokecubeLegends.ITEMS.register("lifeorb",
                () -> new ItemBase("lifeorb", 2, 1));
        LUSTROUS_ORB = PokecubeLegends.ITEMS.register("lustrousorb",
                () -> new ItemBase("lustrousorb", 2, 1));
        OCEAN_ORB = PokecubeLegends.ITEMS.register("oceanorb",
                () -> new ItemBase("oceanorb", 2, 1));
        RAINBOW_ORB = PokecubeLegends.ITEMS.register("legendaryorb",
                () -> new ItemBase("legendaryorb", 2, 1).setShiny());
        RED_ORB = PokecubeLegends.ITEMS.register("redorb",
                () -> new ItemBase("redorb", 2, 1));
        REGIS_ORB = PokecubeLegends.ITEMS.register("regisorb",
                () -> new ItemBase("regisorb", 2, 1));
        BULU_ORB = PokecubeLegends.ITEMS.register("bulu_orb",
                () -> new ItemBase("bulu_orb", 2, 1));
        FINI_ORB = PokecubeLegends.ITEMS.register("fini_orb",
                () -> new ItemBase("fini_orb", 2, 1));
        KOKO_ORB = PokecubeLegends.ITEMS.register("koko_orb",
                () -> new ItemBase("koko_orb", 2, 1));
        LELE_ORB = PokecubeLegends.ITEMS.register("lele_orb",
                () -> new ItemBase("lele_orb", 2, 1));
        SOUL_DEW = PokecubeLegends.ITEMS.register("soul_dew",
                () -> new ItemBase("soul_dew", 2, 1));
        SOUL_HEART = PokecubeLegends.ITEMS.register("soul_heart",
                () -> new ItemBase("soul_heart", 2, 1));
        ADAMANT_ORB = PokecubeLegends.ITEMS.register("adamantorb",
                () -> new ItemBase("adamantorb", 2, 1));
        GRISEOUS_ORB = PokecubeLegends.ITEMS.register("griseousorb",
                () -> new ItemBase("griseousorb", 1, 1));

        // Gem
        AZELF_GEM = PokecubeLegends.ITEMS.register("azelf_gem",
                () -> new ItemBase("azelf_gem", 2, 1));
        DIAMOND_GEM = PokecubeLegends.ITEMS.register("diamond_gem",
                () -> new ItemBase("diamond_gem", 2, 1));
        FLAME_GEM = PokecubeLegends.ITEMS.register("flame_gem",
                () -> new ItemBase("flame_gem", 2, 1));
        MESPRIT_GEM = PokecubeLegends.ITEMS.register("mesprit_gem",
                () -> new ItemBase("mesprit_gem", 2, 1));
        THUNDER_GEM = PokecubeLegends.ITEMS.register("thunder_gem",
                () -> new ItemBase("thunder_gem", 2, 1));
        UXIE_GEM = PokecubeLegends.ITEMS.register("uxie_gem",
                () -> new ItemBase("uxie_gem", 2, 1));
        WATER_GEM = PokecubeLegends.ITEMS.register("water_gem",
                () -> new ItemBase("water_gem", 2, 1));

        // Stones
        ANCIENT_STONE = PokecubeLegends.ITEMS.register("ancient_stone",
                () -> new ItemBase("ancient_stone", 2, 1));
        DARK_STONE = PokecubeLegends.ITEMS.register("darkstone",
                () -> new ItemBase("darkstone", 2, 1));
        LIGHT_STONE = PokecubeLegends.ITEMS.register("lightstone",
                () -> new ItemBase("lightstone", 2, 1));
        ROCK_CORE = PokecubeLegends.ITEMS.register("rockcore",
                () -> new ItemBase("rockcore", 2, 1));

        // Cores
        DRAGO_CORE = PokecubeLegends.ITEMS.register("dragocore",
                () -> new ItemBase("dragocore", 2, 1));
        EMBLEM = PokecubeLegends.ITEMS.register("emblem",
                () -> new ItemBase("emblem", 2, 1));
        ICE_CORE = PokecubeLegends.ITEMS.register("icecore",
                () -> new ItemBase("icecore", 2, 1));
        KYUREM_CORE = PokecubeLegends.ITEMS.register("kyurem_core",
                () -> new ItemBase("kyurem_core", 2, 1));
        MAGMA_CORE = PokecubeLegends.ITEMS.register("magmacore",
                () -> new ItemBase("magmacore", 2, 1));
        STAR_CORE = PokecubeLegends.ITEMS.register("star_core",
                () -> new ItemBase("star_core", 2, 1));
        STEAM_CORE = PokecubeLegends.ITEMS.register("steam_core",
                () -> new ItemBase("steam_core", 2, 1));
        STEEL_CORE = PokecubeLegends.ITEMS.register("steelcore",
                () -> new ItemBase("steelcore", 2, 1));
        THUNDER_CORE = PokecubeLegends.ITEMS.register("thundercore",
                () -> new ItemBase("thundercore", 2, 1));

        // Runes
        BLUE_RUNE = PokecubeLegends.ITEMS.register("blue_rune",
                () -> new ItemBase("blue_rune", 2, 1));
        GREEN_RUNE = PokecubeLegends.ITEMS.register("green_rune",
                () -> new ItemBase("green_rune", 2, 1));
        ORANGE_RUNE = PokecubeLegends.ITEMS.register("orange_rune",
                () -> new ItemBase("orange_rune", 2, 1));

        // Wings
        DARK_FIRE_WING = PokecubeLegends.ITEMS.register("dark_fire_wing",
                () -> new ItemBase("dark_fire_wing", 2, 1));
        ELECTRIC_WING = PokecubeLegends.ITEMS.register("electric_wing",
                () -> new ItemBase("electric_wing", 2, 1));
        FIRE_WING = PokecubeLegends.ITEMS.register("fire_wing",
                () -> new ItemBase("fire_wing", 2, 1));
        ICE_DARK_WING = PokecubeLegends.ITEMS.register("ice_dark_wing",
                () -> new ItemBase("ice_dark_wing", 2, 1));
        ICE_WING = PokecubeLegends.ITEMS.register("ice_wing",
                () -> new ItemBase("ice_wing", 2, 1));
        LUNAR_WING = PokecubeLegends.ITEMS.register("lunar_wing",
                () -> new ItemBase("lunar_wing", 2, 1));
        RAINBOW_WING = PokecubeLegends.ITEMS.register("rainbow_wing",
                () -> new ItemBase("rainbow_wing", 1, 1).setShiny());
        SILVER_WING = PokecubeLegends.ITEMS.register("silver_wing",
                () -> new ItemBase("silver_wing", 1, 1));
        STATIC_WING = PokecubeLegends.ITEMS.register("static_wing",
                () -> new ItemBase("static_wing", 2, 1));

        ICE_CARROT = PokecubeLegends.ITEMS.register("ice_carrot",
                () -> new ItemBase("ice_carrot", 3,
                        Rarity.RARE, FoodInit.ICE_CARROT, 16));
        SHADOW_CARROT = PokecubeLegends.ITEMS.register("shadow_carrot",
                () -> new ItemBase("shadow_carrot", 3,
                        Rarity.RARE, FoodInit.SHADOW_CARROT, 16));

        // Misc
        AZURE_FLUTE = PokecubeLegends.ITEMS.register("azure_flute",
                () -> new ItemBase("azure_flute", 2, 1));
        DNA_SPLICER_A = PokecubeLegends.ITEMS.register("dna_splicera",
                () -> new ItemBase("dna_splicers", 1, 1));
        DNA_SPLICER_B = PokecubeLegends.ITEMS.register("dna_splicerb",
                () -> new ItemBase("dna_splicers", 1, 1));
        GRACIDEA = PokecubeLegends.ITEMS.register("gracidea",
                () -> new ItemBase("gracidea", 1, 10));
        KUBFU_SCARF = PokecubeLegends.ITEMS.register("kubfu_spawn",
                () -> new ItemBase("kubfu_spawn", 2, 1));
        LIGHTING_CRYSTAL = PokecubeLegends.ITEMS.register("lighting_crystal",
                () -> new ItemBase("lighting_crystal", 2, 1));
        MANAPHY_NECKLACE = PokecubeLegends.ITEMS.register("manaphy_necklace",
                () -> new ItemBase("manaphy_necklace", 2, 1));
        MELOETTA_OCARINA = PokecubeLegends.ITEMS.register("meloetta_ocarina",
                () -> new ItemBase("meloetta_ocarina", 2, 1));
        METEOR_SHARD = PokecubeLegends.ITEMS.register("meteor_shard",
                () -> new ItemBase("meteor_shard", 2, 1));
        METEORITE = PokecubeLegends.ITEMS.register("meteorite",
                () -> new ItemBase("meteorite", 1, 16));
        NIGHTMARE_BOOK = PokecubeLegends.ITEMS.register("nightmare_book",
                () -> new ItemBase("nightmare_book", 2, 1));
        N_LUNARIZER = PokecubeLegends.ITEMS.register("n_moon",
                () -> new ItemBase("n_moon", 1, 1));
        N_SOLARIZER = PokecubeLegends.ITEMS.register("n_sun",
                () -> new ItemBase("n_sun", 1, 1));
        PRISION_BOTTLE = PokecubeLegends.ITEMS.register("prisonbottle",
                () -> new ItemBase("prisonbottle", 3, 1));
        REVEAL_GLASS = PokecubeLegends.ITEMS.register("revealglass",
                () -> new ItemBase("revealglass", 1, 1));
        RUSTED_SHIELD = PokecubeLegends.ITEMS.register("rustedshield",
                () -> new ItemBase("rustedshield", 2, 1));
        RUSTED_SWORD = PokecubeLegends.ITEMS.register("rustedsword",
                () -> new ItemBase("rustedsword", 2, 1));
        WOODEN_CROWN = PokecubeLegends.ITEMS.register("wooden_crown",
                () -> new ItemBase("wooden_crown", 2, 1));
        ZYGARDE_CUBE = PokecubeLegends.ITEMS.register("zygardecube",
                () -> new ItemBase("zygardecube", 3, 1));

        IMPRISIONMENT_HELMET = PokecubeLegends.ITEMS.register("imprisonment_helmet",
                () -> new ItemBase("imprisonment_helmet", 2, 1));
        COBALION_SWORD = PokecubeLegends.ITEMS.register("cobalion_sword",
                () -> new LegendsSword(ItemInit.MATERIAL_JUSTISE, 2, -2.4F, new Item.Properties())
                .setTooltipName("cobalion_sword").setTooltipExtraLine(2));
        KELDEO_SWORD = PokecubeLegends.ITEMS.register("keldeo_sword",
                () -> new LegendsSword(ItemInit.MATERIAL_JUSTISE, 2, -2.4F, new Item.Properties())
                .setTooltipName("keldeo_sword").setTooltipExtraLine(2).setShiny());
        TERRAKION_SWORD = PokecubeLegends.ITEMS.register("terrakion_sword",
                () -> new LegendsSword(ItemInit.MATERIAL_JUSTISE, 2, -2.4F, new Item.Properties())
                .setTooltipName("terrakion_sword").setTooltipExtraLine(2));
        VIRIZION_SWORD = PokecubeLegends.ITEMS.register("virizion_sword",
                () -> new LegendsSword(ItemInit.MATERIAL_JUSTISE, 3, -2.4F, new Item.Properties())
                .setTooltipName("virizion_sword").setTooltipExtraLine(2));
        ZACIAN_SWORD = PokecubeLegends.ITEMS.register("zacian_sword",
                () -> new LegendsSword(Tiers.NETHERITE, 3, -2.4F, new Item.Properties().fireResistant())
                .setTooltipName("zacian_sword").setTooltipExtraLine(1));

        // Shields
        ZAMAZENTA_SHIELD = PokecubeLegends.ITEMS.register("zamazenta_shield",
                () -> new ZamazentaShieldItem(Tiers.NETHERITE, "zamazenta_shield",
                        new Item.Properties().durability(200).fireResistant()).setTooltipExtraLine(1));

        // Items Creative Tab - Sorting depends on the order the items are
        // listed in
        // Gems
        COSMIC_DUST = PokecubeLegends.ITEMS.register("cosmic_dust", () -> new ItemBase(16));
        AQUAMARINE = PokecubeLegends.ITEMS.register("aquamarine", () -> new ItemBase(64));
        AQUAMARINE_SHARD = PokecubeLegends.ITEMS.register("aquamarine_shard",
                () -> new ItemBase(64));
        FRACTAL_SHARD = PokecubeLegends.ITEMS.register("fractal_shard",
                () -> new ItemBase(64));
        RUBY = PokecubeLegends.ITEMS.register("ruby", () -> new ItemBase(64));
        SAPPHIRE = PokecubeLegends.ITEMS.register("sapphire", () -> new ItemBase(64));
        SPECTRUM_SHARD = PokecubeLegends.ITEMS.register("spectrum_shard",
                () -> new ItemBase(64));

        PILE_OF_ASH = PokecubeLegends.ITEMS.register("pile_of_ash", () -> new ItemBase(64));

        HEAD_MIRROR = PokecubeLegends.ITEMS.register("head_mirror", () -> new ItemBase(1));
        BODY_MIRROR = PokecubeLegends.ITEMS.register("body_mirror", () -> new ItemBase(1));
        GLASS_MIRROR = PokecubeLegends.ITEMS.register("glass_mirror", () -> new ItemBase(1));

        CHIPPED_POT = PokecubeLegends.ITEMS.register("chippedpot", () -> new ItemBase(1));
        CRACKED_POT = PokecubeLegends.ITEMS.register("crackedpot", () -> new ItemBase(1));
        GALARCUFF = PokecubeLegends.ITEMS.register("galarcuff", () -> new ItemBase(1));
        GALARWREATH = PokecubeLegends.ITEMS.register("galarwreath", () -> new ItemBase(1));
        GIGANTIC_SHARD = PokecubeLegends.ITEMS.register("gigantic_shard", () -> new ItemBase("gigantic_shard", 1).setShiny());
        PARCHMENT_DARK = PokecubeLegends.ITEMS.register("pdark", () -> new ItemBase("pdark", 1, 1));
        PARCHMENT_WATER = PokecubeLegends.ITEMS.register("pwater", () -> new ItemBase("pwater", 1, 1));
        REINS_UNITY = PokecubeLegends.ITEMS.register("reins_u", () -> new ItemBase("reins_u", 1, 1));
        WISHING_PIECE = PokecubeLegends.ITEMS.register("wishing_piece", () -> new ItemBase("wishing_piece", 1));

        RAINBOW_SWORD = PokecubeLegends.ITEMS.register("rainbow_sword",
                () -> new RainbowSword(ItemTiers.RAINBOW_WING, 2, -2.4F));

        // Dimensions Creative Tab - Sorting depends on the order the items are
        // listed in
        // UltraSpace
        GOLDEN_SHROOM = PokecubeLegends.ITEMS.register("golden_shroom",
                () -> new ItemNameBlockItem(PlantsInit.GOLDEN_SHROOM_PLANT.get(),
                        new Item.Properties().food(FoodInit.GOLDEN_SHROOM)));
        GOLDEN_SWEET_BERRIES = PokecubeLegends.ITEMS.register("golden_sweet_berries",
                () -> new ItemNameBlockItem(PlantsInit.GOLDEN_SWEET_BERRY_BUSH.get(),
                        new Item.Properties().food(FoodInit.GOLDEN_SWEET_BERRIES)));
        PINK_TAINTED_LILY_PAD = PokecubeLegends.ITEMS.register("pink_blossom_tainted_lily_pad",
                () -> new PlaceOnWaterBlockItem(PlantsInit.PINK_TAINTED_LILY_PAD.get(),
                        new Item.Properties()));
        TAINTED_LILY_PAD = PokecubeLegends.ITEMS.register("tainted_lily_pad",
                () -> new PlaceOnWaterBlockItem(PlantsInit.TAINTED_LILY_PAD.get(),
                        new Item.Properties()));
        TEMPORAL_BAMBOO = PokecubeLegends.ITEMS.register("temporal_bamboo",
                () -> new TemporalBambooBlockItem(PlantsInit.TEMPORAL_BAMBOO.get(),
                        new Item.Properties()));
        DISTORTIC_VINES = PokecubeLegends.ITEMS.register("distortic_vines",
                () -> new BlockItem(PlantsInit.DISTORTIC_VINES.get(),
                        new Item.Properties()));
        // Signs
        AGED_SIGN = PokecubeLegends.ITEMS.register("aged_sign",
                () -> new SignItem(new Item.Properties().stacksTo(16), BlockInit.AGED_SIGN.get(), BlockInit.AGED_WALL_SIGN.get()));
        CONCRETE_SIGN = PokecubeLegends.ITEMS.register("concrete_sign",
                () -> new SignItem(new Item.Properties().stacksTo(16), BlockInit.CONCRETE_SIGN.get(), BlockInit.CONCRETE_WALL_SIGN.get()));
        CONCRETE_DENSE_SIGN = PokecubeLegends.ITEMS.register("concrete_dense_sign",
                () -> new SignItem(new Item.Properties().stacksTo(16), BlockInit.CONCRETE_DENSE_SIGN.get(), BlockInit.CONCRETE_DENSE_WALL_SIGN.get()));
        CORRUPTED_SIGN = PokecubeLegends.ITEMS.register("corrupted_sign",
                () -> new SignItem(new Item.Properties().stacksTo(16), BlockInit.CORRUPTED_SIGN.get(), BlockInit.CORRUPTED_WALL_SIGN.get()));
        DISTORTIC_SIGN = PokecubeLegends.ITEMS.register("distortic_sign",
                () -> new SignItem(new Item.Properties().stacksTo(16), BlockInit.DISTORTIC_SIGN.get(), BlockInit.DISTORTIC_WALL_SIGN.get()));
        INVERTED_SIGN = PokecubeLegends.ITEMS.register("inverted_sign",
                () -> new SignItem(new Item.Properties().stacksTo(16), BlockInit.INVERTED_SIGN.get(), BlockInit.INVERTED_WALL_SIGN.get()));
        MIRAGE_SIGN = PokecubeLegends.ITEMS.register("mirage_sign",
                () -> new SignItem(new Item.Properties().stacksTo(16), BlockInit.MIRAGE_SIGN.get(), BlockInit.MIRAGE_WALL_SIGN.get()));
        TEMPORAL_SIGN = PokecubeLegends.ITEMS.register("temporal_sign",
                () -> new SignItem(new Item.Properties().stacksTo(16), BlockInit.TEMPORAL_SIGN.get(), BlockInit.TEMPORAL_WALL_SIGN.get()));

        // Hanging Signs
        AGED_HANGING_SIGN = PokecubeLegends.ITEMS.register("aged_hanging_sign",
                () -> new HangingSignItem(BlockInit.AGED_HANGING_SIGN.get(), BlockInit.AGED_WALL_HANGING_SIGN.get(), new Item.Properties().stacksTo(16)));
        CONCRETE_HANGING_SIGN = PokecubeLegends.ITEMS.register("concrete_hanging_sign",
                () -> new HangingSignItem(BlockInit.CONCRETE_HANGING_SIGN.get(), BlockInit.CONCRETE_WALL_HANGING_SIGN.get(), new Item.Properties().stacksTo(16)));
        CORRUPTED_HANGING_SIGN = PokecubeLegends.ITEMS.register("corrupted_hanging_sign",
                () -> new HangingSignItem(BlockInit.CORRUPTED_HANGING_SIGN.get(), BlockInit.CORRUPTED_WALL_HANGING_SIGN.get(), new Item.Properties().stacksTo(16)));
        DISTORTIC_HANGING_SIGN = PokecubeLegends.ITEMS.register("distortic_hanging_sign",
                () -> new HangingSignItem(BlockInit.DISTORTIC_HANGING_SIGN.get(), BlockInit.DISTORTIC_WALL_HANGING_SIGN.get(), new Item.Properties().stacksTo(16)));
        INVERTED_HANGING_SIGN = PokecubeLegends.ITEMS.register("inverted_hanging_sign",
                () -> new HangingSignItem(BlockInit.INVERTED_HANGING_SIGN.get(), BlockInit.INVERTED_WALL_HANGING_SIGN.get(), new Item.Properties().stacksTo(16)));
        MIRAGE_HANGING_SIGN = PokecubeLegends.ITEMS.register("mirage_hanging_sign",
                () -> new HangingSignItem(BlockInit.MIRAGE_HANGING_SIGN.get(), BlockInit.MIRAGE_WALL_HANGING_SIGN.get(), new Item.Properties().stacksTo(16)));
        TEMPORAL_HANGING_SIGN = PokecubeLegends.ITEMS.register("temporal_hanging_sign",
                () -> new HangingSignItem(BlockInit.TEMPORAL_HANGING_SIGN.get(), BlockInit.TEMPORAL_WALL_HANGING_SIGN.get(), new Item.Properties().stacksTo(16)));

        ItemGenerator.BOATS.add(new GenericBoat.BoatRegister(BlockInit.AGED_PLANKS, "aged", PokecubeLegends.ITEMS));
        ItemGenerator.BOATS.add(new GenericBoat.BoatRegister(BlockInit.CONCRETE_PLANKS, "concrete", PokecubeLegends.ITEMS));
        ItemGenerator.BOATS.add(new GenericBoat.BoatRegister(BlockInit.CORRUPTED_PLANKS, "corrupted", PokecubeLegends.ITEMS));
        ItemGenerator.BOATS.add(new GenericBoat.BoatRegister(BlockInit.DISTORTIC_PLANKS, "distortic", PokecubeLegends.ITEMS));
        ItemGenerator.BOATS.add(new GenericBoat.BoatRegister(BlockInit.INVERTED_PLANKS, "inverted", PokecubeLegends.ITEMS));
        ItemGenerator.BOATS.add(new GenericBoat.BoatRegister(BlockInit.MIRAGE_PLANKS, "mirage", PokecubeLegends.ITEMS));
        ItemGenerator.BOATS.add(new GenericBoat.BoatRegister(BlockInit.TEMPORAL_PLANKS, "temporal", PokecubeLegends.ITEMS));

        ULTRA_HELMET = PokecubeLegends.ITEMS.register("ultra_helmet",
                () -> new UltraHelmetEffect(ItemInit.SPECTRUM, ArmorItem.Type.HELMET,
                        new Item.Properties()));
        ULTRA_CHESTPLATE = PokecubeLegends.ITEMS.register("ultra_chestplate",
                () -> new ArmorItem(ItemInit.SPECTRUM, ArmorItem.Type.CHESTPLATE,
                        new Item.Properties()));
        ULTRA_LEGGINGS = PokecubeLegends.ITEMS.register("ultra_leggings", () -> new ArmorItem(ItemInit.SPECTRUM,
                ArmorItem.Type.LEGGINGS, new Item.Properties()));
        ULTRA_BOOTS = PokecubeLegends.ITEMS.register("ultra_boots", () -> new UltraBootsEffect(ItemInit.SPECTRUM,
                ArmorItem.Type.BOOTS, new Item.Properties()));

        ULTRA_KEY = PokecubeLegends.ITEMS.register("ultrakey",
                () -> new UltraKey("ultrakey", 1));
        GIRATINA_MIRROR = PokecubeLegends.ITEMS.register("giratina_mirror",
                () -> new DistortedMirror("giratina_mirror", 1));

        DISTORTIC_WATER_BUCKET = PokecubeLegends.ITEMS.register("distortic_water_bucket",
                () -> new BucketItem(FluidInit.DISTORTIC_WATER, new Item.Properties().craftRemainder(Items.BUCKET)
                        .stacksTo(1).rarity(Rarity.RARE)));

        // Decorations Creative Tab - Sorting depends on
        // the order the items are listed in
        INFECTED_TORCH = PokecubeLegends.ITEMS.register("infected_torch",
                () -> new StandingAndWallBlockItem(BlockInit.INFECTED_TORCH.get(), BlockInit.INFECTED_TORCH_WALL.get(),
                        new Item.Properties(), Direction.DOWN));

        // Berries Creative Tab - Sorting depends on
        // the order the items are listed in
        ASPEAR_POKEPUFF = PokecubeLegends.ITEMS.register("aspear_pokepuff", () -> new ItemBase("aspear_pokepuff", 1,
                Rarity.RARE, FoodInit.REGEN_POKEPUFF, 16));
        CORNN_POKEPUFF = PokecubeLegends.ITEMS.register("cornn_pokepuff", () -> new ItemBase("cornn_pokepuff", 1,
                Rarity.UNCOMMON, FoodInit.GLOWING_POKEPUFF, 16));
        CHERI_POKEPUFF = PokecubeLegends.ITEMS.register("cheri_pokepuff", () -> new ItemBase("cheri_pokepuff", 1,
                Rarity.RARE, FoodInit.REGEN_POKEPUFF, 16));
        CHESTO_POKEPUFF = PokecubeLegends.ITEMS.register("chesto_pokepuff", () -> new ItemBase("chesto_pokepuff", 1,
                Rarity.UNCOMMON, FoodInit.REGEN_POKEPUFF, 16));
        ENIGMA_POKEPUFF = PokecubeLegends.ITEMS.register("enigma_pokepuff", () -> new ItemBase("enigma_pokepuff", 1,
                Rarity.EPIC, FoodInit.ABSORPTION_POKEPUFF, 16));
        GREPA_POKEPUFF = PokecubeLegends.ITEMS.register("grepa_pokepuff", () -> new ItemBase("grepa_pokepuff", 1,
                Rarity.UNCOMMON, FoodInit.WEAKNESS_LUCK_POKEPUFF, 16));
        HONDEW_POKEPUFF = PokecubeLegends.ITEMS.register("hondew_pokepuff", () -> new ItemBase("hondew_pokepuff", 1,
                Rarity.UNCOMMON, FoodInit.JUMP_POKEPUFF, 16));
        JABOCA_POKEPUFF = PokecubeLegends.ITEMS.register("jaboca_pokepuff", () -> new ItemBase("jaboca_pokepuff", 1,
                Rarity.UNCOMMON, FoodInit.DAMAGE_BOOST_POKEPUFF, 16));
        KELPSY_POKEPUFF = PokecubeLegends.ITEMS.register("kelpsy_pokepuff", () -> new ItemBase("kelpsy_pokepuff", 1,
                Rarity.UNCOMMON, FoodInit.HERO_WEAKNESS_POKEPUFF, 16));
        LEPPA_POKEPUFF = PokecubeLegends.ITEMS.register("leppa_pokepuff", () -> new ItemBase("leppa_pokepuff", 1,
                Rarity.UNCOMMON, FoodInit.FOOD_POKEPUFF, 16));
        LUM_POKEPUFF = PokecubeLegends.ITEMS.register("lum_pokepuff", () -> new ItemBase("lum_pokepuff", 1,
                Rarity.UNCOMMON, FoodInit.HEAL_POKEPUFF, 16));
        NANAB_POKEPUFF = PokecubeLegends.ITEMS.register("nanab_pokepuff", () -> new ItemBase("nanab_pokepuff", 1,
                Rarity.UNCOMMON, FoodInit.LUCK_POKEPUFF, 16));
        NULL_POKEPUFF = PokecubeLegends.ITEMS.register("null_pokepuff", () -> new ItemBase("null_pokepuff",
                Rarity.COMMON, FoodInit.NULL_POKEPUFF, 16));
        ORAN_POKEPUFF = PokecubeLegends.ITEMS.register("oran_pokepuff", () -> new ItemBase("oran_pokepuff", 1,
                Rarity.RARE, FoodInit.HEAL_POKEPUFF, 16));
        PECHA_POKEPUFF = PokecubeLegends.ITEMS.register("pecha_pokepuff", () -> new ItemBase("pecha_pokepuff", 1,
                Rarity.RARE, FoodInit.REGEN_POKEPUFF, 16));
        PERSIM_POKEPUFF = PokecubeLegends.ITEMS.register("persim_pokepuff", () -> new ItemBase("persim_pokepuff", 1,
                Rarity.RARE, FoodInit.REGEN_POKEPUFF, 16));
        PINAP_POKEPUFF = PokecubeLegends.ITEMS.register("pinap_pokepuff", () -> new ItemBase("pinap_pokepuff", 1,
                Rarity.UNCOMMON, FoodInit.LUCK_DAMAGE_RESIST_POKEPUFF, 16));
        POMEG_POKEPUFF = PokecubeLegends.ITEMS.register("pomeg_pokepuff", () -> new ItemBase("pomeg_pokepuff", 1,
                Rarity.EPIC, FoodInit.HERO_POISON_POKEPUFF, 16));
        QUALOT_POKEPUFF = PokecubeLegends.ITEMS.register("qualot_pokepuff", () -> new ItemBase("qualot_pokepuff", 1,
                Rarity.UNCOMMON, FoodInit.DAMAGE_BOOST_POKEPUFF, 16));
        RAWST_POKEPUFF = PokecubeLegends.ITEMS.register("rawst_pokepuff", () -> new ItemBase("rawst_pokepuff", 1,
                Rarity.UNCOMMON, FoodInit.FIRE_RESISTANCE_POKEPUFF, 16));
        ROWAP_POKEPUFF = PokecubeLegends.ITEMS.register("rowap_pokepuff", () -> new ItemBase("rowap_pokepuff", 1,
                Rarity.UNCOMMON, FoodInit.DAMAGE_BOOST_POKEPUFF, 16));
        SITRUS_POKEPUFF = PokecubeLegends.ITEMS.register("sitrus_pokepuff", () -> new ItemBase("sitrus_pokepuff", 1,
                Rarity.RARE, FoodInit.REGEN_POKEPUFF, 16));
        TAMATO_POKEPUFF = PokecubeLegends.ITEMS.register("tamato_pokepuff", () -> new ItemBase("tamato_pokepuff", 1,
                Rarity.UNCOMMON, FoodInit.FIRE_RESISTANCE_POKEPUFF, 16));
    }

    public static final ArmorMaterial SPECTRUM = new ArmorMaterial()
    {
        @Override
        public int getDurabilityForType(final ArmorItem.Type armorSlot)
        {
            return new int[]
            { 13, 15, 16, 11 }[armorSlot.getSlot().getIndex()] * 25;
        }

        @Override
        public int getDefenseForType(final ArmorItem.Type armorSlot)
        {
            return new int[]
            { 2, 5, 6, 2 }[armorSlot.getSlot().getIndex()];
        }

        @Override
        public int getEnchantmentValue()
        {
            return 9;
        }

        @Override
        public net.minecraft.sounds.SoundEvent getEquipSound()
        {
            return SoundEvents.ARMOR_EQUIP_NETHERITE;
        }

        @Override
        public Ingredient getRepairIngredient()
        {
            return Ingredient.of(ItemInit.SPECTRUM_SHARD.get());
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public String getName()
        {
            return Reference.ID + ":ultra";
        }

        @Override
        public float getToughness()
        {
            return 1.5f;
        }

        @Override
        public float getKnockbackResistance()
        {
            return 2;
        }
    };

    public static void init()
    {}

    // Properties
    @OnlyIn(Dist.CLIENT)
    public static void addItemModelProperties()
    {
        RenderPokecube.ModelPokecube.CUBE_SHIFTS.put(new ResourceLocation("pokecube", "dynacube"), -0.1875f);

        ItemProperties.register(ItemInit.ZAMAZENTA_SHIELD.get(), new ResourceLocation("blocking"), (stack, world,
                entity, intu) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
    }

    // Nature Item
    public static void addMint()
    {
        for (final Nature type : Nature.values())
        {
            ItemInit.mints.put(type, PokecubeLegends.ITEMS.register("mint_" + type.name().toLowerCase(Locale.ROOT), () -> new ItemNature(type)));
        }
    }

    // Z-Crystal Item
    public static void addZCrystal()
    {
        for (final PokeType type : PokeType.values())
        {
            String name = type.name.equals("???") ? "unknown" : type.name;
            name = "z_" + name.toLowerCase(Locale.ROOT);
            ItemInit.zCrystals.put(type, PokecubeLegends.ITEMS.register(name, () -> new ItemZCrystal(type)));
        }
    }

    public static void registerItems()
    {
        ItemInit.addMint();
        ItemInit.addZCrystal();
    }
}
