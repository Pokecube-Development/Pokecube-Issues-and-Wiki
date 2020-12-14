package pokecube.core.client.gui.watch;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.Button.ITooltip;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.client.gui.helper.TexButton;
import pokecube.core.client.gui.helper.TexButton.ImgRender;
import pokecube.core.client.gui.helper.TexButton.ShiftedTooltip;
import pokecube.core.client.gui.helper.TexButton.UVImgRender;
import pokecube.core.client.gui.watch.util.WatchPage;

public class StartPage extends WatchPage
{
    public static class UVHolder
    {
        static UVHolder DEFAULT = new UVHolder();

        public int uOffset = 0;
        public int vOffset = 0;
        public int vSize   = 0;

        public int buttonX = 0;
        public int buttonY = 0;

        public ITooltip hover = TexButton.NAMEONHOVER;

        public ImgRender render = new ImgRender()
        {
        };
    }

    private final Map<Class<?>, ResourceLocation> BUTTONTEX = Maps.newHashMap();

    private final Map<Class<?>, UVHolder> BUTTONLOC = Maps.newHashMap();

    public StartPage(final GuiPokeWatch watch)
    {
        super(new TranslationTextComponent("pokewatch.title.start"), watch, GuiPokeWatch.TEX_DM, GuiPokeWatch.TEX_NM);

        final UVHolder pokemobInfoLoc = new UVHolder();
        pokemobInfoLoc.render = new UVImgRender(0, 0, 24, 24);
        pokemobInfoLoc.hover = new ShiftedTooltip(12, -12);
        pokemobInfoLoc.buttonX = -50;
        pokemobInfoLoc.buttonY = 0;
        this.BUTTONLOC.put(PokemobInfoPage.class, pokemobInfoLoc);

        final UVHolder wikiLoc = new UVHolder();
        wikiLoc.render = new UVImgRender(96, 0, 24, 24);
        wikiLoc.hover = new ShiftedTooltip(12, -12);
        wikiLoc.buttonX = 5;
        wikiLoc.buttonY = 0;
        this.BUTTONLOC.put(WikiPage.class, wikiLoc);

        final UVHolder spawnsLoc = new UVHolder();
        spawnsLoc.render = new UVImgRender(48, 0, 24, 24);
        spawnsLoc.hover = new ShiftedTooltip(12, -12);
        spawnsLoc.buttonX = 60;
        spawnsLoc.buttonY = 0;
        this.BUTTONLOC.put(SpawnsPage.class, spawnsLoc);

        final UVHolder trainerLoc = new UVHolder();
        trainerLoc.render = new UVImgRender(72, 0, 24, 24);
        trainerLoc.hover = new ShiftedTooltip(12, -12);
        trainerLoc.buttonX = 110;
        trainerLoc.buttonY = 0;
        this.BUTTONLOC.put(ProgressPage.class, trainerLoc);

        final UVHolder teleportLoc = new UVHolder();
        teleportLoc.render = new UVImgRender(24, 0, 24, 24);
        teleportLoc.hover = new ShiftedTooltip(12, -12);
        teleportLoc.buttonX = -20;
        teleportLoc.buttonY = 55;
        this.BUTTONLOC.put(TeleportsPage.class, teleportLoc);

        final UVHolder meteorLoc = new UVHolder();
        meteorLoc.render = new UVImgRender(120, 0, 24, 24);
        meteorLoc.hover = new ShiftedTooltip(12, -12);
        meteorLoc.buttonX = 70;
        meteorLoc.buttonY = 55;
        this.BUTTONLOC.put(SecretBaseRadarPage.class, meteorLoc);
    }

    @Override
    public void onPageOpened()
    {
        this.children.clear();
        this.buttons.clear();
        super.onPageOpened();
        final int offsetX = (this.watch.width - GuiPokeWatch.GUIW) / 2 + 90;
        final int offsetY = (this.watch.height - GuiPokeWatch.GUIH) / 2 + 30;
        for (final Class<? extends WatchPage> page : GuiPokeWatch.PAGELIST)
            if (page != StartPage.class)
            {
                final int index = GuiPokeWatch.PAGELIST.indexOf(page);
                final WatchPage newPage = this.watch.createPage(index);
                final UVHolder loc = this.BUTTONLOC.getOrDefault(page, UVHolder.DEFAULT);
                final ResourceLocation tex = this.BUTTONTEX.getOrDefault(page, GuiPokeWatch.getWidgetTex());

                final Button buttons = new TexButton(offsetX + loc.buttonX, offsetY + loc.buttonY, 24, 24, newPage
                        .getTitle(), b -> this.watch.changePage(index), loc.hover).noName().setTex(tex).setRender(
                                loc.render);
                this.addButton(buttons);
            }

        final UVHolder loc = new UVHolder();
        loc.render = new UVImgRender(120, 0, 24, 24);
        loc.render = new UVImgRender(200, 0, 12, 12);
        loc.buttonX = -80;
        loc.buttonY = 100;
        final ResourceLocation tex = GuiPokeWatch.getWidgetTex();
        final Button buttons = new TexButton(offsetX + loc.buttonX, offsetY + loc.buttonY, 12, 12,
                new StringTextComponent(""), b ->
                {
                    GuiPokeWatch.nightMode = !GuiPokeWatch.nightMode;
                    this.watch.init();
                }, loc.hover).noName().setTex(tex).setRender(loc.render);
        this.addButton(buttons);
    }
}