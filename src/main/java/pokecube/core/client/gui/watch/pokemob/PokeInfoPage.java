package pokecube.core.client.gui.watch.pokemob;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import pokecube.api.data.Pokedex;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.FormeHolder;
import pokecube.core.client.EventsHandlerClient;
import pokecube.core.client.gui.helper.TexButton;
import pokecube.core.client.gui.helper.TexButton.UVImgRender;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.client.gui.watch.PokemobInfoPage;
import pokecube.core.client.gui.watch.util.WatchPage;
import pokecube.core.network.packets.PacketPokedex;
import thut.lib.TComponent;

public abstract class PokeInfoPage extends WatchPage
{
    final PokemobInfoPage parent;
    static List<PokedexEntry> entries = Lists.newArrayList();
    static List<FormeHolder> formes = Lists.newArrayList();
    static int entryIndex = 0;
    static int formIndex = 0;
    public static TexButton formChanger;
    public static TexButton shiny;
    public static TexButton gender;

    public PokeInfoPage(final PokemobInfoPage parent, final String title, final ResourceLocation day,
            final ResourceLocation night)
    {
        super(TComponent.translatable("pokewatch.title.pokeinfo." + title), parent.watch, day, night);
        this.parent = parent;
    }

    @Override
    public void onPageOpened()
    {}

    @Override
    public void onPageClosed()
    {}

    abstract void drawInfo(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks);

