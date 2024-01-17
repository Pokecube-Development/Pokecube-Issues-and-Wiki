package pokecube.core.client.gui.watch;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
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

    public static final ResourceLocation TEX_DM = GuiPokeWatch.makeWatchTexture("pokewatchgui_wiki");
    public static final ResourceLocation TEX_NM = GuiPokeWatch.makeWatchTexture("pokewatchgui_wiki_nm");

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
    public void prePageDraw(final PoseStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {
        final int x = (this.watch.width - GuiPokeWatch.GUIW) / 2;
        final int y = (this.watch.height - GuiPokeWatch.GUIH) / 2;
        final int colour = 0xFF78C850;
        GuiComponent.drawCenteredString(mat, this.font, this.current_page.getTitle().getString(), x + 130, y + 5,
                colour);
        GuiComponent.drawCenteredString(mat, this.font, this.getTitle().getString(), x + 130, y + 18, colour);

        final int title_colour = 0x185100;
        Player player = this.watch.player;
        if (this.watch.target instanceof Player) player = (Player) this.watch.target;
        var title = player.getDisplayName();
        this.font.draw(mat, title, x + 130 - this.font.width(title) / 2, y + 36, title_colour);
    }

    @Override
    public void onPageOpened()
    {
        super.onPageOpened();
        final int x = (this.watch.width - GuiPokeWatch.GUIW) / 2 + 90;
        final int y = (this.watch.height - GuiPokeWatch.GUIH) / 2 + 30;

        PacketPokedex.sendInspectPacket(false, Minecraft.getInstance().getLanguageManager().getSelected().getCode());

        this.addRenderableWidget(new TexButton(x - 108, y + 102, 17, 17,
                TComponent.literal(""), b ->
        {
            GuiPokeWatch.nightMode = !GuiPokeWatch.nightMode;
            this.watch.init();
        }).setTex(GuiPokeWatch.getWidgetTex()).setRender(new TexButton.UVImgRender(110, 72, 17, 17)));
    }

    @Override
    public void preSubOpened()
    {
        final int x = this.watch.width / 2;
        final int y = this.watch.height / 2;
        final Component next = TComponent.literal("");
        final Component prev = TComponent.literal("");

        final TexButton prevBtn = this.addRenderableWidget(new TexButton(x - 116, y - 79, 12, 12, prev, b -> {
            this.changePage(this.index - 1);
        }).setTex(GuiPokeWatch.getWidgetTex()).setRender(new UVImgRender(229, 108, 12, 12)));

        final TexButton nextBtn = this.addRenderableWidget(new TexButton(x + 104, y - 79, 12, 12, next, b -> {
            this.changePage(this.index + 1);
        }).setTex(GuiPokeWatch.getWidgetTex()).setRender(new UVImgRender(241, 108, 12, 12)));

        nextBtn.setFGColor(0x444444);
        prevBtn.setFGColor(0x444444);
    }
}
