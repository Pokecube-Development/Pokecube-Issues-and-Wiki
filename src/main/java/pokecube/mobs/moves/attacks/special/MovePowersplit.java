package pokecube.mobs.moves.attacks.special;

import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.interfaces.pokemob.stats.IStatsModifiers;
import pokecube.core.interfaces.pokemob.stats.StatModifiers;
import pokecube.core.moves.templates.Move_Basic;
import pokecube.core.network.pokemobs.PacketSyncModifier;

public class MovePowersplit extends Move_Basic
{
    public static class Modifier implements IStatsModifiers
    {
        float[] modifiers = new float[Stats.values().length];

        public Modifier()
        {
        }

        @Override
        public float getModifier(Stats stat)
        {
            return this.modifiers[stat.ordinal()];
        }

        @Override
        public float getModifierRaw(Stats stat)
        {
            return this.modifiers[stat.ordinal()];
        }

        @Override
        public int getPriority()
        {
            return 250;
        }

        @Override
        public boolean isFlat()
        {
            return true;
        }

        @Override
        public boolean persistant()
        {
            return false;
        }

        @Override
        public void setModifier(Stats stat, float value)
        {
            this.modifiers[stat.ordinal()] = value;
        }

    }

    static
    {
        StatModifiers.registerModifier("powersplit", Modifier.class);
    }

    public MovePowersplit()
    {
        super("powersplit");
    }

    @Override
    public void postAttack(MovePacket packet)
    {
        super.postAttack(packet);
        if (packet.canceled || packet.failed) return;
        final IPokemob attacked = CapabilityPokemob.getPokemobFor(packet.attacked);
        if (attacked != null)
        {
            final int spatk = packet.attacker.getStat(Stats.SPATTACK, true);
            final int atk = packet.attacker.getStat(Stats.ATTACK, true);

            final int spatk2 = attacked.getStat(Stats.SPATTACK, true);
            final int atk2 = attacked.getStat(Stats.ATTACK, true);

            final int averageAtk = (atk + atk2) / 2;
            final int averageSpatk = (spatk + spatk2) / 2;
            final Modifier mods = packet.attacker.getModifiers().getModifiers("powersplit", Modifier.class);
            final Modifier mods2 = attacked.getModifiers().getModifiers("powersplit", Modifier.class);

            mods.setModifier(Stats.ATTACK, -atk + averageAtk);
            mods2.setModifier(Stats.ATTACK, -atk2 + averageAtk);

            mods.setModifier(Stats.SPATTACK, -spatk + averageSpatk);
            mods2.setModifier(Stats.SPATTACK, -spatk2 + averageSpatk);
            PacketSyncModifier.sendUpdate("powersplit", packet.attacker);
            PacketSyncModifier.sendUpdate("powersplit", attacked);
        }
    }
}
