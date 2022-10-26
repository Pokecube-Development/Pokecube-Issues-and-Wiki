package pokecube.mobs.abilities.simple;

import pokecube.api.data.PokedexEntry;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.database.Database;

@AbilityProvider(name = "schooling")
public class Schooling extends Ability
{
    private static PokedexEntry base;
    private static PokedexEntry school;
    private static boolean      noTurn = false;

    @Override
    public void onUpdate(IPokemob mob)
    {
        if (Schooling.noTurn) return;
        if (Schooling.base == null)
        {
            Schooling.base = Database.getEntry("wishiwashi-solo");
            Schooling.school = Database.getEntry("wishiwashi-school");
            Schooling.noTurn = Schooling.base == null || Schooling.school == null;
            if (Schooling.noTurn) return;
        }
        final PokedexEntry mobs = mob.getPokedexEntry();
        if (!(mobs == Schooling.base || mobs == Schooling.school)) return;
        if (mob.getLevel() < 20)
        {
            if (mobs == Schooling.school) mob.setPokedexEntry(Schooling.base);
            return;
        }
        if (mob.getEntity().getHealth() > mob.getEntity().getMaxHealth() * 0.25)
        {
            if (mobs == Schooling.base) mob.setPokedexEntry(Schooling.school);
        }
        else if (mobs == Schooling.school) mob.setPokedexEntry(Schooling.base);
    }
}
