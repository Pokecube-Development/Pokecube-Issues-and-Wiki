package pokecube.core.init;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.PressurePlateBlock.Sensitivity;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StoneButtonBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WoodButtonBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.events.init.RegisterMiscItems;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.berries.BerryCrop;
import pokecube.core.blocks.berries.BerryFruit;
import pokecube.core.blocks.berries.BerryLeaf;
import pokecube.core.blocks.bookshelves.GenericBookshelf;
import pokecube.core.database.Database;
import pokecube.core.items.ItemFossil;
import pokecube.core.items.ItemTM;
import pokecube.core.items.ItemTyped;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.items.berries.ItemBerry;
import pokecube.core.items.berries.ItemBerry.BerryType;
import pokecube.core.items.megastuff.ItemMegawearable;

public class ItemGenerator
{
    public static interface IMoveModifier
    {
        void processHeldItemUse(MovePacket moveUse, IPokemob mob, ItemStack held);
    }

    public static Map<Predicate<ItemStack>, IMoveModifier> ITEMMODIFIERS = Maps.newHashMap();

    public static ArrayList<String> variants = Lists.newArrayList();
    public static ArrayList<String> other = Lists.newArrayList();
    public static ArrayList<String> fossilVariants = new ArrayList<>();

    public static Map<String, ItemFossil> fossils = Maps.newHashMap();

    public static final Map<String, MaterialColor> berryCrops = Maps.newHashMap();
    public static final Map<String, MaterialColor> berryFruits = Maps.newHashMap();
    public static final Map<String, MaterialColor> berryLeaves = Maps.newHashMap();
    public static final Map<String, MaterialColor> berryWoods = Maps.newHashMap();
    public static final Map<String, MaterialColor> onlyBerryLeaves = Maps.newHashMap();

    public static Map<String, Block> leaves = Maps.newHashMap();
    public static Map<String, Block> logs = Maps.newHashMap();
    public static Map<String, Block> woods = Maps.newHashMap();
    public static Map<String, Block> stripped_logs = Maps.newHashMap();
    public static Map<String, Block> stripped_woods = Maps.newHashMap();
    public static Map<String, Block> bookshelves = Maps.newHashMap();
    public static Map<String, Block> planks = Maps.newHashMap();
    public static Map<String, Block> stairs = Maps.newHashMap();
    public static Map<String, Block> slabs = Maps.newHashMap();
    public static Map<String, Block> fences = Maps.newHashMap();
    public static Map<String, Block> fence_gates = Maps.newHashMap();
    public static Map<String, Block> buttons = Maps.newHashMap();
    public static Map<String, Block> pressure_plates = Maps.newHashMap();
    public static Map<String, Block> trapdoors = Maps.newHashMap();
    public static Map<String, Block> doors = Maps.newHashMap();

    private static Map<String, Block> berry_wood_things = Maps.newHashMap();

    public static Map<Item, Block> potted_berries = Maps.newHashMap();

    private static final List<Function<String, String>> BERRY_WOOD_THINGS = Lists.newArrayList();
    static
    {}

    private static ItemBerry makeBerry(BerryType type)
    {
        final ItemBerry berry = new ItemBerry(type);
        BerryManager.berryItems.put(type.index, berry);
        return berry;
    }

    private static void makeBerries()
    {
        final List<Integer> ids = Lists.newArrayList();
        ids.addAll(BerryManager.berryTypes.keySet());
        PokecubeAPI.LOGGER.debug(BerryManager.berryTypes);
        PokecubeAPI.LOGGER.debug(BerryManager.berryTypes.keySet());
        Collections.sort(ids);
        for (final Integer id : ids)
        {
            final int index = id;
            final BerryType berry = BerryManager.berryTypes.get(index);
            PokecubeAPI.LOGGER.debug("Registering berry_" + berry.name + " " + index + " " + id);
            PokecubeCore.ITEMS.register("berry_" + berry.name, () -> makeBerry(berry));
        }
    }

