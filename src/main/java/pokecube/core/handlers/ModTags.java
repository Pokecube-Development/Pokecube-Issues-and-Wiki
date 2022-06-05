package pokecube.core.handlers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import pokecube.core.PokecubeCore;

public class ModTags {

    public static final TagKey<Item> BOOKS = itemTags("forge", "books");
    public static final TagKey<Item> BOOKSHELF_ITEMS = itemTags(PokecubeCore.MODID, "bookshelf_items");

    public static TagKey<Item> itemTags(String id, String name) {
        return ItemTags.create(new ResourceLocation(id, name));
    }
}
