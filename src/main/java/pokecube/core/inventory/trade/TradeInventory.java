package pokecube.core.inventory.trade;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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

public class TradeInventory extends SimpleContainer implements ICapabilitySerializable<CompoundTag>
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
    public void stopOpen(final Player player)
    {
        super.stopOpen(player);
        if (this.tile != null) this.tile.users.remove(player.getUUID());
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
        // Allow filled cubes.
        if (PokecubeManager.isFilled(stack)) return true;
        // Allow empty cubes for masking.
        if (stack.getItem() instanceof IPokecube && stack.getCount() == 1) return true;

        return false;
    }

    @Override
    public void startOpen(final Player player)
    {
        super.startOpen(player);
        if (this.tile != null) this.tile.users.add(player.getUUID());
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag tag = new CompoundTag();
        InvHelper.save(this, tag);
        return tag;
    }
}
