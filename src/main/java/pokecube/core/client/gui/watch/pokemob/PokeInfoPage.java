package pokecube.core.client.gui.watch.pokemob;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import pokecube.api.data.Pokedex;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob.FormeHolder;
import pokecube.core.client.EventsHandlerClient;
import pokecube.core.client.gui.helper.TexButton;
import pokecube.core.client.gui.helper.TexButton.UVImgRender;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.client.gui.watch.PokemobInfoPage;
import pokecube.core.client.gui.watch.util.WatchPage;
import pokecube.core.database.Database;
import pokecube.core.network.packets.PacketPokedex;
import thut.lib.TComponent;

public abstract class PokeInfoPage extends WatchPage
{
    final PokemobInfoPage parent;
    static List<PokedexEntry> entries = Lists.newArrayList();
    static List<FormeHolder> formes = Lists.newArrayList();
    static int entryIndex = 0;
    static int formIndex = 0;

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
        final int y = this.watch.height / 2 - 3;
        final Component next = TComponent.literal(">");
        final Component prev = TComponent.literal("<");
        final TexButton nextBtn = this.addRenderableWidget(new TexButton.Builder(next, b -> {
            PokedexEntry entry = this.parent.pokemob.getPokedexEntry();
            final int i = Screen.hasShiftDown() ? Screen.hasControlDown() ? 100 : 10 : 1;
            entry = Pokedex.getInstance().getNext(entry, i);
            PacketPokedex.selectedMob.clear();
            this.parent.pokemob = EventsHandlerClient.getRenderMob(entry, this.watch.player.level());
            this.parent.initPages(this.parent.pokemob);
        }).bounds(x - 28, y - 30, 12, 20).setTexture(GuiPokeWatch.getWidgetTex())
        		.setRender(new UVImgRender(212, 0, 12, 20)).build());
        
        final TexButton prevBtn = this.addRenderableWidget(new TexButton.Builder(prev, b -> {
            PokedexEntry entry = this.parent.pokemob.getPokedexEntry();
            final int i = Screen.hasShiftDown() ? Screen.hasControlDown() ? 100 : 10 : 1;
            entry = Pokedex.getInstance().getPrevious(entry, i);
            PacketPokedex.selectedMob.clear();
            this.parent.pokemob = EventsHandlerClient.getRenderMob(entry, this.watch.player.level());
            this.parent.initPages(this.parent.pokemob);
        }).bounds(x - 115, y - 30, 12, 20).setTexture(GuiPokeWatch.getWidgetTex())
        		.setRender(new UVImgRender(212, 0, 12, 20)).build());
        
        //Change Forms
        this.addRenderableWidget(new TexButton.Builder(TComponent.literal(""), b -> { 
        	 // Cycle Form only if not a real mob
            if (this.parent.pokemob.getEntity().isAddedToWorld()) return;
            PokedexEntry entry = this.parent.pokemob.getPokedexEntry();
            FormeHolder holder = null;
            PokeInfoPage.formes = Database.customModels.getOrDefault(entry, Collections.emptyList());
            PokeInfoPage.entries = Lists.newArrayList(Database.getFormes(entry));

            if (entry.getBaseForme() != null && !PokeInfoPage.entries.contains(entry.getBaseForme()))
            {
                PokeInfoPage.entries.add(entry.getBaseForme());
                Collections.sort(PokeInfoPage.entries, Database.COMPARATOR);
            }
            PokeInfoPage.entryIndex = PokeInfoPage.entryIndex % PokeInfoPage.entries.size();
            if (!PokeInfoPage.formes.isEmpty() && PokeInfoPage.formIndex++ < PokeInfoPage.formes.size() - 1)
                holder = PokeInfoPage.formes.get(PokeInfoPage.formIndex);
            else if (PokeInfoPage.entries.size() > 0)
            {
                PokeInfoPage.formIndex = -1;
                entry = PokeInfoPage.entries.get(PokeInfoPage.entryIndex++ % PokeInfoPage.entries.size());
                holder = entry.getModel(this.parent.pokemob.getSexe());
                this.parent.initPages(this.parent.pokemob.changeForm(entry));
            }
            // This initializes the model holder
            this.parent.pokemob.setCustomHolder(holder);
            // This ensures the textures/etc are reset to account for the new
            // model holder.
            this.parent.pokemob.onGenesChanged();
        }).bounds(x - 71, y + 43, 12, 12).setTexture(GuiPokeWatch.getWidgetTex())
        		.setRender(new UVImgRender(241, 72, 12, 12)).build());
        
        this.addRenderableWidget(new TexButton.Builder(TComponent.literal(""), b -> {
            this.watch.player.playSound(this.parent.pokemob.getSound(), 0.5f, 1.0F);
        }).bounds(x - 93, y + 43, 12, 12).setTexture(GuiPokeWatch.getWidgetTex())
        		.setRender(new UVImgRender(229, 72, 12, 12)).build());
        
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
