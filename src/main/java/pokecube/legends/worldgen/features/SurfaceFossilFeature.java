package pokecube.legends.worldgen.features;

import java.util.Random;

import org.apache.commons.lang3.mutable.MutableInt;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.BiomeCategory;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.FossilFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import pokecube.legends.blocks.normalblocks.AshLayerBlock;
import pokecube.legends.init.BlockInit;
import pokecube.legends.worldgen.WorldgenFeatures;

public class SurfaceFossilFeature extends Feature<FossilFeatureConfiguration>
{
   public SurfaceFossilFeature(Codec<FossilFeatureConfiguration> config)
   {
      super(config);
   }

   @Override
   public boolean place(FeaturePlaceContext<FossilFeatureConfiguration> context)
   {
      Random random = context.random();
      WorldGenLevel world = context.level();
      BlockPos posOrigin = context.origin();
      Rotation rotation = Rotation.getRandom(random);
      FossilFeatureConfiguration fossilConfig = context.config();
      int i = random.nextInt(fossilConfig.fossilStructures.size());
      StructureManager structureManager = world.getLevel().getServer().getStructureManager();
      StructureTemplate structureTemplate = structureManager.getOrCreate(fossilConfig.fossilStructures.get(i));
      StructureTemplate structureTemplate1 = structureManager.getOrCreate(fossilConfig.overlayStructures.get(i));
      ChunkPos posChunk = new ChunkPos(posOrigin);
      BoundingBox box = new BoundingBox(posChunk.getMinBlockX() - 16, world.getMinBuildHeight(), posChunk.getMinBlockZ() - 16, posChunk.getMaxBlockX() + 16, world.getMaxBuildHeight(), posChunk.getMaxBlockZ() + 16);
      StructurePlaceSettings structurePlacement = (new StructurePlaceSettings()).setRotation(rotation).setBoundingBox(box).setRandom(random);
      Vec3i vec3i = structureTemplate.getSize(rotation);
      BlockPos posVec = posOrigin.offset(-vec3i.getX() / 2, 0, -vec3i.getZ() / 2);
      int j = posOrigin.getY();

      for(int k = 0; k < vec3i.getX(); ++k)
      {
         for(int l = 0; l < vec3i.getZ(); ++l)
         {
            j = Math.min(j, world.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, posVec.getX() + k, posVec.getZ() + l));
         }
      }

      int i1 = Math.max(j + 1 - random.nextInt(10), world.getMinBuildHeight() + 10);
      BlockPos posStructure = structureTemplate.getZeroPositionWithTransform(posVec.atY(i1), Mirror.NONE, rotation);
      
      if (countEmptyCorners(world, structureTemplate.getBoundingBox(structurePlacement, posStructure)) > fossilConfig.maxEmptyCornersAllowed)
      {
         return false;
      } else
      {
         structurePlacement.clearProcessors();
         fossilConfig.fossilProcessors.get().list().forEach((fossil) ->
         {
            structurePlacement.addProcessor(fossil);
         });
         structureTemplate.placeInWorld(world, posStructure, posStructure, structurePlacement, random, 4);
         structurePlacement.clearProcessors();
         fossilConfig.overlayProcessors.get().list().forEach((fossil2) ->
         {
            structurePlacement.addProcessor(fossil2);
         });
         structureTemplate1.placeInWorld(world, posStructure, posStructure, structurePlacement, random, 4);
         return true;
      }
   }

   private static int countEmptyCorners(WorldGenLevel world, BoundingBox box)
   {
      MutableInt mutableint = new MutableInt(0);
      box.forAllCorners((corners) ->
      {
         BlockState cornerState = world.getBlockState(corners);
         if (cornerState.isAir() || cornerState.is(Blocks.LAVA) || cornerState.is(Blocks.WATER))
         {
            mutableint.add(1);
         }

      });
      return mutableint.getValue();
   }
}