package pokecube.api.entity;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import pokecube.core.network.pokemobs.PacketSyncModifier;

public class SharedAttributes
{
    public static final Holder<Attribute> MOB_SIZE_SCALE = Attributes.SCALE;

    public static double getScale(LivingEntity mob)
    {
        // This can be null if size is checked during constructor.
        if (mob.getAttributes() == null) return 1;
        if (!mob.getAttributes().hasAttribute(MOB_SIZE_SCALE)) return 1;
        return mob.getAttributeValue(MOB_SIZE_SCALE);
    }

    public static void clearScale(LivingEntity mob, ResourceLocation key)
    {
        if (mob.getAttributes() == null) return;
        if (!mob.getAttributes().hasAttribute(MOB_SIZE_SCALE)) return;
        mob.getAttributes().getInstance(MOB_SIZE_SCALE).removeModifier(key);
    }

    public static void adjustScale(LivingEntity mob, double factor, ResourceLocation key, boolean permanent)
    {
        if (mob.getAttributes() == null) return;
        if (!mob.getAttributes().hasAttribute(MOB_SIZE_SCALE)) return;
        double before = getScale(mob);
        if (permanent) mob.getAttributes().getInstance(MOB_SIZE_SCALE).addOrReplacePermanentModifier(
                new AttributeModifier(key, factor, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        else mob.getAttributes().getInstance(MOB_SIZE_SCALE).addOrUpdateTransientModifier(
                new AttributeModifier(key, factor, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        if (getScale(mob) != before)
        {
            mob.refreshDimensions();
            PacketSyncModifier.sendUpdate(mob);
        }
    }
}