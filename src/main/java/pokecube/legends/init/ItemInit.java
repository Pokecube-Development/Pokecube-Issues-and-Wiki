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
import pokecube.core.interfaces.Nature;
import pokecube.core.utils.PokeType;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.Reference;
import pokecube.legends.items.GiganticShard;
import pokecube.legends.items.ItemBase;
import pokecube.legends.items.LegendaryOrb;
import pokecube.legends.items.RainbowSword;
import pokecube.legends.items.armor.UltraBootsEffect;
import pokecube.legends.items.armor.UltraHelmetEffect;
import pokecube.legends.items.natureedit.ItemNature;
import pokecube.legends.items.zmove.ItemZCrystal;

public class ItemInit
{
    // Materials
    public static final IItemTier MATERIAL_RAINBOW = ItemTier.DIAMOND;

    // Orbs
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

    // Evolutions
    public static final RegistryObject<Item> CHPOT;
    public static final RegistryObject<Item> CRPOT;
    public static final RegistryObject<Item> GALARCUFF;
    public static final RegistryObject<Item> PDARK;
    public static final RegistryObject<Item> PWATER;

    // Raids/Dynamax/Gigantamax
    public static final RegistryObject<Item> WISHING_PIECE;
    public static final RegistryObject<Item> GIGANTIC_SHARD;

    // Tools
    public static final RegistryObject<Item> RAINBOW_SWORD;

    // UltraSpace
    public static final RegistryObject<Item> SPECTRUM_SHARD;

    // Armor
    public static final RegistryObject<ArmorItem> ULTRA_HELMET;
    public static final RegistryObject<ArmorItem> ULTRA_CHESTPLATE;
    public static final RegistryObject<ArmorItem> ULTRA_LEGGINGS;
    public static final RegistryObject<ArmorItem> ULTRA_BOOTS;

    // Torch
    public static final RegistryObject<Item> TORCH01;

