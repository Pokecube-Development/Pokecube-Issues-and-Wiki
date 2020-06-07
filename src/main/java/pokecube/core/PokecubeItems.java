package pokecube.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.material.Material;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.Item.Properties;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.state.IProperty;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.core.blocks.bases.BaseBlock;
import pokecube.core.blocks.healer.HealerBlock;
import pokecube.core.blocks.maxspot.MaxBlock;
import pokecube.core.blocks.nests.NestBlock;
import pokecube.core.blocks.pc.PCBlock;
import pokecube.core.blocks.repel.RepelBlock;
import pokecube.core.blocks.tms.TMBlock;
import pokecube.core.blocks.trade.TraderBlock;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntryLoader;
import pokecube.core.handlers.ItemGenerator;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.ItemPokedex;
import pokecube.core.items.UsableItemEffects;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.items.berries.ItemBerry;
import pokecube.core.items.loot.functions.MakeBerry;
import pokecube.core.items.loot.functions.MakeFossil;
import pokecube.core.items.loot.functions.MakeHeldItem;
import pokecube.core.items.pokecubes.DispenserBehaviorPokecube;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import pokecube.core.items.revive.ItemRevive;
import pokecube.core.items.vitamins.ItemCandy;
import pokecube.core.utils.Tools;
import thut.api.item.ItemList;

public class PokecubeItems extends ItemList
{
    public static ItemStack POKECUBE_ITEMS   = ItemStack.EMPTY;
    public static ItemStack POKECUBE_BLOCKS  = ItemStack.EMPTY;
    public static ItemStack POKECUBE_CUBES   = ItemStack.EMPTY;
    public static ItemStack POKECUBE_BERRIES = ItemStack.EMPTY;

    public static final ItemGroup POKECUBEITEMS   = new ItemGroup("pokecube_items")
                                                  {
                                                      @Override
                                                      public ItemStack createIcon()
                                                      {
                                                          return PokecubeItems.POKECUBE_ITEMS;
                                                      }
                                                  };
    public static final ItemGroup POKECUBEBLOCKS  = new ItemGroup("pokecube_blocks")
                                                  {
                                                      @Override
                                                      public ItemStack createIcon()
                                                      {
                                                          return PokecubeItems.POKECUBE_BLOCKS;
                                                      }
                                                  };
    public static final ItemGroup POKECUBECUBES   = new ItemGroup("pokecube_cubes")
                                                  {
                                                      @Override
                                                      public ItemStack createIcon()
                                                      {
                                                          return PokecubeItems.POKECUBE_CUBES;
                                                      }
                                                  };
    public static final ItemGroup POKECUBEBERRIES = new ItemGroup("pokecube_berries")
                                                  {
                                                      @Override
                                                      public ItemStack createIcon()
                                                      {
                                                          return PokecubeItems.POKECUBE_BERRIES;
                                                      }
                                                  };

    public static final ResourceLocation POKEMOBEGG = new ResourceLocation("pokecube:pokemob_egg");
    public static final ResourceLocation HELDKEY    = new ResourceLocation("pokecube:pokemob_held");
    public static final ResourceLocation EVOSKEY    = new ResourceLocation("pokecube:pokemob_evo");
    public static final ResourceLocation TMKEY      = new ResourceLocation("pokecube:tms");

    // Items
    public static final Item BERRYJUICE;
    public static final Item POKEDEX;
    public static final Item POKEWATCH;
    public static final Item EGG;
    public static final Item CANDY;
    public static final Item REVIVE;

    // Blocks
    public static final Block HEALER;
    public static final Block NESTBLOCK;
    public static final Block REPELBLOCK;
    public static final Block DYNABLOCK;
    public static final Block PCTOP;
    public static final Block PCBASE;
    public static final Block TRADER;
    public static final Block TMMACHINE;
    public static final Block SECRETBASE;
    public static final Block FOSSILSTONE;

    public static boolean      resetTimeTags = false;
    public static Vector<Long> times         = new Vector<>();

    private static HashMap<ResourceLocation, Item[]> pokecubes = new HashMap<>();

    /** contains pokecubes that should be rendered using the default renderer */
    private static Set<ResourceLocation>           cubeIds = new HashSet<>();
    /**
     * Items to be considered for re-animation, mapped to the pokedex number to
     * reanimate to.
     */
    public static HashMap<ItemStack, PokedexEntry> fossils = new HashMap<>();

