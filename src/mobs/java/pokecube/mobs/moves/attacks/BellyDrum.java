package pokecube.mobs.moves.attacks;

import net.minecraft.network.chat.Component;
import pokecube.api.data.moves.MoveProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.Damage;
import pokecube.api.moves.utils.MoveApplication.PostMoveUse;

@MoveProvider(name = "belly-drum")
public class BellyDrum implements PostMoveUse
{
    @Override
    public void applyPostMove(Damage t) {
        MoveApplication packet = t.move();
        if (packet.canceled || packet.failed) return;

        IPokemob attacker = packet.getUser();
        var attackerE = packet.getUserEntity();
        final IPokemob target = PokemobCaps.getPokemobFor(packet.getTarget());

        if (target != null)
        {
            if (attacker.getHealth() > (attacker.getMaxHealth() / 2.0f))
            {
                packet.stat_effects[IPokemob.Stats.ATTACK.ordinal()] = 6;

                attackerE.setHealth(attacker.getHealth() - (attacker.getMaxHealth() / 2.0f));
                attacker.displayMessageToOwner(Component.translatableEscape("pokemob.move.bellydrum.user", target.getDisplayName()));
            }
            else
                attacker.displayMessageToOwner(Component.translatableEscape("pokemob.move.failed.user", target.getDisplayName()));
        }
    }
}
