package pokecube.core.inventory.pokemob;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.PokecubeItems;
import pokecube.core.init.MenuTypes;
import pokecube.core.inventory.CustomSlot;
import pokecube.core.inventory.TexturedSlot;
import pokecube.core.network.pokemobs.PacketPokemobGui;
import pokecube.core.utils.EntityTools;
import pokecube.core.utils.Resources;
import thut.api.inventory.BaseContainer;

public class PokemobContainer extends BaseContainer
{

    public final Container pokemobInv;
    public final IPokemob pokemob;

    public byte mode;
    public CompoundTag data;
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
        this.data = data.readNbt();
        String megaMode = data.readUtf();
        entity.getPersistentData().putString("pokecube:mega_mode", megaMode);
        this.pokemobInv.startOpen(playerInv.player);
        this.playerInv = playerInv;
        this.initSlots();
        this.setMode(this.mode);
    }

    private void initSlots()
    {
        int offhand = pokemobInv.getContainerSize() - 1;

        this.addSlot(new CustomSlot(this.pokemobInv, 0, 63, 18)
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
        this.addSlot(new CustomSlot(this.pokemobInv, 1, 63, 36)
        {
            /**
             * Returns the maximum stack size for a given slot (usually the same
             * as getInventoryStackLimit(), but 1 in the case of armor slots)
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
        });
        this.addSlot(new TexturedSlot(this.pokemobInv, offhand, 63, 54, Resources.SLOT_ICON_BOOK));
        for (int k = 0; k < 5; ++k) this.addSlot(new CustomSlot(this.pokemobInv, 2 + k, 83 + k * 18, 18)
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

        this.bindPlayerInventory(this.playerInv, -19);
    }

    public void setMode(final int mode)
    {

        if (mode != this.mode && playerInv.player.level.isClientSide())
        {
            PacketPokemobGui.sendPagePacket((byte) mode, pokemob.getEntity().getId());
        }

        this.mode = (byte) mode;

        // Toggle activation of slots based on mode.
        if (mode == PacketPokemobGui.MAIN)
        {
            // Main has all of them active
            this.slots.forEach(s -> {
                if (s instanceof CustomSlot slot) slot.setActive(true);
            });
        }
        else if (mode == PacketPokemobGui.STORAGE)
        {
            int offhand = pokemobInv.getContainerSize() - 1;
            // Storage only has the offhand active
            this.slots.forEach(s -> {
                if (s instanceof CustomSlot slot) slot.setActive(s.getSlotIndex() == offhand);
            });
        }
        else
        {
            // The rest have none active
            this.slots.forEach(s -> {
                if (s instanceof CustomSlot slot) slot.setActive(false);
            });
        }
    }

    @Override
    public boolean stillValid(final Player user)
    {
        float dh = pokemob.getSize()
                * (Math.max(pokemob.getBasePokedexEntry().width, pokemob.getPokedexEntry().length));
        return this.pokemobInv.stillValid(user) && this.pokemob.getEntity().isAlive()
                && this.pokemob.getEntity().distanceTo(user) < (8.0F + dh);
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
