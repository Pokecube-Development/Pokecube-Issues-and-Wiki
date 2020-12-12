package pokecube.core.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.LogBlock;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.TrapDoorBlock;
import net.minecraft.block.WoodButtonBlock;
import net.minecraft.block.Block.Properties;
import net.minecraft.block.PressurePlateBlock.Sensitivity;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
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

    public static final Map<String, MaterialColor> berryWoods = Maps.newHashMap();

    public static final List<String> onlyBerryLeaves = Lists.newArrayList();

    public static Map<String, Block> leaves = Maps.newHashMap();
    public static Map<String, Block> logs   = Maps.newHashMap();
    public static Map<String, Block> woods  = Maps.newHashMap();
    public static Map<String, Block> stripped_logs   = Maps.newHashMap();
    public static Map<String, Block> stripped_woods  = Maps.newHashMap();
    public static Map<String, Block> stairs  = Maps.newHashMap();
    public static Map<String, Block> slabs  = Maps.newHashMap();
    public static Map<String, Block> fences  = Maps.newHashMap();
    public static Map<String, Block> planks = Maps.newHashMap();

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
            Block block = new BerryCrop(Block.Properties.create(Material.PLANTS).doesNotBlockMovement().tickRandomly()
                    .hardnessAndResistance(0.0F).notSolid().sound(SoundType.CROP), index);
            block.setRegistryName(PokecubeCore.MODID, "crop_" + name);
            BerryManager.berryCrops.put(index, block);
            registry.register(block);

            // Fruit
            block = new BerryFruit(Block.Properties.create(Material.PLANTS).doesNotBlockMovement().tickRandomly()
                    .hardnessAndResistance(0.0F).notSolid().sound(SoundType.CROP), index);
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
            Block block = new BerryLeaf(Block.Properties.create(Material.LEAVES).hardnessAndResistance(0.2F).tickRandomly()
                    .notSolid().sound(SoundType.PLANT), index);
            block.setRegistryName(PokecubeCore.MODID, "leaves_" + name);
            ItemGenerator.leaves.put(name, block);
            registry.register(block);

            // Logs
            block = new LogBlock(ItemGenerator.berryWoods.get(name), Block.Properties.create(Material.WOOD,
                    MaterialColor.WOOD).hardnessAndResistance(2.0F).sound(SoundType.WOOD));
            block.setRegistryName(PokecubeCore.MODID, "log_" + name);
            ItemGenerator.logs.put(name, block);
            registry.register(block);

            // Woods
            block = new LogBlock(ItemGenerator.berryWoods.get(name), Block.Properties.create(Material.WOOD,
                    MaterialColor.WOOD).hardnessAndResistance(2.0F).sound(SoundType.WOOD));
            block.setRegistryName(PokecubeCore.MODID, name + "_wood");
            ItemGenerator.woods.put(name, block);
            registry.register(block);

            // Stripped Logs
            block = new LogBlock(ItemGenerator.berryWoods.get(name), Block.Properties.create(Material.WOOD,
                    MaterialColor.WOOD).hardnessAndResistance(2.0F).sound(SoundType.WOOD));
            block.setRegistryName(PokecubeCore.MODID, "stripped_" + name + "_log");
            ItemGenerator.stripped_logs.put(name, block);
            registry.register(block);

            // Stripped Woods
            block = new LogBlock(ItemGenerator.berryWoods.get(name), Block.Properties.create(Material.WOOD,
                    MaterialColor.WOOD).hardnessAndResistance(2.0F).sound(SoundType.WOOD));
            block.setRegistryName(PokecubeCore.MODID, "stripped_" + name + "_wood");
            ItemGenerator.stripped_woods.put(name, block);
            registry.register(block);

            // Stairs
            block = new GenericWoodStairs(Blocks.OAK_PLANKS.getDefaultState(), Block.Properties.from(Blocks.OAK_PLANKS));
            block.setRegistryName(PokecubeCore.MODID, name + "_stairs");
            ItemGenerator.stairs.put(name, block);
            registry.register(block);

            // Slabs
            block = new SlabBlock(Block.Properties.from(Blocks.OAK_PLANKS));
            block.setRegistryName(PokecubeCore.MODID, name + "_slab");
            ItemGenerator.slabs.put(name, block);
            registry.register(block);

            // Fences
            block = new FenceBlock(Block.Properties.from(Blocks.OAK_PLANKS));
            block.setRegistryName(PokecubeCore.MODID, name + "_fence");
            ItemGenerator.fences.put(name, block);
            registry.register(block);

            // Planks
            block = new RotatedPillarBlock(Block.Properties.create(Material.WOOD, MaterialColor.WOOD)
                    .hardnessAndResistance(2.0F).sound(SoundType.WOOD));
            block.setRegistryName(PokecubeCore.MODID, "plank_" + name);
            ItemGenerator.planks.put(name, block);
            registry.register(block);
        }

        for (final String name : ItemGenerator.onlyBerryLeaves)
        {
            final int index = ((ItemBerry) BerryManager.getBerryItem(name)).type.index;
            // Leaves
            final Block block = new BerryLeaf(Block.Properties.create(Material.LEAVES).hardnessAndResistance(0.2F)
                    .tickRandomly().notSolid().sound(SoundType.PLANT), index);
            block.setRegistryName(PokecubeCore.MODID, "leaves_" + name);
            ItemGenerator.leaves.put(name, block);
            registry.register(block);
        }
    }

    public static void makeFossils(final IForgeRegistry<Item> registry)
    {
        final Item.Properties props = new Item.Properties().group(PokecubeItems.POKECUBEITEMS);
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
        final Item.Properties props = new Item.Properties().group(PokecubeItems.POKECUBEITEMS);
        for (final String type : ItemGenerator.variants)
        {
            final ItemTyped item = new ItemTyped(props, type);
            registry.register(item);
        }
    }

    public static void makeOtherItems(final IForgeRegistry<Item> registry)
    {
        final Item.Properties props = new Item.Properties().group(PokecubeItems.POKECUBEITEMS);
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
            if (type == PokeType.unknown) props = props.group(PokecubeItems.POKECUBEITEMS);
            final Item tm = new ItemTM(props, type);
            registry.register(tm);
        }
    }

    public static void makeWoodItems(final IForgeRegistry<Item> registry)
    {
        final List<String> names = Lists.newArrayList(ItemGenerator.berryWoods.keySet());
        Collections.sort(names);
        for (final String name : names)
        {
            registry.register(new BlockItem(ItemGenerator.leaves.get(name), new Item.Properties().group(
                    PokecubeItems.POKECUBEBERRIES)).setRegistryName(ItemGenerator.leaves.get(name).getRegistryName()));
            registry.register(new BlockItem(ItemGenerator.logs.get(name), new Item.Properties().group(
                    PokecubeItems.POKECUBEBERRIES)).setRegistryName(ItemGenerator.logs.get(name).getRegistryName()));
            registry.register(new BlockItem(ItemGenerator.woods.get(name), new Item.Properties().group(
                    PokecubeItems.POKECUBEBERRIES)).setRegistryName(ItemGenerator.woods.get(name).getRegistryName()));
            registry.register(new BlockItem(ItemGenerator.stripped_logs.get(name), new Item.Properties().group(
                    PokecubeItems.POKECUBEBERRIES)).setRegistryName(ItemGenerator.stripped_logs.get(name).getRegistryName()));
            registry.register(new BlockItem(ItemGenerator.stripped_woods.get(name), new Item.Properties().group(
                    PokecubeItems.POKECUBEBERRIES)).setRegistryName(ItemGenerator.stripped_woods.get(name).getRegistryName()));
            registry.register(new BlockItem(ItemGenerator.stairs.get(name), new Item.Properties().group(
                    PokecubeItems.POKECUBEBERRIES)).setRegistryName(ItemGenerator.stairs.get(name).getRegistryName()));
            registry.register(new BlockItem(ItemGenerator.slabs.get(name), new Item.Properties().group(
                    PokecubeItems.POKECUBEBERRIES)).setRegistryName(ItemGenerator.slabs.get(name).getRegistryName()));
            registry.register(new BlockItem(ItemGenerator.fences.get(name), new Item.Properties().group(
                    PokecubeItems.POKECUBEBERRIES)).setRegistryName(ItemGenerator.fences.get(name).getRegistryName()));
            registry.register(new BlockItem(ItemGenerator.planks.get(name), new Item.Properties().group(
                    PokecubeItems.POKECUBEBERRIES)).setRegistryName(ItemGenerator.planks.get(name).getRegistryName()));
        }
        for (final String name : ItemGenerator.onlyBerryLeaves)
            registry.register(new BlockItem(ItemGenerator.leaves.get(name), new Item.Properties().group(
                    PokecubeItems.POKECUBEBERRIES)).setRegistryName(ItemGenerator.leaves.get(name).getRegistryName()));
    }

    public static class GenericWoodStairs extends StairsBlock 
    {
        @SuppressWarnings("deprecation")
		public GenericWoodStairs(BlockState state, Properties properties) {
            super(state, properties);
        }
    }

    public static class GenericTrapDoorBlock extends TrapDoorBlock {
        public GenericTrapDoorBlock(Properties properties) {
            super(properties);
        }
    }

    public static class GenericDoorBlock extends DoorBlock {
        public GenericDoorBlock(Properties properties) {
            super(properties);
        }
    }

    public static class GenericButtonBlock extends WoodButtonBlock {
        public GenericButtonBlock(Properties properties) {
            super(properties);
        }
    }

    public static class GenericPressurePlate extends PressurePlateBlock {
        public GenericPressurePlate(Sensitivity sesitivity, Properties properties) {
            super(sesitivity, properties);
        }
    }
    
	public static void addStrippable(Block logs, Block strippedLogs) 
	{
		AxeItem.BLOCK_STRIPPING_MAP = Maps.newHashMap(AxeItem.BLOCK_STRIPPING_MAP);
		AxeItem.BLOCK_STRIPPING_MAP.put(logs, strippedLogs);
	}
	
	public static void strippableBlocks()
	{
        final List<String> names = Lists.newArrayList(ItemGenerator.berryWoods.keySet());
        Collections.sort(names);
        for (final String name : names)
        {
        	addStrippable(ItemGenerator.logs.get(name),  ItemGenerator.stripped_logs.get(name));
        	addStrippable(ItemGenerator.woods.get(name), ItemGenerator.stripped_woods.get(name));
        }
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
