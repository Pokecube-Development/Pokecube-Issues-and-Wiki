package pokecube.core.client.gui.blocks;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.PokecubeCore;
import pokecube.core.client.gui.pokemob.GuiPokemobBase;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.inventory.trade.TradeContainer;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.network.packets.PacketTrade;

public class Trade<T extends TradeContainer> extends ContainerScreen<T>
{
    public Trade(final T container, final PlayerInventory inventory, final ITextComponent name)
    {
        super(container, inventory, name);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(final MatrixStack mat, final float f, final int i, final int j)
    {
        GL11.glPushMatrix();
        GL11.glColor4f(1f, 1f, 1f, 1f);
        this.minecraft.getTextureManager()
                .bindTexture(new ResourceLocation(PokecubeMod.ID, "textures/gui/trade_machine.png"));
        final int x = (this.width - this.xSize) / 2;
        final int y = (this.height - this.ySize) / 2;
        this.blit(mat, x, y, 0, 0, this.xSize, this.ySize);
        GL11.glPopMatrix();
    }

    /** Draw the foreground layer for the ContainerScreen (everything in front
     * of the items) */
    @Override
    protected void drawGuiContainerForegroundLayer(final MatrixStack mat, final int p_146979_1_, final int p_146979_2_)
    {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        ItemStack stack = this.container.getInv().getStackInSlot(0);
        if (PokecubeManager.isFilled(stack)) this.renderMob(0);
        stack = this.container.getInv().getStackInSlot(1);
        if (PokecubeManager.isFilled(stack)) this.renderMob(1);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    @Override
    public void init()
    {
        super.init();
        final ITextComponent trade = new TranslationTextComponent("block.trade_machine.trade");
        this.addButton(new Button(this.width / 2 - 70, this.height / 2 - 22, 40, 20, trade, b ->
        {
            final PacketTrade packet = new PacketTrade();
            packet.data.putByte("s", (byte) 0);
            PokecubeCore.packets.sendToServer(packet);
        }));
        this.addButton(new Button(this.width / 2 + 30, this.height / 2 - 22, 40, 20, trade, b ->
        {
            final PacketTrade packet = new PacketTrade();
            packet.data.putByte("s", (byte) 1);
            PokecubeCore.packets.sendToServer(packet);
        }));
    }

    @Override
    public void onClose()
    {
        super.onClose();
        final PacketTrade packet = new PacketTrade();
        packet.data.putBoolean("c", true);
        PokecubeCore.packets.sendToServer(packet);
    }

    @Override
    public void render(final MatrixStack mat, final int i, final int j, final float f)
    {
        this.renderBackground(mat);
        super.render(mat,i, j, f);
        if (this.container.tile.confirmed[0]) this.buttons.get(0).setFGColor(0xFF88FF00);
        else this.buttons.get(0).setFGColor(0xFFFFFFFF);

        if (this.container.tile.confirmed[1]) this.buttons.get(1).setFGColor(0xFF88FF00);
        else this.buttons.get(1).setFGColor(0xFFFFFFFF);
        this.renderHoveredTooltip(mat,i, j);
    }

    protected void renderMob(final int index)
    {
        final LivingEntity mob = PokecubeManager.itemToMob(this.container.getInv().getStackInSlot(index),
                PokecubeCore.proxy.getWorld());
        int dx = 0;
        float rotX = 0;
        float rotY = 50;
        float rotZ = 0;
        final int dy = -10;

        final float size = 0.5f;

        final IPokemob poke = CapabilityPokemob.getPokemobFor(mob);
        switch (index)
        {
        case 0:
            dx = -10;
            rotX = 0;
            rotY = -40;
            rotZ = 0;
            break;
        case 1:
            dx = 80;
            rotX = 0;
            rotY = 260;
            rotZ = 0;
            break;
        }

        GuiPokemobBase.renderMob(mob, dx, dy, 0, rotX, rotY, rotZ, size);

        switch (index)
        {
        case 0:
            dx = -30;
            rotX = 0;
            rotY = -40;
            rotZ = 0;
            break;
        case 1:
            dx = 100;
            rotX = 0;
            rotY = 260;
            rotZ = 0;
            break;
        }

        if (poke != null && poke.getOwner() instanceof PlayerEntity)
            GuiPokemobBase.renderMob(poke.getOwner(), dx, dy, 0, rotX, rotY, rotZ, size);
    }

}
