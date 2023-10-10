package pokecube.core.client.gui.blocks;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import pokecube.core.PokecubeCore;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.init.Sounds;
import pokecube.core.inventory.healer.HealerContainer;
import pokecube.core.network.packets.PacketHeal;
import pokecube.core.utils.Resources;
import thut.lib.TComponent;

public class Healer<T extends HealerContainer> extends AbstractContainerScreen<T>
{
    public static ResourceLocation WIDGETS_GUI =
            new ResourceLocation(PokecubeMod.ID, Resources.TEXTURE_GUI_FOLDER + "widgets/pc_widgets.png");
    Button healButton;
    Button healButton2;
    Inventory inventory;

    public Healer(final T container, final Inventory ivplay, final Component name)
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

        // Blit format: Texture location, gui x pos, gui y position, texture x pos, texture y pos, texture x size, texture y size
        final int x = (this.width - this.imageWidth) / 2;
        final int y = (this.height - this.imageHeight) / 2;
        graphics.blit(Resources.GUI_HEAL_TABLE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        // Heal Button
        if (this.healButton.isHoveredOrFocused() || this.healButton2.isHoveredOrFocused())
            graphics.blit(WIDGETS_GUI, x + 16, y + 25, 45, 215, 36, 36);
        else graphics.blit(WIDGETS_GUI, x + 16, y + 25, 0, 215, 36, 36);
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int x, final int y)
    {
        graphics.drawString(this.font, TComponent.translatable("block.pokecube.pokecenter").getString(),
                8, 6, 4210752, false);
        graphics.drawString(this.font, this.playerInventoryTitle.getString(),
                8, this.imageHeight - 94 + 2, 4210752, false);
    }

    @Override
    public void init()
    {
        super.init();
        final int x = (this.width - this.imageWidth) / 2;
        final int y = (this.height - this.imageHeight) / 2;

        final Component heal = Component.translatable("block.pokecenter.heal");
        this.healButton = this.addRenderableWidget(new Button.Builder(heal, (b) -> {
            final PacketHeal packet = new PacketHeal();
            PokecubeCore.packets.sendToServer(packet);
            this.inventory.player.playSound(Sounds.HEAL_SOUND.get(), 1, 1);
        }).bounds(x + 16, y + 37, 36, 12)
                .tooltip(Tooltip.create(Component.translatable("block.pokecenter.heal.tooltip")))
                .createNarration(supplier -> Component.translatable("block.pokecenter.heal.narrate")).build());
        this.healButton.setAlpha(0);

        this.healButton2 = this.addRenderableWidget(new Button.Builder(heal, (b) -> {
            final PacketHeal packet = new PacketHeal();
            PokecubeCore.packets.sendToServer(packet);
            this.inventory.player.playSound(Sounds.HEAL_SOUND.get(), 1, 1);
        }).bounds(x + 28, y + 25, 12, 36)
                .tooltip(Tooltip.create(Component.translatable("block.pokecenter.heal.tooltip")))
                .createNarration(supplier -> Component.translatable("block.pokecenter.heal.narrate")).build());
        this.healButton2.setAlpha(0);
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