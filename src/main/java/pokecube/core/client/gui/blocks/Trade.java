package pokecube.core.client.gui.blocks;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.PokecubeCore;
import pokecube.core.client.gui.pokemob.GuiPokemobHelper;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.inventory.trade.TradeContainer;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.network.packets.PacketTrade;
import thut.lib.TComponent;

public class Trade<T extends TradeContainer> extends AbstractContainerScreen<T>
{
    public Trade(final T container, final Inventory inventory, final Component name)
    {
        super(container, inventory, name);
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float f, final int i, final int j)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, new ResourceLocation(PokecubeMod.ID, "textures/gui/trade_machine.png"));
        final int x = (this.width - this.imageWidth) / 2;
        final int y = (this.height - this.imageHeight) / 2;
        graphics.blit(new ResourceLocation(PokecubeMod.ID, "textures/gui/trade_machine.png"),
                x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    /**
     * Draw the foreground layer for the ContainerScreen (everything in front of
     * the items)
     */
    @Override
    protected void renderLabels(final GuiGraphics graphics, final int p_146979_1_, final int p_146979_2_)
    {
        graphics.drawString(this.font, this.getTitle().getString() + "'s " + TComponent.translatable("block.pokecube.trade_machine"), 8, 6, 4210752, false);
        graphics.drawString(this.font, this.playerInventoryTitle.getString(),
                8, this.imageHeight - 96 + 2, 4210752, false);

        ItemStack stack = this.menu.getInv().getItem(0);
        if (PokecubeManager.isFilled(stack)) this.renderMob(0, 0);
        stack = this.menu.getInv().getItem(1);
        if (PokecubeManager.isFilled(stack)) this.renderMob(1, 0);
    }

    @Override
    public void init()
    {
        super.init();
        final Component trade = TComponent.translatable("block.trade_machine.trade");

        this.addRenderableWidget(new Button.Builder(trade, (b) -> {
            final PacketTrade packet = new PacketTrade();
            packet.data.putByte("s", (byte) 0);
            PokecubeCore.packets.sendToServer(packet);
        }).bounds(this.width / 2 - 70, this.height / 2 - 12, 40, 20).build());

        this.addRenderableWidget(new Button.Builder(trade, (b) -> {
            final PacketTrade packet = new PacketTrade();
            packet.data.putByte("s", (byte) 1);
            PokecubeCore.packets.sendToServer(packet);
        }).bounds(this.width / 2 + 30, this.height / 2 - 12, 40, 20).build());
    }

    @Override
    public void removed()
    {
        super.removed();
        final PacketTrade packet = new PacketTrade();
        packet.data.putBoolean("c", true);
        PokecubeCore.packets.sendToServer(packet);
    }

    @Override
    public void render(final GuiGraphics graphics, final int i, final int j, final float f)
    {
        this.renderBackground(graphics);
        super.render(graphics, i, j, f);
        if (this.menu.tile.confirmed[0]) ((AbstractWidget) this.renderables.get(0)).setFGColor(0xFF88FF00);
        else((AbstractWidget) this.renderables.get(0)).setFGColor(0xFFFFFFFF);

        if (this.menu.tile.confirmed[1]) ((AbstractWidget) this.renderables.get(1)).setFGColor(0xFF88FF00);
        else((AbstractWidget) this.renderables.get(1)).setFGColor(0xFFFFFFFF);
        this.renderTooltip(graphics, i, j);
    }

    protected void renderMob(final int index, float partialTicks)
    {
        final LivingEntity mob = PokecubeManager.itemToMob(this.menu.getInv().getItem(index),
                PokecubeCore.proxy.getWorld());
        int dx = 0;
        float rotX = 0;
        float rotY = 50;
        float rotZ = 0;
        final int dy = -10;

        final float size = 0.5f;

        final IPokemob poke = PokemobCaps.getPokemobFor(mob);
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

        GuiPokemobHelper.renderMob(mob, dx, dy, 0, rotX, rotY, rotZ, size, partialTicks);

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

        if (poke != null && poke.getOwner() instanceof Player)
            GuiPokemobHelper.renderMob(poke.getOwner(), dx, dy, 0, rotX, rotY, rotZ, size, partialTicks);
    }

}
