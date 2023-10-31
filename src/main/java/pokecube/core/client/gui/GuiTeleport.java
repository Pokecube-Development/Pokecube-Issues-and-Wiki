/**
 *
 */
package pokecube.core.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.api.entity.pokemob.commandhandlers.TeleportHandler;
import pokecube.core.PokecubeCore;
import pokecube.core.client.GuiEvent;
import pokecube.core.network.pokemobs.PacketTeleport;
import thut.core.common.ThutCore;

public class GuiTeleport extends GuiComponent
{
    public static int lightGrey = 0xDDDDDD;
    /**
     * This is made public incase an addon needs to replace it. Do not reference
     * this otherwise, always use instance()
     */
    public static GuiTeleport instance;

    /**
     * Whether the gui goes up or down.
     */
    public static int direction = 1;

    public static void create()
    {
        if (GuiTeleport.instance != null) ThutCore.FORGE_BUS.unregister(GuiTeleport.instance);
        GuiTeleport.instance = new GuiTeleport();
    }

    public static GuiTeleport instance()
    {
        if (GuiTeleport.instance == null) GuiTeleport.create();
        return GuiTeleport.instance;
    }

    protected Font fontRenderer;

    protected Minecraft minecraft;

    public boolean state = false;

    /**
     *
     */
    private GuiTeleport()
    {
        this.minecraft = Minecraft.getInstance();
        ThutCore.FORGE_BUS.register(this);
        this.fontRenderer = this.minecraft.font;
        GuiTeleport.instance = this;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void draw(final GuiEvent.RenderTeleports event)
    {


    }

    public boolean getState()
    {
        return this.state;
    }

    public void nextMove()
    {
        final String uuid = this.minecraft.player.getStringUUID();
        final int index = TeleportHandler.getTeleIndex(uuid) + 1;
        TeleportHandler.setTeleIndex(uuid, index);
        PokecubeCore.packets.sendToServer(new PacketTeleport(index));
    }

    public void previousMove()
    {
        final String uuid = this.minecraft.player.getStringUUID();
        final int index = TeleportHandler.getTeleIndex(uuid) - 1;
        TeleportHandler.setTeleIndex(uuid, index);
        PokecubeCore.packets.sendToServer(new PacketTeleport(index));
    }

    public void setState(final boolean state)
    {
        this.state = state;
    }
}
