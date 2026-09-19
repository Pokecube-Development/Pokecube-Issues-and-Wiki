package pokecube.api.effects.mutators;

import net.minecraft.resources.ResourceLocation;
import pokecube.api.effects.EffectPacketInfo;
import pokecube.api.effects.context.EffectContext;
import pokecube.api.moves.MoveEntry;
import pokecube.core.effects.MoveAnimationBase;

public class MoveEffectMutator implements EffectMutator
{
    @Override
    public void mutate(EffectPacketInfo info)
    {
        MoveEntry entry = info.getContext(EffectContext.MOVE);
        if (info.animation.effect() instanceof MoveAnimationBase base && entry != null)
            base.initColour(info.currentTick, entry);
    }

    @Override
    public ResourceLocation getKey()
    {
        return MOVE;
    }
}
