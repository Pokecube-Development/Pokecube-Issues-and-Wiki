package thut.core.common.handlers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.SaveHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import thut.core.common.ThutCore;

public class PlayerDataHandler
{
    private static interface IPlayerData
    {
        String dataFileName();

        String getIdentifier();

        void readFromNBT(CompoundNBT tag);

        void readSync(ByteBuf data);

        boolean shouldSync();

        void writeSync(ByteBuf data);

        void writeToNBT(CompoundNBT tag);
    }

    public static abstract class PlayerData implements IPlayerData
    {
        @Override
        public void readSync(ByteBuf data)
        {
        }

        @Override
        public void writeSync(ByteBuf data)
        {
        }
    }

    public static class PlayerDataManager
    {
        public Map<Class<? extends PlayerData>, PlayerData> data  = Maps.newHashMap();
        Map<String, PlayerData>                             idMap = Maps.newHashMap();
        final String                                        uuid;

        public PlayerDataManager(String uuid)
        {
            this.uuid = uuid;
            for (final Class<? extends PlayerData> type : PlayerDataHandler.dataMap)
                try
                {
                    final PlayerData toAdd = type.newInstance();
                    this.data.put(type, toAdd);
                    this.idMap.put(toAdd.getIdentifier(), toAdd);
                }
                catch (final InstantiationException e)
                {
                    e.printStackTrace();
                }
                catch (final IllegalAccessException e)
                {
                    e.printStackTrace();
                }
        }

        @SuppressWarnings("unchecked")
        public <T extends PlayerData> T getData(Class<T> type)
        {
            return (T) this.data.get(type);
        }

        public PlayerData getData(String dataType)
        {
            return this.idMap.get(dataType);
        }
    }

    private static Set<Class<? extends PlayerData>> dataMap = Sets.newHashSet();
    private static Set<String>                      dataIds = Sets.newHashSet();
    private static PlayerDataHandler                INSTANCESERVER;
    private static PlayerDataHandler                INSTANCECLIENT;

    public static void clear()
    {
        if (PlayerDataHandler.INSTANCECLIENT != null) MinecraftForge.EVENT_BUS.unregister(
                PlayerDataHandler.INSTANCECLIENT);
        if (PlayerDataHandler.INSTANCESERVER != null) MinecraftForge.EVENT_BUS.unregister(
                PlayerDataHandler.INSTANCESERVER);
        PlayerDataHandler.INSTANCECLIENT = PlayerDataHandler.INSTANCESERVER = null;
    }

    public static Set<String> getDataIDs()
    {
        if (PlayerDataHandler.dataIds.size() != PlayerDataHandler.dataMap.size())
            for (final Class<? extends PlayerData> type : PlayerDataHandler.dataMap)
            {
            PlayerData toAdd;
            try
            {
            toAdd = type.newInstance();
            PlayerDataHandler.dataIds.add(toAdd.getIdentifier());
            }
            catch (InstantiationException | IllegalAccessException e)
            {
            e.printStackTrace();
            }
            }
        return PlayerDataHandler.dataIds;
    }

    public static File getFileForUUID(String uuid, String fileName)
    {
        final MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        final ServerWorld world = server.getWorld(DimensionType.OVERWORLD);
        final SaveHandler saveHandler = world.getSaveHandler();
        final String seperator = System.getProperty("file.separator");
        final File worlddir = saveHandler.getWorldDirectory();
        final File file = new File(worlddir, "thutcore" + seperator + uuid + seperator + fileName + ".dat");
        final File dir = new File(file.getParentFile().getAbsolutePath());
        if (!file.exists()) dir.mkdirs();
        return file;
    }

    public static PlayerDataHandler getInstance()
    {
        if (ThutCore.proxy.isClientSide()) return PlayerDataHandler.INSTANCECLIENT != null
                ? PlayerDataHandler.INSTANCECLIENT : (PlayerDataHandler.INSTANCECLIENT = new PlayerDataHandler());
        return PlayerDataHandler.INSTANCESERVER != null ? PlayerDataHandler.INSTANCESERVER
                : (PlayerDataHandler.INSTANCESERVER = new PlayerDataHandler());
    }

    public static void register(Class<? extends PlayerData> data)
    {
        PlayerDataHandler.dataMap.add(data);
    }

