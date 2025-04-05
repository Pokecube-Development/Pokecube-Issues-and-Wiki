package pokecube.core.inventory.tms;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.tms.TMTile;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.item.ItemList;

public class TMInventory extends SimpleContainer
{
    public TMInventory()
    {
        super(2);
    }

    public TMInventory(final TMTile tile)
    {
        this();
    }

    @Override
    public boolean canPlaceItem(final int index, final ItemStack stack)
    {
        switch (index)
        {
        case 0:
            return ItemList.is(PokecubeItems.TMKEY, stack.getItem());
        case 1:
            return PokecubeManager.isFilled(stack);
        }
        return false;
    }
}
