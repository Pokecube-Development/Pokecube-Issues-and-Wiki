package pokecube.core.client.gui.pokemob.tabs;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import pokecube.core.client.gui.pokemob.GuiPokemob;
import pokecube.core.client.gui.pokemob.GuiPokemobHelper;
import pokecube.core.inventory.pokemob.PokemobContainer;

public abstract class Tab
{
    protected final GuiPokemob parent;

    protected List<AbstractWidget> ours = Lists.newArrayList();

    public boolean active = false;

    public int width;
    public int height;
    public int imageWidth;
    public int imageHeight;
    public PokemobContainer menu;

    public ResourceLocation icon = null;

    private int index;
    private boolean hovored = false;

    public Tab(GuiPokemob parent)
    {
        this.parent = parent;
        this.menu = parent.getMenu();
    }

    public void setIndex(int index)
    {
        this.index = index;
    }

    public void setEnabled(boolean active)
    {
        if (active == this.active) return;
        this.active = active;
        this.clear();
        if (this.active)
        {
            this.init();
        }
        ours.forEach(widget -> {
            widget.visible = active;
        });
    }

    public <T extends AbstractWidget> T addRenderableWidget(T widget)
    {
        parent.addRenderableWidget(widget);
        this.ours.add(widget);
        return widget;
    }

    public boolean isHovored()
    {
        return hovored;
    }

    public void updateHovored(double mx, double my)
    {
        int k = (this.width - this.imageWidth) / 2;
        int l = (this.height - this.imageHeight) / 2;

        l -= 30;
        k += (index + 1) * 28;

        int l2 = l + 32;
        int k2 = k + 28;

        boolean inY = my < l2 && my > l;
        boolean inX = mx < k2 && mx > k;

        this.hovored = inY && inX;
    }

    public final void clear()
    {
        ours.forEach(widget -> {
            parent.removeWidget(widget);
        });
        ours.clear();
    }

    public abstract void init();

    public void renderBg(final PoseStack mat, final float partialTicks, final int mouseX, final int mouseY)
    {
        if (this.menu.pokemob != null)
        {
            final int k = (this.width - this.imageWidth) / 2;
            final int l = (this.height - this.imageHeight) / 2;
            Mob mob = this.menu.pokemob.getEntity();

            float f = 30;
            float yBodyRot = mob.yBodyRot;
            float yBodyRotO = mob.yBodyRotO;
            float yHeadRot = mob.yHeadRot;
            float yHeadRotO = mob.yHeadRotO;

            mob.yBodyRot = mob.yBodyRotO = 180.0F + f * 20.0F;
            mob.yHeadRot = mob.yHeadRotO = mob.yBodyRot;

            GuiPokemobHelper.renderMob(mat, mob, k, l, 0, 0, 0, 0, 1, partialTicks);

            mob.yBodyRot = yBodyRot;
            mob.yBodyRotO = yBodyRotO;
            mob.yHeadRot = yHeadRot;
            mob.yHeadRotO = yHeadRotO;
        }
    }

    public void render(final PoseStack mat, final int x, final int y, final float z)
    {}

    public void renderLabels(final PoseStack mat, final int mouseX, final int mouseY)
    {
        if (this.menu.pokemob != null)
        {
            parent.font.draw(mat, this.menu.pokemob.getDisplayName(), 8.0F, this.imageHeight - 160 + 2, 4210752);
        }
    }

    public boolean charTyped(final char typedChar, final int keyCode)
    {
        return false;
    }

    public boolean keyPressed(int code, int unk1, int unk2)
    {
        return false;
    }

    public boolean mouseClicked(final double x, final double y, final int mouseButton)
    {
        return false;
    }
}