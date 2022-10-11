package pokecube.core.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

public class CustomSlot extends Slot
{
    private boolean active = true;

    public CustomSlot(final Container inventoryIn, final int index, final int xPosition, final int yPosition)
    {
        super(inventoryIn, index, xPosition, yPosition);
    }

    public void setActive(boolean active)
    {
        this.active = active;
    }

    @Override
    public boolean isActive()
    {
        return active;
    }
}