package pokecube.core.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class TexturedSlot extends Slot
{
    final String texture;

    public TexturedSlot(final IInventory inventoryIn, final int index, final int xPosition, final int yPosition,
            final String texture)
    {
        super(inventoryIn, index, xPosition, yPosition);
        this.texture = texture;
    }

    @Override
    public String getSlotTexture()
    {
        if (super.getSlotTexture() == null) super.setBackgroundName(this.texture);
        return super.getSlotTexture();
    }

    @Override
    public boolean isItemValid(final ItemStack stack)
    {
        return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
    }
}
