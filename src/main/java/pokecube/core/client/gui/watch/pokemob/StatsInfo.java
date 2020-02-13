package pokecube.core.client.gui.watch.pokemob;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import pokecube.core.client.gui.watch.PokemobInfoPage;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob.Stats;

public class StatsInfo extends PokeInfoPage
{
    private final FontRenderer fontRender;

    public StatsInfo(final PokemobInfoPage parent)
    {
        super(parent, "stats");
        this.fontRender = Minecraft.getInstance().fontRenderer;
    }

    private void drawBaseStats(final int x, final int y)
    {
        final int HP = this.parent.pokemob.getPokedexEntry().getStatHP();
        final int ATT = this.parent.pokemob.getPokedexEntry().getStatATT();
        final int DEF = this.parent.pokemob.getPokedexEntry().getStatDEF();
        final int ATTSPE = this.parent.pokemob.getPokedexEntry().getStatATTSPE();
        final int DEFSPE = this.parent.pokemob.getPokedexEntry().getStatDEFSPE();
        final int VIT = this.parent.pokemob.getPokedexEntry().getStatVIT();
        final int statYOffSet = y + 0;
        final int offsetX = -50;
        int dx = 20 + offsetX;

        final String H = I18n.format("pokewatch.HP");
        final String A = I18n.format("pokewatch.ATT");
        final String D = I18n.format("pokewatch.DEF");
        final String AS = I18n.format("pokewatch.ATTSP");
        final String DS = I18n.format("pokewatch.DEFSP");
        final String S = I18n.format("pokewatch.VIT");

        this.drawString(this.fontRender, H, x + dx, statYOffSet + 18, 0xFF0000);
        this.drawString(this.fontRender, A, x + dx, statYOffSet + 27, 0xF08030);
        this.drawString(this.fontRender, D, x + dx, statYOffSet + 36, 0xF8D030);
        this.drawString(this.fontRender, AS, x + dx, statYOffSet + 45, 0x6890F0);
        this.drawString(this.fontRender, DS, x + dx, statYOffSet + 54, 0x78C850);
        this.drawString(this.fontRender, S, x + dx, statYOffSet + 63, 0xF85888);

        dx = 60 + offsetX;
        this.drawString(this.fontRender, ": " + HP, x + dx, statYOffSet + 18, 0xFF0000);
        this.drawString(this.fontRender, ": " + ATT, x + dx, statYOffSet + 27, 0xF08030);
        this.drawString(this.fontRender, ": " + DEF, x + dx, statYOffSet + 36, 0xF8D030);
        this.drawString(this.fontRender, ": " + ATTSPE, x + dx, statYOffSet + 45, 0x6890F0);
        this.drawString(this.fontRender, ": " + DEFSPE, x + dx, statYOffSet + 54, 0x78C850);
        this.drawString(this.fontRender, ": " + VIT, x + dx, statYOffSet + 63, 0xF85888);
    }

