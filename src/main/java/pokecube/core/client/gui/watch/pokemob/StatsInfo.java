package pokecube.core.client.gui.watch.pokemob;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import pokecube.api.entity.pokemob.IPokemob.Stats;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.client.gui.watch.PokemobInfoPage;

public class StatsInfo extends PokeInfoPage
{
    public static final ResourceLocation TEX_DM = GuiPokeWatch.makeWatchTexture("pokewatchgui_pokedex");
    public static final ResourceLocation TEX_NM = GuiPokeWatch.makeWatchTexture("pokewatchgui_pokedex_nm");

    public StatsInfo(final PokemobInfoPage parent)
    {
        super(parent, "stats", StatsInfo.TEX_DM, StatsInfo.TEX_NM);
    }

    // Default
    private void drawBaseStats(final PoseStack mat, final int x, final int y)
    {
        final int HP = this.parent.pokemob.getPokedexEntry().getStatHP();
        final int ATT = this.parent.pokemob.getPokedexEntry().getStatATT();
        final int DEF = this.parent.pokemob.getPokedexEntry().getStatDEF();
        final int ATTSPE = this.parent.pokemob.getPokedexEntry().getStatATTSPE();
        final int DEFSPE = this.parent.pokemob.getPokedexEntry().getStatDEFSPE();
        final int VIT = this.parent.pokemob.getPokedexEntry().getStatVIT();
        final int statYOffSet = y + 35; // 0 //45
        final int offsetX = 120; // -50 //135
        int dx = 15 + offsetX; // 20

        final String H = I18n.get("pokewatch.HP");
        final String A = I18n.get("pokewatch.ATT");
        final String D = I18n.get("pokewatch.DEF");
        final String AS = I18n.get("pokewatch.ATTSP");
        final String DS = I18n.get("pokewatch.DEFSP");
        final String S = I18n.get("pokewatch.VIT");

        this.font.draw(mat, H, x + dx, statYOffSet + 18, 0xFF0000);
        this.font.draw(mat, A, x + dx, statYOffSet + 27, 0xF08030);
        this.font.draw(mat, D, x + dx, statYOffSet + 36, 0xF8D030);
        this.font.draw(mat, AS, x + dx, statYOffSet + 45, 0x6890F0);
        this.font.draw(mat, DS, x + dx, statYOffSet + 54, 0x78C850);
        this.font.draw(mat, S, x + dx, statYOffSet + 63, 0xF85888);

        dx = 80 + offsetX; // 60
        this.font.draw(mat, ": " + HP, x + dx, statYOffSet + 18, 0xFF0000);
        this.font.draw(mat, ": " + ATT, x + dx, statYOffSet + 27, 0xF08030);
        this.font.draw(mat, ": " + DEF, x + dx, statYOffSet + 36, 0xF8D030);
        this.font.draw(mat, ": " + ATTSPE, x + dx, statYOffSet + 45, 0x6890F0);
        this.font.draw(mat, ": " + DEFSPE, x + dx, statYOffSet + 54, 0x78C850);
        this.font.draw(mat, ": " + VIT, x + dx, statYOffSet + 63, 0xF85888);
    }

