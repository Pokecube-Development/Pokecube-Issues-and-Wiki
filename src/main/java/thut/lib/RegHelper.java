package thut.lib;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class RegHelper
{
    public static ResourceLocation getKey(EntityType<?> type)
    {
        return type.getRegistryName();
        // 1.19 will require the below option instead.
        // return Registry.ENTITY_TYPE.getKey(type);
    }

    public static ResourceLocation getKey(Block type)
    {
        return type.getRegistryName();
        // 1.19 will require the below option instead.
        // return Registry.BLOCK.getKey(type);
    }

    public static ResourceLocation getKey(Item type)
    {
        return type.getRegistryName();
        // 1.19 will require the below option instead.
        // return Registry.ITEM.getKey(type);
    }
}
