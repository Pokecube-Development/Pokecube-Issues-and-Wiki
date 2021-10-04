package pokecube.core.inventory.tms;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
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
import thut.api.item.ItemList;

public class TMInventory extends SimpleContainer implements ICapabilitySerializable<CompoundTag>
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
    public void deserializeNBT(final CompoundTag nbt)
    {
        InvHelper.load(this, nbt);
    }

    @Override
    public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
    {
        return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, this.holder);
    }

    @Override
    public boolean canPlaceItem(final int index, final ItemStack stack)
    {
        switch (index)
        {
        case 0:
            return ItemList.is(PokecubeItems.TMKEY, stack.getItem());
        case 1:
            return PokecubeManager.isFilled(stack);
        }
        return false;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag tag = new CompoundTag();
        InvHelper.save(this, tag);
        return tag;
    }
}
