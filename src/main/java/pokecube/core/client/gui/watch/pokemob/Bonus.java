package pokecube.core.client.gui.watch.pokemob;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IBidiRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.client.gui.watch.PokemobInfoPage;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.Nature;
import pokecube.core.interfaces.PokecubeMod;

public class Bonus extends PokeInfoPage
{

    public static final ResourceLocation TEX_DM = new ResourceLocation(PokecubeMod.ID,
            "textures/gui/pokewatchgui_battle.png");
    public static final ResourceLocation TEX_NM = new ResourceLocation(PokecubeMod.ID,
            "textures/gui/pokewatchgui_battle_nm.png");

    int                   last = 0;
    final PokemobInfoPage parent;

    private IBidiRenderer      splitRenderer = IBidiRenderer.EMPTY;
    private final FontRenderer fontRender;

    public Bonus(final PokemobInfoPage parent)
    {
        super(parent, "extra", Bonus.TEX_DM, Bonus.TEX_NM);
        this.parent = parent;
        this.fontRender = Minecraft.getInstance().font;
    }

    // Default
    private void drawBaseStats(final MatrixStack mat, final int x, final int y)
    {

    }

    // Your Pokemob
    private void drawInfo(final MatrixStack mat, final int x, final int y)
    {
        final int offsetX = 120; // -52
        int dx = 20 + offsetX;

        // Draw ability, Happiness and Size
        final Ability ability = this.parent.pokemob.getAbility();
        final Nature nature = this.parent.pokemob.getNature();
        dx = 145; // 55
        int dy = 40; // 25

        final int abilitycolour = GuiPokeWatch.nightMode ? 0x444444 : 0x444444;
        final int sizeColour = GuiPokeWatch.nightMode ? 0x444444 : 0x444444;
        final int natureColour = GuiPokeWatch.nightMode ? 0x444444 : 0x444444;

        // Draw ability
        if (ability != null)
        {
            final String abilityName = I18n.get(ability.getName());
            this.font.draw(mat, I18n.get("pokewatch.ability", abilityName), x + dx, y + dy, abilitycolour);
        }
        final int happiness = this.parent.pokemob.getHappiness();
        TextComponent message = new StringTextComponent("");

        // Draw size
        dy += 10; // 50
        message = new TranslationTextComponent("pokewatch.size", this.parent.pokemob.getSize());
        this.splitRenderer = IBidiRenderer.create(this.fontRender, message, 100);
        this.splitRenderer.renderLeftAlignedNoShadow(mat, x + dx, y + dy, 12, sizeColour);

        // Draw Nature
        dy += 11; // 50
        if (nature != null)
        {
            message = new TranslationTextComponent("pokewatch.nature", this.parent.pokemob.getNature());
            this.splitRenderer = IBidiRenderer.create(this.fontRender, message, 100);
            this.splitRenderer.renderLeftAlignedNoShadow(mat, x + dx, y + dy, 12, natureColour);
        }

        if (happiness == 0) message = new TranslationTextComponent("pokemob.info.happy0");
        if (happiness > 0) message = new TranslationTextComponent("pokemob.info.happy1");
        if (happiness > 49) message = new TranslationTextComponent("pokemob.info.happy2");
        if (happiness > 99) message = new TranslationTextComponent("pokemob.info.happy3");
        if (happiness > 149) message = new TranslationTextComponent("pokemob.info.happy4");
        if (happiness > 199) message = new TranslationTextComponent("pokemob.info.happy5");
        if (happiness > 254) message = new TranslationTextComponent("pokemob.info.happy6");
        // Draw Happiness
        dy += 16; // 50
        this.splitRenderer = IBidiRenderer.create(this.fontRender, message, 100);
        this.splitRenderer.renderLeftAlignedNoShadow(mat, x + dx, y + dy, 12, abilitycolour);
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
