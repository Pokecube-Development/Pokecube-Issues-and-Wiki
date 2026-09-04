package pokecube.core.inventory.tms;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import pokecube.api.PokecubeAPI;
import pokecube.api.events.TMMachineEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.tms.TMTile;
import pokecube.core.init.MenuTypes;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.ThutCaps;
import thut.api.inventory.BaseContainer;
import thut.api.inventory.InvHelper;
import thut.api.world.WorldTickManager;

import java.util.ArrayList;
import java.util.List;

public class TMContainer extends BaseContainer
{
    private Container inv;
    private final ContainerLevelAccess pos;
    public TMTile tile;
    public final List<String> moves = new ArrayList<>();

    public TMContainer(final int id, final Inventory inv)
    {
        this(id, inv, ContainerLevelAccess.NULL);
    }

    public TMContainer(final int id, final Inventory inv, final ContainerLevelAccess pos)
    {
        super(MenuTypes.TMS.get(), id);
        this.pos = pos;

        pos.execute((w, p) -> {
            final BlockEntity tile = w.getBlockEntity(p);
            // Server side
            if (tile instanceof TMTile)
            {
                this.tile = (TMTile) tile;
                final InvHelper.ItemCap wrapper = (InvHelper.ItemCap) ThutCaps.getInventory(this.tile);
                this.inv = new thut.api.inventory.InvWrapper(wrapper);
            }
        });
        // Client side
        if (this.inv == null)
        {
            this.tile = new TMTile(inv.player.blockPosition(), PokecubeItems.TM_MACHINE.get().defaultBlockState());
            final InvHelper.ItemCap wrapper = thut.api.attachments.Inventory.get(this.tile);
            this.inv = new thut.api.inventory.InvWrapper(wrapper);
        }
        else
        {
            if (inv.player instanceof ServerPlayer player)
            {
                // Schedule for next tick, as to let player open the inventory
                WorldTickManager.scheduleTask(player.level(), () -> {
                    List<String> _moves = new ArrayList<>();
                    PokecubeAPI.MOVE_BUS.post(new TMMachineEvent(player, this, ItemStack.EMPTY, _moves));
                });
            }
        }

        Level level = inv.player.level();
        this.addSlot(new Slot(this.inv, 0, 8, 17));
        TMContainer cont = this;
        this.addSlot(new Slot(this.inv, 1, 8, 49)
        {
            @Override
            public boolean mayPlace(final ItemStack stack)
            {
                final String owner = PokecubeManager.getOwner(stack, level);
                return owner.isEmpty() ? super.mayPlace(stack) : inv.player.getStringUUID().equals(owner);
            }

            @Override
            public boolean mayPickup(final Player playerIn)
            {
                final String owner = PokecubeManager.getOwner(this.getItem(), level);
                return owner.isEmpty()
                        ? super.mayPickup(playerIn)
                        : playerIn.getStringUUID().equals(owner);
            }

            @Override
            public void setChanged()
            {
                if (inv.player instanceof ServerPlayer player)
                {
                    var stack = this.getItem();
                    List<String> _moves = new ArrayList<>();
                    if (PokecubeManager.isFilled(stack))
                        _moves = cont.tile.getMoves(PokecubeManager.itemToPokemob(stack, level));
                    PokecubeAPI.MOVE_BUS.post(new TMMachineEvent(player, cont, stack, _moves));
                }
                super.setChanged();
            }
        });
        int yOffset = PokecubeCore.getConfig().fancyGUI ? -9 : -23;
        this.bindPlayerInventory(inv, yOffset);
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
        this.pos.execute((world, pos) -> this.clearContainer(playerIn, this.inv));
    }
}
