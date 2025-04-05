package pokecube.world.gen.structures;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.level.LevelEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.world.PokecubeWorld;
import pokecube.world.gen.structures.pool_elements.ExpandedJigsawPiece;
import thut.core.common.ThutCore;
import thut.lib.RegHelper;

public class PokecubeStructures
{
    public static final Supplier<StructureType<GenericJigsawStructure>> STRUCTURES;
    public static final Supplier<StructurePoolElementType<ExpandedJigsawPiece>> EXPANDED_POOL_ELEMENT;

    static
    {
        STRUCTURES = PokecubeWorld.STRUCTURE_TYPES.register("generic_surface_jigsaw",
                () -> () -> GenericJigsawStructure.CODEC);
        EXPANDED_POOL_ELEMENT = PokecubeWorld.POOL_ELEMENT_TYPES.register("expanded_pool_element",
                () -> () -> ExpandedJigsawPiece.makeCodec());
    }

    public static void init(final IEventBus bus)
    {
        ThutCore.FORGE_BUS.addListener(PokecubeStructures::spawnVillageChecker);
    }

    private static void spawnVillageChecker(final LevelEvent.Load event)
    {
        if (event.getLevel().isClientSide()) return;
        if (event.getLevel() instanceof ServerLevel serverWorld)
        {
            final ResourceKey<Level> key = serverWorld.dimension();
            if (PokecubeCore.getConfig().doSpawnBuilding && !PokecubeSerializer.getInstance().hasPlacedSpawn()
                    && key.equals(Level.OVERWORLD))
            {
                serverWorld.getServer().execute(() -> {
                    final ResourceLocation location = ResourceLocation.parse("pokecube_world:starting_town");
                    TagKey<Structure> tagkey = TagKey.create(RegHelper.STRUCTURE_REGISTRY, location);
                    serverWorld.findNearestMapStructure(tagkey, BlockPos.ZERO, 5, false);
                });
            }
        }
    }
}
