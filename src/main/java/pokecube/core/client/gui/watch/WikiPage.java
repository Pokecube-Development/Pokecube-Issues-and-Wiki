package pokecube.core.client.gui.watch;

import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.RenderComponentsUtil;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.ClickEvent.Action;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.util.LineEntry;
import pokecube.core.client.gui.watch.util.LineEntry.IClickListener;
import pokecube.core.client.gui.watch.util.ListPage;
import pokecube.core.database.rewards.XMLRewardsHandler.FreeBookParser.FreeTranslatedReward;
import pokecube.core.handlers.PokedexInspector;
import pokecube.core.handlers.PokedexInspector.IInspectReward;

public class WikiPage extends ListPage<LineEntry>
{
    public static class WikiLine extends LineEntry
    {
        final int page;

        public WikiLine(final ScrollGui<LineEntry> list, final int y0, final int y1, final FontRenderer fontRender,
                final ITextComponent line, final int page)
        {
            super(list, y0, y1, fontRender, line, 0);
            this.page = page;
        }
    }

    private int index = 0;

    public WikiPage(final GuiPokeWatch watch)
    {
        super(new TranslationTextComponent("pokewatch.title.wiki"), watch);
    }

    @Override
    public boolean handleComponentClicked(final ITextComponent component)
    {
        if (component != null)
        {
            ClickEvent clickevent = component.getStyle().getClickEvent();
            if (clickevent == null) for (final ITextComponent sib : component.getSiblings())
                if (sib != null && (clickevent = sib.getStyle().getClickEvent()) != null) break;
            if (clickevent != null) if (clickevent.getAction() == Action.CHANGE_PAGE)
            {
                final int page = Integer.parseInt(clickevent.getValue());
                final int max = this.list.getMaxScroll();
                for (int i = 0; i < this.list.getSize(); i++)
                {
                    final WikiLine line = (WikiLine) this.list.getEntry(i);
                    if (line.page == page)
                    {
                        final double scrollTo = Math.min(max, this.list.itemHeight() * i + this.list.getScrollAmount());
                        System.out.println(i + " " + scrollTo);
                        this.list.skipTo(scrollTo + this.list.getScrollAmount());
                        return true;
                    }
                }
            }
            return false;
        }
        return super.handleComponentClicked(component);
    }

    @Override
    public void initList()
    {
        super.initList();
        final int x = this.watch.width / 2;
        final int y = this.watch.height / 2 - 5;
        final String next = I18n.format("block.pc.next");
        final String prev = I18n.format("block.pc.previous");
        this.addButton(new Button(x + 26, y - 69, 50, 12, next, b ->
        {
            this.index++;
            this.setList();
        }));
        this.addButton(new Button(x - 76, y - 69, 50, 12, prev, b ->
        {
            this.index--;
            this.setList();
        }));
        this.setList();
    }

    @Override
    public void render(final int mouseX, final int mouseY, final float partialTicks)
    {
        final int offsetX = (this.watch.width - 160) / 2 + 10;
        final int offsetY = (this.watch.height - 160) / 2 + 20;
        AbstractGui.fill(offsetX - 2, offsetY - 1, offsetX + 132, offsetY + 122, 0xFFFDF8EC);
        super.render(mouseX, mouseY, partialTicks);
    }

    private void setList()
    {
        final List<FreeTranslatedReward> books = Lists.newArrayList();
        for (final IInspectReward reward : PokedexInspector.rewards)
            if (reward instanceof FreeTranslatedReward) books.add((FreeTranslatedReward) reward);
        books.sort((o1, o2) -> o1.key.compareTo(o2.key));
        final int offsetX = (this.watch.width - 160) / 2 + 20;
        final int offsetY = (this.watch.height - 160) / 2 + 20;
        final int height = 120;

        if (this.list != null) this.children.remove(this.list);

        this.list = new ScrollGui<>(this, this.minecraft, 135, height, this.font.FONT_HEIGHT + 2, offsetX, offsetY);
        if (books.isEmpty()) return;
        if (this.index < 0) this.index = books.size() - 1;
        if (this.index >= books.size()) this.index = 0;

        final WikiPage thisObj = this;
        final IClickListener listener = new IClickListener()
        {
            @Override
            public boolean handleClick(final ITextComponent component)
            {
                return thisObj.handleComponentClicked(component);
            }

            @Override
            public void handleHovor(final ITextComponent component, final int x, final int y)
            {
            }
        };
        final ItemStack book = books.get(this.index).getInfoBook(this.minecraft.getLanguageManager()
                .getCurrentLanguage().getCode().toLowerCase(Locale.ROOT));
        if (!book.hasTag()) return;
        final CompoundNBT tag = book.getTag();
        final ListNBT bookPages = tag.getList("pages", 8);
        ITextComponent line;
        for (int i = 0; i < bookPages.size(); i++)
        {
            final ITextComponent page = ITextComponent.Serializer.fromJsonLenient(bookPages.getString(i));
            final List<ITextComponent> list = RenderComponentsUtil.splitText(page, 120, this.font, true, true);
            for (int j = 0; j < list.size(); j++)
            {
                line = list.get(j);
                final LineEntry wikiline = new WikiLine(this.list, offsetY + 4, offsetY + height + 4, this.font, line,
                        i).setClickListner(listener);
                this.list.addEntry(wikiline);
            }
        }
        this.children.add(this.list);
    }
}
