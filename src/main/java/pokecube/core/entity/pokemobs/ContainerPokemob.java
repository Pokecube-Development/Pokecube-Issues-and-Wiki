package pokecube.core.entity.pokemobs;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.IContainerFactory;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.api.inventory.BaseContainer;
import thut.core.common.ThutCore;

public class ContainerPokemob extends BaseContainer
{
    public static final ContainerType<ContainerPokemob> TYPE = new ContainerType<>(
            (IContainerFactory<ContainerPokemob>) ContainerPokemob::new);

    public final IInventory pokemobInv;
    public final IPokemob pokemob;

    public byte         mode;
    public PacketBuffer data;
    PlayerInventory     playerInv;

    public ContainerPokemob(final int id, final PlayerInventory playerInv, final PacketBuffer data)
    {
        super(ContainerPokemob.TYPE, id);
        LivingEntity entity = playerInv.player;
        final int num = data.readInt();
        final Entity mob = entity.getEntityWorld().getEntityByID(num);
        if (mob instanceof LivingEntity) entity = (LivingEntity) mob;
        this.pokemob = CapabilityPokemob.getPokemobFor(entity);
        this.pokemobInv = this.pokemob.getInventory();
        this.mode = data.readByte();
        this.data = data;
        this.pokemobInv.openInventory(playerInv.player);
        this.playerInv = playerInv;
        this.setMode(this.mode);
    }

    public void setMode(final int mode)
    {
        this.mode = (byte) mode;
        int j;
        int k;

        this.inventorySlots.clear();
        this.inventoryItemStacks.clear();

        if (this.mode == 0)
        {
            this.addSlot(new Slot(this.pokemobInv, 0, 8, 18)
            {
                /**
                 * Check if the stack is a valid item for this slot. Always
                 * true beside for the armor slots.
                 */
                @Override
                public boolean isItemValid(final ItemStack stack)
                {
                    return super.isItemValid(stack) && stack.getItem() == Items.SADDLE;
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
                public int getSlotStackLimit()
                {
                    return 1;
                }

                /**
                 * Check if the stack is a valid item for this slot. Always
                 * true beside for the armor slots.
                 */
                @Override
                public boolean isItemValid(final ItemStack stack)
                {
                    return PokecubeItems.isValidHeldItem(stack);
                }

                @Override
                public ItemStack onTake(final PlayerEntity playerIn, final ItemStack stack)
                {
                    final ItemStack old = this.getStack();
                    if (ThutCore.proxy.isServerSide()) ContainerPokemob.this.pokemob.getPokedexEntry().onHeldItemChange(
                            stack, old, ContainerPokemob.this.pokemob);
                    return super.onTake(playerIn, stack);
                }

                /** Helper method to put a stack in the slot. */
                @Override
                public void putStack(final ItemStack stack)
                {
                    // ItemStack old = getStack();
                    super.putStack(stack);
                    if (ThutCore.proxy.isServerSide()) ContainerPokemob.this.pokemob.setHeldItem(stack);
                }
            });
            for (j = 0; j < 1; ++j)
                for (k = 0; k < 5; ++k)
                    this.addSlot(new Slot(this.pokemobInv, 2 + k + j * 5, 80 + k * 18, 18 + j * 18)
                    {
                        /**
                         * Check if the stack is a valid item for this slot.
                         * Always true beside for the armor slots.
                         */
                        @Override
                        public boolean isItemValid(final ItemStack stack)
                        {
                            return true;// ItemList.isValidHeldItem(stack);
                        }
                    });
        }
        this.bindPlayerInventory(this.playerInv, -19);
    }

    @Override
    public boolean canInteractWith(final PlayerEntity p_75145_1_)
    {
        return this.pokemobInv.isUsableByPlayer(p_75145_1_) && this.pokemob.getEntity().isAlive() && this.pokemob
                .getEntity().getDistance(p_75145_1_) < 8.0F;
    }

    @Override
    public IInventory getInv()
    {
        return this.pokemobInv;
    }

    @Override
    public int getInventorySlotCount()
    {
        return this.mode == 0 ? this.getInv().getSizeInventory() : 0;
    }

    public IPokemob getPokemob()
    {
        return this.pokemob;
    }

    /** Called when the container is closed. */
    @Override
    public void onContainerClosed(final PlayerEntity p_75134_1_)
    {
        super.onContainerClosed(p_75134_1_);
        this.pokemobInv.closeInventory(p_75134_1_);
    }
}
