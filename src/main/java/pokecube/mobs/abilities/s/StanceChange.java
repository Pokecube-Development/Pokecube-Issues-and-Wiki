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

        final Move_Base attack = move.getMove();
        final IPokemob attacker = move.attacker;
        final PokedexEntry mobs = mob.getPokedexEntry();

        final boolean isBlade = mobs == StanceChange.blade_form;
        final boolean isShield = mobs == StanceChange.base_form;

        if (!(isShield || isBlade) || !move.pre) return;

        if (isShield && attack.getPWR(attacker, move.attacked) > 0) mob.setPokedexEntry(StanceChange.blade_form);
        else if (isBlade && move.attack.equals("kingsshield")) mob.setPokedexEntry(StanceChange.base_form);
    }
}
