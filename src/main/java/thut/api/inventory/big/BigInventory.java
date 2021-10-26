package thut.api.inventory.big;

import java.util.UUID;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

public abstract class BigInventory implements Container, INBTSerializable<CompoundTag>
{
    public static interface NewFactory<T>
    {
        T create(Manager<?> manager, UUID id);
    }

    public static interface LoadFactory<T>
    {
        T create(Manager<?> manager, CompoundTag nbt);
    }

    UUID id;
    int  page = 0;

    private final boolean isReal;

    private boolean loading = false;

    public boolean dirty = false;

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
    public BigInventory(final Manager<? extends BigInventory> manager, final CompoundTag tag)
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
    public BigInventory(final Manager<? extends BigInventory> manager, final FriendlyByteBuf buffer)
    {
        this.boxes = new String[this.boxCount()];
        for (int i = 0; i < this.boxCount(); i++)
            this.boxes[i] = "Box " + String.valueOf(i + 1);
        this.opened = new boolean[this.boxCount()];
        this.contents.defaultReturnValue(ItemStack.EMPTY);
        this.manager = manager;
        if (buffer != null) this.deserializeNBT(buffer.readNbt());
        this.isReal = false;
    }

    public FriendlyByteBuf makeBuffer()
    {
        final FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer(0));
        final CompoundTag boxInfo = new CompoundTag();
        this.serializeBoxInfo(boxInfo);
        final CompoundTag tag = new CompoundTag();
        tag.put("boxes", boxInfo);
        tag.putBoolean("Real", false);
        buffer.writeNbt(tag);
        return buffer;
    }

    public void addItem(final ItemStack stack)
    {
        for (int i = this.getPage() * 54; i < this.getContainerSize(); i++)
            if (this.getItem(i).isEmpty())
            {
                this.setItem(i, stack);
                return;
            }
        for (int i = 0; i < this.getPage() * 54; i++)
            if (this.getItem(i).isEmpty())
            {
                this.setItem(i, stack);
                return;
            }
    }

    public abstract int boxCount();

    @Override
    public void clearContent()
    {
        this.contents.clear();
    }

    @Override
    public void stopOpen(final Player player)
    {
        if (this.isReal) this.manager.save(this.id);
    }

    @Override
    public ItemStack removeItem(final int i, final int j)
    {
        if (!this.contents.get(i).isEmpty())
        {
            final ItemStack itemstack = this.contents.get(i).split(j);
            if (this.contents.get(i).isEmpty()) this.contents.remove(i);
            if (this.isReal && !this.loading)
            {
                this.dirty = true;
                this.manager.save(this.id);
                this.dirty = false;
            }
            return itemstack;
        }
        return ItemStack.EMPTY;
    }

    public void deserializeBox(final CompoundTag nbt)
    {
        this.loading = true;
        final int start = nbt.getInt("box") * 54;
        for (int i = start; i < start + 54; i++)
        {
            this.setItem(i, ItemStack.EMPTY);
            if (!nbt.contains("item" + i)) continue;
            final CompoundTag CompoundNBT = nbt.getCompound("item" + i);
            final int j = CompoundNBT.getShort("Slot");
            if (j >= start && j < start + 54)
            {
                final ItemStack itemstack = ItemStack.of(CompoundNBT);
                this.setItem(j, itemstack);
            }
        }
        this.loading = false;
    }

    public void deserializeBoxInfo(final CompoundTag boxes)
    {
        final String id = boxes.getString("UUID");
        this.id = UUID.fromString(id);
        for (int k = 0; k < this.boxCount(); k++)
        {
            if (k == 0) this.setPage(boxes.getInt("page"));
            if (boxes.getString("name" + k) != null) this.boxes[k] = boxes.getString("name" + k);
        }
    }

    public void deserializeItems(final CompoundTag nbt)
    {
        this.contents.clear();
        for (final String key : nbt.getAllKeys())
        {
            if (!key.startsWith("item")) continue;
            final CompoundTag CompoundNBT = nbt.getCompound(key);
            final int j = CompoundNBT.getShort("Slot");
            this.loading = true;
            if (j >= 0 && j < this.getContainerSize())
            {
                if (this.contents.containsKey(j)) continue;
                final ItemStack itemstack = ItemStack.of(CompoundNBT);
                this.setItem(j, itemstack);
            }
            this.loading = false;
        }
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt)
    {
        final CompoundTag boxes = nbt.getCompound("boxes");
        this.deserializeBoxInfo(boxes);
        this.deserializeItems(nbt);
    }

    @Override
    public ItemStack getItem(final int i)
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
    public boolean canPlaceItem(final int par1, final ItemStack stack)
    {
        return this.manager.isItemValid(stack);
    }

    @Override
    public boolean stillValid(final Player PlayerEntity)
    {
        return true;
    }

    @Override
    public void setChanged()
    {
    }

    @Override
    public void startOpen(final Player player)
    {
    }

    @Override
    public ItemStack removeItemNoUpdate(final int i)
    {
        ItemStack stack = this.contents.remove(i);
        if (stack.isEmpty()) stack = ItemStack.EMPTY;
        return stack;
    }

    public CompoundTag serializeBox(final int box)
    {
        final CompoundTag items = new CompoundTag();
        items.putInt("box", box);
        final int start = box * 54;
        for (int i = start; i < start + 54; i++)
        {
            final ItemStack itemstack = this.getItem(i);
            final CompoundTag CompoundNBT = new CompoundTag();
            if (!itemstack.isEmpty())
            {
                CompoundNBT.putShort("Slot", (short) i);
                itemstack.save(CompoundNBT);
                items.put("item" + i, CompoundNBT);
            }
        }
        return items;
    }

    public void serializeBoxInfo(final CompoundTag boxes)
    {
        boxes.putString("UUID", this.id.toString());
        boxes.putInt("page", this.page);
        for (int i = 0; i < this.boxCount(); i++)
            boxes.putString("name" + i, this.boxes[i]);
    }

    public void serializeItems(final CompoundTag items)
    {
        for (int i = 0; i < this.getContainerSize(); i++)
        {
            final ItemStack itemstack = this.getItem(i);
            final CompoundTag CompoundNBT = new CompoundTag();
            if (!itemstack.isEmpty())
            {
                CompoundNBT.putShort("Slot", (short) i);
                itemstack.save(CompoundNBT);
                items.put("item" + i, CompoundNBT);
            }
        }
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag items = new CompoundTag();
        final CompoundTag boxes = new CompoundTag();
        this.serializeBoxInfo(boxes);
        this.serializeItems(items);
        items.put("boxes", boxes);
        return items;
    }

    @Override
    public int getContainerSize()
    {
        return this.boxCount() * 54;
    }

    public int getPage()
    {
        return this.page;
    }

    @Override
    public void setItem(final int i, final ItemStack itemstack)
    {
        final ItemStack old = this.contents.get(i);
        if (!itemstack.isEmpty()) this.contents.put(i, itemstack);
        else this.contents.remove(i);
        if (!old.equals(itemstack) && this.isReal && !this.loading)
        {
            this.dirty = true;
            this.manager.save(this.id);
            this.dirty = false;
        }
    }

    public void setPage(final int page)
    {
        this.page = page;
    }

}
