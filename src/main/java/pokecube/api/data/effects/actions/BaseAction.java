package pokecube.api.data.effects.actions;

import net.minecraft.world.entity.LivingEntity;

public interface BaseAction
{
    void applyEffect(LivingEntity mob);

    void init();
}
