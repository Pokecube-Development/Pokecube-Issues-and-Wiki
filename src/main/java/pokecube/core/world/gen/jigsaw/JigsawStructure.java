package pokecube.core.world.gen.jigsaw;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;

import net.minecraft.block.Blocks;
import net.minecraft.state.properties.StructureMode;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.feature.structure.MarginedStructureStart;
import net.minecraft.world.gen.feature.structure.ScatteredStructure;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.PokecubeCore;
import pokecube.core.database.worldgen.WorldgenHandler;
import pokecube.core.database.worldgen.WorldgenHandler.JigSawConfig;
import pokecube.core.events.StructureEvent.PickLocation;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.world.gen.jigsaw.JigsawPieces.CustomJigsawPiece;
import pokecube.core.world.gen.jigsaw.JigsawPieces.SingleOffsetPiece;

public class JigsawStructure extends ScatteredStructure<JigsawConfig>
{
    final String name;
    final int    modifier;
    JigSawConfig struct;

    List<JigSawConfig> structs = Lists.newArrayList();

    public JigsawStructure(final String name)
    {
        super(JigsawConfig::deserialize);
        this.name = name;
        this.modifier = this.name.hashCode();
    }

    public JigsawStructure addStruct(final JigSawConfig struct)
    {
        boolean has = false;
        for (final JigSawConfig c : this.structs)
            if (c.name.equals(struct.name))
            {
                has = true;
                break;
            }
        if (!has) this.structs.add(struct);
        return this;
    }

    JigSawConfig getStruct()
    {
        if (this.struct == null && !this.structs.isEmpty()) this.struct = this.structs.get(0);
        return this.struct;
    }

    @Override
    public JigsawConfig createConfig(final Dynamic<?> p_214470_1_)
    {
        final JigsawConfig config = super.createConfig(p_214470_1_);
        this.struct = config.struct;
        return config;
    }

    @Override
    public String getStructureName()
    {
        return this.name;
    }

    @Override
    public int getSize()
    {
        return this.getStruct() != null ? this.getStruct().size : 8;
    }

    @Override
    public ChunkPos getStartPositionForPosition(final ChunkGenerator<?> chunkGenerator, final Random random, int x,
            int z, final int spacingOffsetsX, final int spacingOffsetsZ)
    {
        final int i = this.getBiomeFeatureDistance(chunkGenerator);
        final int j = this.getBiomeFeatureSeparation(chunkGenerator);

        final int dx = i * spacingOffsetsX;
        final int dz = i * spacingOffsetsZ;
        final int base = i;

        x += dx;
        z += dz;

        int sx = x / base;
        int sz = z / base;
        ((SharedSeedRandom) random).setLargeFeatureSeedWithSalt(chunkGenerator.getSeed(), sx, sz, this
                .getSeedModifier());

        sx = sx * base + random.nextInt(i - j + 1);
        sz = sz * base + random.nextInt(i - j + 1);

        boolean blocked = WorldgenHandler.isBlocked(chunkGenerator, random, this, sx, sz);
        int tries = 0;
        while (blocked && tries++ < 20)
        {
            sx = x / base;
            sz = z / base;
            sx = sx * base + random.nextInt(i - j + 1);
            sz = sz * base + random.nextInt(i - j + 1);
            blocked = WorldgenHandler.isBlocked(chunkGenerator, random, this, sx, sz);
        }

        return new ChunkPos(sx, sz);
    }

    @Override
    public boolean place(final IWorld worldIn, final ChunkGenerator<? extends GenerationSettings> generator,
            final Random rand, final BlockPos pos, final JigsawConfig config)
    {
        this.struct = config.struct;
        return super.place(worldIn, generator, rand, pos, config);
    }

