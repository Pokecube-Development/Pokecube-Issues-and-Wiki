package pokecube.legends.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import pokecube.legends.worldgen.dimension.ModDimensions;

public class StoneMagneticBlock extends BlockBase
{
    public StoneMagneticBlock(final String name, final Material material)
    {
        super(name, Properties.create(material).sound(SoundType.STONE).hardnessAndResistance(3, 8).harvestTool(
                ToolType.PICKAXE).harvestLevel(1));
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
			System.err.println("Failed to WalkEffect!");
			return;
		}
		Entity entity = (Entity) dependencies.get("entity");
		if ((entity instanceof PlayerEntity)) {
			if (((entity.dimension.getId()) == ModDimensions.DIMENSION_TYPE.getId())) {
				if (entity instanceof LivingEntity)
					((LivingEntity) entity).addPotionEffect(new EffectInstance(Effects.BLINDNESS, (int) 60, (int) 1));
			}
		}
	}
}
