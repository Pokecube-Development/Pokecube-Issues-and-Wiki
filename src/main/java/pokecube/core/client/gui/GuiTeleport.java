/**
 *
 */
package pokecube.core.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.resources.language.I18n;
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

public class GuiTeleport extends GuiComponent
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

    protected Font fontRenderer;

    protected Minecraft minecraft;

    boolean state = false;

    /**
     *
     */
    private GuiTeleport()
    {
        this.minecraft = Minecraft.getInstance();
        MinecraftForge.EVENT_BUS.register(this);
        this.fontRenderer = this.minecraft.font;
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

        event.getMat().pushPose();
        GuiDisplayPokecubeInfo.applyTransform(event.getMat(), PokecubeCore.getConfig().teleRef, PokecubeCore.getConfig().telePos,
                GuiDisplayPokecubeInfo.teleDims, (float) PokecubeCore.getConfig().teleSize);

        final int h = 0;
        final int w = 0;
        int i = 0;
        final int xOffset = 0;
        final int yOffset = 0;
        final int dir = GuiTeleport.direction;
        // bind texture
        RenderSystem.setShaderTexture(0, Resources.GUI_BATTLE);
        RenderSystem.enableBlend();
        this.blit(event.getMat(), xOffset + w, yOffset + h, 44, 0, 90, 13);
        this.fontRenderer.draw(event.getMat(), I18n.get("gui.pokemob.teleport"), 2 + xOffset + w, 2 + yOffset + h,
                GuiTeleport.lightGrey);

        final TeleDest location = TeleportHandler.getTeleport(this.minecraft.player.getStringUUID());
        if (location != null)
        {
            String name = location.getName();
            if(name.isEmpty()) name = location.getInfoName().getString();
            int shift = 13 + 12 * i + yOffset + h;
            if (dir == -1) shift -= 25;
            // bind texture
            RenderSystem.setShaderTexture(0, Resources.GUI_BATTLE);
            RenderSystem.enableBlend();
            this.blit(event.getMat(), xOffset + w, shift, 44, 22, 91, 12);
            this.fontRenderer.draw(event.getMat(), name, 5 + xOffset + w, shift + 2, PokeType.getType("fire").colour);
        }
        i++;
        event.getMat().popPose();

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
