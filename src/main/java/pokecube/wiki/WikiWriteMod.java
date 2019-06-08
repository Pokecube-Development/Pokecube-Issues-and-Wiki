package pokecube.wiki;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.api.distmarker.Dist;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.mobs.PokecubeMobs;
import thut.core.common.ThutCore;

@Mod(modid = WikiWriteMod.MODID, name = "Pokecube AIO", version = WikiWriteMod.VERSION, dependencies = "required-before:pokecube", acceptableRemoteVersions = WikiWriteMod.MINVERSION, acceptedMinecraftVersions = WikiWriteMod.MCVERSIONS, guiFactory = "pokecube.wiki.config.ModGuiFactory")
public class WikiWriteMod
{

    Map<PokedexEntry, Integer> genMap     = Maps.newHashMap();
    public static final String MODID      = "pokecube_aio";
    public static final String VERSION    = "@VERSION";
    public static final String MINVERSION = "@MINVERSION";

    public final static String MCVERSIONS = "*";

    @Instance(value = MODID)
    public static WikiWriteMod instance;

    private void doMetastuff()
    {
        Map<String, ModContainer> containers = Loader.instance().getIndexedModList();
        containers.get(PokecubeMod.ID).getMetadata().parent = MODID;
        containers.get(ThutCore.modid).getMetadata().parent = MODID;
        containers.get(PokecubeAdv.ID).getMetadata().parent = PokecubeMod.ID;
        containers.get(PokecubeMobs.MODID).getMetadata().parent = PokecubeMod.ID;

        if (containers.containsKey("thut_wearables"))
        {
            containers.get("thut_wearables").getMetadata().parent = MODID;
            if (containers.containsKey("thut_bling"))
                containers.get("thut_bling").getMetadata().parent = "thut_wearables";
        }
        if (containers.containsKey("pokeplayer")) containers.get("pokeplayer").getMetadata().parent = PokecubeMod.ID;

    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        if (event.getSide() == Side.CLIENT)
        {
            MinecraftForge.EVENT_BUS.register(this);
        }
        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);

        doMetastuff();
    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandBase()
        {
            @Override
            public String getUsage(ICommandSender sender)
            {
                return "/pokewiki stuff";
            }

            @Override
            public String getName()
            {
                return "pokewiki";
            }

            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
            {
                EntityPlayer player = getCommandSenderAsPlayer(sender);
                if (args.length == 1 && args[0].equals("all"))
                {
                    sender.sendMessage(new TextComponentString("Starting Wiki output"));
                    PokecubeWikiWriter.writeWiki();
                    sender.sendMessage(new TextComponentString("Finished Wiki output"));
                }
                else if (args.length >= 2 && args[0].startsWith("img"))
                {
                    boolean shiny = args.length == 3 && args[2].equalsIgnoreCase("S");
                    boolean all = args[1].equalsIgnoreCase("all");
                    if (server instanceof IntegratedServer)
                    {
                        GuiGifCapture.icon = args[0].endsWith("i");
                        GuiGifCapture.shiny = shiny;
                        PokedexEntry init = Pokedex.getInstance().getFirstEntry();
                        if (all)
                        {

                        }
                        else
                        {
                            try
                            {
                                init = Database.getEntry(args[1]);
                            }
                            catch (Exception e)
                            {
                                init = Database.getEntry(CommandBase.parseInt(args[1]));
                            }
                        }
                        if (init == null) throw new CommandException("Error in pokedex entry for " + args[1]);
                        PokemobImageWriter.one = !all;
                        PokemobImageWriter.gifs = false;
                        PokemobImageWriter.beginGifCapture();
                        GuiGifCapture.pokedexEntry = init;
                        Minecraft.getMinecraft().player.openGui(instance, 0, player.getEntityWorld(), 0, 0, 0);
                    }
                }
            }
        });
    }

    @SidedProxy
    public static CommonProxy proxy;

    public static class CommonProxy implements IGuiHandler
    {
        void setupModels()
        {
        }

        @Override
        public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
        {
            return null;
        }

        @Override
        public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
        {
            return null;
        }
    }

    public static class ServerProxy extends CommonProxy
    {
    }

    public static class ClientProxy extends CommonProxy
    {
        @Override
        public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
        {
            return new GuiGifCapture(null, player);
        }
    }
}
