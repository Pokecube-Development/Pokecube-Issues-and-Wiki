package pokecube.core.inventory.healer;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import pokecube.core.items.pokecubes.PokecubeManager;

public class HealerInventory extends SimpleContainer
{
    public HealerInventory()
    {
        super(6);
    }

    @Override
    public boolean canPlaceItem(final int index, final ItemStack stack)
    {
        return PokecubeManager.isFilled(stack);
    }
}
