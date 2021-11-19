package pokecube.legends.init;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fmllegacy.RegistryObject;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.tileentity.GenericBarrelTile;
import pokecube.legends.tileentity.GenericBookshelfEmptyTile;
import pokecube.legends.tileentity.InfectedCampfireBlockEntity;

public class TileEntityInit
{
    // Tile
    public static final RegistryObject<BlockEntityType<InfectedCampfireBlockEntity>> CAMPFIRE_ENTITY;
    public static final RegistryObject<BlockEntityType<GenericBarrelTile>> BARREL_ENTITY;
    public static final RegistryObject<BlockEntityType<GenericBookshelfEmptyTile>> BOOKSHELF_EMPTY_ENTITY;

    static
    {
        CAMPFIRE_ENTITY = PokecubeLegends.TILES.register("campfire", () -> BlockEntityType.Builder.of(
            InfectedCampfireBlockEntity::new, BlockInit.INFECTED_CAMPFIRE.get()).build(null));

        BARREL_ENTITY = PokecubeLegends.TILES.register("generic_barrel", () -> BlockEntityType.Builder.of(
            GenericBarrelTile::new, BlockInit.AGED_BARREL.get(), BlockInit.CONCRETE_BARREL.get(),
            BlockInit.CONCRETE_DENSE_BARREL.get(), BlockInit.CORRUPTED_BARREL.get(), BlockInit.DISTORTIC_BARREL.get(),
            BlockInit.DISTORTIC_STONE_BARREL.get(), BlockInit.INVERTED_BARREL.get(), BlockInit.MIRAGE_BARREL.get(),
            BlockInit.TEMPORAL_BARREL.get()).build(null));

        BOOKSHELF_EMPTY_ENTITY = PokecubeLegends.TILES.register("generic_bookshelf_empty", () -> BlockEntityType.Builder.of(
            GenericBookshelfEmptyTile::new, BlockInit.AGED_BOOKSHELF_EMPTY.get(), BlockInit.CONCRETE_BOOKSHELF_EMPTY.get(),
            BlockInit.CONCRETE_DENSE_BOOKSHELF_EMPTY.get(), BlockInit.CORRUPTED_BOOKSHELF_EMPTY.get(),
            BlockInit.DISTORTIC_BOOKSHELF_EMPTY.get(), BlockInit.INVERTED_BOOKSHELF_EMPTY.get(), BlockInit.MIRAGE_BOOKSHELF_EMPTY.get(),
            BlockInit.TEMPORAL_BOOKSHELF_EMPTY.get()).build(null));
    }

    public static void init() {}
}
