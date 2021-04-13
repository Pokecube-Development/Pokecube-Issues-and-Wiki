package pokecube.legends.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import pokecube.legends.blocks.BlockBase;
import pokecube.legends.init.ItemInit;

public class EffectBlockBase extends BlockBase
{		
	private final Effect effect;
	
	public EffectBlockBase(String name, Material material, MaterialColor color, float hardness, float resistance,
			SoundType sound, ToolType tool, int harvestLevel, boolean hadDrop, Effect effects) 
	{
		super(name, material, color, hardness, resistance, sound, tool, harvestLevel, hadDrop, effects);
		effect = effects;
	}
	
	public EffectBlockBase(Material material, MaterialColor color, float hardness, float resistance,
			SoundType sound, ToolType tool, int harvestLevel, boolean hadDrop, Effect effects) 
	{
		super(material, color, hardness, resistance, sound, tool, harvestLevel, hadDrop, effects);
		effect = effects;
	}

    @Override
    public void stepOn(final World world, final BlockPos pos, final Entity entity)
    {
        super.stepOn(world, pos, entity);
        {
            applyEffects(entity, effect);
        }
    }

    public static void applyEffects(Entity entity, Effect effects)
    {
        if (entity instanceof ServerPlayerEntity) {
        	if ((((PlayerEntity) entity).inventory.armor.get(3).getItem() != new ItemStack(ItemInit.ULTRA_HELMET.get(), 1).getItem()) ||
                (((PlayerEntity) entity).inventory.armor.get(2).getItem() != new ItemStack(ItemInit.ULTRA_CHESTPLATE.get(), 1).getItem()) ||
                (((PlayerEntity) entity).inventory.armor.get(1).getItem() != new ItemStack(ItemInit.ULTRA_LEGGINGS.get(), 1).getItem()) || 
                (((PlayerEntity) entity).inventory.armor.get(0).getItem() != new ItemStack(ItemInit.ULTRA_BOOTS.get(), 1).getItem())) 
            {
            	((LivingEntity) entity).addEffect(new EffectInstance(effects, 120, 1));
           }
        }
    }
}
