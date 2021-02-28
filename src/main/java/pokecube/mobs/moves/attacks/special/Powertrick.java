package pokecube.mobs.moves.attacks.special;

import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.interfaces.pokemob.stats.IStatsModifiers;
import pokecube.core.interfaces.pokemob.stats.StatModifiers;
import pokecube.core.moves.templates.Move_Basic;
import pokecube.core.network.pokemobs.PacketSyncModifier;

public class Powertrick extends Move_Basic
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
            return 200;
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
        StatModifiers.registerModifier("powertrick", Modifier.class);
    }

    public Powertrick()
    {
        super("powertrick");
    }

    @Override
    public void postAttack(final MovePacket packet)
    {
        super.postAttack(packet);
        if (packet.canceled || packet.failed) return;
        final Modifier mods = packet.attacker.getModifiers().getModifiers(this.name, Modifier.class);
        final int def = packet.attacker.getStat(Stats.DEFENSE, true);
        final int atk = packet.attacker.getStat(Stats.ATTACK, true);
        final float modDef = mods.getModifierRaw(Stats.DEFENSE);
        final float modAtk = mods.getModifierRaw(Stats.ATTACK);
        mods.setModifier(Stats.DEFENSE, modDef - def + atk);
        mods.setModifier(Stats.ATTACK, modAtk - atk + def);
        PacketSyncModifier.sendUpdate("powertrick", packet.attacker);
    }
}
