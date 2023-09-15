package pokecube.adventures.client.gui.items;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import pokecube.adventures.items.bag.BagContainer;
import pokecube.core.impl.PokecubeMod;
import thut.core.common.ThutCore;
import thut.lib.TComponent;

public class Bag<T extends BagContainer> extends AbstractContainerScreen<T>
{

    String page;
    EditBox textFieldSelectedBox;
    EditBox textFieldBoxName;
    EditBox textFieldSearch;
    Button searchButton;

    private String  boxName = "1";
    boolean         release = false;

    public Bag(final T container, final Inventory ivplay, final Component name)
    {
        super(container, ivplay, name);
        this.imageWidth = 176;
        this.imageHeight = 240;
        this.page = container.getPageNb();
        this.boxName = container.getPage();
    }

    @Override
    public boolean keyPressed(final int keyCode, final int b, final int c)
    {
        if (this.textFieldSearch.isFocused() && (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_TAB
                || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER))
        {
//            this.textFieldSearch.setFocused(false);
            return false;
        }

        if (this.textFieldSearch.isFocused() && keyCode == GLFW.GLFW_KEY_E)
        {
            this.textFieldSearch.setFocused(true);
            return true;
        }

        if (this.textFieldBoxName.isFocused() && (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_TAB
                || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER))
        {
//            this.textFieldBoxName.setFocused(false);
            return false;
        }

        if (this.textFieldBoxName.isFocused() && keyCode == GLFW.GLFW_KEY_E)
        {
            this.textFieldBoxName.setFocused(true);
            return true;
        }

        if (this.textFieldSelectedBox.isFocused() && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER))
        {
            final String entry = this.textFieldSelectedBox.getValue();
            int number = 1;
            try
            {
                number = Integer.parseInt(entry);
            }
            catch (final Exception e)
            {
                e.printStackTrace();
            }
            number = Math.max(1, Math.min(number, this.menu.inv.boxCount()));
            this.menu.gotoInventoryPage(number);
            return true;
        }
//        if (this.textFieldBoxName.isFocused() && keyCode != GLFW.GLFW_KEY_BACKSPACE) return true;
//        else if (keyCode == GLFW.GLFW_KEY_ENTER && this.textFieldBoxName.isFocused()) return true;
        return super.keyPressed(keyCode, b, c);
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float f, final int i, final int j)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, new ResourceLocation(PokecubeMod.ID, "textures/gui/pcgui.png"));
        final int x = (this.width - this.imageWidth) / 2;
        final int y = (this.height - this.imageHeight) / 2;

        graphics.blit(new ResourceLocation(PokecubeMod.ID, "textures/gui/pcgui.png"), x, y,
                0, 0, this.imageWidth + 1, this.imageHeight + 1);
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int par1, final int par2)
    {
        String text = Component.translatable("item.pokecube_adventures.bag").getString();
        graphics.drawString(this.font, text, 8, 6, 4210752, false);
        graphics.drawString(this.font, this.playerInventoryTitle.getString(),
                8, this.imageHeight - 94 + 2, 4210752, false);
    }

    @Override
    public void init()
    {
        super.init();
        final int x = this.width / 2 - 88;
        final int y = this.height / 2 - 120;

        this.textFieldBoxName = new EditBox(this.font,
                x + 81, y + 6, 76, 10, TComponent.translatable(this.boxName));
        this.textFieldBoxName.setTooltip(Tooltip.create(Component.translatable("block.pc.rename.tooltip")));
        this.textFieldBoxName.setBordered(false);

        this.textFieldSelectedBox = new EditBox(this.font,
                x + 20, y + 128, 22, 10, TComponent.literal(this.page));
        this.textFieldSelectedBox.setTooltip(Tooltip.create(Component.translatable("block.pc.page.tooltip")));
        this.textFieldSelectedBox.setBordered(false);

        this.textFieldSearch = new EditBox(this.font,
                x + 81, y + 128, 76, 10, TComponent.translatable("block.pc.search.narrate"));
        this.textFieldSearch.setTooltip(Tooltip.create(Component.translatable("block.pc.search.tooltip")));
        this.textFieldSearch.setBordered(false);

        final Component rename = TComponent.translatable("block.pc.rename");
        this.addRenderableWidget(new Button.Builder(rename, (b) -> {
            final String box = this.textFieldBoxName.getValue();
            if (!box.equals(this.boxName)) this.menu.changeName(box);
            this.boxName = box;
        }).bounds(x + 159, y + 5, 10, 10)
                .createNarration(supplier -> TComponent.translatable("block.pc.rename.narrate")).build());

        final Component prev = TComponent.translatable("block.pc.previous");
        this.addRenderableWidget(new Button.Builder(prev, (b) -> {
            this.menu.updateInventoryPages((byte) -1, this.minecraft.player.getInventory());
            this.textFieldSelectedBox.setValue(this.menu.getPageNb());
            this.textFieldBoxName.setValue(this.menu.getPage());
        }).bounds(x + 7, y + 127, 10, 10)
                .createNarration(supplier -> Component.translatable("block.pc.previous.narrate")).build());

        final Component next = TComponent.translatable("block.pc.next");
        this.addRenderableWidget(new Button.Builder(next, (b) -> {
            this.menu.updateInventoryPages((byte) 1, this.minecraft.player.getInventory());
            this.textFieldSelectedBox.setValue(this.menu.getPageNb());
            this.textFieldBoxName.setValue(this.menu.getPage());
        }).bounds(x + 43, y + 127, 10, 10)
                .createNarration(supplier -> Component.translatable("block.pc.next.narrate")).build());

        final Component search = TComponent.translatable("block.pc.search");
        this.searchButton = this.addRenderableWidget(new Button.Builder(search, (b) -> {
        }).bounds(x + 159, y + 127, 10, 10)
                .createNarration(supplier -> TComponent.translatable("block.pc.search.narrate")).build());

        this.addRenderableWidget(this.textFieldBoxName);
        this.addRenderableWidget(this.textFieldSelectedBox);
        this.addRenderableWidget(this.textFieldSearch);

        this.textFieldSelectedBox.value = this.page;
        this.textFieldBoxName.value = this.boxName;
    }

    /** Called when the screen is unloaded. Used to disable keyboard repeat
     * events */
    @Override
    public void removed()
    {
        if (this.minecraft.player != null) this.menu.removed(this.minecraft.player);
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float f)
    {
        this.renderBackground(graphics);
        super.render(graphics,mouseX, mouseY, f);

        for (int i = 0; i < 54; i++)
        {
            if (!this.textFieldSearch.getValue().isEmpty() && this.textFieldSearch != null)
            {
                final ItemStack stack = this.menu.inv.getItem(i + 54 * this.menu.inv.getPage());
                if (stack.isEmpty()) continue;
                final int x = i % 9 * 18 + this.width / 2 - 80;
                final int y = i / 9 * 18 + this.height / 2 - 102;
                final String name = stack == null ? "" : stack.getHoverName().getString();
                if (name.isEmpty() || !ThutCore.trim(name).contains(ThutCore.trim(this.textFieldSearch.getValue())))
                {
                    final int slotColor = 0x55FF0000;
                    graphics.fill(RenderType.guiOverlay(), x, y, x + 16, y + 16, slotColor);
                } else
                {
                    final int slotColor = 0x5500FF00;
                    graphics.fill(RenderType.guiOverlay(), x, y, x + 16, y + 16, slotColor);
                }
            }
        }
        this.renderTooltip(graphics,mouseX, mouseY);
    }
}