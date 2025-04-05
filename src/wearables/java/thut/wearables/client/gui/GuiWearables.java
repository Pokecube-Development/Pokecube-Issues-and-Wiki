package thut.wearables.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;

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
    public static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath(ThutWearables.MODID,
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
    {}

    @Override
    protected void renderBg(final GuiGraphics graphics, final float mouseX, final int mouseY, final int p_146976_3_)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, GuiWearables.background);

        int i = this.leftPos;
        int j = this.topPos;
        graphics.blit(GuiWearables.background, i, j, 0, 0, this.imageWidth, this.imageHeight);
        InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, i + 26, j + 8, i + 75, j + 78, 30, 0.0625F,
                oldMouseX, oldMouseY, this.menu.wearer);
    }

    /** Draws the screen and all the components in it. */
    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks)
    {
        this.renderBackground(graphics, mouseY, mouseY, partialTicks);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.oldMouseX = mouseX;
        this.oldMouseY = mouseY;
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
