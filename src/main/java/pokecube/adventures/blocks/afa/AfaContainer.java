package pokecube.adventures.blocks.afa;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.entity.BlockEntity;
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

    Container  inv;
    IOwnableTE ownable;

    public AfaTile tile;

    public AfaContainer(final int id, final Inventory invIn)
    {
        this(id, invIn, ContainerLevelAccess.NULL);
    }

    public AfaContainer(final int id, final Inventory invIn, final ContainerLevelAccess pos)
    {
        super(PokecubeAdv.AFA_CONT.get(), id);
        pos.execute((w, p) ->
        {
            final BlockEntity tile = w.getBlockEntity(p);
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
            this.tile = new AfaTile(invIn.player.blockPosition(), PokecubeAdv.AFA.get().defaultBlockState());
            this.ownable = (IOwnableTE) this.tile.getCapability(ThutCaps.OWNABLE_CAP).orElse(null);
            this.inv = this.tile.inventory;
            this.tile.setLevel(PokecubeCore.proxy.getWorld());
        }

        final int di = 12;
        final int dj = 36;
        final int i = 0;
        final int j = 0;
        this.addSlot(new TexturedSlot(this.inv, 0, dj - 21 + j * 18, di + i * 18, "pokecube:item/slot_cube"));
        this.bindPlayerInventory(invIn, -19);

        this.addDataSlots(this.tile.syncValues);
    }

    @Override
    public Container getInv()
    {
        return this.inv;
    }

    @Override
    public int getInventorySlotCount()
    {
        return 1;
    }

    @Override
    public boolean stillValid(final Player playerIn)
    {
        return this.ownable.canEdit(playerIn);
    }
}
