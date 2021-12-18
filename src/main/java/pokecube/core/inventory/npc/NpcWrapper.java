package pokecube.core.inventory.npc;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;

public class NpcWrapper implements IItemHandlerModifiable
{
    private Villager npc;
    private InvWrapper mainInv;

    public NpcWrapper(Villager npc)
    {
        this.npc = npc;
        this.mainInv = new InvWrapper(npc.getInventory());
    }

    @Override
    public int getSlots()
    {
        return npc.getInventory().getContainerSize() + 2;
    }

    @Override
    public ItemStack getStackInSlot(int slot)
    {
        if (slot < npc.getInventory().getContainerSize()) return mainInv.getStackInSlot(slot);
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        if (slot < npc.getInventory().getContainerSize()) return mainInv.insertItem(slot, stack, simulate);
        else return ItemStack.EMPTY;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        if (slot < npc.getInventory().getContainerSize()) return mainInv.extractItem(slot, amount, simulate);
        else return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot)
    {
        if (slot < npc.getInventory().getContainerSize()) return mainInv.getSlotLimit(slot);
        else return 0;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack)
    {
        if (slot < npc.getInventory().getContainerSize()) return mainInv.isItemValid(slot, stack);
        else return false;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack)
    {
        if (slot < npc.getInventory().getContainerSize()) mainInv.setStackInSlot(slot, stack);
    }

}
