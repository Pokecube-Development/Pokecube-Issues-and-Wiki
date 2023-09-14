package pokecube.core.client.gui.blocks;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import pokecube.core.PokecubeCore;
import pokecube.core.init.Sounds;
import pokecube.core.inventory.healer.HealerContainer;
import pokecube.core.network.packets.PacketHeal;
import pokecube.core.utils.Resources;
import thut.lib.TComponent;

public class Healer<T extends HealerContainer> extends AbstractContainerScreen<T>
{
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
        // bind texture
        final int j2 = (this.width - this.imageWidth) / 2;
        final int k2 = (this.height - this.imageHeight) / 2;
        graphics.blit(Resources.GUI_HEAL_TABLE, j2, k2, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int x, final int y)
    {
        // NOOP, vanilla here draws labels for inventory titles, we don't need
        // those.
    }

    @Override
    public void init()
    {
        super.init();
        final Component heal = TComponent.translatable("block.pokecenter.heal");

        this.addRenderableWidget(new Button.Builder(heal, (b) -> {
            final PacketHeal packet = new PacketHeal();
            PokecubeCore.packets.sendToServer(packet);
            this.inventory.player.playSound(Sounds.HEAL_SOUND.get(), 1, 1);
        }).bounds(this.width / 2 + 18, this.height / 2 - 50, 60, 20).build());
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