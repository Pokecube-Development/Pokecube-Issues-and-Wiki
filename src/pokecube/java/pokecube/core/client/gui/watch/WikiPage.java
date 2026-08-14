package pokecube.core.client.gui.watch;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import pokecube.api.PokecubeAPI;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.helper.TexButton;
import pokecube.core.client.gui.helper.TexButton.UVImgRender;
import pokecube.core.client.gui.watch.util.LineEntry;
import pokecube.core.client.gui.watch.util.LineEntry.IClickListener;
import pokecube.core.client.gui.watch.util.ListPage;
import pokecube.core.database.rewards.XMLRewardsHandler.FreeBookParser.FreeTranslatedReward;
import pokecube.core.database.rewards.XMLRewardsHandler.FreeBookParser.PagesFile;
import pokecube.core.database.rewards.XMLRewardsHandler.FreeBookParser.PagesFile.Page;
import pokecube.core.handlers.PokedexInspector;
import pokecube.core.handlers.PokedexInspector.IInspectReward;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WikiPage extends ListPage<LineEntry>
{
    public static class WikiLine extends LineEntry
    {
        final int page;

        public WikiLine(final ScrollGui<LineEntry> list, final int x0, final int y1, final Font fontRender,
                final FormattedCharSequence line, final int page)
        {
            super(list, x0, y1, fontRender, line, 0);
            this.page = page;
        }
    }

    public static final ResourceLocation TEX_DM = GuiPokeWatch.makeWatchTexture("pokewatchgui_wiki");
    public static final ResourceLocation TEX_NM = GuiPokeWatch.makeWatchTexture("pokewatchgui_wiki_nm");

    private int index = 0;
    private final Map<String, Integer> refs = Maps.newHashMap();

    public WikiPage(final GuiPokeWatch watch)
    {
        super(Component.translatable("pokewatch.title.wiki"), watch, WikiPage.TEX_DM, WikiPage.TEX_NM);
    }

    @Override
    public boolean handleComponentClicked(final Style component)
    {
        if (component != null)
        {
            final ClickEvent clickevent = component.getClickEvent();
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
                    catch (NumberFormatException e)
                    {
                        PokecubeAPI.LOGGER.error(e);
                    }

                    for (int i = 0; i < this.list.getSize(); i++)
                    {
                        final WikiLine line = (WikiLine) this.list.getEntry(i);
                        if (line.page == page)
                        {
                            final double scrollTo = Math.min(max,
                                    this.list.itemHeight() * i + this.list.getScrollAmount());
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
        final int x = this.watch.width / 2;
        final int y = this.watch.height / 2;
        final Component next = Component.literal("");
        final Component prev = Component.literal("");

        super.initList();
        this.setList();

        final TexButton prevBtn = this.addRenderableWidget(new TexButton.Builder(prev, b -> {
            this.index--;
            super.initList();
            this.setList();
            this.postInitList();
        }).bounds(x - 116, y - 79, 12, 12).setTexture(GuiPokeWatch.getWidgetTex())
                .setRender(new UVImgRender(229, 108, 12, 12))
                .tooltip(Tooltip.create(Component.translatable("button.pokecube.pokewatch.prev_wiki.tooltip")))
                .createNarration(supplier -> Component.translatable("button.pokecube.pokewatch.prev_wiki.narrate"))
                .build());

        final TexButton nextBtn = this.addRenderableWidget(new TexButton.Builder(next, b -> {
            this.index++;
            super.initList();
            this.setList();
            this.postInitList();
        }).bounds(x + 104, y - 79, 12, 12).setTexture(GuiPokeWatch.getWidgetTex())
                .setRender(new UVImgRender(241, 108, 12, 12))
                .tooltip(Tooltip.create(Component.translatable("button.pokecube.pokewatch.next_wiki.tooltip")))
                .createNarration(supplier -> Component.translatable("button.pokecube.pokewatch.next_wiki.narrate"))
                .build());

        nextBtn.setFGColor(0x444444);
        prevBtn.setFGColor(0x444444);
    }

    private void setList()
    {
        this.refs.clear();
        final List<FreeTranslatedReward> books = Lists.newArrayList();
        for (final IInspectReward reward : PokedexInspector.rewards)
            // TODO decide on if to include the item books as well
            if (reward instanceof FreeTranslatedReward book && book.page_file) {
                // Check if the book is valid, if not skip it.
                books.add(book);
            }

        books.sort(Comparator.comparing(o -> o.key));
        final int offsetX = (this.watch.width - GuiPokeWatch.GUIW) / 2 + 16;
        final int offsetY = (this.watch.height - GuiPokeWatch.GUIH) / 2 + 37;
        final int height = this.font.lineHeight * 11; // 100
        List<GuiEventListener> entries = new ArrayList<>();
        for(var v: this.children()) if(v instanceof ScrollGui)entries.add(v);
        entries.forEach(this::removeWidget);
        this.list = new ScrollGui<>(this, this.minecraft, 228, height, this.font.lineHeight, offsetX, offsetY);

        // x - 5 / y
        if (books.isEmpty()) return;
        if (this.index < 0) this.index = books.size() - 1;
        if (this.index >= books.size()) this.index = 0;
        final FreeTranslatedReward book = books.get(this.index);
        final WikiPage thisObj = this;
        final IClickListener listener = new IClickListener()
        {
            @Override
            public boolean handleClick(final Style component)
            {
                return thisObj.handleComponentClicked(component);
            }

            @Override
            public void handleHovor(final GuiGraphics graphics, final Style component, final int x, final int y)
            {}
        };
        final boolean item_book = !book.page_file;
        final String lang = this.minecraft.getLanguageManager().getSelected().toLowerCase(Locale.ROOT);
        if (item_book)
        {
            var stack = books.get(this.index).getInfoStack(lang);
            System.out.println(stack);
            System.out.println(stack.getComponents().keySet());
            Thread.dumpStack();
            // TODO item book pages rendered here.
        }
        else
        {
            final PagesFile pages = book.getInfoBook(lang);
            if (pages == null) return;
            final String ref_pattern = "\\{_ref_:.*}";
            final Pattern ref = Pattern.compile(ref_pattern);
            final String link_pattern = "\\{_link_:.*}";
            final Pattern link = Pattern.compile(link_pattern);

            int pagenum = 0;

            for (final Page page : pages.pages)
            {
                for (String line : page.lines)
                {
                    final String refin = "\u241F";
                    final String linkin = "\u240F";
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

                    final MutableComponent comp = Component.literal(line);
                    var list = this.font.getSplitter().splitLines(comp, 215, Style.EMPTY);
                    Style style = Style.EMPTY;

                    StringBuilder fmt = new StringBuilder();

                    String _text = comp.getString();

                    for (var element : list)
                    {
                        MutableComponent entry;
                        if (element instanceof MutableComponent e)
                        {
                            entry = e;
                            style = entry.getStyle();
                        }
                        else entry = Component.literal(element.getString());
                        String text = entry.getString();

                        if (element instanceof Component c) style = c.getStyle();

                        // We have a link
                        if (text.contains(linkin))
                        {
                            text = text.replace(linkin, "");
                            entry = Component.literal(text);
                            style = style.withClickEvent(new ClickEvent(Action.CHANGE_PAGE, link_val));
                        }
                        // We have a ref
                        if (text.contains(refin))
                        {
                            text = text.replace(refin, "");
                            entry = Component.literal(text);
                            this.refs.put(ref_val, this.list.getSize());
                        }
                        if (text.contains("�"))
                        {
                            int index = _text.indexOf("�");
                            fmt = new StringBuilder();
                            while (index != -1)
                            {
                                fmt.append(_text, index, index + 2);
                                _text = _text.substring(index + 2);
                                index = _text.indexOf("�");
                            }
                        }
                        else entry = Component.literal(fmt + text);
                        entry.setStyle(style);
                        final LineEntry wikiline = new WikiLine(this.list, 0, 0, this.font, entry.getVisualOrderText(),
                                pagenum).setClickListner(listener);
                        this.list.addEntry(wikiline);
                    }
                }
                final LineEntry wikiline = new WikiLine(this.list, 0, 0, this.font,
                        Component.literal("").getVisualOrderText(), pagenum);
                this.list.addEntry(wikiline);
                pagenum++;
            }
        }
    }
}
