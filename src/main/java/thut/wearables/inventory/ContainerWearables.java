package thut.wearables.inventory;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.IContainerFactory;
import thut.wearables.EnumWearable;
import thut.wearables.ThutWearables;

public class ContainerWearables extends Container
{
    public static final ResourceLocation    LOCATION_BLOCKS_TEXTURE     = new ResourceLocation(
            "textures/atlas/blocks.png");
    public static final ResourceLocation    EMPTY_ARMOR_SLOT_HELMET     = new ResourceLocation(
            "item/empty_armor_slot_helmet");
    public static final ResourceLocation    EMPTY_ARMOR_SLOT_CHESTPLATE = new ResourceLocation(
            "item/empty_armor_slot_chestplate");
    public static final ResourceLocation    EMPTY_ARMOR_SLOT_LEGGINGS   = new ResourceLocation(
            "item/empty_armor_slot_leggings");
    public static final ResourceLocation    EMPTY_ARMOR_SLOT_BOOTS      = new ResourceLocation(
            "item/empty_armor_slot_boots");
    private static final ResourceLocation[] ARMOR_SLOT_TEXTURES         = new ResourceLocation[] {
            ContainerWearables.EMPTY_ARMOR_SLOT_BOOTS, ContainerWearables.EMPTY_ARMOR_SLOT_LEGGINGS,
            ContainerWearables.EMPTY_ARMOR_SLOT_CHESTPLATE, ContainerWearables.EMPTY_ARMOR_SLOT_HELMET };

    public static class WornSlot extends Slot
    {
        final LivingEntity     wearer;
        final EnumWearable     slot;
        final InventoryWrapper slots;
        final ResourceLocation LOCATION;

        public WornSlot(final LivingEntity player, final InventoryWrapper inventoryIn, final int index,
                final int xPosition, final int yPosition)
        {
            super(inventoryIn, index, xPosition, yPosition);
            this.slot = EnumWearable.getWearable(index);
            this.slots = inventoryIn;
            this.wearer = player;
            this.LOCATION = new ResourceLocation(EnumWearable.getIcon(this.getSlotIndex()));
        }

        @Override
        /** Return whether this slot's stack can be taken from this slot. */
        public boolean canTakeStack(final PlayerEntity playerIn)
        {
            return EnumWearable.canTakeOff(this.wearer, this.getStack(), this.getSlotIndex());
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public Pair<ResourceLocation, ResourceLocation> getBackground()
        {
            return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, this.LOCATION);
        }

        @Override
        /** Check if the stack is a valid item for this slot. Always true beside
         * for the armor slots. */
        public boolean isItemValid(@Nullable final ItemStack stack)
        {
            return this.slots.isItemValidForSlot(this.getSlotIndex(), stack);
        }

        @Override
        public ItemStack onTake(final PlayerEntity thePlayer, final ItemStack stack)
        {
            if (!this.wearer.getEntityWorld().isRemote) EnumWearable.takeOff(thePlayer, stack, this.getSlotIndex());
            return super.onTake(thePlayer, stack);
        }

        @Override
        public void putStack(final ItemStack stack)
        {
            if (!this.wearer.getEntityWorld().isRemote) EnumWearable.putOn(this.wearer, stack, this.getSlotIndex());
            super.putStack(stack);
        }
    }

    private static final EquipmentSlotType[]              VALID_EQUIPMENT_SLOTS = new EquipmentSlotType[] {
            EquipmentSlotType.HEAD, EquipmentSlotType.CHEST, EquipmentSlotType.LEGS, EquipmentSlotType.FEET };
    public static final ContainerType<ContainerWearables> TYPE                  = new ContainerType<>(
            (IContainerFactory<ContainerWearables>) ContainerWearables::new);

    public PlayerWearables                                slots;
    /** Determines if inventory manipulation should be handled. */
    public LivingEntity                                   wearer;
    final boolean                                         hasPlayerSlots;

