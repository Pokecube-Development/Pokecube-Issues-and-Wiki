package pokecube.core.client.gui.helper;

import java.util.Objects;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;

public class ScrollGui<T extends AbstractList.AbstractListEntry<T>> extends AbstractList<T>
{
    public boolean      smoothScroll    = false;
    private double      scrollAmount;
    public final Screen parent;
    public int          scrollBarOffset = -10;

    public ScrollGui(final Screen parent, final Minecraft mcIn, final int widthIn, final int heightIn,
            final int slotHeightIn, final int offsetX, final int offsetY)
    {
        super(mcIn, widthIn, slotHeightIn * (heightIn / slotHeightIn), offsetY, offsetY + slotHeightIn * (heightIn
                / slotHeightIn), slotHeightIn);
        this.y0 = offsetY;
        this.y1 = this.y0 + this.height;
        this.setLeftPos(offsetX);
        this.parent = parent;
        this.headerHeight = 0;
    }

    @Override
    /**
     * This override is to make this method public.
     */
    public int addEntry(final T p_addEntry_1_)
    {
        return super.addEntry(p_addEntry_1_);
    }

    private int getMaxScroll()
    {
        return Math.max(0, this.getMaxPosition() - (this.y1 - this.y0 - 4));
    }

    private int getRowBottom(final int index)
    {
        return this.getRowTop(index) + this.itemHeight;
    }

    @Override
    protected int getRowTop(final int index)
    {
        int top = super.getRowTop(index);
        // Move this such that it is definitely invalie.
        if (top < this.y0 + 4) top -= 5 * this.itemHeight;
        return top;
    }

    @Override
    /** Gets the width of the list */
    public int getRowWidth()
    {
        return this.width;
    }

    @Override
    public double getScrollAmount()
    {
        if (!this.smoothScroll) this.setScrollAmount(this.itemHeight * ((int) this.scrollAmount / this.itemHeight));
        return this.scrollAmount;
    }

    @Override
    protected int getScrollbarPosition()
    {
        return this.getRowLeft() + this.getRowWidth() + this.scrollBarOffset;
    }

    @Override
    public void render(final int mouseX, final int mouseY, final float tick)
    {
        this.renderBackground();
        final int i = this.getScrollbarPosition();
        final int j = i + 6;
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuffer();
        this.minecraft.getTextureManager().bindTexture(AbstractGui.BACKGROUND_LOCATION);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        final int k = this.getRowLeft();
        final int l = this.y0 - (int) this.getScrollAmount();
        if (this.renderHeader) this.renderHeader(k, l, tessellator);

        this.renderList(k, l, mouseX, mouseY, tick);
        GlStateManager.disableDepthTest();
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO,
                GlStateManager.DestFactor.ONE);
        GlStateManager.disableAlphaTest();
        GlStateManager.shadeModel(7425);
        GlStateManager.disableTexture();

