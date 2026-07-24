package pokecube.mobs.moves.attacks;

import pokecube.api.data.moves.MoveProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.Damage;
import pokecube.api.moves.utils.MoveApplication.HealProvider;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.damage.effects.StatusEffects;
import thut.lib.TComponent;

@MoveProvider(name = "heal-pulse")
public class HealPulse implements HealProvider
{
    @Override
    public void applyHealing(Damage t)
    {
        MoveApplication packet = t.move();
        if (packet.canceled || packet.failed) return;

        IPokemob attacker = packet.getUser();
        IPokemob target = PokemobCaps.getPokemobFor(packet.getTarget());
        if (target != null)
        {
            target.getEntity().heal(Math.min(attacker.getEntity().getMaxHealth() - attacker.getEntity().getHealth(), attacker.getEntity().getMaxHealth() / 2));
            attacker.displayMessageToOwner(TComponent.translatable("pokemob.move.hprestore.target", target.getDisplayName()));
        }

    }
}