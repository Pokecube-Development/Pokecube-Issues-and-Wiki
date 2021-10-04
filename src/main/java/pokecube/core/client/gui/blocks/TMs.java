package pokecube.core.client.gui.blocks;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import pokecube.core.PokecubeCore;
import pokecube.core.client.Resources;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.inventory.tms.TMContainer;
import pokecube.core.moves.MovesUtils;
import pokecube.core.network.packets.PacketTMs;

public class TMs<T extends TMContainer> extends AbstractContainerScreen<T>
{
    public static ResourceLocation TEXTURE = new ResourceLocation(PokecubeMod.ID, Resources.TEXTURE_GUI_FOLDER
            + "tm_machine.png");

    private EditBox search;
    int                     index = 0;

    public TMs(final T container, final Inventory playerInventory, final Component name)
    {
        super(container, playerInventory, name);
    }

    @Override
    public boolean keyPressed(final int keyCode, final int b, final int c)
    {
        if (this.search.isFocused())
        {
            if (keyCode == GLFW.GLFW_KEY_ENTER)
            {
                // TODO search the moves list and go to the one here.
            }
            return true;
        }
        return super.keyPressed(keyCode, b, c);
    }

    @Override
    protected void renderLabels(final PoseStack matrixStack, final int x, final int y)
    {
        // NOOP, this would draw name and title.
    }

    @Override
    protected void renderBg(final PoseStack mat, final float partialTicks, final int mouseX,
            final int mouseY)
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindForSetup(TMs.TEXTURE);
        final int j2 = (this.width - this.imageWidth) / 2;
        final int k2 = (this.height - this.imageHeight) / 2;
        this.blit(mat, j2, k2, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void init()
    {
        super.init();
        final Component apply = new TranslatableComponent("block.tm_machine.apply");
        this.addRenderableWidget(new Button(this.width / 2 - 8, this.height / 2 - 39, 60, 20, apply, b ->
        {
            final PacketTMs packet = new PacketTMs();
            packet.data.putInt("m", this.index);
            PokecubeCore.packets.sendToServer(packet);
        }));
        final Component next = new TranslatableComponent(">");
        this.addRenderableWidget(new Button(this.width / 2 + 68, this.height / 2 - 50, 10, 10, next, b ->
        {
            final String[] moves = this.menu.moves;
            this.index++;
            if (this.index > moves.length - 1) this.index = 0;
        }));
        final Component prev = new TranslatableComponent("<");
        this.addRenderableWidget(new Button(this.width / 2 - 30, this.height / 2 - 50, 10, 10, prev, b ->
        {
            final String[] moves = this.menu.moves;
            this.index--;
            if (this.index < 0 && moves.length > 0) this.index = moves.length - 1;
            else if (this.index < 0) this.index = 0;
        }));
        this.addRenderableWidget(this.search = new EditBox(this.font, this.width / 2 - 19, this.height / 2 - 50, 87, 10,
                new TranslatableComponent("")));
    }

    @Override
    /** Draws the screen and all the components in it. */
    public void render(final PoseStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {
        this.renderBackground(mat);
        super.render(mat, mouseX, mouseY, partialTicks);
        final String[] moves = this.menu.moves;
        final String s = moves.length > 0 ? moves[this.index % moves.length] : "";
        final Move_Base move = MovesUtils.getMoveFromName(s);
        if (move != null)
        {
            final int yOffset = this.height / 2 - 164;
            final int xOffset = this.width / 2 - 42;
            GuiComponent.drawString(mat, this.font, MovesUtils.getMoveName(s).getString(), xOffset + 14, yOffset + 99,
                    move.getType(null).colour);
            GuiComponent.drawString(mat, this.font, "" + move.getPWR(), xOffset + 102, yOffset + 99, 0xffffff);
        }
        this.renderTooltip(mat, mouseX, mouseY);
    }

}
