package pokecube.core.client.gui.watch;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.RenderComponentsUtil;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.ClickEvent.Action;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.util.LineEntry;
import pokecube.core.client.gui.watch.util.LineEntry.IClickListener;
import pokecube.core.client.gui.watch.util.ListPage;
import pokecube.core.database.rewards.XMLRewardsHandler.FreeBookParser.FreeTranslatedReward;
import pokecube.core.database.rewards.XMLRewardsHandler.FreeBookParser.PagesFile;
import pokecube.core.database.rewards.XMLRewardsHandler.FreeBookParser.PagesFile.Page;
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

    private int                        index = 0;
    private final Map<String, Integer> refs  = Maps.newHashMap();

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
            final int max = this.list.getMaxScroll();
            if (clickevent != null) if (clickevent.getAction() == Action.CHANGE_PAGE)
            {
                final String event = clickevent.getValue();
                if (this.refs.containsKey(event))
                {
                    final int lines = this.refs.get(event);
                    final double scrollTo = Math.min(max, this.list.itemHeight() * lines);
                    this.list.skipTo(scrollTo);
                }
                else
                {
                    int page = 0;
                    try
                    {
                        page = Integer.parseInt(clickevent.getValue());
                    }
                    catch (final NumberFormatException e)
                    {
                        e.printStackTrace();
                    }
                    for (int i = 0; i < this.list.getSize(); i++)
                    {
                        final WikiLine line = (WikiLine) this.list.getEntry(i);
                        if (line.page == page)
                        {
                            final double scrollTo = Math.min(max, this.list.itemHeight() * i + this.list
                                    .getScrollAmount());
                            this.list.skipTo(scrollTo + this.list.getScrollAmount());
                            return true;
                        }
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
        final ITextComponent next = new StringTextComponent(">");
        final ITextComponent prev = new StringTextComponent("<");
        this.addButton(new Button(x + 64, y - 70, 12, 12, next, b ->
        {
            this.index++;
            this.setList();
        }));
        this.addButton(new Button(x - 76, y - 70, 12, 12, prev, b ->
        {
            this.index--;
            this.setList();
        }));
        this.setList();
    }

    @Override
    public void render(final MatrixStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {
        final int offsetX = (this.watch.width - 160) / 2 + 10;
        final int offsetY = (this.watch.height - 160) / 2 + 20;
        AbstractGui.fill(mat, offsetX - 2, offsetY - 1, offsetX + 132, offsetY + 122, 0xFFFDF8EC);
        super.render(mouseX, mouseY, partialTicks);
    }

    private void setList()
    {
        this.refs.clear();
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
        final FreeTranslatedReward book = books.get(this.index);
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
        final boolean item_book = !book.page_file;
        final String lang = this.minecraft.getLanguageManager().getCurrentLanguage().getCode().toLowerCase(Locale.ROOT);
        if (item_book)
        {
            final ItemStack bookStack = books.get(this.index).getInfoStack(lang);
            if (!bookStack.hasTag()) return;
            final CompoundNBT tag = bookStack.getTag();
            final ListNBT bookPages = tag.getList("pages", 8);
            ITextComponent line;
            for (int i = 0; i < bookPages.size(); i++)
            {
                final ITextComponent page = ITextComponent.Serializer.fromJsonLenient(bookPages.getString(i));
                final List<ITextComponent> list = RenderComponentsUtil.splitText(page, 120, this.font, true, true);
                for (final ITextComponent element : list)
                {
                    line = element;
                    final LineEntry wikiline = new WikiLine(this.list, -5, 0, this.font, line, i).setClickListner(
                            listener);
                    this.list.addEntry(wikiline);
                }
            }
        }
        else
        {
            final PagesFile pages = book.getInfoBook(lang);
            if (pages == null) return;
            final String ref_pattern = "\\{_ref_:.*\\}";
            final Pattern ref = Pattern.compile(ref_pattern);
            final String link_pattern = "\\{_link_:.*\\}";
            final Pattern link = Pattern.compile(link_pattern);

            int pagenum = 0;
            try
            {
                ITextComponent entry;
                for (final Page page : pages.pages)
                {
                    for (String line : page.lines)
                    {
                        final String refin = "†";
                        final String linkin = "‡";
                        String ref_val = "";
                        String link_val = "";
                        Matcher match = link.matcher(line);
                        // We have a link
                        if (match.find())
                        {
                            link_val = match.group();
                            line = line.replace(link_val, linkin);
                            link_val = link_val.replace("{_link_:", "").replace("}", "");
                        }
                        match = ref.matcher(line);
                        // We have a ref
                        if (match.find())
                        {
                            ref_val = match.group();
                            line = line.replace(ref_val, refin);
                            ref_val = ref_val.replace("{_ref_:", "").replace("}", "");
                        }

                        final ITextComponent comp = new StringTextComponent(line);
                        final List<ITextComponent> list = RenderComponentsUtil.splitText(comp, 120, this.font, true,
                                true);
                        for (int j = 0; j < list.size(); j++)
                        {
                            entry = list.get(j);
                            String text = entry.getString();
                            final Style style = entry.getStyle();
                            // We have a link
                            if (text.contains(linkin))
                            {
                                text = text.replace(linkin, "");
                                entry = new StringTextComponent(text);
                                style.setClickEvent(new ClickEvent(Action.CHANGE_PAGE, link_val));
                                entry.setStyle(style);
                            }
                            // We have a ref
                            if (text.contains(refin))
                            {
                                text = text.replace(refin, "");
                                entry = new StringTextComponent(text);
                                entry.setStyle(style);
                                this.refs.put(ref_val, this.list.getSize());
                            }
                            final LineEntry wikiline = new WikiLine(this.list, -5, 0, this.font, entry, pagenum)
                                    .setClickListner(listener);
                            this.list.addEntry(wikiline);
                        }
                    }
                    final LineEntry wikiline = new WikiLine(this.list, 0, 0, this.font, new StringTextComponent(""),
                            pagenum);
                    this.list.addEntry(wikiline);
                    pagenum++;
                }
            }
            catch (final Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        this.children.add(this.list);
    }
}
