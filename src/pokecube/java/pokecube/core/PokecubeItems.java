package pokecube.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.items.IPokecube;
import pokecube.api.items.IPokecube.PokecubeBehaviour;
import pokecube.api.utils.Tools;
import pokecube.core.blocks.healer.HealerBlock;
import pokecube.core.blocks.healer.HealerTile;
import pokecube.core.blocks.pc.PCBlock;
import pokecube.core.blocks.pc.PCTile;
import pokecube.core.blocks.repel.RepelBlock;
import pokecube.core.blocks.repel.RepelTile;
import pokecube.core.blocks.tms.TMBlock;
import pokecube.core.blocks.tms.TMTile;
import pokecube.core.blocks.trade.TraderBlock;
import pokecube.core.blocks.trade.TraderTile;
import pokecube.core.database.Database;
import pokecube.core.init.ItemGenerator;
import pokecube.core.inventory.barrels.GenericBarrelMenu;
import pokecube.core.items.ItemPokedex;
import pokecube.core.items.ItemTM;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.items.berries.ItemBerry;
import pokecube.core.items.pokecubes.DispenserBehaviorPokecube;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import pokecube.core.items.revive.ItemRevive;
import pokecube.core.items.vitamins.ItemCandy;
import thut.api.attachments.Ownable;
import thut.api.data.HolderProvider;
import thut.api.item.ItemList;
import thut.api.util.JsonUtil;
import thut.lib.RegHelper;

public class PokecubeItems extends ItemList
{

    public static ItemStack POKECUBE_ITEMS = ItemStack.EMPTY;
    public static ItemStack POKECUBE_BLOCKS = ItemStack.EMPTY;
    public static ItemStack POKECUBE_CUBES = ItemStack.EMPTY;
    public static ItemStack POKECUBE_BERRIES = ItemStack.EMPTY;

    public static final ResourceLocation POKEMOBEGG = ResourceLocation.parse("pokecube:pokemobegg");
    public static final ResourceLocation HELDKEY = ResourceLocation.parse("pokecube:pokemob_held");
    public static final ResourceLocation EVOSKEY = ResourceLocation.parse("pokecube:pokemob_evo");
    public static final ResourceLocation TMKEY = ResourceLocation.parse("pokecube:tms");

    // Items
    public static final DeferredItem<Item> BERRYJUICE;
    public static final DeferredItem<Item> CANDY;
    public static final DeferredItem<Item> SPAWN_EGG;
    public static final DeferredItem<Item> EMERALDSHARD;
    public static final DeferredItem<Item> LUCKYEGG;
    public static final DeferredItem<Item> POKEDEX;
    public static final DeferredItem<Item> POKEWATCH;
    public static final DeferredItem<Item> REVIVE;
    public static final DeferredItem<Item> TM;

    // Blocks
    public static final DeferredBlock<Block> DEEPSLATE_FOSSIL_ORE;
    public static final DeferredBlock<Block> FOSSIL_ORE;

    public static final DeferredBlock<Block> HEALER;
    public static final DeferredBlock<Block> PC_BASE;
    public static final DeferredBlock<Block> PC_TOP;
    public static final DeferredBlock<Block> REPEL;
    public static final DeferredBlock<Block> TM_MACHINE;
    public static final DeferredBlock<Block> TRADER;

    // Tile Entities
    public static final Supplier<BlockEntityType<?>> HEALER_TYPE;
    public static final Supplier<BlockEntityType<PCTile>> PC_TYPE;
    public static final Supplier<BlockEntityType<?>> REPEL_TYPE;
    public static final Supplier<BlockEntityType<TMTile>> TM_TYPE;
    public static final Supplier<BlockEntityType<TraderTile>> TRADE_TYPE;

    // Containers
    public static final Supplier<MenuType<GenericBarrelMenu>> BARREL_MENU;

    public static boolean resetTimeTags = false;
    public static Vector<Long> times = new Vector<>();

    public static HashMap<ResourceLocation, Item[]> pokecubes = new HashMap<>();

    /** contains pokecubes by name */
    public static List<ResourceLocation> cubeIds = new ArrayList<>();
    /**
     * Items to be considered for re-animation, mapped to the pokedex number to
     * reanimate to.
     */
    public static HashMap<ItemStack, PokedexEntry> fossils = new HashMap<>();

