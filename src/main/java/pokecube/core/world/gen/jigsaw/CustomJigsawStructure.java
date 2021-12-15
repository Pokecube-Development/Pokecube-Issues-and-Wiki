package pokecube.core.world.gen.jigsaw;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.NoiseAffectingStructureFeature;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator.Context;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.Palette;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.PokecubeCore;
import pokecube.core.database.worldgen.WorldgenHandler;
import pokecube.core.events.StructureEvent;
import pokecube.core.events.StructureEvent.PickLocation;
import pokecube.core.utils.PokecubeSerializer;

public class CustomJigsawStructure extends NoiseAffectingStructureFeature<JigsawConfig>
{
    private static class PostProcessor implements BiConsumer<PieceGenerator.Context<JigsawConfig>, List<StructurePiece>>
    {
        @Override
        @SuppressWarnings("deprecation")
        public void accept(Context<JigsawConfig> context, List<StructurePiece> parts)
        {
            StructureManager templateManagerIn = context.structureManager();
            ChunkGenerator chunkGenerator = context.chunkGenerator();

            final int x = context.chunkPos().getBlockX(7);
            final int z = context.chunkPos().getBlockZ(7);
            final BlockPos blockpos = new BlockPos(x, chunkGenerator.getSeaLevel(), z);

            for (final StructurePiece part : parts) if (part instanceof final PoolElementStructurePiece p)
                if (p.getElement() instanceof final CustomJigsawPiece piece)
            {
                final int dy = piece.opts.dy;
                // Check if the part needs a shift.
                p.move(0, -dy, 0);
                p.getBoundingBox().move(0, dy, 0);

                // Check if we should place a professor.
                if (!PokecubeSerializer.getInstance().hasPlacedSpawn() && PokecubeCore.getConfig().doSpawnBuilding)
                {
                    final StructureTemplate t = piece.getTemplate(templateManagerIn);
                    if (piece.toUse == null) piece.getSettings(part.getRotation(), part.getBoundingBox(), false);
                    components:
                    for (final Palette list : t.palettes)
                    {
                        boolean foundWorldspawn = false;
                        BlockPos localSpawn = null;
                        BlockPos localTrader = null;
                        for (final StructureBlockInfo i : list.blocks())
                            if (i != null && i.nbt != null && i.state.getBlock() == Blocks.STRUCTURE_BLOCK)
                        {
                            final StructureMode structuremode = StructureMode.valueOf(i.nbt.getString("mode"));
                            if (structuremode == StructureMode.DATA)
                            {
                                final String meta = i.nbt.getString("metadata");
                                foundWorldspawn = foundWorldspawn || meta.startsWith("pokecube:worldspawn");
                                if (localSpawn == null && foundWorldspawn) localSpawn = i.pos;
                                if (meta.contains("pokecube:mob:trader")
                                        || meta.contains("pokecube:mob:pokemart_merchant"))
                                    localTrader = i.pos;
                            }
                        }
                        if (localTrader != null && localSpawn != null)
                        {
                            final ServerLevel sworld = JigsawAssmbler.getForGen(chunkGenerator);
                            final BlockPos spos = StructureTemplate.calculateRelativePosition(piece.toUse, localSpawn)
                                    .offset(blockpos).offset(0, part.getBoundingBox().minY, 0);
                            PokecubeCore.LOGGER.info("Setting spawn to {} {}, professor at {}", spos, localSpawn,
                                    localTrader);
                            PokecubeSerializer.getInstance().setPlacedSpawn();
                            sworld.getServer().execute(() -> {
                                sworld.setDefaultSpawnPos(spos, 0);
                            });
                            piece.placedSpawn = false;
                            piece.isSpawn = true;
                            piece.spawnPos = localSpawn;
                            piece.profPos = localTrader;
                            break components;
                        }
                    }
                }
            }
        }
    }

    private static class PieceGeneratorFactory implements PieceGeneratorSupplier<JigsawConfig>
    {
        @Override
        public Optional<PieceGenerator<JigsawConfig>> createGenerator(Context<JigsawConfig> context)
        {
            JigsawConfig config = context.config();

            boolean validContext = false;

            ChunkGenerator chunkGenerator = context.chunkGenerator();

            int x = context.chunkPos().getBlockX(7);
            int z = context.chunkPos().getBlockZ(7);
            int y = chunkGenerator.getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
                    context.heightAccessor());

            if (!config.struct_config.allow_void && y < config.struct_config.minY)
            {
                return Optional.empty();
            }

