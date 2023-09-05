package pokecube.core.client.gui.watch.util;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import pokecube.core.client.gui.helper.INotifiedEntry;
import pokecube.core.client.gui.helper.TexButton;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.network.packets.PacketPokedex;

public class PageEntry extends AbstractSelectionList.Entry<PageEntry> implements INotifiedEntry
{
    public final Button button;
    final int           top;
    
    public PageEntry(final WatchPage parent, final WatchPage page, final int index, final int offsetY, Button button)
    {
        this.top = offsetY;
        this.button = button;
        this.button.visible = false;
        this.button.active = false;
        parent.addRenderableWidget(this.button);
    }

    public PageEntry(final WatchPage parent, final WatchPage page, final int index, final int offsetX,
            final int offsetY)
    {
        this.top = offsetY;

        this.button = new  TexButton.Builder(page.getTitle(), (b) -> {
            parent.watch.changePage(index);
        }).build();

        this.button.visible = false;
        this.button.active = false;
        parent.addRenderableWidget(this.button);
    }

    @Override
    public void preRender(final int slotIndex, final int x, final int y, final int listWidth, final int slotHeight,
            final int mouseX, final int mouseY, final boolean isSelected, final float partialTicks)
    {
        this.button.visible = false;
        this.button.active = false;
    }

    @Override
    public void render(final GuiGraphics graphics, final int slotIndex, final int x, final int y, final int listWidth,
                       final int slotHeight, final int mouseX, final int mouseY, final boolean isSelected,
                       final float partialTicks)
    {
        // Note that this seems to send these backwards.
        this.button.setX(y);
        this.button.setY(x);
        this.button.visible = true;
        this.button.active = true;
    }
}