    static
    {
        LootFunctionManager.registerFunction(new MakeBerry.Serializer());
        LootFunctionManager.registerFunction(new MakeHeldItem.Serializer());
        LootFunctionManager.registerFunction(new MakeFossil.Serializer());
    }

    private static Set<ResourceLocation> errored = Sets.newHashSet();

    static
    {
        // Items
        BERRYJUICE = new Item(new Properties().food(new Food.Builder().hunger(4).saturation(0.3F).build()).group(
                PokecubeItems.POKECUBEITEMS));
        POKEDEX = new ItemPokedex(new Properties().group(PokecubeItems.POKECUBEITEMS), false);
        POKEWATCH = new ItemPokedex(new Properties().group(PokecubeItems.POKECUBEITEMS), true);
        EGG = new ItemPokemobEgg(new Properties().group(PokecubeItems.POKECUBEITEMS));
        CANDY = new ItemCandy(new Item.Properties().rarity(Rarity.EPIC).group(PokecubeItems.POKECUBEITEMS));
        REVIVE = new ItemRevive(new Item.Properties().group(PokecubeItems.POKECUBEITEMS));

        // Blocks
        HEALER = new HealerBlock(Block.Properties.create(Material.IRON).hardnessAndResistance(2000).lightValue(15));
        NESTBLOCK = new NestBlock(Block.Properties.create(Material.ORGANIC));
        REPELBLOCK = new RepelBlock(Block.Properties.create(Material.ORGANIC));
        DYNABLOCK = new MaxBlock(Block.Properties.create(Material.ROCK).lightValue(15).hardnessAndResistance(2000));
        PCTOP = new PCBlock(Block.Properties.create(Material.IRON).hardnessAndResistance(2000).lightValue(15), true);
        PCBASE = new PCBlock(Block.Properties.create(Material.IRON).hardnessAndResistance(2000).lightValue(15), false);
        TRADER = new TraderBlock(Block.Properties.create(Material.IRON).hardnessAndResistance(2000).lightValue(15));
        TMMACHINE = new TMBlock(Block.Properties.create(Material.IRON).hardnessAndResistance(2000).lightValue(15));
        SECRETBASE = new BaseBlock(Block.Properties.create(Material.ROCK).hardnessAndResistance(2000));
        FOSSILSTONE = new Block(Block.Properties.create(Material.ROCK).hardnessAndResistance(1.5f, 10).harvestTool(
                ToolType.PICKAXE));
    }

    public static void init()
    {

        // Blocks
        PokecubeItems.HEALER.setRegistryName(PokecubeCore.MODID, "pokecenter");
        PokecubeItems.NESTBLOCK.setRegistryName(PokecubeCore.MODID, "nest");
        PokecubeItems.REPELBLOCK.setRegistryName(PokecubeCore.MODID, "repel");
        PokecubeItems.DYNABLOCK.setRegistryName(PokecubeCore.MODID, "dynamax");
        PokecubeItems.PCTOP.setRegistryName(PokecubeCore.MODID, "pc_top");
        PokecubeItems.PCBASE.setRegistryName(PokecubeCore.MODID, "pc_base");
        PokecubeItems.TRADER.setRegistryName(PokecubeCore.MODID, "trade_machine");
        PokecubeItems.TMMACHINE.setRegistryName(PokecubeCore.MODID, "tm_machine");
        PokecubeItems.SECRETBASE.setRegistryName(PokecubeCore.MODID, "secret_base");
        PokecubeItems.FOSSILSTONE.setRegistryName(PokecubeCore.MODID, "fossilstone");
    }

    /**
     * Registers a pokecube id, the Object[] is an array with the item or block
     * assosicated with the unfilled and filled cubes. example: Object cubes =
     * new Object[] { pokecube, pokecubeFilled}; where pokecube is the unfilled
     * pokecube block, and pokecubeFilled is the filled one. defaults are: 0 -
     * pokecube 1 - greatcube 2 - ultracube 3 - mastercube
     *
     * @param id
     * @param cubes
     */
    public static void addCube(final ResourceLocation id, final Item[] cubes)
    {
        PokecubeItems.addCube(id, cubes, true);
    }

