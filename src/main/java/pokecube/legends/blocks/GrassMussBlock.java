package pokecube.legends.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.ToolType;
import pokecube.legends.worldgen.dimension.ModDimensions;

public class GrassMussBlock extends BlockBase
{
    public GrassMussBlock(final String name, final Material material)
    {
        super(name, Properties.create(material).sound(SoundType.PLANT).hardnessAndResistance(1, 2).harvestTool(
                ToolType.SHOVEL).harvestLevel(1));
    }
    
    @Override
   	public boolean canSustainPlant(BlockState state, IBlockReader world, BlockPos pos, Direction direction, IPlantable plantable) {
   		return true;
   	}
    
    @SuppressWarnings("unused")
	@Override
	public void onEntityWalk(World world, BlockPos pos, Entity entity) {
		super.onEntityWalk(world, pos, entity);
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		Block block = this;
		{
			java.util.HashMap<String, Object> $_dependencies = new java.util.HashMap<>();
			$_dependencies.put("entity", entity);
			executeProcedure($_dependencies);
		}
	}

    public static void executeProcedure(java.util.HashMap<String, Object> dependencies) {
		if (dependencies.get("entity") == null) {
			System.err.println("Failed to WalkGrassEffect!");
			return;
		}
		Entity entity = (Entity) dependencies.get("entity");
		if ((entity instanceof PlayerEntity)) {
			if (((entity.dimension.getId()) == ModDimensions.DIMENSION_TYPE.getId())) {
				if (entity instanceof LivingEntity)
					((LivingEntity) entity).addPotionEffect(new EffectInstance(Effects.SLOWNESS, (int) 60, (int) 1));
			}
		}
	}
}
