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

    @Override
    public void onUpdate(IPokemob mob)
    {
        if (Schooling.base == null)
        {
            Schooling.base = Database.getEntry("wishiwashi-solo");
            Schooling.school = Database.getEntry("wishiwashi-school");
        }
        final PokedexEntry mobs = mob.getPokedexEntry();
        if (!(mobs == Schooling.base || mobs == Schooling.school)) return;
        if (mob.getLevel() < 20)
        {
            if (mobs == Schooling.school) mob.changeForm(Schooling.base);
            return;
        }
        if (mob.getEntity().getHealth() > mob.getEntity().getMaxHealth() * 0.25)
        {
            if (mobs == Schooling.base) mob.changeForm(Schooling.school);
        }
        else if (mobs == Schooling.school) mob.changeForm(Schooling.base);
    }
}
