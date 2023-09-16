package thut.bling.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;
import thut.bling.ThutBling;
import thut.lib.TComponent;

public class SmallBag<T extends ChestMenu> extends AbstractContainerScreen<T>
{

    String  page;
    EditBox textFieldBoxName;

    private String bagName = "BagName";
    private String boxName = "Bag";
    boolean release = false;

    public SmallBag(final T container, final Inventory ivplay, final Component name)
    {
        super(container, ivplay, name);
        this.imageWidth = 176;
        this.imageHeight = 172;
    }

    @Override
    public boolean keyPressed(final int keyCode, final int b, final int c)
    {
        if (this.textFieldBoxName.isFocused() && (keyCode == GLFW.GLFW_KEY_ESCAPE
                || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER))
        {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)
            {
                final String box = this.textFieldBoxName.getValue();
                if (!box.equals(this.boxName))
                {
                    final CompoundTag tag = new CompoundTag();
                    tag.putString(bagName, box);

                    ItemStack stack = ItemStack.EMPTY;
                    stack.setTag(tag);
                }
            }
            this.textFieldBoxName.setFocused(false);
            return false;
        }

        if (this.textFieldBoxName.isFocused() && keyCode == GLFW.GLFW_KEY_E)
        {
            this.textFieldBoxName.setFocused(true);
            return true;
        }
        return super.keyPressed(keyCode, b, c);
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float f, final int i, final int j)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, new ResourceLocation(ThutBling.MODID, "textures/gui/bag.png"));
        final int x = (this.width - this.imageWidth) / 2;
        final int y = (this.height - this.imageHeight) / 2;
        graphics.blit(new ResourceLocation(ThutBling.MODID, "textures/gui/bag.png"), x, y,
                0, 0, this.imageWidth + 1, this.imageHeight + 1);
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int par1, final int par2)
    {
        ItemStack stack = ItemStack.EMPTY;
        final CompoundTag tag = stack.getTag();

        if (tag != null)
        {
            graphics.drawString(this.font, tag.getString(bagName), 8, 6, 0x330001, false);
        } else graphics.drawString(this.font, "Bag", 8, 6, 0x330001, false);

        graphics.drawString(this.font, this.playerInventoryTitle.getString(),
                8, this.imageHeight - 94 + 2, 4210752, false);
    }

    @Override
    public void init()
    {
        super.init();
        final int x = this.width / 2 - 88;
        final int y = this.height / 2 - 86;

        this.textFieldBoxName = new EditBox(this.font,
                x + 117, y + 6, 40, 10, TComponent.translatable("block.bag.rename.narrate"));
        this.textFieldBoxName.setTooltip(Tooltip.create(Component.translatable("block.bag.rename.tooltip")));
        this.textFieldBoxName.setBordered(false);
        this.textFieldBoxName.maxLength = 17;
        this.textFieldBoxName.value = "";
        this.addRenderableWidget(this.textFieldBoxName);

        final Component rename = TComponent.translatable("block.bag.rename");
        this.addRenderableWidget(new Button.Builder(rename, (b) -> {
            final String box = this.textFieldBoxName.getValue();

            if (!box.equals(this.boxName))
            {
                final CompoundTag tag = new CompoundTag();
                tag.putString(bagName, box);

                ItemStack stack = ItemStack.EMPTY;
                stack.setTag(tag);
            }
            // this.boxName = box;
        }).bounds(x + 157, y + 4, 12, 12)
                .tooltip(Tooltip.create(Component.translatable("block.bag.rename.tooltip")))
                .createNarration(supplier -> TComponent.translatable("block.bag.rename.narrate")).build());
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat
     * events
     */
    @Override
    public void removed()
    {
        if (this.minecraft.player != null) this.menu.removed(this.minecraft.player);
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float f)
    {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, f);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

//    public void changeName(final String name)
//    {
//        if (ThutCore.proxy.isClientSide())
//        {
//            final PacketBag packet = new PacketBag(PacketBag.RENAME);
//            packet.data.putString("N", name);
//            ThutBling.packets.sendToServer(packet);
//            this.boxName = packet.toString();
//        }
//    }
//
//    @OnlyIn(Dist.CLIENT)
//    public String getName()
//    {
//        if (ThutCore.proxy.isClientSide())
//        {
//            final PacketBag packet = new PacketBag(PacketBag.RENAME);
//            return packet.data.getString("N");
//        }
//        return null;
//    }
}