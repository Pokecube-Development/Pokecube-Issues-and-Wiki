package pokecube.nbtedit.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import pokecube.nbtedit.NBTStringHelper;
import pokecube.nbtedit.nbt.NamedNBT;
import pokecube.nbtedit.nbt.Node;

public class GuiNBTNode extends Button
{

    public static final ResourceLocation WIDGET_TEXTURE = new ResourceLocation("nbtedit", "textures/gui/widgets.png");

    private final Minecraft mc = Minecraft.getInstance();

    private final Node<NamedNBT> node;
    private final GuiNBTTree     tree;

    // Last known locations of mouse.
    double x2;
    double y2;

    private String displayString;

    public GuiNBTNode(final GuiNBTTree tree, final Node<NamedNBT> node, final int x, final int y)
    {
        super(x, y, 10, Minecraft.getInstance().font.lineHeight, new TextComponent(node.toString()), b -> tree
                .nodeClicked((GuiNBTNode) b));
        this.tree = tree;
        this.node = node;
        this.x = x;
        this.y = y;
        this.height = this.mc.font.lineHeight;
        this.updateDisplay();
    }

    @Override
    public boolean clicked(final double x, final double y)
    {
        if (!this.shouldDraw(this.tree.START_Y + 5, this.tree.bottom)) return false;
        return super.clicked(x, y);
    }

    public Node<NamedNBT> getNode()
    {
        return this.node;
    }

    public boolean hideShowClicked()
    {
        if (this.node.hasChildren() && this.inHideShowBounds())
        {
            this.node.setDrawChildren(!this.node.shouldDrawChildren());
            return true;
        }
        return false;
    }

    private boolean inHideShowBounds()
    {
        final int dx = this.node.hasChildren() ? 10 : 0;
        return this.x2 - dx <= this.x && this.clicked(this.x2, this.y2);
    }

    @Override
    public void render(final PoseStack mat, final int mx, final int my, final float m)
    {
        if (!this.shouldDraw(this.tree.START_Y + 5, this.tree.bottom)) return;

        this.x2 = mx;
        this.y2 = my;
        final boolean selected = this.tree.focusedNode() == this.node;
        final boolean hover = this.isHovered();
        final boolean chHover = this.inHideShowBounds();
        final int color = selected ? 0xff : hover ? 16777120 : this.node.hasParent() ? 14737632 : -6250336;
        final int dx = this.node.hasChildren() ? 10 : 0;
        final int x = this.x + dx;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GuiNBTNode.WIDGET_TEXTURE);

        if (selected) GuiComponent.fill(mat, x + 11, this.y, x + this.width, this.y + this.height, Integer.MIN_VALUE);
        if (this.node.hasChildren()) this.blit(mat, x - 9, this.y, this.node.shouldDrawChildren() ? 9 : 0, chHover
                ? this.height
                : 0, 9, this.height);
        this.blit(mat, x + 1, this.y, (this.node.getObject().getNBT().getId() - 1) * 9, 18, 9, 9);
        GuiComponent.drawString(mat, this.mc.font, this.displayString, x + 11, this.y + (this.height - 8) / 2, color);
    }

    public void shift(final int dy)
    {
        this.y += dy;
    }

    public boolean shouldDraw(final int top, final int bottom)
    {
        return this.y + this.height >= top && this.y <= bottom;
    }

    public boolean shouldDrawChildren()
    {
        return this.node.shouldDrawChildren();
    }

    public void updateDisplay()
    {
        this.displayString = NBTStringHelper.getNBTNameSpecial(this.node.getObject());
        this.width = this.mc.font.width(this.displayString) + 12;
    }

}
