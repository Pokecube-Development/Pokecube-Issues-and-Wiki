package pokecube.core.client.gui.helper;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ScrollGui<T extends AbstractSelectionList.Entry<T>> extends AbstractSelectionList<T>
{
    public static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("widget/scroller");
    public static final ResourceLocation SCROLLER_BACKGROUND_SPRITE = ResourceLocation
            .withDefaultNamespace("widget/scroller_background");
    public static final ResourceLocation INWORLD_MENU_LIST_BACKGROUND = ResourceLocation
            .withDefaultNamespace("textures/gui/inworld_menu_list_background.png");

    public boolean smoothScroll = false;
    private boolean checkedSmooth = false;
    private double scrollAmount;
    public final Screen parent;
    public int scrollBarOffset = -10;

    public int scrollBarDx = 0;
    public int scrollBarDy = 0;
    public int scrollColorR = 139;
    public int scrollColorG = 139;
    public int scrollColorB = 139;
    public int scrollDarkBorderR = 55;
    public int scrollDarkBorderG = 55;
    public int scrollDarkBorderB = 55;
    public int scrollLightBorderR = 255;
    public int scrollLightBorderG = 255;
    public int scrollLightBorderB = 255;
    public int scrollBarColorR = 198;
    public int scrollBarColorG = 198;
    public int scrollBarColorB = 198;
    public int scrollBarDarkBorderR = 55;
    public int scrollBarDarkBorderG = 55;
    public int scrollBarDarkBorderB = 55;
    public int scrollBarGrayBorderR = 139;
    public int scrollBarGrayBorderG = 139;
    public int scrollBarGrayBorderB = 139;
    public int scrollBarLightBorderR = 255;
    public int scrollBarLightBorderG = 255;
    public int scrollBarLightBorderB = 255;

    public ScrollGui(final Screen parent, final Minecraft mcIn, final int widthIn, final int heightIn,
            final int slotHeightIn, final int offsetX, final int offsetY)
    {
        super(mcIn, widthIn, heightIn, offsetY, slotHeightIn);
        this.setX(offsetX);
        this.parent = parent;
        this.headerHeight = 0;
        // No default background thing
        this.centerListVertically = false;
        this.setRenderHeader(false, (int) (9.0F * 1.5F));
    }

    public ScrollGui<T> setScrollColor(int scrollColorR, int scrollColorG, int scrollColorB)
    {
        this.scrollColorR = scrollColorR;
        this.scrollColorG = scrollColorG;
        this.scrollColorB = scrollColorB;
        return this;
    }

    public ScrollGui<T> setScrollDarkBorder(int scrollDarkBorderR, int scrollDarkBorderG, int scrollDarkBorderB)
    {
        this.scrollDarkBorderR = scrollDarkBorderR;
        this.scrollDarkBorderG = scrollDarkBorderG;
        this.scrollDarkBorderB = scrollDarkBorderB;
        return this;
    }

    public ScrollGui<T> setScrollLightBorder(int scrollLightBorderR, int scrollLightBorderG, int scrollLightBorderB)
    {
        this.scrollLightBorderR = scrollLightBorderR;
        this.scrollLightBorderG = scrollLightBorderG;
        this.scrollLightBorderB = scrollLightBorderB;
        return this;
    }

    public ScrollGui<T> setScrollBarColor(int scrollBarColorR, int scrollBarColorG, int scrollBarColorB)
    {
        this.scrollBarColorR = scrollBarColorR;
        this.scrollBarColorG = scrollBarColorG;
        this.scrollBarColorB = scrollBarColorB;
        return this;
    }

    public ScrollGui<T> setScrollBarDarkBorder(int scrollBarDarkBorderR, int scrollBarDarkBorderG,
            int scrollBarDarkBorderB)
    {
        this.scrollBarDarkBorderR = scrollBarDarkBorderR;
        this.scrollBarDarkBorderG = scrollBarDarkBorderG;
        this.scrollBarDarkBorderB = scrollBarDarkBorderB;
        return this;
    }

    public ScrollGui<T> setScrollBarGrayBorder(int scrollBarGrayBorderR, int scrollBarGrayBorderG,
            int scrollBarGrayBorderB)
    {
        this.scrollBarGrayBorderR = scrollBarGrayBorderR;
        this.scrollBarGrayBorderG = scrollBarGrayBorderG;
        this.scrollBarGrayBorderB = scrollBarGrayBorderB;
        return this;
    }

    public ScrollGui<T> setScrollBarLightBorder(int scrollBarLightBorderR, int scrollBarLightBorderG,
            int scrollBarLightBorderB)
    {
        this.scrollBarLightBorderR = scrollBarLightBorderR;
        this.scrollBarLightBorderG = scrollBarLightBorderG;
        this.scrollBarLightBorderB = scrollBarLightBorderB;
        return this;
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
        if (top < this.getY() + 4) top -= 5 * this.itemHeight;
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

    int mouseX, mouseY;

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float tick)
    {
        if (true)
        {
            super.render(graphics, mouseX, mouseY, tick);
            return;
        }

        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.renderListBackground(graphics);

        final int i = this.getScrollbarPosition();
        final int j = i + 6;
        final Tesselator tessellator = Tesselator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.begin(VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_COLOR);
        RenderSystem.setShaderTexture(0, INWORLD_MENU_LIST_BACKGROUND);
        final int k = this.getRowLeft();
        final int l = this.getY() + 4 - (int) this.getScrollAmount();

        // TODO render header?
//        if (this.renderHeader) this.renderHeader(graphics, k, l);

        this.renderListItems(graphics, k, l, tick);

        final int k1 = this.getMaxScroll();
        if (k1 > (smoothScroll ? 0 : this.itemHeight))
        {

            int y0 = this.getY();
            int y1 = y0 + this.getHeight();
            // TODO: No equivalent?
            // RenderSystem.disableTexture();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            int l1 = (int) ((float) ((y1 - y0) * (y1 - y0)) / (float) this.getMaxPosition());
            l1 = Mth.clamp(l1, 32, y1 - y0 - 4);
            int i2 = (int) this.getScrollAmount() * (y1 - y0 - l1) / k1 + y0;
            if (i2 < y0) i2 = y0;
            i2 += scrollBarDy;

            y0 = y0 + this.scrollBarDy;
            y1 = y1 + this.scrollBarDy;

            int x0 = i + this.scrollBarDx;
            int x1 = j + this.scrollBarDx;

//            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            // This does the gray border corners
            bufferbuilder.addVertex(x0 - 1, y1 + 1, 0.0F).setColor(scrollColorR, scrollColorG, scrollColorB, 255);
            bufferbuilder.addVertex(x1 + 1, y1 + 1, 0.0F).setColor(scrollColorR, scrollColorG, scrollColorB, 255);
            bufferbuilder.addVertex(x1 + 1, y0 - 1, 0.0F).setColor(scrollColorR, scrollColorG, scrollColorB, 255);
            bufferbuilder.addVertex(x0 - 1, y0 - 1, 0.0F).setColor(scrollColorR, scrollColorG, scrollColorB, 255);

            // This does the dark border
            bufferbuilder.addVertex(x0 - 1, y1, 0.0F).setColor(scrollDarkBorderR, scrollDarkBorderG, scrollDarkBorderB,
                    255);
            bufferbuilder.addVertex(x1, y1, 0.0F).setColor(scrollDarkBorderR, scrollDarkBorderG, scrollDarkBorderB,
                    255);
            bufferbuilder.addVertex(x1, y0 - 1, 0.0F).setColor(scrollDarkBorderR, scrollDarkBorderG, scrollDarkBorderB,
                    255);
            bufferbuilder.addVertex(x0 - 1, y0 - 1, 0.0F).setColor(scrollDarkBorderR, scrollDarkBorderG,
                    scrollDarkBorderB, 255);

            // This does the white border
            bufferbuilder.addVertex(x0, y1 + 1, 0.0F).setColor(scrollLightBorderR, scrollLightBorderG,
                    scrollLightBorderB, 255);
            bufferbuilder.addVertex(x1 + 1, y1 + 1, 0.0F).setColor(scrollLightBorderR, scrollLightBorderG,
                    scrollLightBorderB, 255);
            bufferbuilder.addVertex(x1 + 1, y0, 0.0F).setColor(scrollLightBorderR, scrollLightBorderG,
                    scrollLightBorderB, 255);
            bufferbuilder.addVertex(x0, y0, 0.0F).setColor(scrollLightBorderR, scrollLightBorderG, scrollLightBorderB,
                    255);

            // This does the gray background
            bufferbuilder.addVertex(x0, y1, 0.0F).setColor(scrollColorR, scrollColorG, scrollColorB, 255);
            bufferbuilder.addVertex(x1, y1, 0.0F).setColor(scrollColorR, scrollColorG, scrollColorB, 255);
            bufferbuilder.addVertex(x1, y0, 0.0F).setColor(scrollColorR, scrollColorG, scrollColorB, 255);
            bufferbuilder.addVertex(x0, y0, 0.0F).setColor(scrollColorR, scrollColorG, scrollColorB, 255);

            // This does the gray border corners of the scroll bar
            y1 = i2 + l1 + scrollBarDy;
            y0 = i2;
            bufferbuilder.addVertex(x0, y1, 0.0F).setColor(scrollBarGrayBorderR, scrollBarGrayBorderG,
                    scrollBarGrayBorderB, 255);
            bufferbuilder.addVertex(x1, y1, 0.0F).setColor(scrollBarGrayBorderR, scrollBarGrayBorderG,
                    scrollBarGrayBorderB, 255);
            bufferbuilder.addVertex(x1, y0, 0.0F).setColor(scrollBarGrayBorderR, scrollBarGrayBorderG,
                    scrollBarGrayBorderB, 255);
            bufferbuilder.addVertex(x0, y0, 0.0F).setColor(scrollBarGrayBorderR, scrollBarGrayBorderG,
                    scrollBarGrayBorderB, 255);

            // This does the white border of the scroll bar
            bufferbuilder.addVertex(x0, y1 - 1, 0.0F).setColor(scrollBarLightBorderR, scrollBarLightBorderG,
                    scrollBarLightBorderB, 255);
            bufferbuilder.addVertex(x1 - 1, y1 - 1, 0.0F).setColor(scrollBarLightBorderR, scrollBarLightBorderG,
                    scrollBarLightBorderB, 255);
            bufferbuilder.addVertex(x1 - 1, y0, 0.0F).setColor(scrollBarLightBorderR, scrollBarLightBorderG,
                    scrollBarLightBorderB, 255);
            bufferbuilder.addVertex(x0, y0, 0.0F).setColor(scrollBarLightBorderR, scrollBarLightBorderG,
                    scrollBarLightBorderB, 255);

            // This does the dark border of the scroll bar
            bufferbuilder.addVertex(x0 + 1, y1, 0.0F).setColor(scrollBarDarkBorderR, scrollBarDarkBorderG,
                    scrollBarDarkBorderB, 255);
            bufferbuilder.addVertex(x1, y1, 0.0F).setColor(scrollBarDarkBorderR, scrollBarDarkBorderG,
                    scrollBarDarkBorderB, 255);
            bufferbuilder.addVertex(x1, y0 + 1, 0.0F).setColor(scrollBarDarkBorderR, scrollBarDarkBorderG,
                    scrollBarDarkBorderB, 255);
            bufferbuilder.addVertex(x0 + 1, y0 + 1, 0.0F).setColor(scrollBarDarkBorderR, scrollBarDarkBorderG,
                    scrollBarDarkBorderB, 255);

            // This does the center bar of the scroll bar
            x1 -= 1;
            y0 += 1;
            y1 -= 1;
            x0 += 1;
            bufferbuilder.addVertex(x0, y1, 0.0F).setColor(scrollBarColorR, scrollBarColorG, scrollBarColorB, 255);
            bufferbuilder.addVertex(x1, y1, 0.0F).setColor(scrollBarColorR, scrollBarColorG, scrollBarColorB, 255);
            bufferbuilder.addVertex(x1, y0, 0.0F).setColor(scrollBarColorR, scrollBarColorG, scrollBarColorB, 255);
            bufferbuilder.addVertex(x0, y0, 0.0F).setColor(scrollBarColorR, scrollBarColorG, scrollBarColorB, 255);
//            tessellator.end();
        }

        this.renderDecorations(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderListItems(final GuiGraphics graphics, final int x, final int y, final float tick)
    {
        super.renderListItems(graphics, x, y, tick);
//        final int i = this.getItemCount();
//        final Tesselator tessellator = Tesselator.getInstance();
//        final BufferBuilder bufferbuilder = tessellator.getBuilder();
//
//        for (int j = 0; j < i; ++j)
//        {
//            final int k = this.getRowTop(j);
//            final int l = this.getRowBottom(j);
//            final T e = this.getEntry(j);
//            final int i1 = y + j * this.itemHeight + this.headerHeight;
//            final int j1 = this.itemHeight;
//            final int k1 = this.getRowWidth();
//            final int j2 = this.getRowLeft();
//            if (e instanceof INotifiedEntry entry) entry.preRender(j, k, j2, k1, j1, mouseX, mouseY,
//                    this.isMouseOver(mouseX, mouseY) && Objects.equals(this.getEntryAtPosition(mouseX, mouseY), e),
//                    tick);
//
//            if (l >= this.y0 && k <= this.y1)
//            {
//                if (this.renderSelection && this.isSelectedItem(j))
//                {
//                    final int l1 = x + this.x0 + this.width / 2 - k1 / 2;
//                    final int i2 = x + this.x0 + this.width / 2 + k1 / 2;
//                    // TODO: No equivalent?
//                    // RenderSystem.disableTexture();
//                    final float f = this.isFocused() ? 1.0F : 0.5F;
////                    bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
//                    bufferbuilder.addVertex(l1, i1 + j1 + 2, 0.0F).setColor(f, f, f, 1);
//                    bufferbuilder.addVertex(i2, i1 + j1 + 2, 0.0F).setColor(f, f, f, 1);
//                    bufferbuilder.addVertex(i2, i1 - 2, 0.0F).setColor(f, f, f, 1);
//                    bufferbuilder.addVertex(l1, i1 - 2, 0.0F).setColor(f, f, f, 1);
////                    tessellator.end();
////                    bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
//                    bufferbuilder.addVertex(l1 + 1, i1 + j1 + 1, 0.0F).setColor(0, 0, 0, 1f);
//                    bufferbuilder.addVertex(i2 - 1, i1 + j1 + 1, 0.0F).setColor(0, 0, 0, 1f);
//                    bufferbuilder.addVertex(i2 - 1, i1 - 1, 0.0F).setColor(0, 0, 0, 1f);
//                    bufferbuilder.addVertex(l1 + 1, i1 - 1, 0.0F).setColor(0, 0, 0, 1f);
////                    tessellator.end();
//                    // TODO: No equivalent?
//                    // RenderSystem.activeTexture();
//                }
//                e.render(graphics, j, k, j2, k1, j1, mouseX, mouseY,
//                        this.isMouseOver(mouseX, mouseY) && Objects.equals(this.getEntryAtPosition(mouseX, mouseY), e),
//                        tick);
//            }
//        }
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
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput)
    {

    }
}