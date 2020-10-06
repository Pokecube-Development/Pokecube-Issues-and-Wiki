package pokecube.core.client.gui.watch;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.RenderComponentsUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Difficulty;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.util.LineEntry;
import pokecube.core.client.gui.watch.util.LineEntry.IClickListener;
import pokecube.core.client.gui.watch.util.ListPage;
import pokecube.core.client.gui.watch.util.SpawnListEntry;
import pokecube.core.client.gui.watch.util.WatchPage;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.network.packets.PacketPokedex;

public class SpawnsPage extends ListPage<LineEntry>
{
    int     last  = 0;
    int     index = 1;
    boolean repel = false;

    public SpawnsPage(final GuiPokeWatch watch)
    {
        super(new TranslationTextComponent("pokewatch.title.spawns"), watch);
        for (final Class<? extends WatchPage> clazz : GuiPokeWatch.PAGELIST)
            if (clazz == PokemobInfoPage.class)
            {
                this.index = GuiPokeWatch.PAGELIST.indexOf(clazz);
                break;
            }
    }

    @Override
    public boolean handleComponentClicked(final ITextComponent component)
    {
        final PokedexEntry e = Database.getEntry(component.getUnformattedComponentText());
        if (e != null)
        {
            PacketPokedex.updateWatchEntry(e);
            this.watch.changePage(this.index);
            final PokemobInfoPage page = (PokemobInfoPage) this.watch.current_page;
            page.initPages(null);
            this.watch.current_page.onPageOpened();
        }
        return super.handleComponentClicked(component);
    }

    @Override
    public void initList()
    {
        super.initList();
        final int offsetX = (this.watch.width - 160) / 2 + 5;
        final int offsetY = (this.watch.height - 160) / 2 + 25;
        final int max = this.font.FONT_HEIGHT;
        final int height = max * 12;
        final QName local = new QName("Local_Rate");
        final List<PokedexEntry> names = Lists.newArrayList(PacketPokedex.selectedLoc.keySet());
        final Map<PokedexEntry, Float> rates = Maps.newHashMap();
        this.list = new ScrollGui<>(this, this.minecraft, 151, height, max, offsetX, offsetY);
        for (final PokedexEntry e : names)
        {
            final Float value = Float.parseFloat(PacketPokedex.selectedLoc.get(e).spawnRule.values.get(local));
            rates.put(e, value);
        }
        Collections.sort(names, (o1, o2) ->
        {
            final float rate1 = rates.get(o1);
            final float rate2 = rates.get(o2);
            return rate1 > rate2 ? -1 : rate1 < rate2 ? 1 : 0;
        });
        final IClickListener listener = new IClickListener()
        {
            @Override
            public boolean handleClick(final ITextComponent component)
            {
                return SpawnsPage.this.handleComponentClicked(component);
            }

            @Override
            public void handleHovor(final ITextComponent component, final int x, final int y)
            {
                // TODO possibly handle hovor text?
            }
        };

        if (this.repel = PacketPokedex.repelled)
        {
            final ITextComponent comp = new TranslationTextComponent("pokewatch.spawns.repelled");
            final List<ITextComponent> list = RenderComponentsUtil.splitText(comp, 120, this.font, true, true);

            for (final ITextComponent entry : list)
            {
                final LineEntry line = new LineEntry(this.list, 0, 0, this.font, entry, 0xFFFFFFFF);
                this.list.addEntry(line);
            }
        }

        if (Minecraft.getInstance().world.getDifficulty() == Difficulty.PEACEFUL)
        {
            final ITextComponent comp = new TranslationTextComponent("pokewatch.spawns.peaceful");
            final List<ITextComponent> list = RenderComponentsUtil.splitText(comp, 120, this.font, true, true);
            for (final ITextComponent entry : list)
            {
                final LineEntry line = new LineEntry(this.list, 0, 0, this.font, entry, 0xFFFFFFFF);
                this.list.addEntry(line);
            }
        }

        for (final PokedexEntry pokeEntry : names)
        {
            final SpawnListEntry entry = new SpawnListEntry(this, this.font, PacketPokedex.selectedLoc.get(pokeEntry),
                    pokeEntry, height, height, offsetY);
            final List<LineEntry> lines = entry.getLines(this.list, listener);
            int num = 4;
            final ITextComponent water0 = new TranslationTextComponent("pokewatch.spawns.water_only");
            final ITextComponent water1 = new TranslationTextComponent("pokewatch.spawns.water_optional");
            while (lines.size() > num)
            {
                final ITextComponent comp = lines.get(1).line;
                if (comp.getUnformattedComponentText().trim().equals(water0.getUnformattedComponentText().trim()))
                {
                    num++;
                    continue;
                }
                if (comp.getUnformattedComponentText().trim().equals(water1.getUnformattedComponentText().trim()))
                {
                    num++;
                    continue;
                }
                lines.remove(1);
            }
            for (final LineEntry line : lines)
                this.list.addEntry(line);
        }
        this.children.add(this.list);
    }

    @Override
    public void render(final MatrixStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {
        // This is to give extra time for packet syncing.
        if (this.last != PacketPokedex.selectedLoc.size() || this.repel != PacketPokedex.repelled)
        {
            this.initList();
            this.last = PacketPokedex.selectedLoc.size();
        }
        final int x = (this.watch.width - 160) / 2 + 80;
        final int y = (this.watch.height - 160) / 2 + 17;
        this.drawCenteredString(mat, this.font, I18n.format("pokewatch.spawns.info"), x, y, 0xFFFFFFFF);
        super.render(mat, mouseX, mouseY, partialTicks);
    }
}
