package pokecube.api.data.effects.actions;

import net.minecraft.world.entity.LivingEntity;

public interface IEffectAction
{
    void applyEffect(LivingEntity mob);

    default void init()
    {}
}