    private static void makeBerryCrop(int id, int index, String name)
    {
        PokecubeCore.BLOCKS.register("crop_" + name, () -> {
            Block b = new BerryCrop(BlockBehaviour.Properties.of(Material.PLANT, ItemGenerator.berryFruits.get(name))
                    .noCollission().randomTicks().strength(0.0F).noOcclusion().sound(SoundType.CROP), index);
            BerryManager.berryCrops.put(id, b);
            return b;
        });
    }

    private static void makeBerryFruit(int id, int index, String name)
    {
        PokecubeCore.BLOCKS.register("fruit_" + name, () -> {
            Block b = new BerryFruit(BlockBehaviour.Properties.of(Material.PLANT, ItemGenerator.berryCrops.get(name))
                    .noCollission().randomTicks().strength(0.0F).noOcclusion().sound(SoundType.CROP), index);
            BerryManager.berryFruits.put(id, b);
            return b;
        });
    }

    private static void makePottedBerry(int id, int index, String name)
    {
        PokecubeCore.BLOCKS.register("potted_" + name + "_berry", () -> {
            Block b = new GenericPottedPlant(BerryManager.berryCrops.get(index),
                    BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion());
            BerryManager.pottedBerries.put(id, b);
            return b;
        });
    }

    private static void makeBerryWoodThing(String name, int index, String regName, Supplier<Block> block_source,
            Consumer<Block> after)
    {
        PokecubeCore.BLOCKS.register(regName, () -> {
            Block block = block_source.get();
            after.accept(block);
            ItemGenerator.berry_wood_things.put(regName, block);
            return block;
        });
    }

