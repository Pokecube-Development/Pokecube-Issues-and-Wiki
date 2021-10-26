package pokecube.core.inventory.trade;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import pokecube.core.blocks.trade.TraderTile;
import pokecube.core.items.pokecubes.PokecubeManager;

public class TradeSlot extends Slot
{
    public Player validCheck = null;

    TraderTile tile;

    public TradeSlot(final Container inventoryIn, final Player playerIn, final TraderTile tile, final int index,
            final int xPosition, final int yPosition)
    {
        super(inventoryIn, index, xPosition, yPosition);
        this.validCheck = playerIn;
        this.tile = tile;
    }

    @Override
    public boolean mayPickup(final Player playerIn)
    {
        if (this.tile.confirmed[this.getSlotIndex()]) return false;
        final ItemStack stack = this.getItem();
        if (PokecubeManager.isFilled(stack))
        {
            final String id = PokecubeManager.getOwner(stack);
            return id.equals(playerIn.getStringUUID());
        }
        return super.mayPickup(playerIn);
    }

    @Override
    public void onTake(final Player thePlayer, final ItemStack stack)
    {
        this.tile.confirmed[0] = this.tile.confirmed[1] = false;
        super.onTake(thePlayer, stack);
    }

    @Override
    public boolean mayPlace(final ItemStack stack)
    {
        if (this.tile.confirmed[this.getSlotIndex()]) return false;
        if (this.validCheck != null && PokecubeManager.isFilled(stack))
        {
            final String id = PokecubeManager.getOwner(stack);
            return id.equals(this.validCheck.getStringUUID());
        }
        return this.container.canPlaceItem(this.getSlotIndex(), stack);
    }
}
