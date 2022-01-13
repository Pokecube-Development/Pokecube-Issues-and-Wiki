package pokecube.core.client.gui.watch.pokemob;

import java.util.Collections;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
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
    public static final ResourceLocation TEX_DM = new ResourceLocation(PokecubeMod.ID,
            "textures/gui/pokewatchgui_breeding.png");
    public static final ResourceLocation TEX_NM = new ResourceLocation(PokecubeMod.ID,
            "textures/gui/pokewatchgui_breeding_nm.png");

    int                   last = 0;
    final PokemobInfoPage parent;

    public Breeding(final PokemobInfoPage parent)
    {
        super(parent, "breeding", Breeding.TEX_DM, Breeding.TEX_NM);
        this.parent = parent;
    }

    @Override
    void drawInfo(final PoseStack mat, final int mouseX, final int mouseY, final float partialTicks)
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
            if (clickevent != null) if (clickevent.getAction() == Action.CHANGE_PAGE)
            {
                final PokedexEntry entry = Database.getEntry(clickevent.getValue());
                if (entry != null && entry != this.parent.pokemob.getPokedexEntry()) this.parent.initPages(
                        EventsHandlerClient.getRenderMob(entry, this.watch.player.getLevel()));
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
            public void handleHovor(final PoseStack mat, final Style component, final int x, final int y)
            {
                thisObj.renderComponentHoverEffect(mat, component, x, y);
            }
        };
        final PokedexEntry ourEntry = this.parent.pokemob.getPokedexEntry();
        this.list = new ScrollGui<>(this, this.minecraft, width, height - this.font.lineHeight / 2,
                this.font.lineHeight, offsetX, offsetY);
        MutableComponent main = new TranslatableComponent(ourEntry.getUnlocalizedName());
        if (!PacketPokedex.noBreeding.contains(ourEntry)) for (final String name : PacketPokedex.relatedLists
                .getOrDefault(ourEntry.getTrimmedName(), Collections.emptyList()))
        {
            final PokedexEntry entry = Database.getEntry(name);
            if (entry == null) continue;
            main = new TranslatableComponent(entry.getUnlocalizedName());
            main.setStyle(main.getStyle().withColor(TextColor.fromLegacyFormat(ChatFormatting.GREEN)).withClickEvent(
                    new ClickEvent(Action.CHANGE_PAGE, entry.getName())));
            this.list.addEntry(new LineEntry(this.list, 0, 0, this.font, main, colour).setClickListner(listener));
        }
    }
}