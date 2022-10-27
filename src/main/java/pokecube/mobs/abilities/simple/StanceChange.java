package pokecube.mobs.abilities.simple;

import pokecube.api.data.PokedexEntry;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.database.Database;

@AbilityProvider(name = "stance-change")
public class StanceChange extends Ability
{
    private static PokedexEntry base_form;
    private static PokedexEntry blade_form;
    private static boolean noTurn = false;

    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (StanceChange.noTurn) return;
        if (StanceChange.base_form == null)
        {
            StanceChange.base_form = Database.getEntry("aegislash-shield");
            StanceChange.blade_form = Database.getEntry("aegislash-blade");
            StanceChange.noTurn = StanceChange.base_form == null || StanceChange.blade_form == null;
            if (StanceChange.noTurn) return;
        }

        if (!areWeUser(mob, move)) return;

        IPokemob attacker = move.getUser();

        final PokedexEntry mobs = attacker.getPokedexEntry();
        final boolean isBlade = mobs == StanceChange.blade_form;
        final boolean isShield = mobs == StanceChange.base_form;

        if (!(isShield || isBlade)) return;

        final MoveEntry attack = move.getMove();

        if (isShield && attack.getPWR(attacker, move.getTarget()) > 0)
            attacker = attacker.setPokedexEntry(StanceChange.blade_form);
        else if (isBlade && move.getName().equals("kings-shield"))
            attacker = attacker.setPokedexEntry(StanceChange.base_form);
        move.setUser(attacker);
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
