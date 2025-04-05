package pokecube.mobs.moves.attacks;

import pokecube.api.data.moves.MoveProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.Stats;
import pokecube.api.entity.pokemob.stats.IStatsModifiers;
import pokecube.api.entity.pokemob.stats.StatModifiers;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.Damage;
import pokecube.api.moves.utils.MoveApplication.PostMoveUse;
import pokecube.core.network.pokemobs.PacketSyncModifier;

@MoveProvider(name = "power-trick")
public class Powertrick implements PostMoveUse
{
    public static class Modifier implements IStatsModifiers
    {
        float[] modifiers = new float[Stats.values().length];

        public Modifier()
        {}

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
        StatModifiers.registerModifier("power-trick", Modifier.class);
    }

    @Override
    public void applyPostMove(Damage t)
    {
        MoveApplication packet = t.move();
        if (packet.canceled || packet.failed) return;
        IPokemob attacker = packet.getUser();
        final Modifier mods = attacker.getModifiers().getModifiers("power-trick", Modifier.class);
        final int def = attacker.getStat(Stats.DEFENSE, true);
        final int atk = attacker.getStat(Stats.ATTACK, true);
        final float modDef = mods.getModifierRaw(Stats.DEFENSE);
        final float modAtk = mods.getModifierRaw(Stats.ATTACK);
        mods.setModifier(Stats.DEFENSE, modDef - def + atk);
        mods.setModifier(Stats.ATTACK, modAtk - atk + def);
        PacketSyncModifier.sendUpdate("power-trick", attacker);
    }
}
