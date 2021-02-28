package pokecube.core.client.gui.watch.pokemob;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.client.EventsHandlerClient;
import pokecube.core.client.gui.helper.TexButton;
import pokecube.core.client.gui.helper.TexButton.UVImgRender;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.client.gui.watch.PokemobInfoPage;
import pokecube.core.client.gui.watch.util.WatchPage;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob.FormeHolder;
import pokecube.core.network.packets.PacketPokedex;

public abstract class PokeInfoPage extends WatchPage
{
    final PokemobInfoPage     parent;
    static List<PokedexEntry> entries    = Lists.newArrayList();
    static List<FormeHolder>  formes     = Lists.newArrayList();
    static int                entryIndex = 0;
    static int                formIndex  = 0;

    public PokeInfoPage(final PokemobInfoPage parent, final String title, final ResourceLocation day,
            final ResourceLocation night)
    {
        super(new TranslationTextComponent("pokewatch.title.pokeinfo." + title), parent.watch, day, night);
        this.parent = parent;
    }

    @Override
    public void onPageOpened()
    {
    }

    @Override
    public void onPageClosed()
    {
    }

    abstract void drawInfo(MatrixStack mat, int mouseX, int mouseY, float partialTicks);

    @Override
    public void init()
    {
        super.init();
        final int x = this.watch.width / 2;
        final int y = this.watch.height / 2 - 3;
        final ITextComponent next = new StringTextComponent(">");
        final ITextComponent prev = new StringTextComponent("<");
        final ITextComponent form = new StringTextComponent("\u2500");
        final ITextComponent cry = new StringTextComponent("\u266B");
        final TexButton nextBtn = this.addButton(new TexButton(x - 66, y + 35, 12, 20, next, b ->
        {
            PokedexEntry entry = this.parent.pokemob.getPokedexEntry();
            final int i = Screen.hasShiftDown() ? Screen.hasControlDown() ? 100 : 10 : 1;
            entry = Pokedex.getInstance().getNext(entry, i);
            PacketPokedex.selectedMob.clear();
            this.parent.pokemob = EventsHandlerClient.getRenderMob(entry, this.watch.player.getEntityWorld());
            this.parent.initPages(this.parent.pokemob);
        }).setTex(GuiPokeWatch.getWidgetTex()).setRender(new UVImgRender(212, 0, 12, 20)));
        final TexButton prevBtn = this.addButton(new TexButton(x - 96, y + 35, 12, 20, prev, b ->
        {
            PokedexEntry entry = this.parent.pokemob.getPokedexEntry();
            final int i = Screen.hasShiftDown() ? Screen.hasControlDown() ? 100 : 10 : 1;
            entry = Pokedex.getInstance().getPrevious(entry, i);
            PacketPokedex.selectedMob.clear();
            this.parent.pokemob = EventsHandlerClient.getRenderMob(entry, this.watch.player.getEntityWorld());
            this.parent.initPages(this.parent.pokemob);
        }).setTex(GuiPokeWatch.getWidgetTex()).setRender(new UVImgRender(212, 0, 12, 20)));
        final TexButton formBtn = this.addButton(new TexButton(x - 85, y + 35, 20, 10, form, b ->
        { // Cycle Form, only if not a real mob
            if (this.parent.pokemob.getEntity().addedToChunk) return;
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
                this.parent.initPages(this.parent.pokemob.megaEvolve(entry));
            }
            // This initializes the model holder
            this.parent.pokemob.setCustomHolder(holder);
            // This ensures the textures/etc are reset to account for the new
            // model holder.
            this.parent.pokemob.onGenesChanged();
        }).setTex(GuiPokeWatch.getWidgetTex()).setRender(new UVImgRender(224, 0, 20, 10)));
        final TexButton cryBtn = this.addButton(new TexButton(x - 85, y + 45, 20, 10, cry, b ->
        {
            this.watch.player.playSound(this.parent.pokemob.getSound(), 0.5f, 1.0F);
        }).setTex(GuiPokeWatch.getWidgetTex()).setRender(new UVImgRender(224, 0, 20, 10)));

        nextBtn.setFGColor(0x444444);
        prevBtn.setFGColor(0x444444);
        formBtn.setFGColor(0x444444);
        cryBtn.setFGColor(0x444444);
    }

    @Override
    public void render(final MatrixStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {
        this.drawInfo(mat, mouseX, mouseY, partialTicks);
        super.render(mat, mouseX, mouseY, partialTicks);
    }

}
