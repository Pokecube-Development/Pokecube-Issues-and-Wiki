package pokecube.adventures.blocks.afa;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraftforge.items.IItemHandlerModifiable;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.PokecubeCore;
import pokecube.core.inventory.TexturedSlot;
import thut.api.ThutCaps;
import thut.api.block.IOwnableTE;
import thut.api.inventory.BaseContainer;

public class AfaContainer extends BaseContainer
{
    public static class InvWrapper extends pokecube.core.inventory.InvWrapper
    {
        public InvWrapper(final IItemHandlerModifiable wrapped, final IOwnableTE ownable)
        {
            super(wrapped, p -> ownable.canEdit(p));
        }
    }

    IInventory inv;
    IOwnableTE ownable;

    public AfaTile tile;

    public AfaContainer(final int id, final PlayerInventory invIn)
    {
        this(id, invIn, IWorldPosCallable.NULL);
    }

    public AfaContainer(final int id, final PlayerInventory invIn, final IWorldPosCallable pos)
    {
        super(PokecubeAdv.AFA_CONT.get(), id);
        pos.execute((w, p) ->
        {
            final TileEntity tile = w.getBlockEntity(p);
            // Server side
            if (tile instanceof AfaTile)
            {
                this.ownable = (IOwnableTE) tile.getCapability(ThutCaps.OWNABLE_CAP).orElse(null);
                this.tile = (AfaTile) tile;
                this.inv = ((AfaTile) tile).inventory;
            }
        });
        // Client side
        if (this.ownable == null)
        {
            this.tile = new AfaTile();
            this.ownable = (IOwnableTE) this.tile.getCapability(ThutCaps.OWNABLE_CAP).orElse(null);
            this.inv = this.tile.inventory;
            this.tile.setLevelAndPosition(PokecubeCore.proxy.getWorld(), invIn.player.blockPosition());
        }

        final int di = 12;
        final int dj = 36;
        final int i = 0;
        final int j = 0;
        this.addSlot(new TexturedSlot(this.inv, 0, dj - 21 + j * 18, di + i * 18, "pokecube:items/slot_cube"));
        this.bindPlayerInventory(invIn, -19);

        this.addDataSlots(this.tile.syncValues);
    }

    @Override
    public IInventory getInv()
    {
        return this.inv;
    }

    @Override
    public int getInventorySlotCount()
    {
        return 1;
    }

    @Override
    public boolean stillValid(final PlayerEntity playerIn)
    {
        return this.ownable.canEdit(playerIn);
    }
}
