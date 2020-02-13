package pokecube.core.inventory.tms;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.tms.TMTile;
import pokecube.core.inventory.InvHelper;
import pokecube.core.items.pokecubes.PokecubeManager;

public class TMInventory extends Inventory implements ICapabilitySerializable<CompoundNBT>
{
    private final LazyOptional<IItemHandler> holder;

    public TMTile tile;

    public TMInventory()
    {
        super(2);
        this.holder = LazyOptional.of(() -> new InvWrapper(this));
    }

    public TMInventory(final TMTile tile)
    {
        this();
    }

    @Override
    public void deserializeNBT(final CompoundNBT nbt)
    {
        InvHelper.load(this, nbt);
    }

    @Override
    public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
    {
        return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, this.holder);
    }

    @Override
    public boolean isItemValidForSlot(final int index, final ItemStack stack)
    {
        switch (index)
        {
        case 0:
            return PokecubeItems.is(PokecubeItems.TMKEY, stack.getItem());
        case 1:
            return PokecubeManager.isFilled(stack);
        }
        return false;
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT tag = new CompoundNBT();
        InvHelper.save(this, tag);
        return tag;
    }
}
