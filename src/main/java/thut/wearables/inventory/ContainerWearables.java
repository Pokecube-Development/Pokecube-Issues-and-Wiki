package thut.wearables.inventory;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.IContainerFactory;
import thut.wearables.EnumWearable;
import thut.wearables.ThutWearables;

public class ContainerWearables extends AbstractContainerMenu
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
        public boolean mayPickup(final Player playerIn)
        {
            return EnumWearable.canTakeOff(this.wearer, this.getItem(), this.getSlotIndex());
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public Pair<ResourceLocation, ResourceLocation> getNoItemIcon()
        {
            return Pair.of(InventoryMenu.BLOCK_ATLAS, this.LOCATION);
        }

        @Override
        /**
         * Check if the stack is a valid item for this slot. Always true beside
         * for the armor slots.
         */
        public boolean mayPlace(@Nullable final ItemStack stack)
        {
            return this.slots.canPlaceItem(this.getSlotIndex(), stack);
        }

        @Override
        public void onTake(final Player thePlayer, final ItemStack stack)
        {
            if (!this.wearer.getLevel().isClientSide) EnumWearable.takeOff(thePlayer, stack, this
                    .getSlotIndex());
            super.onTake(thePlayer, stack);
        }

        @Override
        public void set(final ItemStack stack)
        {
            if (!this.wearer.getLevel().isClientSide) EnumWearable.putOn(this.wearer, stack, this
                    .getSlotIndex());
            super.set(stack);
        }
    }

    private static final EquipmentSlot[]              VALID_EQUIPMENT_SLOTS = new EquipmentSlot[] {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };
    public static final MenuType<ContainerWearables> TYPE                  = new MenuType<>(
            (IContainerFactory<ContainerWearables>) ContainerWearables::new);

    public PlayerWearables wearables;
    /** Determines if inventory manipulation should be handled. */
    public LivingEntity    wearer;
    final boolean          hasPlayerSlots;

    public ContainerWearables(final int id, final Inventory player, final FriendlyByteBuf extraData)
    {
        super(ContainerWearables.TYPE, id);
        LivingEntity wearer = player.player;
        final int num = extraData.readInt();
        final Entity mob = wearer.getLevel().getEntity(num);
        if (mob instanceof LivingEntity) wearer = (LivingEntity) mob;

        this.wearer = wearer;
        this.wearables = ThutWearables.getWearables(wearer);
        final int xOffset = 116;
        final int yOffset = 8;
        final int xWidth = 18;
        final int yHeight = 18;
        final InventoryWrapper wrapper = new InventoryWrapper(this.wearables);

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

    private void bindVanillaInventory(final Inventory playerInventory)
    {
        // Player armour slots.
        for (int k = 0; k < 4; ++k)
        {
            final EquipmentSlot entityequipmentslot = ContainerWearables.VALID_EQUIPMENT_SLOTS[k];
            this.addSlot(new Slot(playerInventory, 39 - k, 8, 8 + k * 18)
            {
                /**
                 * Return whether this slot's stack can be taken from this
                 * slot.
                 */
                @Override
                public boolean mayPickup(final Player playerIn)
                {
                    final ItemStack itemstack = this.getItem();
                    return !itemstack.isEmpty() && !playerIn.isCreative() && EnchantmentHelper.hasBindingCurse(
                            itemstack) ? false : super.mayPickup(playerIn);
                }

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

                @Override
                @Nullable
                @OnlyIn(Dist.CLIENT)
                public Pair<ResourceLocation, ResourceLocation> getNoItemIcon()
                {
                    return Pair.of(InventoryMenu.BLOCK_ATLAS,
                            ContainerWearables.ARMOR_SLOT_TEXTURES[entityequipmentslot.getIndex()]);
                }

                /**
                 * Check if the stack is allowed to be placed in this slot,
                 * used for armor slots as well as furnace fuel.
                 */
                @Override
                public boolean mayPlace(final ItemStack stack)
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
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon()
            {
                return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
            }
        });
    }

    @Override
    public boolean stillValid(final Player playerIn)
    {
        return true;
    }

    /** Called when the container is closed. */
    @Override
    public void removed(final Player player)
    {
        super.removed(player);
        if (!player.level.isClientSide) ThutWearables.syncWearables(this.wearer);
    }

    /**
     * Called when a player shift-clicks on a slot. You must override this or
     * you will crash when someone does that.
     */
    @Override
    public ItemStack quickMoveStack(final Player par1PlayerEntity, final int index)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        final Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem())
        {
            final ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            final int numRows = this.hasPlayerSlots ? 3 : 0;
            if (index < numRows * 9)
            {
                if (!this.moveItemStackTo(itemstack1, numRows * 9, this.slots.size(), false))
                    return ItemStack.EMPTY;
            }
            else if (!this.moveItemStackTo(itemstack1, 0, numRows * 9, false)) return ItemStack.EMPTY;

            if (itemstack1.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return itemstack;
    }

}
