package pokecube.wiki.config;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.wiki.WikiWriteMod;
import thut.core.common.ThutCore;
import thut.core.common.config.ConfigBase;

public class ModGuiConfig extends GuiConfig
{
    private static boolean          INIT    = false;
    private static List<ConfigBase> configs = Lists.newArrayList();
    private static List<String>     mods    = Lists.newArrayList();

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onConfigChangedEvent(OnConfigChangedEvent event)
    {
        if (event.getModID().equals(WikiWriteMod.MODID))
        {
            Map<String, ModContainer> containers = Loader.instance().getIndexedModList();
            ModContainer original = Loader.instance().activeModContainer();
            for (String modID : mods)
            {
                ModContainer container = containers.get(modID);
                Loader.instance().setActiveModContainer(container);
                event = new OnConfigChangedEvent(modID, event.getConfigID(), event.isWorldRunning(),
                        event.isRequiresMcRestart());
                MinecraftForge.EVENT_BUS.post(event);
            }
            Loader.instance().setActiveModContainer(original);
        }
    }

    private static List<IConfigElement> getConfigElements()
    {
        if (configs.isEmpty())
        {
            configs.add(PokecubeMod.core.getConfig());
            mods.add(PokecubeMod.ID);
            configs.add(ThutCore.instance.config);
            mods.add(ThutCore.modid);
            configs.add(PokecubeAdv.conf);
            mods.add(PokecubeAdv.ID);
        }
        List<IConfigElement> elements = Lists.newArrayList();
        for (ConfigBase config : configs)
            elements.addAll(ConfigBase.getConfigElements(config));
        return elements;
    }

    public ModGuiConfig(GuiScreen guiScreen)
    {
        super(guiScreen, getConfigElements(), WikiWriteMod.MODID, false, false, "pokecube_aio");
        if (!INIT)
        {
            INIT = true;
            MinecraftForge.EVENT_BUS.register(ModGuiConfig.class);
        }
    }
}
