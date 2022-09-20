package pokecube.core.inventory.pokemob;

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
        this.mode = (byte) mode;
        int j;
        int k;

        this.slots.clear();
        this.lastSlots.clear();

        if (this.mode == 0)
        {
            this.addSlot(new Slot(this.pokemobInv, 0, 8, 18)
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
            this.addSlot(new Slot(this.pokemobInv, 1, 8, 36)
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
                    return PokecubeItems.isValidHeldItem(stack);
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
            for (j = 0; j < 1; ++j)
                for (k = 0; k < 5; ++k) this.addSlot(new Slot(this.pokemobInv, 2 + k + j * 5, 80 + k * 18, 18 + j * 18)
                {
                    /**
                     * Check if the stack is a valid item for this slot. Always
                     * true beside for the armor slots.
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
}
