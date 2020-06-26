package pokecube.nbtedit;

import java.io.File;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import pokecube.nbtedit.forge.ClientProxy;
import pokecube.nbtedit.forge.CommonProxy;
import pokecube.nbtedit.nbt.NBTNodeSorter;
import pokecube.nbtedit.nbt.NBTTree;
import pokecube.nbtedit.nbt.NamedNBT;
import pokecube.nbtedit.nbt.SaveStates;
import pokecube.nbtedit.packets.PacketHandler;
import thut.core.common.config.Config.ConfigData;
import thut.core.common.config.Configure;

public class NBTEdit
{
    public static class ConfigHolder extends ConfigData
    {
        @Configure(category = "misc")
        private final boolean opOnly = true;

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
    public static final String NAME  = "NBTEdit - Pokecube Edition";

    public static final String        VERSION = "1.0.0";
    public static final NBTNodeSorter SORTER  = new NBTNodeSorter();

    public static final PacketHandler NETWORK = new PacketHandler();

    public static Logger   LOGGER    = LogManager.getLogger();
    public static NamedNBT clipboard = null;

    public static boolean opOnly = true;

    public final static CommonProxy proxy = DistExecutor.safeRunForDist(
            () -> ClientProxy::new, () -> CommonProxy::new);

    public static final ConfigHolder config = new ConfigHolder();

    private static SaveStates saves;

    static final String SEP = System.getProperty("line.separator");

    public static SaveStates getSaveStates()
    {
        return NBTEdit.saves;
    }

    public static void log(final Level l, final String s)
    {
        NBTEdit.LOGGER.log(l, s);
    }

    public static void logTag(final CompoundNBT tag)
    {
        final NBTTree tree = new NBTTree(tag);
        String sb = "";
        for (final String s : tree.toStrings())
            sb += NBTEdit.SEP + "\t\t\t" + s;
        NBTEdit.log(Level.TRACE, sb);
    }

    @SubscribeEvent
    public static void serverStarting(final FMLServerStartingEvent event)
    {
        CommandNBTEdit.register(event.getCommandDispatcher());
        NBTEdit.LOGGER.trace("Server Starting -- Added \"/pcedit\" command");
    }

    public static void setup(final FMLCommonSetupEvent event)
    {
        NBTEdit.LOGGER.trace("NBTEdit Initalized");
        NBTEdit.saves = new SaveStates(new File(new File(FMLPaths.GAMEDIR.get().toFile(), "saves"), "NBTEditpqb.dat"));
        // DISPATCHER.initialize();
        NBTEdit.NETWORK.initialize();
    }

    public static void setupClient(final FMLClientSetupEvent event)
    {
        NBTEdit.proxy.setupClient();
    }

    public static void throwing(final String cls, final String mthd, final Throwable thr)
    {
        NBTEdit.LOGGER.warn("class: " + cls + " method: " + mthd, thr);
    }
}
