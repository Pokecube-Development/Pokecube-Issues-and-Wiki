package pokecube.mobs.moves.attacks.special;

import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.Stats;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.entity.pokemob.stats.IStatsModifiers;
import pokecube.api.entity.pokemob.stats.StatModifiers;
import pokecube.core.moves.templates.Move_Basic;
import pokecube.core.network.pokemobs.PacketSyncModifier;

public class Powersplit extends Move_Basic
{
    public static class Modifier implements IStatsModifiers
    {
        float[] modifiers = new float[Stats.values().length];

        public Modifier()
        {
        }

        @Override
        public float getModifier(final Stats stat)
        {
            return this.modifiers[stat.ordinal()];
        }

        @Override
        public float getModifierRaw(final Stats stat)
        {
            return this.modifiers[stat.ordinal()];
        }

        @Override
        public int getPriority()
        {
            return 250;
        }

        @Override
        public float apply(final Stats stat, final float valueIn)
        {
            return valueIn + this.getModifier(stat);
        }

        @Override
        public boolean persistant()
        {
            return false;
        }

        @Override
        public void setModifier(final Stats stat, final float value)
        {
            this.modifiers[stat.ordinal()] = value;
        }

    }

    static
    {
        StatModifiers.registerModifier("powersplit", Modifier.class);
    }

    public Powersplit()
    {
        super("powersplit");
    }

    @Override
    public void postAttack(final MovePacket packet)
    {
        super.postAttack(packet);
        if (packet.canceled || packet.failed) return;
        final IPokemob attacked = PokemobCaps.getPokemobFor(packet.attacked);
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
