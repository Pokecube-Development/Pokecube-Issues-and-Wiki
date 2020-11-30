package pokecube.legends.init;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemTier;
import net.minecraft.item.WallOrFloorItem;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.RegistryObject;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.Nature;
import pokecube.core.utils.PokeType;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.Reference;
import pokecube.legends.items.DistortedMirror;
import pokecube.legends.items.GiganticShard;
import pokecube.legends.items.ItemBase;
import pokecube.legends.items.KeldeoSword;
import pokecube.legends.items.LegendaryOrb;
import pokecube.legends.items.RainbowSword;
import pokecube.legends.items.UltraKey;
import pokecube.legends.items.armor.UltraBootsEffect;
import pokecube.legends.items.armor.UltraHelmetEffect;
import pokecube.legends.items.natureedit.ItemNature;
import pokecube.legends.items.zmove.ItemZCrystal;

public class ItemInit
{
    // Materials
    public static final IItemTier MATERIAL_RAINBOW = ItemTier.DIAMOND;
    public static final IItemTier MATERIAL_JUSTISE = ItemTier.DIAMOND;

    // Keys
    public static final RegistryObject<Item> BLUEORB;
    public static final RegistryObject<Item> GREENORB;
    public static final RegistryObject<Item> REDORB;
    public static final RegistryObject<Item> GRAYORB;
    public static final RegistryObject<Item> LEGENDARYORB;
    public static final RegistryObject<Item> LUSTROUSORB;
    public static final RegistryObject<Item> ADAMANTORB;
    public static final RegistryObject<Item> OCEANORB;
    public static final RegistryObject<Item> LIGHTSTONE;
    public static final RegistryObject<Item> DARKSTONE;
    public static final RegistryObject<Item> ROCKCORE;
    public static final RegistryObject<Item> ICECORE;
    public static final RegistryObject<Item> STEELCORE;
    public static final RegistryObject<Item> EMBLEM;
    public static final RegistryObject<Item> MAGMA_CORE;
    public static final RegistryObject<Item> LIFEORB;
    public static final RegistryObject<Item> DESTRUCTORB;
    public static final RegistryObject<Item> ORANGE_RUNE;
    public static final RegistryObject<Item> BLUE_RUNE;
    public static final RegistryObject<Item> GREEN_RUNE;
    public static final RegistryObject<Item> REGIS_ORB;
    public static final RegistryObject<Item> THUNDERCORE;
    public static final RegistryObject<Item> DRAGOCORE;
    public static final RegistryObject<Item> ICE_CARROT;
    public static final RegistryObject<Item> SHADOW_CARROT;
    public static final RegistryObject<Item> ANCIENT_STONE;
    public static final RegistryObject<Item> IMPRISIONMENT_HELMET;

    // Gens_ores
    public static final RegistryObject<Item> SAPPHIRE;
    public static final RegistryObject<Item> RUBY;

    // Extra
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
    public static final RegistryObject<Item> GENE_FOSSIL;
    
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
    
    // Dimensions
    public static final RegistryObject<Item> SPECTRUM_SHARD;
    public static final RegistryObject<Item> ULTRAKEY;
    public static final RegistryObject<Item> COSMIC_DUST;
    
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
    public static final RegistryObject<Item> TORCH01;

