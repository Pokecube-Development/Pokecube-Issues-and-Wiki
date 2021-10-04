package thut.api.item;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ItemList extends Items
{
    public static Map<ResourceLocation, Set<Item>> pendingTags = Maps.newHashMap();

    public static boolean is(final ResourceLocation tag, final Object toCheck)
    {
        if (toCheck instanceof Entity) return ItemList.is(tag, ((Entity) toCheck).getType());
        if (toCheck instanceof EntityType)
        {
            final EntityType<?> type = (EntityType<?>) toCheck;
            final boolean tagged = EntityTypeTags.getAllTags().getTagOrEmpty(tag).contains(type);
            if (!tagged && type.getRegistryName() != null) return type.getRegistryName().equals(tag);
            return tagged;
        }
        if (toCheck instanceof Item)
        {
            final Item item = (Item) toCheck;
            boolean tagged = ItemTags.getAllTags().getTagOrEmpty(tag).contains(item);
            tagged = tagged || ItemList.pendingTags.getOrDefault(tag, Collections.emptySet()).contains(item);
            if (!tagged && item.getRegistryName() != null) return item.getRegistryName().equals(tag);
            return tagged;
        }
        else if (toCheck instanceof ItemStack) return ItemList.is(tag, ((ItemStack) toCheck).getItem());
        else if (toCheck instanceof Block)
        {

            final Block block = (Block) toCheck;
            final boolean tagged = BlockTags.getAllTags().getTagOrEmpty(tag).contains(block);
            if (!tagged && block.getRegistryName() != null) return block.getRegistryName().equals(tag);
            return tagged;
        }
        else if (toCheck instanceof BlockState) return ItemList.is(tag, ((BlockState) toCheck).getBlock());
        return false;
    }
}
