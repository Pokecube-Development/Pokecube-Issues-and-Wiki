package thut.api.inventory.big;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import com.google.common.collect.Maps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fmllegacy.LogicalSidedProvider;
import net.minecraftforge.fmlserverevents.FMLServerAboutToStartEvent;
import thut.api.inventory.big.BigInventory.LoadFactory;
import thut.api.inventory.big.BigInventory.NewFactory;
import thut.core.common.ThutCore;
import thut.core.common.handlers.PlayerDataHandler;

public abstract class Manager<T extends BigInventory>
{
    protected Map<UUID, T> _map = Maps.newHashMap();

    private final Predicate<ItemStack> valid;
    private final NewFactory<T>        new_factory;
    private final LoadFactory<T>       load_factory;

    public Manager(final Predicate<ItemStack> valid, final NewFactory<T> new_factory, final LoadFactory<T> load_factory)
    {
        this.valid = valid;
        this.new_factory = new_factory;
        this.load_factory = load_factory;
        MinecraftForge.EVENT_BUS.register(this);
    }

    public abstract String fileName();

    public abstract String tagID();

    protected void load(final UUID uuid)
    {
        if (ThutCore.proxy.isClientSide()) return;
        try
        {
            final File file = PlayerDataHandler.getFileForUUID(uuid.toString(), this.fileName());
            if (file != null && file.exists())
            {
                final FileInputStream fileinputstream = new FileInputStream(file);
                final CompoundTag CompoundNBT = NbtIo.readCompressed(fileinputstream);
                fileinputstream.close();
                this.loadNBT(CompoundNBT.getCompound("Data"));
            }
        }
        catch (final FileNotFoundException e)
        {
        }
        catch (final Exception e)
        {
        }
    }

    protected void loadNBT(final CompoundTag nbt)
    {
        final Tag temp = nbt.get(this.tagID());
        if (temp instanceof ListTag)
        {
            final ListTag tagListPC = (ListTag) temp;
            for (int i = 0; i < tagListPC.size(); i++)
            {
                final CompoundTag items = tagListPC.getCompound(i);
                final T load = this.load_factory.create(this, items);
                this._map.put(load.id, load);
            }
        }
    }

    protected void save(final UUID uuid)
    {
        if (ThutCore.proxy.isClientSide()) return;
        final T save = this.get(uuid, false);
        if (save == null || !save.dirty) return;
        final MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        try
        {

            final File file = PlayerDataHandler.getFileForUUID(uuid.toString(), this.fileName());
            if (file != null)
            {
                final CompoundTag CompoundNBT = new CompoundTag();
                this.writeToNBT(CompoundNBT, save);
                final CompoundTag CompoundNBT1 = new CompoundTag();
                CompoundNBT1.put("Data", CompoundNBT);
                final FileOutputStream fileoutputstream = new FileOutputStream(file);
                NbtIo.writeCompressed(CompoundNBT1, fileoutputstream);
                fileoutputstream.close();
                // Do not retain these if the owner is not actually a logged in
                // player.
                if (server.getPlayerList().getPlayer(uuid) == null) this._map.remove(uuid);
            }
        }
        catch (final FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    public void writeToNBT(final CompoundTag nbt, final T save)
    {
        final ListTag nbttag = new ListTag();
        final CompoundTag items = save.serializeNBT();
        nbttag.add(items);
        nbt.put(this.tagID(), nbttag);
    }

    public T get(final Entity mob)
    {
        return this.get(mob.getUUID());
    }

    public T get(final UUID id, final boolean create)
    {
        if (!this._map.containsKey(id) && create)
        {
            // First attempt to load it from disc
            this.load(id);
            // If not there, then we can create and add a new one!
            if (!this._map.containsKey(id)) this._map.put(id, this.new_factory.create(this, id));
        }
        return this._map.get(id);
    }

    public T get(final UUID id)
    {
        return this.get(id, true);
    }

    public void clear()
    {
        this._map.clear();
    }

    @SubscribeEvent
    protected void serverStarting(final FMLServerAboutToStartEvent event)
    {
        this.clear();
    }

    boolean isItemValid(final ItemStack stack)
    {
        return this.valid.test(stack);
    }

}
