package pokecube.core.client.gui.helper;

import java.util.Objects;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

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
    private boolean     checkedSmooth   = false;
    private double      scrollAmount;
    public final Screen parent;
    public int          scrollBarOffset = -10;

    public ScrollGui(final Screen parent, final Minecraft mcIn, final int widthIn, final int heightIn,
            final int slotHeightIn, final int offsetX, final int offsetY)
    {
        super(mcIn, widthIn, slotHeightIn * (heightIn / slotHeightIn), offsetY,
                offsetY + slotHeightIn * (heightIn / slotHeightIn), slotHeightIn);
        this.y0 = offsetY;
        this.y1 = this.y0 + this.height;
        this.setLeftPos(offsetX);
        this.parent = parent;
        this.headerHeight = 0;
        // No default background thing
        this.setRenderTopAndBottom(false);
    }

    @Override
    /** This override is to make this method public. */
    public int addEntry(final T p_addEntry_1_)
    {
        return super.addEntry(p_addEntry_1_);
    }

    @Override
    public int getMaxScroll()
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
        if (!this.smoothScroll && !this.checkedSmooth)
        {
            this.setScrollAmount(this.itemHeight * ((int) this.scrollAmount / this.itemHeight));
            this.checkedSmooth = true;
        }
        return this.scrollAmount;
    }

    @Override
    protected int getScrollbarPosition()
    {
        return this.getRowLeft() + this.getRowWidth() + this.scrollBarOffset;
    }

    @Override
    public void render(final MatrixStack mat, final int mouseX, final int mouseY, final float tick)
    {
        this.renderBackground(mat);

        final int i = this.getScrollbarPosition();
        final int j = i + 6;
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuilder();
        this.minecraft.getTextureManager().bind(AbstractGui.BACKGROUND_LOCATION);
        final int k = this.getRowLeft();
        final int l = this.y0 + 4 - (int) this.getScrollAmount();
        if (this.renderHeader) this.renderHeader(mat, k, l, tessellator);

        this.renderList(mat, k, l, mouseX, mouseY, tick);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO,
                GlStateManager.DestFactor.ONE);
        RenderSystem.disableTexture();

        final int j1 = this.getMaxScroll();
        if (j1 > 0)
        {
            int k1 = (int) ((float) ((this.y1 - this.y0) * (this.y1 - this.y0)) / (float) this.getMaxPosition());
            k1 = MathHelper.clamp(k1, 32, this.y1 - this.y0 - 8);
            int l1 = (int) this.getScrollAmount() * (this.y1 - this.y0 - k1) / j1 + this.y0;
            if (l1 < this.y0) l1 = this.y0;

            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
            bufferbuilder.vertex(i, this.y1, 0.0D).color(0, 0, 0, 255).uv(0.0F, 1.0F).endVertex();
            bufferbuilder.vertex(j, this.y1, 0.0D).color(0, 0, 0, 255).uv(1.0F, 1.0F).endVertex();
            bufferbuilder.vertex(j, this.y0, 0.0D).color(0, 0, 0, 255).uv(1.0F, 0.0F).endVertex();
            bufferbuilder.vertex(i, this.y0, 0.0D).color(0, 0, 0, 255).uv(0.0F, 0.0F).endVertex();
            tessellator.end();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
            bufferbuilder.vertex(i, l1 + k1, 0.0D).color(128, 128, 128, 255).uv(0.0F, 1.0F).endVertex();
            bufferbuilder.vertex(j, l1 + k1, 0.0D).color(128, 128, 128, 255).uv(1.0F, 1.0F).endVertex();
            bufferbuilder.vertex(j, l1, 0.0D).color(128, 128, 128, 255).uv(1.0F, 0.0F).endVertex();
            bufferbuilder.vertex(i, l1, 0.0D).color(128, 128, 128, 255).uv(0.0F, 0.0F).endVertex();
            tessellator.end();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
            bufferbuilder.vertex(i, l1 + k1 - 1, 0.0D).color(192, 192, 192, 255).uv(0.0F, 1.0F).endVertex();
            bufferbuilder.vertex(j - 1, l1 + k1 - 1, 0.0D).color(192, 192, 192, 255).uv(1.0F, 1.0F).endVertex();
            bufferbuilder.vertex(j - 1, l1, 0.0D).color(192, 192, 192, 255).uv(1.0F, 0.0F).endVertex();
            bufferbuilder.vertex(i, l1, 0.0D).color(192, 192, 192, 255).uv(0.0F, 0.0F).endVertex();
            tessellator.end();
        }

        this.renderDecorations(mat, mouseX, mouseY);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderList(final MatrixStack mat, final int x, final int y, final int mouseX, final int mouseY, final float tick)
    {
        final int i = this.getItemCount();
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuilder();

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
                    RenderSystem.disableTexture();
                    final float f = this.isFocused() ? 1.0F : 0.5F;
                    bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
                    bufferbuilder.vertex(l1, i1 + j1 + 2, 0.0D).color(f, f, f, 1).endVertex();
                    bufferbuilder.vertex(i2, i1 + j1 + 2, 0.0D).color(f, f, f, 1).endVertex();
                    bufferbuilder.vertex(i2, i1 - 2, 0.0D).color(f, f, f, 1).endVertex();
                    bufferbuilder.vertex(l1, i1 - 2, 0.0D).color(f, f, f, 1).endVertex();
                    tessellator.end();
                    bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
                    bufferbuilder.vertex(l1 + 1, i1 + j1 + 1, 0.0D).color(0, 0, 0, 1f).endVertex();
                    bufferbuilder.vertex(i2 - 1, i1 + j1 + 1, 0.0D).color(0, 0, 0, 1f).endVertex();
                    bufferbuilder.vertex(i2 - 1, i1 - 1, 0.0D).color(0, 0, 0, 1f).endVertex();
                    bufferbuilder.vertex(l1 + 1, i1 - 1, 0.0D).color(0, 0, 0, 1f).endVertex();
                    tessellator.end();
                    RenderSystem.enableTexture();
                }
                e.render(mat, j, k, j2, k1, j1, mouseX, mouseY,
                        this.isMouseOver(mouseX, mouseY) && Objects.equals(this.getEntryAtPosition(mouseX, mouseY), e),
                        tick);
            }
        }
    }

    public void scroll(int ds)
    {
        if (!this.smoothScroll) ds = ds == 0 ? 0 : ds > 0 ? this.itemHeight : -this.itemHeight;
        this.setScrollAmount(this.getScrollAmount() + ds);
    }

    @Override
    public boolean keyPressed(final int keyCode, final int b, final int c)
    {
        for (final T value : this.children())
            if (value.keyPressed(keyCode, b, c)) return true;
        return super.keyPressed(keyCode, b, c);
    }

    @Override
    public void setScrollAmount(double scroll)
    {
        if (!this.smoothScroll)
        {
            this.checkedSmooth = false;
            final double old = this.scrollAmount;
            double ds = scroll - old;
            ds = ds == 0 ? 0 : ds > 0 ? this.itemHeight : -this.itemHeight;
            scroll = old + ds;
            scroll = Math.min(scroll, this.getMaxScroll());
        }
        this.scrollAmount = MathHelper.clamp(scroll, 0.0D, this.getMaxScroll() - 4);
    }

    public void skipTo(final double scroll)
    {
        this.scrollAmount = MathHelper.clamp(scroll, 0.0D, this.getMaxScroll() - 4);
    }

    public int itemHeight()
    {
        return this.itemHeight;
    }

    @Override
    /**
     * This override is to make it public
     */
    public T getEntry(final int index)
    {
        return super.getEntry(index);
    }

    public int getSize()
    {
        return this.getItemCount();
    }
}