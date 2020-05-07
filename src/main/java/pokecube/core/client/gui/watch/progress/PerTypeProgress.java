package pokecube.core.client.gui.watch.progress;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Lists;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.PokecubeCore;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.database.stats.EggStats;
import pokecube.core.database.stats.KillStats;
import pokecube.core.database.stats.SpecialCaseRegister;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.utils.PokeType;
import thut.core.common.ThutCore;

public class PerTypeProgress extends Progress
{
    private static final List<String> NAMES = Lists.newArrayList();
    TextFieldWidget                   text;
    PokeType                          type;

    SuggestionProvider<CommandSource> TYPESUGGESTER = (ctx, sb) -> net.minecraft.command.ISuggestionProvider.suggest(
            PerTypeProgress.NAMES, sb);

    public PerTypeProgress(final GuiPokeWatch watch)
    {
        super(new TranslationTextComponent("pokewatch.progress.type.title"), watch);
        if (PerTypeProgress.NAMES.isEmpty()) for (final PokeType type : PokeType.values())
            PerTypeProgress.NAMES.add(PokeType.getTranslatedName(type));
    }

    @Override
    public boolean keyPressed(final int keyCode, final int b, final int c)
    {
        System.out.println(" " + keyCode);
        if (keyCode == GLFW.GLFW_KEY_TAB)
        {
            final String text = this.text.getText();
            final List<String> ret = new ArrayList<>();
            for (final PokeType type : PokeType.values())
            {
                final String check = ThutCore.trim(PokeType.getTranslatedName(type));
                if (check.startsWith(ThutCore.trim(text)))
                {
                    final String name = PokeType.getTranslatedName(type);
                    ret.add(name);
                }
            }
            if (!ret.isEmpty()) this.text.setText(ret.get(0));
            return true;
        }
        else if (keyCode == GLFW.GLFW_KEY_ENTER)
        {
            final PokeType newType = PokeType.getType(this.text.getText());
            if (newType != null)
            {
                this.text.setText(PokeType.getTranslatedName(newType));
                this.type = newType;
                this.onPageOpened();
            }
            else this.text.setText(PokeType.getTranslatedName(this.type));
            return true;
        }
        return super.keyPressed(keyCode, b, c);
    }

    @Override
    public void init()
    {
        super.init();
        final int x = this.watch.width / 2 - 30;
        final int y = this.watch.height / 2 + 53;
        this.text = new TextFieldWidget(this.font, x, y, 60, 10, "");
        this.addButton(this.text);
    }

    @Override
    public void onPageOpened()
    {
        this.lines.clear();
        if (this.type == null)
        {
            final int index = PokeType.values().length == 0 ? 0 : 1;
            this.type = PokeType.values()[index];
        }
        this.text.setText(PokeType.getTranslatedName(this.type));

        final int total_of_type = SpecialCaseRegister.countSpawnableTypes(this.type);

        this.caught0 = CaptureStats.getUniqueOfTypeCaughtBy(this.watch.player.getUniqueID(), this.type);
        this.caught1 = CaptureStats.getTotalOfTypeCaughtBy(this.watch.player.getUniqueID(), this.type);

        this.hatched0 = EggStats.getUniqueOfTypeHatchedBy(this.watch.player.getUniqueID(), this.type);
        this.hatched1 = EggStats.getTotalOfTypeHatchedBy(this.watch.player.getUniqueID(), this.type);

        this.killed0 = KillStats.getUniqueOfTypeKilledBy(this.watch.player.getUniqueID(), this.type);
        this.killed1 = KillStats.getTotalOfTypeKilledBy(this.watch.player.getUniqueID(), this.type);

        final String captureLine = I18n.format("pokewatch.progress.type.caught", this.caught1, this.caught0, this.type,
                total_of_type);
        final String killLine = I18n.format("pokewatch.progress.type.killed", this.killed1, this.killed0, this.type,
                total_of_type);
        final String hatchLine = I18n.format("pokewatch.progress.type.hatched", this.hatched1, this.hatched0, this.type,
                total_of_type);

        final AxisAlignedBB centre = this.watch.player.getBoundingBox();
        final AxisAlignedBB bb = centre.grow(PokecubeCore.getConfig().maxSpawnRadius, 5, PokecubeCore
                .getConfig().maxSpawnRadius);
        final List<Entity> otherMobs = this.watch.player.getEntityWorld().getEntitiesInAABBexcluding(this.watch.player,
                bb, input ->
                {
                    IPokemob pokemob;
                    if (!(input instanceof AnimalEntity && (pokemob = CapabilityPokemob.getPokemobFor(input)) != null))
                        return false;
                    return pokemob.isType(PerTypeProgress.this.type);
                });
        final String nearbyLine = I18n.format("pokewatch.progress.global.nearby", otherMobs.size());

        for (final String line : this.font.listFormattedStringToWidth(captureLine, 140))
            this.lines.add(line);
        this.lines.add("");
        for (final String line : this.font.listFormattedStringToWidth(killLine, 140))
            this.lines.add(line);
        this.lines.add("");
        for (final String line : this.font.listFormattedStringToWidth(hatchLine, 140))
            this.lines.add(line);
        this.lines.add("");
        for (final String line : this.font.listFormattedStringToWidth(nearbyLine, 140))
            this.lines.add(line);
    }

}
