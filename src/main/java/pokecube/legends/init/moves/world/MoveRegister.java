package pokecube.legends.init.moves.world;

import pokecube.core.moves.implementations.MovesAdder;

public class MoveRegister
{
    public static void init()
    {
        MovesAdder.packages.add(MoveRegister.class.getPackage());
    }
}
