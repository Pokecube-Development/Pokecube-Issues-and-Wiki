package pokecube.mobs.moves.attacks.special;

import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.templates.Move_Basic;
import pokecube.core.network.pokemobs.PacketSyncModifier;
import pokecube.mobs.moves.attacks.special.MovePowersplit.Modifier;

public class MoveGuardsplit extends Move_Basic
{

    public MoveGuardsplit()
    {
        super("guardsplit");
    }

    @Override
    public void postAttack(MovePacket packet)
    {
        super.postAttack(packet);
        if (packet.canceled || packet.failed) return;
        final IPokemob attacked = CapabilityPokemob.getPokemobFor(packet.attacked);
        if (attacked != null)
        {
            final int spdef = packet.attacker.getStat(Stats.SPDEFENSE, true);
            final int def = packet.attacker.getStat(Stats.DEFENSE, true);

            final int spdef2 = attacked.getStat(Stats.SPDEFENSE, true);
            final int def2 = attacked.getStat(Stats.DEFENSE, true);

            final int averageDef = (def + def2) / 2;
            final int averageSpdef = (spdef + spdef2) / 2;
            final Modifier mods = packet.attacker.getModifiers().getModifiers("powersplit", Modifier.class);
            final Modifier mods2 = attacked.getModifiers().getModifiers("powersplit", Modifier.class);

            mods.setModifier(Stats.DEFENSE, -def + averageDef);
            mods2.setModifier(Stats.DEFENSE, -def2 + averageDef);

            mods.setModifier(Stats.SPDEFENSE, -spdef + averageSpdef);
            mods2.setModifier(Stats.SPDEFENSE, -spdef2 + averageSpdef);
            PacketSyncModifier.sendUpdate("powersplit", packet.attacker);
            PacketSyncModifier.sendUpdate("powersplit", attacked);
        }
    }
}
