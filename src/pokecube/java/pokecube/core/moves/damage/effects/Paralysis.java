package pokecube.core.moves.damage.effects;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class Paralysis extends StatusEffect
{
    public static ResourceLocation MODIFIER = ResourceLocation.parse("pokecube:effect.freeze");

    public Paralysis(int color)
    {
        super(MobEffectCategory.HARMFUL, color, StatusEffects.PARALYSIS);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, MODIFIER, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                NEGATIVE_PTFIVE);
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, MODIFIER, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                NEGATIVE_PTFIVE);
        this.addAttributeModifier(Attributes.ATTACK_SPEED, MODIFIER, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                NEGATIVE_PTFIVE);
    }
}
