package pokecube.core.client.gui.blocks;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.PokecubeCore;
import pokecube.core.client.Resources;
import pokecube.core.inventory.healer.HealerContainer;
import pokecube.core.network.packets.PacketHeal;

public class Healer<T extends HealerContainer> extends ContainerScreen<T>
{

    public Healer(final T container, final PlayerInventory ivplay, final ITextComponent name)
    {
        super(container, ivplay, name);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(final MatrixStack mat, final float partialTicks, final int mouseX,
            final int mouseY)
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        // bind texture
        this.minecraft.getTextureManager().bindTexture(Resources.GUI_HEAL_TABLE);
        final int j2 = (this.width - this.xSize) / 2;
        final int k2 = (this.height - this.ySize) / 2;
        this.blit(mat, j2, k2, 0, 0, this.xSize, this.ySize);
    }

    @Override
    public void init()
    {
        super.init();
        final ITextComponent heal = new TranslationTextComponent("block.pokecenter.heal");
        this.addButton(new Button(this.width / 2 + 20, this.height / 2 - 50, 60, 20, heal, b ->
        {
            final PacketHeal packet = new PacketHeal();
            PokecubeCore.packets.sendToServer(packet);
            if (HealerContainer.HEAL_SOUND != null) this.playerInventory.player.playSound(HealerContainer.HEAL_SOUND, 1,
                    1);
        }));
    }

    @Override
    /** Draws the screen and all the components in it. */
    public void render(final MatrixStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {
        this.renderBackground(mat);
        super.render(mat, mouseX, mouseY, partialTicks);
        this.renderHoveredTooltip(mat, mouseX, mouseY);
    }
}