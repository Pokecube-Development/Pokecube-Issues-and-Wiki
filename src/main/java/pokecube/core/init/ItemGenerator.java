package pokecube.core.init;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
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
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.registries.RegistryObject;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.events.init.RegisterMiscItems;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.berries.BerryCrop;
import pokecube.core.blocks.berries.BerryFruit;
import pokecube.core.blocks.berries.BerryLeaf;
import pokecube.core.blocks.bookshelves.GenericBookshelf;
import pokecube.core.blocks.signs.GenericSignBlockEntity;
import pokecube.core.blocks.signs.GenericStandingSign;
import pokecube.core.blocks.signs.GenericWallSign;
import pokecube.core.database.Database;
import pokecube.core.entity.boats.GenericBoat;
import pokecube.core.entity.boats.GenericBoat.BoatRegister;
import pokecube.core.entity.boats.GenericBoat.BoatType;
import pokecube.core.items.GenericBoatItem;
import pokecube.core.items.ItemFossil;
import pokecube.core.items.ItemTyped;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.items.berries.ItemBerry;
import pokecube.core.items.berries.ItemBerry.BerryType;
import pokecube.core.items.megastuff.ItemMegawearable;

public class ItemGenerator
{
    public static interface IMoveModifier
    {
        void processHeldItemUse(MoveApplication moveUse, IPokemob mob, ItemStack held);
    }

    public static Map<Predicate<ItemStack>, IMoveModifier> ITEMMODIFIERS = Maps.newHashMap();

    public static ArrayList<String> variants = Lists.newArrayList();
    public static ArrayList<String> other = Lists.newArrayList();
    public static ArrayList<String> fossilVariants = new ArrayList<>();

    public static Map<String, RegistryObject<ItemFossil>> fossils = Maps.newHashMap();

    public static final Map<String, MapColor> berryCrops = Maps.newHashMap();
    public static final Map<String, MapColor> berryFruits = Maps.newHashMap();
    public static final Map<String, MapColor> berryLeaves = Maps.newHashMap();
    public static final Map<String, MapColor> berryWoods = Maps.newHashMap();
    public static final Map<String, MapColor> onlyBerryLeaves = Maps.newHashMap();

    public static Map<String, RegistryObject<Block>> leaves = Maps.newHashMap();
    public static Map<String, RegistryObject<Block>> logs = Maps.newHashMap();
    public static Map<String, RegistryObject<Block>> woods = Maps.newHashMap();
    public static Map<String, RegistryObject<Block>> stripped_logs = Maps.newHashMap();
    public static Map<String, RegistryObject<Block>> stripped_woods = Maps.newHashMap();
    public static Map<String, RegistryObject<Block>> bookshelves = Maps.newHashMap();
    public static Map<String, RegistryObject<Block>> planks = Maps.newHashMap();
    public static Map<String, RegistryObject<Block>> stairs = Maps.newHashMap();
    public static Map<String, RegistryObject<Block>> slabs = Maps.newHashMap();
    public static Map<String, RegistryObject<Block>> fences = Maps.newHashMap();
    public static Map<String, RegistryObject<Block>> fence_gates = Maps.newHashMap();
    public static Map<String, RegistryObject<Block>> buttons = Maps.newHashMap();
    public static Map<String, RegistryObject<Block>> pressure_plates = Maps.newHashMap();
    public static Map<String, RegistryObject<Block>> trapdoors = Maps.newHashMap();
    public static Map<String, RegistryObject<Block>> doors = Maps.newHashMap();

    public static Map<String, RegistryObject<Block>> berry_wall_signs = Maps.newHashMap();
    public static Map<String, RegistryObject<Block>> berry_signs = Maps.newHashMap();
    public static Map<String, RegistryObject<Item>> berry_sign_items = Maps.newHashMap();

    private static Map<String, RegistryObject<Block>> berry_wood_things = Maps.newHashMap();

    public static Map<Item, RegistryObject<Block>> potted_berries = Maps.newHashMap();

    public static List<RegistryObject<Block>> SIGN_BLOCKS = Lists.newArrayList();

    public static List<BoatRegister> BOATS = Lists.newArrayList();

