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
import thut.lib.RegHelper;

public class ItemList extends Items
{
    public static Map<ResourceLocation, Set<Item>> pendingTags = Maps.newHashMap();

    // Cache maps so we don't keep creating new tag keys everytime TagKey.create
    // is called, and clogging up GC.
    private static Map<ResourceLocation, TagKey<EntityType<?>>> E_TAGS = Maps.newConcurrentMap();
    private static Map<ResourceLocation, TagKey<Item>> I_TAGS = Maps.newConcurrentMap();
    private static Map<ResourceLocation, TagKey<Block>> B_TAGS = Maps.newConcurrentMap();

    public static boolean is(final ResourceLocation tag, final EntityType<?> type)
    {
        var tagkey = E_TAGS.computeIfAbsent(tag, l -> TagKey.create(Registry.ENTITY_TYPE_REGISTRY, l));
        final boolean tagged = type.is(tagkey);
        if (!tagged) return RegHelper.getKey(type).equals(tag);
        return tagged;
    }

    public static boolean is(final ResourceLocation tag, final Entity toCheck)
    {
        return is(tag, toCheck.getType());
    }

    public static boolean is(final ResourceLocation tag, final BlockState toCheck)
    {
        var tagkey = B_TAGS.computeIfAbsent(tag, l -> TagKey.create(Registry.BLOCK_REGISTRY, l));
        final boolean tagged = toCheck.is(tagkey);
        if (!tagged) return RegHelper.getKey(toCheck.getBlock()).equals(tag);
        return tagged;
    }

    public static boolean is(final ResourceLocation tag, final Block toCheck)
    {
        return is(tag, toCheck.defaultBlockState());
    }

    public static boolean is(final ResourceLocation tag, final ItemStack stack)
    {
        var tagkey = I_TAGS.computeIfAbsent(tag, l -> TagKey.create(Registry.ITEM_REGISTRY, l));
        boolean tagged = stack.is(tagkey);
        tagged = tagged || ItemList.pendingTags.getOrDefault(tag, Collections.emptySet()).contains(stack.getItem());
        if (!tagged) return RegHelper.getKey(stack).equals(tag);
        return tagged;
    }

    public static boolean is(final ResourceLocation tag, final Item toCheck)
    {
        return is(tag, new ItemStack(toCheck));
    }
}
