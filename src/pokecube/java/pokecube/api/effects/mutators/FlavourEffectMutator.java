package pokecube.api.effects.mutators;

import net.minecraft.resources.ResourceLocation;
import pokecube.api.effects.EffectPacketInfo;
import pokecube.api.effects.context.EffectContext;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.MoveEntry;
import pokecube.core.effects.MoveAnimationBase;

public class FlavourEffectMutator implements EffectMutator
{
    public static final int[] FLAVCOLOURS = new int[] { 0xFFFF4932, 0xFF4475ED, 0xFFF95B86, 0xFF2EBC63, 0xFFEBCE36 };

    @Override
    public void mutate(EffectPacketInfo info)
    {
        IPokemob pokemob = info.getContext(EffectContext.POKEMOB);
        if (info.animation.effect() instanceof MoveAnimationBase base && pokemob != null)
        {
            int i = pokemob.getEntity().tickCount % 5;
            var flav = pokemob.getFlavourAmount(i);
            if (flav > 0)
            {
                base.values.rgba = FLAVCOLOURS[flav];
                base.values.density = 0.1f / flav;
            }
        }
    }

    @Override
    public ResourceLocation getKey()
    {
        return FLAVOUR;
    }
}