    private static final List<Function<String, String>> BERRY_WOOD_THINGS = Lists.newArrayList();
    private static final Set<String> NO_ITEMS = Sets.newHashSet();
    static
    {}

    private static void makeBerry(BerryType type)
    {
        RegistryObject<ItemBerry> berry = PokecubeCore.ITEMS.register("berry_" + type.name, () -> new ItemBerry(type));
        BerryManager.addBerry(berry, type);
    }

    private static void makeBerries()
    {
        final List<Integer> ids = Lists.newArrayList();
        ids.addAll(BerryManager.berryTypes.keySet());
        if (PokecubeCore.getConfig().debug_data)
        {
            PokecubeAPI.logInfo(BerryManager.berryTypes);
            PokecubeAPI.logInfo(BerryManager.berryTypes.keySet());
        }
        Collections.sort(ids);
        for (final Integer id : ids)
        {
            final int index = id;
            final BerryType berry = BerryManager.berryTypes.get(index);
            if (PokecubeCore.getConfig().debug_data)
                PokecubeAPI.logInfo("Registering berry_" + berry.name + " " + index + " " + id);
            makeBerry(berry);
        }
    }

    private static void makeBerryCrop(int id, int index, String name)
    {
        var reg = PokecubeCore.BLOCKS.register("crop_" + name, () -> {
            Block b = new BerryCrop(BlockBehaviour.Properties.of().mapColor(ItemGenerator.berryFruits.get(name))
                    .strength(0.0F).noCollission().randomTicks().noOcclusion()
                    .sound(SoundType.CROP).pushReaction(PushReaction.DESTROY), index);
            return b;
        });
        BerryManager.berryCrops.put(id, reg);
    }

    private static void makeBerryFruit(int id, int index, String name)
    {
        var reg = PokecubeCore.BLOCKS.register("fruit_" + name, () -> {
            Block b = new BerryFruit(BlockBehaviour.Properties.of().mapColor(ItemGenerator.berryCrops.get(name))
                    .strength(0.0F).noCollission().randomTicks().noOcclusion()
                    .sound(SoundType.SWEET_BERRY_BUSH).pushReaction(PushReaction.DESTROY), index);
            return b;
        });
        BerryManager.berryFruits.put(id, reg);
    }

    // TODO: Check this
    private static void makePottedBerry(int id, int index, String name)
    {
        var reg = PokecubeCore.BLOCKS.register("potted_" + name + "_berry", () -> {
            Block b = new GenericPottedPlant(BerryManager.berryCrops.get(index).get(),
                    BlockBehaviour.Properties.of().instabreak().pushReaction(PushReaction.DESTROY).noOcclusion());
            return b;
        });
        BerryManager.pottedBerries.put(id, reg);
    }

