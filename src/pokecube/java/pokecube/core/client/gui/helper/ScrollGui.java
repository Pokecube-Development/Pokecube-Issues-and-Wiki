package pokecube.core.client.gui.helper;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;

public class ScrollGui<T extends AbstractSelectionList.Entry<T>> extends AbstractSelectionList<T>
{
    private ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("widget/scroller");
    private ResourceLocation SCROLLER_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace(
            "widget/scroller_background");

    public boolean smoothScroll = true;
    private boolean checkedSmooth = false;
    private double scrollAmount;
    public final Screen parent;
    public int scrollBarOffset = -10;

    public int scrollBarDx = 0;
    public int scrollBarDy = 0;
    @Nullable
    private T hovered;
    private boolean renderHeader;

    public ScrollGui(final Screen parent, final Minecraft mcIn, final int widthIn, final int heightIn,
            final int slotHeightIn, final int offsetX, final int offsetY)
    {
        super(mcIn, widthIn, heightIn, offsetY, slotHeightIn);
        this.setX(offsetX);
        this.parent = parent;
        this.headerHeight = 0;
        // No default background thing
        this.centerListVertically = false;
        this.setRenderHeader(false, 0);
    }

    @Override
    protected void setRenderHeader(boolean renderHeader, int headerHeight)
    {
        super.setRenderHeader(renderHeader, headerHeight);
        this.renderHeader = renderHeader;
    }

    @Nullable
    @Override
    public T getHovered()
    {
        return hovered;
    }

    /** This override is to make this method public. */
    @Override
    public int addEntry(final T entry)
    {
        return super.addEntry(entry);
    }

    @Override
    public int getMaxScroll()
    {
        return Math.max(0, this.getMaxPosition() - (this.height - this.getY() - 4));
    }

    protected int getRowBottom(final int index)
    {
        return this.getRowTop(index) + this.itemHeight;
    }

    @Override
    protected int getRowTop(final int index)
    {
        int top = super.getRowTop(index);
        // Move this such that it is definitely invalie.
        if (top < this.getY()) top -= this.itemHeight;
        return top;
    }

    /** Gets the width of the list */
    @Override
    public int getRowWidth()
    {
        return this.width;
    }

    @Override
    public double getScrollAmount()
    {
        if (!this.smoothScroll && !this.checkedSmooth)
        {
            this.setScrollAmount(this.itemHeight * (this.scrollAmount / this.itemHeight));
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
    protected void renderListBackground(GuiGraphics guiGraphics)
    {

    }

    protected void renderListSeparators(GuiGraphics guiGraphics)
    {

    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        // Direct copy from super, as we need to adjust the scroll bar, which is before decorations if present.

        this.hovered = this.isMouseOver(mouseX, mouseY) ? this.getEntryAtPosition(mouseX, mouseY) : null;
        this.renderListBackground(guiGraphics);
        this.enableScissor(guiGraphics);
        if (this.renderHeader)
        {
            int i = this.getRowLeft();
            int j = this.getY() + 4 - (int) this.getScrollAmount();
            this.renderHeader(guiGraphics, i, j);
        }

        this.renderListItems(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.disableScissor();
        this.renderListSeparators(guiGraphics);
        if (this.scrollbarVisible())
        {
            int l = this.getScrollbarPosition();
            int i1 = (int) ((float) (this.height * this.height) / (float) this.getMaxPosition());
            i1 = Mth.clamp(i1, 32, this.height - 8);
            int k = (int) this.getScrollAmount() * (this.height - i1) / this.getMaxScroll() + this.getY();
            if (k < this.getY())
            {
                k = this.getY();
            }

            RenderSystem.enableBlend();
            guiGraphics.blitSprite(SCROLLER_BACKGROUND_SPRITE, l, this.getY(), 6, this.getHeight());
            guiGraphics.blitSprite(SCROLLER_SPRITE, l, k, 6, i1);
            RenderSystem.disableBlend();
        }

        this.renderDecorations(guiGraphics, mouseX, mouseY);
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderListItems(final GuiGraphics graphics, final int x, final int y, final float tick)
    {
        super.renderListItems(graphics, x, y, tick);
    }

    public void scroll(int ds)
    {
        if (!this.smoothScroll) ds = ds == 0 ? 0 : ds > 0 ? this.itemHeight : -this.itemHeight;
        this.setScrollAmount(this.getScrollAmount() + ds);
    }

    @Override
    public boolean keyPressed(final int keyCode, final int b, final int c)
    {
        for (final T value : this.children()) if (value.keyPressed(keyCode, b, c)) return true;
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
        this.scrollAmount = Mth.clamp(scroll, 0.0F, this.getMaxScroll() - 4);
    }

    public void skipTo(final double scroll)
    {
        this.scrollAmount = Mth.clamp(scroll, 0.0F, this.getMaxScroll() - 4);
    }

    public int itemHeight()
    {
        return this.itemHeight;
    }

    /**
     * This override is to make it public
     */
    @Override
    public T getEntry(final int index)
    {
        return super.getEntry(index);
    }

    public int getSize()
    {
        return this.getItemCount();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput)
    {

    }

    public void setScrollSprite(ResourceLocation SCROLLER_SPRITE)
    {
        this.SCROLLER_SPRITE = SCROLLER_SPRITE;
    }

    public void setScrollSpriteBG(ResourceLocation SCROLLER_BACKGROUND_SPRITE)
    {
        this.SCROLLER_BACKGROUND_SPRITE = SCROLLER_BACKGROUND_SPRITE;
    }
}