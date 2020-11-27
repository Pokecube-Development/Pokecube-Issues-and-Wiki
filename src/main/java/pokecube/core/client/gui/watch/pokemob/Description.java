package pokecube.core.client.gui.watch.pokemob;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.RenderComponentsUtil;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.ClickEvent.Action;
import pokecube.core.client.EventsHandlerClient;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.PokemobInfoPage;
import pokecube.core.client.gui.watch.util.LineEntry;
import pokecube.core.client.gui.watch.util.LineEntry.IClickListener;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Description extends ListPage<LineEntry>
{
    final PokemobInfoPage parent;

    public Description(final PokemobInfoPage parent)
    {
        super(parent, "description");
        this.parent = parent;
    }

    @Override
    public boolean handleComponentClicked(final Style component)
    {
        if (component != null)
        {
            final ClickEvent clickevent = component.getClickEvent();
            //TODO somehow check sub styles?
//            if (clickevent == null) for (final ITextComponent sib : component.getSiblings())
//                if (sib != null && (clickevent = sib.getStyle().getClickEvent()) != null) break;
            if (clickevent != null) if (clickevent.getAction() == Action.CHANGE_PAGE)
            {
                final PokedexEntry entry = Database.getEntry(clickevent.getValue());
                if (entry != null && entry != this.parent.pokemob.getPokedexEntry()) this.parent.initPages(
                        EventsHandlerClient.getRenderMob(entry, this.watch.player.getEntityWorld()));
                return true;
            }
        }
        return false;
    }

    @Override
    public void initList()
    {
        super.initList();
        int offsetX = (this.watch.width - 160) / 2 + 20;
        int offsetY = (this.watch.height - 160) / 2 + 85;
        final int height = this.font.FONT_HEIGHT * 12;
        final int dx = 25;
        final int dy = -57;
        offsetY += dy;
        offsetX += dx;

        new IClickListener()
        {
            @Override
            public boolean handleClick(final Style component)
            {
                return Description.this.handleComponentClicked(component);
            }

            @Override
            public void handleHovor(final MatrixStack mat, final Style component, final int x, final int y)
            {
                Description.this.renderComponentHoverEffect(mat, component, x, y);
            }
        };
        ITextComponent line;
        ITextComponent page = this.parent.pokemob.getPokedexEntry().getDescription();
        this.list = new ScrollGui<>(this, this.minecraft, 107, height, this.font.FONT_HEIGHT, offsetX, offsetY);
        page = new StringTextComponent(page.getString());
//        final List<ITextComponent> list = RenderComponentsUtil.splitText(page, 100, this.font, false, false);
        final List<IReorderingProcessor> list = RenderComponentsUtil.func_238505_a_(page, 100, this.font);
        for (final IReorderingProcessor element : list)
        {
            line = new StringTextComponent(element.toString());
            this.list.addEntry(new LineEntry(this.list, 0, 0, this.font, line, 0xFFFFFF));
        }
    }

    @Override
    public void onPageOpened()
    {
        super.onPageOpened();
    }
}