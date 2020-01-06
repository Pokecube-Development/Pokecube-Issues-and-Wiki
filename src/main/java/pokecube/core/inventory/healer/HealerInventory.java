package pokecube.core.inventory.healer;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import pokecube.core.items.pokecubes.PokecubeManager;

public class HealerInventory extends Inventory
{
    public HealerInventory()
    {
        super(6);
    }

    @Override
    public boolean isItemValidForSlot(final int index, final ItemStack stack)
    {
        return PokecubeManager.isFilled(stack);
    }
}
