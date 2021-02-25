package pokecube.core.client.gui.watch.progress;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Lists;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.PokecubeCore;
import pokecube.core.client.gui.helper.ListHelper;
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
            PerTypeProgress.NAMES.add(PokeType.getTranslatedName(type).getString());
    }

    @Override
    public boolean keyPressed(final int keyCode, final int b, final int c)
    {
        if (keyCode == GLFW.GLFW_KEY_TAB)
        {
            final String text = this.text.getText();
            final List<String> ret = new ArrayList<>();
            for (final PokeType type : PokeType.values())
            {
                final String check = ThutCore.trim(PokeType.getTranslatedName(type).getString());
                if (check.startsWith(ThutCore.trim(text)))
                {
                    final String name = PokeType.getTranslatedName(type).getString();
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
                this.text.setText(PokeType.getTranslatedName(newType).getString());
                this.type = newType;
                this.onPageOpened();
            }
            else this.text.setText(PokeType.getTranslatedName(this.type).getString());
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
        this.text = new TextFieldWidget(this.font, x, y - 30, 60, 10, new StringTextComponent(""));
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
        PlayerEntity player = this.watch.player;
        if (this.watch.target instanceof PlayerEntity) player = (PlayerEntity) this.watch.target;
        this.text.setText(PokeType.getTranslatedName(this.type).getString());

        final int total_of_type = SpecialCaseRegister.countSpawnableTypes(this.type);

        this.caught0 = CaptureStats.getUniqueOfTypeCaughtBy(player.getUniqueID(), this.type);
        this.caught1 = CaptureStats.getTotalOfTypeCaughtBy(player.getUniqueID(), this.type);

        this.hatched0 = EggStats.getUniqueOfTypeHatchedBy(player.getUniqueID(), this.type);
        this.hatched1 = EggStats.getTotalOfTypeHatchedBy(player.getUniqueID(), this.type);

        this.killed0 = KillStats.getUniqueOfTypeKilledBy(player.getUniqueID(), this.type);
        this.killed1 = KillStats.getTotalOfTypeKilledBy(player.getUniqueID(), this.type);

        final TranslationTextComponent captureLine = new TranslationTextComponent("pokewatch.progress.type.caught",
                this.caught1, this.caught0, this.type, total_of_type);
        final TranslationTextComponent killLine = new TranslationTextComponent("pokewatch.progress.type.killed",
                this.killed1, this.killed0, this.type, total_of_type);
        final TranslationTextComponent hatchLine = new TranslationTextComponent("pokewatch.progress.type.hatched",
                this.hatched1, this.hatched0, this.type, total_of_type);

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
        final TranslationTextComponent nearbyLine = new TranslationTextComponent("pokewatch.progress.global.nearby",
                otherMobs.size());

        for (final IFormattableTextComponent line : ListHelper.splitText(captureLine, 190, this.font, false))
            this.lines.add(line.getString());
        this.lines.add("");
        for (final IFormattableTextComponent line : ListHelper.splitText(killLine, 190, this.font, false))
            this.lines.add(line.getString());
        this.lines.add("");
        for (final IFormattableTextComponent line : ListHelper.splitText(hatchLine, 190, this.font, false))
            this.lines.add(line.getString());
        this.lines.add("");
        for (final IFormattableTextComponent line : ListHelper.splitText(nearbyLine, 190, this.font, false))
            this.lines.add(line.getString());
    }

}
