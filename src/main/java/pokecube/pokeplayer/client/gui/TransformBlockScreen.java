package pokecube.pokeplayer.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import pokecube.pokeplayer.Reference;
import pokecube.pokeplayer.block.PokeTransformContainer;

public class TransformBlockScreen extends ContainerScreen<PokeTransformContainer> {

	private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(Reference.ID,
			"textures/gui/pokeplayer_gui.png");

	public TransformBlockScreen(PokeTransformContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
		super(screenContainer, inv, titleIn);
		this.guiLeft = 0;
		this.guiTop = 0;
		this.xSize = 176;
		this.ySize = 166;
	}

	@Override
	public void render(MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack mat, int mouseX, int mouseY) 
	{
		this.font.drawString(mat, this.getTitle().getString(), 8.0f, 8.0f, 4210752);
		this.font.drawString(mat, this.playerInventory.getName().getString(), 8.0F, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
		this.minecraft.getTextureManager().bindTexture(BACKGROUND_TEXTURE);
		int x = (this.width - this.xSize) / 2;
		int y = (this.height - this.ySize) / 2;
		this.blit(matrixStack, x, y, 0, 0, this.xSize, this.ySize);
	}
}