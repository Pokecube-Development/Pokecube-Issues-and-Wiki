package pokecube.core.world.gen.jigsaw;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
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
    public int priority = 100;

    public int spacing = 6;

    JigsawConfig lastUsed = null;

    public static BiConsumer<PieceGenerator.Context<JigsawConfig>, List<StructurePiece>> POSTPROCESS = (context,
            parts) ->
    {
        StructureManager templateManagerIn = context.structureManager();
        ChunkGenerator chunkGenerator = context.chunkGenerator();

        final int x = context.chunkPos().getMinBlockX() + 7;
        final int z = context.chunkPos().getMinBlockZ() + 7;
        final BlockPos blockpos = new BlockPos(x, 0, z);

        for (final StructurePiece part : parts) if (part instanceof final PoolElementStructurePiece p)
            if (p.getElement()instanceof final CustomJigsawPiece piece)
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
                            if (meta.contains("pokecube:mob:trader") || meta.contains("pokecube:mob:pokemart_merchant"))
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
                        sworld.getServer().execute(() ->
                        {
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
    };

    public CustomJigsawStructure(final Codec<JigsawConfig> codec)
    {
        super(codec, (context) ->
        {
            JigsawConfig config = context.config();

            boolean validContext = true;

            if (!validContext)
            {
                return Optional.empty();
            }
            else
            {
                final JigsawAssmbler assembler = new JigsawAssmbler(config.struct_config);
                return assembler.build(context, POSTPROCESS);
            }
        });
    }

    @Override
    public Decoration step()
    {
        if (super.step() == null) return Decoration.SURFACE_STRUCTURES;
        else return super.step();
    }

    @Override
    protected boolean linearSeparation()
    {
        // End structures return false here, we might need to see about
        // adjustments to account for that, will call super for now just as an
        // initial test.
        return super.linearSeparation();
    }

    @Override
    public String toString()
    {
        return this.getRegistryName() + " " + this.getFeatureName() + " " + this.lastUsed;
    }

    @Override
    public boolean canGenerate(RegistryAccess regaccess, ChunkGenerator generator, BiomeSource biomes,
            StructureManager manager, long seed, ChunkPos pos, JigsawConfig config, LevelHeightAccessor heightAccess,
            Predicate<Biome> valid_biome)
    {
        WorldgenRandom rand = new WorldgenRandom(new LegacyRandomSource(0L));
        rand.setLargeFeatureSeed(seed, pos.x, pos.z);
        final int x = pos.x;
        final int z = pos.z;
        if (!config.struct_config.allow_void)
        {
            final int y = CustomJigsawStructure.getMinY(x, z, generator, heightAccess);
            if (y < config.struct_config.minY) return false;
        }

        final StructureEvent.PickLocation event = new PickLocation(generator, rand, pos, config.struct_config,
                heightAccess);
        if (MinecraftForge.EVENT_BUS.post(event)) return false;

        // Here we check if there are any conflicting structures around.
        final int ds0 = WorldgenHandler.getNeededSpace(this);
        final Decoration stage0 = StructureFeature.STEP.get(this);

        final ServerLevel world = JigsawAssmbler.getForGen(generator);
        final StructureFeatureManager sfmanager = world.structureFeatureManager();
        final StructureSettings settings = generator.getSettings();

        for (final StructureFeature<?> s : WorldgenHandler.getSortedList())
        {
            if (s == this) break;
            // TODO check if feature is invalid?
            
            final int ds1 = WorldgenHandler.getNeededSpace(s);
            final int ds = Math.max(ds0, ds1);

            final StructureFeatureConfiguration structureseparationsettings = settings.getConfig(s);
            // This means it doesn't spawn in this world, so we skip.
            if (structureseparationsettings == null) continue;

            final Decoration stage1 = StructureFeature.STEP.get(s);
            // Only care about things that are of same stage!
            if (stage1 != stage0) continue;

            for (int i = x - ds; i <= x + ds; ++i) for (int j = z - ds; j <= z + ds; ++j)
            {
                // We ask for EMPTY chunk, and allow it to be null, so that
                // we don't cause issues if the chunk doesn't exist yet.
                final ChunkAccess ichunk = world.getChunk(i, j, ChunkStatus.EMPTY, false);
                // We then only care about chunks which have already reached
                // at least this stage of loading.
                if (ichunk == null || !ichunk.getStatus().isOrAfter(ChunkStatus.STRUCTURE_STARTS)) continue;
                // This is the way to tell if an actual real structure would
                // be at this location.
                final StructureStart<?> structurestart = sfmanager.getStartForFeature(SectionPos.of(ichunk.getPos(), 0),
                        s, ichunk);
                // This means we do conflict, so no spawn here.
                if (structurestart != null && structurestart.isValid()) return false;
            }
        }

        // Super just returns true, but we will call it anyway incase it is
        // needed/mixined/etc
        return super.canGenerate(regaccess, generator, biomes, manager, seed, pos, config, heightAccess, valid_biome);
    }

    private static int getMinY(final int chunkX, final int chunkZ, final ChunkGenerator generatorIn,
            final LevelHeightAccessor heightAccess)
    {
        final int k = (chunkX << 4) + 7;
        final int l = (chunkZ << 4) + 7;
        final int i1 = generatorIn.getFirstOccupiedHeight(k + 5, l + 5, Heightmap.Types.WORLD_SURFACE_WG, heightAccess);
        final int j1 = generatorIn.getFirstOccupiedHeight(k + 5, l - 5, Heightmap.Types.WORLD_SURFACE_WG, heightAccess);
        final int k1 = generatorIn.getFirstOccupiedHeight(k - 5, l + 5, Heightmap.Types.WORLD_SURFACE_WG, heightAccess);
        final int l1 = generatorIn.getFirstOccupiedHeight(k - 5, l - 5, Heightmap.Types.WORLD_SURFACE_WG, heightAccess);
        return Math.min(Math.min(i1, j1), Math.min(k1, l1));
    }
}
