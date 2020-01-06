package pokecube.core.handlers;

import java.util.Locale;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.healer.HealerTile;
import pokecube.core.blocks.nests.NestTile;
import pokecube.core.blocks.pc.PCTile;
import pokecube.core.blocks.repel.RepelTile;
import pokecube.core.blocks.tms.TMTile;
import pokecube.core.blocks.trade.TraderTile;
import pokecube.core.database.Database;
import pokecube.core.events.onload.RegisterPokecubes;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.Pokecube;
import pokecube.core.items.vitamins.ItemVitamin;
import thut.api.OwnableCaps;

public class ItemHandler
{
    private static void addFossilBlocks(final IForgeRegistry<Block> registry)
    {
        // PokecubeItems.register(PokecubeItems.fossilStone, registry);
        // PokecubeItems.fossilStone.setCreativeTab(PokecubeMod.creativeTabPokecubeBlocks);
    }

    private static void addMiscBlocks(final IForgeRegistry<Block> registry)
    {
        registry.register(PokecubeItems.HEALER);
        registry.register(PokecubeItems.NESTBLOCK);
        registry.register(PokecubeItems.REPELBLOCK);
        registry.register(PokecubeItems.PCTOP);
        registry.register(PokecubeItems.PCBASE);
        registry.register(PokecubeItems.TMMACHINE);
        registry.register(PokecubeItems.TRADER);

        // tableBlock.setUnlocalizedName("pokecube_table").setRegistryName(PokecubeMod.ID,
        // "pokecube_table");
        // tableBlock.setCreativeTab(creativeTabPokecubeBlocks);
        // register(tableBlock, registry);
    }

    private static void addMiscItems(final IForgeRegistry<Item> registry)
    {
        registry.register(PokecubeItems.POKEDEX.setRegistryName(PokecubeCore.MODID, "pokedex"));
        registry.register(PokecubeItems.POKEWATCH.setRegistryName(PokecubeCore.MODID, "pokewatch"));
        registry.register(PokecubeItems.BERRYJUICE.setRegistryName(PokecubeCore.MODID, "berryjuice"));
        registry.register(PokecubeItems.EGG.setRegistryName(PokecubeCore.MODID, "pokemobegg"));
        registry.register(PokecubeItems.CANDY.setRegistryName(PokecubeMod.ID, "candy"));
        Item item = new Item(new Item.Properties().rarity(Rarity.RARE).group(PokecubeItems.POKECUBEITEMS))
                .setRegistryName(PokecubeMod.ID, "luckyegg");
        PokecubeItems.setAs(PokecubeItems.HELDKEY, item);
        registry.register(item);
        item = new Item(new Item.Properties().group(PokecubeItems.POKECUBEITEMS)).setRegistryName(PokecubeMod.ID,
                "revive");
        registry.register(item);
        item = new Item(new Item.Properties().group(PokecubeItems.POKECUBEITEMS)).setRegistryName(PokecubeMod.ID,
                "emerald_shard");
        registry.register(item);

        ItemGenerator.registerItems(registry);
        for (final String s : PokecubeCore.getConfig().customHeldItems)
            if (!ItemGenerator.variants.contains(s.toLowerCase(Locale.ENGLISH))) ItemGenerator.variants.add(s
                    .toLowerCase(Locale.ENGLISH));
    }

    private static void addMiscTiles(final IForgeRegistry<TileEntityType<?>> registry)
    {
        // Register the tiles
        registry.register(NestTile.TYPE.setRegistryName(PokecubeCore.MODID, "nest"));
        registry.register(RepelTile.TYPE.setRegistryName(PokecubeCore.MODID, "repel"));

        registry.register(TraderTile.TYPE.setRegistryName(PokecubeCore.MODID, "trade_machine"));
        registry.register(TMTile.TYPE.setRegistryName(PokecubeCore.MODID, "tm_machine"));
        registry.register(HealerTile.TYPE.setRegistryName(PokecubeCore.MODID, "pokecenter"));
        registry.register(PCTile.TYPE.setRegistryName(PokecubeCore.MODID, "pc"));

        // Register classes for ownable caps
        OwnableCaps.TILES.add(TraderTile.class);
        OwnableCaps.TILES.add(TMTile.class);
        OwnableCaps.TILES.add(PCTile.class);
        OwnableCaps.TILES.add(HealerTile.class);

        // GameRegistry.registerTileEntity(TileEntityPokecubeTable.class, new
        // ResourceLocation("pokecube:pokecube_table"));
        // GameRegistry.registerTileEntity(TileEntityBasePortal.class,
        // new ResourceLocation("pokecube:pokecubebaseportal"));
    }

