package pokecube.core.impl.entity.impl;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.IOngoingAffected;
import pokecube.api.entity.IOngoingAffected.IOngoingEffect;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.moves.templates.Move_Ongoing;

public class OngoingMoveEffect extends BaseEffect
{
    public static final ResourceLocation ID = new ResourceLocation(PokecubeMod.ID, "move_effects");

    public Move_Ongoing move;
    public LivingEntity user;

    public OngoingMoveEffect(final LivingEntity user)
    {
        super(OngoingMoveEffect.ID);
        this.user = user;
    }

    @Override
    public void affectTarget(final IOngoingAffected target)
    {
        if (this.move != null) this.move.doOngoingEffect(this.user, target, this);
        final IPokemob pokemob = PokemobCaps.getPokemobFor(target.getEntity());
        final boolean toRemove = pokemob != null ? false : Math.random() > 0.8;
        if (toRemove) this.setDuration(0);
    }

    @Override
    public boolean allowMultiple()
    {
        return true;
    }

    @Override
    public AddType canAdd(final IOngoingAffected affected, final IOngoingEffect toAdd)
    {
        if (toAdd instanceof OngoingMoveEffect move && move.move == this.move) return AddType.DENY;
        return AddType.ACCEPT;
    }

    @Override
    public boolean onSavePersistant()
    {
        return false;
    }

}
