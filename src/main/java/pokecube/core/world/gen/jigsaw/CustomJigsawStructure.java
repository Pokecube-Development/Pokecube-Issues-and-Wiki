package pokecube.core.world.gen.jigsaw;

import com.mojang.serialization.Codec;

import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.structure.JigsawStructure;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.structure.VillageConfig;
import net.minecraft.world.gen.settings.StructureSeparationSettings;

public class CustomJigsawStructure extends JigsawStructure//Structure<JigsawConfig>
{
    public CustomJigsawStructure(final Codec<VillageConfig> p_i231977_1_) {
        super(p_i231977_1_, 0, true, true);
     }

    @Override
    protected boolean func_230363_a_(final ChunkGenerator p_230363_1_, final BiomeProvider p_230363_2_, final long p_230363_3_,
            final SharedSeedRandom p_230363_5_, final int p_230363_6_, final int p_230363_7_, final Biome p_230363_8_, final ChunkPos p_230363_9_,
            final VillageConfig p_230363_10_)
    {
        // TODO Auto-generated method stub
        return super.func_230363_a_(p_230363_1_, p_230363_2_, p_230363_3_, p_230363_5_, p_230363_6_, p_230363_7_, p_230363_8_,
                p_230363_9_, p_230363_10_);
    }

    @Override
    public BlockPos func_236388_a_(final IWorldReader world, final StructureManager manager, final BlockPos p_236388_3_, final int radius,
            final boolean skipExistingChunks, final long seed, final StructureSeparationSettings separationSettings)
    {
        // TODO Auto-generated method stub
        return super.func_236388_a_(world, manager, p_236388_3_, radius, skipExistingChunks, seed, separationSettings);
    }


//    private final Decoration stage = Decoration.SURFACE_STRUCTURES;
//
//    public CustomJigsawStructure(final Codec<JigsawConfig> codec, final Decoration stage)
//    {
//        super(codec);
//        this.stage = stage;
//    }
//
//    @Override
//    public Decoration getDecorationStage()
//    {
//        return this.stage;
//    }
//
//    @Override
//    public IStartFactory<JigsawConfig> getStartFactory()
//    {
//        return (struct, x, z, box, refs, seed) ->
//        {
//            return new CustomJigsawStructure.Start(this, x, z, box, refs, seed);
//        };
//    }
//
//    public static void initStructure(final ChunkGenerator chunk_gen, final TemplateManager templateManagerIn,
//            final BlockPos pos, final List<StructurePiece> parts, final SharedSeedRandom rand,
//            final JigSawConfig struct, final Biome biome)
//    {
//        Structure.init();
//        final ResourceLocation key = new ResourceLocation(struct.root);
//        final JigsawAssmbler assembler = new JigsawAssmbler();
//        boolean built = assembler.build(key, struct.size, CustomVillagePiece::new, chunk_gen, templateManagerIn, pos,
//                parts, rand, biome);
//        int n = 0;
//        while (!built && n++ < 20)
//        {
//            parts.clear();
//            built = assembler.build(key, struct.size, CustomVillagePiece::new, chunk_gen, templateManagerIn, pos, parts,
//                    rand, biome);
//        }
//        if (!built) PokecubeCore.LOGGER.warn("Failed to complete a structure at " + pos);
//    }
//
//    public static class Start extends MarginedStructureStart<JigsawConfig>
//    {
//        public Start(final CustomJigsawStructure struct, final int x, final int z, final MutableBoundingBox box,
//                final int refs, final long seed)
//        {
//            super(struct, x, z, box, refs, seed);
//        }
//
//        @Override
//        public void func_230364_a_(final DynamicRegistries registry, final ChunkGenerator chunkGen,
//                final TemplateManager templateManager, final int chunkX, final int chunkY, final Biome biome,
//                final JigsawConfig config)
//        {
//            // This "0" used to be related to depth of structure, it is 33 for
//            // the nether things, 0 for overworld
//            final BlockPos blockpos = new BlockPos(chunkX * 16, 0, chunkY * 16);
//            JigsawPatternRegistry.func_244093_a();
//            CustomJigsawStructure.initStructure(chunkGen, templateManager, blockpos, this.getComponents(), this.rand,
//                    config.struct_config, biome);
//            this.recalculateStructureSize();
//        }
//    }

}
