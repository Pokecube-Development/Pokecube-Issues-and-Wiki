package pokecube.core.inventory.trade;

import net.minecraft.entity.player.PlayerEntity;
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
import pokecube.core.blocks.trade.TraderTile;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.inventory.InvHelper;
import pokecube.core.items.pokecubes.PokecubeManager;

public class TradeInventory extends Inventory implements ICapabilitySerializable<CompoundNBT>
{
    private final LazyOptional<IItemHandler> holder;

    public TraderTile tile;

    public TradeInventory()
    {
        super(2);
        this.holder = LazyOptional.of(() -> new InvWrapper(this));
    }

    public TradeInventory(final TraderTile tile)
    {
        this();
        this.tile = tile;
    }

    @Override
    public void stopOpen(final PlayerEntity player)
    {
        super.stopOpen(player);
        if (this.tile != null) this.tile.users.remove(player.getUUID());
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
    public boolean canPlaceItem(final int index, final ItemStack stack)
    {
        // Allow filled cubes.
        if (PokecubeManager.isFilled(stack)) return true;
        // Allow empty cubes for masking.
        if (stack.getItem() instanceof IPokecube && stack.getCount() == 1) return true;

        return false;
    }

    @Override
    public void startOpen(final PlayerEntity player)
    {
        super.startOpen(player);
        if (this.tile != null) this.tile.users.add(player.getUUID());
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT tag = new CompoundNBT();
        InvHelper.save(this, tag);
        return tag;
    }
}
