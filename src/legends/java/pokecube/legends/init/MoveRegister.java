package pokecube.legends.init;

import pokecube.core.moves.implementations.MovesAdder;

public class MoveRegister 
{
	public static void init()
    {
        MovesAdder.worldActionPackages.add(MoveRegister.class.getPackage());
    }
}