    private static void makeBerryBlocks()
    {
        // make all the crops
        final List<Integer> ids = Lists.newArrayList();
        ids.addAll(BerryManager.berryNames.keySet());
        Collections.sort(ids);
        for (final Integer id : ids)
        {
            final int index = id.intValue();
            final String name = BerryManager.berryNames.get(index);
            // Crop
            makeBerryCrop(id, index, name);
            // Fruit
            makeBerryFruit(id, index, name);
            // Potted berry
            makePottedBerry(id, index, name);
        }

        BERRY_WOOD_THINGS.add(name -> "leaves_" + name);
        BERRY_WOOD_THINGS.add(name -> "log_" + name);
        BERRY_WOOD_THINGS.add(name -> name + "_wood");
        BERRY_WOOD_THINGS.add(name -> "stripped_" + name + "_log");
        BERRY_WOOD_THINGS.add(name -> "stripped_" + name + "_wood");
        BERRY_WOOD_THINGS.add(name -> name + "_bookshelf");
        BERRY_WOOD_THINGS.add(name -> "plank_" + name);
        BERRY_WOOD_THINGS.add(name -> name + "_stairs");
        BERRY_WOOD_THINGS.add(name -> name + "_slab");
        BERRY_WOOD_THINGS.add(name -> name + "_fence");
        BERRY_WOOD_THINGS.add(name -> name + "_fence_gate");
        BERRY_WOOD_THINGS.add(name -> name + "_button");
        BERRY_WOOD_THINGS.add(name -> name + "_pressure_plate");
        BERRY_WOOD_THINGS.add(name -> name + "_trapdoor");
        BERRY_WOOD_THINGS.add(name -> name + "_door");

        // Make the logs and planks.
        final List<String> names = Lists.newArrayList(ItemGenerator.berryWoods.keySet());
        Collections.sort(names);
        for (final String name : names)
        {
            final int index = BerryManager.indexByName.get(name);

            // Leaves
            makeBerryWoodThing(name, index, BERRY_WOOD_THINGS.get(0).apply(name),
                    () -> new BerryLeaf(BlockBehaviour.Properties
                            .of(Material.LEAVES, ItemGenerator.berryLeaves.get(name)).strength(0.2F).randomTicks()
                            .noOcclusion().sound(SoundType.GRASS).isSuffocating((s, r, p) -> false)
                            .isValidSpawn(ItemGenerator::ocelotOrParrot).isViewBlocking((s, r, p) -> false), index),
                    block ->
                    {
                        ItemGenerator.leaves.put(name, block);
                        BerryManager.berryLeaves.put(index, block);
                    });

            // Logs
            makeBerryWoodThing(name, index, BERRY_WOOD_THINGS.get(1).apply(name),
                    () -> Blocks.log(ItemGenerator.berryWoods.get(name), ItemGenerator.berryWoods.get(name)), block ->
                    {
                        ItemGenerator.logs.put(name, block);
                        BerryManager.berryLogs.put(index, block);
                    });

            // Woods
            makeBerryWoodThing(name, index, BERRY_WOOD_THINGS.get(2).apply(name),
                    () -> Blocks.log(ItemGenerator.berryWoods.get(name), ItemGenerator.berryWoods.get(name)), block ->
                    {
                        ItemGenerator.woods.put(name, block);
                    });

            // Stripped Logs
            makeBerryWoodThing(name, index, BERRY_WOOD_THINGS.get(3).apply(name),
                    () -> Blocks.log(ItemGenerator.berryWoods.get(name), ItemGenerator.berryWoods.get(name)), block ->
                    {
                        ItemGenerator.stripped_logs.put(name, block);
                    });

            // Stripped Woods
            makeBerryWoodThing(name, index, BERRY_WOOD_THINGS.get(4).apply(name),
                    () -> Blocks.log(ItemGenerator.berryWoods.get(name), ItemGenerator.berryWoods.get(name)), block ->
                    {
                        ItemGenerator.stripped_woods.put(name, block);
                    });

            // Bookshelves
            makeBerryWoodThing(name, index, BERRY_WOOD_THINGS.get(5).apply(name),
                    () -> new GenericBookshelf(
                            BlockBehaviour.Properties.of(Material.WOOD, ItemGenerator.berryWoods.get(name))
                                    .strength(2.0F).sound(SoundType.WOOD).noOcclusion()),
                    block ->
                    {
                        ItemGenerator.bookshelves.put(name, block);
                    });

            // Planks
            makeBerryWoodThing(name, index, BERRY_WOOD_THINGS.get(6).apply(name),
                    () -> new RotatedPillarBlock(
                            BlockBehaviour.Properties.of(Material.WOOD, ItemGenerator.berryWoods.get(name))
                                    .strength(2.0F).sound(SoundType.WOOD)),
                    block ->
                    {
                        ItemGenerator.planks.put(name, block);
                        BerryManager.berryPlanks.put(index, block);
                    });

            // Stairs
            makeBerryWoodThing(name, index, BERRY_WOOD_THINGS.get(7).apply(name),
                    () -> new GenericStairs(Blocks.OAK_PLANKS.defaultBlockState(),
                            BlockBehaviour.Properties.of(Material.WOOD, ItemGenerator.berryWoods.get(name))
                                    .strength(2.0F).sound(SoundType.WOOD)),
                    block ->
                    {
                        ItemGenerator.stairs.put(name, block);
                    });

            // Slabs
            makeBerryWoodThing(name, index, BERRY_WOOD_THINGS.get(8).apply(name),
                    () -> new SlabBlock(BlockBehaviour.Properties.of(Material.WOOD, ItemGenerator.berryWoods.get(name))
                            .strength(2.0F).sound(SoundType.WOOD)),
                    block ->
                    {
                        ItemGenerator.slabs.put(name, block);
                    });

            // Fences
            makeBerryWoodThing(name, index, BERRY_WOOD_THINGS.get(9).apply(name),
                    () -> new FenceBlock(BlockBehaviour.Properties.of(Material.WOOD, ItemGenerator.berryWoods.get(name))
                            .strength(2.0F).sound(SoundType.WOOD)),
                    block ->
                    {
                        ItemGenerator.slabs.put(name, block);
                    });

            // Fence Gates
            makeBerryWoodThing(name, index, BERRY_WOOD_THINGS.get(10).apply(name),
                    () -> new FenceGateBlock(
                            BlockBehaviour.Properties.of(Material.WOOD, ItemGenerator.berryWoods.get(name))
                                    .strength(2.0F).sound(SoundType.WOOD)),
                    block ->
                    {
                        ItemGenerator.fence_gates.put(name, block);
                    });

            // Buttons
            makeBerryWoodThing(name, index, BERRY_WOOD_THINGS.get(11).apply(name),
                    () -> new GenericWoodButton(
                            BlockBehaviour.Properties.of(Material.WOOD, ItemGenerator.berryWoods.get(name))
                                    .strength(2.0F).sound(SoundType.WOOD)),
                    block ->
                    {
                        ItemGenerator.buttons.put(name, block);
                    });

            // Pressure Plates
            makeBerryWoodThing(name, index, BERRY_WOOD_THINGS.get(12).apply(name),
                    () -> new GenericPressurePlate(Sensitivity.EVERYTHING,
                            BlockBehaviour.Properties.of(Material.WOOD, ItemGenerator.berryWoods.get(name))
                                    .strength(2.0F).sound(SoundType.WOOD)),
                    block ->
                    {
                        ItemGenerator.pressure_plates.put(name, block);
                    });

            // Trapdoors
            makeBerryWoodThing(name, index, BERRY_WOOD_THINGS.get(13).apply(name),
                    () -> new GenericTrapDoor(
                            BlockBehaviour.Properties.of(Material.WOOD, ItemGenerator.berryWoods.get(name))
                                    .strength(2.0F).sound(SoundType.WOOD).noOcclusion()),
                    block ->
                    {
                        ItemGenerator.trapdoors.put(name, block);
                    });

            // Doors
            makeBerryWoodThing(name, index, BERRY_WOOD_THINGS.get(14).apply(name),
                    () -> new GenericDoor(
                            BlockBehaviour.Properties.of(Material.WOOD, ItemGenerator.berryWoods.get(name))
                                    .strength(2.0F).sound(SoundType.WOOD).noOcclusion()),
                    block ->
                    {
                        ItemGenerator.doors.put(name, block);
                    });
        }

        final List<String> leaves = Lists.newArrayList(ItemGenerator.onlyBerryLeaves.keySet());
        for (final String name : leaves)
        {
            final int index = BerryManager.indexByName.get(name);
            // Leaves
            makeBerryWoodThing(name, index, BERRY_WOOD_THINGS.get(0).apply(name),
                    () -> new BerryLeaf(BlockBehaviour.Properties
                            .of(Material.LEAVES, ItemGenerator.onlyBerryLeaves.get(name)).strength(0.2F).randomTicks()
                            .noOcclusion().sound(SoundType.GRASS).isSuffocating((s, r, p) -> false)
                            .isValidSpawn(ItemGenerator::ocelotOrParrot).isViewBlocking((s, r, p) -> false), index),
                    block ->
                    {
                        ItemGenerator.leaves.put(name, block);
                        BerryManager.berryLeaves.put(index, block);
                    });
        }
    }

