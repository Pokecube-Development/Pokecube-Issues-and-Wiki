package pokecube.core.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.commandhandlers.TeleportHandler;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;
import pokecube.core.client.GuiEvent;
import pokecube.core.client.gui.GuiDisplayPokecubeInfo;
import pokecube.core.client.gui.GuiTeleport;
import pokecube.core.utils.Resources;
import thut.api.entity.teleporting.TeleDest;

public class TeleportInfo extends GuiEventComponent
{
    @Override
    protected void onMovedGui()
    {
        PokecubeCore.getConfig().guiTeleportPos.set(0, this.bounds.x0);
        PokecubeCore.getConfig().guiTeleportPos.set(1, this.bounds.y0);
        super.onMovedGui();
    }

    @Override
    protected void preDraw(GuiEvent event)
    {
        if (clickA == 0)
        {
            int x0 = PokecubeCore.getConfig().guiTeleportPos.get(0);
            int y0 = PokecubeCore.getConfig().guiTeleportPos.get(1);
            if (x0 != bounds.x0 || y0 != bounds.y0 || bounds.h == 0) this.bounds.setBox(x0, y0, 89, 64);
            this.ref = PokecubeCore.getConfig().teleRef;
        }
    }

    @Override
    public void _drawGui(GuiEvent event)
    {
        if (!GuiTeleport.instance().state) return;
        final IPokemob pokemob = GuiDisplayPokecubeInfo.instance().getCurrentPokemob();
        if (pokemob == null) return;
        var gui = event.getGui();

        event.getMat().pushPose();
        event.getMat().translate(this.pos.x0, this.pos.y0, 0);

        final int h = 0;
        final int w = 0;
        int i = 0;
        final int xOffset = 0;
        final int yOffset = 0;
        final int dir = GuiTeleport.direction;
        // bind texture
        RenderSystem.setShaderTexture(0, Resources.GUI_BATTLE);
        RenderSystem.enableBlend();
        gui.blit(event.getMat(), xOffset + w, yOffset + h, 44, 0, 90, 13);
        gui.getFont().draw(event.getMat(), I18n.get("gui.pokemob.teleport"), 2 + xOffset + w, 2 + yOffset + h,
                GuiTeleport.lightGrey);

        final TeleDest location = TeleportHandler.getTeleport(Minecraft.getInstance().player.getStringUUID());
        if (location != null)
        {
            String name = location.getName();
            if (name.isEmpty()) name = location.getInfoName().getString();
            int shift = 13 + 12 * i + yOffset + h;
            if (dir == -1) shift -= 25;
            // bind texture
            RenderSystem.setShaderTexture(0, Resources.GUI_BATTLE);
            RenderSystem.enableBlend();
            gui.blit(event.getMat(), xOffset + w, shift, 44, 22, 91, 12);
            gui.getFont().draw(event.getMat(), name, 5 + xOffset + w, shift + 2, PokeType.getType("fire").colour);
        }
        i++;
        event.getMat().popPose();
    }

}