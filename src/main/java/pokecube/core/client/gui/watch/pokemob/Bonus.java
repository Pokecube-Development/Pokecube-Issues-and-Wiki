package pokecube.core.client.gui.watch.pokemob;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.client.gui.watch.PokemobInfoPage;
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

    private MultiLineLabel      splitRenderer = MultiLineLabel.EMPTY;
    private final Font fontRender;

    public Bonus(final PokemobInfoPage parent)
    {
        super(parent, "extra", Bonus.TEX_DM, Bonus.TEX_NM);
        this.parent = parent;
        this.fontRender = Minecraft.getInstance().font;
    }

    // Default
    private void drawBaseStats(final PoseStack mat, final int x, final int y)
    {

    }

    // Your Pokemob
    private void drawInfo(final PoseStack mat, final int x, final int y)
    {
        final int offsetX = 120; // -52
        int dx = 20 + offsetX;

        // Draw ability, Happiness and Size
        final String ability = this.parent.pokemob.getAbilityName();
        final Nature nature = this.parent.pokemob.getNature();
        dx = 145; // 55
        int dy = 40; // 25

        final int abilitycolour = GuiPokeWatch.nightMode ? 0x444444 : 0x444444;
        final int sizeColour = GuiPokeWatch.nightMode ? 0x444444 : 0x444444;
        final int natureColour = GuiPokeWatch.nightMode ? 0x444444 : 0x444444;

        // Draw ability
        if (!ability.isEmpty())
        {
            final String abilityName = I18n.get(ability);
            this.font.draw(mat, I18n.get("pokewatch.ability", abilityName), x + dx, y + dy, abilitycolour);
        }
        final int happiness = this.parent.pokemob.getHappiness();
        BaseComponent message = new TextComponent("");

        // Draw size
        dy += 10; // 50
        message = new TranslatableComponent("pokewatch.size", this.parent.pokemob.getSize());
        this.splitRenderer = MultiLineLabel.create(this.fontRender, message, 100);
        this.splitRenderer.renderLeftAlignedNoShadow(mat, x + dx, y + dy, 12, sizeColour);

        // Draw Nature
        dy += 11; // 50
        if (nature != null)
        {
            message = new TranslatableComponent("pokewatch.nature", this.parent.pokemob.getNature());
            this.splitRenderer = MultiLineLabel.create(this.fontRender, message, 100);
            this.splitRenderer.renderLeftAlignedNoShadow(mat, x + dx, y + dy, 12, natureColour);
        }

        if (happiness == 0) message = new TranslatableComponent("pokemob.info.happy0");
        if (happiness > 0) message = new TranslatableComponent("pokemob.info.happy1");
        if (happiness > 49) message = new TranslatableComponent("pokemob.info.happy2");
        if (happiness > 99) message = new TranslatableComponent("pokemob.info.happy3");
        if (happiness > 149) message = new TranslatableComponent("pokemob.info.happy4");
        if (happiness > 199) message = new TranslatableComponent("pokemob.info.happy5");
        if (happiness > 254) message = new TranslatableComponent("pokemob.info.happy6");
        // Draw Happiness
        dy += 16; // 50
        this.splitRenderer = MultiLineLabel.create(this.fontRender, message, 100);
        this.splitRenderer.renderLeftAlignedNoShadow(mat, x + dx, y + dy, 12, abilitycolour);
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
