package pokecube.core.entity.pokemobs;

import net.minecraft.world.SimpleContainer;

public class AnimalChest extends SimpleContainer
{
    public static int INVENSIZE = 7;

    public AnimalChest()
    {
        super(AnimalChest.INVENSIZE);
    }
}
