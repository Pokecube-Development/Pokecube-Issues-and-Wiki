package pokecube.core.client.gui.watch.progress;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Lists;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.stats.CaptureStats;
import pokecube.api.stats.EggStats;
import pokecube.api.stats.KillStats;
import pokecube.api.stats.SpecialCaseRegister;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import thut.core.common.ThutCore;
import thut.lib.TComponent;

public class PerTypeProgress extends Progress
{
    private static final List<String> NAMES = Lists.newArrayList();
    EditBox text;
    PokeType type;

    SuggestionProvider<CommandSourceStack> TYPESUGGESTER = (ctx, sb) -> net.minecraft.commands.SharedSuggestionProvider
            .suggest(PerTypeProgress.NAMES, sb);

    public PerTypeProgress(final GuiPokeWatch watch)
    {
        super(TComponent.translatable("pokewatch.progress.type.title"), watch);
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
        final int y = this.watch.height / 2 + 53; //-30
        this.text = new EditBox(this.font, x, y - 18, 60, 10, TComponent.literal(""));
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

        final MutableComponent captureLine = TComponent.translatable("pokewatch.progress.type.caught", this.caught1,
                this.caught0, this.type, total_of_type);
        final MutableComponent killLine = TComponent.translatable("pokewatch.progress.type.killed", this.killed1,
                this.killed0, this.type, total_of_type);
        final MutableComponent hatchLine = TComponent.translatable("pokewatch.progress.type.hatched", this.hatched1,
                this.hatched0, this.type, total_of_type);

        final AABB centre = this.watch.player.getBoundingBox();
        final AABB bb = centre.inflate(PokecubeCore.getConfig().maxSpawnRadius, 5,
                PokecubeCore.getConfig().maxSpawnRadius);
        final List<Entity> otherMobs = this.watch.player.getLevel().getEntities(this.watch.player, bb, input -> {
            IPokemob pokemob;
            if (!(input instanceof Animal && (pokemob = PokemobCaps.getPokemobFor(input)) != null)) return false;
            return pokemob.isType(PerTypeProgress.this.type);
        });
        final MutableComponent nearbyLine = TComponent.translatable("pokewatch.progress.global.nearby",
                otherMobs.size());

        for (var line : this.font.getSplitter().splitLines(captureLine, 205, Style.EMPTY))
            this.lines.add(line.getString());
        this.lines.add("");
        for (var line : this.font.getSplitter().splitLines(killLine, 205, Style.EMPTY))
            this.lines.add(line.getString());
        this.lines.add("");
        for (var line : this.font.getSplitter().splitLines(hatchLine, 205, Style.EMPTY))
            this.lines.add(line.getString());
        this.lines.add("");
        for (var line : this.font.getSplitter().splitLines(nearbyLine, 205, Style.EMPTY))
            this.lines.add(line.getString());
    }

}
