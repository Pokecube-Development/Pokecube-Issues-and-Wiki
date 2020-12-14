package pokecube.pokeplayer;

import net.minecraft.entity.LivingEntity;

public interface IMobHolder
{
    LivingEntity getTransformedTo();

    void setTransformedTo(LivingEntity to);
}
