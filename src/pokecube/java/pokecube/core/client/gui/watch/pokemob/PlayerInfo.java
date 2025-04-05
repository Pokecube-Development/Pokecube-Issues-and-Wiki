package pokecube.core.client.gui.watch.pokemob;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.client.gui.watch.StartWatch;
import thut.lib.TComponent;

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

        final int colour_red = ChatFormatting.DARK_RED.getColor();
        final int colour_blue = ChatFormatting.DARK_BLUE.getColor();
        Player player = this.watch.player;
        if (this.watch.target instanceof Player) player = (Player) this.watch.target;

        // Name Player
        final Component player_pokemob_red = TComponent.translatable("pokewatch.home.player_pokemob",
                player.getDisplayName(), this.parent.pokemob.getDisplayName()).withStyle(ChatFormatting.DARK_RED);
        final Component player_pokemob_blue = TComponent.translatable("pokewatch.home.player_pokemob",
                player.getDisplayName(), this.parent.pokemob.getDisplayName()).withStyle(ChatFormatting.DARK_BLUE);
        final Component pokemob_name = this.parent.pokemob.getDisplayName();

        if (this.parent.pokemob.isPlayerOwned())
        {
            if (GuiPokeWatch.nightMode)
                graphics.drawString(this.font, player_pokemob_blue, x + 130 - this.font.width(player_pokemob_blue) / 2, y + 129, colour_blue, false);
            else graphics.drawString(this.font, player_pokemob_red, x + 130 - this.font.width(player_pokemob_red) / 2, y + 129, colour_red, false);
        }
        else {
            if (GuiPokeWatch.nightMode)
                graphics.drawString(this.font, pokemob_name, x + 130 - this.font.width(pokemob_name) / 2, y + 129, colour_blue, false);
            else graphics.drawString(this.font, pokemob_name, x + 130 - this.font.width(pokemob_name) / 2, y + 129, colour_red, false);
        }
    }
}
