package pokecube.core.client.gui.watch.pokemob;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
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
    public static final ResourceLocation TEX_DM = GuiPokeWatch.makeWatchTexture("pokewatchgui_pokedex_desc");
    public static final ResourceLocation TEX_NM = GuiPokeWatch.makeWatchTexture("pokewatchgui_pokedex_desc_nm");

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
            final TexButton button = this.addRenderableWidget(new TexButton(x + 3, y + 21, 100, 12, check_conditions, b -> {
                PacketPokedex.sendCaptureCheck(e);
            }).setTex(GuiPokeWatch.getWidgetTex()).setRender(new UVImgRender(0, 72, 100, 12)));
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
                    this.parent.initPages(EventsHandlerClient.getRenderMob(entry, this.watch.player.getLevel()));
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
        int offsetY = (this.watch.height - GuiPokeWatch.GUIH) / 2 + 26;

        final int height = this.font.lineHeight * 10; // 8
        final int dx = 41;
        final int dy = 8;
        offsetX += dx;
        offsetY += dy;

        final int textColour = 0x333333;

        final IClickListener listen = new IClickListener()
        {
            @Override
            public boolean handleClick(final Style component)
            {
                return Description.this.handleComponentClicked(component);
            }

            @Override
            public void handleHovor(final PoseStack mat, final Style component, final int x, final int y)
            {
                Description.this.renderComponentHoverEffect(mat, component, x, y);
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
            String key = "entity.pokecube." + pokedexEntry.getTrimmedName() + ".dexDesc";
            page = TComponent.translatable(key);
            // No description
            if (page.getString().equals(key))
            {
                // Check if we have a base form
                if (pokedexEntry.generated)
                {
                    key = "entity.pokecube." + pokedexEntry.getBaseForme().getTrimmedName() + ".dexDesc";
                    page = TComponent.translatable(key);
                }
                else page = TComponent.literal("");
            }
            list = Lists.newArrayList(this.font.split(page, 108));
            if (page.getString().isBlank()) list.clear();
            list.add(TComponent.literal("").getVisualOrderText());
            page = pokedexEntry.getDescription(this.parent.pokemob, this.parent.pokemob.getCustomHolder());
            list.addAll(this.font.split(page, 108));
        }
        else
        {
            page = pokedexEntry.getDescription(this.parent.pokemob, this.parent.pokemob.getCustomHolder());
            list = this.font.split(page, 112);
        }

        final PokedexEntry e = this.parent.pokemob.getPokedexEntry();
        if (PacketPokedex.haveConditions.contains(e))
        {
            if (GuiPokeWatch.nightMode)
            {
                this.list = new ScrollGui<LineEntry>(this, this.minecraft, 120, height, this.font.lineHeight, offsetX, offsetY)
                    .setScrollBarColor(255, 150, 79)
                    .setScrollBarDarkBorder(211, 81, 29)
                    .setScrollBarGrayBorder(244, 123, 58)
                    .setScrollBarLightBorder(255, 190, 111)
                    .setScrollColor(244, 123, 58)
                    .setScrollDarkBorder(211, 81, 29)
                    .setScrollLightBorder(255, 190, 111);
            } else this.list = new ScrollGui<LineEntry>(this, this.minecraft, 120, height, this.font.lineHeight, offsetX, offsetY)
                    .setScrollBarColor(83, 175, 255)
                    .setScrollBarDarkBorder(39, 75, 142)
                    .setScrollBarGrayBorder(69, 132, 249)
                    .setScrollBarLightBorder(255, 255, 255)
                    .setScrollColor(69, 132, 249)
                    .setScrollDarkBorder(39, 75, 142)
                    .setScrollLightBorder(255, 255, 255);

            for (var line : list)
                this.list.addEntry(new LineEntry(this.list, 0, 0, this.font, line, textColour).setClickListner(listen));
        } else {
            if (GuiPokeWatch.nightMode)
            {
                this.list = new ScrollGui<LineEntry>(this,
                    this.minecraft, 120, this.font.lineHeight * 12, this.font.lineHeight, offsetX, offsetY)
                    .setScrollBarColor(255, 150, 79)
                    .setScrollBarDarkBorder(211, 81, 29)
                    .setScrollBarGrayBorder(244, 123, 58)
                    .setScrollBarLightBorder(255, 190, 111)
                    .setScrollColor(244, 123, 58)
                    .setScrollDarkBorder(211, 81, 29)
                    .setScrollLightBorder(255, 190, 111);
            } else this.list = new ScrollGui<LineEntry>(this,
                    this.minecraft, 120, this.font.lineHeight * 12, this.font.lineHeight, offsetX, offsetY)
                    .setScrollBarColor(83, 175, 255)
                    .setScrollBarDarkBorder(39, 75, 142)
                    .setScrollBarGrayBorder(69, 132, 249)
                    .setScrollBarLightBorder(255, 255, 255)
                    .setScrollColor(69, 132, 249)
                    .setScrollDarkBorder(39, 75, 142)
                    .setScrollLightBorder(255, 255, 255);

            for (var line : list)
                this.list.addEntry(new LineEntry(this.list, 0, 0, this.font, line, textColour).setClickListner(listen));
        }

    }

    @Override
    public void onPageOpened()
    {
        super.onPageOpened();
    }
}