    private static void addPokecubes(final IForgeRegistry<Item> registry)
    {
        final RegisterPokecubes event = new RegisterPokecubes();
        PokecubeCore.POKEMOB_BUS.post(event);

        // Register any cube behaviours and cubes from event.
        for (final PokecubeBehavior i : event.behaviors)
        {
            PokecubeBehavior.addCubeBehavior(i);
            final String name = i.getRegistryName().getPath();
            final Item.Properties props = new Item.Properties();
            props.group(PokecubeItems.POKECUBECUBES);
            props.setNoRepair();
            final Pokecube cube = new Pokecube(props);
            if (PokecubeItems.POKECUBE_CUBES.isEmpty()) PokecubeItems.POKECUBE_CUBES = new ItemStack(cube);
            registry.register(cube.setRegistryName(PokecubeMod.ID, name + "cube"));

            PokecubeItems.addCube(i.getRegistryName(), new Item[] { cube });
        }

        final Item.Properties props = new Item.Properties();
        props.group(PokecubeItems.POKECUBECUBES);
        final Pokecube pokeseal = new Pokecube(props);
        PokecubeBehavior.POKESEAL = new ResourceLocation("pokecube:seal");
        registry.register(pokeseal.setRegistryName(PokecubeMod.ID, "pokeseal"));

        PokecubeItems.addCube(PokecubeBehavior.POKESEAL, new Item[] { pokeseal });

    }

    private static void addVitamins(final IForgeRegistry<Item> registry)
    {
        final Item.Properties props = new Item.Properties().group(PokecubeItems.POKECUBEITEMS);
        for (final String type : ItemVitamin.vitamins)
        {
            final ItemVitamin item = new ItemVitamin(props, Database.trim(type));
            registry.register(item);
        }
    }

    public static void registerBlocks(final IForgeRegistry<Block> iForgeRegistry)
    {
        ItemHandler.addMiscBlocks(iForgeRegistry);
        ItemHandler.addFossilBlocks(iForgeRegistry);
        ItemGenerator.registerBlocks(iForgeRegistry);
    }

    private static void registerItemBlocks(final IForgeRegistry<Item> registry)
    {
        registry.register(new BlockItem(PokecubeItems.HEALER, new Item.Properties().group(PokecubeItems.POKECUBEBLOCKS))
                .setRegistryName(PokecubeItems.HEALER.getRegistryName()));
        registry.register(new BlockItem(PokecubeItems.NESTBLOCK, new Item.Properties().group(
                PokecubeItems.POKECUBEBLOCKS)).setRegistryName(PokecubeItems.NESTBLOCK.getRegistryName()));
        registry.register(new BlockItem(PokecubeItems.REPELBLOCK, new Item.Properties().group(
                PokecubeItems.POKECUBEBLOCKS)).setRegistryName(PokecubeItems.REPELBLOCK.getRegistryName()));
        registry.register(new BlockItem(PokecubeItems.PCTOP, new Item.Properties().group(PokecubeItems.POKECUBEBLOCKS))
                .setRegistryName(PokecubeItems.PCTOP.getRegistryName()));
        registry.register(new BlockItem(PokecubeItems.PCBASE, new Item.Properties().group(PokecubeItems.POKECUBEBLOCKS))
                .setRegistryName(PokecubeItems.PCBASE.getRegistryName()));
        registry.register(new BlockItem(PokecubeItems.TMMACHINE, new Item.Properties().group(
                PokecubeItems.POKECUBEBLOCKS)).setRegistryName(PokecubeItems.TMMACHINE.getRegistryName()));
        registry.register(new BlockItem(PokecubeItems.TRADER, new Item.Properties().group(PokecubeItems.POKECUBEBLOCKS))
                .setRegistryName(PokecubeItems.TRADER.getRegistryName()));

        PokecubeItems.POKECUBE_BLOCKS = new ItemStack(PokecubeItems.HEALER);
        // registerItemBlock(fossilStone, registry);
        //
        // item = new ItemBlock(tableBlock);
        // item.setRegistryName(PokecubeMod.ID, "pokecube_table");
        // register(item, registry);
    }

    public static void registerItems(final IForgeRegistry<Item> iForgeRegistry)
    {
        ItemHandler.addPokecubes(iForgeRegistry);
        ItemHandler.addVitamins(iForgeRegistry);
        ItemHandler.addMiscItems(iForgeRegistry);
        ItemHandler.registerItemBlocks(iForgeRegistry);
    }

    public static void registerTiles(final IForgeRegistry<TileEntityType<?>> iForgeRegistry)
    {
        ItemHandler.addMiscTiles(iForgeRegistry);
    }
}