    @Override
    public void init()
    {
        super.init();
        final int x = this.watch.width / 2;
        final int y = this.watch.height / 2;
        final Component next = TComponent.literal("");
        final Component prev = TComponent.literal("");

        final TexButton prevBtn = this.addRenderableWidget(new TexButton.Builder(next, b -> {
            PokedexEntry entry = this.parent.pokemob.getPokedexEntry();
            final int i = Screen.hasShiftDown() ? Screen.hasControlDown() ? 100 : 10 : 1;
            entry = Pokedex.getInstance().getPrevious(entry, i);
            PacketPokedex.selectedMob.clear();
            this.parent.pokemob = EventsHandlerClient.getRenderMob(entry, this.watch.player.level());
            this.parent.initPages(this.parent.pokemob);

            PokedexEntry nextEntry = Pokedex.getInstance().getNextForm(entry);
            PokedexEntry firstEntry = Pokedex.getInstance().getFirstForm(entry);
            PokedexEntry previousEntry = Pokedex.getInstance().getPreviousForm(entry);
            formChanger.active = (nextEntry != firstEntry && previousEntry != firstEntry) && !this.parent.pokemob.getEntity().isAddedToWorld();
            shiny.active = this.parent.pokemob.getPokedexEntry().hasShiny && !this.parent.pokemob.getEntity().isAddedToWorld();
        }).bounds(x - 115, y - 27, 12, 20).setTexture(GuiPokeWatch.getWidgetTex())
        		.setRender(new UVImgRender(212, 0, 12, 20)).build());
        
        final TexButton nextBtn = this.addRenderableWidget(new TexButton.Builder(prev, b -> {
            PokedexEntry entry = this.parent.pokemob.getPokedexEntry();
            final int i = Screen.hasShiftDown() ? Screen.hasControlDown() ? 100 : 10 : 1;
            entry = Pokedex.getInstance().getNext(entry, i);
            PacketPokedex.selectedMob.clear();
            this.parent.pokemob = EventsHandlerClient.getRenderMob(entry, this.watch.player.level());
            this.parent.initPages(this.parent.pokemob);

            PokedexEntry nextEntry = Pokedex.getInstance().getNextForm(entry);
            PokedexEntry firstEntry = Pokedex.getInstance().getFirstForm(entry);
            PokedexEntry previousEntry = Pokedex.getInstance().getPreviousForm(entry);
            formChanger.active = (nextEntry != firstEntry && previousEntry != firstEntry) && !this.parent.pokemob.getEntity().isAddedToWorld();
            shiny.active = this.parent.pokemob.getPokedexEntry().hasShiny && !this.parent.pokemob.getEntity().isAddedToWorld();
        }).bounds(x - 27, y - 27, 12, 20).setTexture(GuiPokeWatch.getWidgetTex())
        		.setRender(new UVImgRender(48, 108, 12, 20)).build());

        // Play Sound Button
        this.addRenderableWidget(new TexButton.Builder(TComponent.literal(""), b -> {
            this.watch.player.playSound(this.parent.pokemob.getSound(), 0.5f, 1.0F);
        }).bounds(x - 95, y + 40, 12, 12).setTexture(GuiPokeWatch.getWidgetTex())
                .setRender(new UVImgRender(229, 72, 12, 12)).build());

        // Change Forms Button
        formChanger = this.addRenderableWidget(new TexButton.Builder(TComponent.literal(""), b -> {
            if (this.parent.pokemob.getEntity().isAddedToWorld()) return;
            PokedexEntry entry = this.parent.pokemob.getPokedexEntry();
            PokedexEntry nextE = Pokedex.getInstance().getNextForm(entry);
            if (nextE == entry) nextE = Pokedex.getInstance().getFirstForm(entry);
            this.parent.pokemob = this.parent.pokemob.setPokedexEntry(nextE);
            this.parent.pokemob.setBasePokedexEntry(nextE);
            this.parent.initPages(this.parent.pokemob);
        }).bounds(x - 79, y + 40, 12, 12).setTexture(GuiPokeWatch.getWidgetTex())
        		.setRender(new UVImgRender(241, 72, 12, 12)).build());

        PokedexEntry entry = this.parent.pokemob.getPokedexEntry();
        PokedexEntry firstEntry = Pokedex.getInstance().getFirstForm(entry);
        PokedexEntry nextEntry = Pokedex.getInstance().getNextForm(firstEntry);
        PokedexEntry previousEntry = Pokedex.getInstance().getPreviousForm(firstEntry);
        formChanger.active = (firstEntry != nextEntry && previousEntry != firstEntry) && !this.parent.pokemob.getEntity().isAddedToWorld();

        // Shiny Button
        shiny = this.addRenderableWidget(new TexButton.Builder(TComponent.literal(""), b -> {
            if (this.parent.pokemob.getPokedexEntry().hasShiny && !this.parent.pokemob.getEntity().isAddedToWorld())
            {
                this.parent.pokemob.setShiny(!this.parent.pokemob.isShiny());
                this.parent.pokemob.onGenesChanged();
            }
        }).bounds(x - 63, y + 40, 12, 12).setTexture(GuiPokeWatch.getWidgetTex())
                .setRender(new UVImgRender(241, 36, 12, 12)).build());

        shiny.active = this.parent.pokemob.getPokedexEntry().hasShiny && !this.parent.pokemob.getEntity().isAddedToWorld();

        // Gender Button
        Component genderText = TComponent.literal("");
        if (this.parent.pokemob.getSexe() == IPokemob.MALE)
        {
            genderText = TComponent.literal("\u2642");
        } else if (this.parent.pokemob.getSexe() == IPokemob.FEMALE)
        {
            genderText = TComponent.literal("\u2640");
        }

        gender = this.addRenderableWidget(new TexButton.Builder(genderText, b -> {
            var old = this.parent.pokemob.getPokedexEntry();
            var e = old;
            switch (this.parent.pokemob.getSexe())
            {
                case IPokemob.MALE:
                    e = old.getForGender(IPokemob.FEMALE);
                    this.parent.pokemob.setSexe(IPokemob.FEMALE);
                    if (e != old)
                    {
                        this.parent.pokemob = this.parent.pokemob.setPokedexEntry(e);
                        this.parent.pokemob.setBasePokedexEntry(e);
                    }
                    this.parent.initPages(this.parent.pokemob);
                    break;
                case IPokemob.FEMALE:
                    e = old.getForGender(IPokemob.MALE);
                    this.parent.pokemob.setSexe(IPokemob.MALE);
                    if (e != old)
                    {
                        this.parent.pokemob = this.parent.pokemob.setPokedexEntry(e);
                        this.parent.pokemob.setBasePokedexEntry(e);
                    }
                    this.parent.initPages(this.parent.pokemob);
                    break;
            }
            this.parent.pokemob.onGenesChanged();
        }).bounds(x - 47, y + 40, 12, 12).setTexture(GuiPokeWatch.getWidgetTex())
                .setRender(new UVImgRender(200, 0, 12, 12)).shadow(true).build());

        gender.active = !this.parent.pokemob.getEntity().isAddedToWorld() &&
                (this.parent.pokemob.getSexe() == IPokemob.MALE || this.parent.pokemob.getSexe() == IPokemob.FEMALE);
        if (this.parent.pokemob.getSexe() == IPokemob.MALE) gender.setFGColor(ChatFormatting.DARK_BLUE.getColor());
        else if (this.parent.pokemob.getSexe() == IPokemob.FEMALE) gender.setFGColor(ChatFormatting.DARK_RED.getColor());

        nextBtn.setFGColor(0x444444);
        prevBtn.setFGColor(0x444444);
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks)
    {
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.drawInfo(graphics, mouseX, mouseY, partialTicks);
    }

}