    private static RegistryObject<Block> makeBerryWoodThing(String name, int index, String regName,
            Supplier<Block> block_source, Consumer<RegistryObject<Block>> after)
    {
        var reg = PokecubeCore.BLOCKS.register(regName, () -> {
            Block block = block_source.get();
            return block;
        });
        after.accept(reg);
        ItemGenerator.berry_wood_things.put(regName, reg);
        return reg;
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

        // Signs
        BERRY_WOOD_THINGS.add(name -> name + "_wall_sign");
        BERRY_WOOD_THINGS.add(name -> name + "_sign");

        // Make the logs and planks.
        final List<String> names = Lists.newArrayList(ItemGenerator.berryWoods.keySet());
        Collections.sort(names);
        for (final String name : names)
        {
            final int index = BerryManager.indexByName.get(name);

            // Leaves
            makeBerryWoodThing(name, index, BERRY_WOOD_THINGS.get(0).apply(name),
                    () -> new BerryLeaf(BlockBehaviour.Properties
                            .of().mapColor(ItemGenerator.berryLeaves.get(name)).strength(0.2F).randomTicks().noOcclusion()
                            .sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY).isValidSpawn(ItemGenerator::ocelotOrParrot)
                            .isSuffocating((s, r, p) -> false).isViewBlocking((s, r, p) -> false), index),
                    block ->
                    {
                        ItemGenerator.leaves.put(name, block);
                        BerryManager.berryLeaves.put(index, block);
                    });

            // Logs
            makeBerryWoodThing(name, index, BERRY_WOOD_THINGS.get(1).apply(name),
                    () -> Blocks.log(ItemGenerator.berryWoods.get(name), ItemGenerator.berryWoods.get(name), SoundType.WOOD), block ->
                    {
                        ItemGenerator.logs.put(name, block);
                        BerryManager.berryLogs.put(index, block);
                    });

            // Woods
            makeBerryWoodThing(name, index, BERRY_WOOD_THINGS.get(2).apply(name),
                    () -> Blocks.log(ItemGenerator.berryWoods.get(name), ItemGenerator.berryWoods.get(name), SoundType.WOOD), block ->
                    {
                        ItemGenerator.woods.put(name, block);
                    });

            // Stripped Logs
            makeBerryWoodThing(name, index, BERRY_WOOD_THINGS.get(3).apply(name),
                    () -> Blocks.log(ItemGenerator.berryWoods.get(name), ItemGenerator.berryWoods.get(name), SoundType.WOOD), block ->
                    {
                        ItemGenerator.stripped_logs.put(name, block);
                    });

            // Stripped Woods
            makeBerryWoodThing(name, index, BERRY_WOOD_THINGS.get(4).apply(name),
                    () -> Blocks.log(ItemGenerator.berryWoods.get(name), ItemGenerator.berryWoods.get(name), SoundType.WOOD), block ->
                    {
                        ItemGenerator.stripped_woods.put(name, block);
                    });

            // Bookshelves
            makeBerryWoodThing(name, index, BERRY_WOOD_THINGS.get(5).apply(name),
                    () -> new GenericBookshelf(
                            BlockBehaviour.Properties.of().mapColor(ItemGenerator.berryWoods.get(name))
                                    .strength(2.0F).sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS).ignitedByLava().noOcclusion()),
                    block ->
                    {
                        ItemGenerator.bookshelves.put(name, block);
                    });

