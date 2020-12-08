package pokecube.core.client.gui.watch.pokemob;

import java.util.Collections;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.ClickEvent.Action;
import pokecube.core.client.EventsHandlerClient;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.client.gui.watch.PokemobInfoPage;
import pokecube.core.client.gui.watch.util.LineEntry;
import pokecube.core.client.gui.watch.util.LineEntry.IClickListener;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.network.packets.PacketPokedex;

public class Breeding extends ListPage<LineEntry>
{
    int                   last = 0;
    final PokemobInfoPage parent;

    public Breeding(final PokemobInfoPage parent)
    {
        super(parent, "breeding");
        this.parent = parent;
    }

    public static final ResourceLocation           TEXTURE_BASE  = new ResourceLocation(PokecubeMod.ID,
    		"textures/gui/pokewatchgui_breeding.png");
    
    @Override
    public void renderBackground(final MatrixStack mat) 
    {
    	this.minecraft.textureManager.bindTexture(Breeding.TEXTURE_BASE);
    	int offsetX = (this.watch.width - GuiPokeWatch.GUIW) / 2;
        int offsetY = (this.watch.height - GuiPokeWatch.GUIH) / 2;
    	this.blit(mat, offsetX, offsetY, 0, 0, GuiPokeWatch.GUIW, GuiPokeWatch.GUIH);
    }
    
    @Override
    void drawInfo(final MatrixStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {	
        final PokedexEntry ourEntry = this.parent.pokemob.getPokedexEntry();
        final int num = PacketPokedex.relatedLists.getOrDefault(ourEntry.getTrimmedName(), Collections.emptyList())
                .size();
        // This is to give extra time for packet syncing.
        if (this.last != num)
        {
            this.initList();
            this.last = num;
            this.children.add(this.list);
        }
    }

    @Override
    public boolean handleComponentClicked(final Style component)
    {
        if (component != null)
        {
            final ClickEvent clickevent = component.getClickEvent();
            // TODO see if we need a sub style somehow?
            // if (clickevent == null) for (final ITextComponent sib :
            // component.getSiblings())
            // if (sib != null && (clickevent = sib.getStyle().getClickEvent())
            // != null) break;
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
        int offsetX = (this.watch.width - GuiPokeWatch.GUIW) / 2 + 90;
        int offsetY = (this.watch.height - GuiPokeWatch.GUIH) / 2 + 30;
        final int height = this.font.FONT_HEIGHT * 7;
        int width = 90; //135

        final int colour = 0xFFFFFFFF;

        width = 90;
        final int dx = 55;
        final int dy = 10;
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
            public void handleHovor(final MatrixStack mat, final Style component, final int x, final int y)
            {
                thisObj.renderComponentHoverEffect(mat, component, x, y);
            }
        };
        final PokedexEntry ourEntry = this.parent.pokemob.getPokedexEntry();
        this.list = new ScrollGui<>(this, this.minecraft, width, height - this.font.FONT_HEIGHT / 2,
                this.font.FONT_HEIGHT, offsetX, offsetY);
        IFormattableTextComponent main = new TranslationTextComponent(ourEntry.getUnlocalizedName());
        if (ourEntry.breeds) for (final String name : PacketPokedex.relatedLists.getOrDefault(ourEntry.getTrimmedName(),
                Collections.emptyList()))
        {
            final PokedexEntry entry = Database.getEntry(name);
            if (entry == null) continue;
            main = new TranslationTextComponent(entry.getUnlocalizedName());
            main.setStyle(main.getStyle().setColor(Color.fromTextFormatting(TextFormatting.GREEN)).setClickEvent(
                    new ClickEvent(Action.CHANGE_PAGE, entry.getName())));
            this.list.addEntry(new LineEntry(this.list, 0, 0, this.font, main, colour).setClickListner(listener));
        }
    }
}