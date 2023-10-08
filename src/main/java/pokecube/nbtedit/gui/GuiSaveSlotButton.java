package pokecube.nbtedit.gui;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import pokecube.nbtedit.nbt.SaveStates;
import thut.lib.TComponent;

public class GuiSaveSlotButton extends Button
{

    public static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/widgets.png");
    private static final int             X_SIZE  = 14, HEIGHT = 20, MAX_WIDTH = 150, MIN_WIDTH = 82, GAP = 3;
    private final Minecraft              mc;
    public final SaveStates.SaveState    save;
    /**
     * Whether the close option is visible.
     */
    boolean                              xVisible;
    int                                  rightX;
    private int                          tickCount;

    public GuiSaveSlotButton(final SaveStates.SaveState save, final int x, final int y, final OnPress onClick, CreateNarration narration)
    {
        super(x, y, GuiSaveSlotButton.X_SIZE, GuiSaveSlotButton.HEIGHT, TComponent.literal((save.tag.isEmpty() ? "Save " : "Load ")
                + save.name), onClick, narration);
        this.save = save;
        this.setX(this.rightX = x);
        this.setY(y);
        this.mc = Minecraft.getInstance();
        this.xVisible = !save.tag.isEmpty();
        this.tickCount = -1;
        this.updatePosition();
    }

    public void backSpace()
    {
        if (this.save.name.length() > 0)
        {
            this.save.name = this.save.name.substring(0, this.save.name.length() - 1);
            this.setMessage((this.save.tag.isEmpty() ? "Save " : "Load ") + this.save.name);
            this.updatePosition();
        }
    }

    @Override
    public boolean charTyped(final char c, final int key)
    {
        if (key == GLFW.GLFW_KEY_BACKSPACE)
        {
            this.backSpace();
            return true;
        }
        if (Character.isDigit(c) || Character.isLetter(c))
        {
            this.save.name += c;
            this.setMessage((this.save.tag.isEmpty() ? "Save " : "Load ") + this.save.name);
            this.updatePosition();
            return true;
        }
        return false;
    }

    public void draw(final GuiGraphics graphics, final int mx, final int my)
    {

        int textColor = this.isHoveredOrFocused() ? 16777120 : 0xffffff;
        this.renderVanillaButton(graphics, this.getX(), this.getY(), 0, 66, this.width, GuiSaveSlotButton.HEIGHT);
        graphics.drawCenteredString(this.mc.font, this.getMessage(), this.getX() + this.width / 2, this.getY() + 6, textColor);
        // TODO: Fix this
//        if (this.tickCount != -1 && this.tickCount / 6 % 2 == 0)
//            this.mc.font.drawShadow(graphics, "_", this.getX() +
//                    (this.width + this.mc.font.width(this.getMessage().getString())) / 2 + 1, this.getY() + 6, 0xffffff);

        if (this.xVisible)
        {
            textColor = this.inBoundsOfX(mx, my) ? 16777120 : 0xffffff;
            this.renderVanillaButton(graphics, this.leftBoundOfX(),
                    this.topBoundOfX(), 0, 66, GuiSaveSlotButton.X_SIZE, GuiSaveSlotButton.X_SIZE);
            graphics.drawCenteredString(this.mc.font, "x", this.getX() -
                    GuiSaveSlotButton.GAP - GuiSaveSlotButton.X_SIZE / 2, this.getY() + 6, textColor);
        }
    }

    public boolean inBoundsOfX(final double x2, final double y2)
    {
        final int buttonX = this.leftBoundOfX();
        final int buttonY = this.topBoundOfX();
        return this.xVisible && x2 >= buttonX && y2 >= buttonY && x2 < buttonX + GuiSaveSlotButton.X_SIZE
                && y2 < buttonY + GuiSaveSlotButton.X_SIZE;
    }

    private int leftBoundOfX()
    {
        return this.getX() - GuiSaveSlotButton.X_SIZE - GuiSaveSlotButton.GAP;
    }

    private void renderVanillaButton(final GuiGraphics graphics, final int x, final int y, final int u, final int v, final int width,
            final int height)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GuiSaveSlotButton.TEXTURE);

        // TODO: Check this
        // Top Left
        graphics.blit(GuiSaveSlotButton.TEXTURE, x, y, u, v, width / 2, height / 2);
        // Top Right
        graphics.blit(GuiSaveSlotButton.TEXTURE, x + width / 2, y, u + 200 - width / 2, v, width / 2, height / 2);
        // Bottom Left
        graphics.blit(GuiSaveSlotButton.TEXTURE, x, y + height / 2, u, v + 20 - height / 2, width / 2, height / 2);
        // Bottom Right
        graphics.blit(GuiSaveSlotButton.TEXTURE, x + width / 2, y + height / 2, u + 200 - width / 2, v + 20 - height / 2, width / 2, height / 2);
    }

    public void reset()
    {
        this.xVisible = false;
        this.save.tag = new CompoundTag();
        this.setMessage("Save " + this.save.name);
        this.updatePosition();
    }

    public void saved()
    {
        this.xVisible = true;
        this.setMessage("Load " + this.save.name);
        this.updatePosition();
    }

    private void setMessage(final String string)
    {
        this.setMessage(TComponent.literal(string));
    }

    public void startEditing()
    {
        this.tickCount = 0;
    }

    public void stopEditing()
    {
        this.tickCount = -1;
    }

    private int topBoundOfX()
    {
        return this.getY() + (GuiSaveSlotButton.HEIGHT - GuiSaveSlotButton.X_SIZE) / 2;
    }

    public void update()
    {
        ++this.tickCount;
    }

    private void updatePosition()
    {
        this.width = this.mc.font.width(this.getMessage().getString()) + 24;
        if (this.width % 2 == 1) ++this.width;
        this.width = Mth.clamp(this.width, GuiSaveSlotButton.MIN_WIDTH, GuiSaveSlotButton.MAX_WIDTH);
        this.setX(this.rightX - this.width);
    }

}