    private static void makeFossils()
    {
        final Item.Properties props = new Item.Properties().tab(PokecubeItems.TAB_ITEMS);
        for (final String type : ItemGenerator.fossilVariants)
        {
            if (Database.getEntry(type) == null)
            {
                PokecubeAPI.LOGGER.error("No Pokedex entry for {}", type);
                continue;
            }
            PokecubeCore.ITEMS.register("fossil_" + type, () -> {
                final ItemFossil item = new ItemFossil(props, Database.trim(type));
                ItemGenerator.fossils.put(type, item);
                return item;
            });
        }
    }

    private static void makeHeldItems()
    {
        final Item.Properties props = new Item.Properties().tab(PokecubeItems.TAB_ITEMS);
        for (final String type : ItemGenerator.variants)
            PokecubeCore.ITEMS.register(type, () -> new ItemTyped(props, Database.trim(type)));
    }

    private static void makeOtherItems()
    {
        final Item.Properties props = new Item.Properties().tab(PokecubeItems.TAB_ITEMS);
        for (final String type : ItemGenerator.other)
            PokecubeCore.ITEMS.register(type, () -> new ItemTyped(props, Database.trim(type)));
    }

    private static void makeMegaWearables()
    {
        for (final String type : ItemMegawearable.getWearables())
        {
            PokecubeCore.ITEMS.register("mega_" + type, () -> new ItemMegawearable(type));
        }
    }

