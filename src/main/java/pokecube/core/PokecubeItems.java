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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.OreBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import pokecube.core.blocks.bases.BaseBlock;
import pokecube.core.blocks.bases.BaseTile;
import pokecube.core.blocks.healer.HealerBlock;
import pokecube.core.blocks.healer.HealerTile;
import pokecube.core.blocks.maxspot.MaxBlock;
import pokecube.core.blocks.maxspot.MaxTile;
import pokecube.core.blocks.nests.NestBlock;
import pokecube.core.blocks.nests.NestTile;
import pokecube.core.blocks.pc.PCBlock;
import pokecube.core.blocks.pc.PCTile;
import pokecube.core.blocks.repel.RepelBlock;
import pokecube.core.blocks.repel.RepelTile;
import pokecube.core.blocks.tms.TMBlock;
import pokecube.core.blocks.tms.TMTile;
import pokecube.core.blocks.trade.TraderBlock;
import pokecube.core.blocks.trade.TraderTile;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.handlers.ItemGenerator;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.ItemPokedex;
import pokecube.core.items.UsableItemEffects;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.items.berries.ItemBerry;
import pokecube.core.items.pokecubes.DispenserBehaviorPokecube;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import pokecube.core.items.revive.ItemRevive;
import pokecube.core.items.vitamins.ItemCandy;
import pokecube.core.utils.Tools;
import thut.api.item.ItemList;
import thut.api.util.JsonUtil;

