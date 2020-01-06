package thut.wearables.client.gui;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
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
    public void guiPostAction(final GuiScreenEvent.ActionPerformedEvent.Post event)
    {
        System.out.println("test");
    }

    @OnlyIn(value = Dist.CLIENT)
    @SubscribeEvent
    public void guiPostInit(final GuiScreenEvent.InitGuiEvent.Post event)
    {
        if (!ThutWearables.hasButton) return;
        if (event.getGui() instanceof InventoryScreen || event.getGui() instanceof GuiWearables)
        {
            this.active = event.getGui() instanceof GuiWearables;
            final DisplayEffectsScreen<?> gui = (DisplayEffectsScreen<?>) event.getGui();
            final GuiWearableButton button;
            event.getGui().addButton(button = new GuiWearableButton(gui.getGuiLeft() + ThutWearables.buttonPos[0], gui
                    .getGuiTop() + ThutWearables.buttonPos[1], 9, 9, I18n.format(this.active ? "button.wearables.off"
                            : "button.wearables.on"), b -> this.pressButton(gui)));
            button.setFGColor(0xFFFF00FF);
        }
        else if (event.getGui() instanceof CreativeScreen)
        {
            final CreativeScreen gui = (CreativeScreen) event.getGui();
            this.active = event.getGui() instanceof GuiWearables;
            GuiWearableButton button;
            event.getGui().addButton(button = new GuiWearableButton(gui.getGuiLeft() + 37, gui.getGuiTop() + 9, 9, 9,
                    I18n.format(this.active ? "button.wearables.off" : "button.wearables.on"), b -> this.pressButton(
                            gui)));
            button.setFGColor(0xFFFF00FF);
            button.visible = button.active = gui.getSelectedTabIndex() == 11;
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
