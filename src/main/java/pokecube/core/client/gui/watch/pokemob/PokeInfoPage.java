package pokecube.core.client.gui.watch.pokemob;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.client.EventsHandlerClient;
import pokecube.core.client.gui.watch.PokemobInfoPage;
import pokecube.core.client.gui.watch.util.WatchPage;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;

public abstract class PokeInfoPage extends WatchPage
{
    final PokemobInfoPage parent;

    public PokeInfoPage(final PokemobInfoPage parent, final String title)
    {
        super(new TranslationTextComponent("pokewatch.title.pokeinfo." + title), parent.watch);
        this.parent = parent;
    }

    abstract void drawInfo(int mouseX, int mouseY, float partialTicks);

    @Override
    public void init()
    {
        super.init();
        final int x = this.watch.width / 2;
        final int y = this.watch.height / 2 - 5;
        final String next = ">";
        final String prev = "<";
        this.addButton(new Button(x - 46, y + 4, 12, 20, next, b ->
        {
            PokedexEntry entry = this.parent.pokemob.getPokedexEntry();
            final int i = Screen.hasShiftDown() ? Screen.hasControlDown() ? 100 : 10 : 1;
            entry = Pokedex.getInstance().getNext(entry, i);
            this.parent.pokemob = EventsHandlerClient.getRenderMob(entry, this.watch.player.getEntityWorld());
            this.parent.initPages(this.parent.pokemob);
        }));
        this.addButton(new Button(x - 76, y + 4, 12, 20, prev, b ->
        {
            PokedexEntry entry = this.parent.pokemob.getPokedexEntry();
            final int i = Screen.hasShiftDown() ? Screen.hasControlDown() ? 100 : 10 : 1;
            entry = Pokedex.getInstance().getPrevious(entry, i);
            this.parent.pokemob = EventsHandlerClient.getRenderMob(entry, this.watch.player.getEntityWorld());
            this.parent.initPages(this.parent.pokemob);
        }));
        this.addButton(new Button(x - 65, y + 4, 20, 9, "\u2500", b ->
        { // Cycle Form.
            PokedexEntry entry = this.parent.pokemob.getPokedexEntry();
            final List<PokedexEntry> forms = Lists.newArrayList(Database.getFormes(entry));
            final int index = forms.indexOf(this.parent.pokemob.getPokedexEntry());
            if (index != -1)
            {
                entry = forms.get((index + 1) % forms.size());
                this.parent.initPages(this.parent.pokemob.megaEvolve(entry));
            }
        }));
        this.addButton(new Button(x - 65, y + 13, 20, 10, "\u266B", b ->
        {
            this.watch.player.playSound(this.parent.pokemob.getSound(), 0.5f, 1.0F);
        }));
    }

    @Override
    public void render(final int mouseX, final int mouseY, final float partialTicks)
    {
        this.drawInfo(mouseX, mouseY, partialTicks);
        super.render(mouseX, mouseY, partialTicks);
    }

}
