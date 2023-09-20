package pokecube.core.client.gui.watch.pokemob;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import pokecube.api.data.PokedexEntry;
import pokecube.core.client.EventsHandlerClient;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.helper.TexButton;
import pokecube.core.client.gui.helper.TexButton.UVImgRender;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.client.gui.watch.PokemobInfoPage;
import pokecube.core.client.gui.watch.util.LineEntry;
import pokecube.core.client.gui.watch.util.LineEntry.IClickListener;
import pokecube.core.database.Database;
import pokecube.core.eventhandlers.StatsCollector;
import pokecube.core.network.packets.PacketPokedex;
import thut.lib.TComponent;

public class Description extends ListPage<LineEntry>
{
    public static final ResourceLocation TEX_DM = GuiPokeWatch.makeWatchTexture("pokewatchgui_desc");
    public static final ResourceLocation TEX_NM = GuiPokeWatch.makeWatchTexture("pokewatchgui_desc_nm");

    final PokemobInfoPage parent;

    public Description(final PokemobInfoPage parent)
    {
        super(parent, "description", Description.TEX_DM, Description.TEX_NM);
        this.parent = parent;
    }

    @Override
    public void init()
    {
        super.init();
        final PokedexEntry e = this.parent.pokemob.getPokedexEntry();
        if (PacketPokedex.haveConditions.contains(e))
        {
            final int x = this.watch.width / 2 + 10;
            final int y = this.watch.height / 2 + 22;
            final Component check_conditions = TComponent.translatable("pokewatch.capture.check");

            final TexButton button = this.addRenderableWidget(new TexButton.Builder(check_conditions, (b) -> {
                PacketPokedex.sendCaptureCheck(e);
            }).bounds(x, y, 100, 12).setTexture(GuiPokeWatch.getWidgetTex())
                    .setRender(new UVImgRender(0, 72, 100, 12)).build());
            button.setFGColor(0x444444);
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
        return false;
    }

    @Override
    public void initList()
    {
        super.initList();
        int offsetX = (this.watch.width - GuiPokeWatch.GUIW) / 2 + 90;
        int offsetY = (this.watch.height - GuiPokeWatch.GUIH) / 2 + 30;

        final int height = this.font.lineHeight * 8;
        final int dx = 49;
        final int dy = -1;
        offsetY += dy;
        offsetX += dx;

        final int textColour = 0x333333;

        final IClickListener listen = new IClickListener()
        {
            @Override
            public boolean handleClick(final Style component)
            {
                return Description.this.handleComponentClicked(component);
            }

            @Override
            public void handleHovor(final GuiGraphics graphics, final Style component, final int x, final int y)
            {
//                TODO: Fix
//                Description.this.renderComponentHoverEffect(graphics, component, x, y);
            }
        };
        MutableComponent page;

        final PokedexEntry pokedexEntry = this.parent.pokemob.getPokedexEntry();
        boolean fullColour = StatsCollector.getCaptured(pokedexEntry, Minecraft.getInstance().player) > 0
                || StatsCollector.getHatched(pokedexEntry, Minecraft.getInstance().player) > 0
                || this.minecraft.player.getAbilities().instabuild;

        // Megas Inherit colouring from the base form.
        if (!fullColour && pokedexEntry.isMega())
            fullColour = StatsCollector.getCaptured(pokedexEntry.getBaseForme(), Minecraft.getInstance().player) > 0
                    || StatsCollector.getHatched(pokedexEntry.getBaseForme(), Minecraft.getInstance().player) > 0;
        final List<FormattedCharSequence> list;
        if (fullColour)
        {
            page = TComponent.translatable("entity.pokecube." + pokedexEntry.getTrimmedName() + ".dexDesc");
            list = Lists.newArrayList(this.font.split(page, 100));
            list.add(TComponent.literal("").getVisualOrderText());
            page = pokedexEntry.getDescription(this.parent.pokemob.getCustomHolder());
            list.addAll(this.font.split(page, 100));
        }
        else
        {
            page = pokedexEntry.getDescription(this.parent.pokemob.getCustomHolder());
            list = this.font.split(page, 100);
        }

        this.list = new ScrollGui<>(this, this.minecraft, 107, height, this.font.lineHeight, offsetX, offsetY);
        for (var line : list)
            this.list.addEntry(new LineEntry(this.list, 0, 0, this.font, line, textColour).setClickListner(listen));

    }

    @Override
    public void onPageOpened()
    {
        super.onPageOpened();
    }
}