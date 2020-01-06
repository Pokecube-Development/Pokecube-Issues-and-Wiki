package pokecube.adventures.items.bag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;
import pokecube.core.PokecubeCore;
import pokecube.core.handlers.playerdata.PlayerPokemobCache;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.inventory.pc.PCContainer;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.core.common.ThutCore;
import thut.core.common.handlers.PlayerDataHandler;

public class BagInventory implements IInventory, INBTSerializable<CompoundNBT>
{
    static HashMap<UUID, BagInventory> map_server = new HashMap<>();
    static HashMap<UUID, BagInventory> map_client = new HashMap<>();

    // blank PC for client use.
    public static BagInventory blank;

    public static UUID defaultId = new UUID(1234, 4321);
    public static int  PAGECOUNT = 32;

    public static void addPokecubeToPC(final ItemStack mob, final World world)
    {
        if (!PokecubeManager.isFilled(mob)) return;
        final String player = PokecubeManager.getOwner(mob);
        UUID id;
        try
        {
            id = UUID.fromString(player);
            BagInventory.addStackToPC(id, mob);
        }
        catch (final Exception e)
        {

        }
    }

    public static void addStackToPC(final UUID uuid, final ItemStack mob)
    {
        if (uuid == null || mob.isEmpty())
        {
            System.err.println("Could not find the owner of this item " + mob + " " + uuid);
            return;
        }
        final BagInventory pc = BagInventory.getPC(uuid);

        if (pc == null) return;

        if (PokecubeManager.isFilled(mob))
        {
            final ItemStack stack = mob;
            PokecubeManager.heal(stack);
            PlayerPokemobCache.UpdateCache(mob, true, false);
            if (PokecubeCore.proxy.getPlayer(uuid) != null) PokecubeCore.proxy.getPlayer(uuid).sendMessage(
                    new TranslationTextComponent("block.pc.sentto", mob.getDisplayName()));
        }
        pc.addItem(mob.copy());
        BagSaveHandler.getInstance().savePC(uuid);
    }

    public static void clearPC()
    {
        BagInventory.getMap().clear();
    }

    public static HashMap<UUID, BagInventory> getMap()
    {
        if (ThutCore.proxy.isClientSide()) return BagInventory.map_client;
        return BagInventory.map_server;
    }

    public static BagInventory getPC(final Entity player)
    {// TODO Sync box names/numbers to blank
        if (player == null || player.getEntityWorld().isRemote) return BagInventory.blank == null
                ? BagInventory.blank = new BagInventory(BagInventory.defaultId) : BagInventory.blank;
        return BagInventory.getPC(player.getUniqueID());
    }

    public static BagInventory getPC(final UUID uuid)
    {
        if (uuid != null)
        {
            if (!BagInventory.getMap().containsKey(uuid)) BagSaveHandler.getInstance().loadPC(uuid);
            if (BagInventory.getMap().containsKey(uuid)) return BagInventory.getMap().get(uuid);
            return new BagInventory(uuid);
        }
        return null;
    }

    public static void loadFromNBT(final ListNBT nbt)
    {
        BagInventory.loadFromNBT(nbt, true);
    }

    public static void loadFromNBT(final ListNBT nbt, final boolean replace)
    {
        int i;
        tags:
        for (i = 0; i < nbt.size(); i++)
        {
            final CompoundNBT items = nbt.getCompound(i);
            final BagInventory loaded = new BagInventory();
            loaded.deserializeNBT(items);
            if (!replace && BagInventory.getMap().containsKey(loaded.owner)) continue;
            if (PokecubeMod.debug) PokecubeCore.LOGGER.info("Loading PC for " + loaded.owner);
            BagInventory load = null;
            load = replace ? loaded : BagInventory.getPC(loaded.owner);
            if (load == null)
            {
                if (PokecubeMod.debug) PokecubeCore.LOGGER.info("Skipping " + loaded.owner);
                continue tags;
            }
            load.setPage(loaded.getPage());
            if (load != loaded)
            {
                load.contents.clear();
                load.contents.putAll(loaded.contents);
            }
            BagInventory.getMap().put(loaded.owner, load);
        }
    }

    public static ListNBT saveToNBT(final UUID uuid)
    {
        if (PokecubeMod.debug) PokecubeCore.LOGGER.info("Saving PC for " + uuid);
        final ListNBT nbttag = new ListNBT();
        final CompoundNBT items = BagInventory.getMap().get(uuid).serializeNBT();
        nbttag.add(items);
        return nbttag;
    }

