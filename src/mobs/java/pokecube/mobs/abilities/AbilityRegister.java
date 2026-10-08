package pokecube.mobs.abilities;

import pokecube.api.data.abilities.AbilityManager;

public class AbilityRegister
{
    public static void init()
    {
        AbilityManager.registerAbilityPackage(AbilityRegister.class.getPackage());
    }
}
