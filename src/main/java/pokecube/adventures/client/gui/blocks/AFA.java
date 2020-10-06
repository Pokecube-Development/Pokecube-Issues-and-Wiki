package pokecube.adventures.client.gui.blocks;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.afa.AfaContainer;
import pokecube.adventures.network.PacketAFA;

public class AFA extends ContainerScreen<AfaContainer>
{

    public AFA(final AfaContainer screenContainer, final PlayerInventory inv, final ITextComponent titleIn)
    {
        super(screenContainer, inv, titleIn);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(final MatrixStack mat, final float partialTicks, final int mouseX, final int mouseY)
    {
        GL11.glPushMatrix();
        GL11.glColor4f(1f, 1f, 1f, 1f);
        this.minecraft.getTextureManager().bindTexture(new ResourceLocation(PokecubeAdv.MODID, "textures/gui/afa.png"));
        final int x = (this.width - this.xSize) / 2;
        final int y = (this.height - this.ySize) / 2;
        this.blit(mat, x, y, 0, 0, this.xSize, this.ySize);

        GL11.glPopMatrix();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(final MatrixStack mat, final int mouseX, final int mouseY)
    {
        String text = this.getTitle().getString();
        this.font.drawString(mat, text, 172 - this.font.getStringWidth(text), 6, 4210752);
        this.font.drawString(mat, this.playerInventory.getName().getString(), 8, this.ySize - 96 + 2, 4210752);

        text = this.container.tile.ability != null ? I18n.format("block.afa.ability.info", I18n.format(
                this.container.tile.ability.getName())) : I18n.format("block.afa.ability.none");

        this.font.drawString(mat, text, 172 - this.font.getStringWidth(text), 22, 4210752);

        text = I18n.format("block.afa.range.info", this.container.tile.distance);

        this.font.drawString(mat, text, 172 - this.font.getStringWidth(text), 42, 4210752);

        text = I18n.format("block.afa.power.info", this.container.tile.cost, this.container.tile.orig);

        this.font.drawString(mat, text, 172 - this.font.getStringWidth(text), 62, 4210752);
    }

    @Override
    /** Draws the screen and all the components in it. */
    public void render(final MatrixStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {
        this.renderBackground(mat);
        super.render(mat, mouseX, mouseY, partialTicks);
        this.renderHoveredTooltip(mat, mouseX, mouseY);
    }

    @Override
    protected void init()
    {
        super.init();

        final int xOffset = -119;
        final int yOffset = -88;
        final ITextComponent next = new TranslationTextComponent("block.pc.next");
        this.addButton(new Button(this.width / 2 - xOffset - 44, this.height / 2 - yOffset - 121, 10, 10, next, b ->
        {
            final PacketAFA message = new PacketAFA();
            message.data.putBoolean("U", true);
            message.data.putBoolean("S", Screen.hasShiftDown());
            PokecubeAdv.packets.sendToServer(message);
        }));
        final ITextComponent prev = new TranslationTextComponent("block.pc.previous");
        this.addButton(new Button(this.width / 2 - xOffset - 54, this.height / 2 - yOffset - 121, 10, 10, prev, b ->
        {
            final PacketAFA message = new PacketAFA();
            message.data.putBoolean("U", false);
            message.data.putBoolean("S", Screen.hasShiftDown());
            PokecubeAdv.packets.sendToServer(message);
        }));

    }

}