public class PokecubeItems extends ItemList
{
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
            PokecubeCore.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
            PokecubeCore.MODID);

    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister
            .create(ForgeRegistries.BLOCK_ENTITIES, PokecubeCore.MODID);

    public static ItemStack POKECUBE_ITEMS = ItemStack.EMPTY;
    public static ItemStack POKECUBE_BLOCKS = ItemStack.EMPTY;
    public static ItemStack POKECUBE_CUBES = ItemStack.EMPTY;
    public static ItemStack POKECUBE_BERRIES = ItemStack.EMPTY;

    public static final CreativeModeTab TAB_ITEMS = new CreativeModeTab("pokecube_items")
    {
        @Override
        public ItemStack makeIcon()
        {
            return PokecubeItems.POKECUBE_ITEMS;
        }
    };
    public static final CreativeModeTab TAB_BLOCKS = new CreativeModeTab("pokecube_blocks")
    {
        @Override
        public ItemStack makeIcon()
        {
            return PokecubeItems.POKECUBE_BLOCKS;
        }
    };
    public static final CreativeModeTab TAB_POKECUBES = new CreativeModeTab("pokecube_cubes")
    {
        @Override
        public ItemStack makeIcon()
        {
            return PokecubeItems.POKECUBE_CUBES;
        }
    };
    public static final CreativeModeTab TAB_BERRIES = new CreativeModeTab("pokecube_berries")
    {
        @Override
        public ItemStack makeIcon()
        {
            return PokecubeItems.POKECUBE_BERRIES;
        }
    };

    public static final ResourceLocation POKEMOBEGG = new ResourceLocation("pokecube:pokemob_egg");
    public static final ResourceLocation HELDKEY = new ResourceLocation("pokecube:pokemob_held");
    public static final ResourceLocation EVOSKEY = new ResourceLocation("pokecube:pokemob_evo");
    public static final ResourceLocation TMKEY = new ResourceLocation("pokecube:tms");

    // Items
    public static final RegistryObject<Item> BERRYJUICE;
    public static final RegistryObject<Item> CANDY;
    public static final RegistryObject<Item> EGG;
    public static final RegistryObject<Item> POKEDEX;
    public static final RegistryObject<Item> POKEWATCH;
    public static final RegistryObject<Item> REVIVE;

    // Blocks
    public static final RegistryObject<Block> DEEPSLATE_FOSSIL_ORE;
    public static final RegistryObject<Block> FOSSIL_ORE;
    
    public static final RegistryObject<Block> DYNAMAX;
    public static final RegistryObject<Block> HEALER;
    public static final RegistryObject<Block> NEST;
    public static final RegistryObject<Block> PC_BASE;
    public static final RegistryObject<Block> PC_TOP;
    public static final RegistryObject<Block> REPEL;
    public static final RegistryObject<Block> SECRET_BASE;
    public static final RegistryObject<Block> TM_MACHINE;
    public static final RegistryObject<Block> TRADER;

    // Tile Entities
    public static final RegistryObject<BlockEntityType<?>> BASE_TYPE;
    public static final RegistryObject<BlockEntityType<?>> HEALER_TYPE;
    public static final RegistryObject<BlockEntityType<?>> MAX_TYPE;
    public static final RegistryObject<BlockEntityType<?>> NEST_TYPE;
    public static final RegistryObject<BlockEntityType<?>> PC_TYPE;
    public static final RegistryObject<BlockEntityType<?>> REPEL_TYPE;
    public static final RegistryObject<BlockEntityType<?>> TM_TYPE;
    public static final RegistryObject<BlockEntityType<?>> TRADE_TYPE;

    public static boolean resetTimeTags = false;
    public static Vector<Long> times = new Vector<>();

    private static HashMap<ResourceLocation, Item[]> pokecubes = new HashMap<>();

    /** contains pokecubes that should be rendered using the default renderer */
    private static Set<ResourceLocation> cubeIds = new HashSet<>();
    /**
     * Items to be considered for re-animation, mapped to the pokedex number to
     * reanimate to.
     */
    public static HashMap<ItemStack, PokedexEntry> fossils = new HashMap<>();

    private static Set<ResourceLocation> errored = Sets.newHashSet();

    static
    {
        // Items
        POKEDEX = PokecubeItems.ITEMS.register("pokedex",
                () -> new ItemPokedex(new Properties().tab(PokecubeItems.TAB_ITEMS), false));
        POKEWATCH = PokecubeItems.ITEMS.register("pokewatch",
                () -> new ItemPokedex(new Properties().tab(PokecubeItems.TAB_ITEMS), true));
        BERRYJUICE = PokecubeItems.ITEMS
                .register("berryjuice",
                        () -> new Item(new Properties()
                                .food(new FoodProperties.Builder().nutrition(4).saturationMod(0.3F).build())
                                .tab(PokecubeItems.TAB_ITEMS)));
        EGG = PokecubeItems.ITEMS.register("pokemobegg",
                () -> new ItemPokemobEgg(new Properties().tab(PokecubeItems.TAB_ITEMS)));
        CANDY = PokecubeItems.ITEMS.register("candy",
                () -> new ItemCandy(new Item.Properties().rarity(Rarity.EPIC).tab(PokecubeItems.TAB_ITEMS)));
        REVIVE = PokecubeItems.ITEMS.register("revive",
                () -> new ItemRevive(new Item.Properties().tab(PokecubeItems.TAB_ITEMS)));

        // Blocks
        FOSSIL_ORE = PokecubeItems.BLOCKS.register("fossil_ore", () -> new OreBlock(BlockBehaviour.Properties.of(
                Material.STONE, MaterialColor.STONE).strength(3.0f, 3.0f).sound(SoundType.STONE).requiresCorrectToolForDrops(), UniformInt.of(0, 3)));
        DEEPSLATE_FOSSIL_ORE = PokecubeItems.BLOCKS.register("deepslate_fossil_ore", () -> new OreBlock(BlockBehaviour.Properties.of(
                Material.STONE, MaterialColor.DEEPSLATE).strength(4.5f, 3.0f).sound(SoundType.DEEPSLATE).requiresCorrectToolForDrops(), UniformInt.of(0, 3)));
        
        NEST = PokecubeItems.BLOCKS.register("nest", () -> new NestBlock(BlockBehaviour.Properties.of(
                Material.GRASS, MaterialColor.COLOR_BROWN).sound(SoundType.GRASS).strength(0.5F)));
        SECRET_BASE = PokecubeItems.BLOCKS.register("secret_base", () -> new BaseBlock(BlockBehaviour.Properties.of(
                Material.STONE, MaterialColor.STONE).strength(2000).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        REPEL = PokecubeItems.BLOCKS.register("repel", () -> new RepelBlock(BlockBehaviour.Properties.of(
                Material.GRASS, MaterialColor.COLOR_GREEN).strength(0.5F, 2.5F).sound(SoundType.GRASS).requiresCorrectToolForDrops()));
        HEALER = PokecubeItems.BLOCKS.register("pokecenter", () -> new HealerBlock(BlockBehaviour.Properties.of(
                Material.METAL, MaterialColor.WOOL).strength(2000).sound(SoundType.METAL).requiresCorrectToolForDrops()));
        PC_TOP = PokecubeItems.BLOCKS.register("pc_top", () -> new PCBlock(BlockBehaviour.Properties.of(Material.METAL,
                MaterialColor.COLOR_RED).strength(2000).sound(SoundType.METAL).requiresCorrectToolForDrops(), true));
        PC_BASE = PokecubeItems.BLOCKS.register("pc_base", () -> new PCBlock(BlockBehaviour.Properties.of(Material.METAL,
                MaterialColor.COLOR_RED).strength(2000).sound(SoundType.METAL).requiresCorrectToolForDrops(), false));
        TM_MACHINE = PokecubeItems.BLOCKS.register("tm_machine", () -> new TMBlock(BlockBehaviour.Properties.of(Material.METAL,
                MaterialColor.COLOR_LIGHT_BLUE).strength(2000).sound(SoundType.METAL).requiresCorrectToolForDrops()));
        TRADER = PokecubeItems.BLOCKS.register("trade_machine", () -> new TraderBlock(BlockBehaviour.Properties.of(
                Material.METAL, MaterialColor.COLOR_GREEN).strength(2000).sound(SoundType.METAL).requiresCorrectToolForDrops()));
        DYNAMAX = PokecubeItems.BLOCKS.register("dynamax", () -> new MaxBlock(BlockBehaviour.Properties.of(Material.STONE,
                MaterialColor.COLOR_MAGENTA).sound(SoundType.GLASS).sound(SoundType.AMETHYST_CLUSTER).strength(0.8F).requiresCorrectToolForDrops()));

        // Tile Entity Types
        NEST_TYPE = PokecubeItems.TILES.register("nest", 
                () -> BlockEntityType.Builder.of(NestTile::new, PokecubeItems.NEST.get()).build(null));
        REPEL_TYPE = PokecubeItems.TILES.register("repel",
                () -> BlockEntityType.Builder.of(RepelTile::new, PokecubeItems.REPEL.get()).build(null));
        MAX_TYPE = PokecubeItems.TILES.register("dynamax",
                () -> BlockEntityType.Builder.of(MaxTile::new, PokecubeItems.DYNAMAX.get()).build(null));
        BASE_TYPE = PokecubeItems.TILES.register("secret_base",
                () -> BlockEntityType.Builder.of(BaseTile::new, PokecubeItems.SECRET_BASE.get()).build(null));

        TRADE_TYPE = PokecubeItems.TILES.register("trade_machine",
                () -> BlockEntityType.Builder.of(TraderTile::new, PokecubeItems.TRADER.get()).build(null));
        TM_TYPE = PokecubeItems.TILES.register("tm_machine",
                () -> BlockEntityType.Builder.of(TMTile::new, PokecubeItems.TM_MACHINE.get()).build(null));
        HEALER_TYPE = PokecubeItems.TILES.register("pokecenter",
                () -> BlockEntityType.Builder.of(HealerTile::new, PokecubeItems.HEALER.get()).build(null));
        PC_TYPE = PokecubeItems.TILES.register("pc",
                () -> BlockEntityType.Builder.of(PCTile::new, PokecubeItems.PC_TOP.get(), PokecubeItems.PC_BASE.get()).build(null));
    }

    public static void init()
    {
        for (final RegistryObject<Block> reg : PokecubeItems.BLOCKS.getEntries())
            PokecubeItems.ITEMS.register(reg.getId().getPath(),
                    () -> new BlockItem(reg.get(), new Item.Properties().tab(PokecubeItems.TAB_BLOCKS)));
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
        if (PokecubeItems.pokecubes.containsKey(id)) System.err
                .println("Pokecube Id " + id + " Has already been registered as " + PokecubeItems.getEmptyCube(id));

        if (cubes.length == 1) cubes = new Item[]
        { cubes[0], cubes[0] };

        final Item[] items = cubes;

        DispenserBlock.registerBehavior(() -> items[0], new DispenserBehaviorPokecube());
        DispenserBlock.registerBehavior(() -> items[1], new DispenserBehaviorPokecube());

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
            for (final Item cube : cubes) if (cube == stack.getItem()) return i;
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
        for (final ItemStack s : PokecubeItems.fossils.keySet()) if (Tools.isSameStack(fossil, s))
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
        final Tag<Item> tag = ItemTags.getAllTags().getTag(loc);
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
        return new Predicate<>()
        {
            final Pattern modidPattern = Pattern.compile(modid);
            final Pattern blockPattern = Pattern.compile(blockName);
            Map<ResourceLocation, Boolean> checks = Maps.newHashMap();

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
                for (final Property<?> prop : input.getProperties()) if (prop.getName().equals(key))
                {
                    final Object inputVal = input.getValue(prop);
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
        for (final PokedexEntry entry : Database.getSortedFormes()) array.add("pokecube:" + entry.getTrimmedName());
        json.add("values", array);
        File folder = new File(".", "generated/entity_types");
        folder.mkdirs();
        File file = new File(folder, "pokemob.json");
        FileWriter writer;
        try
        {
            writer = new FileWriter(file);
            writer.write(JsonUtil.gson.toJson(json));
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
        for (final String type : ItemGenerator.fossilVariants) array.add(PokecubeCore.MODID + ":fossil_" + type);
        json.add("values", array);
        file = new File(folder, "pokemob_fossils.json");
        try
        {
            writer = new FileWriter(file);
            writer.write(JsonUtil.gson.toJson(json));
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
            writer.write(JsonUtil.gson.toJson(json));
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
            writer.write(JsonUtil.gson.toJson(json));
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
            for (final Item item : items) array.add(item.getRegistryName().toString());
            json.add("values", array);
            final String fileConts = JsonUtil.gson.toJson(json);
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

    public static Set<ResourceLocation> ADDED_HELD = Sets.newHashSet();

    public static boolean isValidHeldItem(final ItemStack stack)
    {
        if (stack.getCapability(UsableItemEffects.USABLEITEM_CAP, null).isPresent()) return true;
        if (ADDED_HELD.contains(stack.getItem().getRegistryName())) return true;
        return ItemList.is(PokecubeItems.HELDKEY, stack) || PokecubeItems.isValidEvoItem(stack);
    }

    public static void loadTime(final CompoundTag nbt)
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
        final ItemStack candy = PokecubeItems.getStack("candy");
        if (candy.isEmpty()) return ItemStack.EMPTY;
        PokecubeItems.makeStackValid(candy);
        candy.setHoverName(new TranslatableComponent("item.pokecube.candy.rare"));
        return candy;
    }

    public static void makeStackValid(final ItemStack stack)
    {
        final long time = System.nanoTime();
        if (PokecubeItems.isValid(stack)) PokecubeItems.deValidate(stack);
        if (!stack.hasTag()) stack.setTag(new CompoundTag());
        PokecubeItems.times.add(time);
        stack.getTag().putLong("time", time);
    }

    public static void registerFossil(final ItemStack fossil, final String pokemonName)
    {
        if (Database.entryExists(pokemonName)) PokecubeItems.fossils.put(fossil.copy(), Database.getEntry(pokemonName));
    }

    public static void saveTime(final CompoundTag nbt)
    {
        final Long[] i = PokecubeItems.times.toArray(new Long[0]);

        int num = 0;
        if (nbt == null || i == null)
        {
            PokecubeCore.LOGGER.error("No Data to save for Item Validations.");
            return;
        }
        for (final Long l : i) if (l != null)
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
        final Tag<Item> old = ItemTags.getAllTags().getTag(loc);
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
