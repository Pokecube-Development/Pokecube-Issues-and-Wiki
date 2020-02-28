package pokecube.core.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class TexturedSlot extends Slot
{
    public TexturedSlot(final IInventory inventoryIn, final int index, final int xPosition, final int yPosition,
            final String texture)
    {
        super(inventoryIn, index, xPosition, yPosition);
        this.setBackground(PlayerContainer.LOCATION_BLOCKS_TEXTURE, new ResourceLocation(texture));
    }

    @Override
    public boolean isItemValid(final ItemStack stack)
    {
        return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
    }
}
