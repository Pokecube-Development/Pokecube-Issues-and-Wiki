package pokecube.api.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.neoforge.registries.DeferredHolder;
import thut.core.init.RegistryObjects;

public class SharedAttributes
{
    public static final DeferredHolder<Attribute, Attribute> MOB_SIZE_SCALE = RegistryObjects.MOB_SIZE_SCALE;

    public static double getScale(LivingEntity mob)
    {
        // This can be null if size is checked during constructor.
        if (mob.getAttributes() == null) return 1;
        if (!mob.getAttributes().hasAttribute(MOB_SIZE_SCALE)) return 1;
        return mob.getAttributeValue(MOB_SIZE_SCALE);
    }
}