package pokecube.core.client.gui.watch;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import pokecube.api.PokecubeAPI;
import pokecube.core.client.gui.helper.TexButton;
import pokecube.core.client.gui.helper.TexButton.UVImgRender;
import pokecube.core.client.gui.watch.progress.GlobalProgress;
import pokecube.core.client.gui.watch.progress.PerMobProgress;
import pokecube.core.client.gui.watch.progress.PerTypeProgress;
import pokecube.core.client.gui.watch.progress.Progress;
import pokecube.core.client.gui.watch.util.PageWithSubPages;
import pokecube.core.network.packets.PacketPokedex;
import thut.lib.TComponent;

public class ProgressPage extends PageWithSubPages<Progress>
{
    public static List<Class<? extends Progress>> PAGELIST = Lists.newArrayList();

    static
    {
        ProgressPage.PAGELIST.add(GlobalProgress.class);
        ProgressPage.PAGELIST.add(PerTypeProgress.class);
        ProgressPage.PAGELIST.add(PerMobProgress.class);
    }

    private static Progress makePage(final Class<? extends Progress> clazz, final GuiPokeWatch parent)
    {
        try
        {
            return clazz.getConstructor(GuiPokeWatch.class).newInstance(parent);
        }
        catch (final Exception e)
        {
            PokecubeAPI.LOGGER.error("Error with making a page for watch", e);
            return null;
        }
    }

    public static final ResourceLocation TEX_DM = GuiPokeWatch.makeWatchTexture("pokewatchgui_trainer");
    public static final ResourceLocation TEX_NM = GuiPokeWatch.makeWatchTexture("pokewatchgui_trainer_nm");

    public ProgressPage(final GuiPokeWatch watch)
    {
        super(TComponent.translatable("pokewatch.progress.main.title"), watch, ProgressPage.TEX_DM,
                ProgressPage.TEX_NM);
    }

    @Override
    protected Progress createPage(final int index)
    {
        return ProgressPage.makePage(ProgressPage.PAGELIST.get(index), this.watch);
    }

    @Override
    protected int pageCount()
    {
        return ProgressPage.PAGELIST.size();
    }

    @Override
    public void prePageDraw(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks)
    {
        final int x = (this.watch.width - GuiPokeWatch.GUIW) / 2;
        final int y = (this.watch.height - GuiPokeWatch.GUIH) / 2;
        final int colour = 0xFF78C850;
        graphics.drawCenteredString(this.font, this.getTitle().getString(), x + 135, y + 10, colour);
        graphics.drawCenteredString(this.font, this.current_page.getTitle().getString(), x + 135, y + 20,
                colour);

        Player player = this.watch.player;
        if (this.watch.target instanceof Player) player = (Player) this.watch.target;
        graphics.drawCenteredString(this.font, player.getDisplayName().getString(), x + 135, y + 30, colour);
    }

    @Override
    public void onPageOpened()
    {
        super.onPageOpened();
        // TODO: Check for .code() replacement
        PacketPokedex.sendInspectPacket(false, Minecraft.getInstance().getLanguageManager().getSelected()/*.code()*/);
    }

    @Override
    public void preSubOpened()
    {
        final int x = this.watch.width / 2;
        final int y = this.watch.height / 2 - 5;
        final Component next = TComponent.literal(">");
        final Component prev = TComponent.literal("<");

        // TODO: Check this
        final TexButton nextBtn = this.addRenderableWidget(new TexButton.Builder(next, (b) -> {
            this.changePage(this.index + 1);
        }).bounds(x + 90, y - 70, 12, 12).setTex(GuiPokeWatch.getWidgetTex())
                .setRender(new UVImgRender(200, 0, 12, 12)).build());

        final TexButton prevBtn = this.addRenderableWidget(new TexButton.Builder(prev, (b) -> {
            this.changePage(this.index - 1);
        }).bounds(x - 90, y - 70, 12, 12).setTex(GuiPokeWatch.getWidgetTex())
                .setRender(new UVImgRender(200, 0, 12, 12)).build());

        nextBtn.setFGColor(0x444444);
        prevBtn.setFGColor(0x444444);
    }
}
