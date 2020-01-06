package pokecube.core.client.gui.watch.pokemob;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.ClickEvent.Action;
import pokecube.core.client.EventsHandlerClient;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.PokemobInfoPage;
import pokecube.core.client.gui.watch.util.LineEntry;
import pokecube.core.client.gui.watch.util.LineEntry.IClickListener;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Breeding extends ListPage<LineEntry>
{
    final PokemobInfoPage parent;

    public Breeding(final PokemobInfoPage parent)
    {
        super(parent, "breeding");
        this.parent = parent;
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
                final PokedexEntry entry = Database.getEntry(clickevent.getValue());
                if (entry != null && entry != this.parent.pokemob.getPokedexEntry()) this.parent.initPages(
                        EventsHandlerClient.getRenderMob(entry, this.watch.player.getEntityWorld()));
                return true;
            }
        }
        return super.handleComponentClicked(component);
    }

    @Override
    public void initList()
    {
        super.initList();
        int offsetX = (this.watch.width - 160) / 2 + 20;
        int offsetY = (this.watch.height - 160) / 2 + 85;
        final int height = this.font.FONT_HEIGHT * 12;
        int width = 135;

        int y0 = offsetY;
        int y1 = offsetY + height;
        final int colour = 0xFFFFFFFF;

        width = 111;
        final int dx = 25;
        final int dy = -57;
        y0 += dy;
        y1 += dy;
        offsetY += dy;
        offsetX += dx;

        final Breeding thisObj = this;
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
                thisObj.renderComponentHoverEffect(component, x, y);
            }
        };
        this.list = new ScrollGui<>(this, this.minecraft, width, height - this.font.FONT_HEIGHT / 2,
                this.font.FONT_HEIGHT, offsetX, offsetY);
        ITextComponent main = new TranslationTextComponent(this.parent.pokemob.getPokedexEntry().getUnlocalizedName());
        main.setStyle(new Style());
        main.getStyle().setColor(TextFormatting.GREEN);
        main.getStyle().setClickEvent(new ClickEvent(Action.CHANGE_PAGE, this.parent.pokemob.getPokedexEntry()
                .getName()));
        if (this.parent.pokemob.getPokedexEntry().breeds)
        {
            this.list.addEntry(new LineEntry(this.list, y0, y1, this.font, main, colour).setClickListner(listener));
            for (final PokedexEntry entry : this.parent.pokemob.getPokedexEntry().getRelated())
            {
                main = new TranslationTextComponent(entry.getUnlocalizedName());
                main.setStyle(new Style());
                main.getStyle().setColor(TextFormatting.GREEN);
                main.getStyle().setClickEvent(new ClickEvent(Action.CHANGE_PAGE, entry.getName()));
                this.list.addEntry(new LineEntry(this.list, y0, y1, this.font, main, colour).setClickListner(listener));
            }
        }
    }
}