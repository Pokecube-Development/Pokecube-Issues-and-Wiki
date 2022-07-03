package thut.api.item;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
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

    public static boolean is(final ResourceLocation tag, final EntityType<?> toCheck)
    {
        final EntityType<?> type = (EntityType<?>) toCheck;
        TagKey<EntityType<?>> tagkey = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, tag);
        final boolean tagged = type.is(tagkey);
        if (!tagged && type.getRegistryName() != null) return type.getRegistryName().equals(tag);
        return tagged;
    }

    public static boolean is(final ResourceLocation tag, final Entity toCheck)
    {
        return is(tag, toCheck.getType());
    }

    public static boolean is(final ResourceLocation tag, final BlockState toCheck)
    {
        final Block block = toCheck.getBlock();
        TagKey<Block> tagkey = TagKey.create(Registry.BLOCK_REGISTRY, tag);
        final boolean tagged = toCheck.is(tagkey);
        if (!tagged && block.getRegistryName() != null) return block.getRegistryName().equals(tag);
        return tagged;
    }

    public static boolean is(final ResourceLocation tag, final Block toCheck)
    {
        return is(tag, toCheck.defaultBlockState());
    }

    public static boolean is(final ResourceLocation tag, final ItemStack toCheck)
    {
        ItemStack stack = (ItemStack) toCheck;
        TagKey<Item> tagkey = TagKey.create(Registry.ITEM_REGISTRY, tag);
        boolean tagged = stack.is(tagkey);
        tagged = tagged || ItemList.pendingTags.getOrDefault(tag, Collections.emptySet()).contains(stack.getItem());
        if (!tagged && stack.getItem().getRegistryName() != null) return stack.getItem().getRegistryName().equals(tag);
        return tagged;
    }

    public static boolean is(final ResourceLocation tag, final Item toCheck)
    {
        return is(tag, new ItemStack(toCheck));
    }
}