            // Planks
            var plank_block = makeBerryWoodThing(name, index, BERRY_WOOD_THINGS.get(6).apply(name),
                    () -> new RotatedPillarBlock(
                            BlockBehaviour.Properties.of().mapColor(ItemGenerator.berryWoods.get(name))
                                    .strength(2.0F).sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS).ignitedByLava()),
                    block ->
                    {
                        ItemGenerator.planks.put(name, block);
                        BerryManager.berryPlanks.put(index, block);
                    });

            ItemGenerator.BOATS.add(new BoatRegister(plank_block, name, PokecubeItems.TAB_BERRIES, PokecubeCore.ITEMS));

            // Stairs
            makeBerryWoodThing(name, index, BERRY_WOOD_THINGS.get(7).apply(name),
                    () -> new GenericStairs(Blocks.OAK_PLANKS.defaultBlockState(),
                            BlockBehaviour.Properties.of().mapColor(ItemGenerator.berryWoods.get(name))
                                    .strength(2.0F).sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS).ignitedByLava()),
                    block ->
                    {
                        ItemGenerator.stairs.put(name, block);
                    });

            // Slabs
            makeBerryWoodThing(name, index, BERRY_WOOD_THINGS.get(8).apply(name),
                    () -> new SlabBlock(BlockBehaviour.Properties.of().mapColor(ItemGenerator.berryWoods.get(name))
                            .strength(2.0F).sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS).ignitedByLava()),
                    block ->
                    {
                        ItemGenerator.slabs.put(name, block);
                    });

            // Fences
            makeBerryWoodThing(name, index, BERRY_WOOD_THINGS.get(9).apply(name),
                    () -> new FenceBlock(BlockBehaviour.Properties.of().mapColor(ItemGenerator.berryWoods.get(name))
                            .strength(2.0F).sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS).ignitedByLava()),
                    block ->
                    {
                        ItemGenerator.slabs.put(name, block);
                    });

            // Fence Gates
            makeBerryWoodThing(name, index, BERRY_WOOD_THINGS.get(10).apply(name),
                    () -> new FenceGateBlock(
                            BlockBehaviour.Properties.of().mapColor(ItemGenerator.berryWoods.get(name))
                                    .strength(2.0F).sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)
                                    .forceSolidOn().ignitedByLava().forceSolidOn(), SoundEvents.FENCE_GATE_CLOSE, SoundEvents.FENCE_GATE_OPEN),
                    block ->
                    {
                        ItemGenerator.fence_gates.put(name, block);
                    });

            // TODO: Check this
            // Buttons
            makeBerryWoodThing(name, index, BERRY_WOOD_THINGS.get(11).apply(name),
                    () -> Blocks.woodenButton(BlockSetType.OAK),
                    block ->
                    {
                        ItemGenerator.buttons.put(name, block);
                    });

            // Pressure Plates
            makeBerryWoodThing(name, index, BERRY_WOOD_THINGS.get(12).apply(name),
                    () -> new GenericPressurePlate(Sensitivity.EVERYTHING, BlockSetType.OAK,
                            BlockBehaviour.Properties.of().mapColor(ItemGenerator.berryWoods.get(name))
                                    .strength(2.0F).forceSolidOn().noCollission().ignitedByLava()
                                    .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS).pushReaction(PushReaction.DESTROY)),
                    block ->
                    {
                        ItemGenerator.pressure_plates.put(name, block);
                    });

            // Trapdoors
            makeBerryWoodThing(name, index, BERRY_WOOD_THINGS.get(13).apply(name),
                    () -> new GenericTrapDoor(BlockSetType.OAK,
                            BlockBehaviour.Properties.of().mapColor(ItemGenerator.berryWoods.get(name))
                                    .strength(2.0F).noOcclusion().ignitedByLava().isValidSpawn(ItemGenerator::never)
                                    .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)),
                    block ->
                    {
                        ItemGenerator.trapdoors.put(name, block);
                    });

            // Doors
            makeBerryWoodThing(name, index, BERRY_WOOD_THINGS.get(14).apply(name),
                    () -> new GenericDoor(BlockSetType.OAK,
                            BlockBehaviour.Properties.of().mapColor(ItemGenerator.berryWoods.get(name))
                                    .strength(2.0F).noOcclusion().ignitedByLava().pushReaction(PushReaction.DESTROY)
                                    .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS)),
                    block ->
                    {
                        ItemGenerator.doors.put(name, block);
                    });

            // Sign stuff, first make the wood type.
            WoodType type = BerriesWoodType.addWoodTypes(name);
            // sign_blocks
            var standing_sign = makeBerryWoodThing(name, index, BERRY_WOOD_THINGS.get(16).apply(name),
                    () -> new GenericStandingSign(
                            BlockBehaviour.Properties.of().mapColor(ItemGenerator.berryWoods.get(name))
                                    .strength(2.0F).noOcclusion().noCollission().forceSolidOn().ignitedByLava()
                                    .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS),
                            type),
                    block ->
                    {
                        ItemGenerator.berry_signs.put(name, block);
                    });
            // TODO: Check this
            // wall_sign_blocks
            var wall_sign = makeBerryWoodThing(name, index, BERRY_WOOD_THINGS.get(15).apply(name),
                    () -> new GenericWallSign(
                            BlockBehaviour.Properties.of().mapColor(ItemGenerator.berryWoods.get(name))
                                    .strength(2.0F).noOcclusion().noCollission().forceSolidOn().ignitedByLava()
                                    .dropsLike(standing_sign.get()).sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS),
                            type),
                    block ->
                    {
                        ItemGenerator.berry_wall_signs.put(name, block);
                    });
            NO_ITEMS.add(BERRY_WOOD_THINGS.get(15).apply(name));
            ItemGenerator.SIGN_BLOCKS.add(standing_sign);
            ItemGenerator.SIGN_BLOCKS.add(wall_sign);
            NO_ITEMS.add(BERRY_WOOD_THINGS.get(16).apply(name));
            PokecubeCore.ITEMS.register(BERRY_WOOD_THINGS.get(16).apply(name),
                    () -> new SignItem(new Item.Properties().stacksTo(16),
                            standing_sign.get(), wall_sign.get()));
        }

        final List<String> leaves = Lists.newArrayList(ItemGenerator.onlyBerryLeaves.keySet());
        for (final String name : leaves)
        {
            final int index = BerryManager.indexByName.get(name);
            // Leaves
            makeBerryWoodThing(name, index, BERRY_WOOD_THINGS.get(0).apply(name),
                    () -> new BerryLeaf(BlockBehaviour.Properties.of().mapColor(ItemGenerator.onlyBerryLeaves.get(name))
                            .strength(0.2F).randomTicks().noOcclusion().ignitedByLava()
                            .sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)
                            .isSuffocating((s, r, p) -> false).isViewBlocking((s, r, p) -> false)
                            .isRedstoneConductor((s, r, p) -> false).isValidSpawn(ItemGenerator::ocelotOrParrot), index),
                    block ->
                    {
                        ItemGenerator.leaves.put(name, block);
                        BerryManager.berryLeaves.put(index, block);
                    });
        }
    }

    private static void makeFossils()
    {
        final Item.Properties props = new Item.Properties();
        for (final String type : ItemGenerator.fossilVariants)
        {
            if (Database.getEntry(type) == null)
            {
                PokecubeAPI.LOGGER.error("No Pokedex entry for {}", type);
                continue;
            }
            var reg = PokecubeCore.ITEMS.register("fossil_" + type, () -> {
                final ItemFossil item = new ItemFossil(props, Database.trim(type));
                return item;
            });
            ItemGenerator.fossils.put(type, reg);
        }
    }

    private static void makeHeldItems()
    {
        final Item.Properties props = new Item.Properties();
        for (final String type : ItemGenerator.variants)
            PokecubeCore.ITEMS.register(type, () -> new ItemTyped(props, Database.trim(type)));
    }

    private static void makeOtherItems()
    {
        final Item.Properties props = new Item.Properties();
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
                if (NO_ITEMS.contains(regName)) continue;
                Item.Properties props = new Item.Properties();
                PokecubeCore.ITEMS.register(regName, () -> new BlockItem(berry_wood_things.get(regName).get(), props));
            }
        }
        for (final String name : leaves)
        {
            String regName = BERRY_WOOD_THINGS.get(0).apply(name);
            Item.Properties props = new Item.Properties();
            PokecubeCore.ITEMS.register(regName, () -> new BlockItem(berry_wood_things.get(regName).get(), props));
        }
    }

    private static class BoatGiver implements Supplier<BoatType>
    {
        BoatType type = null;

        @Override
        public BoatType get()
        {
            return type;
        }
    }

    public static void makeBoats()
    {
        ItemGenerator.BOATS.forEach(boat -> {
            String regName = boat.name() + "_boat";
            BoatGiver giver = new BoatGiver();
            RegistryObject<Item> reg = boat.register().register(regName,
                    () -> new GenericBoatItem(giver.get(), false, new Item.Properties().stacksTo(1)));
            regName = boat.name() + "_chest_boat";
            RegistryObject<Item> chest_reg = boat.register().register(regName,
                    () -> new GenericBoatItem(giver.get(), true, new Item.Properties().stacksTo(1)));
            giver.type = GenericBoat.registerBoat(boat.block(), reg, chest_reg, boat.name());
        });
    }

    public static void makeSigns()
    {
        if (!SIGN_BLOCKS.isEmpty())
        {
            GenericSignBlockEntity.SIGN_TYPE = PokecubeCore.TILES.register("sign", () -> {
                List<Block> regs = Lists.newArrayList();
                SIGN_BLOCKS.forEach(r -> regs.add(r.get()));
                Block[] blocks = regs.toArray(new Block[0]);
                var type = BlockEntityType.Builder.of(GenericSignBlockEntity::new, blocks).build(null);
                return type;
            });
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

    public static RotatedPillarBlock stoneLog(final MapColor mapColorSide, final MapColor mapColorTop, NoteBlockInstrument instrument,
                                              float destroyTIme, float explosionResistance)
    {
        return new RotatedPillarBlock(BlockBehaviour.Properties.of().strength(destroyTIme, explosionResistance)
                .sound(SoundType.STONE).instrument(instrument).requiresCorrectToolForDrops().mapColor((state) -> {
            return state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? mapColorSide : mapColorTop;
        }));
    }

    public static RotatedPillarBlock stoneLog(final MapColor mapColorSide, final MapColor mapColorTop, SoundType sound,
                                              NoteBlockInstrument instrument, float destroyTime, float explosionResistance)
    {
        return new RotatedPillarBlock(BlockBehaviour.Properties.of().strength(destroyTime, explosionResistance)
                .sound(sound).instrument(instrument).requiresCorrectToolForDrops().mapColor((state) -> {
                    return state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? mapColorSide : mapColorTop;
                }));
    }

    public static class GenericTrapDoor extends TrapDoorBlock
    {
        public GenericTrapDoor(final BlockSetType setType, final Properties properties)
        {
            super(properties, setType);
        }
    }

    public static class GenericDoor extends DoorBlock
    {
        public GenericDoor(final BlockSetType setType, final Properties properties)
        {
            super(properties, setType);
        }
    }

    public static class GenericPressurePlate extends PressurePlateBlock
    {
        public GenericPressurePlate(final Sensitivity sensitivity, final BlockSetType setType, final Properties properties)
        {
            super(sensitivity, properties, setType);
        }
    }

    public static class GenericWoodButton extends ButtonBlock
    {
        public GenericWoodButton(BlockSetType setType, boolean arrowsCanPress, int ticksPressed, final Properties properties)
        {
            super(properties, setType, ticksPressed, arrowsCanPress);
        }
    }
    public static class GenericStoneButton extends ButtonBlock
    {
        public GenericStoneButton(BlockSetType setType, boolean arrowsCanPress, int ticksPressed, final Properties properties)
        {
            super(properties, setType, ticksPressed, arrowsCanPress);
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
                ItemGenerator.addStrippables(ItemGenerator.logs.get(name).get(),
                        ItemGenerator.stripped_logs.get(name).get());
                ItemGenerator.addStrippables(ItemGenerator.woods.get(name).get(),
                        ItemGenerator.stripped_woods.get(name).get());
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
                ItemGenerator.compostableBlocks(0.3f, ItemGenerator.leaves.get(name).get().asItem());
            for (final String name : onlyLeaves)
                ItemGenerator.compostableBlocks(0.3f, ItemGenerator.leaves.get(name).get().asItem());
            for (final Integer id : ids)
            {
                final int index = id;
                ItemGenerator.compostableBlocks(0.65f, BerryManager.berryItems.get(index).get());
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
                ItemGenerator.flammableBlocks(ItemGenerator.logs.get(name).get(), 5, 5);
                ItemGenerator.flammableBlocks(ItemGenerator.woods.get(name).get(), 5, 5);
                ItemGenerator.flammableBlocks(ItemGenerator.stripped_logs.get(name).get(), 5, 5);
                ItemGenerator.flammableBlocks(ItemGenerator.stripped_woods.get(name).get(), 5, 5);

                // Leaves
                ItemGenerator.flammableBlocks(ItemGenerator.leaves.get(name).get(), 30, 60);

                // Woods
                ItemGenerator.flammableBlocks(ItemGenerator.planks.get(name).get(), 5, 20);
                ItemGenerator.flammableBlocks(ItemGenerator.slabs.get(name).get(), 5, 20);
                ItemGenerator.flammableBlocks(ItemGenerator.stairs.get(name).get(), 5, 20);
                ItemGenerator.flammableBlocks(ItemGenerator.fences.get(name).get(), 5, 20);
                ItemGenerator.flammableBlocks(ItemGenerator.fence_gates.get(name).get(), 5, 20);
                ItemGenerator.flammableBlocks(ItemGenerator.bookshelves.get(name).get(), 5, 20);
            }
        });
    }

    public static Boolean ocelotOrParrot(final BlockState state, final BlockGetter reader, final BlockPos pos,
            final EntityType<?> entity)
    {
        return entity == EntityType.OCELOT || entity == EntityType.PARROT;
    }

    public static Boolean never(BlockState state, BlockGetter block, BlockPos pos, EntityType<?> type)
    {
        return Boolean.FALSE;
    }

    public static void postInitItems()
    {
        for (final String type : ItemGenerator.fossilVariants)
            PokecubeItems.registerFossil(new ItemStack(ItemGenerator.fossils.get(type).get()), type);
    }

    public static void processHeldItemUse(final MoveApplication moveUse, final IPokemob mob, final ItemStack held)
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
        ItemGenerator.makeBoats();
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
        ItemGenerator.makeSigns();
    }
}
