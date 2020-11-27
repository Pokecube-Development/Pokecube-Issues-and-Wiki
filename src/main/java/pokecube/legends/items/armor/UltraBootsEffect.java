package pokecube.legends.items.armor;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class UltraBootsEffect extends ArmorItem
{
	public UltraBootsEffect(final IArmorMaterial materialIn, final EquipmentSlotType slot, final Properties builder) {
		super(materialIn, slot, builder);
	}

	@Override
	public void onArmorTick(final ItemStack itemstack, final World world, final PlayerEntity entity) {
		super.onArmorTick(itemstack, world, entity);
		{
			final java.util.HashMap<String, Object> $_dependencies = new java.util.HashMap<>();
			$_dependencies.put("entity", entity);
			UltraBootsEffect.executeProcedure($_dependencies);
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
//				((LivingEntity) entity).addPotionEffect(new EffectInstance(Effects.JUMP_BOOST, 120, 1));
//			}
//		}
	}
}
