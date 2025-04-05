package pokecube.core.inventory.trade;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import pokecube.api.items.IPokecube;
import pokecube.core.blocks.trade.TraderTile;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.inventory.InvHelper;

public class TradeInventory extends SimpleContainer implements INBTSerializable<CompoundTag>
{
    public TraderTile tile;
    public InvWrapper wrapper;

    public TradeInventory()
    {
        super(2);
        wrapper = new InvWrapper(this);
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
    public void deserializeNBT(Provider provider, CompoundTag nbt)
    {
        InvHelper.load(this, nbt, provider);
    }

    @Override
    public CompoundTag serializeNBT(Provider provider)
    {
        final CompoundTag tag = new CompoundTag();
        InvHelper.save(this, tag, provider);
        return tag;
    }
}
