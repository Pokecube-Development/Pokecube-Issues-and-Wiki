package pokecube.legends.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraftforge.common.ToolType;
import pokecube.legends.init.DimensionInit;

public class GrassMussBlock extends BlockBase
{
    public GrassMussBlock(final String name, final Material material)
    {
        super(name, Properties.create(material).sound(SoundType.PLANT).hardnessAndResistance(1, 2).harvestTool(
                ToolType.SHOVEL).harvestLevel(1));
    }
    
    public static void executeProcedure(java.util.HashMap<String, Object> dependencies) {
		if (dependencies.get("entity") == null) {
			System.err.println("Failed to load dependency entity for procedure WalkGrassEffect!");
			return;
		}
		Entity entity = (Entity) dependencies.get("entity");
		if ((entity instanceof PlayerEntity)) {
			if (((entity.dimension.getId()) == DimensionInit.type.getId())) {
				if (entity instanceof LivingEntity)
					((LivingEntity) entity).addPotionEffect(new EffectInstance(Effects.SLOWNESS, (int) 60, (int) 1));
			}
		}
	}
}
