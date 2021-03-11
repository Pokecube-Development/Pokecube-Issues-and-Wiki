package pokecube.core.client.gui.watch;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.PokecubeCore;
import pokecube.core.client.gui.helper.TexButton;
import pokecube.core.client.gui.helper.TexButton.UVImgRender;
import pokecube.core.client.gui.watch.progress.GlobalProgress;
import pokecube.core.client.gui.watch.progress.PerMobProgress;
import pokecube.core.client.gui.watch.progress.PerTypeProgress;
import pokecube.core.client.gui.watch.progress.Progress;
import pokecube.core.client.gui.watch.util.PageWithSubPages;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.network.packets.PacketPokedex;

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
            PokecubeCore.LOGGER.error("Error with making a page for watch", e);
            return null;
        }
    }

    public static final ResourceLocation TEX_DM = new ResourceLocation(PokecubeMod.ID,
            "textures/gui/pokewatchgui_trainer.png");
    public static final ResourceLocation TEX_NM = new ResourceLocation(PokecubeMod.ID,
            "textures/gui/pokewatchgui_trainer_nm.png");

    public ProgressPage(final GuiPokeWatch watch)
    {
        super(new TranslationTextComponent("pokewatch.progress.main.title"), watch, ProgressPage.TEX_DM,
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
    public void prePageDraw(final MatrixStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {
        final int x = (this.watch.width - GuiPokeWatch.GUIW) / 2;
        final int y = (this.watch.height - GuiPokeWatch.GUIH) / 2;
        final int colour = 0xFF78C850;
        AbstractGui.drawCenteredString(mat, this.font, this.getTitle().getString(), x + 135, y + 10, colour);
        AbstractGui.drawCenteredString(mat, this.font, this.current_page.getTitle().getString(), x + 135, y + 20,
                colour);

        PlayerEntity player = this.watch.player;
        if (this.watch.target instanceof PlayerEntity) player = (PlayerEntity) this.watch.target;
        AbstractGui.drawCenteredString(mat, this.font, player.getDisplayName().getString(), x + 135, y + 30, colour);
    }

    @Override
    public void onPageOpened()
    {
        super.onPageOpened();
        PacketPokedex.sendInspectPacket(false, Minecraft.getInstance().getLanguageManager().getSelected()
                .getCode());
    }

    @Override
    public void preSubOpened()
    {
        final int x = this.watch.width / 2;
        final int y = this.watch.height / 2 - 5;
        final ITextComponent next = new StringTextComponent(">");
        final ITextComponent prev = new StringTextComponent("<");
        final TexButton nextBtn = this.addButton(new TexButton(x + 90, y - 70, 12, 12, next, b ->
        {
            this.changePage(this.index + 1);
        }).setTex(GuiPokeWatch.getWidgetTex()).setRender(new UVImgRender(200, 0, 12, 12)));
        final TexButton prevBtn = this.addButton(new TexButton(x - 90, y - 70, 12, 12, prev, b ->
        {
            this.changePage(this.index - 1);
        }).setTex(GuiPokeWatch.getWidgetTex()).setRender(new UVImgRender(200, 0, 12, 12)));

        nextBtn.setFGColor(0x444444);
        prevBtn.setFGColor(0x444444);
    }
}