    public ContainerWearables(final int id, final PlayerInventory player, final PacketBuffer extraData)
    {
        super(ContainerWearables.TYPE, id);
        LivingEntity wearer = player.player;
        final int num = extraData.readInt();
        final Entity mob = wearer.getEntityWorld().getEntityByID(num);
        if (mob instanceof LivingEntity) wearer = (LivingEntity) mob;

        this.wearer = wearer;
        this.slots = ThutWearables.getWearables(wearer);
        final int xOffset = 116;
        final int yOffset = 8;
        final int xWidth = 18;
        final int yHeight = 18;
        final InventoryWrapper wrapper = new InventoryWrapper(this.slots);

        // First row of ear - hat - ear
        this.addSlot(new WornSlot(wearer, wrapper, 9, xOffset, yOffset));
        this.addSlot(new WornSlot(wearer, wrapper, 12, xOffset + xWidth, yOffset));
        this.addSlot(new WornSlot(wearer, wrapper, 10, xOffset + 2 * xWidth, yOffset));

        // Second row of arm - eye - arm
        this.addSlot(new WornSlot(wearer, wrapper, 2, xOffset, yOffset + yHeight));
        this.addSlot(new WornSlot(wearer, wrapper, 11, xOffset + xWidth, yOffset + yHeight));
        this.addSlot(new WornSlot(wearer, wrapper, 3, xOffset + 2 * xWidth, yOffset + yHeight));

        // Third row of finger - neck - finger
        this.addSlot(new WornSlot(wearer, wrapper, 0, xOffset, yOffset + yHeight * 2));
        this.addSlot(new WornSlot(wearer, wrapper, 6, xOffset + xWidth, yOffset + yHeight * 2));
        this.addSlot(new WornSlot(wearer, wrapper, 1, xOffset + 2 * xWidth, yOffset + yHeight * 2));

        // Fourth row of ankle - waist - ankle
        this.addSlot(new WornSlot(wearer, wrapper, 4, xOffset, yOffset + yHeight * 3));
        this.addSlot(new WornSlot(wearer, wrapper, 8, xOffset + xWidth, yOffset + yHeight * 3));
        this.addSlot(new WornSlot(wearer, wrapper, 5, xOffset + 2 * xWidth, yOffset + yHeight * 3));

        // back slot
        this.addSlot(new WornSlot(wearer, wrapper, 7, xOffset - xWidth, yOffset + yHeight * 3));

        this.hasPlayerSlots = player != null;
        if (this.hasPlayerSlots) this.bindVanillaInventory(player);
    }

    private void bindVanillaInventory(final PlayerInventory playerInventory)
    {
        // Player armour slots.
        for (int k = 0; k < 4; ++k)
        {
            final EquipmentSlotType entityequipmentslot = ContainerWearables.VALID_EQUIPMENT_SLOTS[k];
            this.addSlot(new Slot(playerInventory, 39 - k, 8, 8 + k * 18)
            {
                /** Return whether this slot's stack can be taken from this
                 * slot. */
                @Override
                public boolean canTakeStack(final PlayerEntity playerIn)
                {
                    final ItemStack itemstack = this.getStack();
                    return !itemstack.isEmpty() && !playerIn.isCreative()
                            && EnchantmentHelper.hasBindingCurse(itemstack) ? false : super.canTakeStack(playerIn);
                }

                /** Returns the maximum stack size for a given slot (usually the
                 * same as getInventoryStackLimit(), but 1 in the case of armor
                 * slots) */
                @Override
                public int getSlotStackLimit()
                {
                    return 1;
                }

                @Override
                @Nullable
                @OnlyIn(Dist.CLIENT)
                public Pair<ResourceLocation, ResourceLocation> getBackground()
                {
                    return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE,
                            ContainerWearables.ARMOR_SLOT_TEXTURES[entityequipmentslot.getIndex()]);
                }

                /** Check if the stack is allowed to be placed in this slot,
                 * used for armor slots as well as furnace fuel. */
                @Override
                public boolean isItemValid(final ItemStack stack)
                {
                    return stack.getItem().canEquip(stack, entityequipmentslot, ContainerWearables.this.wearer);
                }
            });
        }

        // Main player inventory
        for (int l = 0; l < 3; ++l)
            for (int j1 = 0; j1 < 9; ++j1)
                this.addSlot(new Slot(playerInventory, j1 + (l + 1) * 9, 8 + j1 * 18, 84 + l * 18));

        // Player hotbar
        for (int i1 = 0; i1 < 9; ++i1)
            this.addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 142));

        // Offhand slot
        this.addSlot(new Slot(playerInventory, 40, 77, 62)
        {
            @Override
            @Nullable
            @OnlyIn(Dist.CLIENT)
            public Pair<ResourceLocation, ResourceLocation> getBackground()
            {
                return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, PlayerContainer.EMPTY_ARMOR_SLOT_SHIELD);
            }
        });
    }

    @Override
    public boolean canInteractWith(final PlayerEntity playerIn)
    {
        return true;
    }

    /** Called when the container is closed. */
    @Override
    public void onContainerClosed(final PlayerEntity player)
    {
        super.onContainerClosed(player);
        if (!player.world.isRemote) ThutWearables.syncWearables(this.wearer);
    }

    /** Called when a player shift-clicks on a slot. You must override this or
     * you will crash when someone does that. */
    @Override
    public ItemStack transferStackInSlot(final PlayerEntity par1PlayerEntity, final int index)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        final Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            final ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            final int numRows = this.hasPlayerSlots ? 3 : 0;
            if (index < numRows * 9)
            {
                if (!this.mergeItemStack(itemstack1, numRows * 9, this.inventorySlots.size(), false))
                    return ItemStack.EMPTY;
            }
            else if (!this.mergeItemStack(itemstack1, 0, numRows * 9, false)) return ItemStack.EMPTY;

            if (itemstack1.isEmpty()) slot.putStack(ItemStack.EMPTY);
            else slot.onSlotChanged();
        }
        return itemstack;
    }

}
