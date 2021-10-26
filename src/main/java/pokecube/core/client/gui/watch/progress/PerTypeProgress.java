package pokecube.core.client.gui.watch.progress;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Lists;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
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
    EditBox                   text;
    PokeType                          type;

    SuggestionProvider<CommandSourceStack> TYPESUGGESTER = (ctx, sb) -> net.minecraft.commands.SharedSuggestionProvider.suggest(
            PerTypeProgress.NAMES, sb);

    public PerTypeProgress(final GuiPokeWatch watch)
    {
        super(new TranslatableComponent("pokewatch.progress.type.title"), watch);
        if (PerTypeProgress.NAMES.isEmpty()) for (final PokeType type : PokeType.values())
            PerTypeProgress.NAMES.add(PokeType.getTranslatedName(type).getString());
    }

    @Override
    public boolean keyPressed(final int keyCode, final int b, final int c)
    {
        if (keyCode == GLFW.GLFW_KEY_TAB)
        {
            final String text = this.text.getValue();
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
            if (!ret.isEmpty()) this.text.setValue(ret.get(0));
            return true;
        }
        else if (keyCode == GLFW.GLFW_KEY_ENTER)
        {
            final PokeType newType = PokeType.getType(this.text.getValue());
            if (newType != null)
            {
                this.text.setValue(PokeType.getTranslatedName(newType).getString());
                this.type = newType;
                this.onPageOpened();
            }
            else this.text.setValue(PokeType.getTranslatedName(this.type).getString());
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
        this.text = new EditBox(this.font, x, y - 30, 60, 10, new TextComponent(""));
        this.addRenderableWidget(this.text);
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
        Player player = this.watch.player;
        if (this.watch.target instanceof Player) player = (Player) this.watch.target;
        this.text.setValue(PokeType.getTranslatedName(this.type).getString());

        final int total_of_type = SpecialCaseRegister.countSpawnableTypes(this.type);

        this.caught0 = CaptureStats.getUniqueOfTypeCaughtBy(player.getUUID(), this.type);
        this.caught1 = CaptureStats.getTotalOfTypeCaughtBy(player.getUUID(), this.type);

        this.hatched0 = EggStats.getUniqueOfTypeHatchedBy(player.getUUID(), this.type);
        this.hatched1 = EggStats.getTotalOfTypeHatchedBy(player.getUUID(), this.type);

        this.killed0 = KillStats.getUniqueOfTypeKilledBy(player.getUUID(), this.type);
        this.killed1 = KillStats.getTotalOfTypeKilledBy(player.getUUID(), this.type);

        final TranslatableComponent captureLine = new TranslatableComponent("pokewatch.progress.type.caught",
                this.caught1, this.caught0, this.type, total_of_type);
        final TranslatableComponent killLine = new TranslatableComponent("pokewatch.progress.type.killed",
                this.killed1, this.killed0, this.type, total_of_type);
        final TranslatableComponent hatchLine = new TranslatableComponent("pokewatch.progress.type.hatched",
                this.hatched1, this.hatched0, this.type, total_of_type);

        final AABB centre = this.watch.player.getBoundingBox();
        final AABB bb = centre.inflate(PokecubeCore.getConfig().maxSpawnRadius, 5, PokecubeCore
                .getConfig().maxSpawnRadius);
        final List<Entity> otherMobs = this.watch.player.getCommandSenderWorld().getEntities(this.watch.player,
                bb, input ->
                {
                    IPokemob pokemob;
                    if (!(input instanceof Animal && (pokemob = CapabilityPokemob.getPokemobFor(input)) != null))
                        return false;
                    return pokemob.isType(PerTypeProgress.this.type);
                });
        final TranslatableComponent nearbyLine = new TranslatableComponent("pokewatch.progress.global.nearby",
                otherMobs.size());

        for (final MutableComponent line : ListHelper.splitText(captureLine, 190, this.font, false))
            this.lines.add(line.getString());
        this.lines.add("");
        for (final MutableComponent line : ListHelper.splitText(killLine, 190, this.font, false))
            this.lines.add(line.getString());
        this.lines.add("");
        for (final MutableComponent line : ListHelper.splitText(hatchLine, 190, this.font, false))
            this.lines.add(line.getString());
        this.lines.add("");
        for (final MutableComponent line : ListHelper.splitText(nearbyLine, 190, this.font, false))
            this.lines.add(line.getString());
    }

}
