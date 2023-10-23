package pokecube.core.client.gui.watch.pokemob;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import pokecube.api.entity.pokemob.Nature;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.client.gui.watch.PokemobInfoPage;
import thut.lib.TComponent;

public class Bonus extends PokeInfoPage
{
    public static final ResourceLocation TEX_DM = GuiPokeWatch.makeWatchTexture("pokewatchgui_pokedex_battle");
    public static final ResourceLocation TEX_NM = GuiPokeWatch.makeWatchTexture("pokewatchgui_pokedex_battle_nm");

    int last = 0;
    final PokemobInfoPage parent;

    private MultiLineLabel splitRenderer = MultiLineLabel.EMPTY;
    private final Font fontRender;

    public Bonus(final PokemobInfoPage parent)
    {
        super(parent, "extra", Bonus.TEX_DM, Bonus.TEX_NM);
        this.parent = parent;
        this.fontRender = Minecraft.getInstance().font;
    }

    // Default
    private void drawBaseStats(final GuiGraphics graphics, final int x, final int y)
    {

    }

    // Your Pokemob
    private void drawInfo(final GuiGraphics graphics, final int x, final int y)
    {
        final int offsetX = 120;
        int dx = 20 + offsetX;

        // Draw ability, Happiness and Size
        final String ability = this.parent.pokemob.getAbilityName();
        final Nature nature = this.parent.pokemob.getNature();
        dx = 145;
        int dy = 40;

        final int abilitycolour = 0x000080;
        final int sizeColour = 0x333333;
        final int natureColour = 0x9ACD32;

        // Draw ability
        if (!ability.isEmpty())
        {
            final String abilityName = I18n.get(ability);
            graphics.drawString(font, I18n.get("pokewatch.ability", abilityName), x + dx, y + dy, abilitycolour, false);
        }
        final int happiness = this.parent.pokemob.getHappiness();
        MutableComponent message = TComponent.literal("");

        // Draw size
        dy += 15; // 50 //10
        message = TComponent.translatable("pokewatch.size", "%.2f".formatted(this.parent.pokemob.getSize()));
        this.splitRenderer = MultiLineLabel.create(this.fontRender, message, 100);
        this.splitRenderer.renderLeftAlignedNoShadow(graphics, x + dx, y + dy, 12, sizeColour);

        // Draw Nature
        dy += 14; // 50 //11
        if (nature != null)
        {
            message = TComponent.translatable("pokewatch.nature", this.parent.pokemob.getNature());
            this.splitRenderer = MultiLineLabel.create(this.fontRender, message, 100);
            this.splitRenderer.renderLeftAlignedNoShadow(graphics, x + dx, y + dy, 12, natureColour);
        }

        if (happiness == 0) message = TComponent.translatable("pokemob.info.happy0");
        if (happiness > 0) message = TComponent.translatable("pokemob.info.happy1");
        if (happiness > 49) message = TComponent.translatable("pokemob.info.happy2");
        if (happiness > 99) message = TComponent.translatable("pokemob.info.happy3");
        if (happiness > 149) message = TComponent.translatable("pokemob.info.happy4");
        if (happiness > 199) message = TComponent.translatable("pokemob.info.happy5");
        if (happiness > 254) message = TComponent.translatable("pokemob.info.happy6");
        // Draw Happiness
        dy += 19; // 50 //16
        this.splitRenderer = MultiLineLabel.create(this.fontRender, message, 100);
        this.splitRenderer.renderLeftAlignedNoShadow(graphics, x + dx, y + dy, 12, sizeColour);
    }

    @Override
    void drawInfo(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks)
    {
        final int x = (this.watch.width - GuiPokeWatch.GUIW) / 2;
        final int y = (this.watch.height - GuiPokeWatch.GUIH) / 2;
        if (this.watch.canEdit(this.parent.pokemob)) this.drawInfo(graphics, x, y);
        else this.drawBaseStats(graphics, x, y);
    }
}
