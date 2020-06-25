package pokecube.mobs.abilities.s;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.pokemob.moves.MovePacket;

public class StanceChange extends Ability
{
    private static PokedexEntry base_form;
    private static PokedexEntry blade_form;
    private static boolean      noTurn = false;

    @Override
    public void onMoveUse(final IPokemob mob, final MovePacket move)
    {
        if (StanceChange.noTurn) return;
        if (StanceChange.base_form == null)
        {
            StanceChange.base_form = Database.getEntry("Aegislash Shield");
            StanceChange.blade_form = Database.getEntry("Aegislash Blade");
            StanceChange.noTurn = StanceChange.base_form == null || StanceChange.blade_form == null;
            if (StanceChange.noTurn) return;
        }

        IPokemob attacker = move.attacker;

        final PokedexEntry mobs = attacker.getPokedexEntry();
        final boolean isBlade = mobs == StanceChange.blade_form;
        final boolean isShield = mobs == StanceChange.base_form;

        if (!(isShield || isBlade) || !move.pre) return;

        final Move_Base attack = move.getMove();

        if (isShield && attack.getPWR(attacker, move.attacked) > 0) attacker = attacker.setPokedexEntry(
                StanceChange.blade_form);
        else if (isBlade && move.attack.equals("kingsshield")) attacker = attacker.setPokedexEntry(
                StanceChange.base_form);
        move.attacker = attacker;
    }

    @Override
    public IPokemob onRecall(final IPokemob mob)
    {
        final PokedexEntry mobs = mob.getPokedexEntry();
        final boolean isBlade = mobs == StanceChange.blade_form;
        if (isBlade) return mob.setPokedexEntry(StanceChange.base_form);
        return super.onRecall(mob);
    }
}
