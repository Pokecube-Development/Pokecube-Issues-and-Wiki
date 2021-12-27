package pokecube.core.inventory.tms;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.tms.TMTile;
import pokecube.core.inventory.TexturedSlot;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.inventory.BaseContainer;

public class TMContainer extends BaseContainer
{

    public static final MenuType<TMContainer> TYPE  = new MenuType<>(TMContainer::new);
    private Container                             inv;
    private final ContainerLevelAccess                pos;
    public TMTile                                  tile;
    public String[]                                moves = new String[0];

    public TMContainer(final int id, final Inventory inv)
    {
        this(id, inv, ContainerLevelAccess.NULL);
    }

    public TMContainer(final int id, final Inventory inv, final ContainerLevelAccess pos)
    {
        super(TMContainer.TYPE, id);
        this.pos = pos;

        pos.execute((w, p) ->
        {
            final BlockEntity tile = w.getBlockEntity(p);
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
            this.tile = new TMTile(inv.player.blockPosition(), PokecubeItems.TM_MACHINE.get().defaultBlockState());
            this.tile.setLevel(PokecubeCore.proxy.getWorld());
            final InvWrapper wrapper = (InvWrapper) this.tile.getCapability(
                    CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
            this.inv = wrapper.getInv();
        }

        this.addSlot(new TexturedSlot(this.inv, 0, 15 + 00, 12 + 00, "pokecube:gui/slot_tm"));
        final TMContainer cont = this;
        this.addSlot(new TexturedSlot(this.inv, 1, 15 + 00, 12 + 49, "pokecube:gui/slot_cube")
        {
            @Override
            public boolean mayPlace(final ItemStack stack)
            {
                if (PokecubeManager.isFilled(stack)) cont.moves = cont.tile.getMoves(PokecubeManager.itemToPokemob(
                        stack, cont.tile.getLevel()));
                final String owner = PokecubeManager.getOwner(stack);
                if (owner.isEmpty()) return super.mayPlace(stack);
                return inv.player.getStringUUID().equals(owner);
            }

            @Override
            public boolean mayPickup(final Player playerIn)
            {
                final String owner = PokecubeManager.getOwner(this.getItem());
                if (owner.isEmpty()) return super.mayPickup(playerIn);
                return playerIn.getStringUUID().equals(owner);
            }
        });
        this.bindPlayerInventory(inv, -19);
    }

    @Override
    public boolean stillValid(final Player playerIn)
    {
        return true;
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
