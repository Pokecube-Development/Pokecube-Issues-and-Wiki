package pokecube.nbtedit.gui;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import pokecube.nbtedit.nbt.SaveStates;

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

    public GuiSaveSlotButton(final SaveStates.SaveState save, final int x, final int y, final IPressable onClick)
    {
        super(x, y, GuiSaveSlotButton.X_SIZE, GuiSaveSlotButton.HEIGHT, new StringTextComponent((save.tag.isEmpty() ? "Save " : "Load ")
                + save.name), onClick);
        this.save = save;
        this.x = this.rightX = x;
        this.y = y;
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
        System.out.println(c + " " + key);
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

    public void draw(final MatrixStack mat, final int mx, final int my)
    {

        int textColor = this.isHovered() ? 16777120 : 0xffffff;
        this.renderVanillaButton(mat, this.x, this.y, 0, 66, this.width, GuiSaveSlotButton.HEIGHT);
        AbstractGui.drawCenteredString(mat, this.mc.fontRenderer, this.getMessage(), this.x + this.width / 2, this.y + 6,
                textColor);
        if (this.tickCount != -1 && this.tickCount / 6 % 2 == 0) this.mc.fontRenderer.drawStringWithShadow(mat, "_", this.x
                + (this.width + this.mc.fontRenderer.getStringWidth(this.getMessage().getString())) / 2 + 1, this.y + 6, 0xffffff);

        if (this.xVisible)
        {
            textColor = this.inBoundsOfX(mx, my) ? 16777120 : 0xffffff;
            this.renderVanillaButton(mat, this.leftBoundOfX(), this.topBoundOfX(), 0, 66, GuiSaveSlotButton.X_SIZE,
                    GuiSaveSlotButton.X_SIZE);
            AbstractGui.drawCenteredString(mat, this.mc.fontRenderer, "x", this.x - GuiSaveSlotButton.GAP
                    - GuiSaveSlotButton.X_SIZE / 2, this.y + 6, textColor);
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
        return this.x - GuiSaveSlotButton.X_SIZE - GuiSaveSlotButton.GAP;
    }

    private void renderVanillaButton(final MatrixStack mat, final int x, final int y, final int u, final int v, final int width,
            final int height)
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(GuiSaveSlotButton.TEXTURE);

        // Top Left
        this.blit(mat, x, y, u, v, width / 2, height / 2);
        // Top Right
        this.blit(mat, x + width / 2, y, u + 200 - width / 2, v, width / 2, height / 2);
        // Bottom Left
        this.blit(mat, x, y + height / 2, u, v + 20 - height / 2, width / 2, height / 2);
        // Bottom Right
        this.blit(mat, x + width / 2, y + height / 2, u + 200 - width / 2, v + 20 - height / 2, width / 2, height / 2);
    }

    public void reset()
    {
        this.xVisible = false;
        this.save.tag = new CompoundNBT();
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
        this.setMessage(new StringTextComponent(string));
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
        return this.y + (GuiSaveSlotButton.HEIGHT - GuiSaveSlotButton.X_SIZE) / 2;
    }

    public void update()
    {
        ++this.tickCount;
    }

    private void updatePosition()
    {
        this.width = this.mc.fontRenderer.getStringWidth(this.getMessage().getString()) + 24;
        if (this.width % 2 == 1) ++this.width;
        this.width = MathHelper.clamp(this.width, GuiSaveSlotButton.MIN_WIDTH, GuiSaveSlotButton.MAX_WIDTH);
        this.x = this.rightX - this.width;
    }

}
