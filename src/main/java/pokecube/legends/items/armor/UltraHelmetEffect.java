package pokecube.legends.items.armor;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;
import pokecube.legends.worldgen.dimension.ModDimensions;

public class UltraHelmetEffect extends ArmorItem
{
	public UltraHelmetEffect(final IArmorMaterial materialIn, final EquipmentSlotType slot, final Properties builder) {
		super(materialIn, slot, builder);
	}

	@Override
	public void onArmorTick(final ItemStack itemstack, final World world, final PlayerEntity entity) {
		super.onArmorTick(itemstack, world, entity);
		{
			final java.util.HashMap<String, Object> $_dependencies = new java.util.HashMap<>();
			$_dependencies.put("entity", entity);
			UltraHelmetEffect.executeProcedure($_dependencies);
		}
	}

	public static void executeProcedure(final java.util.HashMap<String, Object> dependencies) {
		if (dependencies.get("entity") == null) {
			System.err.println("Failed Effect Helmet!");
			return;
		}
		final Entity entity = (Entity) dependencies.get("entity");
		if (entity instanceof ServerPlayerEntity) if (entity.getEntityWorld().getDimensionKey() == ModDimensions.DIMENSION_TYPE) ((LivingEntity) entity).addPotionEffect(new EffectInstance(Effects.NIGHT_VISION, 210, 1));
	}
}
