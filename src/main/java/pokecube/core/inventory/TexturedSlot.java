package pokecube.core.inventory;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

public class TexturedSlot extends CustomSlot
{
    public static final ResourceLocation BLOCK_ATLAS = new ResourceLocation("pokecube:textures/atlas/slots.png");
    
    public TexturedSlot(final Container inventoryIn, final int index, final int xPosition, final int yPosition,
            final String texture)
    {
        super(inventoryIn, index, xPosition, yPosition);
        this.setBackground(InventoryMenu.BLOCK_ATLAS, new ResourceLocation(texture));
    }

    public TexturedSlot(final Container inventoryIn, final int index, final int xPosition, final int yPosition,
            final ResourceLocation texture)
    {
        super(inventoryIn, index, xPosition, yPosition);
        this.setBackground(InventoryMenu.BLOCK_ATLAS, texture);
        System.out.println(getNoItemIcon());
    }

    @Override
    public boolean mayPlace(final ItemStack stack)
    {
        return this.container.canPlaceItem(this.getSlotIndex(), stack);
    }
}
