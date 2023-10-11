package pokecube.api.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.registries.RegistryObject;
import thut.core.init.RegistryObjects;

public class SharedAttributes
{
    public static final RegistryObject<Attribute> MOB_SIZE_SCALE = RegistryObjects.MOB_SIZE_SCALE;

    public static double getScale(LivingEntity mob)
    {
        if (!mob.getAttributes().hasAttribute(MOB_SIZE_SCALE.get())) return 1;
        return mob.getAttributeValue(MOB_SIZE_SCALE.get());
    }
}