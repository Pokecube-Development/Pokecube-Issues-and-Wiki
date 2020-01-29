package pokecube.legends.init;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import net.minecraft.item.SwordItem;
import net.minecraftforge.event.RegistryEvent;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.Nature;
import pokecube.core.utils.PokeType;
import pokecube.legends.items.ItemBase;
import pokecube.legends.items.LegendaryOrb;
import pokecube.legends.items.RainbowSword;
import pokecube.legends.items.natureedit.ItemNature;
import pokecube.legends.items.zmove.ItemZCrystal;

public class ItemInit
{

    public static final List<Item> ITEMS = new ArrayList<>();

    // Materials//TODO custom material here.
    public static final IItemTier MATERIAL_RAINBOW = ItemTier.DIAMOND;

    // Orbs
    public static final Item BLUEORB      = new ItemBase("blueorb", 1);
    public static final Item GREENORB     = new ItemBase("greenorb", 1);
    public static final Item REDORB       = new ItemBase("redorb", 1);
    public static final Item GRAYORB      = new ItemBase("grayorb", 1);
    public static final Item LEGENDARYORB = new LegendaryOrb("legendaryorb", 1);
    public static final Item LUSTROUSORB  = new ItemBase("lustrousorb", 1);
    public static final Item ADAMANTORB   = new ItemBase("adamantorb", 1);
    public static final Item OCEANORB     = new ItemBase("oceanorb", 1);
    public static final Item LIGHTSTONE   = new ItemBase("lightstone", 1);
    public static final Item DARKSTONE    = new ItemBase("darkstone", 1);
    public static final Item ROCKCORE     = new ItemBase("rockcore", 1);
    public static final Item ICECORE      = new ItemBase("icecore", 1);
    public static final Item STEELCORE    = new ItemBase("steelcore", 1);
    public static final Item EMBLEM       = new ItemBase("emblem", 1);
    public static final Item MAGMA_CORE   = new ItemBase("magmacore", 1);
    public static final Item LIFEORB      = new ItemBase("lifeorb", 1);
    public static final Item DESTRUCTORB  = new ItemBase("destructorb", 1);
    public static final Item ORANGE_RUNE  = new ItemBase("orange_rune", 1);
    public static final Item BLUE_RUNE    = new ItemBase("blue_rune", 1);
    public static final Item GREEN_RUNE   = new ItemBase("green_rune", 1);
    public static final Item REGIS_ORB    = new ItemBase("regisorb", 1);

    // Gens_ores
    public static final Item SAPPHIRE = new ItemBase("sapphire", 64).noTooltop();
    public static final Item RUBY     = new ItemBase("ruby", 64).noTooltop();

    // Extra
    public static final Item SILVER_WING    = new ItemBase("silver_wing", 5);
    public static final Item RAINBOW_WING   = new ItemBase("rainbow_wing", 5);
    public static final Item CRYSTAL_SHARD  = new ItemBase("crystal_shard", 35);
    public static final Item GRISEOUS_ORB   = new ItemBase("griseousorb", 1);
    public static final Item ZYGARDE_CUBE   = new ItemBase("zygardecube", 1);
    public static final Item PRISION_BOTTLE = new ItemBase("prisonbottle", 1);
    public static final Item REVEAL_GLASS   = new ItemBase("revealglass", 1);
    public static final Item DNA_SPLICERA   = new ItemBase("dna_splicera", 1).setTooltipName("dnasplicer");
    public static final Item DNA_SPLICERB   = new ItemBase("dna_splicerb", 1).setTooltipName("dnasplicer");
    public static final Item GRACIDEA       = new ItemBase("gracidea", 10);
    public static final Item METEORITE      = new ItemBase("meteorite", 16);
    public static final Item NSUN           = new ItemBase("n_sun", 1);
    public static final Item NMOON          = new ItemBase("n_moon", 1);
    public static final Item AZURE_FLUTE    = new ItemBase("azure_flute", 1);
    public static final Item RSHIELD        = new ItemBase("rustedshield", 1);
    public static final Item RSWORD         = new ItemBase("rustedsword", 1);
    public static final Item CHPOT          = new ItemBase("chippedpot", 1);
    public static final Item CRPOT          = new ItemBase("crackedpot", 1);

    // Tools
    public static final SwordItem RAINBOW_SWORD = new RainbowSword("rainbow_sword", 4, -4, ItemInit.MATERIAL_RAINBOW);

    // Nature Item
    public static void addMint(final RegistryEvent.Register<Item> event)
    {
        for (final Nature type : Nature.values())
        {
            final Item mind = new ItemNature(type);
            event.getRegistry().register(mind);
            final ItemStack stack = new ItemStack(mind);
            PokecubeItems.addToHoldables(stack);
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

        // ItemGenerator.ITEMMODIFIERS.put(t -> ItemNature.isNature(t), null);
        // ItemGenerator.ITEMMODIFIERS.put(t -> ItemZCrystal.isZCrystal(t),
        // null);
    }
}
