package pokecube.core.inventory.pc;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import pokecube.core.handlers.playerdata.PlayerPokemobCache;

public class PCSlot extends Slot
{
    private int actualSlot = -1;
    public boolean release = false;

    public PCSlot(final Container inventory, final int slotIndex, final int xDisplay, final int yDisplay)
    {
        super(inventory, slotIndex, xDisplay, yDisplay);
        actualSlot = slotIndex;
    }

    /** Return whether this slot's stack can be taken from this slot. */
    @Override
    public boolean mayPickup(final Player par1PlayerEntity)
    {
        return !this.release;
    }

    @Override
    public ItemStack getItem()
    {
        return this.container.getItem(this.getContainerSlot());
    }

    @Override
    public boolean mayPlace(final ItemStack itemstack)
    {
        return this.container.canPlaceItem(this.getContainerSlot(), itemstack);
    }

    /** Called when the stack in a Slot changes */
    @Override
    public void setChanged()
    {
        if (this.getItem().isEmpty()) this.container.setItem(this.getContainerSlot(), ItemStack.EMPTY);
        this.container.setChanged();
    }

    /** Helper method to put a stack in the slot. */
    @Override
    public void set(final ItemStack par1ItemStack)
    {
        this.container.setItem(this.getContainerSlot(), par1ItemStack);
        PlayerPokemobCache.UpdateCache(par1ItemStack, true, false);
        this.setChanged();
    }

    @Override
    public ItemStack remove(int amount)
    {
        return this.container.removeItem(this.getContainerSlot(), amount);
    }

    @Override
    public void onTake(final Player thePlayer, final ItemStack stack)
    {
        PlayerPokemobCache.UpdateCache(stack, false, false);
        super.onTake(thePlayer, stack);
    }

    @Override
    public int getContainerSlot()
    {
        return actualSlot;
    }

    @Override
    public int getSlotIndex()
    {
        return actualSlot;
    }

    public void setSlotIndex(int slot)
    {
        actualSlot = slot;
    }
}