    private int page = 0;

    public boolean[] opened = new boolean[BagInventory.PAGECOUNT];

    public String[]                                boxes    = new String[BagInventory.PAGECOUNT];
    private final Int2ObjectOpenHashMap<ItemStack> contents = new Int2ObjectOpenHashMap<>();

    public UUID owner;

    public BagInventory()
    {
        this.opened = new boolean[BagInventory.PAGECOUNT];
        this.boxes = new String[BagInventory.PAGECOUNT];
        for (int i = 0; i < BagInventory.PAGECOUNT; i++)
            this.boxes[i] = "Box " + String.valueOf(i + 1);
        this.contents.defaultReturnValue(ItemStack.EMPTY);
    }

    public BagInventory(final UUID player)
    {
        this();
        if (!BagInventory.getMap().containsKey(player)) BagInventory.getMap().put(player, this);
        this.owner = player;
    }

    public void addItem(final ItemStack stack)
    {
        for (int i = this.page * 54; i < this.getSizeInventory(); i++)
            if (this.getStackInSlot(i).isEmpty())
            {
                this.setInventorySlotContents(i, stack);
                return;
            }
        for (int i = 0; i < this.page * 54; i++)
            if (this.getStackInSlot(i).isEmpty())
            {
                this.setInventorySlotContents(i, stack);
                return;
            }
    }

    @Override
    public void clear()
    {
        this.contents.clear();
    }

    @Override
    public void closeInventory(final PlayerEntity player)
    {
        BagSaveHandler.getInstance().savePC(this.owner);
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

    @Override
    public void deserializeNBT(final CompoundNBT nbt)
    {
        final CompoundNBT boxes = nbt.getCompound("boxes");
        final String id = boxes.getString("UUID");
        this.owner = UUID.fromString(id);
        final PlayerPokemobCache cache = PlayerDataHandler.getInstance().getPlayerData(this.owner).getData(
                PlayerPokemobCache.class);
        for (int k = 0; k < BagInventory.PAGECOUNT; k++)
        {
            if (k == 0) this.setPage(boxes.getInt("page"));
            if (boxes.getString("name" + k) != null) this.boxes[k] = boxes.getString("name" + k);
        }
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
                cache.addPokemob(id, itemstack, true, false);
            }
        }
    }

    public HashSet<ItemStack> getContents()
    {
        final HashSet<ItemStack> ret = new HashSet<>();
        for (final int i : this.contents.keySet())
            if (!this.contents.get(i).isEmpty()) ret.add(this.contents.get(i));
        return ret;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return PCContainer.STACKLIMIT;
    }

    public int getPage()
    {
        return this.page;
    }

    @Override
    public int getSizeInventory()
    {
        return BagInventory.PAGECOUNT * 54;
    }

    @Override
    public ItemStack getStackInSlot(final int i)
    {
        ItemStack stack = this.contents.get(i);
        if (stack == null) stack = ItemStack.EMPTY;
        return stack;
    }

    @Override
    public boolean isEmpty()
    {
        return true;
    }

    /**
     * Returns true if automation is allowed to insert the given stack
     * (ignoring stack size) into the given slot.
     */
    @Override
    public boolean isItemValidForSlot(final int par1, final ItemStack stack)
    {
        return PCContainer.isItemValid(stack);
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
        if (stack == null) stack = ItemStack.EMPTY;
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

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT items = new CompoundNBT();
        final CompoundNBT boxes = new CompoundNBT();
        boxes.putString("UUID", this.owner.toString());
        boxes.putInt("page", this.page);
        for (int i = 0; i < BagInventory.PAGECOUNT; i++)
            boxes.putString("name" + i, this.boxes[i]);
        items.putInt("page", this.getPage());
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
        items.put("boxes", boxes);
        return items;
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

    @Override
    public String toString()
    {
        String ret = "Owner: " + this.owner + ", Current Page, " + (this.getPage() + 1) + ": ";
        final String eol = System.getProperty("line.separator");
        ret += eol;
        for (final int i : this.contents.keySet())
            if (!this.getStackInSlot(i).isEmpty()) ret += "Slot " + i + ", " + this.getStackInSlot(i).getDisplayName()
                    + "; ";
        ret += eol;
        for (int i = 0; i < this.boxes.length; i++)
            ret += "Box " + (i + 1) + ", " + this.boxes[i] + "; ";
        ret += eol;
        return ret;
    }

}
