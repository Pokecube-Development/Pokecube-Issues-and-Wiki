package pokecube.core.client.gui.watch.pokemob;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
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
        dx = 133; // 145
        int dy = 40; // 40

        final int abilitycolour = 0x000080; //GuiPokeWatch.nightMode ? 0x444444 : 0x444444;
        final int sizeColour = 0x333333; //GuiPokeWatch.nightMode ? 0x444444 : 0x444444;
        final int natureColour = 0x9ACD32; //GuiPokeWatch.nightMode ? 0x444444 : 0x444444;

        // Draw ability
        if (!ability.isEmpty())
        {
            final String abilityName = I18n.get(ability);
            this.font.draw(mat, I18n.get("pokewatch.ability", abilityName), x + dx, y + dy, abilitycolour);
        }
        final int happiness = this.parent.pokemob.getHappiness();
        MutableComponent message = TComponent.literal("");

        // Draw size
        dy += 12; // 52
        message = TComponent.translatable("pokewatch.size", "%.2f".formatted(this.parent.pokemob.getSize()));
        this.splitRenderer = MultiLineLabel.create(this.fontRender, message, 112);
        this.splitRenderer.renderLeftAlignedNoShadow(mat, x + dx, y + dy, 12, sizeColour);

        // Draw Nature
        dy += 12; // 64
        if (nature != null)
        {
            message = TComponent.translatable("pokewatch.nature", this.parent.pokemob.getNature());
            this.splitRenderer = MultiLineLabel.create(this.fontRender, message, 112);
            this.splitRenderer.renderLeftAlignedNoShadow(mat, x + dx, y + dy, 12, natureColour);
        }

        if (happiness == 0) message = TComponent.translatable("pokemob.info.happy0", this.parent.pokemob.getDisplayName()).withStyle(ChatFormatting.DARK_RED);
        if (happiness > 0) message = TComponent.translatable("pokemob.info.happy1", this.parent.pokemob.getDisplayName()).withStyle(ChatFormatting.RED);
        if (happiness > 49) message = TComponent.translatable("pokemob.info.happy2", this.parent.pokemob.getDisplayName());
        if (happiness > 99) message = TComponent.translatable("pokemob.info.happy3", this.parent.pokemob.getDisplayName());
        if (happiness > 149) message = TComponent.translatable("pokemob.info.happy4", this.parent.pokemob.getDisplayName());
        if (happiness > 199) message = TComponent.translatable("pokemob.info.happy5", this.parent.pokemob.getDisplayName()).withStyle(ChatFormatting.DARK_GREEN);
        if (happiness > 254) message = TComponent.translatable("pokemob.info.happy6", this.parent.pokemob.getDisplayName()).withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.BOLD);
        // Draw Happiness
        dy += 12; // 76
        this.splitRenderer = MultiLineLabel.create(this.fontRender, message, 112);
        this.splitRenderer.renderLeftAlignedNoShadow(mat, x + dx, y + dy, 12, sizeColour);
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
