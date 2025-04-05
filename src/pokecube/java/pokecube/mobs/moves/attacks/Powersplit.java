package pokecube.mobs.moves.attacks;

import pokecube.api.data.moves.MoveProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.Stats;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.stats.IStatsModifiers;
import pokecube.api.entity.pokemob.stats.StatModifiers;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.Damage;
import pokecube.api.moves.utils.MoveApplication.PostMoveUse;
import pokecube.core.network.pokemobs.PacketSyncModifier;

@MoveProvider(name = "power-split")
public class Powersplit implements PostMoveUse
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
        StatModifiers.registerModifier("power-split", Modifier.class);
    }

    @Override
    public void applyPostMove(Damage t)
    {
        MoveApplication packet = t.move();
        if (packet.canceled || packet.failed) return;
        IPokemob attacker = packet.getUser();
        final IPokemob attacked = PokemobCaps.getPokemobFor(packet.getTarget());
        if (attacked != null)
        {
            final int spatk = attacker.getStat(Stats.SPATTACK, true);
            final int atk = attacker.getStat(Stats.ATTACK, true);

            final int spatk2 = attacked.getStat(Stats.SPATTACK, true);
            final int atk2 = attacked.getStat(Stats.ATTACK, true);

            final int averageAtk = (atk + atk2) / 2;
            final int averageSpatk = (spatk + spatk2) / 2;
            final Modifier mods = attacker.getModifiers().getModifiers("power-split", Modifier.class);
            final Modifier mods2 = attacked.getModifiers().getModifiers("power-split", Modifier.class);

            mods.setModifier(Stats.ATTACK, -atk + averageAtk);
            mods2.setModifier(Stats.ATTACK, -atk2 + averageAtk);

            mods.setModifier(Stats.SPATTACK, -spatk + averageSpatk);
            mods2.setModifier(Stats.SPATTACK, -spatk2 + averageSpatk);
            PacketSyncModifier.sendUpdate("power-split", attacker);
            PacketSyncModifier.sendUpdate("power-split", attacked);
        }
    }
}
