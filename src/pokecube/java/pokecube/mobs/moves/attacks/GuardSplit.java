package pokecube.mobs.moves.attacks;

import pokecube.api.data.moves.MoveProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.Stats;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.Damage;
import pokecube.api.moves.utils.MoveApplication.PostMoveUse;
import pokecube.core.network.pokemobs.PacketSyncModifier;
import pokecube.mobs.moves.attacks.Powersplit.Modifier;

@MoveProvider(name = "guard-split")
public class GuardSplit implements PostMoveUse
{
    @Override
    public void applyPostMove(Damage t)
    {
        MoveApplication packet = t.move();
        if (packet.canceled || packet.failed) return;
        IPokemob attacker = packet.getUser();
        final IPokemob attacked = PokemobCaps.getPokemobFor(packet.getTarget());
        if (attacked != null)
        {
            final int spdef = attacker.getStat(Stats.SPDEFENSE, true);
            final int def = attacker.getStat(Stats.DEFENSE, true);

            final int spdef2 = attacked.getStat(Stats.SPDEFENSE, true);
            final int def2 = attacked.getStat(Stats.DEFENSE, true);

            final int averageDef = (def + def2) / 2;
            final int averageSpdef = (spdef + spdef2) / 2;
            final Modifier mods = attacker.getModifiers().getModifiers("power-split", Modifier.class);
            final Modifier mods2 = attacked.getModifiers().getModifiers("power-split", Modifier.class);

            mods.setModifier(Stats.DEFENSE, -def + averageDef);
            mods2.setModifier(Stats.DEFENSE, -def2 + averageDef);

            mods.setModifier(Stats.SPDEFENSE, -spdef + averageSpdef);
            mods2.setModifier(Stats.SPDEFENSE, -spdef2 + averageSpdef);
            PacketSyncModifier.sendUpdate("power-split", attacker);
            PacketSyncModifier.sendUpdate("power-split", attacked);
        }
    }
}
