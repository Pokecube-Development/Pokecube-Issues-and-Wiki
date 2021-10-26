package pokecube.core.client.gui.watch.util;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import pokecube.core.client.gui.watch.GuiPokeWatch;

public abstract class WatchPage extends Screen implements GuiEventListener
{
    public final GuiPokeWatch watch;
    private final Component   title;

    private final ResourceLocation tex_dm;
    private final ResourceLocation tex_nm;

    public WatchPage(final Component title, final GuiPokeWatch watch, final ResourceLocation day,
            final ResourceLocation night)
    {
        super(title);
        this.title = title;
        this.watch = watch;
        this.minecraft = Minecraft.getInstance();
        this.font = this.minecraft.font;
        this.tex_dm = day;
        this.tex_nm = night;
    }

    protected ResourceLocation getBackgroundTex()
    {
        return GuiPokeWatch.nightMode ? this.tex_nm : this.tex_dm;
    }

    @Override
    public void renderBackground(final PoseStack matrixStack)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.getBackgroundTex());

        final int j2 = (this.watch.width - GuiPokeWatch.GUIW) / 2;
        final int k2 = (this.watch.height - GuiPokeWatch.GUIH) / 2;
        this.blit(matrixStack, j2, k2, 0, 0, GuiPokeWatch.GUIW, GuiPokeWatch.GUIH);
    }

    @Override
    public Component getTitle()
    {
        return this.title;
    }

    @Override
    /**
     * This is made public here to make it accessible for others.
     */
    public void init()
    {
        this.onPageClosed();
        super.init();
    }

    public void onPageClosed()
    {
        this.watch.children().remove(this);
    }

    @Override
    public boolean keyPressed(final int keyCode, final int b, final int c)
    {
        // We overwrite this to reverse the ordering of checking if tab was
        // pressed
        final boolean subpages = this.getFocused() != null && this.getFocused().keyPressed(keyCode, b, c);
        if (subpages) return true;
        if (keyCode == GLFW.GLFW_KEY_TAB)
        {
            final boolean flag = !Screen.hasShiftDown();
            if (!this.changeFocus(flag)) this.changeFocus(flag);

            return true;
        }
        return false;
    }

    public void onPageOpened()
    {
        @SuppressWarnings("unchecked")
        final List<GuiEventListener> list = (List<GuiEventListener>) this.watch.children();
        list.add(this);
    }
}
