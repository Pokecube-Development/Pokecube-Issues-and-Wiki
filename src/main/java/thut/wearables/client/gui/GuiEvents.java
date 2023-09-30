package thut.wearables.client.gui;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ScreenEvent.Init;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import thut.lib.TComponent;
import thut.wearables.ThutWearables;
import thut.wearables.network.PacketGui;

public class GuiEvents
{

    public static Map<String, int[]> whitelistedGuis = Maps.newHashMap();

    static
    {
        GuiEvents.whitelistedGuis.put("net.minecraft.client.gui.inventory.GuiInventory", new int[2]);
    }

    public static void init()
    {
        MinecraftForge.EVENT_BUS.register(GuiEvents.class);
    }

    public static boolean active;

    @OnlyIn(value = Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void guiPostInit(final Init.Post event)
    {
        if (!ThutWearables.config.hasButton) return;
        if (ThutWearables.config.noButton) return;

        if (event.getScreen() instanceof InventoryScreen || event.getScreen() instanceof GuiWearables)
        {
            GuiEvents.active = false;
            if (event.getScreen() instanceof GuiWearables wear)
            {
                if (wear.getMenu().wearer != Minecraft.getInstance().player) return;
                GuiEvents.active = true;
            }
            final EffectRenderingInventoryScreen<?> gui = (EffectRenderingInventoryScreen<?>) event.getScreen();
            int x = gui.getGuiLeft() + ThutWearables.config.buttonPos.get(0);
            int y = gui.getGuiTop() + ThutWearables.config.buttonPos.get(1);
            final GuiWearableButton button = new GuiWearableButton(x, y, 9, 9,
                    TComponent.translatable(GuiEvents.active ? "button.wearables.off" : "button.wearables.on"),
                    b -> pressButton(gui), supplier -> TComponent
                            .translatable(GuiEvents.active ? "button.wearables.off" : "button.wearables.on"),
                    gui);
            event.getScreen().addRenderableWidget(button);
            button.setFGColor(0xFFFF00FF);
        }
        else if (event.getScreen() instanceof CreativeModeInventoryScreen gui)
        {
            GuiEvents.active = event.getScreen() instanceof GuiWearables;
            GuiWearableButton button = new GuiWearableButton(gui.getGuiLeft() + 43, gui.getGuiTop() + 9, 9, 9,
                    TComponent.translatable(GuiEvents.active ? "button.wearables.off" : "button.wearables.on"),
                    b -> pressButton(gui), supplier -> TComponent
                            .translatable(GuiEvents.active ? "button.wearables.off" : "button.wearables.on"),
                    gui);
            event.getScreen().addRenderableWidget(button);
            button.stillVisible = () -> gui.isInventoryOpen();
            button.setFGColor(0xFFFF00FF);
        }
    }

    private static void pressButton(final Screen gui)
    {
        final boolean close = gui instanceof GuiWearables;
        final PacketGui packet = new PacketGui();
        packet.data.putBoolean("close", close);
        ThutWearables.packets.sendToServer(packet);
    }
}
