package pokecube.mobs.moves.attacks;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.IOngoingAffected;
import pokecube.api.entity.IOngoingAffected.IOngoingEffect;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.impl.entity.impl.OngoingMoveEffect;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.templates.Move_Ongoing;

public class Taunt extends Move_Ongoing
{
    private static class TauntEffect extends OngoingMoveEffect
    {
        public TauntEffect(LivingEntity user)
        {
            super(user);
        }

        @Override
        public void onTick(IOngoingAffected target)
        {
            final IPokemob pokemob = PokemobCaps.getPokemobFor(target.getEntity());

            // Check of moves, and if they are damaging moves, mark them as
            // disabled.
            if (pokemob != null)
            {
                for (int index = 0; index < pokemob.getMovesCount(); index++)
                {
                    var move = MovesUtils.getMove(pokemob.getMove(index));
                    // Skip no move
                    if (move == null) continue;
                    // Skip move with direct damage power
                    if (move.getPWR(pokemob, user) > 0) continue;
                    // Disable for 2 ticks, this should get reset every tick,
                    // and this only results in 1 extra tick of no disable.
                    pokemob.setDisableTimer(index, 2);
                }
            }
        }
    }

    @Override
    public void doOngoingEffect(final LivingEntity user, final IOngoingAffected mob, final IOngoingEffect effect)
    {
        // No effect here.
    }

    @Override
    public OngoingMoveEffect makeEffect(LivingEntity user)
    {
        final OngoingMoveEffect effect = new TauntEffect(user);
        effect.setDuration(this.getDuration());
        effect.move = this;
        return effect;
    }
}
