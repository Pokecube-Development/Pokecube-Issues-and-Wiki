package pokecube.core.moves.damage.effects;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class Flinch extends StatusEffect
{
    public static ResourceLocation MODIFIER = ResourceLocation.parse("pokecube:effect.flinch");

    public Flinch(int color)
    {
        super(MobEffectCategory.HARMFUL, color, StatusEffects.FLINCH);
        this.addAttributeModifier(Attributes.ATTACK_SPEED, MODIFIER, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                NEGATIVE_ONE);
    }
}
