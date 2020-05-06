package thut.api.inventory.big;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
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

    public void load(final UUID uuid)
    {
        if (ThutCore.proxy.isClientSide()) return;
        try
        {
            final File file = PlayerDataHandler.getFileForUUID(uuid.toString(), this.fileName());
            if (file != null && file.exists())
            {
                final FileInputStream fileinputstream = new FileInputStream(file);
                final CompoundNBT CompoundNBT = CompressedStreamTools.readCompressed(fileinputstream);
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

    public void loadNBT(final CompoundNBT nbt)
    {
        final INBT temp = nbt.get(this.tagID());
        if (temp instanceof ListNBT)
        {
            final ListNBT tagListPC = (ListNBT) temp;
            for (int i = 0; i < tagListPC.size(); i++)
            {
                final CompoundNBT items = tagListPC.getCompound(i);
                final T load = this.load_factory.create(this, items);
                this._map.put(load.id, load);
            }
        }
    }

    public void save(final UUID uuid)
    {
        if (ThutCore.proxy.isClientSide()) return;
        try
        {
            final File file = PlayerDataHandler.getFileForUUID(uuid.toString(), this.fileName());
            if (file != null)
            {
                final CompoundNBT CompoundNBT = new CompoundNBT();
                this.writeToNBT(CompoundNBT, uuid);
                final CompoundNBT CompoundNBT1 = new CompoundNBT();
                CompoundNBT1.put("Data", CompoundNBT);
                final FileOutputStream fileoutputstream = new FileOutputStream(file);
                CompressedStreamTools.writeCompressed(CompoundNBT1, fileoutputstream);
                fileoutputstream.close();
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

    public void writeToNBT(final CompoundNBT nbt, final UUID uuid)
    {
        final ListNBT nbttag = new ListNBT();
        final CompoundNBT items = this.get(uuid).serializeNBT();
        nbttag.add(items);
        nbt.put(this.tagID(), nbttag);
    }

    public T get(final Entity mob)
    {
        return this.get(mob.getUniqueID());
    }

    public T get(final UUID id)
    {
        if (!this._map.containsKey(id))
        {
            // First attempt to load it from disc
            this.load(id);
            // If not there, then we can create and add a new one!
            if (!this._map.containsKey(id)) this._map.put(id, this.new_factory.create(this, id));
        }
        return this._map.get(id);
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
