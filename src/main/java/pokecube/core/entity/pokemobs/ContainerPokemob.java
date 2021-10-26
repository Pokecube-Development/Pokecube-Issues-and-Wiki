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
import pokecube.core.utils.EntityTools;
import thut.api.inventory.BaseContainer;
import thut.core.common.ThutCore;

public class ContainerPokemob extends BaseContainer
{
    public static final ContainerType<ContainerPokemob> TYPE = new ContainerType<>(
            (IContainerFactory<ContainerPokemob>) ContainerPokemob::new);

    public final IInventory pokemobInv;
    public final IPokemob   pokemob;

    public byte         mode;
    public PacketBuffer data;
    PlayerInventory     playerInv;

    public ContainerPokemob(final int id, final PlayerInventory playerInv, final PacketBuffer data)
    {
        super(ContainerPokemob.TYPE, id);
        LivingEntity entity = playerInv.player;
        final int num = data.readInt();
        Entity mob = entity.getCommandSenderWorld().getEntity(num);
        mob = EntityTools.getCoreEntity(mob);
        if (mob instanceof LivingEntity) entity = (LivingEntity) mob;
        this.pokemob = CapabilityPokemob.getPokemobFor(entity);
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
                 * Check if the stack is a valid item for this slot. Always
                 * true beside for the armor slots.
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
                 * Check if the stack is a valid item for this slot. Always
                 * true beside for the armor slots.
                 */
                @Override
                public boolean mayPlace(final ItemStack stack)
                {
                    return PokecubeItems.isValidHeldItem(stack);
                }

                @Override
                public ItemStack onTake(final PlayerEntity playerIn, final ItemStack stack)
                {
                    final ItemStack old = this.getItem();
                    if (ThutCore.proxy.isServerSide()) ContainerPokemob.this.pokemob.getPokedexEntry().onHeldItemChange(
                            stack, old, ContainerPokemob.this.pokemob);
                    return super.onTake(playerIn, stack);
                }

                /** Helper method to put a stack in the slot. */
                @Override
                public void set(final ItemStack stack)
                {
                    // ItemStack old = getStack();
                    super.set(stack);
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
                        public boolean mayPlace(final ItemStack stack)
                        {
                            return true;// ItemList.isValidHeldItem(stack);
                        }
                    });
        }
        this.bindPlayerInventory(this.playerInv, -19);
    }

    @Override
    public boolean stillValid(final PlayerEntity p_75145_1_)
    {
        return this.pokemobInv.stillValid(p_75145_1_) && this.pokemob.getEntity().isAlive() && this.pokemob
                .getEntity().distanceTo(p_75145_1_) < 8.0F;
    }

    @Override
    public IInventory getInv()
    {
        return this.pokemobInv;
    }

    @Override
    public int getInventorySlotCount()
    {
        return this.mode == 0 ? this.getInv().getContainerSize() : 0;
    }

    public IPokemob getPokemob()
    {
        return this.pokemob;
    }

    /** Called when the container is closed. */
    @Override
    public void removed(final PlayerEntity p_75134_1_)
    {
        super.removed(p_75134_1_);
        this.pokemobInv.stopOpen(p_75134_1_);
    }
}
