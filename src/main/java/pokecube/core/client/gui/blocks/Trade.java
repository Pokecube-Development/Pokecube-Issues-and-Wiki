package pokecube.core.client.gui.blocks;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
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
import pokecube.core.utils.Resources;
import thut.lib.TComponent;

public class Trade<T extends TradeContainer> extends AbstractContainerScreen<T>
{
    public static ResourceLocation TRADE_GUI = new ResourceLocation(PokecubeMod.ID, Resources.TEXTURE_GUI_FOLDER + "trade_machine.png");
    public static ResourceLocation TRADE_DARK_GUI = new ResourceLocation(PokecubeMod.ID, Resources.TEXTURE_GUI_FOLDER + "trade_machine_dark.png");
    public static ResourceLocation WIDGETS_GUI = new ResourceLocation(PokecubeMod.ID, "textures/gui/widgets/pc_widgets.png");
    public static ResourceLocation WIDGETS_DARK_GUI = new ResourceLocation(PokecubeMod.ID, "textures/gui/widgets/pc_widgets_dark.png");
    Button darkModeButton;
    Button lightModeButton;
    Button tradeButton;
    Button trade2Button;

    public Trade(final T container, final Inventory inventory, final Component name)
    {
        super(container, inventory, name);
        this.imageWidth = 176;
        this.imageHeight = 192;
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float f, final int i, final int j)
    {
        ResourceLocation WIDGETS_DARK_OR_LIGHT_GUI = this.darkModeButton.visible ? WIDGETS_GUI : WIDGETS_DARK_GUI;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, new ResourceLocation(PokecubeMod.ID, "textures/gui/trade_machine.png"));
        final int x = (this.width - this.imageWidth) / 2;
        final int y = (this.height - this.imageHeight) / 2;

        //  Blit format: Texture location, gui x pos, gui y position, texture x pos, texture y pos, texture x size, texture y size
        if (this.darkModeButton.visible)
            graphics.blit(TRADE_GUI, x, y, 0, 0, this.imageWidth, this.imageHeight);
        else if (this.lightModeButton.visible)
            graphics.blit(TRADE_DARK_GUI, x, y, 0, 0, this.imageWidth, this.imageHeight);

        // PokeCube slot icon
        graphics.blit(WIDGETS_DARK_OR_LIGHT_GUI, x + 25, y + 22, 75, 165, 18, 18);

        // PokeCube slot icons
        graphics.blit(WIDGETS_DARK_OR_LIGHT_GUI, x + 133, y + 22, 75, 165, 18, 18);

        if (this.darkModeButton.isHoveredOrFocused() && this.darkModeButton.visible)
        {
            graphics.blit(WIDGETS_DARK_GUI, x - 17, y + 1, 240, 20, 15, 13);
        } else if (this.darkModeButton.visible) {
            graphics.blit(WIDGETS_DARK_GUI, x - 16, y + 1, 240, 0, 14, 13);
        }

        if (this.lightModeButton.isHoveredOrFocused() && this.lightModeButton.visible)
        {
            graphics.blit(WIDGETS_GUI, x - 17, y + 1, 240, 20, 15, 13);
        } else if (this.lightModeButton.visible) {
            graphics.blit(WIDGETS_GUI, x - 16, y + 1, 240, 0, 14, 13);
        }

        if (this.menu.tile.confirmed[0] && this.tradeButton.isHoveredOrFocused())
        {
            graphics.blit(WIDGETS_DARK_OR_LIGHT_GUI, x + 24, y + 70, 0, 190, 20, 20);
            graphics.blit(WIDGETS_DARK_OR_LIGHT_GUI, x + 24, y + 70, 25, 190, 20, 20);
        } else if (this.menu.tile.confirmed[0])
        {
            graphics.blit(WIDGETS_DARK_OR_LIGHT_GUI, x + 25, y + 71, 0, 165, 19, 19);
            graphics.blit(WIDGETS_DARK_OR_LIGHT_GUI, x + 25, y + 71, 25, 190, 19, 19);
        } else if (this.tradeButton.isHoveredOrFocused())
        {
            graphics.blit(WIDGETS_DARK_OR_LIGHT_GUI, x + 24, y + 70, 0, 190, 20, 20);
            graphics.blit(WIDGETS_DARK_OR_LIGHT_GUI, x + 24, y + 70, 25, 165, 20, 20);
        } else {
            graphics.blit(WIDGETS_DARK_OR_LIGHT_GUI, x + 25, y + 71, 0, 165, 19, 19);
            graphics.blit(WIDGETS_DARK_OR_LIGHT_GUI, x + 25, y + 71, 25, 165, 19, 19);
        }

