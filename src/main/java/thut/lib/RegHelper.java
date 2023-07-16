package thut.lib;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

public class RegHelper
{
    public static ResourceLocation getKey(EntityType<?> type)
    {
        return type.getRegistryName();
        // 1.19 will require the below option instead.
        // return ForgeRegistries.ENTITIES.getKey(type);
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
    
    public static ResourceLocation getKey(ParticleType<?> partice)
    {
        return partice.getRegistryName();
        // 1.19 will require the below option instead.
        // return ForgeRegistries.PARTICLE_TYPES.getKey(type);
    }

    public static ResourceLocation getKey(Biome biome)
    {
        return biome.getRegistryName();
    }

    public static ResourceLocation getKey(ItemStack stack)
    {
        return getKey(stack.getItem());
    }
    public static ResourceLocation getKey(Entity mob)
    {
        return getKey(mob.getType());
    }
}
