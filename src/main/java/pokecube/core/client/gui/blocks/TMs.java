package pokecube.core.client.gui.blocks;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;
import pokecube.api.moves.MoveEntry;
import pokecube.core.PokecubeCore;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.inventory.tms.TMContainer;
import pokecube.core.moves.MovesUtils;
import pokecube.core.network.packets.PacketTMs;
import pokecube.core.utils.Resources;
import thut.lib.TComponent;

public class TMs<T extends TMContainer> extends AbstractContainerScreen<T>
{
    public static ResourceLocation TEXTURE = new ResourceLocation(PokecubeMod.ID,
            Resources.TEXTURE_GUI_FOLDER + "tm_machine.png");

    private EditBox search;
    int index = 0;

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
    protected void renderLabels(final GuiGraphics graphics, final int x, final int y)
    {
        graphics.drawString(this.font, this.getTitle().getString(), 8, 6, 4210752, false);
        graphics.drawString(this.font, this.playerInventoryTitle.getString(),
                8, this.imageHeight - 96 + 2, 4210752, false);
        // NOOP, this would draw name and title.
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float partialTicks, final int mouseX, final int mouseY)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TMs.TEXTURE);
        final int j2 = (this.width - this.imageWidth) / 2;
        final int k2 = (this.height - this.imageHeight) / 2;

        // TODO: Check this
        graphics.blit(TMs.TEXTURE, j2, k2, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void init()
    {
        super.init();

        final Component apply = TComponent.translatable("block.tm_machine.apply");
        this.addRenderableWidget(new Button.Builder(apply, (b) -> {
            final PacketTMs packet = new PacketTMs();
            packet.data.putInt("m", this.index);
            PokecubeCore.packets.sendToServer(packet);
        }).bounds(this.width / 2 - 8, this.height / 2 - 40, 58, 20).build());

        final Component next = TComponent.translatable(">");
        this.addRenderableWidget(new Button.Builder(next, (b) -> {
            final String[] moves = this.menu.moves;
            this.index++;
            if (this.index > moves.length - 1) this.index = 0;
        }).bounds(this.width / 2 + 70, this.height / 2 - 50, 10, 10).build());

        final Component prev = TComponent.translatable("<");
        this.addRenderableWidget(new Button.Builder(prev, (b) -> {
            final String[] moves = this.menu.moves;
            this.index--;
            if (this.index < 0 && moves.length > 0) this.index = moves.length - 1;
            else if (this.index < 0) this.index = 0;
        }).bounds(this.width / 2 - 31, this.height / 2 - 50, 10, 10).build());

        this.addRenderableWidget(this.search = new EditBox(this.font, this.width / 2 - 19, this.height / 2 - 50, 87, 10,
                TComponent.translatable("")));
    }

    @Override
    /** Draws the screen and all the components in it. */
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks)
    {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        final String[] moves = this.menu.moves;
        final String s = moves.length > 0 ? moves[this.index % moves.length] : "";
        final MoveEntry move = MovesUtils.getMove(s);
        if (move != null)
        {
            final int yOffset = this.height / 2 - 161;
            final int xOffset = this.width / 2 - 42;

            graphics.drawString(this.font, MovesUtils.getMoveName(s, null).getString(), xOffset + 15,
                    yOffset + 99, move.getType(null).colour);
            graphics.drawString(this.font, "" + move.getPWR(), xOffset + 102, yOffset + 99, 0xffffff);
        }
        this.renderTooltip(graphics, mouseX, mouseY);
    }

}
