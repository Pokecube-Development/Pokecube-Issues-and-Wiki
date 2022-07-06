package pokecube.world.gen.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import pokecube.core.PokecubeCore;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.world.PokecubeWorld;
import pokecube.world.gen.structures.pool_elements.ExpandedJigsawPiece;

public class PokecubeStructures
{
    public static final RegistryObject<StructureFeature<?>> SURFACE_STRUCTURES;
    public static final RegistryObject<StructureFeature<?>> SUBSURFACE_STRUCTURES;
    public static final RegistryObject<StructurePoolElementType<ExpandedJigsawPiece>> EXPANDED_POOL_ELEMENT;

    static
    {
        SURFACE_STRUCTURES = PokecubeWorld.STRUCTURE_TYPES.register("generic_surface_jigsaw",
                GenericSurfaceJigsawStructure::new);
        SUBSURFACE_STRUCTURES = PokecubeWorld.STRUCTURE_TYPES.register("generic_underground_jigsaw",
                GenericSurfaceJigsawStructure::new);
        EXPANDED_POOL_ELEMENT = PokecubeWorld.POOL_ELEMENT_TYPES.register("expanded_pool_element",
                () -> () -> ExpandedJigsawPiece.makeCodec());
    }

    public static void init(final IEventBus bus)
    {
        MinecraftForge.EVENT_BUS.addListener(PokecubeStructures::spawnVillageChecker);
    }

    private static void spawnVillageChecker(final WorldEvent.Load event)
    {
        if (event.getWorld().isClientSide()) return;
        if (event.getWorld() instanceof ServerLevel serverWorld)
        {
            final ResourceKey<Level> key = serverWorld.dimension();
            if (PokecubeCore.getConfig().doSpawnBuilding && !PokecubeSerializer.getInstance().hasPlacedSpawn()
                    && key.equals(Level.OVERWORLD))
            {
                serverWorld.getServer().execute(() -> {
                    final ResourceLocation location = new ResourceLocation("pokecube_world:town");
                    TagKey<ConfiguredStructureFeature<?, ?>> tagkey = TagKey
                            .create(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, location);
                    serverWorld.findNearestMapFeature(tagkey, BlockPos.ZERO, 5, false);
                });
            }
        }
    }
}
