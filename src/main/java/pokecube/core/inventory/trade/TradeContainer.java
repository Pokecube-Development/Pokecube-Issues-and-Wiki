package pokecube.core.inventory.trade;

import java.util.UUID;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import pokecube.core.blocks.trade.TraderTile;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.inventory.BaseContainer;

public class TradeContainer extends BaseContainer
{

    public static final ContainerType<TradeContainer> TYPE = new ContainerType<>(TradeContainer::new);
    private IInventory                                inv;
    private final IWorldPosCallable                   pos;
    public TraderTile                                 tile;

    public TradeContainer(final int id, final PlayerInventory inv)
    {
        this(id, inv, IWorldPosCallable.DUMMY);
    }

    public TradeContainer(final int id, final PlayerInventory inv, final IWorldPosCallable pos)
    {
        super(TradeContainer.TYPE, id);
        this.pos = pos;

        pos.consume((w, p) ->
        {
            final TileEntity tile = w.getTileEntity(p);
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
            this.tile = new TraderTile();
            final InvWrapper wrapper = (InvWrapper) this.tile.getCapability(
                    CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
            this.inv = wrapper.getInv();
        }

        this.addSlot(new TradeSlot(this.inv, inv.player, this.tile, 0, 35 + 00, 14 + 00));
        this.addSlot(new TradeSlot(this.inv, inv.player, this.tile, 1, 35 + 90, 14 + 00));
        this.bindPlayerInventory(inv, -19);
        this.trackIntArray(this.tile.syncValues);
    }

    @Override
    public boolean canInteractWith(final PlayerEntity playerIn)
    {
        return this.tile.users.size() <= 2;
    }

    @Override
    public void clearContainer(final PlayerEntity playerIn, final World worldIn, final IInventory inventoryIn)
    {
        if (!(playerIn instanceof ServerPlayerEntity))
        {
            super.clearContainer(playerIn, worldIn, inventoryIn);
            return;
        }
        for (int i = 0; i < inventoryIn.getSizeInventory(); ++i)
        {
            final ItemStack stack = inventoryIn.getStackInSlot(i);
            if (!PokecubeManager.isFilled(stack)) continue;

            final String ids = PokecubeManager.getOwner(stack);
            if (!ids.isEmpty())
            {
                final UUID owner = UUID.fromString(ids);
                final ServerPlayerEntity player = playerIn.getServer().getPlayerList().getPlayerByUUID(owner);
                final boolean shouldReAdd = player.isAlive() && !player.hasDisconnected();
                if (shouldReAdd) player.inventory.placeItemBackInInventory(worldIn, inventoryIn.removeStackFromSlot(i));
                else playerIn.dropItem(inventoryIn.removeStackFromSlot(i), false);
            }
        }
    }

    @Override
    public IInventory getInv()
    {
        return this.inv;
    }

    @Override
    public int getInventorySlotCount()
    {
        return 2;
    }

    @Override
    public void onContainerClosed(final PlayerEntity playerIn)
    {
        super.onContainerClosed(playerIn);
        this.pos.consume((world, pos) ->
        {
            this.clearContainer(playerIn, world, this.inv);
        });
    }
}