        if (this.menu.tile.confirmed[1] && this.trade2Button.isHoveredOrFocused())
        {
            graphics.blit(WIDGETS_DARK_OR_LIGHT_GUI, x + 132, y + 70, 0, 190, 20, 20);
            graphics.blit(WIDGETS_DARK_OR_LIGHT_GUI, x + 132, y + 70, 25, 190, 20, 20);
        } else if (this.menu.tile.confirmed[1])
        {
            graphics.blit(WIDGETS_DARK_OR_LIGHT_GUI, x + 133, y + 71, 0, 165, 19, 19);
            graphics.blit(WIDGETS_DARK_OR_LIGHT_GUI, x + 133, y + 71, 25, 190, 19, 19);
        } else if (this.trade2Button.isHoveredOrFocused())
        {
            graphics.blit(WIDGETS_DARK_OR_LIGHT_GUI, x + 132, y + 70, 0, 190, 20, 20);
            graphics.blit(WIDGETS_DARK_OR_LIGHT_GUI, x + 132, y + 70, 25, 165, 20, 20);
        } else {
            graphics.blit(WIDGETS_DARK_OR_LIGHT_GUI, x + 133, y + 71, 0, 165, 19, 19);
            graphics.blit(WIDGETS_DARK_OR_LIGHT_GUI, x + 133, y + 71, 25, 165, 19, 19);
        }
    }

    /**
     * Draw the foreground layer for the ContainerScreen (everything in front of
     * the items)
     */
    @Override
    protected void renderLabels(final GuiGraphics graphics, final int p_146979_1_, final int p_146979_2_)
    {
        if (this.lightModeButton.visible) graphics.drawString(this.font, TComponent.translatable("block.pokecube.trade_machine"),
                8, 6, 0xB2AFD6, false);
        else graphics.drawString(this.font, TComponent.translatable("block.pokecube.trade_machine"),
                8, 6, 0xFFFFFF, false);
        graphics.drawString(this.font, this.playerInventoryTitle.getString(),
                8, this.imageHeight - 94 + 2, 4210752, false);

        ItemStack stack = this.menu.getInv().getItem(0);
        if (PokecubeManager.isFilled(stack)) this.renderMob(0, 0);
        stack = this.menu.getInv().getItem(1);
        if (PokecubeManager.isFilled(stack)) this.renderMob(1, 0);
    }

    @Override
    public void init()
    {
        super.init();
        final int x = this.width / 2 - 88;
        final int y = this.height / 2 - 96;

        // Elements placed in order of selection when pressing tab
        final Component trade = TComponent.translatable("block.trade_machine.trade");
        this.tradeButton = this.addRenderableWidget(new Button.Builder(trade, (b) -> {
            final PacketTrade packet = new PacketTrade();
            packet.data.putByte("s", (byte) 0);
            PokecubeCore.packets.sendToServer(packet);
        }).bounds(x + 25, y + 71, 18, 18)
                .createNarration(supplier -> Component.translatable("block.trade_machine.trade.narrate")).build());
        this.tradeButton.setAlpha(0);

        this.trade2Button = this.addRenderableWidget(new Button.Builder(trade, (b) -> {
            final PacketTrade packet = new PacketTrade();
            packet.data.putByte("s", (byte) 1);
            PokecubeCore.packets.sendToServer(packet);
        }).bounds(x + 133, y + 71, 18, 18)
                .createNarration(supplier -> Component.translatable("block.trade_machine.trade.narrate")).build());
        this.trade2Button.setAlpha(0);

        final Component darkMode = TComponent.literal("");
        this.darkModeButton = this.addRenderableWidget(new Button.Builder(darkMode, (b) -> {
            this.darkModeButton.visible = false;
            this.lightModeButton.visible = true;
        }).bounds(x - 16, y + 1, 14, 13)
                .tooltip(Tooltip.create(Component.translatable("block.trade_machine.dark_mode.tooltip")))
                .createNarration(supplier -> Component.translatable("block.trade_machine.dark_mode.narrate")).build());
        this.darkModeButton.visible = !PokecubeCore.getConfig().darkMode;
        this.darkModeButton.setAlpha(0);

        final Component lightMode = TComponent.literal("");
        this.lightModeButton = this.addRenderableWidget(new Button.Builder(lightMode, (b) -> {
            this.lightModeButton.visible = false;
            this.darkModeButton.visible = true;
        }).bounds(x - 16, y + 1, 14, 13)
                .tooltip(Tooltip.create(Component.translatable("block.trade_machine.light_mode.tooltip")))
                .createNarration(supplier -> Component.translatable("block.trade_machine.light_mode.narrate")).build());
        this.lightModeButton.visible = PokecubeCore.getConfig().darkMode;
        this.lightModeButton.setAlpha(0);
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
        if (this.menu.tile.confirmed[0]) ((AbstractWidget) this.renderables.get(0))
                .setTooltip(Tooltip.create(Component.translatable("block.trade_machine.trade_confirmed.tooltip")));
        else ((AbstractWidget) this.renderables.get(0))
                .setTooltip(Tooltip.create(Component.translatable("block.trade_machine.trade.tooltip")));

        if (this.menu.tile.confirmed[1]) ((AbstractWidget) this.renderables.get(1))
                .setTooltip(Tooltip.create(Component.translatable("block.trade_machine.trade_confirmed.tooltip")));
        else ((AbstractWidget) this.renderables.get(1))
                .setTooltip(Tooltip.create(Component.translatable("block.trade_machine.trade.tooltip")));
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
