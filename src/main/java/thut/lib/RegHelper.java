package thut.lib;

import com.mojang.serialization.Codec;

import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
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
    public static final ResourceKey<Registry<EntityType<?>>> ENTITY_TYPE_REGISTRY = key("entity_type");
    public static final ResourceKey<Registry<BlockEntityType<?>>> BLOCK_ENTITY_TYPE_REGISTRY = key("block_entity_type");
    public static final ResourceKey<Registry<Level>> DIMENSION_REGISTRY = key("dimension");
    public static final ResourceKey<Registry<RecipeType<?>>> RECIPE_TYPE_REGISTRY = key("recipe_type");
    public static final ResourceKey<Registry<MenuType<?>>> MENU_REGISTRY = key("menu");
    public static final ResourceKey<Registry<SoundEvent>> SOUND_EVENT_REGISTRY = key("sound_event");
    public static final ResourceKey<Registry<PaintingVariant>> PAINTING_VARIANT_REGISTRY = key("painting_variant");
    public static final ResourceKey<Registry<LootItemFunctionType>> LOOT_FUNCTION_REGISTRY = key("loot_function_type");
    
    public static final ResourceKey<Registry<Activity>> ACTIVITY_REGISTRY = key("activity");
    public static final ResourceKey<Registry<Schedule>> SCHEDULE_REGISTRY = key("schedule");
    public static final ResourceKey<Registry<MemoryModuleType<?>>> MEMORY_MODULE_TYPE_REGISTRY = key("memory_module_type");
    public static final ResourceKey<Registry<SensorType<?>>> SENSOR_TYPE_REGISTRY = key("sensor_type");

    public static final ResourceKey<Registry<Structure>> STRUCTURE_REGISTRY = key("worldgen/structure");
    public static final ResourceKey<Registry<Biome>> BIOME_REGISTRY = key("worldgen/biome");
    public static final ResourceKey<Registry<StructureProcessorList>> PROCESSOR_LIST_REGISTRY = key("worldgen/processor_list");
    public static final ResourceKey<Registry<StructureType<?>>> STRUCTURE_TYPE_REGISTRY = key("worldgen/structure_type");
    public static final ResourceKey<Registry<ConfiguredFeature<?, ?>>> CONFIGURED_FEATURE_REGISTRY = key("worldgen/configured_feature");
    public static final ResourceKey<Registry<Codec<? extends ChunkGenerator>>> CHUNK_GENERATOR_REGISTRY = key("worldgen/chunk_generator");
    public static final ResourceKey<Registry<PlacedFeature>> PLACED_FEATURE_REGISTRY = key("worldgen/placed_feature");
    public static final ResourceKey<Registry<StructureProcessorType<?>>> STRUCTURE_PROCESSOR_REGISTRY = key("worldgen/structure_processor");
    public static final ResourceKey<Registry<StructurePoolElementType<?>>> STRUCTURE_POOL_ELEMENT_REGISTRY = key("worldgen/structure_pool_element");
    public static final ResourceKey<Registry<FoliagePlacerType<?>>> FOLIAGE_PLACER_TYPE_REGISTRY = key("worldgen/foliage_placer_type");
    public static final ResourceKey<Registry<TrunkPlacerType<?>>> TRUNK_PLACER_TYPE_REGISTRY = key("worldgen/trunk_placer_type");
    
    private static <T> ResourceKey<Registry<T>> key(String name)
    {
        return ResourceKey.createRegistryKey(new ResourceLocation(name));
    }
}