    private static void makeTMs()
    {
        for (final PokeType type : PokeType.values())
        {
            Item.Properties props = new Item.Properties();
            if (type == PokeType.unknown) props = props.tab(PokecubeItems.TAB_ITEMS);
            final Item.Properties use = props;
            PokecubeCore.ITEMS.register("tm" + type.ordinal(), () -> new ItemTM(use, type));
        }
    }

    private static void makeWoodItems()
    {
        final List<String> names = Lists.newArrayList(ItemGenerator.berryWoods.keySet());
        final List<String> leaves = Lists.newArrayList(ItemGenerator.onlyBerryLeaves.keySet());
        Collections.sort(names);
        for (final String name : names)
        {
            for (var regger : BERRY_WOOD_THINGS)
            {
                String regName = regger.apply(name);
                Item.Properties props = new Item.Properties().tab(PokecubeItems.TAB_BERRIES);
                PokecubeCore.ITEMS.register(regName, () -> new BlockItem(berry_wood_things.get(regName), props));
            }
        }
        for (final String name : leaves)
        {
            String regName = BERRY_WOOD_THINGS.get(0).apply(name);
            Item.Properties props = new Item.Properties().tab(PokecubeItems.TAB_BERRIES);
            PokecubeCore.ITEMS.register(regName, () -> new BlockItem(berry_wood_things.get(regName), props));
        }
    }

    public static class GenericStairs extends StairBlock
    {
        @SuppressWarnings("deprecation")
        public GenericStairs(final BlockState blockForStairs, final Properties properties)
        {
            super(blockForStairs, properties);
        }
    }

    public static RotatedPillarBlock stoneLog(final MaterialColor color1, final MaterialColor color2,
            final BlockBehaviour.Properties properties)
    {
        return new RotatedPillarBlock(BlockBehaviour.Properties.of(Material.STONE, (state) -> {
            return state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? color1 : color2;
        }).strength(2.4f, 6.0f).sound(SoundType.STONE).requiresCorrectToolForDrops());
    }

    public static class GenericTrapDoor extends TrapDoorBlock
    {
        public GenericTrapDoor(final Properties properties)
        {
            super(properties);
        }
    }

    public static class GenericDoor extends DoorBlock
    {
        public GenericDoor(final Properties properties)
        {
            super(properties);
        }
    }

    public static class GenericWoodButton extends WoodButtonBlock
    {
        public GenericWoodButton(final Properties properties)
        {
            super(properties);
        }
    }

    public static class GenericStoneButton extends StoneButtonBlock
    {
        public GenericStoneButton(final Properties properties)
        {
            super(properties);
        }
    }

    public static class GenericPressurePlate extends PressurePlateBlock
    {
        public GenericPressurePlate(final Sensitivity sesitivity, final Properties properties)
        {
            super(sesitivity, properties);
        }
    }

    public static class GenericPottedPlant extends FlowerPotBlock
    {
        @SuppressWarnings("deprecation")
        public GenericPottedPlant(final Block pottedPlant, final Properties properties)
        {
            super(pottedPlant, properties);
        }
    }

    public static void addStrippables(final Block logs, final Block strippedLogs)
    {
        AxeItem.STRIPPABLES = Maps.newHashMap(AxeItem.STRIPPABLES);
        AxeItem.STRIPPABLES.put(logs, strippedLogs);
    }

    public static void strippableBlocks(final FMLLoadCompleteEvent event)
    {
        final List<String> names = Lists.newArrayList(ItemGenerator.berryWoods.keySet());
        Collections.sort(names);
        // Enqueue this so that it runs on main thread, to prevent concurrency
        // issues.
        event.enqueueWork(() -> {
            for (final String name : names)
            {
                ItemGenerator.addStrippables(ItemGenerator.logs.get(name), ItemGenerator.stripped_logs.get(name));
                ItemGenerator.addStrippables(ItemGenerator.woods.get(name), ItemGenerator.stripped_woods.get(name));
            }
        });
    }

    public static void compostableBlocks(final float chance, final ItemLike item)
    {
        ComposterBlock.COMPOSTABLES.put(item, chance);
    }

