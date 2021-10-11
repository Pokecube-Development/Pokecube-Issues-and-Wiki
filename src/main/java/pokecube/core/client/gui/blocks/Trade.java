package pokecube.core.client.gui.blocks;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import pokecube.core.PokecubeCore;
import pokecube.core.client.gui.pokemob.GuiPokemobBase;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.inventory.trade.TradeContainer;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.network.packets.PacketTrade;

public class Trade<T extends TradeContainer> extends AbstractContainerScreen<T>
{
    public Trade(final T container, final Inventory inventory, final Component name)
    {
        super(container, inventory, name);
    }

    @Override
    protected void renderBg(final PoseStack mat, final float f, final int i, final int j)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, new ResourceLocation(PokecubeMod.ID, "textures/gui/trade_machine.png"));
        final int x = (this.width - this.imageWidth) / 2;
        final int y = (this.height - this.imageHeight) / 2;
        this.blit(mat, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    /**
     * Draw the foreground layer for the ContainerScreen (everything in front
     * of the items)
     */
    @Override
    protected void renderLabels(final PoseStack mat, final int p_146979_1_, final int p_146979_2_)
    {
        ItemStack stack = this.menu.getInv().getItem(0);
        if (PokecubeManager.isFilled(stack)) this.renderMob(0);
        stack = this.menu.getInv().getItem(1);
        if (PokecubeManager.isFilled(stack)) this.renderMob(1);
    }

    @Override
    public void init()
    {
        super.init();
        final Component trade = new TranslatableComponent("block.trade_machine.trade");
        this.addRenderableWidget(new Button(this.width / 2 - 70, this.height / 2 - 22, 40, 20, trade, b ->
        {
            final PacketTrade packet = new PacketTrade();
            packet.data.putByte("s", (byte) 0);
            PokecubeCore.packets.sendToServer(packet);
        }));
        this.addRenderableWidget(new Button(this.width / 2 + 30, this.height / 2 - 22, 40, 20, trade, b ->
        {
            final PacketTrade packet = new PacketTrade();
            packet.data.putByte("s", (byte) 1);
            PokecubeCore.packets.sendToServer(packet);
        }));
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
    public void render(final PoseStack mat, final int i, final int j, final float f)
    {
        this.renderBackground(mat);
        super.render(mat, i, j, f);
        if (this.menu.tile.confirmed[0]) ((AbstractWidget) this.renderables.get(0)).setFGColor(0xFF88FF00);
        else((AbstractWidget) this.renderables.get(0)).setFGColor(0xFFFFFFFF);

        if (this.menu.tile.confirmed[1]) ((AbstractWidget) this.renderables.get(1)).setFGColor(0xFF88FF00);
        else((AbstractWidget) this.renderables.get(1)).setFGColor(0xFFFFFFFF);
        this.renderTooltip(mat, i, j);
    }

    protected void renderMob(final int index)
    {
        final LivingEntity mob = PokecubeManager.itemToMob(this.menu.getInv().getItem(index), PokecubeCore.proxy
                .getWorld());
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

        if (poke != null && poke.getOwner() instanceof Player) GuiPokemobBase.renderMob(poke.getOwner(), dx, dy, 0,
                rotX, rotY, rotZ, size);
    }

}
