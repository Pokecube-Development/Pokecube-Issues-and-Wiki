package pokecube.adventures.client.gui.blocks;

import com.mojang.blaze3d.systems.RenderSystem;

import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.afa.AfaContainer;
import pokecube.adventures.network.PacketAFA;
import thut.lib.TComponent;

public class AFA extends AbstractContainerScreen<AfaContainer>
{

    public AFA(final AfaContainer screenContainer, final Inventory inv, final Component titleIn)
    {
        super(screenContainer, inv, titleIn);
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float partialTicks, final int mouseX, final int mouseY)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, new ResourceLocation(PokecubeAdv.MODID,
                "textures/gui/afa.png"));
        final int x = (this.width - this.imageWidth) / 2;
        final int y = (this.height - this.imageHeight) / 2;
        // TODO: Check this
        graphics.blit(new ResourceLocation(PokecubeAdv.MODID, "textures/gui/afa.png"), x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int mouseX, final int mouseY)
    {
        String text = this.getTitle().getString();
        graphics.drawString(this.font, text, 8, 6, 4210752, false);
        graphics.drawString(this.font, this.playerInventoryTitle.getString(), 8, this.imageHeight - 96 + 2, 4210752, false);

        text = this.menu.tile.ability != null ? I18n.get("block.afa.ability.info", I18n.get(this.menu.tile.ability
                .getName())) : I18n.get("block.afa.ability.none");

        int color = this.menu.tile.ability == null ? 0xBF1E0B : 0x0A4C0B;
        graphics.drawString(this.font, text, 62, 26, color, false);

        text = I18n.get("block.afa.range.info", this.menu.tile.distance);

        graphics.drawString(this.font, text, 62, 40, 4210752, false);

        color = this.menu.tile.cost > this.menu.tile.orig ? 0xBF1E0B : 4210752;
        text = I18n.get("block.afa.power.info", this.menu.tile.cost, this.menu.tile.orig);

        graphics.drawString(this.font, text, 62, 54, color, false);
    }

    @Override
    /** Draws the screen and all the components in it. */
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks)
    {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void init()
    {
        super.init();

        final int xOffset = -86;
        final int yOffset = -88;

        // Elements placed in order of selection when pressing tab
        final Component prev = TComponent.translatable("block.ability_field_amplifier.previous");
        this.addRenderableWidget(new Button.Builder(prev, (b) -> {
            final PacketAFA message = new PacketAFA();
            message.data.putBoolean("U", false);
            message.data.putBoolean("S", Screen.hasShiftDown());
            PokecubeAdv.packets.sendToServer(message);
        }).bounds(this.width / 2 + xOffset + 30, this.height / 2 - yOffset - 117, 10, 10)
                .createNarration(supplier -> Component.translatable("block.ability_field_amplifier.previous.narrate")).build());

        final Component next = TComponent.translatable("block.ability_field_amplifier.next");
        this.addRenderableWidget(new Button.Builder(next, (b) -> {
            final PacketAFA message = new PacketAFA();
            message.data.putBoolean("U", true);
            message.data.putBoolean("S", Screen.hasShiftDown());
            PokecubeAdv.packets.sendToServer(message);
        }).bounds(this.width / 2 + xOffset + 42, this.height / 2 - yOffset - 117, 10, 10)
                .createNarration(supplier -> Component.translatable("block.ability_field_amplifier.next.narrate")).build());
    }

}
