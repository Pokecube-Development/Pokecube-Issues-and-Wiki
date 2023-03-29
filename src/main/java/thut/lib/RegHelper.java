package thut.lib;

import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistries.Keys;
import net.minecraftforge.server.ServerLifecycleHooks;

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
        return ServerLifecycleHooks.getCurrentServer().registryAccess().registryOrThrow(Keys.BIOMES).getKey(biome);
    }

    public static ResourceLocation getKey(ItemStack stack)
    {
        return getKey(stack.getItem());
    }

    public static ResourceLocation getKey(Entity mob)
    {
        return getKey(mob.getType());
    }

    public static final ResourceKey<Registry<Block>> BLOCK_REGISTRY = key("block");
    public static final ResourceKey<Registry<Item>> ITEM_REGISTRY = key("item");
    public static final ResourceKey<Registry<Structure>> STRUCTURE_REGISTRY = key("worldgen/structure");
    public static final ResourceKey<Registry<Biome>> BIOME_REGISTRY = key("worldgen/biome");
    public static final ResourceKey<Registry<Level>> DIMENSION_REGISTRY = key("dimension");
    public static final ResourceKey<Registry<EntityType<?>>> ENTITY_TYPE_REGISTRY = key("entity_type");
    public static final ResourceKey<Registry<ConfiguredFeature<?, ?>>> CONFIGURED_FEATURE_REGISTRY = key(
            "worldgen/configured_feature");

    private static <T> ResourceKey<Registry<T>> key(String name)
    {
        return ResourceKey.createRegistryKey(new ResourceLocation(name));
    }
}