    /**
     * Registers a pokecube id, the Object[] is an array with the item or block
     * assosicated with the unfilled and filled cubes. example: Object cubes =
     * new Object[] { pokecube, pokecubeFilled}; where pokecube is the unfilled
     * pokecube block, and pokecubeFilled is the filled one. defaults are: 0 -
     * pokecube 1 - greatcube 2 - ultracube 3 - mastercube
     *
     * @param id
     * @param cubes
     */
    public static void addCube(final ResourceLocation id, Item[] cubes, final boolean defaultRenderer)
    {
        if (PokecubeItems.pokecubes.containsKey(id)) System.err.println("Pokecube Id " + id
                + " Has already been registered as " + PokecubeItems.getEmptyCube(id));

        if (cubes.length == 1) cubes = new Item[] { cubes[0], cubes[0] };

        final Item[] items = cubes;

        DispenserBlock.registerDispenseBehavior(() -> items[0], new DispenserBehaviorPokecube());
        DispenserBlock.registerDispenseBehavior(() -> items[1], new DispenserBehaviorPokecube());

        if (defaultRenderer) PokecubeItems.cubeIds.add(id);

        PokecubeItems.pokecubes.put(id, items);
    }

    public static void deValidate(final ItemStack stack)
    {
        if (stack.hasTag())
        {
            final long time = stack.getTag().getLong("time");
            PokecubeItems.times.remove(time);
            stack.setTag(null);
            stack.split(1);
        }
    }

    /**
     * defaults are: 0 - pokecube 1 - greatcube 2 - ultracube 3 - mastercube if
     * you pass in a non- pokecube stack, it returns 0, defaults to a pokecube.
     *
     * @param stack
     * @return
     */
    public static ResourceLocation getCubeId(final ItemStack stack)
    {
        if (!stack.isEmpty()) for (final ResourceLocation i : PokecubeItems.pokecubes.keySet())
        {
            final Item[] cubes = PokecubeItems.pokecubes.get(i);
            for (final Item cube : cubes)
                if (cube == stack.getItem()) return i;
        }
        return null;
    }

    public static Item getEmptyCube(final ItemStack stack)
    {
        return PokecubeItems.getEmptyCube(PokecubeItems.getCubeId(stack));
    }

    /**
     * defaults are: 0 - pokecube 1 - greatcube 2 - ultracube 3 - mastercube if
     * you request a non-registerd id, it returns pokecube.
     */
    public static Item getEmptyCube(final ResourceLocation id)
    {
        Item ret = null;

        if (PokecubeItems.pokecubes.containsKey(id)) ret = PokecubeItems.pokecubes.get(id)[0];

        if (ret == null) try
        {
            ret = PokecubeItems.pokecubes.get(PokecubeBehavior.DEFAULTCUBE)[0];
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.error("No Cubes Registered!", e);
            return Items.STONE_HOE;
        }

        return ret;
    }

    public static Item getFilledCube(final ItemStack stack)
    {
        return PokecubeItems.getFilledCube(PokecubeItems.getCubeId(stack));
    }

    /**
     * defaults are: 0 - pokecube 1 - greatcube 2 - ultracube 3 - mastercube if
     * you request a non-registerd id, it returns pokecube.
     */
    public static Item getFilledCube(final ResourceLocation id)
    {
        Item ret = null;

        if (PokecubeItems.pokecubes.containsKey(id)) ret = PokecubeItems.pokecubes.get(id)[1];

        if (ret == null)
        {
            ret = PokecubeItems.pokecubes.get(PokecubeBehavior.DEFAULTCUBE)[1];
            if (id != null) System.err.println("Could not find filled cube for id " + id);
        }

        return ret;
    }

    public static PokedexEntry getFossilEntry(final ItemStack fossil)
    {
        if (fossil.isEmpty()) return null;
        PokedexEntry ret = null;
        for (final ItemStack s : PokecubeItems.fossils.keySet())
            if (Tools.isSameStack(fossil, s))
            {
                ret = PokecubeItems.fossils.get(s);
                break;
            }
        return ret;
    }

    public static ItemStack getStack(final ResourceLocation loc)
    {
        return PokecubeItems.getStack(loc, true);
    }