    // Your Pokemob
    private void drawInfo(final PoseStack mat, final int x, final int y)
    {
        final byte[] nature = this.parent.pokemob.getNature().getStatsMod();
        int HP = this.parent.pokemob.getStat(Stats.HP, true);
        int ATT = this.parent.pokemob.getStat(Stats.ATTACK, true);
        int DEF = this.parent.pokemob.getStat(Stats.DEFENSE, true);
        int ATTSPE = this.parent.pokemob.getStat(Stats.SPATTACK, true);
        int DEFSPE = this.parent.pokemob.getStat(Stats.SPDEFENSE, true);
        int VIT = this.parent.pokemob.getStat(Stats.VIT, true);
        final int statYOffSet = y + 35; // 58 //25
        final String[] nat = new String[6];
        final int[] colours = new int[6];
        for (int n = 0; n < 6; n++)
        {
            nat[n] = " ";
            colours[n] = 0;
            if (nature[n] == -1)
            {
                // up arrow
                nat[n] = "\u2b07";
                colours[n] = 0x4400FF00;
            }
            if (nature[n] == 1)
            {
                // down arrow
                nat[n] = "\u2b06";
                colours[n] = 0x44FF0000;
            }
        }
        final int offsetX = 120;
        int dx = 15 + offsetX;
        for (int i = 0; i < nature.length; i++)
        {
            final int dy = 17 + i * 9;
            GuiComponent.fill(mat, x + dx, statYOffSet + dy, x + dx + 107, statYOffSet + dy + 9, colours[i]);
        }

        final String H = I18n.get("pokewatch.HP");
        final String A = I18n.get("pokewatch.ATT");
        final String D = I18n.get("pokewatch.DEF");
        final String AS = I18n.get("pokewatch.ATTSP");
        final String DS = I18n.get("pokewatch.DEFSP");
        final String S = I18n.get("pokewatch.VIT");

        final String Header = I18n.get("pokewatch.TVIVEV");

        this.font.draw(mat, Header, 43 + x + dx, statYOffSet + 9, 0x666666);
        this.font.draw(mat, H, x + dx, statYOffSet + 18, 0xFF0000);
        this.font.draw(mat, A, x + dx, statYOffSet + 27, 0xF08030);
        this.font.draw(mat, D, x + dx, statYOffSet + 36, 0xF8D030);
        this.font.draw(mat, AS, x + dx, statYOffSet + 45, 0x6890F0);
        this.font.draw(mat, DS, x + dx, statYOffSet + 54, 0x78C850);
        this.font.draw(mat, S, x + dx, statYOffSet + 63, 0xF85888);

        dx = 50 + offsetX; // 56
        this.font.draw(mat, nat[0] + ": " + HP, x + dx, statYOffSet + 18, 0xFF0000);
        this.font.draw(mat, nat[1] + ": " + ATT, x + dx, statYOffSet + 27, 0xF08030);
        this.font.draw(mat, nat[2] + ": " + DEF, x + dx, statYOffSet + 36, 0xF8D030);
        this.font.draw(mat, nat[3] + ": " + ATTSPE, x + dx, statYOffSet + 45, 0x6890F0);
        this.font.draw(mat, nat[4] + ": " + DEFSPE, x + dx, statYOffSet + 54, 0x78C850);
        this.font.draw(mat, nat[5] + ": " + VIT, x + dx, statYOffSet + 63, 0xF85888);

        dx = 15 + offsetX; // 20
        byte[] stats2 = this.parent.pokemob.getIVs();
        HP = stats2[0];
        ATT = stats2[1];
        DEF = stats2[2];
        ATTSPE = stats2[3];
        DEFSPE = stats2[4];
        VIT = stats2[5];
        stats2 = this.parent.pokemob.getEVs();
        final int HP2 = stats2[0] + 128;
        final int ATT2 = stats2[1] + 128;
        final int DEF2 = stats2[2] + 128;
        final int ATTSPE2 = stats2[3] + 128;
        final int DEFSPE2 = stats2[4] + 128;
        final int VIT2 = stats2[5] + 128;

        int shift = 83 + offsetX; // 44
        this.font.draw(mat, "" + HP, x + shift, statYOffSet + 18, 0xFF0000);
        this.font.draw(mat, "" + ATT, x + shift, statYOffSet + 27, 0xF08030);
        this.font.draw(mat, "" + DEF, x + shift, statYOffSet + 36, 0xF8D030);
        this.font.draw(mat, "" + ATTSPE, x + shift, statYOffSet + 45, 0x6890F0);
        this.font.draw(mat, "" + DEFSPE, x + shift, statYOffSet + 54, 0x78C850);
        this.font.draw(mat, "" + VIT, x + shift, statYOffSet + 63, 0xF85888);
        shift += 21; // 21
        this.font.draw(mat, "" + HP2, x + shift, statYOffSet + 18, 0xFF0000);
        this.font.draw(mat, "" + ATT2, x + shift, statYOffSet + 27, 0xF08030);
        this.font.draw(mat, "" + DEF2, x + shift, statYOffSet + 36, 0xF8D030);
        this.font.draw(mat, "" + ATTSPE2, x + shift, statYOffSet + 45, 0x6890F0);
        this.font.draw(mat, "" + DEFSPE2, x + shift, statYOffSet + 54, 0x78C850);
        this.font.draw(mat, "" + VIT2, x + shift, statYOffSet + 63, 0xF85888);
    }

    @Override
    void drawInfo(final PoseStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {
        final int x = (this.watch.width - GuiPokeWatch.GUIW) / 2;
        final int y = (this.watch.height - GuiPokeWatch.GUIH) / 2;
        if (this.watch.canEdit(this.parent.pokemob)) this.drawInfo(mat, x, y);
        else this.drawBaseStats(mat, x, y);
    }

}