    public static Set<Class<?>> DEFAULT_OWNABLE_TE = new HashSet<>();

    private static Set<ResourceLocation> errored = Sets.newHashSet();

    static
    {
        // Items
        POKEDEX = PokecubeCore.ITEMS.register("pokedex", () -> new ItemPokedex(new Properties().stacksTo(1), false));
        POKEWATCH = PokecubeCore.ITEMS.register("pokewatch", () -> new ItemPokedex(new Properties().stacksTo(1), true));
        BERRYJUICE = PokecubeCore.ITEMS.register("berryjuice", () -> new Item(
                new Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.3F).build())));
        SPAWN_EGG = PokecubeCore.ITEMS.register("pokemobegg", () -> new ItemPokemobEgg(new Properties()));
        CANDY = PokecubeCore.ITEMS.register("candy", () -> new ItemCandy(new Item.Properties().rarity(Rarity.EPIC)));
        REVIVE = PokecubeCore.ITEMS.register("revive", () -> new ItemRevive(new Item.Properties()));

        LUCKYEGG = PokecubeCore.ITEMS.register("luckyegg",
                () -> new ItemRevive(new Item.Properties().rarity(Rarity.RARE)));
        EMERALDSHARD = PokecubeCore.ITEMS.register("emerald_shard", () -> new ItemRevive(new Item.Properties()));

        TM = PokecubeCore.ITEMS.register("tm", () -> new ItemTM(new Item.Properties()));

        FOSSIL_ORE = PokecubeCore.BLOCKS.register("fossil_ore",
                () -> new DropExperienceBlock(UniformInt.of(0, 3),
                        BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops()
                                .strength(3.0f, 3.0f).sound(SoundType.STONE).instrument(NoteBlockInstrument.BASEDRUM)));
        DEEPSLATE_FOSSIL_ORE = PokecubeCore.BLOCKS.register("deepslate_fossil_ore",
                () -> new DropExperienceBlock(UniformInt.of(0, 3),
                        BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).requiresCorrectToolForDrops()
                                .strength(4.5f, 3.0f).sound(SoundType.DEEPSLATE)));

        REPEL = PokecubeCore.BLOCKS.register("repel",
                () -> new RepelBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN)
                        .requiresCorrectToolForDrops().ignitedByLava().isValidSpawn(PokecubeItems::never)
                        .strength(2.0F, 2.5F).requiresCorrectToolForDrops().sound(SoundType.NETHERITE_BLOCK)));
        HEALER = PokecubeCore.BLOCKS.register("pokecenter",
                () -> new HealerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(10)
                        .requiresCorrectToolForDrops().sound(SoundType.NETHERITE_BLOCK)));
        PC_TOP = PokecubeCore.BLOCKS.register("pc_top",
                () -> new PCBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(10)
                        .requiresCorrectToolForDrops().sound(SoundType.NETHERITE_BLOCK), true));
        PC_BASE = PokecubeCore.BLOCKS.register("pc_base",
                () -> new PCBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(10)
                        .requiresCorrectToolForDrops().sound(SoundType.NETHERITE_BLOCK), false));
        TM_MACHINE = PokecubeCore.BLOCKS.register("tm_machine",
                () -> new TMBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).strength(10)
                        .requiresCorrectToolForDrops().sound(SoundType.NETHERITE_BLOCK)));
        TRADER = PokecubeCore.BLOCKS.register("trade_machine",
                () -> new TraderBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(10)
                        .requiresCorrectToolForDrops().sound(SoundType.NETHERITE_BLOCK)));

        // Tile Entity Types
        REPEL_TYPE = PokecubeCore.TILES.register("repel",
                () -> BlockEntityType.Builder.of(RepelTile::new, PokecubeItems.REPEL.get()).build(null));
        HEALER_TYPE = PokecubeCore.TILES.register("pokecenter",
                () -> BlockEntityType.Builder.of(HealerTile::new, PokecubeItems.HEALER.get()).build(null));
        PC_TYPE = PokecubeCore.TILES.register("pc", () -> BlockEntityType.Builder
                .of(PCTile::new, PokecubeItems.PC_TOP.get(), PokecubeItems.PC_BASE.get()).build(null));
        TM_TYPE = PokecubeCore.TILES.register("tm_machine",
                () -> BlockEntityType.Builder.of(TMTile::new, PokecubeItems.TM_MACHINE.get()).build(null));
        TRADE_TYPE = PokecubeCore.TILES.register("trade_machine",
                () -> BlockEntityType.Builder.of(TraderTile::new, PokecubeItems.TRADER.get()).build(null));

        // TODO: Check this
        // Menus
        BARREL_MENU = PokecubeCore.MENU.register("barrel_menu",
                () -> new MenuType<>(GenericBarrelMenu::threeRows, FeatureFlagSet.of()));


        DEFAULT_OWNABLE_TE.add(HealerTile.class);
        DEFAULT_OWNABLE_TE.add(PCTile.class);
        DEFAULT_OWNABLE_TE.add(TraderTile.class);

        Ownable._REGISTRY.register(new HolderProvider.Provider<>()
        {
            final ResourceLocation KEY = ResourceLocation.parse("pokecube:ownable_blocks");
            @Override
            protected ResourceLocation key()
            {
                return KEY;
            }

            @Override
            public Ownable.IOwnableSerializable apply(IAttachmentHolder h)
            {
                if (DEFAULT_OWNABLE_TE.contains(h.getClass())) return new Ownable.ImplTE(PokecubeCore.getConfig().allowRaidingPokecenters, false);
                return null;
            }
        });
    }

    public static void init()
    {
        for (final DeferredHolder<Block, ? extends Block> reg : PokecubeCore.BLOCKS.getEntries())
            PokecubeCore.ITEMS.register(reg.getId().getPath(), () -> new BlockItem(reg.get(), new Item.Properties()));

        for (final DeferredHolder<Block, ? extends Block> reg : PokecubeCore.BERRY_BLOCKS.getEntries())
        {
            PokecubeCore.ITEMS.register(reg.getId().getPath(), () -> new BlockItem(reg.get(), new Item.Properties()));
        }
    }

    public static Boolean ocelotOrParrot(final BlockState state, final BlockGetter reader, final BlockPos pos,
            final EntityType<?> entity)
    {
        return entity == EntityType.OCELOT || entity == EntityType.PARROT;
    }

    public static boolean always(BlockState state, BlockGetter block, BlockPos pos)
    {
        return true;
    }

    public static Boolean never(BlockState state, BlockGetter block, BlockPos pos, EntityType<?> type)
    {
        return Boolean.FALSE;
    }

    public static boolean never(BlockState state, BlockGetter blockGetter, BlockPos pos)
    {
        return Boolean.FALSE;
    }

    /**
     * Registers a pokecube id, the Object[] is an array with the item or block
     * assosicated with the unfilled and filled cubes. example: Object cubes =
     * new Object[] { pokecube, pokecubeFilled}; where pokecube is the unfilled
     * pokecube block, and pokecubeFilled is the filled one. defaults are: 0 -
     * pokecube 1 - greatcube 2 - ultracube 3 - mastercube
     *
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

        PokecubeItems.cubeIds.add(id);
        PokecubeItems.pokecubes.put(id, items);
    }

    public static void deValidate(final ItemStack stack)
    {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null && data.contains("time"))
        {
            var tag = data.copyTag();
            final long time = tag.getLong("time");
            PokecubeItems.times.remove(time);
            tag.remove("time");
            if (tag.isEmpty()) stack.remove(DataComponents.CUSTOM_DATA);
            else stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            stack.split(1);
        }
    }

    /**
     * defaults are: 0 - pokecube 1 - greatcube 2 - ultracube 3 - mastercube if
     * you pass in a non- pokecube stack, it returns 0, defaults to a pokecube.
     *
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
            ret = PokecubeItems.pokecubes.get(PokecubeBehaviour.DEFAULTCUBE)[0];
        }
        catch (final Exception e)
        {
            PokecubeAPI.LOGGER.error("No Cubes Registered!", e);
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
            ret = PokecubeItems.pokecubes.get(PokecubeBehaviour.DEFAULTCUBE)[1];
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
        final TagKey<Item> tag = TagKey.create(RegHelper.ITEM_REGISTRY, loc);
        if (tag != null)
        {
            List<Holder<Item>> items = new ArrayList<>();
            BuiltInRegistries.ITEM.getTagOrEmpty(tag).forEach(items::add);
            if (!items.isEmpty())
            {
                final Holder<Item> item = items.get(new Random(2).nextInt(items.size()));
                if (item != null) return new ItemStack(item);
            }
        }
        final Item item = BuiltInRegistries.ITEM.get(loc);
        if (item != null) return new ItemStack(item);
        if (stacktrace && PokecubeItems.errored.add(loc))
        {
            PokecubeAPI.LOGGER.error(loc + " Not found in list of items.");
            if (PokecubeCore.getConfig().debug_misc)
                PokecubeAPI.LOGGER.error("stacktrace: ", new NullPointerException());
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
                final ResourceLocation name = RegHelper.getKey(block);
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
        if (PokecubeCore.getConfig().debug_data) PokecubeItems.initTags(server);
    }

    private static void initTags(final MinecraftServer server)
    {
        JsonObject json;
        JsonArray array;
        File folder;
        File file;
        FileOutputStream writer;

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
            writer = new FileOutputStream(file);
            writer.write(JsonUtil.gson.toJson(json).getBytes());
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
        for (final ResourceLocation type : IPokecube.PokecubeBehaviour.BEHAVIORS.keySet())
            array.add(PokecubeCore.MODID + ":" + type.getPath() + "cube");
        json.add("values", array);
        file = new File(folder, "pokecubes.json");
        try
        {
            writer = new FileOutputStream(file);
            writer.write(JsonUtil.gson.toJson(json).getBytes());
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
        for (final Entry<DeferredItem<ItemBerry>> type : BerryManager.berryItems.int2ObjectEntrySet())
            array.add(RegHelper.getKey(type.getValue().get()).toString());
        json.add("values", array);
        file = new File(folder, "berries.json");
        try
        {
            writer = new FileOutputStream(file);
            writer.write(JsonUtil.gson.toJson(json).getBytes());
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
            items.sort((a, b) -> RegHelper.getKey(a).compareTo(RegHelper.getKey(b)));
            for (final Item item : items) array.add(RegHelper.getKey(item).toString());
            json.add("values", array);
            final String fileConts = JsonUtil.gson.toJson(json);
            file = new File(folder, name.getPath() + ".json");
            try
            {
                writer = new FileOutputStream(file);
                writer.write(fileConts.getBytes());
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
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null && data.contains("time")) return PokecubeItems.times.contains(data.copyTag().getLong("time"));
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
        if (PokemobCaps.getPokemobUsable(stack) != null) return true;
        if (ADDED_HELD.contains(RegHelper.getKey(stack))) return true;
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
            if (nbt.getLong("" + i) != 0) PokecubeItems.times.add(nbt.getLong("" + i));
    }

    public static ItemStack makeCandyStack()
    {
        final ItemStack candy = PokecubeItems.getStack("candy");
        if (candy.isEmpty()) return ItemStack.EMPTY;
        PokecubeItems.makeStackValid(candy);
        candy.set(DataComponents.ITEM_NAME, Component.translatable("item.pokecube.candy.rare"));
        return candy;
    }

    public static void makeStackValid(final ItemStack stack)
    {
        final long time = System.nanoTime();
        if (PokecubeItems.isValid(stack)) PokecubeItems.deValidate(stack);
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = data == null ? new CompoundTag() : data.copyTag();
        PokecubeItems.times.add(time);
        tag.putLong("time", time);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
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
            PokecubeAPI.LOGGER.error("No Data to save for Item Validations.");
            return;
        }
        for (final Long l : i) if (l != null)
        {
            nbt.putLong("" + num, l);
            num++;
        }
        nbt.putInt("count", num);
    }

    public static boolean stackExists(final String name)
    {
        if (name == null) return false;
        final ResourceLocation loc = PokecubeItems.toPokecubeResource(name);
        final TagKey<Item> old = TagKey.create(RegHelper.ITEM_REGISTRY, loc);
        final Item item = BuiltInRegistries.ITEM.get(loc);
        return old != null || ItemList.pendingTags.containsKey(loc) || item != null;
    }

    public static ResourceLocation toPokecubeResource(final String name)
    {
        return toResource(name, PokecubeCore.MODID);
    }

    public static ResourceLocation toResource(final String name, final String modid)
    {
        ResourceLocation loc;
        if (!name.contains(":")) loc = ResourceLocation.fromNamespaceAndPath(modid, name);
        else loc = ResourceLocation.parse(name);
        return loc;
    }
}