    public static ItemStack getStack(final ResourceLocation loc, final boolean stacktrace)
    {
        final Tag<Item> tag = ItemTags.getCollection().get(loc);
        if (tag != null)
        {
            final Item item = tag.getRandomElement(new Random(2));
            if (item != null) return new ItemStack(item);
        }
        final Item item = ForgeRegistries.ITEMS.getValue(loc);
        if (item != null) return new ItemStack(item);
        if (stacktrace && PokecubeItems.errored.add(loc))
        {
            PokecubeCore.LOGGER.error(loc + " Not found in list of items.");
            if (PokecubeMod.debug) PokecubeCore.LOGGER.error("stacktrace: ", new NullPointerException());
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack getStack(final String name)
    {
        return PokecubeItems.getStack(name, true);
    }

    public static ItemStack getStack(final String name, final boolean stacktrace)
    {
        if (!PokecubeItems.stackExists(name)) return ItemStack.EMPTY;
        final ResourceLocation loc = PokecubeItems.toPokecubeResource(name);
        return PokecubeItems.getStack(loc, stacktrace);
    }

    public static Predicate<BlockState> getState(final String arguments)
    {
        final String[] args = arguments.split(" ");

        final String[] resource = args[0].split(":");
        final String modid = resource[0];
        final String blockName = resource[1];
        String keyTemp = null;
        String valTemp = null;

        if (args.length > 1)
        {
            final String[] state = args[1].split("=");
            keyTemp = state[0];
            valTemp = state[1];
        }
        final String key = keyTemp;
        final String val = valTemp;
        return new Predicate<BlockState>()
        {
            final Pattern                  modidPattern = Pattern.compile(modid);
            final Pattern                  blockPattern = Pattern.compile(blockName);
            Map<ResourceLocation, Boolean> checks       = Maps.newHashMap();

            @Override
            public boolean apply(final BlockState input)
            {
                if (input == null || input.getBlock() == null) return false;
                final Block block = input.getBlock();
                final ResourceLocation name = block.getRegistryName();
                if (this.checks.containsKey(name) && !this.checks.get(name)) return false;
                else if (!this.checks.containsKey(name))
                {
                    if (!this.modidPattern.matcher(name.getNamespace()).matches())
                    {
                        this.checks.put(name, false);
                        return false;
                    }
                    if (!this.blockPattern.matcher(name.getPath()).matches())
                    {
                        this.checks.put(name, false);
                        return false;
                    }
                    this.checks.put(name, true);
                }
                if (key == null) return true;
                for (final IProperty<?> prop : input.getProperties())
                    if (prop.getName().equals(key))
                    {
                        final Object inputVal = input.get(prop);
                        return inputVal.toString().equalsIgnoreCase(val);
                    }
                return false;
            }
        };
    }

    public static void init(final MinecraftServer server)
    {
        if (PokecubeMod.debug) PokecubeItems.initTags(server);
    }

    private static void initTags(final MinecraftServer server)
    {
        // Init an entity type tag for pokemobs
        JsonObject json = new JsonObject();
        json.addProperty("replace", false);
        JsonArray array = new JsonArray();
        for (final PokedexEntry entry : Database.getSortedFormes())
            array.add("pokecube:" + entry.getTrimmedName());
        json.add("values", array);
        File folder = new File(".", "generated/entity_types");
        folder.mkdirs();
        File file = new File(folder, "pokemob.json");
        FileWriter writer;
        try
        {
            writer = new FileWriter(file);
            writer.write(PokedexEntryLoader.gson.toJson(json));
            writer.close();
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }

        folder = new File(".", "generated/items");
        folder.mkdirs();

        // Init tag for the fossils
        json = new JsonObject();
        json.addProperty("replace", false);
        array = new JsonArray();
        for (final String type : ItemGenerator.fossilVariants)
            array.add(PokecubeCore.MODID + ":fossil_" + type);
        json.add("values", array);
        file = new File(folder, "pokemob_fossils.json");
        try
        {
            writer = new FileWriter(file);
            writer.write(PokedexEntryLoader.gson.toJson(json));
            writer.close();
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }

        // Init tag for pokecubes
        json = new JsonObject();
        json.addProperty("replace", false);
        array = new JsonArray();
        for (final ResourceLocation type : IPokecube.BEHAVIORS.getKeys())
            array.add(PokecubeCore.MODID + ":" + type.getPath() + "cube");
        json.add("values", array);
        file = new File(folder, "pokecubes.json");
        try
        {
            writer = new FileWriter(file);
            writer.write(PokedexEntryLoader.gson.toJson(json));
            writer.close();
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }

        // Init tag for berries
        json = new JsonObject();
        json.addProperty("replace", false);
        array = new JsonArray();
        for (final Entry<ItemBerry> type : BerryManager.berryItems.int2ObjectEntrySet())
            array.add(type.getValue().getRegistryName().toString());
        json.add("values", array);
        file = new File(folder, "berries.json");
        try
        {
            writer = new FileWriter(file);
            writer.write(PokedexEntryLoader.gson.toJson(json));
            writer.close();
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }

        // Init the specific tags registerd
        for (final ResourceLocation name : ItemList.pendingTags.keySet())
        {
            json = new JsonObject();
            json.addProperty("replace", false);
            array = new JsonArray();
            final List<Item> items = Lists.newArrayList(ItemList.pendingTags.get(name));
            items.sort((a, b) -> a.getRegistryName().compareTo(b.getRegistryName()));
            for (final Item item : items)
                array.add(item.getRegistryName().toString());
            json.add("values", array);
            final String fileConts = PokedexEntryLoader.gson.toJson(json);
            file = new File(folder, name.getPath() + ".json");
            try
            {
                writer = new FileWriter(file);
                writer.write(fileConts);
                writer.close();
            }
            catch (final IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static boolean isValid(final ItemStack stack)
    {
        if (stack.hasTag()) return PokecubeItems.times.contains(stack.getTag().getLong("time"));
        return false;
    }

    public static boolean isValidEvoItem(final ItemStack stack)
    {
        if (stack.isEmpty()) return false;
        return ItemList.is(PokecubeItems.EVOSKEY, stack);
    }

    public static boolean isValidHeldItem(final ItemStack stack)
    {
        if (stack.getCapability(UsableItemEffects.USABLEITEM_CAP, null).isPresent()) return true;
        return ItemList.is(PokecubeItems.HELDKEY, stack) || PokecubeItems.isValidEvoItem(stack);
    }

    public static void loadTime(final CompoundNBT nbt)
    {
        if (PokecubeItems.resetTimeTags)
        {
            PokecubeItems.resetTimeTags = false;
            return;
        }
        PokecubeItems.times.clear();
        final int num = nbt.getInt("count");
        for (int i = 0; i < num; i++)
            if (Long.valueOf(nbt.getLong("" + i)) != 0) PokecubeItems.times.add(Long.valueOf(nbt.getLong("" + i)));
    }

    public static ItemStack makeCandyStack()
    {
        final ItemStack candy = PokecubeItems.getStack("rarecandy");
        if (candy.isEmpty()) return ItemStack.EMPTY;
        PokecubeItems.makeStackValid(candy);
        candy.setDisplayName(new TranslationTextComponent("pokecube.candy.rare"));
        return candy;
    }

    public static void makeStackValid(final ItemStack stack)
    {
        final long time = System.nanoTime();
        if (PokecubeItems.isValid(stack)) PokecubeItems.deValidate(stack);
        if (!stack.hasTag()) stack.setTag(new CompoundNBT());
        PokecubeItems.times.add(time);
        stack.getTag().putLong("time", time);
    }

    public static void registerFossil(final ItemStack fossil, final int number)
    {
        PokecubeItems.fossils.put(fossil.copy(), Database.getEntry(number));
    }

    public static void registerFossil(final ItemStack fossil, final String pokemonName)
    {
        if (Database.entryExists(pokemonName)) PokecubeItems.fossils.put(fossil.copy(), Database.getEntry(pokemonName));
    }

    public static void saveTime(final CompoundNBT nbt)
    {
        final Long[] i = PokecubeItems.times.toArray(new Long[0]);

        int num = 0;
        if (nbt == null || i == null)
        {
            PokecubeCore.LOGGER.error("No Data to save for Item Validations.");
            return;
        }
        for (final Long l : i)
            if (l != null)
            {
                nbt.putLong("" + num, l.longValue());
                num++;
            }
        nbt.putInt("count", num);
    }

    public static boolean stackExists(final String name)
    {
        if (name == null) return false;
        final ResourceLocation loc = PokecubeItems.toPokecubeResource(name);
        final Tag<Item> old = ItemTags.getCollection().get(loc);
        final Item item = ForgeRegistries.ITEMS.getValue(loc);
        // TODO confirm this works
        return old != null || ItemList.pendingTags.containsKey(loc) || item != null;
    }

    public static ResourceLocation toPokecubeResource(final String name)
    {
        ResourceLocation loc;
        if (!name.contains(":")) loc = new ResourceLocation(PokecubeCore.MODID, name);
        else loc = new ResourceLocation(name);
        return loc;
    }
}
