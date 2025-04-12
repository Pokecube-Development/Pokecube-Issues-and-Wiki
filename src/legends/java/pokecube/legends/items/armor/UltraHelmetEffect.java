package pokecube.legends.items.armor;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class UltraHelmetEffect extends ArmorItem
{
    public UltraHelmetEffect(final Holder<ArmorMaterial> materialIn, final Type armorSlot, final Properties builder)
    {
        super(materialIn, armorSlot, builder);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected)
    {
        // TODO test if the slot is actually boots slot
        {
            final java.util.HashMap<String, Object> $_dependencies = new java.util.HashMap<>();
            $_dependencies.put("entity", entity);
            UltraHelmetEffect.executeProcedure($_dependencies);
        }
    }

    public static void executeProcedure(final java.util.HashMap<String, Object> dependencies)
    {
        if (dependencies.get("entity") == null)
        {
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
