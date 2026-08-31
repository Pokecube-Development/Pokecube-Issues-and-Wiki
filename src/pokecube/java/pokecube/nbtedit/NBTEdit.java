package pokecube.nbtedit;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import pokecube.nbtedit.forge.CommonProxy;
import pokecube.nbtedit.nbt.NBTNodeSorter;
import pokecube.nbtedit.nbt.NBTTree;
import pokecube.nbtedit.nbt.NamedNBT;
import pokecube.nbtedit.packets.PacketHandler;
import thut.core.common.config.Config.ConfigData;
import thut.core.common.config.Configure;
import thut.lib.DistExecutor;

public class NBTEdit
{
    public static class ConfigHolder extends ConfigData
    {
        @Configure(category = "misc")
        public boolean opOnly = true;

        public ConfigHolder()
        {
            super(NBTEdit.MODID);
        }

        @Override
        public void onUpdated()
        {
            NBTEdit.opOnly = this.opOnly;
        }

    }

    public static final String MODID = "pceditmod";

    public static final NBTNodeSorter SORTER = new NBTNodeSorter();
    public static final PacketHandler NETWORK = new PacketHandler();

    public static Logger LOGGER = LogManager.getLogger();
    public static NamedNBT clipboard = null;

    public static boolean opOnly = true;

    public static final CommonProxy proxy = DistExecutor.runForDist(() -> pokecube.nbtedit.forge.ClientProxy::new,
            () -> pokecube.nbtedit.forge.CommonProxy::new);

    public static final ConfigHolder config = new ConfigHolder();

    static final String SEP = System.lineSeparator();

    public static void log(final Level l, final String s)
    {
        NBTEdit.LOGGER.log(l, s);
    }

    public static void logTag(final CompoundTag tag)
    {
        final NBTTree tree = new NBTTree(tag);
        StringBuilder sb = new StringBuilder();
        for (final String s : tree.toStrings()) sb.append(NBTEdit.SEP).append("\t\t\t").append(s);
        NBTEdit.log(Level.TRACE, sb.toString());
    }

    public static void registerCommands(final RegisterCommandsEvent event)
    {
        CommandNBTEdit.register(event.getDispatcher());
    }

    public static void setup(final FMLCommonSetupEvent event)
    {
        // DISPATCHER.initialize();
        NBTEdit.NETWORK.initialize();
    }

    public static void setupClient(final FMLClientSetupEvent event)
    {
        NBTEdit.proxy.setupClient();
    }

    public static void throwing(final String cls, final String mthd, final Throwable thr)
    {
        NBTEdit.LOGGER.warn("class: {} method: {}", cls, mthd, thr);
    }
}
