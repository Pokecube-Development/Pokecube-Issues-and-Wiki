package pokecube.legends.worldgen.dimension;

import net.minecraft.world.IWorld;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.EndGenerationSettings;
import net.minecraft.world.gen.NoiseChunkGenerator;

public class DistorticWorldChunkGenerator extends NoiseChunkGenerator<EndGenerationSettings> {

   public DistorticWorldChunkGenerator(IWorld worldIn, BiomeProvider biomeProviderIn, EndGenerationSettings settingsIn) {
      super(worldIn, biomeProviderIn, 8, 4, 108, settingsIn, true);
   }

   protected void fillNoiseColumn(double[] noiseColumn, int noiseX, int noiseZ) {
      this.calcNoiseColumn(noiseColumn, noiseX, noiseZ, 1368.824D, 884.412D, 15D, 5D, 64, -3000);
   }

   protected double[] getBiomeNoiseColumn(int noiseX, int noiseZ) {
      return new double[]{(double)this.biomeProvider.func_222365_c(noiseX, noiseZ), 0.0D};
   }

   protected double func_222545_a(double p_222545_1_, double p_222545_3_, int p_222545_5_) {
      return 8.0D - p_222545_1_;
   }

   protected double func_222551_g() {
      return (double)((int)super.func_222551_g() / 2);
   }

   protected double func_222553_h() {
      return 8.0D;
   }

   public int getGroundHeight() {
      return 30;
   }

   public int getSeaLevel() {
      return 0;
   }
}