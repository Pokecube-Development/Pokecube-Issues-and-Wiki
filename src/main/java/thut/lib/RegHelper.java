package thut.lib;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public class RegHelper
{
    public static ResourceLocation getKey(EntityType<?> type)
    {
         return ForgeRegistries.ENTITY_TYPES.getKey(type);
    }

    public static ResourceLocation getKey(Block type)
    {
         return ForgeRegistries.BLOCKS.getKey(type);
    }

    public static ResourceLocation getKey(Item type)
    {
         return ForgeRegistries.ITEMS.getKey(type);
    }
    
    public static ResourceLocation getKey(ParticleType<?> partice)
    {
        return ForgeRegistries.PARTICLE_TYPES.getKey(partice);
    }

    public static ResourceLocation getKey(Biome biome)
    {
        return ForgeRegistries.BIOMES.getKey(biome);
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
