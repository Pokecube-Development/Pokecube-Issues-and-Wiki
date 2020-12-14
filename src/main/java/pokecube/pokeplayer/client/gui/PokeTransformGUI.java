//package pokecube.pokeplayer.client.gui;
//
//import com.mojang.blaze3d.matrix.MatrixStack;
//import net.minecraft.client.gui.screen.inventory.ContainerScreen;
//import net.minecraft.entity.player.PlayerInventory;
//import net.minecraft.util.ResourceLocation;
//import net.minecraft.util.text.ITextComponent;
//import pokecube.pokeplayer.Reference;
//import pokecube.pokeplayer.blocks.PokePlayerContainer;
//
//public class PokeTransformGUI extends ContainerScreen<PokePlayerContainer>
//{
//	private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(Reference.ID, "textures/gui/pokemake_box.png");
//
//	public PokeTransformGUI(final PokePlayerContainer screenContainer,final PlayerInventory inv, final ITextComponent titleIn) {
//		super(screenContainer, inv, titleIn);
//		this.guiLeft = 0;
//		this.guiTop = 0;
//		this.xSize = 175;
//		this.ySize = 183;
//	}
//	
//	@Override
//	public void render(final MatrixStack matrixStack,final int mouseX, final int mouseY, final float partialTicks) {
//		this.renderBackground(matrixStack);
//		super.render(matrixStack, mouseX, mouseY, partialTicks);
//		this.renderHoveredTooltip(matrixStack, mouseX, mouseX);
//	}
//	
//	@Override
//	protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {
//		super.drawGuiContainerForegroundLayer(matrixStack, x, y);
//		this.font.drawString(matrixStack, this.getTitle().getString(), 8.0f, 6.0f, 4210752);
//		this.font.drawString(matrixStack, this.playerInventory.getName().getString(), 8.0f, 9.0f, 4210752);
//	}
//	
//	@Override
//	protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
//		this.minecraft.getTextureManager().bindTexture(BACKGROUND_TEXTURE);
//		int x = (this.width - this.xSize)/ 2;
//		int y = (this.height - this.ySize)/ 2;
//		this.blit(matrixStack, x, y, 0, 0, this.xSize, this.ySize);
//	}
//}
