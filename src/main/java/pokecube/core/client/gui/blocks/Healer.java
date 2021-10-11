package pokecube.core.client.gui.blocks;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import pokecube.core.PokecubeCore;
import pokecube.core.client.Resources;
import pokecube.core.inventory.healer.HealerContainer;
import pokecube.core.network.packets.PacketHeal;

public class Healer<T extends HealerContainer> extends AbstractContainerScreen<T>
{
    Inventory inventory;

    public Healer(final T container, final Inventory ivplay, final Component name)
    {
        super(container, ivplay, name);
        this.inventory = ivplay;
    }

    @Override
    protected void renderBg(final PoseStack mat, final float partialTicks, final int mouseX,
            final int mouseY)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, Resources.GUI_HEAL_TABLE);
        // bind texture
        final int j2 = (this.width - this.imageWidth) / 2;
        final int k2 = (this.height - this.imageHeight) / 2;
        this.blit(mat, j2, k2, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(final PoseStack matrixStack, final int x, final int y)
    {
        // NOOP, vanilla here draws labels for inventory titles, we don't need
        // those.
    }

    @Override
    public void init()
    {
        super.init();
        final Component heal = new TranslatableComponent("block.pokecenter.heal");
        this.addRenderableWidget(new Button(this.width / 2 + 21, this.height / 2 - 50, 60, 20, heal, b ->
        {
            final PacketHeal packet = new PacketHeal();
            PokecubeCore.packets.sendToServer(packet);
            if (HealerContainer.HEAL_SOUND != null) this.inventory.player.playSound(HealerContainer.HEAL_SOUND, 1,
                    1);
        }));
    }

    @Override
    /** Draws the screen and all the components in it. */
    public void render(final PoseStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {
        this.renderBackground(mat);
        super.render(mat, mouseX, mouseY, partialTicks);
        this.renderTooltip(mat, mouseX, mouseY);
    }
}