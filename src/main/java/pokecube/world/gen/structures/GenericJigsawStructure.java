package pokecube.world.gen.structures;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import pokecube.api.PokecubeAPI;
import pokecube.core.PokecubeCore;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.world.gen.structures.configs.ExpandedJigsawConfiguration;
import pokecube.world.gen.structures.configs.ExpandedJigsawConfiguration.AvoidanceSettings.AvoidanceEntry;
import pokecube.world.gen.structures.pieces.ExpandedPoolElementStructurePiece;
import pokecube.world.gen.structures.utils.ExpandedJigsawPacement;
import pokecube.world.gen.structures.utils.ExpandedPostPlacementProcessor;

public abstract class GenericJigsawStructure extends StructureFeature<ExpandedJigsawConfiguration>
{
    private final GenerationStep.Decoration step;

    public GenericJigsawStructure(GenerationStep.Decoration step)
    {
        // Create the pieces layout of the structure and give it to the game
        super(ExpandedJigsawConfiguration.CODEC, GenericJigsawStructure::createPiecesGenerator,
                ExpandedPostPlacementProcessor.INSTANCE);
        this.step = step;
    }

    @Override
    public final GenerationStep.Decoration step()
    {
        return step;
    }

    public static boolean tooClose(PieceGeneratorSupplier.Context<ExpandedJigsawConfiguration> context)
    {
        ExpandedJigsawConfiguration config = context.config();
        ChunkPos pos = context.chunkPos();
        Level level = ExpandedJigsawPacement.getForGen(context);
        List<AvoidanceEntry> avoidances = config.avoidances.avoidances;
        BlockPos bpos = pos.getMiddleBlockPosition(0);
        for (var avoid : avoidances)
        {
            if (avoid.distance > 0 && !avoid.name.isBlank()) if (!PokecubeSerializer.getInstance()
                    .shouldPlace(avoid.name, bpos, level.dimension(), avoid.distance * 16))
            {
                if (PokecubeCore.getConfig().debug_misc)
                    PokecubeAPI.logDebug(config.avoidances.flags + " Conflicts with " + avoid.name);
                return true;
            }
        }
        return false;
    }

    public static void markPlaced(PieceGeneratorSupplier.Context<ExpandedJigsawConfiguration> context)
    {
        ExpandedJigsawConfiguration config = context.config();
        List<String> flags = config.avoidances.flags;
        if (flags.isEmpty()) return;
        ChunkPos pos = context.chunkPos();
        Level level = ExpandedJigsawPacement.getForGen(context);
        BlockPos bpos = pos.getMiddleBlockPosition(0);
        if (PokecubeCore.getConfig().debug_misc)
            PokecubeAPI.logDebug(config.avoidances.flags + " " + level.dimension());
        for (String flag : flags) PokecubeSerializer.getInstance().place(flag.strip(), bpos, level.dimension());
    }

    private static boolean isFeatureChunk(PieceGeneratorSupplier.Context<ExpandedJigsawConfiguration> context)
    {
        ExpandedJigsawConfiguration config = context.config();
        ChunkGenerator generator = context.chunkGenerator();
        BiomeSource biomes = context.biomeSource();
        ChunkPos pos = context.chunkPos();

        if (tooClose(context)) return false;

        // Check if we need to avoid any structures.
        for (ResourceKey<StructureSet> key : config.structures_to_avoid)
        {
            if (generator.hasFeatureChunkInRange(key, context.seed(), pos.x, pos.z, config.avoid_range))
            {
                if (PokecubeCore.getConfig().debug_misc)
                    PokecubeAPI.logDebug("Skipping generation of {} due to conflict with {}",
                            context.config().startPool().value().getName(), key);
                return false;
            }
        }

        // Check if we have enough biome room around us.
        if (config.biome_room > 0 || config.hasValidator())
        {
            BlockPos p = pos.getMiddleBlockPosition(0);
            int y = generator.getBaseHeight(p.getX(), p.getZ(), config.height_type, context.heightAccessor());
            Set<Holder<Biome>> biome_set = biomes.getBiomesWithin(p.getX(), y, p.getZ(), config.biome_room,
                    generator.climateSampler());
            for (var holder : biome_set)
            {
                if (!context.validBiome().test(holder)) return false;
                if (!config.isValid(holder)) return false;
            }
        }

        // Check the settings for max slope and other height bounds
        int max_y = Integer.MIN_VALUE;
        int min_y = Integer.MAX_VALUE;
        for (int x = pos.x - config.y_settings.y_check_radius; x <= pos.x + config.y_settings.y_check_radius; x++)
            for (int z = pos.z - config.y_settings.y_check_radius; z <= pos.z + config.y_settings.y_check_radius; z++)
        {
            int height = context.chunkGenerator().getBaseHeight((x << 4) + 7, (z << 4) + 7, config.height_type,
                    context.heightAccessor());
            max_y = Math.max(max_y, height);
            min_y = Math.min(min_y, height);
            if (min_y < config.y_settings.min_y) return false;
            if (max_y > config.y_settings.max_y) return false;
        }
        if (max_y - min_y > config.y_settings.max_dy)
        {
            return false;
        }
        return true;
    }

    private static Optional<PieceGenerator<ExpandedJigsawConfiguration>> createPiecesGenerator(
            PieceGeneratorSupplier.Context<ExpandedJigsawConfiguration> context)
    {

        // Check if the spot is valid for our structure. This is just as another
        // method for cleanness.
        // Returning an empty optional tells the game to skip this spot as it
        // will not generate the structure.
        if (!GenericJigsawStructure.isFeatureChunk(context))
        {
            return Optional.empty();
        }

        // Turns the chunk coordinates into actual coordinates we can use. (Gets
        // center of that chunk)
        BlockPos blockpos = context.chunkPos().getMiddleBlockPosition(0);
        Pools.bootstrap();

        Optional<PieceGenerator<ExpandedJigsawConfiguration>> structurePiecesGenerator;

        structurePiecesGenerator = ExpandedJigsawPacement.addPieces(context, ExpandedPoolElementStructurePiece::new,
                blockpos, false, true);

        // Return the pieces generator that is now set up so that the game runs
        // it when it needs to create the layout of structure pieces.
        return structurePiecesGenerator;
    }
}
