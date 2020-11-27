package pokecube.core.client.gui.watch.pokemob;

import com.mojang.blaze3d.matrix.MatrixStack;

import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.PokemobInfoPage;
import pokecube.core.client.gui.watch.util.LineEntry;
import pokecube.core.client.gui.watch.util.SpawnListEntry;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.network.packets.PacketPokedex;

public class Spawns extends ListPage<LineEntry>
{

    int last = 0;

    public Spawns(final PokemobInfoPage parent)
    {
        super(parent, "spawns");
    }

    @Override
    void drawInfo(final MatrixStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {
        // This is to give extra time for packet syncing.
        if (this.last != PacketPokedex.selectedMob.size())
        {
            this.initList();
            this.last = PacketPokedex.selectedMob.size();
            this.children.add(this.list);
        }
    }

    @Override
    public void initList()
    {
        super.initList();
        final int offsetX = (this.watch.width - 160) / 2 + 45;
        final int offsetY = (this.watch.height - 160) / 2 + 27;
        final int height = 110;
        final int max = this.font.FONT_HEIGHT;
        this.list = new ScrollGui<>(this, this.minecraft, 110, 10 * max, max, offsetX, offsetY);
        for (final SpawnBiomeMatcher matcher : PacketPokedex.selectedMob)
        {
            final SpawnListEntry entry = new SpawnListEntry(this, this.font, matcher, null, 100, height, offsetY);
            entry.getLines(this.list, null).forEach(c -> this.list.addEntry(c));
        }

    }

}
