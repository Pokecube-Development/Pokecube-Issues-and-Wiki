package pokecube.core.client.gui.blocks;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.PokecubeCore;
import pokecube.core.client.Resources;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.inventory.tms.TMContainer;
import pokecube.core.moves.MovesUtils;
import pokecube.core.network.packets.PacketTMs;

public class TMs<T extends TMContainer> extends ContainerScreen<T>
{
    public static ResourceLocation TEXTURE = new ResourceLocation(PokecubeMod.ID, Resources.TEXTURE_GUI_FOLDER
            + "tm_machine.png");

    private TextFieldWidget search;
    int                     index = 0;

    public TMs(final T container, final PlayerInventory playerInventory, final ITextComponent name)
    {
        super(container, playerInventory, name);
    }

    @Override
    public boolean charTyped(final char p_charTyped_1_, final int p_charTyped_2_)
    {
        System.out.println(this.search.getText());
        return super.charTyped(p_charTyped_1_, p_charTyped_2_);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(final MatrixStack mat, final float partialTicks, final int mouseX,
            final int mouseY)
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(TMs.TEXTURE);
        final int j2 = (this.width - this.xSize) / 2;
        final int k2 = (this.height - this.ySize) / 2;
        this.blit(mat, j2, k2, 0, 0, this.xSize, this.ySize);
    }

    @Override
    public void init()
    {
        super.init();
        final ITextComponent apply = new TranslationTextComponent("block.tm_machine.apply");
        this.addButton(new Button(this.width / 2 - 8, this.height / 2 - 39, 60, 20, apply, b ->
        {
            final PacketTMs packet = new PacketTMs();
            packet.data.putInt("m", this.index);
            PokecubeCore.packets.sendToServer(packet);
        }));
        final ITextComponent next = new TranslationTextComponent(">");
        this.addButton(new Button(this.width / 2 + 68, this.height / 2 - 50, 10, 10, next, b ->
        {
            final String[] moves = this.container.moves;
            this.index++;
            if (this.index > moves.length - 1) this.index = 0;
        }));
        final ITextComponent prev = new TranslationTextComponent("<");
        this.addButton(new Button(this.width / 2 - 30, this.height / 2 - 50, 10, 10, prev, b ->
        {
            final String[] moves = this.container.moves;
            this.index--;
            if (this.index < 0 && moves.length > 0) this.index = moves.length - 1;
            else if (this.index < 0) this.index = 0;
        }));
        this.addButton(this.search = new TextFieldWidget(this.font, this.width / 2 - 19, this.height / 2 - 50, 87, 10,
                new TranslationTextComponent("")));
    }

    @Override
    /** Draws the screen and all the components in it. */
    public void render(final MatrixStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {
        this.renderBackground(mat);
        super.render(mat, mouseX, mouseY, partialTicks);
        final String[] moves = this.container.moves;
        final String s = moves.length > 0 ? moves[this.index % moves.length] : "";
        final Move_Base move = MovesUtils.getMoveFromName(s);
        if (move != null)
        {
            final int yOffset = this.height / 2 - 164;
            final int xOffset = this.width / 2 - 42;
            AbstractGui.drawString(mat, this.font, MovesUtils.getMoveName(s).getString(), xOffset + 14, yOffset + 99,
                    move
                    .getType(null).colour);
            AbstractGui.drawString(mat, this.font, "" + move.getPWR(), xOffset + 102, yOffset + 99, 0xffffff);
        }
        this.renderHoveredTooltip(mat, mouseX, mouseY);
    }

}
