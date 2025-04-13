package pokecube.legends.init;

import net.minecraft.world.level.block.entity.BlockEntityType;
import pokecube.core.init.ItemGenerator;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.tileentity.InfectedCampfireBlockEntity;

import java.util.List;
import java.util.function.Supplier;

public class TileEntityInit
{
    // Tile
    public static final Supplier<BlockEntityType<InfectedCampfireBlockEntity>> CAMPFIRE_ENTITY;

    static
    {
        CAMPFIRE_ENTITY = PokecubeLegends.TILES.register("campfire",
                () -> BlockEntityType.Builder.of(InfectedCampfireBlockEntity::new, BlockInit.INFECTED_CAMPFIRE.get())
                        .build(null));
    }

    public static void init()
    {
        ItemGenerator.BARRELS.addAll(
                List.of(BlockInit.AGED_BARREL, BlockInit.CONCRETE_BARREL, BlockInit.CONCRETE_DENSE_BARREL,
                        BlockInit.CORRUPTED_BARREL, BlockInit.DISTORTIC_BARREL, BlockInit.DISTORTIC_STONE_BARREL,
                        BlockInit.INVERTED_BARREL, BlockInit.MIRAGE_BARREL, BlockInit.TEMPORAL_BARREL));

        ItemGenerator.FILLABLE_SHELVES.addAll(
                List.of(BlockInit.AGED_BOOKSHELF_EMPTY, BlockInit.CONCRETE_BOOKSHELF_EMPTY,
                        BlockInit.CONCRETE_DENSE_BOOKSHELF_EMPTY, BlockInit.CORRUPTED_BOOKSHELF_EMPTY,
                        BlockInit.DISTORTIC_BOOKSHELF_EMPTY, BlockInit.INVERTED_BOOKSHELF_EMPTY,
                        BlockInit.MIRAGE_BOOKSHELF_EMPTY, BlockInit.TEMPORAL_BOOKSHELF_EMPTY));
    }
}
