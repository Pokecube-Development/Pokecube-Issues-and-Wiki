package pokecube.legends.worldgen.structuregen;

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import pokecube.legends.Reference;
import pokecube.legends.handlers.events.IStructure;

public class WorldGenStructure extends WorldGenerator implements IStructure
{
	public static String structureName;
	
	@SuppressWarnings("static-access")
	public WorldGenStructure(String name) 
	{
		this.structureName = name;
	}
	
	@SuppressWarnings("static-access")
	@Override
	public boolean generate(World worldIn, Random rand, BlockPos position) 
	{
		this.generateStructure(worldIn, position, rand);
		return true;
	}
	
	public static void generateStructure(World world, BlockPos pos, Random rand)
	{
		MinecraftServer mcServer = world.getMinecraftServer();
		TemplateManager manager = worldServer.getStructureTemplateManager();
		ResourceLocation location = new ResourceLocation(Reference.ID, structureName);
		Template template = manager.get(mcServer, location);
		
		if(template != null)
		{
			IBlockState state = world.getBlockState(pos);
			world.notifyBlockUpdate(pos, state, state, 3);
			int r = rand.nextInt(40);
			if(r <= 10){
			    template.addBlocksToWorldChunk(world, pos, settings);
			}else if(r <= 20){
			    template.addBlocksToWorldChunk(world, pos, settings1);
			}else if(r <= 30){
			    template.addBlocksToWorldChunk(world, pos, settings2);
			}else{
			    template.addBlocksToWorldChunk(world, pos, settings3);
			}
		}
	}
}