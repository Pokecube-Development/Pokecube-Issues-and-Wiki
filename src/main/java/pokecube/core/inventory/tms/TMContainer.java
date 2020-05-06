package pokecube.core.inventory.tms;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import pokecube.core.PokecubeCore;
import pokecube.core.blocks.tms.TMTile;
import pokecube.core.inventory.TexturedSlot;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.inventory.BaseContainer;

public class TMContainer extends BaseContainer
{

    public static final ContainerType<TMContainer> TYPE  = new ContainerType<>(TMContainer::new);
    private IInventory                             inv;
    private final IWorldPosCallable                pos;
    public TMTile                                  tile;
    public String[]                                moves = new String[0];

    public TMContainer(final int id, final PlayerInventory inv)
    {
        this(id, inv, IWorldPosCallable.DUMMY);
    }

    public TMContainer(final int id, final PlayerInventory inv, final IWorldPosCallable pos)
    {
        super(TMContainer.TYPE, id);
        this.pos = pos;

        pos.consume((w, p) ->
        {
            final TileEntity tile = w.getTileEntity(p);
            // Server side
            if (tile instanceof TMTile)
            {
                this.tile = (TMTile) tile;
                final InvWrapper wrapper = (InvWrapper) this.tile.getCapability(
                        CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
                this.inv = wrapper.getInv();
            }
        });
        // Client side
        if (this.inv == null)
        {
            this.tile = new TMTile();
            this.tile.setWorldAndPos(PokecubeCore.proxy.getWorld(), inv.player.getPosition());
            final InvWrapper wrapper = (InvWrapper) this.tile.getCapability(
                    CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
            this.inv = wrapper.getInv();
        }

        this.addSlot(new TexturedSlot(this.inv, 0, 15 + 00, 12 + 00, "pokecube:items/slot_tm"));
        final TMContainer cont = this;
        this.addSlot(new TexturedSlot(this.inv, 1, 15 + 00, 12 + 49, "pokecube:items/slot_cube")
        {
            @Override
            public boolean isItemValid(final ItemStack stack)
            {
                if (PokecubeManager.isFilled(stack)) cont.moves = cont.tile.getMoves(PokecubeManager.itemToPokemob(
                        stack, cont.tile.getWorld()));
                return super.isItemValid(stack);
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
