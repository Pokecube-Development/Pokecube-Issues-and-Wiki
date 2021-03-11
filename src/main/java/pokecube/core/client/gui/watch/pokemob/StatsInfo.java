package pokecube.core.client.gui.watch.pokemob;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.client.gui.watch.PokemobInfoPage;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.PokecubeMod;

public class StatsInfo extends PokeInfoPage
{
    public static final ResourceLocation TEX_DM = new ResourceLocation(PokecubeMod.ID,
            "textures/gui/pokewatchgui_pokedex.png");
    public static final ResourceLocation TEX_NM = new ResourceLocation(PokecubeMod.ID,
            "textures/gui/pokewatchgui_pokedex_nm.png");

    public StatsInfo(final PokemobInfoPage parent)
    {
        super(parent, "stats", StatsInfo.TEX_DM, StatsInfo.TEX_NM);
    }

    // Default
    private void drawBaseStats(final MatrixStack mat, final int x, final int y)
    {
        final int HP = this.parent.pokemob.getPokedexEntry().getStatHP();
        final int ATT = this.parent.pokemob.getPokedexEntry().getStatATT();
        final int DEF = this.parent.pokemob.getPokedexEntry().getStatDEF();
        final int ATTSPE = this.parent.pokemob.getPokedexEntry().getStatATTSPE();
        final int DEFSPE = this.parent.pokemob.getPokedexEntry().getStatDEFSPE();
        final int VIT = this.parent.pokemob.getPokedexEntry().getStatVIT();
        final int statYOffSet = y + 35; // 0
        final int offsetX = 130; // -50
        int dx = 20 + offsetX;

        final String H = I18n.get("pokewatch.HP");
        final String A = I18n.get("pokewatch.ATT");
        final String D = I18n.get("pokewatch.DEF");
        final String AS = I18n.get("pokewatch.ATTSP");
        final String DS = I18n.get("pokewatch.DEFSP");
        final String S = I18n.get("pokewatch.VIT");

        AbstractGui.drawString(mat, this.font, H, x + dx, statYOffSet + 18, 0xFF0000);
        AbstractGui.drawString(mat, this.font, A, x + dx, statYOffSet + 27, 0xF08030);
        AbstractGui.drawString(mat, this.font, D, x + dx, statYOffSet + 36, 0xF8D030);
        AbstractGui.drawString(mat, this.font, AS, x + dx, statYOffSet + 45, 0x6890F0);
        AbstractGui.drawString(mat, this.font, DS, x + dx, statYOffSet + 54, 0x78C850);
        AbstractGui.drawString(mat, this.font, S, x + dx, statYOffSet + 63, 0xF85888);

        dx = 60 + offsetX;
        AbstractGui.drawString(mat, this.font, ": " + HP, x + dx, statYOffSet + 18, 0xFF0000);
        AbstractGui.drawString(mat, this.font, ": " + ATT, x + dx, statYOffSet + 27, 0xF08030);
        AbstractGui.drawString(mat, this.font, ": " + DEF, x + dx, statYOffSet + 36, 0xF8D030);
        AbstractGui.drawString(mat, this.font, ": " + ATTSPE, x + dx, statYOffSet + 45, 0x6890F0);
        AbstractGui.drawString(mat, this.font, ": " + DEFSPE, x + dx, statYOffSet + 54, 0x78C850);
        AbstractGui.drawString(mat, this.font, ": " + VIT, x + dx, statYOffSet + 63, 0xF85888);
    }

