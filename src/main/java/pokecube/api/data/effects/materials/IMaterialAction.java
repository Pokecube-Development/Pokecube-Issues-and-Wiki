package pokecube.api.data.effects.materials;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.data.effects.actions.IEffectAction;

public interface IMaterialAction extends IEffectAction
{
    default void mergeFrom(IMaterialAction other)
    {}

    default void init(LivingEntity mob)
    {}

    boolean shouldApply(LivingEntity mob);
}
