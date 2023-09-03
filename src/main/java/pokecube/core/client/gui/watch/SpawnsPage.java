package pokecube.core.client.gui.watch;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.util.LineEntry;
import pokecube.core.client.gui.watch.util.LineEntry.IClickListener;
import pokecube.core.client.gui.watch.util.ListPage;
import pokecube.core.client.gui.watch.util.SpawnListEntry;
import pokecube.core.client.gui.watch.util.WatchPage;
import pokecube.core.database.Database;
import pokecube.core.network.packets.PacketPokedex;
import thut.lib.TComponent;

public class SpawnsPage extends ListPage<LineEntry>
{
    int last = 0;
    int index = 1;
    boolean repel = false;

    public static final ResourceLocation TEX_DM = GuiPokeWatch.makeWatchTexture("pokewatchgui_location");
    public static final ResourceLocation TEX_NM = GuiPokeWatch.makeWatchTexture("pokewatchgui_location_nm");

    public SpawnsPage(final GuiPokeWatch watch)
    {
        super(TComponent.translatable("pokewatch.title.spawns"), watch, SpawnsPage.TEX_DM, SpawnsPage.TEX_NM);
        for (final Class<? extends WatchPage> clazz : GuiPokeWatch.PAGELIST) if (clazz == PokemobInfoPage.class)
        {
            this.index = GuiPokeWatch.PAGELIST.indexOf(clazz);
            break;
        }
    }

    @Override
    public boolean handleComponentClicked(final Style component)
    {
        ClickEvent clickevent;
        if (component == null || (clickevent = component.getClickEvent()) == null) return false;
        final PokedexEntry e = Database.getEntry(clickevent.getValue());
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
        int offsetX = (this.watch.width - GuiPokeWatch.GUIW) / 2;
        int offsetY = (this.watch.height - GuiPokeWatch.GUIH) / 2;
        final int max = this.font.lineHeight;
        final int height = max * 6;

        final int dx = 55;
        final int dy = 40;
        offsetX += dx;
        offsetY += dy;

        final String local = "Local_Rate";
        final List<PokedexEntry> names = Lists.newArrayList(PacketPokedex.selectedLoc.keySet());
        final Map<PokedexEntry, Float> rates = Maps.newHashMap();
        this.list = new ScrollGui<>(this, this.minecraft, 151, height, max, offsetX, offsetY);
        for (final PokedexEntry e : names)
        {
            try
            {
                final Float value = Float.parseFloat(PacketPokedex.selectedLoc.get(e).spawnRule.values.get(local));
                rates.put(e, value);
            }
            catch (Exception e1)
            {
                rates.put(e, 0f);
                PokecubeAPI.LOGGER.error("Error with rate sent for " + e);
            }
        }
        Collections.sort(names, (o1, o2) -> {
            final float rate1 = rates.get(o1);
            final float rate2 = rates.get(o2);
            return rate1 > rate2 ? -1 : rate1 < rate2 ? 1 : o1.getTrimmedName().compareTo(o2.getTrimmedName());
        });
        final IClickListener listener = new IClickListener()
        {
            @Override
            public boolean handleClick(final Style component)
            {
                return SpawnsPage.this.handleComponentClicked(component);
            }

            @Override
            public void handleHovor(final GuiGraphics graphics, final Style component, final int x, final int y)
            {
                // TODO possibly handle hovor text?
            }
        };

        for (final PokedexEntry pokeEntry : names)
        {
            final SpawnListEntry entry = new SpawnListEntry(this, this.font, PacketPokedex.selectedLoc.get(pokeEntry),
                    pokeEntry, 120, height, offsetY);
            final List<LineEntry> lines = entry.getLines(this.list, listener);
            final Component water0 = TComponent.translatable("pokewatch.spawns.water_only");
            final Component water1 = TComponent.translatable("pokewatch.spawns.water_optional");
            // This is the name
            final LineEntry first = lines.get(0);
            // This is the blank line
            final LineEntry last0 = lines.get(lines.size() - 1);
            // This is the adjusted spawn rate
            final LineEntry last1 = lines.get(lines.size() - 2);
            // Remove all, unless they specifiy water vs ground
            lines.removeIf(e -> {
                // FIXME fixme
                final String string = e.line.toString();
                final boolean isWater0 = water0.getString().equals(string);
                final boolean isWater1 = water1.getString().equals(string);
                return !(isWater0 || isWater1);
            });
            // Re-add name
            lines.add(0, first);
            // Re-add spawn rate
            lines.add(last1);
            // Re-add blank line
            lines.add(last0);
            for (final LineEntry line : lines) this.list.addEntry(line);
        }
        this.children.add(this.list);
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks)
    {
        // This is to give extra time for packet syncing.
        if (this.last != PacketPokedex.selectedLoc.size() || this.repel != PacketPokedex.repelled)
        {
            this.initList();
            this.last = PacketPokedex.selectedLoc.size();
        }
        final int x = (this.watch.width - GuiPokeWatch.GUIW) / 2;
        final int y = (this.watch.height - GuiPokeWatch.GUIH) / 2;
        final int colour = 0xFF78C850;
        graphics.drawCenteredString(this.font, I18n.get("pokewatch.spawns.info"), x + 130, y + 30, colour);

        if (Minecraft.getInstance().level.getDifficulty() == Difficulty.PEACEFUL)
        {
            final MutableComponent comp = TComponent.translatable("pokewatch.spawns.peaceful");
            var list = this.font.split(comp, 120);
            int n = 0;
            for (var entry : list)
                graphics.drawCenteredString(this.font, entry, x + 130, y + 100 + 10 * n++, 0);
        }
        else if (this.repel = PacketPokedex.repelled)
        {
            final MutableComponent comp = TComponent.translatable("pokewatch.spawns.repelled");
            var list = this.font.split(comp, 120);
            int n = 0;
            for (var entry : list)
                graphics.drawCenteredString(this.font, entry, x + 130, y + 100 + 10 * n++, 0);
        }

        super.render(graphics, mouseX, mouseY, partialTicks);
    }
}
