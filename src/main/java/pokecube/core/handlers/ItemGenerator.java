package pokecube.core.handlers;

import static net.minecraft.item.AxeItem.STRIPABLES;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.*;
import net.minecraft.block.PressurePlateBlock.Sensitivity;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.registries.IForgeRegistry;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.berries.BerryCrop;
import pokecube.core.blocks.berries.BerryFruit;
import pokecube.core.blocks.berries.BerryLeaf;
import pokecube.core.database.Database;
import pokecube.core.events.onload.RegisterMiscItems;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.items.ItemFossil;
import pokecube.core.items.ItemTM;
import pokecube.core.items.ItemTyped;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.items.berries.ItemBerry;
import pokecube.core.items.berries.ItemBerry.BerryType;
import pokecube.core.items.megastuff.ItemMegawearable;
import pokecube.core.utils.PokeType;

public class ItemGenerator
{
    public static interface IMoveModifier
    {
        void processHeldItemUse(MovePacket moveUse, IPokemob mob, ItemStack held);
    }

    public static Map<Predicate<ItemStack>, IMoveModifier> ITEMMODIFIERS = Maps.newHashMap();

    public static ArrayList<String> variants       = Lists.newArrayList();
    public static ArrayList<String> other          = Lists.newArrayList();
    public static ArrayList<String> fossilVariants = new ArrayList<>();

    public static Map<String, ItemFossil> fossils = Maps.newHashMap();

    public static final Map<String, MaterialColor> berryCrops = Maps.newHashMap();
    public static final Map<String, MaterialColor> berryFruits = Maps.newHashMap();
    public static final Map<String, MaterialColor> berryLeaves = Maps.newHashMap();
    public static final Map<String, MaterialColor> berryWoods = Maps.newHashMap();
    public static final Map<String, MaterialColor> onlyBerryLeaves = Maps.newHashMap();

    public static Map<String, Block> leaves          = Maps.newHashMap();
    public static Map<String, Block> logs            = Maps.newHashMap();
    public static Map<String, Block> woods           = Maps.newHashMap();
    public static Map<String, Block> stripped_logs   = Maps.newHashMap();
    public static Map<String, Block> stripped_woods  = Maps.newHashMap();
    public static Map<String, Block> planks          = Maps.newHashMap();
    public static Map<String, Block> stairs          = Maps.newHashMap();
    public static Map<String, Block> slabs           = Maps.newHashMap();
    public static Map<String, Block> fences          = Maps.newHashMap();
    public static Map<String, Block> fence_gates     = Maps.newHashMap();
    public static Map<String, Block> pressure_plates = Maps.newHashMap();
    public static Map<String, Block> buttons         = Maps.newHashMap();
    public static Map<String, Block> trapdoors       = Maps.newHashMap();
    public static Map<String, Block> doors           = Maps.newHashMap();

    public static void makeBerries(final IForgeRegistry<Item> registry)
    {
        final List<Integer> ids = Lists.newArrayList();
        ids.addAll(BerryManager.berryNames.keySet());
        PokecubeCore.LOGGER.debug(BerryManager.berryNames);
        PokecubeCore.LOGGER.debug(BerryManager.berryNames.keySet());
        Collections.sort(ids);
        for (final Integer id : ids)
        {
            final int index = id;
            final ItemBerry berry = BerryManager.berryItems.get(index);
            PokecubeCore.LOGGER.debug("Registering berry_" + berry.type.name + " " + index + " " + id);
            berry.setRegistryName(PokecubeCore.MODID, "berry_" + berry.type.name);
            registry.register(berry);
        }
        BerryManager.registerTrees();
    }

    public static void makeBerryBlocks(final IForgeRegistry<Block> registry)
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
            Block block = new BerryCrop(AbstractBlock.Properties.of(Material.PLANT, berryFruits.get(name)).noCollission().randomTicks()
                    .strength(0.0F).noOcclusion().sound(SoundType.CROP), index);
            block.setRegistryName(PokecubeCore.MODID, "crop_" + name);
            BerryManager.berryCrops.put(index, block);
            registry.register(block);