    public static void saveCustomData(PlayerEntity player)
    {
        PlayerDataHandler.saveCustomData(player.getCachedUniqueIdString());
    }

    public static void saveCustomData(String cachedUniqueIdString)
    {
        PlayerDataHandler.getInstance().save(cachedUniqueIdString, "misc");
    }

    private final Map<String, PlayerDataManager> data = Maps.newHashMap();

    public PlayerDataHandler()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void cleanupOfflineData(WorldEvent.Save event)
    {
        // Whenever overworld saves, check player list for any that are not
        // online, and remove them. This is done here, and not on logoff, as
        // something may have requested the manager for an offline player, which
        // would have loaded it.
        final DimensionType type = event.getWorld().getDimension().getType();
        if (type == DimensionType.OVERWORLD)
        {
            final Set<String> toUnload = Sets.newHashSet();
            final MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
            for (final String uuid : this.data.keySet())
            {
                final ServerPlayerEntity player = server.getPlayerList().getPlayerByUUID(UUID.fromString(uuid));
                if (player == null) toUnload.add(uuid);
            }
            for (final String s : toUnload)
            {
                this.save(s);
                this.data.remove(s);
            }
        }
    }

    public PlayerDataManager getPlayerData(PlayerEntity player)
    {
        return this.getPlayerData(player.getCachedUniqueIdString());
    }

    public PlayerDataManager getPlayerData(String uuid)
    {
        PlayerDataManager manager = this.data.get(uuid);
        if (manager == null) manager = this.load(uuid);
        return manager;
    }

    public PlayerDataManager getPlayerData(UUID uniqueID)
    {
        return this.getPlayerData(uniqueID.toString());
    }

    public PlayerDataManager load(String uuid)
    {
        final PlayerDataManager manager = new PlayerDataManager(uuid);
        if (this == PlayerDataHandler.INSTANCESERVER) for (final PlayerData data : manager.data.values())
        {
            final String fileName = data.dataFileName();
            File file = null;
            try
            {
                file = PlayerDataHandler.getFileForUUID(uuid, fileName);
            }
            catch (final Exception e)
            {

            }
            if (file != null && file.exists()) try
            {
                final FileInputStream fileinputstream = new FileInputStream(file);
                final CompoundNBT CompoundNBT = CompressedStreamTools.readCompressed(fileinputstream);
                fileinputstream.close();
                data.readFromNBT(CompoundNBT.getCompound("Data"));
            }
            catch (final IOException e)
            {
                e.printStackTrace();
            }
        }
        this.data.put(uuid, manager);
        return manager;
    }

    public void save(String uuid)
    {
        final PlayerDataManager manager = this.data.get(uuid);
        if (manager != null && this == PlayerDataHandler.INSTANCESERVER) for (final PlayerData data : manager.data
                .values())
        {
            final String fileName = data.dataFileName();
            final File file = PlayerDataHandler.getFileForUUID(uuid, fileName);
            if (file != null)
            {
                final CompoundNBT CompoundNBT = new CompoundNBT();
                data.writeToNBT(CompoundNBT);
                final CompoundNBT CompoundNBT1 = new CompoundNBT();
                CompoundNBT1.put("Data", CompoundNBT);
                try
                {
                    final FileOutputStream fileoutputstream = new FileOutputStream(file);
                    CompressedStreamTools.writeCompressed(CompoundNBT1, fileoutputstream);
                    fileoutputstream.close();
                }
                catch (final IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public void save(String uuid, String dataType)
    {
        final PlayerDataManager manager = this.data.get(uuid);
        if (manager != null && this == PlayerDataHandler.INSTANCESERVER) for (final PlayerData data : manager.data
                .values())
        {
            if (!data.getIdentifier().equals(dataType)) continue;
            final String fileName = data.dataFileName();
            final File file = PlayerDataHandler.getFileForUUID(uuid, fileName);
            if (file != null)
            {
                final CompoundNBT CompoundNBT = new CompoundNBT();
                data.writeToNBT(CompoundNBT);
                final CompoundNBT CompoundNBT1 = new CompoundNBT();
                CompoundNBT1.put("Data", CompoundNBT);
                try
                {
                    final FileOutputStream fileoutputstream = new FileOutputStream(file);
                    CompressedStreamTools.writeCompressed(CompoundNBT1, fileoutputstream);
                    fileoutputstream.close();
                }
                catch (final IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
