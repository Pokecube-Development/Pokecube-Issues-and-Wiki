package pokecube.core.client.gui.watch.pokemob;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.resources.ResourceLocation;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.client.gui.watch.PokemobInfoPage;
import pokecube.core.client.gui.watch.util.LineEntry;
import pokecube.core.client.gui.watch.util.SpawnListEntry;
import pokecube.core.network.packets.PacketPokedex;

public class Spawns extends ListPage<LineEntry>
{
    public static final ResourceLocation TEX_DM = GuiPokeWatch.makeWatchTexture("pokewatchgui_pokedex_spawn");
    public static final ResourceLocation TEX_NM = GuiPokeWatch.makeWatchTexture("pokewatchgui_pokedex_spawn_nm");

    int last = 0;

    public Spawns(final PokemobInfoPage parent)
    {
        super(parent, "spawns", Spawns.TEX_DM, Spawns.TEX_NM);
    }

    @Override
    void drawInfo(final PoseStack mat, final int mouseX, final int mouseY, final float partialTicks)
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
        int offsetX = (this.watch.width - GuiPokeWatch.GUIW) / 2 + 90;
        int offsetY = (this.watch.height - GuiPokeWatch.GUIH) / 2 + 30;
        final int height = this.font.lineHeight * 9; //7
        final int width = 110;

        final int dx = 48; //50
        final int dy = 12; //5
        offsetX += dx;
        offsetY += dy;

        this.list = new ScrollGui<>(this, this.minecraft, width, height - this.font.lineHeight / 2,
                this.font.lineHeight, offsetX, offsetY);
        for (final SpawnBiomeMatcher matcher : PacketPokedex.selectedMob)
        {
            final SpawnListEntry entry = new SpawnListEntry(this, this.font, matcher, null, 100, height, offsetY)
                    .noRate();
            entry.getLines(this.list, null).forEach(c -> this.list.addEntry(c));
        }

    }

}
