package pokecube.adventures.client.gui.trainer.editor.pages.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.network.chat.Component;
import pokecube.adventures.client.gui.trainer.editor.EditorGui;
import pokecube.core.client.gui.helper.INotifiedEntry;
import pokecube.core.client.gui.helper.ScrollGui;

public abstract class ListPage<T extends AbstractSelectionList.Entry<T>> extends Page
{
    protected ScrollGui<T> list;

    public ListPage(final Component title, final EditorGui parent)
    {
        super(title, parent);
    }

    public void drawTitle(final GuiGraphics graphics)
    {
        final int x = (this.parent.width - 160) / 2 + 80;
        final int y = (this.parent.height - 160) / 2 + 8;
        graphics.drawCenteredString(this.font, this.getTitle().getString(), x, y, 0xFFFFFFFF);
    }

    protected void postInitList()
    {
        this.addRenderableWidget(this.list);
    }

    @Override
    public void init()
    {
        super.init();
        this.initList();
        postInitList();
    }

    public void initList()
    {
        if (this.list != null)
        {
            this.removeWidget(this.list);
            this.list.children().forEach(entry -> {
                if (entry instanceof INotifiedEntry notified) notified.addOrRemove(this::removeWidget);
            });
        }
    }

    @Override
    public void onPageOpened()
    {
        this.initList();
        super.onPageOpened();
    }

    @Override
    public void renderPage(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks)
    {
        this.drawTitle(graphics);
    }
}
