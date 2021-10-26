package pokecube.core.inventory.trade;

import java.util.UUID;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.trade.TraderTile;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.inventory.BaseContainer;

public class TradeContainer extends BaseContainer
{

    public static final MenuType<TradeContainer> TYPE = new MenuType<>(TradeContainer::new);
    private Container                            inv;
    private final ContainerLevelAccess           pos;
    public TraderTile                            tile;

    public TradeContainer(final int id, final Inventory inv)
    {
        this(id, inv, ContainerLevelAccess.NULL);
    }

    public TradeContainer(final int id, final Inventory inv, final ContainerLevelAccess pos)
    {
        super(TradeContainer.TYPE, id);
        this.pos = pos;

        pos.execute((w, p) ->
        {
            final BlockEntity tile = w.getBlockEntity(p);
            // Server side
            if (tile instanceof TraderTile)
            {
                this.tile = (TraderTile) tile;
                final InvWrapper wrapper = (InvWrapper) this.tile.getCapability(
                        CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
                this.inv = wrapper.getInv();
            }
        });
        // Client side
        if (this.inv == null)
        {
            this.tile = new TraderTile(inv.player.blockPosition(), PokecubeItems.TRADER.get().defaultBlockState());
            final InvWrapper wrapper = (InvWrapper) this.tile.getCapability(
                    CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
            this.inv = wrapper.getInv();
        }

        this.addSlot(new TradeSlot(this.inv, inv.player, this.tile, 0, 35 + 00, 14 + 00));
        this.addSlot(new TradeSlot(this.inv, inv.player, this.tile, 1, 35 + 90, 14 + 00));
        this.bindPlayerInventory(inv, -19);
        this.addDataSlots(this.tile.syncValues);
    }

    @Override
    public boolean stillValid(final Player playerIn)
    {
        return this.tile.users.size() <= 2;
    }

    @Override
    public void clearContainer(final Player playerIn, final Container inventoryIn)
    {
        if (!(playerIn instanceof ServerPlayer))
        {
            super.clearContainer(playerIn, inventoryIn);
            return;
        }
        for (int i = 0; i < inventoryIn.getContainerSize(); ++i)
        {
            final ItemStack stack = inventoryIn.getItem(i);
            if (!PokecubeManager.isFilled(stack)) continue;

            final String ids = PokecubeManager.getOwner(stack);
            if (!ids.isEmpty())
            {
                final UUID owner = UUID.fromString(ids);
                final ServerPlayer player = playerIn.getServer().getPlayerList().getPlayer(owner);
                final boolean shouldReAdd = player.isAlive() && !player.hasDisconnected();
                if (shouldReAdd) player.getInventory().placeItemBackInInventory(inventoryIn.removeItemNoUpdate(i));
                else playerIn.drop(inventoryIn.removeItemNoUpdate(i), false);
            }
        }
    }

    @Override
    public Container getInv()
    {
        return this.inv;
    }

    @Override
    public int getInventorySlotCount()
    {
        return 2;
    }

    @Override
    public void removed(final Player playerIn)
    {
        super.removed(playerIn);
        this.pos.execute((world, pos) ->
        {
            this.clearContainer(playerIn, this.inv);
        });
    }
}
