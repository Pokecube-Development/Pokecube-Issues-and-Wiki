package pokecube.core.client.gui.watch.util;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.GuiPokeWatch;

public abstract class ListPage<T extends AbstractSelectionList.Entry<T>> extends WatchPage
{
    protected ScrollGui<T> list;
    /**
     * Set this to true if the page handles rendering the list itself.
     */
    protected boolean      handlesList = false;

    public ListPage(final Component title, final GuiPokeWatch watch, final ResourceLocation day, final ResourceLocation night)
    {
        super(title, watch, day, night);
    }

    public void drawTitle(final PoseStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {
        final int x = (this.watch.width - 160) / 2 + 80;
        final int y = (this.watch.height - 160) / 2 + 8;
        final int colour = 0xFF78C850;
        GuiComponent.drawCenteredString(mat, this.font, this.getTitle().getString(), x, y, colour);
    }

    @Override
    public void init()
    {
        this.children().clear();
        super.init();
        this.initList();
    }

    public void initList()
    {
        if (this.list != null) this.children.remove(this.list);
    }

    @Override
    public void onPageOpened()
    {
        this.children().clear();
        this.initList();
        super.onPageOpened();
    }

    @Override
    public void render(final PoseStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {
        this.drawTitle(mat, mouseX, mouseY, partialTicks);
        super.render(mat, mouseX, mouseY, partialTicks);
        // Draw the list
        if (!this.handlesList && this.list != null) this.list.render(mat, mouseX, mouseY, partialTicks);
    }
}
