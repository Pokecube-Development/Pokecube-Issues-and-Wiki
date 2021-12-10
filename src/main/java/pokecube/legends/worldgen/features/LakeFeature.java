//package pokecube.legends.worldgen.features;
//
//import java.util.Random;
//
//import com.mojang.serialization.Codec;
//
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.SectionPos;
//import net.minecraft.tags.BlockTags;
//import net.minecraft.world.level.LightLayer;
//import net.minecraft.world.level.WorldGenLevel;
//import net.minecraft.world.level.biome.Biome;
//import net.minecraft.world.level.block.Blocks;
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraft.world.level.levelgen.feature.Feature;
//import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
//import net.minecraft.world.level.levelgen.feature.StructureFeature;
//import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
//import net.minecraft.world.level.material.Material;
//import pokecube.legends.init.BlockInit;
//
//public class LakeFeature extends Feature<BlockStateConfiguration>
//{
//   private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();
//
//   public LakeFeature(final Codec<BlockStateConfiguration> config)
//   {
//      super(config);
//   }
//
//   @Override
//   public boolean place(final FeaturePlaceContext<BlockStateConfiguration> context)
//   {
//      BlockPos pos = context.origin();
//      final WorldGenLevel world = context.level();
//      final Random random = context.random();
//
//      BlockStateConfiguration stateConfig;
//      for(stateConfig = context.config(); pos.getY() > world.getMinBuildHeight() + 5 && world.isEmptyBlock(pos); pos = pos.below())
//      {
//      }
//
//      if (pos.getY() <= world.getMinBuildHeight() + 4) return false;
//    else
//      {
//         pos = pos.below(4);
//         for (final StructureFeature<?> village : StructureFeature.NOISE_AFFECTING_FEATURES)
//            if (!world.startsForFeature(SectionPos.of(pos), village).isEmpty()) return false;
//         if (!world.startsForFeature(SectionPos.of(pos), StructureFeature.VILLAGE).isEmpty()) return false;
//        else
//         {
//            final boolean[] aboolean = new boolean[2048];
//            final int i = random.nextInt(4) + 4;
//
//            for(int j = 0; j < i; ++j)
//            {
//               final double d0 = random.nextDouble() * 6.0D + 3.0D;
//               final double d1 = random.nextDouble() * 4.0D + 2.0D;
//               final double d2 = random.nextDouble() * 6.0D + 3.0D;
//               final double d3 = random.nextDouble() * (16.0D - d0 - 2.0D) + 1.0D + d0 / 2.0D;
//               final double d4 = random.nextDouble() * (8.0D - d1 - 4.0D) + 2.0D + d1 / 2.0D;
//               final double d5 = random.nextDouble() * (16.0D - d2 - 2.0D) + 1.0D + d2 / 2.0D;
//
//               for(int l = 1; l < 15; ++l)
//                for(int i1 = 1; i1 < 15; ++i1)
//                    for(int j1 = 1; j1 < 7; ++j1)
//                     {
//                        final double d6 = (l - d3) / (d0 / 2.0D);
//                        final double d7 = (j1 - d4) / (d1 / 2.0D);
//                        final double d8 = (i1 - d5) / (d2 / 2.0D);
//                        final double d9 = d6 * d6 + d7 * d7 + d8 * d8;
//                        if (d9 < 1.0D) aboolean[(l * 16 + i1) * 8 + j1] = true;
//                     }
//            }
//
//            for(int k1 = 0; k1 < 16; ++k1)
//                for(int k2 = 0; k2 < 16; ++k2)
//                    for(int k = 0; k < 8; ++k)
//                      {
//                         final boolean flag = !aboolean[(k1 * 16 + k2) * 8 + k] && (k1 < 15 && aboolean[((k1 + 1) * 16 + k2) * 8 + k] ||
//                                 k1 > 0 && aboolean[((k1 - 1) * 16 + k2) * 8 + k] || k2 < 15 && aboolean[(k1 * 16 + k2 + 1) * 8 + k] ||
//                                 k2 > 0 && aboolean[(k1 * 16 + (k2 - 1)) * 8 + k] || k < 7 && aboolean[(k1 * 16 + k2) * 8 + k + 1] ||
//                                 k > 0 && aboolean[(k1 * 16 + k2) * 8 + (k - 1)]);
//                         if (flag)
//                         {
//                            final Material material = world.getBlockState(pos.offset(k1, k, k2)).getMaterial();
//                            if (k >= 4 && material.isLiquid()) return false;
//
//                            if (k < 4 && !material.isSolid() && world.getBlockState(pos.offset(k1, k, k2)) != stateConfig.state) return false;
//                         }
//                      }
//
//            for(int l1 = 0; l1 < 16; ++l1)
//                for(int l2 = 0; l2 < 16; ++l2)
//                    for(int l3 = 0; l3 < 8; ++l3)
//                        if (aboolean[(l1 * 16 + l2) * 8 + l3])
//                         {
//                            final BlockPos pos1 = pos.offset(l1, l3, l2);
//                            final boolean flag1 = l3 >= 4;
//                            world.setBlock(pos1, flag1 ? LakeFeature.AIR : stateConfig.state, 2);
//                            if (flag1)
//                            {
//                               world.scheduleTick(pos1, LakeFeature.AIR.getBlock(), 0);
//                               this.markAboveForPostProcessing(world, pos1);
//                            }
//                         }
//
//            for(int i2 = 0; i2 < 16; ++i2)
//                for(int i3 = 0; i3 < 16; ++i3)
//                    for(int i4 = 4; i4 < 8; ++i4)
//                        if (aboolean[(i2 * 16 + i3) * 8 + i4])
//                         {
//                            final BlockPos pos2 = pos.offset(i2, i4 - 1, i3);
//                            if (Feature.isDirt(world.getBlockState(pos2)) && world.getBrightness(LightLayer.SKY, pos.offset(i2, i4, i3)) > 0)
//                            {
//                               final Biome biome = world.getBiome(pos2);
//                               if (biome.getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial().is(Blocks.MYCELIUM)) world.setBlock(pos2, Blocks.MYCELIUM.defaultBlockState(), 2);
//                            else if (biome.getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial().is(BlockInit.AGED_GRASS.get())) world.setBlock(pos2, BlockInit.AGED_GRASS.get().defaultBlockState(), 2);
//                            else if (biome.getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial().is(BlockInit.CORRUPTED_GRASS.get())) world.setBlock(pos2, BlockInit.CORRUPTED_GRASS.get().defaultBlockState(), 2);
//                            else if (biome.getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial().is(BlockInit.CRYSTALLIZED_SAND.get())) world.setBlock(pos2, BlockInit.CRYSTALLIZED_SAND.get().defaultBlockState(), 2);
//                            else if (biome.getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial().is(BlockInit.DISTORTIC_GRASS.get())) world.setBlock(pos2, BlockInit.DISTORTIC_GRASS.get().defaultBlockState(), 2);
//                            else if (biome.getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial().is(BlockInit.JUNGLE_GRASS.get())) world.setBlock(pos2, BlockInit.JUNGLE_GRASS.get().defaultBlockState(), 2);
//                            else if (biome.getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial().is(BlockInit.MUSHROOM_GRASS.get())) world.setBlock(pos2, BlockInit.MUSHROOM_GRASS.get().defaultBlockState(), 2);
//                            else world.setBlock(pos2, BlockInit.ULTRA_STONE.get().defaultBlockState(), 2);
//                            }
//                         }
//
//            if (stateConfig.state.getMaterial() == Material.LAVA)
//            {
//               final BaseStoneSource stoneSource = context.chunkGenerator().getBaseStoneSource();
//
//               for(int j3 = 0; j3 < 16; ++j3)
//                for(int j4 = 0; j4 < 16; ++j4)
//                    for(int l4 = 0; l4 < 8; ++l4)
//                     {
//                        final boolean flag2 = !aboolean[(j3 * 16 + j4) * 8 + l4] && (j3 < 15 && aboolean[((j3 + 1) * 16 + j4) * 8 + l4] ||
//                                j3 > 0 && aboolean[((j3 - 1) * 16 + j4) * 8 + l4] || j4 < 15 && aboolean[(j3 * 16 + j4 + 1) * 8 + l4] ||
//                                j4 > 0 && aboolean[(j3 * 16 + (j4 - 1)) * 8 + l4] || l4 < 7 && aboolean[(j3 * 16 + j4) * 8 + l4 + 1] ||
//                                l4 > 0 && aboolean[(j3 * 16 + j4) * 8 + (l4 - 1)]);
//                        if (flag2 && (l4 < 4 || random.nextInt(2) != 0))
//                        {
//                           final BlockState blockstate = world.getBlockState(pos.offset(j3, l4, j4));
//                           if (blockstate.getMaterial().isSolid() && !blockstate.is(BlockTags.LAVA_POOL_STONE_CANNOT_REPLACE))
//                           {
//                              final BlockPos pos3 = pos.offset(j3, l4, j4);
//                              world.setBlock(pos3, stoneSource.getBaseBlock(pos3), 2);
//                              this.markAboveForPostProcessing(world, pos3);
//                           }
//                        }
//                     }
//            }
//
//            if (stateConfig.state.getMaterial() == Material.WATER) for(int j2 = 0; j2 < 16; ++j2)
//                for(int k3 = 0; k3 < 16; ++k3)
//                  {
//                     final int k4 = 4;
//                     final BlockPos pos4 = pos.offset(j2, k4, k3);
//                     if (world.getBiome(pos4).shouldFreeze(world, pos4, false)) world.setBlock(pos4, Blocks.ICE.defaultBlockState(), 2);
//                  }
//            return true;
//         }
//      }
//   }
//}