    // Your Pokemob
    private void drawInfo(final MatrixStack mat, final int x, final int y)
    {
        final byte[] nature = this.parent.pokemob.getNature().getStatsMod();
        int HP = this.parent.pokemob.getStat(Stats.HP, true);
        int ATT = this.parent.pokemob.getStat(Stats.ATTACK, true);
        int DEF = this.parent.pokemob.getStat(Stats.DEFENSE, true);
        int ATTSPE = this.parent.pokemob.getStat(Stats.SPATTACK, true);
        int DEFSPE = this.parent.pokemob.getStat(Stats.SPDEFENSE, true);
        int VIT = this.parent.pokemob.getStat(Stats.VIT, true);
        final int statYOffSet = y + 25; // 58
        final String[] nat = new String[6];
        final int[] colours = new int[6];
        for (int n = 0; n < 6; n++)
        {
            nat[n] = "";
            colours[n] = 0;
            if (nature[n] == -1)
            {
                nat[n] = "-";
                colours[n] = 0x4400FF00;
            }
            if (nature[n] == 1)
            {
                nat[n] = "+";
                colours[n] = 0x44FF0000;
            }
        }
        final int offsetX = 120;
        int dx = 20 + offsetX;
        for (int i = 0; i < nature.length; i++)
        {
            final int dy = 17 + i * 9;
            AbstractGui.fill(mat, x + dx, statYOffSet + dy, x + dx + 107, statYOffSet + dy + 9, colours[i]);
        }

        final String H = I18n.get("pokewatch.HP");
        final String A = I18n.get("pokewatch.ATT");
        final String D = I18n.get("pokewatch.DEF");
        final String AS = I18n.get("pokewatch.ATTSP");
        final String DS = I18n.get("pokewatch.DEFSP");
        final String S = I18n.get("pokewatch.VIT");

        final String Header = I18n.get("pokewatch.TVIVEV");

        AbstractGui.drawString(mat, this.font, Header, 43 + x + dx, statYOffSet + 9, 0xFFFFFF);
        AbstractGui.drawString(mat, this.font, H, x + dx, statYOffSet + 18, 0xFF0000);
        AbstractGui.drawString(mat, this.font, A, x + dx, statYOffSet + 27, 0xF08030);
        AbstractGui.drawString(mat, this.font, D, x + dx, statYOffSet + 36, 0xF8D030);
        AbstractGui.drawString(mat, this.font, AS, x + dx, statYOffSet + 45, 0x6890F0);
        AbstractGui.drawString(mat, this.font, DS, x + dx, statYOffSet + 54, 0x78C850);
        AbstractGui.drawString(mat, this.font, S, x + dx, statYOffSet + 63, 0xF85888);

        dx = 60 + offsetX;
        AbstractGui.drawString(mat, this.font, ": " + HP, x + dx, statYOffSet + 18, 0xFF0000);
        AbstractGui.drawString(mat, this.font, ": " + ATT, x + dx, statYOffSet + 27, 0xF08030);
        AbstractGui.drawString(mat, this.font, ": " + DEF, x + dx, statYOffSet + 36, 0xF8D030);
        AbstractGui.drawString(mat, this.font, ": " + ATTSPE, x + dx, statYOffSet + 45, 0x6890F0);
        AbstractGui.drawString(mat, this.font, ": " + DEFSPE, x + dx, statYOffSet + 54, 0x78C850);
        AbstractGui.drawString(mat, this.font, ": " + VIT, x + dx, statYOffSet + 63, 0xF85888);

        dx = 20 + offsetX;
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

        int shift = 88 + offsetX;
        AbstractGui.drawString(mat, this.font, "" + HP, x + shift, statYOffSet + 18, 0xFF0000);
        AbstractGui.drawString(mat, this.font, "" + ATT, x + shift, statYOffSet + 27, 0xF08030);
        AbstractGui.drawString(mat, this.font, "" + DEF, x + shift, statYOffSet + 36, 0xF8D030);
        AbstractGui.drawString(mat, this.font, "" + ATTSPE, x + shift, statYOffSet + 45, 0x6890F0);
        AbstractGui.drawString(mat, this.font, "" + DEFSPE, x + shift, statYOffSet + 54, 0x78C850);
        AbstractGui.drawString(mat, this.font, "" + VIT, x + shift, statYOffSet + 63, 0xF85888);
        shift += 21;
        AbstractGui.drawString(mat, this.font, "" + HP2, x + shift, statYOffSet + 18, 0xFF0000);
        AbstractGui.drawString(mat, this.font, "" + ATT2, x + shift, statYOffSet + 27, 0xF08030);
        AbstractGui.drawString(mat, this.font, "" + DEF2, x + shift, statYOffSet + 36, 0xF8D030);
        AbstractGui.drawString(mat, this.font, "" + ATTSPE2, x + shift, statYOffSet + 45, 0x6890F0);
        AbstractGui.drawString(mat, this.font, "" + DEFSPE2, x + shift, statYOffSet + 54, 0x78C850);
        AbstractGui.drawString(mat, this.font, "" + VIT2, x + shift, statYOffSet + 63, 0xF85888);
    }

    @Override
    void drawInfo(final MatrixStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {
        final int x = (this.watch.width - GuiPokeWatch.GUIW) / 2;
        final int y = (this.watch.height - GuiPokeWatch.GUIH) / 2;
        if (this.watch.canEdit(this.parent.pokemob)) this.drawInfo(mat, x, y);
        else this.drawBaseStats(mat, x, y);
    }

}
