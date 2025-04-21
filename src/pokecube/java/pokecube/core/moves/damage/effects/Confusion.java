package pokecube.core.moves.damage.effects;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import pokecube.core.ai.brain.BrainUtils;

public class Confusion extends StatusEffect
{
    public Confusion(int color)
    {
        super(MobEffectCategory.HARMFUL, color, StatusEffects.CONFUSE);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier)
    {
        if (!super.applyEffectTick(entity, amplifier)) return false;
        if (entity.getRandom().nextFloat() > 0.25) return true;
        BrainUtils.setAttackTarget(entity, entity);
        return true;
    }
}