    public static void compostables(final FMLLoadCompleteEvent event)
    {
        final List<String> leaves = Lists.newArrayList(ItemGenerator.berryLeaves.keySet());
        final List<String> onlyLeaves = Lists.newArrayList(ItemGenerator.onlyBerryLeaves.keySet());
        final List<Integer> ids = Lists.newArrayList(BerryManager.berryItems.keySet());
        Collections.sort(leaves);
        Collections.sort(onlyLeaves);
        Collections.sort(ids);
        event.enqueueWork(() -> {
            for (final String name : leaves)
                ItemGenerator.compostableBlocks(0.3f, ItemGenerator.leaves.get(name).asItem());
            for (final String name : onlyLeaves)
                ItemGenerator.compostableBlocks(0.3f, ItemGenerator.leaves.get(name).asItem());
            for (final Integer id : ids)
            {
                final int index = id;
                ItemGenerator.compostableBlocks(0.65f, BerryManager.berryItems.get(index));
            }
        });
    }

    public static void flammableBlocks(final Block block, final int speed, final int flammability)
    {
        final FireBlock fire = (FireBlock) Blocks.FIRE;
        fire.setFlammable(block, speed, flammability);
    }

    public static void flammables(final FMLLoadCompleteEvent event)
    {
        final List<String> names = Lists.newArrayList(ItemGenerator.berryWoods.keySet());
        Collections.sort(names);
        event.enqueueWork(() -> {
            for (final String name : names)
            {
                // Logs
                ItemGenerator.flammableBlocks(ItemGenerator.logs.get(name), 5, 5);
                ItemGenerator.flammableBlocks(ItemGenerator.woods.get(name), 5, 5);
                ItemGenerator.flammableBlocks(ItemGenerator.stripped_logs.get(name), 5, 5);
                ItemGenerator.flammableBlocks(ItemGenerator.stripped_woods.get(name), 5, 5);

                // Leaves
                ItemGenerator.flammableBlocks(ItemGenerator.leaves.get(name), 30, 60);

                // Woods
                ItemGenerator.flammableBlocks(ItemGenerator.planks.get(name), 5, 20);
                ItemGenerator.flammableBlocks(ItemGenerator.slabs.get(name), 5, 20);
                ItemGenerator.flammableBlocks(ItemGenerator.stairs.get(name), 5, 20);
                ItemGenerator.flammableBlocks(ItemGenerator.fences.get(name), 5, 20);
                ItemGenerator.flammableBlocks(ItemGenerator.fence_gates.get(name), 5, 20);
                ItemGenerator.flammableBlocks(ItemGenerator.bookshelves.get(name), 5, 20);
            }
        });
    }

    public static Boolean ocelotOrParrot(final BlockState state, final BlockGetter reader, final BlockPos pos,
            final EntityType<?> entity)
    {
        return entity == EntityType.OCELOT || entity == EntityType.PARROT;
    }

    public static void postInitItems()
    {
        for (final String type : ItemGenerator.fossilVariants)
            PokecubeItems.registerFossil(new ItemStack(ItemGenerator.fossils.get(type)), type);
    }

    public static void processHeldItemUse(final MovePacket moveUse, final IPokemob mob, final ItemStack held)
    {
        for (final Map.Entry<Predicate<ItemStack>, IMoveModifier> entry : ItemGenerator.ITEMMODIFIERS.entrySet())
            if (entry.getKey().test(held)) entry.getValue().processHeldItemUse(moveUse, mob, held);
    }

    public static void registerItems()
    {
        ItemGenerator.makeFossils();
        ItemGenerator.makeHeldItems();
        ItemGenerator.makeOtherItems();
        ItemGenerator.makeMegaWearables();
        ItemGenerator.makeWoodItems();
        ItemGenerator.makeTMs();
    }

    public static void init()
    {
        // Initialize the nullberry
        ItemBerry.registerBerryType("null", null, 0, 0, 0, 0, 0, 0);
        // Fire event so that others can initialize their berries.
        PokecubeAPI.POKEMOB_BUS.post(new RegisterMiscItems());
        // Make the berries here.
        ItemGenerator.makeBerries();
        ItemGenerator.makeBerryBlocks();
    }
}
