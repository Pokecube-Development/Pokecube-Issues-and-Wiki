package pokecube.core.entity.pokemobs;

import net.minecraft.inventory.Inventory;

public class AnimalChest extends Inventory
{
    public static int INVENSIZE = 7;

    public AnimalChest()
    {
        super(AnimalChest.INVENSIZE);
    }
}
