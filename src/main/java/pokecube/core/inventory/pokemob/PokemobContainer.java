package pokecube.core.inventory.pokemob;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.PokecubeItems;
import pokecube.core.init.MenuTypes;
import pokecube.core.inventory.TexturedSlot;
import pokecube.core.network.pokemobs.PacketPokemobGui;
import pokecube.core.utils.EntityTools;
import thut.api.inventory.BaseContainer;
import thut.core.common.ThutCore;

public class PokemobContainer extends BaseContainer
{

    public final Container pokemobInv;
    public final IPokemob pokemob;

    public byte mode;
    public FriendlyByteBuf data;
    Inventory playerInv;

    public PokemobContainer(final int id, final Inventory playerInv, final FriendlyByteBuf data)
    {
        super(MenuTypes.POKEMOB.get(), id);
        LivingEntity entity = playerInv.player;
        final int num = data.readInt();
        Entity mob = entity.getLevel().getEntity(num);
        mob = EntityTools.getCoreEntity(mob);
        if (mob instanceof LivingEntity) entity = (LivingEntity) mob;
        this.pokemob = PokemobCaps.getPokemobFor(entity);
        this.pokemobInv = this.pokemob.getInventory();
        this.mode = data.readByte();
        this.data = data;
        this.pokemobInv.startOpen(playerInv.player);
        this.playerInv = playerInv;
        this.setMode(this.mode);
    }

    public void setMode(final int mode)
    {
        if (mode != this.mode && playerInv.player.level.isClientSide())
        {
            PacketPokemobGui.sendPagePacket((byte) mode, pokemob.getEntity().getId());
        }

        this.mode = (byte) mode;

        this.slots.clear();
        this.lastSlots.clear();

        int offhand = pokemobInv.getContainerSize() - 1;

        if (mode == PacketPokemobGui.STORAGE)
        {
            this.addSlot(new TexturedSlot(this.pokemobInv, offhand, 64, 54, "pokecube_adventures:gui/slot_selector"));
        }
        else if (this.mode == PacketPokemobGui.MAIN)
        {
            this.addSlot(new Slot(this.pokemobInv, 0, 64, 18)
            {
                /**
                 * Check if the stack is a valid item for this slot. Always true
                 * beside for the armor slots.
                 */
                @Override
                public boolean mayPlace(final ItemStack stack)
                {
                    return super.mayPlace(stack) && stack.getItem() == Items.SADDLE;
                }
            });
            this.addSlot(new Slot(this.pokemobInv, 1, 64, 36)
            {
                /**
                 * Returns the maximum stack size for a given slot (usually the
                 * same as getInventoryStackLimit(), but 1 in the case of armor
                 * slots)
                 */
                @Override
                public int getMaxStackSize()
                {
                    return 1;
                }

                /**
                 * Check if the stack is a valid item for this slot. Always true
                 * beside for the armor slots.
                 */
                @Override
                public boolean mayPlace(final ItemStack stack)
                {
                    return !pokemob.getPokedexEntry().stock || PokecubeItems.isValidHeldItem(stack);
                }

                @Override
                public void onTake(final Player playerIn, final ItemStack stack)
                {
                    final ItemStack old = this.getItem();
                    if (ThutCore.proxy.isServerSide()) PokemobContainer.this.pokemob.getPokedexEntry()
                            .onHeldItemChange(stack, old, PokemobContainer.this.pokemob);
                    super.onTake(playerIn, stack);
                }

                /** Helper method to put a stack in the slot. */
                @Override
                public void set(final ItemStack stack)
                {
                    // ItemStack old = getStack();
                    super.set(stack);
                    if (ThutCore.proxy.isServerSide()) PokemobContainer.this.pokemob.setHeldItem(stack);
                }
            });
            this.addSlot(new TexturedSlot(this.pokemobInv, offhand, 64, 54, "pokecube_adventures:gui/slot_selector"));
            for (int k = 0; k < 5; ++k) this.addSlot(new Slot(this.pokemobInv, 2 + k, 83 + k * 18, 18)
            {
                /**
                 * Check if the stack is a valid item for this slot. Always true
                 * beside for the armor slots.
                 */
                @Override
                public boolean mayPlace(final ItemStack stack)
                {
                    return true;// ItemList.isValidHeldItem(stack);
                }
            });
        }

        this.bindPlayerInventory(this.playerInv, -19);
    }

    @Override
    public boolean stillValid(final Player user)
    {
        return this.pokemobInv.stillValid(user) && this.pokemob.getEntity().isAlive()
                && this.pokemob.getEntity().distanceTo(user) < 8.0F;
    }

    @Override
    public Container getInv()
    {
        return this.pokemobInv;
    }

    @Override
    public int getInventorySlotCount()
    {
        return this.mode == 0 ? PokemobInventory.MAIN_INVENTORY_SIZE : 0;
    }

    public IPokemob getPokemob()
    {
        return this.pokemob;
    }

    /** Called when the container is closed. */
    @Override
    public void removed(final Player player)
    {
        super.removed(player);
        this.pokemobInv.stopOpen(player);
    }

    @Override
    public void initializeContents(int stateId, List<ItemStack> list, ItemStack carried)
    {
        if (list.size() > this.slots.size())
        {
            list = Lists.newArrayList(list);
            while (list.size() > this.slots.size()) list.remove(list.size() - 1);
        }
        super.initializeContents(stateId, list, carried);
    }
}