            int dist = context.config().struct_config.needed_space;
            boolean any = dist == -1;
            if (any) dist = 1;

            final BlockPos pos = new BlockPos(x, y, z);

            Set<Biome> biomes = context.biomeSource().getBiomesWithin(pos.getX(), pos.getY(), pos.getZ(), dist,
                    chunkGenerator.climateSampler());

            if (!any) validContext = !biomes.isEmpty();

            for (Biome b : biomes)
            {
                if (any) validContext = validContext || config.struct_config._matcher.checkBiome(b.getRegistryName());
                else validContext = validContext && config.struct_config._matcher.checkBiome(b.getRegistryName());
            }
            if (!validContext)
            {
                return Optional.empty();
            }

            WorldgenRandom rand = new WorldgenRandom(new LegacyRandomSource(0L));
            rand.setLargeFeatureSeed(context.seed(), context.chunkPos().x, context.chunkPos().z);

            final StructureEvent.PickLocation event = new PickLocation(chunkGenerator, rand, context.chunkPos(),
                    config.struct_config, context.heightAccessor());
            if (MinecraftForge.EVENT_BUS.post(event)) return Optional.empty();

            final ResourceLocation structName = new ResourceLocation(
                    config.struct_config.type.isEmpty() ? config.struct_config.name : config.struct_config.type);

            StructureFeature<?> thisFeature = WorldgenHandler.getFeature(structName);
            final JigsawAssmbler assembler = new JigsawAssmbler(config.struct_config);

            if (thisFeature != null)
            {
                // Here we check if there are any conflicting structures around.
                final int ds0 = WorldgenHandler.getNeededSpace(structName);
                final Decoration stage0 = StructureFeature.STEP.get(thisFeature);

                final ServerLevel world = JigsawAssmbler.getForGen(chunkGenerator);
                final StructureFeatureManager sfmanager = world.structureFeatureManager();
                final StructureSettings settings = chunkGenerator.getSettings();

                x = context.chunkPos().x;
                z = context.chunkPos().z;

                for (final StructureFeature<?> s : WorldgenHandler.getSortedList())
                {
                    // Check if we have reached this structure, if so, it is
                    // valid and we break.
                    if (s.getRegistryName().equals(structName)) break;

                    final int ds1 = WorldgenHandler.getNeededSpace(s.getRegistryName());
                    final int ds = Math.max(ds0, ds1);

                    final StructureFeatureConfiguration structureseparationsettings = settings.getConfig(s);
                    // This means it doesn't spawn in this world, so we skip.
                    if (structureseparationsettings == null) continue;

                    final Decoration stage1 = StructureFeature.STEP.get(s);
                    // Only care about things that are of same stage!
                    if (stage1 != stage0) continue;

                    for (int i = x - ds; i <= x + ds; ++i) for (int j = z - ds; j <= z + ds; ++j)
                    {
                        // We ask for EMPTY chunk, and allow it to be null, so
                        // that
                        // we don't cause issues if the chunk doesn't exist yet.
                        final ChunkAccess ichunk = world.getChunk(i, j, ChunkStatus.EMPTY, false);
                        // We then only care about chunks which have already
                        // reached
                        // at least this stage of loading.
                        if (ichunk == null || !ichunk.getStatus().isOrAfter(ChunkStatus.STRUCTURE_STARTS)) continue;
                        // This is the way to tell if an actual real structure
                        // would be at this location.
                        final StructureStart<?> structurestart = sfmanager
                                .getStartForFeature(SectionPos.bottomOf(ichunk), s, ichunk);
                        // This means we do conflict, so no spawn here.
                        if (structurestart != null && structurestart.isValid())
                        {
                            assembler.addConflict(structurestart.getBoundingBox());
                        }
                    }
                }
            }
            return assembler.build(context, POSTPROCESS);
        }
    }

    public static BiConsumer<PieceGenerator.Context<JigsawConfig>, List<StructurePiece>> POSTPROCESS = new PostProcessor();
    public static PieceGeneratorSupplier<JigsawConfig> SUPPLIER = new PieceGeneratorFactory();

    public int priority = 100;

    public int spacing = 6;

    public CustomJigsawStructure(final Codec<JigsawConfig> codec)
    {
        super(codec, new PieceGeneratorFactory());
    }

    @Override
    public Decoration step()
    {
        if (super.step() == null) return Decoration.SURFACE_STRUCTURES;
        else return super.step();
    }

    @Override
    public String toString()
    {
        return this.getRegistryName() + " " + this.getFeatureName();
    }
}
