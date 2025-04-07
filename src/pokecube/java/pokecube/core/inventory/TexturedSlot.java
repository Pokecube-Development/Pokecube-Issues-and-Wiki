package pokecube.core.inventory;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import pokecube.core.utils.Resources;

public class TexturedSlot extends CustomSlot
{
    public TexturedSlot(final Container inventoryIn, final int index, final int xPosition, final int yPosition,
            final ResourceLocation texture)
    {
        super(inventoryIn, index, xPosition, yPosition);
        this.setBackground(Resources.ICONS_GUI_SHEET, texture);
    }

    @Override
    public boolean mayPlace(final ItemStack stack)
    {
        return this.container.canPlaceItem(this.getSlotIndex(), stack);
    }
}
