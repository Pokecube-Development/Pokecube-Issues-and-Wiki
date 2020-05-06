package pokecube.core.inventory.trade;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
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

        this.addSlot(new Slot(this.inv, 0, 35 + 00, 14 + 00)
        {
            @Override
            public boolean isItemValid(final ItemStack stack)
            {
                if (PokecubeManager.isFilled(stack))
                {
                    final String id = PokecubeManager.getOwner(stack);
                    return id.equals(inv.player.getCachedUniqueIdString());
                }
                return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
            }
        });
        this.addSlot(new Slot(this.inv, 1, 35 + 90, 14 + 00)
        {
            @Override
            public boolean isItemValid(final ItemStack stack)
            {
                if (PokecubeManager.isFilled(stack))
                {
                    final String id = PokecubeManager.getOwner(stack);
                    return id.equals(inv.player.getCachedUniqueIdString());
                }
                return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
            }
        });
        this.bindPlayerInventory(inv, -19);
    }

    @Override
    public boolean canInteractWith(final PlayerEntity playerIn)
    {
        return true;
    }

    @Override
    public void clearContainer(final PlayerEntity playerIn, final World worldIn, final IInventory inventoryIn)
    {
        final boolean otherUsers = this.tile.users.size() > 1;
        if (!otherUsers)
        {
            super.clearContainer(playerIn, worldIn, inventoryIn);
            return;
        }

        if (!playerIn.isAlive() || playerIn instanceof ServerPlayerEntity && ((ServerPlayerEntity) playerIn)
                .hasDisconnected()) for (int j = 0; j < inventoryIn.getSizeInventory(); ++j)
        {
            final ItemStack stack = inventoryIn.getStackInSlot(j);
            if (PokecubeManager.getOwner(stack).equals(playerIn.getCachedUniqueIdString())) playerIn.dropItem(
                    inventoryIn.removeStackFromSlot(j), false);
        }
        else for (int i = 0; i < inventoryIn.getSizeInventory(); ++i)
        {
            final ItemStack stack = inventoryIn.getStackInSlot(i);
            if (PokecubeManager.getOwner(stack).equals(playerIn.getCachedUniqueIdString())) playerIn.inventory
                    .placeItemBackInInventory(worldIn, inventoryIn.removeStackFromSlot(i));
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
