package pokecube.mobs.moves;

import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.moves.implementations.MovesAdder;

public class MoveRegister
{
    public static void init()
    {
        AbilityManager.packages.add(Package.getPackage("pokecube.mobs.abilities."));
        MovesAdder.packages.add(MoveRegister.class.getPackage());
    }
}
