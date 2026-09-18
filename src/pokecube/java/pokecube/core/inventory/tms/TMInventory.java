package pokecube.core.inventory.tms;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import pokecube.core.PokecubeItems;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.item.ItemList;

public class TMInventory extends SimpleContainer
{
    public TMInventory()
    {
        super(2);
    }

    @Override
    public boolean canPlaceItem(final int index, final ItemStack stack)
    {
        return switch (index)
        {
            case 0 -> ItemList.is(PokecubeItems.TMKEY, stack.getItem());
            case 1 -> PokecubeManager.isFilled(stack);
            default -> false;
        };
    }
}
