package thut.wearables.client.gui;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ScreenEvent.InitScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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
        MinecraftForge.EVENT_BUS.register(new GuiEvents());
    }

    public boolean active;

    public GuiEvents()
    {
    }

    @OnlyIn(value = Dist.CLIENT)
    @SubscribeEvent
    public void guiPostInit(final InitScreenEvent.Post event)
    {
        if (!ThutWearables.config.hasButton) return;
        if (ThutWearables.config.noButton) return;
        if (event.getScreen() instanceof InventoryScreen || event.getScreen() instanceof GuiWearables)
        {
            this.active = event.getScreen() instanceof GuiWearables;
            final EffectRenderingInventoryScreen<?> gui = (EffectRenderingInventoryScreen<?>) event.getScreen();
            final GuiWearableButton button;
            event.getScreen().addRenderableWidget(button = new GuiWearableButton(gui.getGuiLeft()
                    + ThutWearables.config.buttonPos.get(0), gui.getGuiTop() + ThutWearables.config.buttonPos.get(1), 9,
                    9, new TranslatableComponent(this.active ? "button.wearables.off" : "button.wearables.on"),
                    b -> this.pressButton(gui)));
            button.setFGColor(0xFFFF00FF);
        }
        else if (event.getScreen() instanceof CreativeModeInventoryScreen)
        {
            final CreativeModeInventoryScreen gui = (CreativeModeInventoryScreen) event.getScreen();
            this.active = event.getScreen() instanceof GuiWearables;
            GuiWearableButton button;
            event.getScreen().addRenderableWidget(button = new GuiWearableButton(gui.getGuiLeft() + 37, gui.getGuiTop()
                    + 9, 9, 9, new TranslatableComponent(this.active ? "button.wearables.off" : "button.wearables.on"),
                    b -> this.pressButton(gui)));
            button.setFGColor(0xFFFF00FF);
            button.visible = button.active = gui.getSelectedTab() == 11;
        }
    }

    private void pressButton(final Screen gui)
    {
        final boolean close = gui instanceof GuiWearables;
        final PacketGui packet = new PacketGui();
        packet.data.putBoolean("close", close);
        ThutWearables.packets.sendToServer(packet);
    }

}
