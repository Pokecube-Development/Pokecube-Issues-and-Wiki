package thut.wearables.client.gui;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ScreenEvent.InitScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import thut.core.common.ThutCore;
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
        ThutCore.FORGE_BUS.register(GuiEvents.class);
    }

    public static boolean active;

    @OnlyIn(value = Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void guiPostInit(final InitScreenEvent.Post event)
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
            final GuiWearableButton button;
            int x = gui.getGuiLeft() + ThutWearables.config.buttonPos.get(0);
            int y = gui.getGuiTop() + ThutWearables.config.buttonPos.get(1);
            event.getScreen()
                    .addRenderableWidget(button = new GuiWearableButton(x, y, 9, 9,
                            TComponent.translatable(GuiEvents.active ? "button.wearables.off" : "button.wearables.on"),
                            b -> pressButton(gui), gui));
            button.setFGColor(0xFFFF00FF);
        }
        else if (event.getScreen() instanceof CreativeModeInventoryScreen gui)
        {
            GuiEvents.active = event.getScreen() instanceof GuiWearables;
            GuiWearableButton button;
            event.getScreen().addRenderableWidget(
                    button = new GuiWearableButton(gui.getGuiLeft() + 43, gui.getGuiTop() + 9, 9, 9,
                            TComponent.translatable(GuiEvents.active ? "button.wearables.off" : "button.wearables.on"),
                            b -> pressButton(gui), gui));
            button.stillVisible = () -> gui.getSelectedTab() == CreativeModeTab.TAB_INVENTORY.getId();
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