    static
    {
        // Orbs
        BLUEORB = PokecubeLegends.ITEMS.register("blueorb", () -> new ItemBase("blueorb", 1));
        GREENORB = PokecubeLegends.ITEMS.register("greenorb", () -> new ItemBase("greenorb", 1));
        REDORB = PokecubeLegends.ITEMS.register("redorb", () -> new ItemBase("redorb", 1));
        GRAYORB = PokecubeLegends.ITEMS.register("grayorb", () -> new ItemBase("grayorb", 1).noTooltop());
        LEGENDARYORB = PokecubeLegends.ITEMS.register("legendaryorb", () -> new LegendaryOrb("legendaryorb", 1));
        LUSTROUSORB = PokecubeLegends.ITEMS.register("lustrousorb", () -> new ItemBase("lustrousorb", 1));
        ADAMANTORB = PokecubeLegends.ITEMS.register("adamantorb", () -> new ItemBase("adamantorb", 1));
        OCEANORB = PokecubeLegends.ITEMS.register("oceanorb", () -> new ItemBase("oceanorb", 1));
        LIGHTSTONE = PokecubeLegends.ITEMS.register("lightstone", () -> new ItemBase("lightstone", 1));
        DARKSTONE = PokecubeLegends.ITEMS.register("darkstone", () -> new ItemBase("darkstone", 1));
        ROCKCORE = PokecubeLegends.ITEMS.register("rockcore", () -> new ItemBase("rockcore", 1));
        ICECORE = PokecubeLegends.ITEMS.register("icecore", () -> new ItemBase("icecore", 1));
        STEELCORE = PokecubeLegends.ITEMS.register("steelcore", () -> new ItemBase("steelcore", 1));
        EMBLEM = PokecubeLegends.ITEMS.register("emblem", () -> new ItemBase("emblem", 1));
        MAGMA_CORE = PokecubeLegends.ITEMS.register("magmacore", () -> new ItemBase("magmacore", 1));
        LIFEORB = PokecubeLegends.ITEMS.register("lifeorb", () -> new ItemBase("lifeorb", 1));
        DESTRUCTORB = PokecubeLegends.ITEMS.register("destructorb", () -> new ItemBase("destructorb", 1));
        ORANGE_RUNE = PokecubeLegends.ITEMS.register("orange_rune", () -> new ItemBase("orange_rune", 1).setTooltipName(
                "orangerune"));
        BLUE_RUNE = PokecubeLegends.ITEMS.register("blue_rune", () -> new ItemBase("blue_rune", 1).setTooltipName(
                "bluerune"));
        GREEN_RUNE = PokecubeLegends.ITEMS.register("green_rune", () -> new ItemBase("green_rune", 1).setTooltipName(
                "greenrune"));
        REGIS_ORB = PokecubeLegends.ITEMS.register("regisorb", () -> new ItemBase("regisorb", 1));

        // Gens_ores
        SAPPHIRE = PokecubeLegends.ITEMS.register("sapphire", () -> new ItemBase("sapphire", 64).noTooltop());
        RUBY = PokecubeLegends.ITEMS.register("ruby", () -> new ItemBase("ruby", 64).noTooltop());

        // Extra
        SILVER_WING = PokecubeLegends.ITEMS.register("silver_wing", () -> new ItemBase("silver_wing", 5).noTooltop());
        RAINBOW_WING = PokecubeLegends.ITEMS.register("rainbow_wing", () -> new ItemBase("rainbow_wing", 5)
                .noTooltop());
        CRYSTAL_SHARD = PokecubeLegends.ITEMS.register("crystal_shard", () -> new ItemBase("crystal_shard", 35)
                .noTooltop());
        GRISEOUS_ORB = PokecubeLegends.ITEMS.register("griseousorb", () -> new ItemBase("griseousorb", 1));
        ZYGARDE_CUBE = PokecubeLegends.ITEMS.register("zygardecube", () -> new ItemBase("zygardecube", 1));
        PRISION_BOTTLE = PokecubeLegends.ITEMS.register("prisonbottle", () -> new ItemBase("prisonbottle", 1));
        REVEAL_GLASS = PokecubeLegends.ITEMS.register("revealglass", () -> new ItemBase("revealglass", 1));
        DNA_SPLICERA = PokecubeLegends.ITEMS.register("dna_splicera", () -> new ItemBase("dna_splicera", 1)
                .setTooltipName("dnasplicer"));
        DNA_SPLICERB = PokecubeLegends.ITEMS.register("dna_splicerb", () -> new ItemBase("dna_splicerb", 1)
                .setTooltipName("dnasplicer"));
        GRACIDEA = PokecubeLegends.ITEMS.register("gracidea", () -> new ItemBase("gracidea", 10));
        METEORITE = PokecubeLegends.ITEMS.register("meteorite", () -> new ItemBase("meteorite", 16));
        NSUN = PokecubeLegends.ITEMS.register("n_sun", () -> new ItemBase("n_sun", 1).setTooltipName("nsun"));
        NMOON = PokecubeLegends.ITEMS.register("n_moon", () -> new ItemBase("n_moon", 1).setTooltipName("nmoon"));
        AZURE_FLUTE = PokecubeLegends.ITEMS.register("azure_flute", () -> new ItemBase("azure_flute", 1).setTooltipName(
                "azureflute"));
        RSHIELD = PokecubeLegends.ITEMS.register("rustedshield", () -> new ItemBase("rustedshield", 1));
        RSWORD = PokecubeLegends.ITEMS.register("rustedsword", () -> new ItemBase("rustedsword", 1));

        CHPOT = PokecubeLegends.ITEMS.register("chippedpot", () -> new ItemBase("chippedpot", 1).noTooltop());
        CRPOT = PokecubeLegends.ITEMS.register("crackedpot", () -> new ItemBase("crackedpot", 1).noTooltop());
        GALARCUFF = PokecubeLegends.ITEMS.register("galarcuff", () -> new ItemBase("galarcuff", 1).noTooltop());
        PDARK = PokecubeLegends.ITEMS.register("pdark", () -> new ItemBase("pdark", 1).setTooltipName("pdark"));
        PWATER = PokecubeLegends.ITEMS.register("pwater", () -> new ItemBase("pwater", 1).setTooltipName("pwater"));

        WISHING_PIECE = PokecubeLegends.ITEMS.register("wishing_piece", () -> new ItemBase("wishing_piece", 1));
        GIGANTIC_SHARD = PokecubeLegends.ITEMS.register("gigantic_shard", () -> new GiganticShard("gigantic_shard", 1));

        RAINBOW_SWORD = PokecubeLegends.ITEMS.register("rainbow_sword", () -> new RainbowSword(4, -3,
                ItemInit.MATERIAL_RAINBOW));

        // UltraSpace
        SPECTRUM_SHARD = PokecubeLegends.ITEMS_TAB.register("spectrum_shard", () -> new ItemBase("spectrum_shard", 32));

        ULTRA_HELMET = PokecubeLegends.ITEMS_TAB.register("ultra_helmet", () -> new UltraHelmetEffect(
                ItemInit.armormaterial, EquipmentSlotType.HEAD, new Item.Properties().group(PokecubeLegends.TAB)));
        ULTRA_CHESTPLATE = PokecubeLegends.ITEMS_TAB.register("ultra_chestplate", () -> new ArmorItem(
                ItemInit.armormaterial, EquipmentSlotType.CHEST, new Item.Properties().group(PokecubeLegends.TAB)));
        ULTRA_LEGGINGS = PokecubeLegends.ITEMS_TAB.register("ultra_leggings", () -> new ArmorItem(
                ItemInit.armormaterial, EquipmentSlotType.LEGS, new Item.Properties().group(PokecubeLegends.TAB)));
        ULTRA_BOOTS = PokecubeLegends.ITEMS_TAB.register("ultra_boots", () -> new UltraBootsEffect(
                ItemInit.armormaterial, EquipmentSlotType.FEET, new Item.Properties().group(PokecubeLegends.TAB)));

        // Torchs
        TORCH01 = PokecubeLegends.ITEMS_TAB.register("ultra_torch1", () -> new WallOrFloorItem(BlockInit.ULTRA_TORCH1
                .get(), BlockInit.ULTRA_TORCH1_WALL.get(), new Item.Properties().group(PokecubeLegends.TAB)));

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
