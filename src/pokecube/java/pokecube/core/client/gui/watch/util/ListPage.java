package pokecube.core.client.gui.watch.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import pokecube.core.client.gui.helper.INotifiedEntry;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.GuiPokeWatch;

public abstract class ListPage<T extends AbstractSelectionList.Entry<T>> extends WatchPage
{
    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.parse("pokecube:widgets/scroller");
    private static final ResourceLocation SCROLLER_BACKGROUND_SPRITE = ResourceLocation.parse(
            "pokecube:widgets/scroller_background");
    private static final ResourceLocation SCROLLER_SPRITE_NM = ResourceLocation.parse("pokecube:widgets/scroller_nm");
    private static final ResourceLocation SCROLLER_BACKGROUND_SPRITE_NM = ResourceLocation.parse(
            "pokecube:widgets/scroller_background_nm");

    protected ScrollGui<T> list;

    public ListPage(final Component title, final GuiPokeWatch watch, final ResourceLocation day,
            final ResourceLocation night)
    {
        super(title, watch, day, night);
    }

    public void drawTitle(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks)
    {
        final int x = (this.watch.width - 160) / 2 + 80;
        final int y = (this.watch.height - 160) / 2 + 8;
        final int colour = 0xFF78C850;
        graphics.drawCenteredString(this.font, this.getTitle().getString(), x, y, colour);
    }

    protected void postInitList()
    {
        boolean nm = GuiPokeWatch.nightMode;
        list.setScrollSprite(nm ? SCROLLER_SPRITE_NM : SCROLLER_SPRITE);
        list.setScrollSpriteBG(nm ? SCROLLER_BACKGROUND_SPRITE_NM : SCROLLER_BACKGROUND_SPRITE);
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

    private Runnable updateRunnable;

    protected void scheduleUpdate(Runnable toRun)
    {
        this.updateRunnable = toRun;
    }

    @Override
    public void onPageOpened()
    {
        this.children().clear();
        this.initList();
        postInitList();
        super.onPageOpened();
    }

    @Override
    public boolean keyPressed(int keyCode, int b, int c)
    {
        return list.keyPressed(keyCode, b, c) || super.keyPressed(keyCode, b, c);
    }

    @Override
    public void renderPage(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks)
    {
        if (this.updateRunnable != null)
        {
            this.updateRunnable.run();
            this.updateRunnable = null;
        }
        this.drawTitle(graphics, mouseX, mouseY, partialTicks);
    }
}
