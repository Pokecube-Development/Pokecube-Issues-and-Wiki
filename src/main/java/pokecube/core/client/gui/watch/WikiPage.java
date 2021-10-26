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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.ClickEvent.Action;
import pokecube.core.client.gui.helper.ListHelper;
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
import pokecube.core.interfaces.PokecubeMod;

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

    public static final ResourceLocation TEX_DM = new ResourceLocation(PokecubeMod.ID,
            "textures/gui/pokewatchgui_wiki.png");
    public static final ResourceLocation TEX_NM = new ResourceLocation(PokecubeMod.ID,
            "textures/gui/pokewatchgui_wiki_nm.png");

    private int                        index = 0;
    private final Map<String, Integer> refs  = Maps.newHashMap();

    public WikiPage(final GuiPokeWatch watch)
    {
        super(new TranslationTextComponent("pokewatch.title.wiki"), watch, WikiPage.TEX_DM, WikiPage.TEX_NM);
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
        final TexButton nextBtn = this.addButton(new TexButton(x + 94, y - 70, 12, 12, next, b ->
        {
            this.index++;
            this.setList();
        }).setTex(GuiPokeWatch.getWidgetTex()).setRender(new UVImgRender(200, 0, 12, 12)));
        final TexButton prevBtn = this.addButton(new TexButton(x - 94, y - 70, 12, 12, prev, b ->
        {
            this.index--;
            this.setList();
        }).setTex(GuiPokeWatch.getWidgetTex()).setRender(new UVImgRender(200, 0, 12, 12)));
        this.setList();

        nextBtn.setFGColor(0x444444);
        prevBtn.setFGColor(0x444444);
    }

    @Override
    public void renderBackground(final MatrixStack matrixStack)
    {
        super.renderBackground(matrixStack);

        final int offsetX = (this.watch.width - GuiPokeWatch.GUIW) / 2;
        final int offsetY = (this.watch.height - GuiPokeWatch.GUIH) / 2;
        AbstractGui.fill(matrixStack, offsetX + 55, offsetY + 30, offsetX + 200, offsetY + 120, 0xFFFDF8EC);

    }

    private void setList()
    {
        this.refs.clear();
        final List<FreeTranslatedReward> books = Lists.newArrayList();
        for (final IInspectReward reward : PokedexInspector.rewards)
            if (reward instanceof FreeTranslatedReward) books.add((FreeTranslatedReward) reward);

        books.sort((o1, o2) -> o1.key.compareTo(o2.key));
        final int offsetX = (this.watch.width - GuiPokeWatch.GUIW) / 2 + 70;
        final int offsetY = (this.watch.height - GuiPokeWatch.GUIH) / 2 + 30;
        final int height = 85; // 100

        if (this.list != null) this.children.remove(this.list);

        this.list = new ScrollGui<>(this, this.minecraft, 135, height, this.font.lineHeight + 2, offsetX - 5, offsetY);
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
            public void handleHovor(final MatrixStack mat, final Style component, final int x, final int y)
            {
            }
        };
        final boolean item_book = !book.page_file;
        final String lang = this.minecraft.getLanguageManager().getSelected().getCode().toLowerCase(Locale.ROOT);
        if (item_book)
        {
            final ItemStack bookStack = books.get(this.index).getInfoStack(lang);
            if (!bookStack.hasTag()) return;
            final CompoundNBT tag = bookStack.getTag();
            final ListNBT bookPages = tag.getList("pages", 8);
            ITextComponent line;
            for (int i = 0; i < bookPages.size(); i++)
            {
                final IFormattableTextComponent page = ITextComponent.Serializer.fromJsonLenient(bookPages
                        .getString(i));
                final List<IFormattableTextComponent> list = ListHelper.splitText(page, 120, this.font, false);
                for (final IFormattableTextComponent element : list)
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
                IFormattableTextComponent entry;
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

                        final IFormattableTextComponent comp = new StringTextComponent(line);
                        final List<IFormattableTextComponent> list = ListHelper.splitText(comp, 120, this.font, false);
                        for (final IFormattableTextComponent element : list)
                        {
                            entry = element;
                            String text = entry.getString();
                            Style style = entry.getStyle();
                            // We have a link
                            if (text.contains(linkin))
                            {
                                text = text.replace(linkin, "");
                                entry = new StringTextComponent(text);
                                style = style.withClickEvent(new ClickEvent(Action.CHANGE_PAGE, link_val));
                            }
                            // We have a ref
                            if (text.contains(refin))
                            {
                                text = text.replace(refin, "");
                                entry = new StringTextComponent(text);
                                this.refs.put(ref_val, this.list.getSize());
                            }
                            entry.setStyle(style);
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
                e.printStackTrace();
            }

        }
        this.children.add(this.list);
    }
}
