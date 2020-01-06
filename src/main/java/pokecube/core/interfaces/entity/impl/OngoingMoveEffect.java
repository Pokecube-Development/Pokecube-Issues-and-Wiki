package pokecube.core.interfaces.entity.impl;

import net.minecraft.util.ResourceLocation;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.entity.IOngoingAffected;
import pokecube.core.interfaces.entity.IOngoingAffected.IOngoingEffect;
import pokecube.core.moves.templates.Move_Ongoing;

public class OngoingMoveEffect extends BaseEffect
{
    public static final ResourceLocation ID = new ResourceLocation(PokecubeMod.ID, "move_effects");

    public Move_Ongoing move;

    public OngoingMoveEffect()
    {
        super(OngoingMoveEffect.ID);
    }

    @Override
    public void affectTarget(IOngoingAffected target)
    {
        if (this.move != null) this.move.doOngoingEffect(target, this);
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(target.getEntity());
        final boolean toRemove = pokemob != null ? false : Math.random() > 0.8;
        if (toRemove) this.setDuration(0);
    }

    @Override
    public boolean allowMultiple()
    {
        return true;
    }

    @Override
    public AddType canAdd(IOngoingAffected affected, IOngoingEffect toAdd)
    {
        if (toAdd instanceof OngoingMoveEffect && ((OngoingMoveEffect) toAdd).move == this.move) return AddType.DENY;
        return AddType.ACCEPT;
    }

    @Override
    public boolean onSavePersistant()
    {
        return false;
    }

}
