package pokecube.adventures.blocks.genetics.helper.crafting;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.CraftingResultSlot;

public class PoweredResultSlot extends CraftingResultSlot
{

    public PoweredResultSlot(final PlayerEntity player, final CraftingInventory craftingInventory,
            final IInventory inventoryIn, final int slotIndex, final int xPosition, final int yPosition)
    {
        super(player, craftingInventory, inventoryIn, slotIndex, xPosition, yPosition);
    }

}
