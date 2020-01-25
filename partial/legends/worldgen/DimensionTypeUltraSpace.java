package pokecube.legends.worldgen;

//import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.legends.init.DimensionInit;
import pokecube.legends.worldgen.dimension.OverChunkProvider;
import pokecube.legends.worldgen.gencustom.BiomeProviderCustom;
//import net.minecraftforge.client.EnumHelperClient;

public class DimensionTypeUltraSpace extends WorldProvider{

	public static final boolean NETHER_TYPE = false;
	
	@Override
	protected void init() {
		this.biomeProvider = new BiomeProviderCustom(this.world.getSeed());
		this.nether = NETHER_TYPE;
		this.hasSkyLight = true;
	}
	
	@Override
	public DimensionType getDimensionType() {
		return DimensionInit.ULTRASPACE;
	}
	
	@Override
	public IChunkGenerator createChunkGenerator() {
		//return new IslandChunkProvider(this.world, true, this.world.getSeed(), this.world.getSpawnPoint());
		return new OverChunkProvider(this.world, this.world.getSeed());
	}
	
	@Override
	public boolean isSurfaceWorld() {
		return true;
	}
	
	@Override
	public boolean canRespawnHere() {
		return true;
	}
	
	/*@Override
	@OnlyIn(Dist.CLIENT)
	public net.minecraft.client.audio.MusicTicker.MusicType getMusicType() {
		return EnumHelperClient.addMusicType("pokecube_legends:legend.ultraspace.music",
				(net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation(
						("pokecube_legends:legend.ultraspace.music"))), 6000, 24000);
	}*/
	
	@OnlyIn(Dist.CLIENT)
	public Vec3d getFogColor(float part1,float part2) {
		return new Vec3d(0, 0, 0);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean doesXZShowFog(int par1, int par2) {
		return true;
	}
	
	@Override
	public float getCloudHeight() 
	{
		return 70.0f;
	}

}
