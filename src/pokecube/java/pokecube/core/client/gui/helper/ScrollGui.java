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
        this.setRenderHeader(false,0);
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
        if (top < this.getY()) top -= this.itemHeight;
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

    int mouseX, mouseY;

    @Override
    protected void renderListBackground(GuiGraphics guiGraphics)
    {

    }

    protected void renderListSeparators(GuiGraphics guiGraphics) {

    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
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