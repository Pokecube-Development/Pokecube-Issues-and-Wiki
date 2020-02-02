package pokecube.mobs.abilities;

import pokecube.core.database.abilities.AbilityManager;

public class AbilityRegister
{
    public static void init()
    {
        AbilityManager.packages.add(AbilityRegister.class.getPackage());
    }
}
