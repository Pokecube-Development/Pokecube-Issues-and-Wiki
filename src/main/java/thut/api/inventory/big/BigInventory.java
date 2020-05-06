package thut.api.inventory.big;

import java.util.UUID;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.INBTSerializable;

public abstract class BigInventory implements IInventory, INBTSerializable<CompoundNBT>
{
    public static interface NewFactory<T>
    {
        T create(Manager<?> manager, UUID id);
    }

    public static interface LoadFactory<T>
    {
        T create(Manager<?> manager, CompoundNBT nbt);
    }

    UUID id;
    int  page = 0;

    private final boolean isReal;

    public boolean[] opened;

    public String[] boxes;

    Manager<? extends BigInventory> manager;

    private final Int2ObjectOpenHashMap<ItemStack> contents = new Int2ObjectOpenHashMap<>();

    /**
     * This constructor is called when generating a new inventory
     *
     * @param manager
     * @param id
     */
    public BigInventory(final Manager<? extends BigInventory> manager, final UUID id)
    {
        this.boxes = new String[this.boxCount()];
        for (int i = 0; i < this.boxCount(); i++)
            this.boxes[i] = "Box " + String.valueOf(i + 1);
        this.opened = new boolean[this.boxCount()];
        this.id = id;
        this.contents.defaultReturnValue(ItemStack.EMPTY);
        this.manager = manager;
        this.isReal = true;
    }

    /**
     * This constructor is called when loading the inventory from file
     *
     * @param manager
     * @param id
     */
    public BigInventory(final Manager<? extends BigInventory> manager, final CompoundNBT tag)
    {
        this.boxes = new String[this.boxCount()];
        for (int i = 0; i < this.boxCount(); i++)
            this.boxes[i] = "Box " + String.valueOf(i + 1);
        this.opened = new boolean[this.boxCount()];
        this.contents.defaultReturnValue(ItemStack.EMPTY);
        this.manager = manager;
        this.deserializeNBT(tag);
        this.isReal = true;
    }

    /**
     * This constructor is called when opening the screen on the client
     *
     * @param manager
     * @param id
     */
    public BigInventory(final Manager<? extends BigInventory> manager, final PacketBuffer buffer)
    {
        this.boxes = new String[this.boxCount()];
        for (int i = 0; i < this.boxCount(); i++)
            this.boxes[i] = "Box " + String.valueOf(i + 1);
        this.opened = new boolean[this.boxCount()];
        this.contents.defaultReturnValue(ItemStack.EMPTY);
        this.manager = manager;
        if (buffer != null) this.deserializeNBT(buffer.readCompoundTag());
        this.isReal = false;
    }

