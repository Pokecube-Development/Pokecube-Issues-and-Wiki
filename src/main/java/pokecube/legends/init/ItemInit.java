package pokecube.legends.init;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fmllegacy.RegistryObject;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.Nature;
import pokecube.core.utils.PokeType;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.Reference;
import pokecube.legends.items.DistortedMirror;
import pokecube.legends.items.GiganticShard;
import pokecube.legends.items.ItemBase;
import pokecube.legends.items.ItemTiers;
import pokecube.legends.items.LegendsSword;
import pokecube.legends.items.RainbowSword;
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

    public static final RegistryObject<Item> GRAY_SCARF;
    public static final RegistryObject<Item> WOODEN_CROWN;

    // Gens_ores
    public static final RegistryObject<Item> SAPPHIRE;
    public static final RegistryObject<Item> RUBY;

    // Forms
    public static final RegistryObject<Item> SILVER_WING;
    public static final RegistryObject<Item> RAINBOW_WING;
    public static final RegistryObject<Item> CRYSTAL_SHARD;
    public static final RegistryObject<Item> GRISEOUS_ORB;
    public static final RegistryObject<Item> ZYGARDE_CUBE;
    public static final RegistryObject<Item> PRISION_BOTTLE;
    public static final RegistryObject<Item> REVEAL_GLASS;
    public static final RegistryObject<Item> DNA_SPLICERA;
    public static final RegistryObject<Item> DNA_SPLICERB;
    public static final RegistryObject<Item> GRACIDEA;
    public static final RegistryObject<Item> METEORITE;
    public static final RegistryObject<Item> NSUN;
    public static final RegistryObject<Item> NMOON;
    public static final RegistryObject<Item> AZURE_FLUTE;
    public static final RegistryObject<Item> RSHIELD;
    public static final RegistryObject<Item> RSWORD;
    public static final RegistryObject<Item> REINS_U;

    // Evolutions
    public static final RegistryObject<Item> CHPOT;
    public static final RegistryObject<Item> CRPOT;
    public static final RegistryObject<Item> GALARCUFF;
    public static final RegistryObject<Item> PDARK;
    public static final RegistryObject<Item> PWATER;
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
    public static final RegistryObject<Item> SPECTRUM_SHARD;
    public static final RegistryObject<Item> ULTRAKEY;
    public static final RegistryObject<Item> COSMIC_DUST;
    public static final RegistryObject<Item> FRACTAL_SHARD;

    //Giratina
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

    // Foods
    public static final RegistryObject<Item> POKEPUFF_GRASS;
    public static final RegistryObject<Item> POKEPUFF_FIRE;
    public static final RegistryObject<Item> POKEPUFF_WATER;
    public static final RegistryObject<Item> POKEPUFF_DARK;
    public static final RegistryObject<Item> POKEPUFF_STEEL;
    public static final RegistryObject<Item> POKEPUFF_FLYING;
    public static final RegistryObject<Item> POKEPUFF_GHOST;
    public static final RegistryObject<Item> POKEPUFF_BUG;
    public static final RegistryObject<Item> POKEPUFF_FIGHTING;
    public static final RegistryObject<Item> POKEPUFF_PSYCHIC;
    public static final RegistryObject<Item> POKEPUFF_ICE;
    public static final RegistryObject<Item> POKEPUFF_FAIRY;
    public static final RegistryObject<Item> POKEPUFF_DRAGON;
    public static final RegistryObject<Item> POKEPUFF_GROUND;
    public static final RegistryObject<Item> POKEPUFF_ROCK;
    public static final RegistryObject<Item> POKEPUFF_NORMAL;
    public static final RegistryObject<Item> POKEPUFF_ELECTRIC;
    public static final RegistryObject<Item> POKEPUFF_POISON;

    static
    {
        // Keys

    	// Orbs
        BLUE_ORB = PokecubeLegends.ITEMS.register("blueorb", () -> new ItemBase("blueorb", 1,PokecubeLegends.LEGEND_TAB));
        GREEN_ORB = PokecubeLegends.ITEMS.register("greenorb", () -> new ItemBase("greenorb", 1,PokecubeLegends.LEGEND_TAB));
        RED_ORB = PokecubeLegends.ITEMS.register("redorb", () -> new ItemBase("redorb", 1,PokecubeLegends.LEGEND_TAB));
        GRAY_ORB = PokecubeLegends.ITEMS.register("grayorb", () -> new ItemBase(1,PokecubeLegends.LEGEND_TAB));
        RAINBOW_ORB = PokecubeLegends.ITEMS.register("legendaryorb", () -> new ItemBase("legendaryorb", 1, PokecubeLegends.LEGEND_TAB).setShiny());
        LUSTROUS_ORB = PokecubeLegends.ITEMS.register("lustrousorb", () -> new ItemBase("lustrousorb", 1,PokecubeLegends.LEGEND_TAB));
        ADAMANT_ORB = PokecubeLegends.ITEMS.register("adamantorb", () -> new ItemBase("adamantorb", 1,PokecubeLegends.LEGEND_TAB));
        OCEAN_ORB = PokecubeLegends.ITEMS.register("oceanorb", () -> new ItemBase("oceanorb", 1,PokecubeLegends.LEGEND_TAB));
        LIFE_ORB = PokecubeLegends.ITEMS.register("lifeorb", () -> new ItemBase("lifeorb", 1,PokecubeLegends.LEGEND_TAB));
        DESTRUCT_ORB = PokecubeLegends.ITEMS.register("destructorb", () -> new ItemBase("destructorb", 1,PokecubeLegends.LEGEND_TAB));
        REGIS_ORB = PokecubeLegends.ITEMS.register("regisorb", () -> new ItemBase("regisorb", 1,PokecubeLegends.LEGEND_TAB));
        KOKO_ORB = PokecubeLegends.ITEMS.register("koko_orb", () -> new ItemBase("koko_orb", 1,PokecubeLegends.LEGEND_TAB));
        BULU_ORB = PokecubeLegends.ITEMS.register("bulu_orb", () -> new ItemBase("bulu_orb", 1,PokecubeLegends.LEGEND_TAB));
        LELE_ORB = PokecubeLegends.ITEMS.register("lele_orb", () -> new ItemBase("lele_orb", 1,PokecubeLegends.LEGEND_TAB));
        FINI_ORB = PokecubeLegends.ITEMS.register("fini_orb", () -> new ItemBase("fini_orb", 1,PokecubeLegends.LEGEND_TAB));
        GRISEOUS_ORB = PokecubeLegends.ITEMS.register("griseousorb", () -> new ItemBase("griseousorb", 1,PokecubeLegends.LEGEND_TAB));
        COSMIC_ORB = PokecubeLegends.ITEMS.register("cosmic_orb", () -> new ItemBase("cosmic_orb", 1,PokecubeLegends.LEGEND_TAB));
        SOUL_HEART = PokecubeLegends.ITEMS.register("soul_heart", () -> new ItemBase("soul_heart", 1,PokecubeLegends.LEGEND_TAB));
        SOUL_DEW = PokecubeLegends.ITEMS.register("soul_dew", () -> new ItemBase("soul_dew", 1,PokecubeLegends.LEGEND_TAB));

        // Gem
        FLAME_GEM = PokecubeLegends.ITEMS.register("flame_gem", () -> new ItemBase("flame_gem", 1,PokecubeLegends.LEGEND_TAB));
        THUNDER_GEM = PokecubeLegends.ITEMS.register("thunder_gem", () -> new ItemBase("thunder_gem", 1,PokecubeLegends.LEGEND_TAB));
        WATER_GEM = PokecubeLegends.ITEMS.register("water_gem", () -> new ItemBase("water_gem", 1,PokecubeLegends.LEGEND_TAB));
        AZELF_GEM = PokecubeLegends.ITEMS.register("azelf_gem", () -> new ItemBase("azelf_gem", 1,PokecubeLegends.LEGEND_TAB));
        MESPRIT_GEM = PokecubeLegends.ITEMS.register("mesprit_gem", () -> new ItemBase("mesprit_gem", 1,PokecubeLegends.LEGEND_TAB));
        UXIE_GEM = PokecubeLegends.ITEMS.register("uxie_gem", () -> new ItemBase("uxie_gem", 1,PokecubeLegends.LEGEND_TAB));
        DIAMOND_GEM = PokecubeLegends.ITEMS.register("diamond_gem", () -> new ItemBase("diamond_gem", 1,PokecubeLegends.LEGEND_TAB));


        // Stones
        LIGHT_STONE = PokecubeLegends.ITEMS.register("lightstone", () -> new ItemBase("lightstone", 1,PokecubeLegends.LEGEND_TAB));
        DARK_STONE = PokecubeLegends.ITEMS.register("darkstone", () -> new ItemBase("darkstone", 1,PokecubeLegends.LEGEND_TAB));
        ROCK_CORE = PokecubeLegends.ITEMS.register("rockcore", () -> new ItemBase("rockcore", 1,PokecubeLegends.LEGEND_TAB));
        ANCIENT_STONE = PokecubeLegends.ITEMS.register("ancient_stone", () -> new ItemBase("ancient_stone", 1,PokecubeLegends.LEGEND_TAB));


        // Cores
        ICE_CORE = PokecubeLegends.ITEMS.register("icecore", () -> new ItemBase("icecore", 1,PokecubeLegends.LEGEND_TAB));
        STEEL_CORE = PokecubeLegends.ITEMS.register("steelcore", () -> new ItemBase("steelcore", 1,PokecubeLegends.LEGEND_TAB));
        EMBLEM = PokecubeLegends.ITEMS.register("emblem", () -> new ItemBase("emblem", 1,PokecubeLegends.LEGEND_TAB));
        MAGMA_CORE = PokecubeLegends.ITEMS.register("magmacore", () -> new ItemBase("magmacore", 1,PokecubeLegends.LEGEND_TAB));
        THUNDER_CORE = PokecubeLegends.ITEMS.register("thundercore", () -> new ItemBase("thundercore", 1,PokecubeLegends.LEGEND_TAB) );
        DRAGO_CORE   = PokecubeLegends.ITEMS.register("dragocore", () -> new ItemBase("dragocore", 1,PokecubeLegends.LEGEND_TAB) );
        STAR_CORE   = PokecubeLegends.ITEMS.register("star_core", () -> new ItemBase("star_core", 1,PokecubeLegends.LEGEND_TAB) );
        STEAM_CORE = PokecubeLegends.ITEMS.register("steam_core", () -> new ItemBase("steam_core", 1,PokecubeLegends.LEGEND_TAB));
        KYUREM_CORE = PokecubeLegends.ITEMS.register("kyurem_core", () -> new ItemBase("kyurem_core", 1,PokecubeLegends.LEGEND_TAB));

        // Runes
        ORANGE_RUNE = PokecubeLegends.ITEMS.register("orange_rune", () -> new ItemBase("orange_rune", 1, PokecubeLegends.LEGEND_TAB) );
        BLUE_RUNE = PokecubeLegends.ITEMS.register("blue_rune", () -> new ItemBase("blue_rune", 1, PokecubeLegends.LEGEND_TAB) );
        GREEN_RUNE = PokecubeLegends.ITEMS.register("green_rune", () -> new ItemBase("green_rune", 1, PokecubeLegends.LEGEND_TAB) );


        //Wings
        SILVER_WING = PokecubeLegends.ITEMS.register("silver_wing", () -> new ItemBase("silver_wing", 5,PokecubeItems.POKECUBEITEMS));
        RAINBOW_WING = PokecubeLegends.ITEMS.register("rainbow_wing", () -> new ItemBase("rainbow_wing", 5,PokecubeItems.POKECUBEITEMS).setShiny());
        FIRE_WING = PokecubeLegends.ITEMS.register("fire_wing", () -> new ItemBase("fire_wing", 1, PokecubeLegends.LEGEND_TAB) );
        DARK_FIRE_WING = PokecubeLegends.ITEMS.register("dark_fire_wing", () -> new ItemBase("dark_fire_wing", 1, PokecubeLegends.LEGEND_TAB) );
        ELECTRIC_WING = PokecubeLegends.ITEMS.register("electric_wing", () -> new ItemBase("electric_wing", 1, PokecubeLegends.LEGEND_TAB) );
        STATIC_WING = PokecubeLegends.ITEMS.register("static_wing", () -> new ItemBase("static_wing", 1, PokecubeLegends.LEGEND_TAB) );
        ICE_WING = PokecubeLegends.ITEMS.register("ice_wing", () -> new ItemBase("ice_wing", 1, PokecubeLegends.LEGEND_TAB) );
        ICE_DARK_WING = PokecubeLegends.ITEMS.register("ice_dark_wing", () -> new ItemBase("ice_dark_wing", 1, PokecubeLegends.LEGEND_TAB) );
        LUNAR_WING = PokecubeLegends.ITEMS.register("lunar_wing", () -> new ItemBase("lunar_wing", 1,PokecubeLegends.LEGEND_TAB));

        // Misc
        WOODEN_CROWN = PokecubeLegends.ITEMS.register("wooden_crown", () -> new ItemBase("wooden_crown", 1,PokecubeLegends.LEGEND_TAB));
        GRAY_SCARF = PokecubeLegends.ITEMS.register("kubfu_spawn", () -> new ItemBase("kubfu_spawn", 1,PokecubeLegends.LEGEND_TAB));
        METEOR_SHARD   = PokecubeLegends.ITEMS.register("meteor_shard", () -> new ItemBase("meteor_shard", 1,PokecubeLegends.LEGEND_TAB));
        LIGHTING_CRYSTAL   = PokecubeLegends.ITEMS.register("lighting_crystal", () -> new ItemBase("lighting_crystal", 1,PokecubeLegends.LEGEND_TAB));
        MANAPHY_NECKLACE = PokecubeLegends.ITEMS.register("manaphy_necklace", () -> new ItemBase("manaphy_necklace", 1,PokecubeLegends.LEGEND_TAB));
        NIGHTMARE_BOOK = PokecubeLegends.ITEMS.register("nightmare_book", () -> new ItemBase("nightmare_book", 1,PokecubeLegends.LEGEND_TAB));
        MELOETTA_OCARINA = PokecubeLegends.ITEMS.register("meloetta_ocarina", () -> new ItemBase("meloetta_ocarina", 1,PokecubeLegends.LEGEND_TAB));

        ZYGARDE_CUBE = PokecubeLegends.ITEMS.register("zygardecube", () -> new ItemBase("zygardecube", 1,PokecubeLegends.LEGEND_TAB));
        PRISION_BOTTLE = PokecubeLegends.ITEMS.register("prisonbottle", () -> new ItemBase("prisonbottle", 1,PokecubeItems.POKECUBEITEMS));
        REVEAL_GLASS = PokecubeLegends.ITEMS.register("revealglass", () -> new ItemBase("revealglass", 1,PokecubeItems.POKECUBEITEMS));
        DNA_SPLICERA = PokecubeLegends.ITEMS.register("dna_splicera", () -> new ItemBase("dna_splicers", 1,PokecubeItems.POKECUBEITEMS));
        DNA_SPLICERB = PokecubeLegends.ITEMS.register("dna_splicerb", () -> new ItemBase("dna_splicers", 1,PokecubeItems.POKECUBEITEMS));
        GRACIDEA = PokecubeLegends.ITEMS.register("gracidea", () -> new ItemBase("gracidea", 10,PokecubeItems.POKECUBEITEMS));
        METEORITE = PokecubeLegends.ITEMS.register("meteorite", () -> new ItemBase("meteorite", 16,PokecubeItems.POKECUBEITEMS));
        NSUN = PokecubeLegends.ITEMS.register("n_sun", () -> new ItemBase("n_sun", 1,PokecubeItems.POKECUBEITEMS));
        NMOON = PokecubeLegends.ITEMS.register("n_moon", () -> new ItemBase("n_moon", 1,PokecubeItems.POKECUBEITEMS));
        AZURE_FLUTE = PokecubeLegends.ITEMS.register("azure_flute", () -> new ItemBase("azure_flute", 1, PokecubeLegends.LEGEND_TAB));
        RSHIELD = PokecubeLegends.ITEMS.register("rustedshield", () -> new ItemBase("rustedshield", 1, PokecubeLegends.LEGEND_TAB));
        RSWORD = PokecubeLegends.ITEMS.register("rustedsword", () -> new ItemBase("rustedsword", 1, PokecubeLegends.LEGEND_TAB));

        CHPOT = PokecubeLegends.ITEMS.register("chippedpot", () -> new ItemBase(1,PokecubeItems.POKECUBEITEMS));
        CRPOT = PokecubeLegends.ITEMS.register("crackedpot", () -> new ItemBase(1,PokecubeItems.POKECUBEITEMS));
        GALARCUFF = PokecubeLegends.ITEMS.register("galarcuff", () -> new ItemBase(1,PokecubeItems.POKECUBEITEMS));
        PDARK = PokecubeLegends.ITEMS.register("pdark", () -> new ItemBase("pdark", 1,PokecubeItems.POKECUBEITEMS));
        PWATER = PokecubeLegends.ITEMS.register("pwater", () -> new ItemBase("pwater", 1,PokecubeItems.POKECUBEITEMS));
        REINS_U = PokecubeLegends.ITEMS.register("reins_u", () -> new ItemBase("reins_u", 1,PokecubeItems.POKECUBEITEMS));
        GALARWREATH = PokecubeLegends.ITEMS.register("galarwreath", () -> new ItemBase(1,PokecubeItems.POKECUBEITEMS));

        WISHING_PIECE = PokecubeLegends.ITEMS.register("wishing_piece", () -> new ItemBase("wishing_piece", 1,PokecubeItems.POKECUBEITEMS));
        GIGANTIC_SHARD = PokecubeLegends.ITEMS.register("gigantic_shard", () -> new GiganticShard("gigantic_shard", 1));

        RAINBOW_SWORD = PokecubeLegends.ITEMS.register("rainbow_sword", () -> new RainbowSword(ItemTiers.RAINBOW_WING,
        		2, -2.4F, PokecubeItems.POKECUBEITEMS));
        ICE_CARROT   = PokecubeLegends.ITEMS.register("ice_carrot", () -> new ItemBase("ice_carrot", 1,PokecubeLegends.LEGEND_TAB));
        SHADOW_CARROT   = PokecubeLegends.ITEMS.register("shadow_carrot", () -> new ItemBase("shadow_carrot", 1,PokecubeLegends.LEGEND_TAB));
        IMPRISIONMENT_HELMET   = PokecubeLegends.ITEMS.register("imprisonment_helmet", () -> new ItemBase("imprisonment_helmet", 1,PokecubeLegends.LEGEND_TAB));

        // Swords
        KELDEO_SWORD = PokecubeLegends.ITEMS.register("keldeo_sword", () -> new LegendsSword(ItemInit.MATERIAL_JUSTISE,
            2, -2.4F, new Item.Properties(), PokecubeLegends.LEGEND_TAB).setTooltipName("keldeo_sword").setShiny());
        TERRAKION_SWORD = PokecubeLegends.ITEMS.register("terrakion_sword", () -> new LegendsSword(ItemInit.MATERIAL_JUSTISE,
            2, -2.4F, new Item.Properties(), PokecubeLegends.LEGEND_TAB).setTooltipName("terrakion_sword"));
        VIRIZION_SWORD = PokecubeLegends.ITEMS.register("virizion_sword", () -> new LegendsSword(ItemInit.MATERIAL_JUSTISE,
            3, -2.4F, new Item.Properties(), PokecubeLegends.LEGEND_TAB).setTooltipName("virizion_sword"));
        COBALION_SWORD = PokecubeLegends.ITEMS.register("cobalion_sword", () -> new LegendsSword(ItemInit.MATERIAL_JUSTISE,
            2, -2.4F, new Item.Properties(), PokecubeLegends.LEGEND_TAB).setTooltipName("cobalion_sword"));
        ZACIAN_SWORD = PokecubeLegends.ITEMS.register("zacian_sword", () -> new LegendsSword(Tiers.NETHERITE,
            3, -2.4F, new Item.Properties().fireResistant(), PokecubeLegends.LEGEND_TAB).setTooltipName("zacian_sword"));

        //Shields
        ZAMAZENTA_SHIELD = PokecubeLegends.ITEMS.register("zamazenta_shield", () -> new ZamazentaShieldItem(Tiers.NETHERITE,"zamazenta_shield",
        		new Item.Properties().durability(200).tab(PokecubeLegends.LEGEND_TAB).fireResistant()));


        // Ores
        SAPPHIRE = PokecubeLegends.ITEMS.register("sapphire", () -> new ItemBase(64,PokecubeItems.POKECUBEITEMS));
        RUBY = PokecubeLegends.ITEMS.register("ruby", () -> new ItemBase(64,PokecubeItems.POKECUBEITEMS));
        SPECTRUM_SHARD = PokecubeLegends.ITEMS.register("spectrum_shard", () -> new ItemBase(64, PokecubeItems.POKECUBEITEMS));


        // Dimensions
        // UltraSpace
        CRYSTAL_SHARD = PokecubeLegends.ITEMS.register("crystal_shard", () -> new ItemBase(35,PokecubeItems.POKECUBEITEMS));
        ULTRAKEY 		= PokecubeLegends.ITEMS.register("ultrakey", () -> new UltraKey("ultrakey",1) );
        COSMIC_DUST 	= PokecubeLegends.ITEMS.register("cosmic_dust", () -> new ItemBase(30, PokecubeItems.POKECUBEITEMS));
        FRACTAL_SHARD 	= PokecubeLegends.ITEMS.register("fractal_shard", () -> new ItemBase(64, PokecubeItems.POKECUBEITEMS));

        ULTRA_HELMET = PokecubeLegends.ITEMS.register("ultra_helmet", () -> new UltraHelmetEffect(
                ItemInit.armormaterial, EquipmentSlot.HEAD, new Item.Properties().tab(PokecubeLegends.TAB)));
        ULTRA_CHESTPLATE = PokecubeLegends.ITEMS.register("ultra_chestplate", () -> new ArmorItem(
                ItemInit.armormaterial, EquipmentSlot.CHEST, new Item.Properties().tab(PokecubeLegends.TAB)));
        ULTRA_LEGGINGS = PokecubeLegends.ITEMS.register("ultra_leggings", () -> new ArmorItem(
                ItemInit.armormaterial, EquipmentSlot.LEGS, new Item.Properties().tab(PokecubeLegends.TAB)));
        ULTRA_BOOTS = PokecubeLegends.ITEMS.register("ultra_boots", () -> new UltraBootsEffect(
                ItemInit.armormaterial, EquipmentSlot.FEET, new Item.Properties().tab(PokecubeLegends.TAB)));

        //Distortic World
        GIRATINA_MIRROR = PokecubeLegends.ITEMS.register("giratina_mirror", () -> new DistortedMirror("giratina_mirror", 1));
        HEAD_MIRROR = PokecubeLegends.ITEMS.register("head_mirror", () -> new ItemBase(1, PokecubeItems.POKECUBEITEMS));
        BODY_MIRROR = PokecubeLegends.ITEMS.register("body_mirror", () -> new ItemBase(1, PokecubeItems.POKECUBEITEMS));
        GLASS_MIRROR = PokecubeLegends.ITEMS.register("glass_mirror", () -> new ItemBase(1, PokecubeItems.POKECUBEITEMS));

        // Torch
        INFECTED_TORCH = PokecubeLegends.ITEMS.register("ultra_torch1", () -> new StandingAndWallBlockItem(BlockInit.INFECTED_TORCH
                .get(), BlockInit.INFECTED_TORCH_WALL.get(), new Item.Properties().tab(PokecubeLegends.TAB)));

        // Plants
        DISTORTIC_VINES = PokecubeLegends.ITEMS.register("distortic_vines", () -> new BlockItem(BlockInit.DISTORTIC_VINES.get(),
        		new Item.Properties().tab(PokecubeLegends.TAB)));

        // Foods
        POKEPUFF_GRASS = PokecubeLegends.ITEMS.register("pokepuff_grass", () -> new ItemBase("pokepuff_grass", 32, PokecubeItems.POKECUBEBERRIES, FoodInit.POKEPUFF_GREEN));
        POKEPUFF_FIRE = PokecubeLegends.ITEMS.register("pokepuff_fire", () -> new ItemBase("pokepuff_fire", 32, PokecubeItems.POKECUBEBERRIES, FoodInit.POKEPUFF_GREEN));
        POKEPUFF_WATER = PokecubeLegends.ITEMS.register("pokepuff_water", () -> new ItemBase("pokepuff_water", 32, PokecubeItems.POKECUBEBERRIES, FoodInit.POKEPUFF_GREEN));
        POKEPUFF_DARK = PokecubeLegends.ITEMS.register("pokepuff_dark", () -> new ItemBase("pokepuff_dark", 32, PokecubeItems.POKECUBEBERRIES, FoodInit.POKEPUFF_BLUE));
        POKEPUFF_STEEL = PokecubeLegends.ITEMS.register("pokepuff_steel", () -> new ItemBase("pokepuff_steel", 32, PokecubeItems.POKECUBEBERRIES, FoodInit.POKEPUFF_BLUE));
        POKEPUFF_PSYCHIC = PokecubeLegends.ITEMS.register("pokepuff_psychic", () -> new ItemBase("pokepuff_psychic", 32, PokecubeItems.POKECUBEBERRIES, FoodInit.POKEPUFF_BLUE));
        POKEPUFF_GHOST = PokecubeLegends.ITEMS.register("pokepuff_ghost", () -> new ItemBase("pokepuff_ghost", 32, PokecubeItems.POKECUBEBERRIES, FoodInit.POKEPUFF_ORANGE));
        POKEPUFF_GROUND = PokecubeLegends.ITEMS.register("pokepuff_ground", () -> new ItemBase("pokepuff_ground", 32, PokecubeItems.POKECUBEBERRIES, FoodInit.POKEPUFF_ORANGE));
        POKEPUFF_ROCK = PokecubeLegends.ITEMS.register("pokepuff_rock", () -> new ItemBase("pokepuff_rock", 32, PokecubeItems.POKECUBEBERRIES, FoodInit.POKEPUFF_ORANGE));
        POKEPUFF_FAIRY = PokecubeLegends.ITEMS.register("pokepuff_fairy", () -> new ItemBase("pokepuff_fairy", 32, PokecubeItems.POKECUBEBERRIES, FoodInit.POKEPUFF_BROWN));
        POKEPUFF_FIGHTING = PokecubeLegends.ITEMS.register("pokepuff_fighting", () -> new ItemBase("pokepuff_fighting", 32, PokecubeItems.POKECUBEBERRIES, FoodInit.POKEPUFF_BROWN));
        POKEPUFF_FLYING = PokecubeLegends.ITEMS.register("pokepuff_flying", () -> new ItemBase("pokepuff_flying", 32, PokecubeItems.POKECUBEBERRIES, FoodInit.POKEPUFF_BROWN));
    	POKEPUFF_BUG = PokecubeLegends.ITEMS.register("pokepuff_bug", () -> new ItemBase("pokepuff_bug", 32, PokecubeItems.POKECUBEBERRIES, FoodInit.POKEPUFF_PINK));
       	POKEPUFF_ELECTRIC = PokecubeLegends.ITEMS.register("pokepuff_electric", () -> new ItemBase("pokepuff_electric", 32, PokecubeItems.POKECUBEBERRIES, FoodInit.POKEPUFF_PINK));
       	POKEPUFF_POISON = PokecubeLegends.ITEMS.register("pokepuff_poison", () -> new ItemBase("pokepuff_poison", 32, PokecubeItems.POKECUBEBERRIES, FoodInit.POKEPUFF_PINK));
       	POKEPUFF_DRAGON = PokecubeLegends.ITEMS.register("pokepuff_dragon", () -> new ItemBase("pokepuff_dragon", 32, PokecubeItems.POKECUBEBERRIES, FoodInit.POKEPUFF_BLUE));
       	POKEPUFF_NORMAL = PokecubeLegends.ITEMS.register("pokepuff_normal", () -> new ItemBase("pokepuff_normal", 32, PokecubeItems.POKECUBEBERRIES, FoodInit.POKEPUFF_ORANGE));
       	POKEPUFF_ICE = PokecubeLegends.ITEMS.register("pokepuff_ice", () -> new ItemBase("pokepuff_ice", 32, PokecubeItems.POKECUBEBERRIES, FoodInit.POKEPUFF_GREEN));

    }

    public static final ArmorMaterial armormaterial = new ArmorMaterial()
    {
        @Override
        public int getDurabilityForSlot(final EquipmentSlot slot)
        {
            return new int[] { 13, 15, 16, 11 }[slot.getIndex()] * 25;
        }

        @Override
        public int getDefenseForSlot(final EquipmentSlot slot)
        {
            return new int[] { 2, 5, 6, 2 }[slot.getIndex()];
        }

        @Override
        public int getEnchantmentValue()
        {
            return 9;
        }

        @Override
        public net.minecraft.sounds.SoundEvent getEquipSound()
        {
            return SoundEvents.ZOMBIE_ATTACK_IRON_DOOR;
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
		public float getKnockbackResistance() {
			return 2;
		}
    };

    public static void init() {}

    //Properties
    @OnlyIn(Dist.CLIENT)
    public static void addItemModelProperties() {
        ItemProperties.register(ItemInit.ZAMAZENTA_SHIELD.get(), new ResourceLocation("blocking"), (stack, world,
                entity, intu) ->
    	entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
    }

    // Nature Item
    public static void addMint(final RegistryEvent.Register<Item> event)
    {
        for (final Nature type : Nature.values())
        {
            final Item mind = new ItemNature(type);
            event.getRegistry().register(mind);
        }
    }

    // Z-Crystal Item
    public static void addZCrystal(final RegistryEvent.Register<Item> event)
    {
        for (final PokeType type : PokeType.values())
        {
            final Item crystal = new ItemZCrystal(type);
            event.getRegistry().register(crystal);
        }
    }

    public static void registerItems(final RegistryEvent.Register<Item> event)
    {
        ItemInit.addMint(event);
        ItemInit.addZCrystal(event);
    }
}
