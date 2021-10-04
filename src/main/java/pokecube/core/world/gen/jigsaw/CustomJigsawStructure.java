package pokecube.core.world.gen.jigsaw;

import java.util.Random;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
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
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.Palette;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.PokecubeCore;
import pokecube.core.database.worldgen.WorldgenHandler;
import pokecube.core.events.StructureEvent;
import pokecube.core.events.StructureEvent.PickLocation;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokecubeSerializer;

public class CustomJigsawStructure extends StructureFeature<JigsawConfig>
{
    public int priority = 100;

    public int spacing = 6;

    public CustomJigsawStructure(final Codec<JigsawConfig> config)
    {
        super(config);
    }

    @Override
    public StructureStartFactory<JigsawConfig> getStartFactory()
    {
        return Start::new;
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
    protected boolean isFeatureChunk(final ChunkGenerator generator, final BiomeSource biomes, final long seed,
            final WorldgenRandom rand, final int x, final int z, final Biome biome, final ChunkPos pos,
            final JigsawConfig config)
    {
        if (!config.struct_config.allow_void)
        {
            final int y = CustomJigsawStructure.getMinY(x, z, generator);
            if (y < config.struct_config.minY) return false;
        }

        final StructureEvent.PickLocation event = new PickLocation(generator, rand, x, z, config.struct_config);
        if (MinecraftForge.EVENT_BUS.post(event)) return false;

        // Here we check if there are any conflicting structures around.
        final int ds0 = WorldgenHandler.getNeededSpace(this.getStructure());
        final Decoration stage0 = StructureFeature.STEP.get(this.getStructure());

        final ServerLevel world = JigsawAssmbler.getForGen(generator);
        final StructureFeatureManager manager = world.structureFeatureManager();
        final StructureSettings settings = generator.getSettings();
        for (final StructureFeature<?> s : WorldgenHandler.getSortedList())
        {
            if (s == this.getStructure()) break;
            if (!biomes.canGenerateStructure(s)) continue;
            final int ds1 = WorldgenHandler.getNeededSpace(s);
            final int ds = Math.max(ds0, ds1);

            final StructureFeatureConfiguration structureseparationsettings = settings.getConfig(s);
            // This means it doesn't spawn in this world, so we skip.
            if (structureseparationsettings == null) continue;

            final Decoration stage1 = StructureFeature.STEP.get(s);
            // Only care about things that are of same stage!
            if (stage1 != stage0) continue;

            for (int i = x - ds; i <= x + ds; ++i)
                for (int j = z - ds; j <= z + ds; ++j)
                {
                    // We ask for EMPTY chunk, and allow it to be null, so that
                    // we don't cause issues if the chunk doesn't exist yet.
                    final ChunkAccess ichunk = world.getChunk(i, j, ChunkStatus.EMPTY, false);
                    // We then only care about chunks which have already reached
                    // at least this stage of loading.
                    if (ichunk == null || !ichunk.getStatus().isOrAfter(ChunkStatus.STRUCTURE_STARTS)) continue;
                    // This is the way to tell if an actual real structure would
                    // be at this location.
                    final StructureStart<?> structurestart = manager.getStartForFeature(SectionPos.of(ichunk.getPos(),
                            0), s, ichunk);
                    // This means we do conflict, so no spawn here.
                    if (structurestart != null && structurestart.isValid()) return false;
                }
        }

        // Super just returns true, but we will call it anyway incase it is
        // needed/mixined/etc
        return super.isFeatureChunk(generator, biomes, seed, rand, x, z, biome, pos, config);
    }

    private static int getMinY(final int chunkX, final int chunkZ, final ChunkGenerator generatorIn)
    {
        final int k = (chunkX << 4) + 7;
        final int l = (chunkZ << 4) + 7;
        final int i1 = generatorIn.getFirstOccupiedHeight(k + 5, l + 5, Heightmap.Types.WORLD_SURFACE_WG);
        final int j1 = generatorIn.getFirstOccupiedHeight(k + 5, l - 5, Heightmap.Types.WORLD_SURFACE_WG);
        final int k1 = generatorIn.getFirstOccupiedHeight(k - 5, l + 5, Heightmap.Types.WORLD_SURFACE_WG);
        final int l1 = generatorIn.getFirstOccupiedHeight(k - 5, l - 5, Heightmap.Types.WORLD_SURFACE_WG);
        return Math.min(Math.min(i1, j1), Math.min(k1, l1));
    }

    /**
     * Handles calling up the structure's pieces class and height that structure
     * will spawn at.
     */
    public static class Start extends StructureStart<JigsawConfig>
    {
        public Start(final StructureFeature<JigsawConfig> structureIn, final int chunkX, final int chunkZ,
                final BoundingBox mutableBoundingBox, final int referenceIn, final long seedIn)
        {
            super(structureIn, chunkX, chunkZ, mutableBoundingBox, referenceIn, seedIn);
        }

        @Override
        public void generatePieces(final RegistryAccess dynamicRegistryManager, final ChunkGenerator chunkGenerator,
                final StructureManager templateManagerIn, final int chunkX, final int chunkZ, final Biome biomeIn,
                final JigsawConfig config)
        {
            // Turns the chunk coordinates into actual coordinates we can use.
            // (Gets center of that chunk)
            final int x = (chunkX << 4) + 7;
            final int z = (chunkZ << 4) + 7;
            final BlockPos blockpos = new BlockPos(x, 0, z);
            if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Trying to place {}", config.struct_config.name);

            try
            {
                final JigsawAssmbler assembler = new JigsawAssmbler(config.struct_config);
                boolean built = assembler.build(dynamicRegistryManager, new ResourceLocation(config.struct_config.root),
                        config.struct_config.size, PoolElementStructurePiece::new, chunkGenerator, templateManagerIn,
                        blockpos, this.pieces, this.random, biomeIn, c -> true);

                int n = 0;
                while (!built && n++ < 20)
                {
                    this.pieces.clear();
                    final Random newRand = new Random(this.random.nextLong());
                    built = assembler.build(dynamicRegistryManager, new ResourceLocation(config.struct_config.root),
                            config.struct_config.size, PoolElementStructurePiece::new, chunkGenerator, templateManagerIn,
                            blockpos, this.pieces, newRand, biomeIn, c -> true);
                    if (PokecubeMod.debug) PokecubeCore.LOGGER.warn("Try {}, {} parts.", n, this.pieces.size());
                }
                if (!built)
                {
                    PokecubeCore.LOGGER.warn("Failed to complete {} in {} at {}", this.pieces,
                            config.struct_config.name, blockpos);
                    return;
                }
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.warn("Failed to complete {} in {} at {}", this.pieces, config.struct_config.name,
                        blockpos, e);
                return;
            }

            // Check if any components are valid spawn spots, if so, set the
            // spawned flag

            for (final StructurePiece part : this.pieces)
                if (part instanceof PoolElementStructurePiece)
                {
                    final PoolElementStructurePiece p = (PoolElementStructurePiece) part;
                    if (p.getElement() instanceof CustomJigsawPiece)
                    {
                        final CustomJigsawPiece piece = (CustomJigsawPiece) p.getElement();
                        final int dy = piece.opts.dy;
                        // Check if the part needs a shift.
                        p.move(0, -dy, 0);
                        p.getBoundingBox().move(0, dy, 0);

                        // Check if we should place a professor.
                        if (!PokecubeSerializer.getInstance().hasPlacedSpawn() && PokecubeCore
                                .getConfig().doSpawnBuilding)
                        {
                            final StructureTemplate t = piece.getTemplate(templateManagerIn);
                            if (piece.toUse == null) piece.getSettings(part.getRotation(), part.getBoundingBox(),
                                    false);
                            components:
                            for (final Palette list : t.palettes)
                            {
                                boolean foundWorldspawn = false;
                                String tradeString = "";
                                BlockPos pos = null;
                                for (final StructureBlockInfo i : list.blocks())
                                    if (i != null && i.nbt != null && i.state.getBlock() == Blocks.STRUCTURE_BLOCK)
                                    {
                                        final StructureMode structuremode = StructureMode.valueOf(i.nbt.getString(
                                                "mode"));
                                        if (structuremode == StructureMode.DATA)
                                        {
                                            final String meta = i.nbt.getString("metadata");
                                            foundWorldspawn = foundWorldspawn || meta.startsWith("pokecube:worldspawn");
                                            if (pos == null && foundWorldspawn) pos = i.pos;
                                            if (meta.startsWith("pokecube:mob:trader") || meta.startsWith(
                                                    "pokecube:mob:pokemart_merchant")) tradeString = meta;
                                        }
                                    }
                                if (!tradeString.isEmpty() && foundWorldspawn)
                                {
                                    final ServerLevel sworld = JigsawAssmbler.getForGen(chunkGenerator);
                                    final BlockPos spos = StructureTemplate.calculateRelativePosition(piece.toUse, pos).offset(
                                            blockpos).offset(0, part.getBoundingBox().y0, 0);
                                    PokecubeCore.LOGGER.info("Setting spawn to {} {}", spos, pos);
                                    sworld.getServer().execute(() ->
                                    {
                                        sworld.setDefaultSpawnPos(spos, 0);
                                    });
                                    PokecubeSerializer.getInstance().setPlacedSpawn();
                                    piece.isSpawn = true;
                                    piece.spawnReplace = tradeString;
                                    piece.mask = new BoundingBox(part.getBoundingBox());
                                    break components;
                                }
                            }
                        }
                    }
                }
            // Sets the bounds of the structure once you are finished.
            this.calculateBoundingBox();

            // I use to debug and quickly find out if the structure is spawning
            // or not and where it is.
            if (PokecubeMod.debug) PokecubeCore.LOGGER.debug(config.struct_config.name + " at " + blockpos.getX() + " "
                    + this.getBoundingBox().getCenter().getY() + " " + blockpos.getZ() + " of size " + this.pieces
                            .size() + " " + this.getBoundingBox().getLength());
        }

    }
}
