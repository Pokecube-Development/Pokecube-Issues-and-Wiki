package pokecube.core.handlers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.Tags;
import pokecube.core.PokecubeCore;

public class ModTags {

    public static final Tags.IOptionalNamedTag<Item> BOOKS = itemTags("forge", "books");
    public static final Tags.IOptionalNamedTag<Item> BOOKSHELF_ITEMS = itemTags(PokecubeCore.MODID, "bookshelf_items");

    public static Tags.IOptionalNamedTag<Item> itemTags(String id, String name) {
        return ItemTags.createOptional(new ResourceLocation(id, name));
    }
}
