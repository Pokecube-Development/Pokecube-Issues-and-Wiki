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

    long last = 0;

    public Spawns(final PokemobInfoPage parent)
    {
        super(parent, "spawns", Spawns.TEX_DM, Spawns.TEX_NM);
    }

    @Override
    void drawInfo(final PoseStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {
        // This is to give extra time for packet syncing.
        if (PacketPokedex.changed != this.last)
        {
            this.initList();
            this.last = PacketPokedex.changed;
            this.children.add(this.list);
        }
    }

    @Override
    public void initList()
    {
        super.initList();
        int offsetX = (this.watch.width - GuiPokeWatch.GUIW) / 2 + 90;
        int offsetY = (this.watch.height - GuiPokeWatch.GUIH) / 2 + 26;
        final int height = this.font.lineHeight * 12;
        final int width = 120;

        final int dx = 41;
        final int dy = 8;
        offsetX += dx;
        offsetY += dy;

        if (GuiPokeWatch.nightMode)
        {
            this.list = new ScrollGui<LineEntry>(this, this.minecraft, width, height,
                this.font.lineHeight, offsetX, offsetY)
                .setScrollBarColor(255, 172, 56)
                .setScrollBarDarkBorder(165, 81, 36)
                .setScrollBarGrayBorder(255, 128, 55)
                .setScrollBarLightBorder(255, 255, 255)
                .setScrollColor(255, 128, 55)
                .setScrollDarkBorder(165, 81, 36)
                .setScrollLightBorder(255, 255, 255);
        } else {
            this.list = new ScrollGui<LineEntry>(this, this.minecraft, width, height,
                this.font.lineHeight, offsetX, offsetY)
                .setScrollBarColor(83, 175, 255)
                .setScrollBarDarkBorder(39, 75, 142)
                .setScrollBarGrayBorder(69, 132, 249)
                .setScrollBarLightBorder(255, 255, 255)
                .setScrollColor(69, 132, 249)
                .setScrollDarkBorder(39, 75, 142)
                .setScrollLightBorder(255, 255, 255);
        }

        for (int i = 0; i < PacketPokedex.selectedMob.size(); i++)
        {
            SpawnBiomeMatcher matcher = PacketPokedex.selectedMob.get(i);
            int colour = -1;
            if (PacketPokedex.validSpawnIndex.get(i)) colour = 0x267F00;
            SpawnListEntry entry = new SpawnListEntry(this, this.font, matcher, null, width - 10, height, offsetY).noRate();
            entry.getLines(this.list, null, colour).forEach(c -> this.list.addEntry(c));
        }
    }

}
