package pokecube.core.client.gui.watch.pokemob;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

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

    abstract void drawInfo(PoseStack mat, int mouseX, int mouseY, float partialTicks);

    @Override
    public void init()
    {
        super.init();
        final int x = this.watch.width / 2;
        final int y = this.watch.height / 2;
        final Component next = TComponent.literal("");
        final Component prev = TComponent.literal("");

        final TexButton prevBtn = this.addRenderableWidget(new TexButton(x - 115, y - 27, 12, 20, prev, b -> {
            PokedexEntry entry = this.parent.pokemob.getPokedexEntry();
            final int i = Screen.hasShiftDown() ? Screen.hasControlDown() ? 100 : 10 : 1;
            entry = Pokedex.getInstance().getPrevious(entry, i);
            PacketPokedex.selectedMob.clear();
            this.parent.pokemob = EventsHandlerClient.getRenderMob(entry, this.watch.player.getLevel());
            this.parent.initPages(this.parent.pokemob);
        }).setTex(GuiPokeWatch.getWidgetTex()).setRender(new UVImgRender(48, 108, 12, 20)));

        final TexButton nextBtn = this.addRenderableWidget(new TexButton(x - 27, y - 27, 12, 20, next, b -> {
            PokedexEntry entry = this.parent.pokemob.getPokedexEntry();
            final int i = Screen.hasShiftDown() ? Screen.hasControlDown() ? 100 : 10 : 1;
            entry = Pokedex.getInstance().getNext(entry, i);
            PacketPokedex.selectedMob.clear();
            this.parent.pokemob = EventsHandlerClient.getRenderMob(entry, this.watch.player.getLevel());
            this.parent.initPages(this.parent.pokemob);
        }).setTex(GuiPokeWatch.getWidgetTex()).setRender(new UVImgRender(60, 108, 12, 20)));

        this.addRenderableWidget(new TexButton(x - 93, y + 40, 12, 12, TComponent.literal(""), b -> {
            this.watch.player.playSound(this.parent.pokemob.getSound(), 0.5f, 1.0F);
        }).setTex(GuiPokeWatch.getWidgetTex()).setRender(new UVImgRender(229, 72, 12, 12)));

        // Change Forms
        this.addRenderableWidget(new TexButton(x - 71, y + 40, 12, 12, TComponent.literal(""), b -> {
            if (this.parent.pokemob.getEntity().isAddedToWorld()) return;
            PokedexEntry entry = this.parent.pokemob.getPokedexEntry();
            PokedexEntry nextE = Pokedex.getInstance().getNextForm(entry);
            if (nextE == entry) nextE = Pokedex.getInstance().getFirstForm(entry);
            this.parent.pokemob = this.parent.pokemob.setPokedexEntry(nextE);
            this.parent.pokemob.setBasePokedexEntry(nextE);
            this.parent.initPages(this.parent.pokemob);
        }).setTex(GuiPokeWatch.getWidgetTex()).setRender(new UVImgRender(241, 72, 12, 12)));

        nextBtn.setFGColor(0x444444);
        prevBtn.setFGColor(0x444444);
    }

    @Override
    public void render(final PoseStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {
        super.render(mat, mouseX, mouseY, partialTicks);
        this.drawInfo(mat, mouseX, mouseY, partialTicks);
    }

}
