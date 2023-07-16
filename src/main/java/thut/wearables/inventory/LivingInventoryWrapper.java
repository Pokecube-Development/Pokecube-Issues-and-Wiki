package thut.wearables.inventory;

import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Nameable;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class LivingInventoryWrapper extends SimpleContainer implements Nameable
{
    public static final int[] ALL_ARMOR_SLOTS = new int[]
    { 0, 1, 2, 3 };
    public static final int[] HELMET_SLOT_ONLY = new int[]
    { 3 };

    private Int2ObjectArrayMap<EquipmentSlot> SLOTMAP = new Int2ObjectArrayMap<>(5);

    private NonNullList<ItemStack> tmpList = NonNullList.of(ItemStack.EMPTY, ItemStack.EMPTY);
    Container start = null;

    public final LivingEntity entity;

    private int startSize = 0;

    public LivingInventoryWrapper(LivingEntity entity)
    {
        this.entity = entity;
        if (entity instanceof InventoryCarrier carrier) start = carrier.getInventory();
        else start = new SimpleContainer(0);

        int i = start.getContainerSize();
        startSize = i;

        SLOTMAP.put(i + 0, EquipmentSlot.FEET);
        SLOTMAP.put(i + 1, EquipmentSlot.LEGS);
        SLOTMAP.put(i + 2, EquipmentSlot.CHEST);
        SLOTMAP.put(i + 3, EquipmentSlot.HEAD);
        SLOTMAP.put(i + 4, EquipmentSlot.OFFHAND);
    }

    @Override
    public void clearContent()
    {
        start.clearContent();
        for (var slot : SLOTMAP.values()) entity.setItemSlot(slot, ItemStack.EMPTY);
    }

    @Override
    public Component getName()
    {
        return new TranslatableComponent("container.inventory");
    }

    @Override
    public int getContainerSize()
    {
        return startSize + SLOTMAP.size();
    }

    @Override
    public boolean isEmpty()
    {
        for (var slot : SLOTMAP.values()) if (!entity.getItemBySlot(slot).isEmpty()) return false;
        return start.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot)
    {
        if (slot < startSize) return start.getItem(slot);
        return entity.getItemBySlot(SLOTMAP.get(slot));
    }

    @Override
    public ItemStack removeItem(int slot, int amount)
    {
        if (slot < startSize) return start.removeItem(slot, amount);
        List<ItemStack> list = tmpList;
        list.set(0, getItem(slot));
        return list != null && !list.get(0).isEmpty() ? ContainerHelper.removeItem(list, 0, amount) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot)
    {
        if (slot < startSize) return start.removeItemNoUpdate(slot);
        List<ItemStack> list = tmpList;
        list.set(0, getItem(slot));
        if (!list.get(0).isEmpty())
        {
            ItemStack itemstack = list.get(0);
            this.setItem(slot, ItemStack.EMPTY);
            return itemstack;
        }
        else
        {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public void setItem(int slot, ItemStack stack)
    {
        if (slot < startSize)
        {
            start.setItem(slot, stack);
            return;
        }
        entity.setItemSlot(SLOTMAP.get(slot), stack);
    }

    @Override
    public boolean stillValid(Player user)
    {
        if (this.entity.isRemoved())
        {
            return false;
        }
        else
        {
            return !(user.distanceToSqr(this.entity) > 64.0D);
        }
    }

    @Override
    public void setChanged()
    {
        start.setChanged();
        super.setChanged();
    }
}
