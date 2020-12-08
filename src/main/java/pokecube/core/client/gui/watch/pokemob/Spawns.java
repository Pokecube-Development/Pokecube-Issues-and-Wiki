package pokecube.core.client.gui.watch.pokemob;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.util.ResourceLocation;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.client.gui.watch.PokemobInfoPage;
import pokecube.core.client.gui.watch.util.LineEntry;
import pokecube.core.client.gui.watch.util.SpawnListEntry;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.network.packets.PacketPokedex;

public class Spawns extends ListPage<LineEntry>
{

    int last = 0;

    public static final ResourceLocation           TEXTURE_BASE  = new ResourceLocation(PokecubeMod.ID,
    		"textures/gui/pokewatchgui_spawn.png");
    
    public Spawns(final PokemobInfoPage parent)
    {
        super(parent, "spawns");
    }

    @Override
    public void renderBackground(MatrixStack mat) 
    {
    	this.minecraft.textureManager.bindTexture(Spawns.TEXTURE_BASE);
    	int offsetX = (this.watch.width - GuiPokeWatch.GUIW) / 2;
        int offsetY = (this.watch.height - GuiPokeWatch.GUIH) / 2;
    	this.blit(mat, offsetX, offsetY, 0, 0, GuiPokeWatch.GUIW, GuiPokeWatch.GUIH);
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
        int offsetX = (this.watch.width - GuiPokeWatch.GUIW) / 2 + 90;
        int offsetY = (this.watch.height - GuiPokeWatch.GUIH) / 2 + 30;
        final int height = this.font.FONT_HEIGHT * 7;
        int width = 110;
        
        final int dx = 50;
        final int dy = 5;
        offsetX += dx;
        offsetY += dy;
        
        this.list = new ScrollGui<>(this, this.minecraft, width, height - this.font.FONT_HEIGHT / 2,
        		this.font.FONT_HEIGHT, offsetX, offsetY);
        for (final SpawnBiomeMatcher matcher : PacketPokedex.selectedMob)
        {
            final SpawnListEntry entry = new SpawnListEntry(this, this.font, matcher, null, 100, height, offsetY);
            entry.getLines(this.list, null).forEach(c -> this.list.addEntry(c));
        }

    }

}