    @Override
    public boolean hasStartAt(final ChunkGenerator<?> chunkGen, final Random rand, final int chunkPosX,
            final int chunkPosZ)
    {
        if (this.getStruct() == null) return false;

        final DimensionType dim = chunkGen.world.getDimension().getType();
        if (this.getStruct().isBlackisted(dim)) return false;

        // Check if spawn building and should build.
        if (this.getStruct().atSpawn && (PokecubeSerializer.getInstance().hasPlacedSpawnOrCenter() || !PokecubeCore
                .getConfig().doSpawnBuilding)) return false;

        final ChunkPos chunkpos = this.getStartPositionForPosition(chunkGen, rand, chunkPosX, chunkPosZ, 0, 0);

        if (chunkPosX == chunkpos.x && chunkPosZ == chunkpos.z)
        {
            final int i = chunkPosX >> 4;
            final int j = chunkPosZ >> 4;
            rand.setSeed(i ^ j << 4 ^ chunkGen.getSeed());
            final Biome biome = chunkGen.getBiomeProvider().getBiome(new BlockPos((chunkPosX << 4) + 9, 0,
                    (chunkPosZ << 4) + 9));
            JigSawConfig matched = this.getStruct();
            if (matched._matcher == null) return false;
            if (!matched._matcher.checkBiome(biome)) for (final JigSawConfig m : this.structs)
                if (m._matcher.checkBiome(biome))
                {
                    matched = m;
                    break;
                }
            if (chunkGen.hasStructure(biome, this))
            {
                final boolean valid = !MinecraftForge.EVENT_BUS.post(new PickLocation(chunkGen, rand, chunkPosX,
                        chunkPosZ, matched));
                return valid;
            }
        }
        return false;
    }

    @Override
    protected int getBiomeFeatureDistance(final ChunkGenerator<?> chunkGenerator)
    {
        return this.getStruct() != null ? this.getStruct().distance : 100;
    }

    @Override
    protected int getBiomeFeatureSeparation(final ChunkGenerator<?> chunkGenerator)
    {
        return this.getStruct() != null ? this.getStruct().separation : 100;
    }

    @Override
    public Structure.IStartFactory getStartFactory()
    {
        return JigsawStructure.Start::new;
    }

    @Override
    protected int getSeedModifier()
    {
        return this.modifier;
    }

    public static class Start extends MarginedStructureStart
    {
        public Start(final Structure<?> struct, final int x, final int z, final Biome biome,
                final MutableBoundingBox box, final int ref, final long seed)
        {
            super(struct, x, z, biome, box, ref, seed);
        }

        @Override
        public void init(final ChunkGenerator<?> generator, final TemplateManager templateManagerIn, final int chunkX,
                final int chunkZ, final Biome biome)
        {
            if (this.getStructure() instanceof JigsawStructure)
            {
                final BlockPos blockpos = new BlockPos(chunkX * 16, 90, chunkZ * 16);
                JigSawConfig matched = ((JigsawStructure) this.getStructure()).getStruct();

                if (matched._matcher == null) return;
                if (!matched._matcher.checkBiome(biome)) for (final JigSawConfig m : ((JigsawStructure) this
                        .getStructure()).structs)
                    if (m._matcher.checkBiome(biome))
                    {
                        matched = m;
                        break;
                    }
                JigsawPieces.initStructure(generator, templateManagerIn, blockpos, this.components, this.rand, matched,
                        biome);
                PokecubeCore.LOGGER.debug("Placing structure {} at {} {} {} composed of {} parts ", matched.name,
                        blockpos.getX(), blockpos.getY(), blockpos.getZ(), this.components.size());
                // Check if any components are valid spawn spots, if so, set the
                // spawned flag
                if (!PokecubeSerializer.getInstance().hasPlacedProf()) components:
                {
                    for (final StructurePiece part : this.components)
                        if (part instanceof CustomJigsawPiece)
                        {
                            final CustomJigsawPiece p = (CustomJigsawPiece) part;
                            if (p.getJigsawPiece() instanceof SingleOffsetPiece)
                            {
                                final SingleOffsetPiece piece = (SingleOffsetPiece) p.getJigsawPiece();
                                final Template t = piece.getTemplate(templateManagerIn);
                                for (final List<BlockInfo> list : t.blocks)
                                {
                                    boolean foundWorldspawn = false;
                                    String tradeString = "";
                                    for (final BlockInfo i : list)
                                        if (i != null && i.nbt != null && i.state.getBlock() == Blocks.STRUCTURE_BLOCK)
                                        {
                                            final StructureMode structuremode = StructureMode.valueOf(i.nbt.getString(
                                                    "mode"));
                                            if (structuremode == StructureMode.DATA)
                                            {
                                                final String meta = i.nbt.getString("metadata");
                                                foundWorldspawn = foundWorldspawn || meta.startsWith(
                                                        "pokecube:worldspawn");
                                                if (meta.startsWith("pokecube:mob:trader")) tradeString = meta;
                                            }
                                        }
                                    if (!tradeString.isEmpty() && foundWorldspawn)
                                    {
                                        piece.isSpawn = true;
                                        piece.spawnReplace = tradeString;
                                        piece.mask = new MutableBoundingBox(part.getBoundingBox());
                                        break components;
                                    }
                                }
                            }
                        }
                }
                this.recalculateStructureSize();
            }
        }
    }
}