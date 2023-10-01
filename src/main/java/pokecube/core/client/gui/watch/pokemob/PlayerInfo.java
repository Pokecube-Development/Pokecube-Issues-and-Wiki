package pokecube.core.client.gui.watch.pokemob;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.client.gui.watch.StartWatch;

public class PlayerInfo extends PokeStartPage
{
    public static final ResourceLocation TEX_DM = GuiPokeWatch.makeWatchTexture("pokewatchgui_start");
    public static final ResourceLocation TEX_NM = GuiPokeWatch.makeWatchTexture("pokewatchgui_start_nm");

    public PlayerInfo(final StartWatch parent)
    {
        super(parent, "player", PlayerInfo.TEX_DM, PlayerInfo.TEX_NM);
    }

    @Override
    void drawInfo(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks)
    {
    	final int x = (this.watch.width - GuiPokeWatch.GUIW) / 2;
        final int y = (this.watch.height - GuiPokeWatch.GUIH) / 2;
        
        final int colour = ChatFormatting.GOLD.getColor();
        Player player = this.watch.player;
        if (this.watch.target instanceof Player) player = (Player) this.watch.target;
        
        //Name Player
        graphics.drawString(this.font, player.getDisplayName().getString(), x + 130, y + 129, colour, false);
    }
}
