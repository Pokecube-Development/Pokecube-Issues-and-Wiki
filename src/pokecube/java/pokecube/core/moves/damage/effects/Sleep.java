package pokecube.core.moves.damage.effects;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class Sleep extends StatusEffect
{
    public static int NATURAL_SLEEP = 128;

    public static ResourceLocation MODIFIER = ResourceLocation.parse("pokecube:effect.sleep");

    public Sleep(int color)
    {
        super(MobEffectCategory.HARMFUL, color, StatusEffects.SLEEP);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, MODIFIER, -1,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    public void onMobHurt(LivingEntity livingEntity, int amplifier, DamageSource damageSource, float amount)
    {
        if (amplifier == NATURAL_SLEEP) livingEntity.removeEffect(StatusEffects.SLEEP);
        super.onMobHurt(livingEntity, amplifier, damageSource, amount);
    }
}
