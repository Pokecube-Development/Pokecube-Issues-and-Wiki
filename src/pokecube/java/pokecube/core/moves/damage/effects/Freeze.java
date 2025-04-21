package pokecube.core.moves.damage.effects;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class Freeze extends StatusEffect
{
    public static ResourceLocation MODIFIER = ResourceLocation.parse("pokecube:effect.freeze");

    public Freeze(int color)
    {
        super(MobEffectCategory.HARMFUL, color, StatusEffects.FREEZE);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, MODIFIER, -1,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }
}
