package pokecube.mobs.abilities.s;

import java.util.Random;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;

public class ShieldsDown extends Ability
{
    private static final PokedexEntry[] cores  = new PokedexEntry[7];
    private static PokedexEntry         base;
    private static boolean              noBase = false;

    private static void initFormes()
    {
        ShieldsDown.base = Database.getEntry(774);
        if (ShieldsDown.base == null)
        {
            ShieldsDown.noBase = true;
            return;
        }
        ShieldsDown.cores[0] = Database.getEntry("Blue Core Minior");
        ShieldsDown.cores[1] = Database.getEntry("Green Core Minior");
        ShieldsDown.cores[2] = Database.getEntry("Indigo Core Minior");
        ShieldsDown.cores[3] = Database.getEntry("Orange Core Minior");
        ShieldsDown.cores[4] = Database.getEntry("Red Core Minior");
        ShieldsDown.cores[5] = Database.getEntry("Violet Core Minior");
        ShieldsDown.cores[6] = Database.getEntry("Yellow Core Minior");
        for (int i = 0; i < 7; i++)
            if (ShieldsDown.cores[i] == null) ShieldsDown.cores[i] = ShieldsDown.base;
    }

    private PokedexEntry getCoreEntry(IPokemob mob)
    {
        final int num = mob.getRNGValue();
        final Random rand = new Random(num);
        final int index = rand.nextInt(ShieldsDown.cores.length);
        return ShieldsDown.cores[index];
    }

    @Override
    public void onUpdate(IPokemob mob)
    {
        if (ShieldsDown.noBase) return;
        if (ShieldsDown.base == null) ShieldsDown.initFormes();
        final PokedexEntry entry = mob.getPokedexEntry();
        if (ShieldsDown.base == null || entry.getPokedexNb() != ShieldsDown.base.getPokedexNb()) return;
        final float ratio = mob.getEntity().getHealth() / mob.getEntity().getMaxHealth();
        if (ratio < 0.5)
        {
            final PokedexEntry core = this.getCoreEntry(mob);
            if (core != null && core != entry) mob.setPokedexEntry(core);
        }
        else if (entry != ShieldsDown.base) mob.setPokedexEntry(ShieldsDown.base);
    }
}
