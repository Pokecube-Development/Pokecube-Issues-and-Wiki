package thut.wearables.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import thut.wearables.ThutWearables;
import thut.wearables.inventory.ContainerWearables;

public class GuiWearables extends DisplayEffectsScreen<ContainerWearables>
{
    public static final ResourceLocation background = new ResourceLocation(ThutWearables.MODID,
            "textures/gui/wearables.png");

    /** The old x position of the mouse pointer */
    private float oldMouseX;
    /** The old y position of the mouse pointer */
    private float oldMouseY;

    public GuiWearables(final ContainerWearables container, final PlayerInventory player)
    {
        super(container, player, container.wearer.getDisplayName());
    }

    @Override
    protected void drawGuiContainerForegroundLayer(final MatrixStack matrixStack, final int x, final int y)
    {
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(final MatrixStack mat, final float p_146976_1_,
            final int p_146976_2_, final int p_146976_3_)
    {
        this.minecraft.getTextureManager().bindTexture(GuiWearables.background);
        final int i = this.guiLeft;
        final int j = this.guiTop;
        this.blit(mat, i, j, 0, 0, this.xSize, this.ySize);
        InventoryScreen.drawEntityOnScreen(i + 51, j + 75, 30, i + 51 - this.oldMouseX, j + 75 - 50 - this.oldMouseY,
                this.container.wearer);
    }

    /** Draws the screen and all the components in it. */
    @Override
    public void render(final MatrixStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {
        this.renderBackground(mat);
        super.render(mat, mouseX, mouseY, partialTicks);
        this.oldMouseX = mouseX;
        this.oldMouseY = mouseY;
        this.renderHoveredTooltip(mat, mouseX, mouseY);
    }
}
