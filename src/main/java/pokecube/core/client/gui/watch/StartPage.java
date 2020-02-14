package pokecube.core.client.gui.watch;

import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.util.ListPage;
import pokecube.core.client.gui.watch.util.PageEntry;
import pokecube.core.client.gui.watch.util.WatchPage;

public class StartPage extends ListPage<PageEntry>
{

    public StartPage(final GuiPokeWatch watch)
    {
        super(new TranslationTextComponent("pokewatch.title.start"), watch);
    }

    @Override
    public void initList()
    {
        super.initList();
        final int offsetX = (this.watch.width - 160) / 2 + 10;
        final int offsetY = (this.watch.height - 160) / 2 + 30;
        final int height = 101;
        this.list = new ScrollGui<>(this, this.minecraft, 146, height, 20, offsetX, offsetY);
        for (final Class<? extends WatchPage> page : GuiPokeWatch.PAGELIST)
            if (page != StartPage.class) this.list.addEntry(new PageEntry(this, this.watch.createPage(
                    GuiPokeWatch.PAGELIST.indexOf(page)), GuiPokeWatch.PAGELIST.indexOf(page), offsetX, offsetY));
        this.children.add(this.list);
    }
}
