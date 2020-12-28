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
    public Decoration getDecorationStage()
    {
        if (super.getDecorationStage() == null) return Decoration.SURFACE_STRUCTURES;
        else return super.getDecorationStage();
    }

    @Override
    protected boolean func_230365_b_()
    {
        // End structures return false here, we might need to see about
        // adjustments to account for that, will call super for now just as an
        // initial test.
        return super.func_230365_b_();
    }

    @Override
    protected boolean func_230363_a_(final ChunkGenerator generator, final BiomeProvider biomes, final long seed,
            final SharedSeedRandom rand, final int x, final int z, final Biome biome, final ChunkPos pos,
            final JigsawConfig config)
    {
        if (!config.struct_config.allow_void)
        {
            final int y = CustomJigsawStructure.getMinY(x, z, generator);
            if (y <= 5) return false;
        }

        final StructureEvent.PickLocation event = new PickLocation(generator, rand, x, z, config.struct_config);
        if(MinecraftForge.EVENT_BUS.post(event)) return false;

        // Here we check if there are any conflicting structures around.
        final int ds0 = WorldgenHandler.getNeededSpace(this.getStructure());
        final Decoration stage0 = Structure.STRUCTURE_DECORATION_STAGE_MAP.get(this.getStructure());

        final ServerWorld world = JigsawAssmbler.getForGen(generator);
        final StructureManager manager = world.func_241112_a_();
        final DimensionStructuresSettings settings = generator.func_235957_b_();
        for (final Structure<?> s : WorldgenHandler.getSortedList())
        {
            if (s == this.getStructure()) break;
            if (!biomes.hasStructure(s)) continue;
            final int ds1 = WorldgenHandler.getNeededSpace(s);
            final int ds = Math.max(ds0, ds1);

            final StructureSeparationSettings structureseparationsettings = settings.func_236197_a_(s);
            // This means it doesn't spawn in this world, so we skip.
            if (structureseparationsettings == null) continue;

            final Decoration stage1 = Structure.STRUCTURE_DECORATION_STAGE_MAP.get(s);
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
                    if (ichunk == null || !ichunk.getStatus().isAtLeast(ChunkStatus.STRUCTURE_STARTS)) continue;
                    // This is the way to tell if an actual real structure would
                    // be at this location.
                    final StructureStart<?> structurestart = manager.getStructureStart(SectionPos.from(ichunk.getPos(),
                            0), s, ichunk);
                    // This means we do conflict, so no spawn here.
                    if (structurestart != null && structurestart.isValid()) return false;
                }
        }

        // Super just returns true, but we will call it anyway incase it is
        // needed/mixined/etc
        return super.func_230363_a_(generator, biomes, seed, rand, x, z, biome, pos, config);
    }

    private static int getMinY(final int chunkX, final int chunkZ, final ChunkGenerator generatorIn)
    {
        final int k = (chunkX << 4) + 7;
        final int l = (chunkZ << 4) + 7;
        final int i1 = generatorIn.getNoiseHeightMinusOne(k + 5, l + 5, Heightmap.Type.WORLD_SURFACE_WG);
        final int j1 = generatorIn.getNoiseHeightMinusOne(k + 5, l - 5, Heightmap.Type.WORLD_SURFACE_WG);
        final int k1 = generatorIn.getNoiseHeightMinusOne(k - 5, l + 5, Heightmap.Type.WORLD_SURFACE_WG);
        final int l1 = generatorIn.getNoiseHeightMinusOne(k - 5, l - 5, Heightmap.Type.WORLD_SURFACE_WG);
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
        public void func_230364_a_(final DynamicRegistries dynamicRegistryManager, final ChunkGenerator chunkGenerator,
                final TemplateManager templateManagerIn, final int chunkX, final int chunkZ, final Biome biomeIn,
                final JigsawConfig config)
        {
            // Turns the chunk coordinates into actual coordinates we can use.
            // (Gets center of that chunk)
            final int x = (chunkX << 4) + 7;
            final int z = (chunkZ << 4) + 7;
            final BlockPos blockpos = new BlockPos(x, 0, z);

            final JigsawAssmbler assembler = new JigsawAssmbler(config.struct_config);
            boolean built = assembler.build(dynamicRegistryManager, new ResourceLocation(config.struct_config.root),
                    config.struct_config.size, AbstractVillagePiece::new, chunkGenerator, templateManagerIn, blockpos,
                    this.components, this.rand, biomeIn, c -> true);

            int n = 0;
            while (!built && n++ < 20)
            {
                this.components.clear();
                final Random newRand = new Random(this.rand.nextLong());
                built = assembler.build(dynamicRegistryManager, new ResourceLocation(config.struct_config.root),
                        config.struct_config.size, AbstractVillagePiece::new, chunkGenerator, templateManagerIn,
                        blockpos, this.components, newRand, biomeIn, c -> true);
                PokecubeCore.LOGGER.warn("Try {}, {} parts.", n, this.components.size());
            }
            if (!built) PokecubeCore.LOGGER.warn("Failed to complete a structure at " + blockpos);

            // Check if any components are valid spawn spots, if so, set the
            // spawned flag

            for (final StructurePiece part : this.components)
                if (part instanceof AbstractVillagePiece)
                {
                    final AbstractVillagePiece p = (AbstractVillagePiece) part;
                    if (p.getJigsawPiece() instanceof CustomJigsawPiece)
                    {
                        final CustomJigsawPiece piece = (CustomJigsawPiece) p.getJigsawPiece();
                        // Check if the part needs a shift.
                        p.offset(0, -piece.opts.dy, 0);

                        // Check if we should place a professor.
                        if (!PokecubeSerializer.getInstance().hasPlacedSpawn() && PokecubeCore
                                .getConfig().doSpawnBuilding)
                        {
                            final Template t = piece.getTemplate(templateManagerIn);
                            if (piece.toUse == null) piece.func_230379_a_(part.getRotation(), part.getBoundingBox(),
                                    false);
                            components:
                            for (final Palette list : t.blocks)
                            {
                                boolean foundWorldspawn = false;
                                String tradeString = "";
                                BlockPos pos = null;
                                for (final BlockInfo i : list.func_237157_a_())
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
                                    final BlockPos spos = Template.transformedBlockPos(piece.toUse, pos).add(blockpos)
                                            .add(0, part.getBoundingBox().minY, 0);
                                    PokecubeCore.LOGGER.info("Setting spawn to {} {}", spos, pos);
                                    sworld.getServer().execute(() ->
                                    {
                                        sworld.func_241124_a__(spos, 0);
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
            this.recalculateStructureSize();

            // I use to debug and quickly find out if the structure is spawning
            // or not and where it is.
            if (PokecubeCore.getConfig().debug) PokecubeCore.LOGGER.debug(config.struct_config.name + " at " + blockpos
                    .getX() + " " + this.getBoundingBox().func_215126_f().getY() + " " + blockpos.getZ() + " of size "
                    + this.components.size() + " " + this.getBoundingBox().getLength());
        }

    }
}
