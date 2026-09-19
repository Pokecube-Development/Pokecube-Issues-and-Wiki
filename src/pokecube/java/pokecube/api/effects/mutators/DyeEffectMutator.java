package pokecube.api.effects.mutators;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import pokecube.api.effects.EffectPacketInfo;
import pokecube.api.effects.context.EffectContext;
import pokecube.core.effects.MoveAnimationBase;

public class DyeEffectMutator implements EffectMutator
{
    @Override
    public void mutate(EffectPacketInfo info)
    {
        CompoundTag tag = info.getContext(EffectContext.NBT);
        if (info.animation.effect() instanceof MoveAnimationBase base && tag != null)
        {
            int id = tag.getInt("dye");
            int colour = DyeColor.byId(id).getTextColor();
            base.values.rgba = colour | 0xFF000000;
        }
    }

    @Override
    public ResourceLocation getKey()
    {
        return DYE;
    }
}