    public PacketBuffer makeBuffer()
    {
        final PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(0));
        final CompoundNBT boxInfo = new CompoundNBT();
        this.serializeBoxInfo(boxInfo);
        final CompoundNBT tag = new CompoundNBT();
        tag.put("boxes", boxInfo);
        tag.putBoolean("Real", false);
        buffer.writeCompoundTag(tag);
        return buffer;
    }

    public void addItem(final ItemStack stack)
    {
        for (int i = this.getPage() * 54; i < this.getSizeInventory(); i++)
            if (this.getStackInSlot(i).isEmpty())
            {
                this.setInventorySlotContents(i, stack);
                return;
            }
        for (int i = 0; i < this.getPage() * 54; i++)
            if (this.getStackInSlot(i).isEmpty())
            {
                this.setInventorySlotContents(i, stack);
                return;
            }
    }

    public abstract int boxCount();

    @Override
    public void clear()
    {
        this.contents.clear();
    }

    @Override
    public void closeInventory(final PlayerEntity player)
    {
        if (this.isReal) this.manager.save(this.id);
    }

    @Override
    public ItemStack decrStackSize(final int i, final int j)
    {
        if (!this.contents.get(i).isEmpty())
        {
            final ItemStack itemstack = this.contents.get(i).split(j);
            if (this.contents.get(i).isEmpty()) this.contents.remove(i);
            return itemstack;
        }
        return ItemStack.EMPTY;
    }

    public void deserializeBox(final CompoundNBT nbt)
    {
        final int start = nbt.getInt("box") * 54;
        for (int i = start; i < start + 54; i++)
        {
            this.setInventorySlotContents(i, ItemStack.EMPTY);
            if (!nbt.contains("item" + i)) continue;
            final CompoundNBT CompoundNBT = nbt.getCompound("item" + i);
            final int j = CompoundNBT.getShort("Slot");
            if (j >= start && j < start + 54)
            {
                final ItemStack itemstack = ItemStack.read(CompoundNBT);
                this.setInventorySlotContents(j, itemstack);
            }
        }
    }

    public void deserializeBoxInfo(final CompoundNBT boxes)
    {
        final String id = boxes.getString("UUID");
        this.id = UUID.fromString(id);
        for (int k = 0; k < this.boxCount(); k++)
        {
            if (k == 0) this.setPage(boxes.getInt("page"));
            if (boxes.getString("name" + k) != null) this.boxes[k] = boxes.getString("name" + k);
        }
    }

    public void deserializeItems(final CompoundNBT nbt)
    {
        this.contents.clear();
        for (final String key : nbt.keySet())
        {
            if (!key.startsWith("item")) continue;
            final CompoundNBT CompoundNBT = nbt.getCompound(key);
            final int j = CompoundNBT.getShort("Slot");
            if (j >= 0 && j < this.getSizeInventory())
            {
                if (this.contents.containsKey(j)) continue;
                final ItemStack itemstack = ItemStack.read(CompoundNBT);
                this.setInventorySlotContents(j, itemstack);
            }
        }
    }

    @Override
    public void deserializeNBT(final CompoundNBT nbt)
    {
        final CompoundNBT boxes = nbt.getCompound("boxes");
        this.deserializeBoxInfo(boxes);
        this.deserializeItems(nbt);
    }

    @Override
    public ItemStack getStackInSlot(final int i)
    {
        ItemStack stack = this.contents.get(i);
        if (stack.isEmpty()) stack = ItemStack.EMPTY;
        return stack;
    }

    @Override
    public boolean isEmpty()
    {
        return true;
    }

    public UUID getOwner()
    {
        return this.id;
    }

    /**
     * Returns true if automation is allowed to insert the given stack
     * (ignoring stack size) into the given slot.
     */
    @Override
    public boolean isItemValidForSlot(final int par1, final ItemStack stack)
    {
        return this.manager.isItemValid(stack);
    }

    @Override
    public boolean isUsableByPlayer(final PlayerEntity PlayerEntity)
    {
        return true;
    }

    @Override
    public void markDirty()
    {
    }

    @Override
    public void openInventory(final PlayerEntity player)
    {
    }

    @Override
    public ItemStack removeStackFromSlot(final int i)
    {
        ItemStack stack = this.contents.remove(i);
        if (stack.isEmpty()) stack = ItemStack.EMPTY;
        return stack;
    }

    public CompoundNBT serializeBox(final int box)
    {
        final CompoundNBT items = new CompoundNBT();
        items.putInt("box", box);
        final int start = box * 54;
        for (int i = start; i < start + 54; i++)
        {
            final ItemStack itemstack = this.getStackInSlot(i);
            final CompoundNBT CompoundNBT = new CompoundNBT();
            if (!itemstack.isEmpty())
            {
                CompoundNBT.putShort("Slot", (short) i);
                itemstack.write(CompoundNBT);
                items.put("item" + i, CompoundNBT);
            }
        }
        return items;
    }

    public void serializeBoxInfo(final CompoundNBT boxes)
    {
        boxes.putString("UUID", this.id.toString());
        boxes.putInt("page", this.page);
        for (int i = 0; i < this.boxCount(); i++)
            boxes.putString("name" + i, this.boxes[i]);
    }

    public void serializeItems(final CompoundNBT items)
    {
        for (int i = 0; i < this.getSizeInventory(); i++)
        {
            final ItemStack itemstack = this.getStackInSlot(i);
            final CompoundNBT CompoundNBT = new CompoundNBT();
            if (!itemstack.isEmpty())
            {
                CompoundNBT.putShort("Slot", (short) i);
                itemstack.write(CompoundNBT);
                items.put("item" + i, CompoundNBT);
            }
        }
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT items = new CompoundNBT();
        final CompoundNBT boxes = new CompoundNBT();
        this.serializeBoxInfo(boxes);
        this.serializeItems(items);
        items.put("boxes", boxes);
        return items;
    }

    @Override
    public int getSizeInventory()
    {
        return this.boxCount() * 54;
    }

    public int getPage()
    {
        return this.page;
    }

    @Override
    public void setInventorySlotContents(final int i, final ItemStack itemstack)
    {
        if (!itemstack.isEmpty()) this.contents.put(i, itemstack);
        else this.contents.remove(i);
    }

    public void setPage(final int page)
    {
        this.page = page;
    }

}