    private void drawInfo(final int x, final int y)
    {
        final byte[] nature = this.parent.pokemob.getNature().getStatsMod();
        int HP = this.parent.pokemob.getStat(Stats.HP, true);
        int ATT = this.parent.pokemob.getStat(Stats.ATTACK, true);
        int DEF = this.parent.pokemob.getStat(Stats.DEFENSE, true);
        int ATTSPE = this.parent.pokemob.getStat(Stats.SPATTACK, true);
        int DEFSPE = this.parent.pokemob.getStat(Stats.SPDEFENSE, true);
        int VIT = this.parent.pokemob.getStat(Stats.VIT, true);
        final int statYOffSet = y + 58;
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
        final int offsetX = -52;
        int dx = 20 + offsetX;
        for (int i = 0; i < nature.length; i++)
        {
            final int dy = 17 + i * 9;
            AbstractGui.fill(x + dx, statYOffSet + dy, x + dx + 107, statYOffSet + dy + 9, colours[i]);
        }

        final String H = I18n.format("pokewatch.HP");
        final String A = I18n.format("pokewatch.ATT");
        final String D = I18n.format("pokewatch.DEF");
        final String AS = I18n.format("pokewatch.ATTSP");
        final String DS = I18n.format("pokewatch.DEFSP");
        final String S = I18n.format("pokewatch.VIT");

        final String Header = I18n.format("pokewatch.TVIVEV");

        this.drawString(this.fontRender, Header, x + dx, statYOffSet + 9, 0xFFFFFF);
        this.drawString(this.fontRender, H, x + dx, statYOffSet + 18, 0xFF0000);
        this.drawString(this.fontRender, A, x + dx, statYOffSet + 27, 0xF08030);
        this.drawString(this.fontRender, D, x + dx, statYOffSet + 36, 0xF8D030);
        this.drawString(this.fontRender, AS, x + dx, statYOffSet + 45, 0x6890F0);
        this.drawString(this.fontRender, DS, x + dx, statYOffSet + 54, 0x78C850);
        this.drawString(this.fontRender, S, x + dx, statYOffSet + 63, 0xF85888);

        dx = 60 + offsetX;
        this.drawString(this.fontRender, ": " + HP, x + dx, statYOffSet + 18, 0xFF0000);
        this.drawString(this.fontRender, ": " + ATT, x + dx, statYOffSet + 27, 0xF08030);
        this.drawString(this.fontRender, ": " + DEF, x + dx, statYOffSet + 36, 0xF8D030);
        this.drawString(this.fontRender, ": " + ATTSPE, x + dx, statYOffSet + 45, 0x6890F0);
        this.drawString(this.fontRender, ": " + DEFSPE, x + dx, statYOffSet + 54, 0x78C850);
        this.drawString(this.fontRender, ": " + VIT, x + dx, statYOffSet + 63, 0xF85888);

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
        this.drawString(this.fontRender, "" + HP, x + shift, statYOffSet + 18, 0xFF0000);
        this.drawString(this.fontRender, "" + ATT, x + shift, statYOffSet + 27, 0xF08030);
        this.drawString(this.fontRender, "" + DEF, x + shift, statYOffSet + 36, 0xF8D030);
        this.drawString(this.fontRender, "" + ATTSPE, x + shift, statYOffSet + 45, 0x6890F0);
        this.drawString(this.fontRender, "" + DEFSPE, x + shift, statYOffSet + 54, 0x78C850);
        this.drawString(this.fontRender, "" + VIT, x + shift, statYOffSet + 63, 0xF85888);
        shift += 21;
        this.drawString(this.fontRender, "" + HP2, x + shift, statYOffSet + 18, 0xFF0000);
        this.drawString(this.fontRender, "" + ATT2, x + shift, statYOffSet + 27, 0xF08030);
        this.drawString(this.fontRender, "" + DEF2, x + shift, statYOffSet + 36, 0xF8D030);
        this.drawString(this.fontRender, "" + ATTSPE2, x + shift, statYOffSet + 45, 0x6890F0);
        this.drawString(this.fontRender, "" + DEFSPE2, x + shift, statYOffSet + 54, 0x78C850);
        this.drawString(this.fontRender, "" + VIT2, x + shift, statYOffSet + 63, 0xF85888);

        // Draw ability, Happiness and Size
        final Ability ability = this.parent.pokemob.getAbility();
        dx = -25;
        int dy = 25;
        // Draw ability
        if (ability != null)
        {
            final String abilityName = I18n.format(ability.getName());
            this.drawString(this.fontRender, I18n.format("pokewatch.ability", abilityName), x + dx, y + dy, 0xFFFFFF);
        }
        final int happiness = this.parent.pokemob.getHappiness();
        String message = "";

        // Draw size
        dy += 11;
        message = I18n.format("pokewatch.size", this.parent.pokemob.getSize());
        this.fontRender.drawSplitString(message, x + dx, y + dy, 100, 0xFFFFFF);

        if (happiness == 0) message = "pokemob.info.happy0";
        if (happiness > 0) message = "pokemob.info.happy1";
        if (happiness > 49) message = "pokemob.info.happy2";
        if (happiness > 99) message = "pokemob.info.happy3";
        if (happiness > 149) message = "pokemob.info.happy4";
        if (happiness > 199) message = "pokemob.info.happy5";
        if (happiness > 254) message = "pokemob.info.happy6";
        // Draw Happiness
        message = I18n.format(message);
        dy += 11;
        this.fontRender.drawSplitString(message, x + dx, y + dy, 100, 0xFFFFFF);
    }

    @Override
    void drawInfo(final int mouseX, final int mouseY, final float partialTicks)
    {
        final int x = (this.watch.width - 160) / 2 + 80;
        final int y = (this.watch.height - 160) / 2 + 8;
        if (this.watch.canEdit(this.parent.pokemob)) this.drawInfo(x, y);
        else this.drawBaseStats(x, y);
    }

}
