package pokecube.core.client.gui.blocks;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import pokecube.core.PokecubeCore;
import pokecube.core.inventory.bookshelves.LargeChiseledBookshelfMenu;
import pokecube.core.utils.Resources;

public class LargeChiseledBookshelf<T extends LargeChiseledBookshelfMenu> extends AbstractContainerScreen<T>
{
    Inventory inventory;

    public LargeChiseledBookshelf(final T container, final Inventory ivplay, final Component name)
    {
        super(container, ivplay, name);
        this.inventory = ivplay;
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float partialTicks, final int mouseX, final int mouseY)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, Resources.GUI_HEAL_TABLE);
        // bind texture
        final int j2 = (this.width - this.imageWidth) / 2;
        final int k2 = (this.height - this.imageHeight) / 2;
        graphics.blit(new ResourceLocation(PokecubeCore.MODID, "textures/gui/large_chiseled_bookshelf.png"), j2, k2, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int x, final int y)
    {
        graphics.drawString(this.font, this.getTitle().getString(),
                8, 6, 4210752, false);
        graphics.drawString(this.font, this.playerInventoryTitle.getString(),
                8, this.imageHeight - 100 + 2, 4210752, false);
    }

    @Override
    public void init()
    {
        super.init();
    }

    @Override
    /** Draws the screen and all the components in it. */
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks)
    {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}