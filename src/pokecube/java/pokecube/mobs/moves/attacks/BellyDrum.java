package pokecube.mobs.moves.attacks;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import pokecube.api.data.moves.MoveProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.Damage;
import pokecube.api.moves.utils.MoveApplication.PostMoveUse;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.damage.effects.StatusEffects;
import thut.lib.TComponent;

@MoveProvider(name = "belly-drum")
public class BellyDrum implements PostMoveUse
{
    @Override
    public void applyPostMove(Damage t) {
        MoveApplication packet = t.move();
        if (packet.canceled || packet.failed) return;

        IPokemob attacker = packet.getUser();
        final IPokemob target = PokemobCaps.getPokemobFor(packet.getTarget());

        if (target != null)
        {
            if (attacker.getHealth() > (attacker.getMaxHealth() / 2.0f))
            {
                packet.stat_effects[IPokemob.Stats.ATTACK.ordinal()] = 6;

                attacker.setHealth(attacker.getHealth() - (attacker.getMaxHealth() / 2.0f));
                attacker.displayMessageToOwner(TComponent.translatable("pokemob.move.bellydrum.user", target.getDisplayName()));
            }
            else
                attacker.displayMessageToOwner(TComponent.translatable("pokemob.move.failed.user", target.getDisplayName()));
        }
    }
}