    static
    {
        // Keys
        BLUEORB = PokecubeLegends.ITEMS.register("blueorb", () -> new ItemBase("blueorb", 1,PokecubeItems.POKECUBEITEMS));
        GREENORB = PokecubeLegends.ITEMS.register("greenorb", () -> new ItemBase("greenorb", 1,PokecubeItems.POKECUBEITEMS));
        REDORB = PokecubeLegends.ITEMS.register("redorb", () -> new ItemBase("redorb", 1,PokecubeItems.POKECUBEITEMS));
        GRAYORB = PokecubeLegends.ITEMS.register("grayorb", () -> new ItemBase("grayorb", 1,PokecubeItems.POKECUBEITEMS).noTooltop());
        LEGENDARYORB = PokecubeLegends.ITEMS.register("legendaryorb", () -> new LegendaryOrb("legendaryorb", 1));
        LUSTROUSORB = PokecubeLegends.ITEMS.register("lustrousorb", () -> new ItemBase("lustrousorb", 1,PokecubeItems.POKECUBEITEMS));
        ADAMANTORB = PokecubeLegends.ITEMS.register("adamantorb", () -> new ItemBase("adamantorb", 1,PokecubeItems.POKECUBEITEMS));
        OCEANORB = PokecubeLegends.ITEMS.register("oceanorb", () -> new ItemBase("oceanorb", 1,PokecubeItems.POKECUBEITEMS));
        LIGHTSTONE = PokecubeLegends.ITEMS.register("lightstone", () -> new ItemBase("lightstone", 1,PokecubeItems.POKECUBEITEMS));
        DARKSTONE = PokecubeLegends.ITEMS.register("darkstone", () -> new ItemBase("darkstone", 1,PokecubeItems.POKECUBEITEMS));
        ROCKCORE = PokecubeLegends.ITEMS.register("rockcore", () -> new ItemBase("rockcore", 1,PokecubeItems.POKECUBEITEMS));
        ICECORE = PokecubeLegends.ITEMS.register("icecore", () -> new ItemBase("icecore", 1,PokecubeItems.POKECUBEITEMS));
        STEELCORE = PokecubeLegends.ITEMS.register("steelcore", () -> new ItemBase("steelcore", 1,PokecubeItems.POKECUBEITEMS));
        EMBLEM = PokecubeLegends.ITEMS.register("emblem", () -> new ItemBase("emblem", 1,PokecubeItems.POKECUBEITEMS));
        MAGMA_CORE = PokecubeLegends.ITEMS.register("magmacore", () -> new ItemBase("magmacore", 1,PokecubeItems.POKECUBEITEMS));
        LIFEORB = PokecubeLegends.ITEMS.register("lifeorb", () -> new ItemBase("lifeorb", 1,PokecubeItems.POKECUBEITEMS));
        DESTRUCTORB = PokecubeLegends.ITEMS.register("destructorb", () -> new ItemBase("destructorb", 1,PokecubeItems.POKECUBEITEMS).setTooltipName(
        		"destructorb"));
        ANCIENT_STONE = PokecubeLegends.ITEMS.register("ancient_stone", () -> new ItemBase("ancient_stone", 1,PokecubeItems.POKECUBEITEMS).setTooltipName(
        		"ancient_stone"));
        ORANGE_RUNE = PokecubeLegends.ITEMS.register("orange_rune", () -> new ItemBase("orange_rune", 1,PokecubeItems.POKECUBEITEMS).setTooltipName(
                "orangerune"));
        BLUE_RUNE = PokecubeLegends.ITEMS.register("blue_rune", () -> new ItemBase("blue_rune", 1,PokecubeItems.POKECUBEITEMS).setTooltipName(
                "bluerune"));
        GREEN_RUNE = PokecubeLegends.ITEMS.register("green_rune", () -> new ItemBase("green_rune", 1,PokecubeItems.POKECUBEITEMS).setTooltipName(
                "greenrune"));
        REGIS_ORB = PokecubeLegends.ITEMS.register("regisorb", () -> new ItemBase("regisorb", 1,PokecubeItems.POKECUBEITEMS));
        THUNDERCORE = PokecubeLegends.ITEMS.register("thundercore", () -> new ItemBase("thundercore", 1,PokecubeItems.POKECUBEITEMS));
        DRAGOCORE   = PokecubeLegends.ITEMS.register("dragocore", () -> new ItemBase("dragocore", 1,PokecubeItems.POKECUBEITEMS));
        ICE_CARROT   = PokecubeLegends.ITEMS.register("ice_carrot", () -> new ItemBase("ice_carrot", 1,PokecubeItems.POKECUBEITEMS).setTooltipName("ice_c"));
        SHADOW_CARROT   = PokecubeLegends.ITEMS.register("shadow_carrot", () -> new ItemBase("shadow_carrot", 1,PokecubeItems.POKECUBEITEMS).setTooltipName("shadow_c"));
        IMPRISIONMENT_HELMET   = PokecubeLegends.ITEMS.register("imprisonment_helmet", () -> new ItemBase("imprisonment_helmet", 1,PokecubeItems.POKECUBEITEMS).setTooltipName("helmet"));
        
        // Gens_ores
        SAPPHIRE = PokecubeLegends.ITEMS.register("sapphire", () -> new ItemBase("sapphire", 64,PokecubeItems.POKECUBEITEMS).noTooltop());
        RUBY = PokecubeLegends.ITEMS.register("ruby", () -> new ItemBase("ruby", 64,PokecubeItems.POKECUBEITEMS).noTooltop());

        // Extra
        SILVER_WING = PokecubeLegends.ITEMS.register("silver_wing", () -> new ItemBase("silver_wing", 5,PokecubeItems.POKECUBEITEMS).noTooltop());
        RAINBOW_WING = PokecubeLegends.ITEMS.register("rainbow_wing", () -> new ItemBase("rainbow_wing", 5,PokecubeItems.POKECUBEITEMS)
                .noTooltop());
        CRYSTAL_SHARD = PokecubeLegends.ITEMS.register("crystal_shard", () -> new ItemBase("crystal_shard", 35,PokecubeItems.POKECUBEITEMS)
                .noTooltop());
        GRISEOUS_ORB = PokecubeLegends.ITEMS.register("griseousorb", () -> new ItemBase("griseousorb", 1,PokecubeItems.POKECUBEITEMS));
        ZYGARDE_CUBE = PokecubeLegends.ITEMS.register("zygardecube", () -> new ItemBase("zygardecube", 1,PokecubeItems.POKECUBEITEMS));
        PRISION_BOTTLE = PokecubeLegends.ITEMS.register("prisonbottle", () -> new ItemBase("prisonbottle", 1,PokecubeItems.POKECUBEITEMS));
        REVEAL_GLASS = PokecubeLegends.ITEMS.register("revealglass", () -> new ItemBase("revealglass", 1,PokecubeItems.POKECUBEITEMS));
        DNA_SPLICERA = PokecubeLegends.ITEMS.register("dna_splicera", () -> new ItemBase("dna_splicera", 1,PokecubeItems.POKECUBEITEMS)
                .setTooltipName("dnasplicer"));
        DNA_SPLICERB = PokecubeLegends.ITEMS.register("dna_splicerb", () -> new ItemBase("dna_splicerb", 1,PokecubeItems.POKECUBEITEMS)
                .setTooltipName("dnasplicer"));
        GRACIDEA = PokecubeLegends.ITEMS.register("gracidea", () -> new ItemBase("gracidea", 10,PokecubeItems.POKECUBEITEMS));
        METEORITE = PokecubeLegends.ITEMS.register("meteorite", () -> new ItemBase("meteorite", 16,PokecubeItems.POKECUBEITEMS));
        NSUN = PokecubeLegends.ITEMS.register("n_sun", () -> new ItemBase("n_sun", 1,PokecubeItems.POKECUBEITEMS).setTooltipName("nsun"));
        NMOON = PokecubeLegends.ITEMS.register("n_moon", () -> new ItemBase("n_moon", 1,PokecubeItems.POKECUBEITEMS).setTooltipName("nmoon"));
        AZURE_FLUTE = PokecubeLegends.ITEMS.register("azure_flute", () -> new ItemBase("azure_flute", 1,PokecubeItems.POKECUBEITEMS).setTooltipName(
                "azureflute"));
        RSHIELD = PokecubeLegends.ITEMS.register("rustedshield", () -> new ItemBase("rustedshield", 1,PokecubeItems.POKECUBEITEMS));
        RSWORD = PokecubeLegends.ITEMS.register("rustedsword", () -> new ItemBase("rustedsword", 1,PokecubeItems.POKECUBEITEMS));

        GENE_FOSSIL = PokecubeLegends.ITEMS.register("fossil_gene", () -> new ItemBase("fossil_gene", 1,PokecubeItems.POKECUBEITEMS).noTooltop());
        CHPOT = PokecubeLegends.ITEMS.register("chippedpot", () -> new ItemBase("chippedpot", 1,PokecubeItems.POKECUBEITEMS).noTooltop());
        CRPOT = PokecubeLegends.ITEMS.register("crackedpot", () -> new ItemBase("crackedpot", 1,PokecubeItems.POKECUBEITEMS).noTooltop());
        GALARCUFF = PokecubeLegends.ITEMS.register("galarcuff", () -> new ItemBase("galarcuff", 1,PokecubeItems.POKECUBEITEMS).noTooltop());
        PDARK = PokecubeLegends.ITEMS.register("pdark", () -> new ItemBase("pdark", 1,PokecubeItems.POKECUBEITEMS).setTooltipName("pdark"));
        PWATER = PokecubeLegends.ITEMS.register("pwater", () -> new ItemBase("pwater", 1,PokecubeItems.POKECUBEITEMS).setTooltipName("pwater"));
        REINS_U = PokecubeLegends.ITEMS.register("reins_u", () -> new ItemBase("reins_u", 1,PokecubeItems.POKECUBEITEMS).setTooltipName("reins_unity"));
        GALARWREATH = PokecubeLegends.ITEMS.register("galarwreath", () -> new ItemBase("galarwreath", 1,PokecubeItems.POKECUBEITEMS).noTooltop());
        
        WISHING_PIECE = PokecubeLegends.ITEMS.register("wishing_piece", () -> new ItemBase("wishing_piece", 1,PokecubeItems.POKECUBEITEMS));
        GIGANTIC_SHARD = PokecubeLegends.ITEMS.register("gigantic_shard", () -> new GiganticShard("gigantic_shard", 1));

        RAINBOW_SWORD = PokecubeLegends.ITEMS.register("rainbow_sword", () -> new RainbowSword(5, 1,
                ItemInit.MATERIAL_RAINBOW));
        KELDEO_SWORD = PokecubeLegends.ITEMS.register("keldeo_sword", () -> new KeldeoSword(6, 1,
                ItemInit.MATERIAL_JUSTISE, "keldeo_sword").setTooltipName("keldeosword"));

        // UltraSpace
        SPECTRUM_SHARD = PokecubeLegends.ITEMS.register("spectrum_shard", () -> new ItemBase("spectrum_shard", 32, PokecubeItems.POKECUBEITEMS).noTooltop());
        
        ULTRA_HELMET = PokecubeLegends.ITEMS.register("ultra_helmet", () -> new UltraHelmetEffect(
                ItemInit.armormaterial, EquipmentSlotType.HEAD, new Item.Properties().group(PokecubeLegends.TAB)));
        ULTRA_CHESTPLATE = PokecubeLegends.ITEMS.register("ultra_chestplate", () -> new ArmorItem(
                ItemInit.armormaterial, EquipmentSlotType.CHEST, new Item.Properties().group(PokecubeLegends.TAB)));
        ULTRA_LEGGINGS = PokecubeLegends.ITEMS.register("ultra_leggings", () -> new ArmorItem(
                ItemInit.armormaterial, EquipmentSlotType.LEGS, new Item.Properties().group(PokecubeLegends.TAB)));
        ULTRA_BOOTS = PokecubeLegends.ITEMS.register("ultra_boots", () -> new UltraBootsEffect(
                ItemInit.armormaterial, EquipmentSlotType.FEET, new Item.Properties().group(PokecubeLegends.TAB)));

        //Distortic World
        GIRATINA_MIRROR = PokecubeLegends.ITEMS.register("giratina_mirror", () -> new DistortedMirror("giratina_mirror", 1).setTooltipName("mirror"));
        HEAD_MIRROR = PokecubeLegends.ITEMS.register("head_mirror", () -> new ItemBase("head_mirror", 1, PokecubeItems.POKECUBEITEMS).noTooltop());
        BODY_MIRROR = PokecubeLegends.ITEMS.register("body_mirror", () -> new ItemBase("body_mirror", 1, PokecubeItems.POKECUBEITEMS).noTooltop());
        GLASS_MIRROR = PokecubeLegends.ITEMS.register("glass_mirror", () -> new ItemBase("glass_mirror", 1, PokecubeItems.POKECUBEITEMS).noTooltop());
        
        // Torchs
        TORCH01 = PokecubeLegends.ITEMS.register("ultra_torch1", () -> new WallOrFloorItem(BlockInit.ULTRA_TORCH1
                .get(), BlockInit.ULTRA_TORCH1_WALL.get(), new Item.Properties().group(PokecubeLegends.TAB)));

        
        ULTRAKEY 		= PokecubeLegends.ITEMS.register("ultrakey", () -> new UltraKey("ultrakey",1).setTooltipName("ultrakey"));
        COSMIC_DUST 	= PokecubeLegends.ITEMS.register("cosmic_dust", () -> new ItemBase("cosmic_dust", 30, PokecubeItems.POKECUBEITEMS).noTooltop());
    }

    public static final IArmorMaterial armormaterial = new IArmorMaterial()
    {
        @Override
        public int getDurability(final EquipmentSlotType slot)
        {
            return new int[] { 13, 15, 16, 11 }[slot.getIndex()] * 25;
        }

        @Override
        public int getDamageReductionAmount(final EquipmentSlotType slot)
        {
            return new int[] { 2, 5, 6, 2 }[slot.getIndex()];
        }

        @Override
        public int getEnchantability()
        {
            return 9;
        }

        @Override
        public net.minecraft.util.SoundEvent getSoundEvent()
        {
            return SoundEvents.ENTITY_ZOMBIE_ATTACK_IRON_DOOR;
        }

        @Override
        public Ingredient getRepairMaterial()
        {
            return Ingredient.fromItems(ItemInit.CRYSTAL_SHARD.get());
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
    };

    public static void init()
    {

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