            // Fruit
            block = new BerryFruit(AbstractBlock.Properties.of(Material.PLANT, berryCrops.get(name)).noCollission().randomTicks()
                    .strength(0.0F).noOcclusion().sound(SoundType.CROP), index);
            block.setRegistryName(PokecubeCore.MODID, "fruit_" + name);
            BerryManager.berryFruits.put(index, block);
            registry.register(block);
        }

        // Make the logs and planks.
        final List<String> names = Lists.newArrayList(ItemGenerator.berryWoods.keySet());
        Collections.sort(names);
        for (final String name : names)
        {
            final int index = ((ItemBerry) BerryManager.getBerryItem(name)).type.index;

            // Leaves
            Block block = new BerryLeaf(AbstractBlock.Properties.of(Material.LEAVES, berryLeaves.get(name)).strength(0.2F)
                    .randomTicks().noOcclusion().sound(SoundType.GRASS), index);
            block.setRegistryName(PokecubeCore.MODID, "leaves_" + name);
            ItemGenerator.leaves.put(name, block);
            registry.register(block);

            // Logs
            block = Blocks.log(berryWoods.get(name), berryWoods.get(name));
            block.setRegistryName(PokecubeCore.MODID, "log_" + name);
            ItemGenerator.logs.put(name, block);
            registry.register(block);

            // Woods
            block = Blocks.log(berryWoods.get(name), berryWoods.get(name));
            block.setRegistryName(PokecubeCore.MODID, name + "_wood");
            ItemGenerator.woods.put(name, block);
            registry.register(block);

            // Stripped Logs
            block = Blocks.log(berryWoods.get(name), berryWoods.get(name));
            block.setRegistryName(PokecubeCore.MODID, "stripped_" + name + "_log");
            ItemGenerator.stripped_logs.put(name, block);
            registry.register(block);

            // Stripped Woods
            block = Blocks.log(berryWoods.get(name), berryWoods.get(name));
            block.setRegistryName(PokecubeCore.MODID, "stripped_" + name + "_wood");
            ItemGenerator.stripped_woods.put(name, block);
            registry.register(block);

            // Planks
            block = new RotatedPillarBlock(AbstractBlock.Properties.of(Material.WOOD, berryWoods.get(name))
                    .strength(2.0F).sound(SoundType.WOOD));
            block.setRegistryName(PokecubeCore.MODID, "plank_" + name);
            ItemGenerator.planks.put(name, block);
            registry.register(block);

            // Stairs
            block = new GenericStairs(Blocks.OAK_PLANKS.defaultBlockState(), AbstractBlock.Properties.of(
            		Material.WOOD, berryWoods.get(name)).strength(2.0F).sound(SoundType.WOOD));
            block.setRegistryName(PokecubeCore.MODID, name + "_stairs");
            ItemGenerator.stairs.put(name, block);
            registry.register(block);

            // Slabs
            block = new SlabBlock(AbstractBlock.Properties.of(
            		Material.WOOD, berryWoods.get(name)).strength(2.0F).sound(SoundType.WOOD));
            block.setRegistryName(PokecubeCore.MODID, name + "_slab");
            ItemGenerator.slabs.put(name, block);
            registry.register(block);

            // Fences
            block = new FenceBlock(AbstractBlock.Properties.of(
            		Material.WOOD, berryWoods.get(name)).strength(2.0F).sound(SoundType.WOOD));
            block.setRegistryName(PokecubeCore.MODID, name + "_fence");
            ItemGenerator.fences.put(name, block);
            registry.register(block);

            // Fence Gates
            block = new FenceGateBlock(AbstractBlock.Properties.of(
            		Material.WOOD, berryWoods.get(name)).strength(2.0F).sound(SoundType.WOOD));
            block.setRegistryName(PokecubeCore.MODID, name + "_fence_gate");
            ItemGenerator.fence_gates.put(name, block);
            registry.register(block);

            // Pressure Plates
            block = new GenericPressurePlate(Sensitivity.EVERYTHING, AbstractBlock.Properties.of(
            		Material.WOOD, berryWoods.get(name)).strength(2.0F).sound(SoundType.WOOD));
            block.setRegistryName(PokecubeCore.MODID, name + "_pressure_plate");
            ItemGenerator.pressure_plates.put(name, block);
            registry.register(block);

            // Buttons
            block = new GenericWoodButton(AbstractBlock.Properties.of(
            		Material.WOOD, berryWoods.get(name)).strength(2.0F).sound(SoundType.WOOD));
            block.setRegistryName(PokecubeCore.MODID, name + "_button");
            ItemGenerator.buttons.put(name, block);
            registry.register(block);

            // Trapdoors
            block = new GenericTrapDoor(AbstractBlock.Properties.of(
            		Material.WOOD, berryWoods.get(name)).strength(2.0F).sound(SoundType.WOOD).noOcclusion());
            block.setRegistryName(PokecubeCore.MODID, name + "_trapdoor");
            ItemGenerator.trapdoors.put(name, block);
            registry.register(block);

            // Doors
            block = new GenericDoor(AbstractBlock.Properties.of(
            		Material.WOOD, berryWoods.get(name)).strength(2.0F).sound(SoundType.WOOD).noOcclusion());
            block.setRegistryName(PokecubeCore.MODID, name + "_door");
            ItemGenerator.doors.put(name, block);
            registry.register(block);
        }

        final List<String> leaves = Lists.newArrayList(ItemGenerator.onlyBerryLeaves.keySet());
        for (final String name : leaves)
        {
            final int index = ((ItemBerry) BerryManager.getBerryItem(name)).type.index;
            // Leaves
            final Block block = new BerryLeaf(AbstractBlock.Properties.of(Material.LEAVES, onlyBerryLeaves.get(name)).strength(0.2F)
                    .randomTicks().noOcclusion().sound(SoundType.GRASS), index);
            block.setRegistryName(PokecubeCore.MODID, "leaves_" + name);
            ItemGenerator.leaves.put(name, block);
            registry.register(block);
        }
    }

    public static void makeFossils(final IForgeRegistry<Item> registry)
    {
        final Item.Properties props = new Item.Properties().tab(PokecubeItems.POKECUBEITEMS);
        for (final String type : ItemGenerator.fossilVariants)
        {
            if (Database.getEntry(type) == null)
            {
                PokecubeCore.LOGGER.error("No Pokedex entry for {}", type);
                continue;
            }
            final ItemFossil item = new ItemFossil(props, Database.trim(type));
            registry.register(item);
            ItemGenerator.fossils.put(type, item);
        }
    }

    public static void makeHeldItems(final IForgeRegistry<Item> registry)
    {
        final Item.Properties props = new Item.Properties().tab(PokecubeItems.POKECUBEITEMS);
        for (final String type : ItemGenerator.variants)
        {
            final ItemTyped item = new ItemTyped(props, type);
            registry.register(item);
        }
    }

    public static void makeOtherItems(final IForgeRegistry<Item> registry)
    {
        final Item.Properties props = new Item.Properties().tab(PokecubeItems.POKECUBEITEMS);
        for (final String type : ItemGenerator.other)
        {
            final ItemTyped item = new ItemTyped(props, type);
            registry.register(item);
        }
    }

    public static void makeMegaWearables(final IForgeRegistry<Item> registry)
    {
        for (final String type : ItemMegawearable.getWearables())
        {
            final ItemMegawearable item = new ItemMegawearable(type, ItemMegawearable.getSlot(type));
            registry.register(item);
        }
    }

    public static void makeTMs(final IForgeRegistry<Item> registry)
    {
        for (final PokeType type : PokeType.values())
        {
            Item.Properties props = new Item.Properties();
            if (type == PokeType.unknown) props = props.tab(PokecubeItems.POKECUBEITEMS);
            final Item tm = new ItemTM(props, type);
            registry.register(tm);
        }
    }

    public static void makeWoodItems(final IForgeRegistry<Item> registry)
    {
        final List<String> names = Lists.newArrayList(ItemGenerator.berryWoods.keySet());
        final List<String> leaves = Lists.newArrayList(ItemGenerator.onlyBerryLeaves.keySet());
        Collections.sort(names);
        for (final String name : names)
        {
            registry.register(new BlockItem(ItemGenerator.leaves.get(name), new Item.Properties().tab(
                    PokecubeItems.POKECUBEBERRIES)).setRegistryName(ItemGenerator.leaves.get(name).getRegistryName()));
            registry.register(new BlockItem(ItemGenerator.logs.get(name), new Item.Properties().tab(
                    PokecubeItems.POKECUBEBERRIES)).setRegistryName(ItemGenerator.logs.get(name).getRegistryName()));
            registry.register(new BlockItem(ItemGenerator.woods.get(name), new Item.Properties().tab(
                    PokecubeItems.POKECUBEBERRIES)).setRegistryName(ItemGenerator.woods.get(name).getRegistryName()));
            registry.register(new BlockItem(ItemGenerator.stripped_logs.get(name), new Item.Properties().tab(
                    PokecubeItems.POKECUBEBERRIES)).setRegistryName(ItemGenerator.stripped_logs.get(name)
                            .getRegistryName()));
            registry.register(new BlockItem(ItemGenerator.stripped_woods.get(name), new Item.Properties().tab(
                    PokecubeItems.POKECUBEBERRIES)).setRegistryName(ItemGenerator.stripped_woods.get(name)
                            .getRegistryName()));
            registry.register(new BlockItem(ItemGenerator.planks.get(name), new Item.Properties().tab(
                    PokecubeItems.POKECUBEBERRIES)).setRegistryName(ItemGenerator.planks.get(name).getRegistryName()));
            registry.register(new BlockItem(ItemGenerator.stairs.get(name), new Item.Properties().tab(
                    PokecubeItems.POKECUBEBERRIES)).setRegistryName(ItemGenerator.stairs.get(name).getRegistryName()));
            registry.register(new BlockItem(ItemGenerator.slabs.get(name), new Item.Properties().tab(
                    PokecubeItems.POKECUBEBERRIES)).setRegistryName(ItemGenerator.slabs.get(name).getRegistryName()));
            registry.register(new BlockItem(ItemGenerator.fences.get(name), new Item.Properties().tab(
                    PokecubeItems.POKECUBEBERRIES)).setRegistryName(ItemGenerator.fences.get(name).getRegistryName()));
            registry.register(new BlockItem(ItemGenerator.fence_gates.get(name), new Item.Properties().tab(
                    PokecubeItems.POKECUBEBERRIES)).setRegistryName(ItemGenerator.fence_gates.get(name)
                            .getRegistryName()));
            registry.register(new BlockItem(ItemGenerator.pressure_plates.get(name), new Item.Properties().tab(
                    PokecubeItems.POKECUBEBERRIES)).setRegistryName(ItemGenerator.pressure_plates.get(name)
                            .getRegistryName()));
            registry.register(new BlockItem(ItemGenerator.buttons.get(name), new Item.Properties().tab(
                    PokecubeItems.POKECUBEBERRIES)).setRegistryName(ItemGenerator.buttons.get(name).getRegistryName()));
            registry.register(new BlockItem(ItemGenerator.trapdoors.get(name), new Item.Properties().tab(
                    PokecubeItems.POKECUBEBERRIES)).setRegistryName(ItemGenerator.trapdoors.get(name)
                            .getRegistryName()));
            registry.register(new BlockItem(ItemGenerator.doors.get(name), new Item.Properties().tab(
                    PokecubeItems.POKECUBEBERRIES)).setRegistryName(ItemGenerator.doors.get(name).getRegistryName()));
        }
        for (final String name : leaves)
            registry.register(new BlockItem(ItemGenerator.leaves.get(name), new Item.Properties().tab(
                    PokecubeItems.POKECUBEBERRIES)).setRegistryName(ItemGenerator.leaves.get(name).getRegistryName()));
    }

    public static class GenericStairs extends StairsBlock
    {
        @SuppressWarnings("deprecation")
        public GenericStairs(final BlockState state, final Properties properties)
        {
            super(state, properties);
        }
    }

    public static RotatedPillarBlock stoneLog(MaterialColor color1, MaterialColor color2) {
        return new RotatedPillarBlock(AbstractBlock.Properties.of(Material.STONE, (state) -> {
            return state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? color1 : color2;
        }).strength(2.4F).sound(SoundType.STONE));
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

    public static void addStrippable(final Block logs, final Block strippedLogs)
    {
        STRIPABLES = Maps.newHashMap(STRIPABLES);
        STRIPABLES.put(logs, strippedLogs);
    }

    public static void strippableBlocks(final FMLLoadCompleteEvent event)
    {
        final List<String> names = Lists.newArrayList(ItemGenerator.berryWoods.keySet());
        Collections.sort(names);
        // Enqueue this so that it runs on main thread, to prevent concurrency
        // issues.
        event.enqueueWork(() ->
        {
            for (final String name : names)
            {
                ItemGenerator.addStrippable(ItemGenerator.logs.get(name), ItemGenerator.stripped_logs.get(name));
                ItemGenerator.addStrippable(ItemGenerator.woods.get(name), ItemGenerator.stripped_woods.get(name));
            }
        });
    }
    
    public static void compostableBlocks(float chance, IItemProvider item) 
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
        event.enqueueWork(() ->
        {
        	for (final String name : leaves)
        	{
                compostableBlocks(0.3f, ItemGenerator.leaves.get(name).asItem());
        	}
        	for (final String name : onlyLeaves)
        	{
                compostableBlocks(0.3f, ItemGenerator.leaves.get(name).asItem());
        	}
            for (final Integer id : ids)
            {
                final int index = id;
                compostableBlocks(0.65f, BerryManager.berryItems.get(index));
            }
        });
    }

    public static void flammableBlocks(Block block, int speed, int flammability) {
        FireBlock fire = (FireBlock) Blocks.FIRE;
        fire.setFlammable(block, speed, flammability);
    }

    public static void flammables(final FMLLoadCompleteEvent event) {
        final List<String> names = Lists.newArrayList(ItemGenerator.berryWoods.keySet());
        Collections.sort(names);
        event.enqueueWork(() ->
        {
            for (final String name : names) {
                //Logs
                flammableBlocks(ItemGenerator.logs.get(name), 5, 5);
                flammableBlocks(ItemGenerator.woods.get(name), 5, 5);
                flammableBlocks(ItemGenerator.stripped_logs.get(name), 5, 5);
                flammableBlocks(ItemGenerator.stripped_woods.get(name), 5, 5);

                //Leaves
                flammableBlocks(ItemGenerator.leaves.get(name), 30, 60);

                //Woods
                flammableBlocks(ItemGenerator.planks.get(name), 5, 20);
                flammableBlocks(ItemGenerator.slabs.get(name), 5, 20);
                flammableBlocks(ItemGenerator.stairs.get(name), 5, 20);
                flammableBlocks(ItemGenerator.fences.get(name), 5, 20);
                flammableBlocks(ItemGenerator.fence_gates.get(name), 5, 20);
            }
        });
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

    public static void registerBlocks(final IForgeRegistry<Block> registry)
    {

        // Initialize the nullberry
        new BerryType("null", null, 0, 0, 0, 0, 0, 0);
        // Fire event so that others can initialize their berries.
        PokecubeCore.POKEMOB_BUS.post(new RegisterMiscItems());

        ItemGenerator.makeBerryBlocks(registry);
    }

    public static void registerItems(final IForgeRegistry<Item> registry)
    {
        ItemGenerator.makeBerries(registry);
        ItemGenerator.makeFossils(registry);
        ItemGenerator.makeHeldItems(registry);
        ItemGenerator.makeOtherItems(registry);
        ItemGenerator.makeMegaWearables(registry);
        ItemGenerator.makeWoodItems(registry);
        ItemGenerator.makeTMs(registry);
    }
}
