package pokecube.core.client.gui.pokemob;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import pokecube.core.client.Resources;
import pokecube.core.client.gui.helper.Rectangle;
import pokecube.core.client.gui.pokemob.tabs.AI;
import pokecube.core.client.gui.pokemob.tabs.Routes;
import pokecube.core.client.gui.pokemob.tabs.Storage;
import pokecube.core.client.gui.pokemob.tabs.Tab;
import pokecube.core.inventory.pokemob.PokemobContainer;
import thut.lib.TComponent;

public class GuiPokemob extends AbstractContainerScreen<PokemobContainer>
{
    private static final ResourceLocation TAB_TEX = new ResourceLocation(
            "textures/gui/container/creative_inventory/tabs.png");

    List<Tab> modules = Lists.newArrayList();
    List<Rectangle> tabs = Lists.newArrayList();
    public int moduleIndex = 0;

    public GuiPokemob(PokemobContainer container, Inventory inv, Component name)
    {
        super(container, inv, name);
        this.moduleIndex = container.mode;
        modules.add(new pokecube.core.client.gui.pokemob.tabs.Inventory(this));
        modules.add(new AI(this));
        modules.add(new Routes(this));
        modules.add(new Storage(this));
    }

    @Override
    /**
     * @Override to make public for removing widgets
     */
    public void removeWidget(GuiEventListener p_169412_)
    {
        super.removeWidget(p_169412_);
    }

    @Override
    public void init()
    {
        super.init();
        this.tabs.clear();
        final int k = (this.width - this.imageWidth) / 2;
        final int l = (this.height - this.imageHeight) / 2;
        for (int i = 0; i < 4; i++)
        {
            int x0 = k + (i + 1) * 28;
            int y0 = l - 28;
            int x1 = x0 + 28;
            int y1 = y0 + 32;
            tabs.add(new Rectangle(x0, y0, x1, y1));
        }
        AtomicInteger counter = new AtomicInteger();
        modules.forEach(m -> {
            m.clear();
            m.width = this.width;
            m.height = this.height;
            m.imageHeight = this.imageHeight;
            m.imageWidth = this.imageWidth;
            m.tabBounds = this.tabs.get(counter.getAndIncrement() % 4);
            m.setEnabled(false);
        });
        modules.get(moduleIndex).setEnabled(true);
    }

    /** Draws the screen and all the components in it. */
    @Override
    public void render(final PoseStack mat, final int x, final int y, final float z)
    {
        super.renderBackground(mat);
        super.render(mat, x, y, z);
        modules.get(moduleIndex).render(mat, x, y, z);
        for (int i = 0; i < modules.size(); i++)
        {
            Tab t = modules.get(i);
            if (t.isHovored())
            {
                this.renderComponentTooltip(mat, Lists.newArrayList(TComponent.translatable(t.desc)), x, y);
            }
        }
        this.renderTooltip(mat, x, y);
    }

    @Override
    protected void renderBg(PoseStack pose, float tick, int mx, int my)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, Resources.GUI_POKEMOB);
        final int k = (this.width - this.imageWidth) / 2;
        final int l = (this.height - this.imageHeight) / 2;
        for (int i = 0; i < 4; i++)
        {
            Tab t = modules.get(i);
            Rectangle r = this.tabs.get(i);
            t.updateHovored(mx, my);
            if (i == moduleIndex) continue;
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, TAB_TEX);
            RenderSystem.enableBlend();
            this.blit(pose, r.x0, r.y0, 28, 0, r.w, r.h);
            if (t.icon != null)
            {
                RenderSystem.setShaderTexture(0, t.icon);
                RenderSystem.enableBlend();
                this.blit(pose, r.x0, r.y0, 0, 0, 16, 16);
            }
        }
        RenderSystem.setShaderTexture(0, Resources.GUI_POKEMOB);
        this.blit(pose, k, l, 0, 0, this.imageWidth, this.imageHeight);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TAB_TEX);
        RenderSystem.enableBlend();
        Tab t = modules.get(moduleIndex);
        Rectangle r = this.tabs.get(moduleIndex);
        this.blit(pose, r.x0, r.y0, 28, 32, r.w, r.h);
        if (t.icon != null)
        {
            RenderSystem.setShaderTexture(0, t.icon);
            RenderSystem.enableBlend();
            this.blit(pose, r.x0, r.y0, 0, 32, 16, 16);
        }
        RenderSystem.setShaderTexture(0, Resources.GUI_POKEMOB);
        modules.get(moduleIndex).renderBg(pose, tick, mx, my);
    }

    @Override
    public boolean keyPressed(int code, int unk1, int unk2)
    {
        return modules.get(moduleIndex).keyPressed(code, unk1, unk2) || super.keyPressed(code, unk1, unk2);
    }

    @Override
    protected void renderLabels(PoseStack mat, int p_97809_, int p_97810_)
    {
        this.font.draw(mat, this.playerInventoryTitle, 8.0F, this.imageHeight - 96 + 2, 4210752);

        final int k = 6;
        final int l = this.imageHeight - 152;

        for (int i = 0; i < modules.size(); i++)
        {
            Tab t = modules.get(i);
            if (t.icon == null)
            {
                Component tab = TComponent.translatable(t.text);
                this.font.draw(mat, tab, k + 28 * (i + 1), l - 28, 4210752);
            }
        }
        modules.get(moduleIndex).renderLabels(mat, p_97809_, p_97810_);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button)
    {
        for (var w : this.children)
        {
            if (w instanceof EditBox b) b.setFocus(false);
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            for (int i = 0; i < modules.size(); i++)
            {
                Tab t = modules.get(i);
                if (t.isHovored())
                {
                    if (i != this.moduleIndex)
                    {
                        modules.get(moduleIndex).setEnabled(false);
                        t.setEnabled(true);
                        this.moduleIndex = i;
                    }
                    break;
                }
            }
        }
        return modules.get(moduleIndex).mouseClicked(mx, my, button) || super.mouseClicked(mx, my, button);
    }
}
