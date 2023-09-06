package thut.wearables.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import thut.wearables.ThutWearables;
import thut.wearables.inventory.ContainerWearables;

public class GuiWearables extends EffectRenderingInventoryScreen<ContainerWearables>
{
    public static final ResourceLocation background = new ResourceLocation(ThutWearables.MODID,
            "textures/gui/wearables.png");

    /** The old x position of the mouse pointer */
    private float oldMouseX;
    /** The old y position of the mouse pointer */
    private float oldMouseY;

    public GuiWearables(final ContainerWearables container, final Inventory player)
    {
        super(container, player, container.wearer.getDisplayName());
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int x, final int y)
    {
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float p_146976_1_, final int p_146976_2_, final int p_146976_3_)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, GuiWearables.background);
        final int i = this.leftPos;
        final int j = this.topPos;
        graphics.blit(new ResourceLocation(""), i, j, 0, 0, this.imageWidth, this.imageHeight);
        InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, i + 51, j + 75, 30, i + 51 - this.oldMouseX, j + 75 - 50
                - this.oldMouseY, this.menu.wearer);
    }

    /** Draws the screen and all the components in it. */
    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks)
    {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.oldMouseX = mouseX;
        this.oldMouseY = mouseY;
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
