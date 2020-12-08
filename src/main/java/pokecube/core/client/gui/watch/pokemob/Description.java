package pokecube.core.client.gui.watch.pokemob;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.ClickEvent.Action;
import pokecube.core.client.EventsHandlerClient;
import pokecube.core.client.gui.helper.ListHelper;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.client.gui.watch.PokemobInfoPage;
import pokecube.core.client.gui.watch.util.LineEntry;
import pokecube.core.client.gui.watch.util.LineEntry.IClickListener;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.PokecubeMod;

public class Description extends ListPage<LineEntry>
{
    final PokemobInfoPage parent;

    public Description(final PokemobInfoPage parent)
    {
        super(parent, "description");
        this.parent = parent;
    }

    public static final ResourceLocation           TEXTURE_BASE  = new ResourceLocation(PokecubeMod.ID,
    		"textures/gui/pokewatchgui_desc.png");
    
    @Override
    public void renderBackground(final MatrixStack mat)
    {
    	this.minecraft.textureManager.bindTexture(Description.TEXTURE_BASE);
    	int offsetX = (this.watch.width - GuiPokeWatch.GUIW) / 2;
        int offsetY = (this.watch.height - GuiPokeWatch.GUIH) / 2;
    	this.blit(mat, offsetX, offsetY, 0, 0, GuiPokeWatch.GUIW, GuiPokeWatch.GUIH);
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
        int offsetX = (this.watch.width - GuiPokeWatch.GUIW) / 2 + 90;
        int offsetY = (this.watch.height - GuiPokeWatch.GUIH) / 2 + 30;
        
        final int height = this.font.FONT_HEIGHT * 8;
        final int dx = 49;
        final int dy = -1;
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
        IFormattableTextComponent line;
        final IFormattableTextComponent page = (IFormattableTextComponent) this.parent.pokemob.getPokedexEntry().getDescription();
        this.list = new ScrollGui<>(this, this.minecraft, 107, height, this.font.FONT_HEIGHT, offsetX, offsetY);
        final List<IFormattableTextComponent> list = ListHelper.splitText(page, 100, this.font, false);
        for (final ITextComponent element : list)
        {
            line = (IFormattableTextComponent) element;
            this.list.addEntry(new LineEntry(this.list, 0, 0, this.font, line, 0xFFFFFF));
        }
    }

    @Override
    public void onPageOpened()
    {
        super.onPageOpened();
    }
}