package pokecube.core.world.gen.jigsaw;

import java.util.Random;

import com.mojang.serialization.Codec;

import net.minecraft.block.Blocks;
import net.minecraft.state.properties.StructureMode;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.SectionPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage.Decoration;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraft.world.gen.feature.template.Template.Palette;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.PokecubeCore;
import pokecube.core.database.worldgen.WorldgenHandler;
import pokecube.core.events.StructureEvent;
import pokecube.core.events.StructureEvent.PickLocation;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokecubeSerializer;

public class CustomJigsawStructure extends Structure<JigsawConfig>
{
    public int priority = 100;

    public int spacing = 4;

    public CustomJigsawStructure(final Codec<JigsawConfig> config)
    {
        super(config);
    }

    @Override
    public IStartFactory<JigsawConfig> getStartFactory()
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
    protected boolean isFeatureChunk(final ChunkGenerator generator, final BiomeProvider biomes, final long seed,
            final SharedSeedRandom rand, final int x, final int z, final Biome biome, final ChunkPos pos,
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
        final Decoration stage0 = Structure.STEP.get(this.getStructure());

        final ServerWorld world = JigsawAssmbler.getForGen(generator);
        final StructureManager manager = world.structureFeatureManager();
        final DimensionStructuresSettings settings = generator.getSettings();
        for (final Structure<?> s : WorldgenHandler.getSortedList())
        {
            if (s == this.getStructure()) break;
            if (!biomes.canGenerateStructure(s)) continue;
            final int ds1 = WorldgenHandler.getNeededSpace(s);
            final int ds = Math.max(ds0, ds1);

            final StructureSeparationSettings structureseparationsettings = settings.getConfig(s);
            // This means it doesn't spawn in this world, so we skip.
            if (structureseparationsettings == null) continue;

            final Decoration stage1 = Structure.STEP.get(s);
            // Only care about things that are of same stage!
            if (stage1 != stage0) continue;

            for (int i = x - ds; i <= x + ds; ++i)
                for (int j = z - ds; j <= z + ds; ++j)
                {
                    // We ask for EMPTY chunk, and allow it to be null, so that
                    // we don't cause issues if the chunk doesn't exist yet.
                    final IChunk ichunk = world.getChunk(i, j, ChunkStatus.EMPTY, false);
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
        final int i1 = generatorIn.getFirstOccupiedHeight(k + 5, l + 5, Heightmap.Type.WORLD_SURFACE_WG);
        final int j1 = generatorIn.getFirstOccupiedHeight(k + 5, l - 5, Heightmap.Type.WORLD_SURFACE_WG);
        final int k1 = generatorIn.getFirstOccupiedHeight(k - 5, l + 5, Heightmap.Type.WORLD_SURFACE_WG);
        final int l1 = generatorIn.getFirstOccupiedHeight(k - 5, l - 5, Heightmap.Type.WORLD_SURFACE_WG);
        return Math.min(Math.min(i1, j1), Math.min(k1, l1));
    }

    /**
     * Handles calling up the structure's pieces class and height that structure
     * will spawn at.
     */
    public static class Start extends StructureStart<JigsawConfig>
    {
        public Start(final Structure<JigsawConfig> structureIn, final int chunkX, final int chunkZ,
                final MutableBoundingBox mutableBoundingBox, final int referenceIn, final long seedIn)
        {
            super(structureIn, chunkX, chunkZ, mutableBoundingBox, referenceIn, seedIn);
        }

        @Override
        public void generatePieces(final DynamicRegistries dynamicRegistryManager, final ChunkGenerator chunkGenerator,
                final TemplateManager templateManagerIn, final int chunkX, final int chunkZ, final Biome biomeIn,
                final JigsawConfig config)
        {
            // Turns the chunk coordinates into actual coordinates we can use.
            // (Gets center of that chunk)
            final int x = (chunkX << 4) + 7;
            final int z = (chunkZ << 4) + 7;
            final BlockPos blockpos = new BlockPos(x, 0, z);
            if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Trying to place {}", config.struct_config.name);

            final JigsawAssmbler assembler = new JigsawAssmbler(config.struct_config);
            boolean built = assembler.build(dynamicRegistryManager, new ResourceLocation(config.struct_config.root),
                    config.struct_config.size, AbstractVillagePiece::new, chunkGenerator, templateManagerIn, blockpos,
                    this.pieces, this.random, biomeIn, c -> true);

            int n = 0;
            while (!built && n++ < 20)
            {
                this.pieces.clear();
                final Random newRand = new Random(this.random.nextLong());
                built = assembler.build(dynamicRegistryManager, new ResourceLocation(config.struct_config.root),
                        config.struct_config.size, AbstractVillagePiece::new, chunkGenerator, templateManagerIn,
                        blockpos, this.pieces, newRand, biomeIn, c -> true);
                if (PokecubeMod.debug) PokecubeCore.LOGGER.warn("Try {}, {} parts.", n, this.pieces.size());
            }
            if (!built)
            {
                PokecubeCore.LOGGER.warn("Failed to complete {} in {} at {}", this.pieces,
                        config.struct_config.name, blockpos);
                return;
            }

            // Check if any components are valid spawn spots, if so, set the
            // spawned flag

            for (final StructurePiece part : this.pieces)
                if (part instanceof AbstractVillagePiece)
                {
                    final AbstractVillagePiece p = (AbstractVillagePiece) part;
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
                            final Template t = piece.getTemplate(templateManagerIn);
                            if (piece.toUse == null) piece.getSettings(part.getRotation(), part.getBoundingBox(),
                                    false);
                            components:
                            for (final Palette list : t.palettes)
                            {
                                boolean foundWorldspawn = false;
                                String tradeString = "";
                                BlockPos pos = null;
                                for (final BlockInfo i : list.blocks())
                                    if (i != null && i.nbt != null && i.state.getBlock() == Blocks.STRUCTURE_BLOCK)
                                    {
                                        final StructureMode structuremode = StructureMode.valueOf(i.nbt.getString(
                                                "mode"));
                                        if (structuremode == StructureMode.DATA)
                                        {
                                            final String meta = i.nbt.getString("metadata");
                                            foundWorldspawn = foundWorldspawn || meta.startsWith("pokecube:worldspawn");
                                            if (pos == null && foundWorldspawn) pos = i.pos;
                                            if (meta.startsWith("pokecube:mob:trader")) tradeString = meta;
                                        }
                                    }
                                if (!tradeString.isEmpty() && foundWorldspawn)
                                {
                                    final ServerWorld sworld = JigsawAssmbler.getForGen(chunkGenerator);
                                    final BlockPos spos = Template.calculateRelativePosition(piece.toUse, pos).offset(blockpos)
                                            .offset(0, part.getBoundingBox().y0, 0);
                                    PokecubeCore.LOGGER.info("Setting spawn to {} {}", spos, pos);
                                    sworld.getServer().execute(() ->
                                    {
                                        sworld.setDefaultSpawnPos(spos, 0);
                                    });
                                    PokecubeSerializer.getInstance().setPlacedSpawn();
                                    piece.isSpawn = true;
                                    piece.spawnReplace = tradeString;
                                    piece.mask = new MutableBoundingBox(part.getBoundingBox());
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
                    + this.getBoundingBox().getCenter().getY() + " " + blockpos.getZ() + " of size "
                    + this.pieces.size() + " " + this.getBoundingBox().getLength());
        }

    }
}
