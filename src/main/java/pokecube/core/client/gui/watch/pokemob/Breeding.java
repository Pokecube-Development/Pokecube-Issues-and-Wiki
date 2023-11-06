package pokecube.core.client.gui.watch.pokemob;

import java.util.Collections;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import pokecube.api.data.PokedexEntry;
import pokecube.core.client.EventsHandlerClient;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.client.gui.watch.PokemobInfoPage;
import pokecube.core.client.gui.watch.util.LineEntry;
import pokecube.core.client.gui.watch.util.LineEntry.IClickListener;
import pokecube.core.database.Database;
import pokecube.core.network.packets.PacketPokedex;
import thut.lib.TComponent;

public class Breeding extends ListPage<LineEntry>
{
    public static final ResourceLocation TEX_DM = GuiPokeWatch.makeWatchTexture("pokewatchgui_pokedex_breeding");
    public static final ResourceLocation TEX_NM = GuiPokeWatch.makeWatchTexture("pokewatchgui_pokedex_breeding_nm");

    long last = 0;
    final PokemobInfoPage parent;

    public Breeding(final PokemobInfoPage parent)
    {
        super(parent, "breeding", Breeding.TEX_DM, Breeding.TEX_NM);
        this.parent = parent;
    }

    @Override
    void drawInfo(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks)
    {
        // This is to give extra time for packet syncing.
        if (PacketPokedex.changed != this.last)
        {
            this.initList();
            this.last = PacketPokedex.changed;
            this.children.add(this.list);
        }
    }

    @Override
    public boolean handleComponentClicked(final Style component)
    {
        if (component != null)
        {
            final ClickEvent clickevent = component.getClickEvent();
            if (clickevent != null) if (clickevent.getAction() == Action.CHANGE_PAGE)
            {
                final PokedexEntry entry = Database.getEntry(clickevent.getValue());
                if (entry != null && entry != this.parent.pokemob.getPokedexEntry())
                    this.parent.initPages(EventsHandlerClient.getRenderMob(entry, this.watch.player.level()));
                return true;
            }
        }
        return super.handleComponentClicked(component);
    }

    @Override
    public void initList()
    {
        super.initList();
        int offsetX = (this.watch.width - GuiPokeWatch.GUIW) / 2 + 90;
        int offsetY = (this.watch.height - GuiPokeWatch.GUIH) / 2 + 30;
        final int height = this.font.lineHeight * 7;
        int width = 90; // 135

        final int colour = 0xFFFFFFFF;

        width = 90;
        final int dx = 65; //55
        final int dy = 15; //10
        offsetY += dy;
        offsetX += dx;

        final Breeding thisObj = this;
        final IClickListener listener = new IClickListener()
        {
            @Override
            public boolean handleClick(final Style component)
            {
                return thisObj.handleComponentClicked(component);
            }

            @Override
            public void handleHovor(final GuiGraphics graphics, final Style component, final int x, final int y)
            {
                //thisObj.renderComponentHoverEffect(mat, component, x, y);
            }
        };
        final PokedexEntry ourEntry = this.parent.pokemob.getPokedexEntry();
        this.list = new ScrollGui<>(this, this.minecraft, width, height - this.font.lineHeight / 2,
                this.font.lineHeight, offsetX, offsetY);
        MutableComponent main = TComponent.translatable(ourEntry.getUnlocalizedName());
        if (!PacketPokedex.noBreeding.contains(ourEntry)) for (final String name : PacketPokedex.relatedLists
                .getOrDefault(ourEntry.getTrimmedName(), Collections.emptyList()))
        {
            final PokedexEntry entry = Database.getEntry(name);
            if (entry == null) continue;
            main = TComponent.translatable(entry.getUnlocalizedName());
            main.setStyle(main.getStyle().withColor(TextColor.fromLegacyFormat(ChatFormatting.DARK_GRAY))
                    .withClickEvent(new ClickEvent(Action.CHANGE_PAGE, entry.getName())));
            this.list.addEntry(new LineEntry(this.list, 0, 0, this.font, main.getVisualOrderText(), colour)
                    .setClickListner(listener));
        }
    }
}