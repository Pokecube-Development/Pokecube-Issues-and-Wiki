/**
 *
 */
package pokecube.core.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.client.GuiEvent;
import pokecube.core.client.Resources;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.commandhandlers.TeleportHandler;
import pokecube.core.network.pokemobs.PacketTeleport;
import pokecube.core.utils.PokeType;
import thut.api.entity.ThutTeleporter.TeleDest;

public class GuiTeleport extends AbstractGui
{
    protected static int      lightGrey = 0xDDDDDD;
    /**
     * This is made public incase an addon needs to replace it. Do not
     * reference this otherwise, always use instance()
     */
    public static GuiTeleport instance;

    /**
     * Whether the gui goes up or down.
     */
    public static int direction = 1;

    public static void create()
    {
        if (GuiTeleport.instance != null) MinecraftForge.EVENT_BUS.unregister(GuiTeleport.instance);
        GuiTeleport.instance = new GuiTeleport();
    }

    public static GuiTeleport instance()
    {
        if (GuiTeleport.instance == null) GuiTeleport.create();
        return GuiTeleport.instance;
    }

    protected FontRenderer fontRenderer;

    protected Minecraft minecraft;

    boolean state = false;

    /**
     *
     */
    private GuiTeleport()
    {
        this.minecraft = Minecraft.getInstance();
        MinecraftForge.EVENT_BUS.register(this);
        this.fontRenderer = this.minecraft.fontRenderer;
        GuiTeleport.instance = this;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void draw(final GuiEvent.RenderTeleports event)
    {
        if (!this.state) return;
        GuiDisplayPokecubeInfo.teleDims[0] = 89;
        GuiDisplayPokecubeInfo.teleDims[1] = 25;
        final IPokemob pokemob = GuiDisplayPokecubeInfo.instance().getCurrentPokemob();
        if (pokemob == null) return;

        event.mat.push();
        GuiDisplayPokecubeInfo.applyTransform(event.mat, PokecubeCore.getConfig().teleRef, PokecubeCore.getConfig().telePos,
                GuiDisplayPokecubeInfo.teleDims, (float) PokecubeCore.getConfig().teleSize);

        final int h = 0;
        final int w = 0;
        int i = 0;
        final int xOffset = 0;
        final int yOffset = 0;
        final int dir = GuiTeleport.direction;
        // bind texture
        this.minecraft.getTextureManager().bindTexture(Resources.GUI_BATTLE);
        this.blit(event.mat, xOffset + w, yOffset + h, 44, 0, 90, 13);
        this.fontRenderer.drawString(event.mat, I18n.format("gui.pokemob.teleport"), 2 + xOffset + w, 2 + yOffset + h,
                GuiTeleport.lightGrey);

        final TeleDest location = TeleportHandler.getTeleport(this.minecraft.player.getCachedUniqueIdString());
        if (location != null)
        {
            final String name = location.getName();
            int shift = 13 + 12 * i + yOffset + h;
            if (dir == -1) shift -= 25;
            // bind texture
            this.minecraft.getTextureManager().bindTexture(Resources.GUI_BATTLE);
            this.blit(event.mat, xOffset + w, shift, 44, 22, 91, 12);
            this.fontRenderer.drawString(event.mat, name, 5 + xOffset + w, shift + 2, PokeType.getType("fire").colour);
        }
        i++;
        event.mat.pop();

    }

    public boolean getState()
    {
        return this.state;
    }

    public void nextMove()
    {
        final String uuid = this.minecraft.player.getCachedUniqueIdString();
        final int index = TeleportHandler.getTeleIndex(uuid) + 1;
        TeleportHandler.setTeleIndex(uuid, index);
        PokecubeCore.packets.sendToServer(new PacketTeleport(index));
    }

    public void previousMove()
    {
        final String uuid = this.minecraft.player.getCachedUniqueIdString();
        final int index = TeleportHandler.getTeleIndex(uuid) - 1;
        TeleportHandler.setTeleIndex(uuid, index);
        PokecubeCore.packets.sendToServer(new PacketTeleport(index));
    }

    public void setState(final boolean state)
    {
        this.state = state;
    }
}
