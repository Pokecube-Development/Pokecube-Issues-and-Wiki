package pokecube.legends.items.armor;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class UltraHelmetEffect extends ArmorItem
{
	public UltraHelmetEffect(final ArmorMaterial materialIn, final EquipmentSlot slot, final Properties builder) {
		super(materialIn, slot, builder);
	}

	@Override
	public void onArmorTick(final ItemStack itemstack, final Level world, final Player entity) {
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
//		Entity entity = (Entity) dependencies.get("entity");
//		if ((entity instanceof ServerPlayerEntity)) {
//			if (entity.dimension.getId() == ModDimensions.DIMENSION_TYPE_US.getId()) {
//				((LivingEntity) entity).addPotionEffect(new EffectInstance(Effects.NIGHT_VISION, 210, 1));
//			}
//		}
	}
}