        final int j1 = Math.max(0, this.getMaxPosition() - (this.y1 - this.y0));
        if (j1 > 0)
        {
            int k1 = (int) ((float) ((this.y1 - this.y0) * (this.y1 - this.y0)) / (float) this.getMaxPosition());
            k1 = MathHelper.clamp(k1, 32, this.y1 - this.y0 - 4);
            int l1 = (int) this.getScrollAmount() * (this.y1 - this.y0 - k1) / j1 + this.y0 + 4;
            if (l1 < this.y0) l1 = this.y0;
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            bufferbuilder.pos(i, l1 + k1, 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
            bufferbuilder.pos(j, l1 + k1, 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
            bufferbuilder.pos(j, l1, 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
            bufferbuilder.pos(i, l1, 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
            tessellator.draw();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            bufferbuilder.pos(i, l1 + k1 - 1, 0.0D).tex(0.0D, 1.0D).color(192, 192, 192, 255).endVertex();
            bufferbuilder.pos(j - 1, l1 + k1 - 1, 0.0D).tex(1.0D, 1.0D).color(192, 192, 192, 255).endVertex();
            bufferbuilder.pos(j - 1, l1, 0.0D).tex(1.0D, 0.0D).color(192, 192, 192, 255).endVertex();
            bufferbuilder.pos(i, l1, 0.0D).tex(0.0D, 0.0D).color(192, 192, 192, 255).endVertex();
            tessellator.draw();
        }

        this.renderDecorations(mouseX, mouseY);
        GlStateManager.enableTexture();
        GlStateManager.shadeModel(7424);
        GlStateManager.enableAlphaTest();
        GlStateManager.disableBlend();
    }

    @Override
    protected void renderHoleBackground(final int p_renderHoleBackground_1_, final int p_renderHoleBackground_2_,
            final int p_renderHoleBackground_3_, final int p_renderHoleBackground_4_)
    {
        // Nope
    }

    @Override
    protected void renderList(final int x, final int y, final int mouseX, final int mouseY, final float tick)
    {
        final int i = this.getItemCount();
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuffer();

        for (int j = 0; j < i; ++j)
        {
            final int k = this.getRowTop(j);
            final int l = this.getRowBottom(j);
            final T e = this.getEntry(j);
            final int i1 = y + j * this.itemHeight + this.headerHeight;
            final int j1 = this.itemHeight;
            final int k1 = this.getRowWidth();
            final int j2 = this.getRowLeft();
            if (e instanceof INotifiedEntry) ((INotifiedEntry) e).preRender(j, k, j2, k1, j1, mouseX, mouseY, this
                    .isMouseOver(mouseX, mouseY) && Objects.equals(this.getEntryAtPosition(mouseX, mouseY), e), tick);

            if (l >= this.y0 && k <= this.y1)
            {
                if (this.renderSelection && this.isSelectedItem(j))
                {
                    final int l1 = x + this.x0 + this.width / 2 - k1 / 2;
                    final int i2 = x + this.x0 + this.width / 2 + k1 / 2;
                    GlStateManager.disableTexture();
                    final float f = this.isFocused() ? 1.0F : 0.5F;
                    GlStateManager.color4f(f, f, f, 1.0F);
                    bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
                    bufferbuilder.pos(l1, i1 + j1 + 2, 0.0D).endVertex();
                    bufferbuilder.pos(i2, i1 + j1 + 2, 0.0D).endVertex();
                    bufferbuilder.pos(i2, i1 - 2, 0.0D).endVertex();
                    bufferbuilder.pos(l1, i1 - 2, 0.0D).endVertex();
                    tessellator.draw();
                    GlStateManager.color4f(0.0F, 0.0F, 0.0F, 1.0F);
                    bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
                    bufferbuilder.pos(l1 + 1, i1 + j1 + 1, 0.0D).endVertex();
                    bufferbuilder.pos(i2 - 1, i1 + j1 + 1, 0.0D).endVertex();
                    bufferbuilder.pos(i2 - 1, i1 - 1, 0.0D).endVertex();
                    bufferbuilder.pos(l1 + 1, i1 - 1, 0.0D).endVertex();
                    tessellator.draw();
                    GlStateManager.enableTexture();
                }
                e.render(j, k, j2, k1, j1, mouseX, mouseY, this.isMouseOver(mouseX, mouseY) && Objects.equals(this
                        .getEntryAtPosition(mouseX, mouseY), e), tick);
            }
        }
    }

    public void scroll(int ds)
    {
        if (!this.smoothScroll) ds = ds == 0 ? 0 : ds > 0 ? this.itemHeight : -this.itemHeight;
        this.setScrollAmount(this.getScrollAmount() + ds);
        this.yDrag = -2;
    }

    @Override
    public void setScrollAmount(double scroll)
    {
        if (!this.smoothScroll)
        {
            final double old = this.scrollAmount;
            double ds = scroll - old;
            ds = ds == 0 ? 0 : ds > 0 ? this.itemHeight : -this.itemHeight;
            scroll = old + ds;
            scroll = Math.min(scroll, this.getMaxScroll());
        }
        this.scrollAmount = MathHelper.clamp(scroll, 0.0D, this.getMaxScroll() - 4);
    }
}