package pokecube.core.client.gui.watch;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.Font;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
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
import thut.lib.TComponent;

public class WikiPage extends ListPage<LineEntry>
{
    public static class WikiLine extends LineEntry
    {
        final int page;

        public WikiLine(final ScrollGui<LineEntry> list, final int y0, final int y1, final Font fontRender,
                final FormattedCharSequence line, final int page)
        {
            super(list, y0, y1, fontRender, line, 0);
            this.page = page;
        }
    }

    public static final ResourceLocation TEX_DM = GuiPokeWatch.makeWatchTexture("pokewatchgui_wiki");
    public static final ResourceLocation TEX_NM = GuiPokeWatch.makeWatchTexture("pokewatchgui_wiki_nm");

    private int index = 0;
    private final Map<String, Integer> refs = Maps.newHashMap();

    public WikiPage(final GuiPokeWatch watch)
    {
        super(TComponent.translatable("pokewatch.title.wiki"), watch, WikiPage.TEX_DM, WikiPage.TEX_NM);
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
                    int page = Integer.parseInt(clickevent.getValue());

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
        super.initList();
        final int x = this.watch.width / 2;
        final int y = this.watch.height / 2;
        final Component next = TComponent.literal("");
        final Component prev = TComponent.literal("");

        final TexButton prevBtn = this.addRenderableWidget(new TexButton(x - 116, y - 79, 12, 12, prev, b -> {
            this.index--;
            this.setList();
        }).setTex(GuiPokeWatch.getWidgetTex()).setRender(new UVImgRender(229, 108, 12, 12)));

        final TexButton nextBtn = this.addRenderableWidget(new TexButton(x + 104, y - 79, 12, 12, next, b -> {
            this.index++;
            this.setList();
        }).setTex(GuiPokeWatch.getWidgetTex()).setRender(new UVImgRender(241, 108, 12, 12)));
        this.setList();
        
        nextBtn.setFGColor(0x444444);
        prevBtn.setFGColor(0x444444);
    }

    @Override
    public void onPageOpened()
    {
        super.onPageOpened();
        final int x = (this.watch.width - GuiPokeWatch.GUIW) / 2 + 90;
        final int y = (this.watch.height - GuiPokeWatch.GUIH) / 2 + 30;

        this.addRenderableWidget(new TexButton(x - 108, y + 102, 17, 17,
                TComponent.literal(""), b ->
        {
            GuiPokeWatch.nightMode = !GuiPokeWatch.nightMode;
            this.watch.init();
        }).setTex(GuiPokeWatch.getWidgetTex()).setRender(new TexButton.UVImgRender(110, 72, 17, 17)));
    }

    @Override
    public void renderBackground(final PoseStack matrixStack)
    {
        super.renderBackground(matrixStack);
    }

    private void setList()
    {
        this.refs.clear();
        final List<FreeTranslatedReward> books = Lists.newArrayList();
        for (final IInspectReward reward : PokedexInspector.rewards)
            if (reward instanceof FreeTranslatedReward) books.add((FreeTranslatedReward) reward);

        books.sort((o1, o2) -> o1.key.compareTo(o2.key));
        final int offsetX = (this.watch.width - GuiPokeWatch.GUIW) / 2 + 16;
        final int offsetY = (this.watch.height - GuiPokeWatch.GUIH) / 2 + 37;
        final int height = this.font.lineHeight * 11; // 100

        if (this.list != null) this.children.remove(this.list);

        if (GuiPokeWatch.nightMode)
        {
            this.list = new ScrollGui<LineEntry>(this,
                this.minecraft, 228, height, this.font.lineHeight, offsetX, offsetY)
                    .setScrollBarColor(255, 150, 79)
                    .setScrollBarDarkBorder(211, 81, 29)
                    .setScrollBarGrayBorder(244, 123, 58)
                    .setScrollBarLightBorder(255, 190, 111)
                    .setScrollColor(244, 123, 58)
                    .setScrollDarkBorder(211, 81, 29)
                    .setScrollLightBorder(255, 190, 111);
        } else {
            this.list = new ScrollGui<LineEntry>(this,
                this.minecraft, 228, height, this.font.lineHeight, offsetX, offsetY)
                    .setScrollBarColor(83, 175, 255)
                    .setScrollBarDarkBorder(39, 75, 142)
                    .setScrollBarGrayBorder(69, 132, 249)
                    .setScrollBarLightBorder(255, 255, 255)
                    .setScrollColor(69, 132, 249)
                    .setScrollDarkBorder(39, 75, 142)
                    .setScrollLightBorder(255, 255, 255);
        }

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
            public void handleHovor(final PoseStack mat, final Style component, final int x, final int y)
            {}
        };
        final boolean item_book = !book.page_file;
        final String lang = this.minecraft.getLanguageManager().getSelected().getCode().toLowerCase(Locale.ROOT);
        if (item_book)
        {
            final ItemStack bookStack = books.get(this.index).getInfoStack(lang);
            if (!bookStack.hasTag()) return;
            final CompoundTag tag = bookStack.getTag();
            final ListTag bookPages = tag.getList("pages", 8);
            for (int i = 0; i < bookPages.size(); i++)
            {
                final MutableComponent page = Component.Serializer.fromJsonLenient(bookPages.getString(i));
                var list = this.font.split(page, 215);
                for (var line : list)
                {
                    final LineEntry wikiline = new WikiLine(this.list, -5, 0, this.font, line, i)
                            .setClickListner(listener);
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

                    final MutableComponent comp = TComponent.literal(line);
                    var list = this.font.getSplitter().splitLines(comp, 215, Style.EMPTY);
                    Style style = Style.EMPTY;

                    String fmt = "";

                    String _text = comp.getString();

                    for (var element : list)
                    {
                        MutableComponent entry;
                        if (element instanceof MutableComponent e)
                        {
                            entry = e;
                            style = entry.getStyle();
                        }
                        else entry = TComponent.literal(element.getString());
                        String text = entry.getString();

                        if (element instanceof Component c) style = c.getStyle();

                        // We have a link
                        if (text.contains(linkin))
                        {
                            text = text.replace(linkin, "");
                            entry = TComponent.literal(text);
                            style = style.withClickEvent(new ClickEvent(Action.CHANGE_PAGE, link_val));
                        }
                        // We have a ref
                        if (text.contains(refin))
                        {
                            text = text.replace(refin, "");
                            entry = TComponent.literal(text);
                            this.refs.put(ref_val, this.list.getSize());
                        }
                        if (text.contains("�"))
                        {
                            int index = _text.indexOf("�");
                            fmt = "";
                            while (index != -1)
                            {
                                fmt = fmt + _text.substring(index, index + 2);
                                _text = _text.substring(index + 2);
                                index = _text.indexOf("�");
                            }
                        }
                        else entry = TComponent.literal(fmt + text);
                        entry.setStyle(style);
                        final LineEntry wikiline = new WikiLine(this.list, -5, 0, this.font, entry.getVisualOrderText(),
                                pagenum).setClickListner(listener);
                        this.list.addEntry(wikiline);
                    }
                }
                final LineEntry wikiline = new WikiLine(this.list, 0, 0, this.font,
                        TComponent.literal("").getVisualOrderText(), pagenum);
                this.list.addEntry(wikiline);
                pagenum++;
            }

        }
        this.children.add(this.list);
    }
}
