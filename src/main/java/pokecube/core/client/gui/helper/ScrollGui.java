package pokecube.core.client.gui.helper;

import java.util.Objects;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;

public class ScrollGui<T extends AbstractSelectionList.Entry<T>> extends AbstractSelectionList<T>
{
    public boolean      smoothScroll    = false;
    private boolean     checkedSmooth   = false;
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
    public void render(final PoseStack mat, final int mouseX, final int mouseY, final float tick)
    {
        this.renderBackground(mat);

        final int i = this.getScrollbarPosition();
        final int j = i + 6;
        final Tesselator tessellator = Tesselator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuilder();
        RenderSystem.setShaderTexture(0, GuiComponent.BACKGROUND_LOCATION);
        final int k = this.getRowLeft();
        final int l = this.y0 + 4 - (int) this.getScrollAmount();
        if (this.renderHeader) this.renderHeader(mat, k, l, tessellator);

        this.renderList(mat, k, l, mouseX, mouseY, tick);

        final int k1 = this.getMaxScroll();
        if (k1 > 0) {
           RenderSystem.disableTexture();
           RenderSystem.setShader(GameRenderer::getPositionColorShader);
           int l1 = (int)((float)((this.y1 - this.y0) * (this.y1 - this.y0)) / (float)this.getMaxPosition());
           l1 = Mth.clamp(l1, 32, this.y1 - this.y0 - 8);
           int i2 = (int)this.getScrollAmount() * (this.y1 - this.y0 - l1) / k1 + this.y0;
           if (i2 < this.y0) i2 = this.y0;

           bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
           bufferbuilder.vertex(i, this.y1, 0.0D).color(0, 0, 0, 255).endVertex();
           bufferbuilder.vertex(j, this.y1, 0.0D).color(0, 0, 0, 255).endVertex();
           bufferbuilder.vertex(j, this.y0, 0.0D).color(0, 0, 0, 255).endVertex();
           bufferbuilder.vertex(i, this.y0, 0.0D).color(0, 0, 0, 255).endVertex();
           bufferbuilder.vertex(i, i2 + l1, 0.0D).color(128, 128, 128, 255).endVertex();
           bufferbuilder.vertex(j, i2 + l1, 0.0D).color(128, 128, 128, 255).endVertex();
           bufferbuilder.vertex(j, i2, 0.0D).color(128, 128, 128, 255).endVertex();
           bufferbuilder.vertex(i, i2, 0.0D).color(128, 128, 128, 255).endVertex();
           bufferbuilder.vertex(i, i2 + l1 - 1, 0.0D).color(192, 192, 192, 255).endVertex();
           bufferbuilder.vertex(j - 1, i2 + l1 - 1, 0.0D).color(192, 192, 192, 255).endVertex();
           bufferbuilder.vertex(j - 1, i2, 0.0D).color(192, 192, 192, 255).endVertex();
           bufferbuilder.vertex(i, i2, 0.0D).color(192, 192, 192, 255).endVertex();
           tessellator.end();
        }

        this.renderDecorations(mat, mouseX, mouseY);
    }

    @Override
    protected void renderList(final PoseStack mat, final int x, final int y, final int mouseX, final int mouseY,
            final float tick)
    {
        final int i = this.getItemCount();
        final Tesselator tessellator = Tesselator.getInstance();
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
            if (e instanceof INotifiedEntry entry) entry.preRender(j, k, j2, k1, j1, mouseX, mouseY, this
                    .isMouseOver(mouseX, mouseY) && Objects.equals(this.getEntryAtPosition(mouseX, mouseY), e), tick);

            if (l >= this.y0 && k <= this.y1)
            {
                if (this.renderSelection && this.isSelectedItem(j))
                {
                    final int l1 = x + this.x0 + this.width / 2 - k1 / 2;
                    final int i2 = x + this.x0 + this.width / 2 + k1 / 2;
                    RenderSystem.disableTexture();
                    final float f = this.isFocused() ? 1.0F : 0.5F;
                    bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                    bufferbuilder.vertex(l1, i1 + j1 + 2, 0.0D).color(f, f, f, 1).endVertex();
                    bufferbuilder.vertex(i2, i1 + j1 + 2, 0.0D).color(f, f, f, 1).endVertex();
                    bufferbuilder.vertex(i2, i1 - 2, 0.0D).color(f, f, f, 1).endVertex();
                    bufferbuilder.vertex(l1, i1 - 2, 0.0D).color(f, f, f, 1).endVertex();
                    tessellator.end();
                    bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                    bufferbuilder.vertex(l1 + 1, i1 + j1 + 1, 0.0D).color(0, 0, 0, 1f).endVertex();
                    bufferbuilder.vertex(i2 - 1, i1 + j1 + 1, 0.0D).color(0, 0, 0, 1f).endVertex();
                    bufferbuilder.vertex(i2 - 1, i1 - 1, 0.0D).color(0, 0, 0, 1f).endVertex();
                    bufferbuilder.vertex(l1 + 1, i1 - 1, 0.0D).color(0, 0, 0, 1f).endVertex();
                    tessellator.end();
                    RenderSystem.enableTexture();
                }
                e.render(mat, j, k, j2, k1, j1, mouseX, mouseY, this.isMouseOver(mouseX, mouseY) && Objects.equals(this
                        .getEntryAtPosition(mouseX, mouseY), e), tick);
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
        this.scrollAmount = Mth.clamp(scroll, 0.0D, this.getMaxScroll() - 4);
    }

    public void skipTo(final double scroll)
    {
        this.scrollAmount = Mth.clamp(scroll, 0.0D, this.getMaxScroll() - 4);
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

    @Override
    public void updateNarration(final NarrationElementOutput p_169152_)
    {
        // TODO Auto-generated method stub

    }
}