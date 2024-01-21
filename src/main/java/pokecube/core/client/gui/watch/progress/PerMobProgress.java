package pokecube.core.client.gui.watch.progress;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import pokecube.api.data.Pokedex;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.stats.CaptureStats;
import pokecube.api.stats.EggStats;
import pokecube.api.stats.KillStats;
import pokecube.core.PokecubeCore;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.database.Database;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.network.packets.PacketPokedex;
import thut.core.common.ThutCore;
import thut.lib.TComponent;

public class PerMobProgress extends Progress
{
    EditBox text;
    PokedexEntry entry = null;

    public PerMobProgress(final GuiPokeWatch watch)
    {
        super(TComponent.translatable("pokewatch.progress.mob.title"), watch);
    }

    @Override
    public boolean keyPressed(final int keyCode, final int b, final int c)
    {
        if (keyCode == GLFW.GLFW_KEY_TAB)
        {
            final String text = this.text.getValue();
            final String trimmed = ThutCore.trim(text);
            final List<String> ret = new ArrayList<>();
            for (final PokedexEntry entry : Database.getSortedFormes())
            {
                final String check = ThutCore.trim(entry.getName());
                if (check.startsWith(trimmed))
                {
                    String name = entry.getName();
                    if (name.contains(" ")) name = "\'" + name + "\'";
                    ret.add(name);
                }
            }
            Collections.sort(ret, (o1, o2) -> {
                if (o1.startsWith("'") && !o2.startsWith("'")) return 1;
                else if (o2.startsWith("'") && !o1.startsWith("'")) return -1;
                return o1.compareToIgnoreCase(o2);
            });
            ret.replaceAll(t -> {
                if (t.startsWith("'") && t.endsWith("'")) t = t.substring(1, t.length() - 1);
                return t;
            });
            if (!ret.isEmpty()) this.text.setValue(ret.get(0));
            return true;
        }
        else if (keyCode == GLFW.GLFW_KEY_ENTER)
        {
            final PokedexEntry newEntry = Database.getEntry(this.text.getValue());
            if (newEntry != null)
            {
                this.text.setValue(newEntry.getName());
                PacketPokedex.updateWatchEntry(newEntry);
                this.entry = newEntry;
                this.onPageOpened();
            }
            else this.text.setValue(this.entry.getName());
            return true;
        }
        return super.keyPressed(keyCode, b, c);
    }

    @Override
    public void init()
    {
        super.init();
        final int x = this.watch.width / 2 - 70;
        final int y = this.watch.height / 2 + 53;
        this.text = new EditBox(this.font, x, y - 18, 140, 10, TComponent.literal(""));
        this.addRenderableWidget(this.text);
    }

    @Override
    public void onPageOpened()
    {
        this.lines.clear();
        if (this.entry == null)
        {
            final String name = PokecubePlayerDataHandler.getCustomDataTag(this.watch.player).getString("WEntry");
            this.entry = Database.getEntry(name);
            if (this.entry == null) this.entry = Pokedex.getInstance().getFirstEntry();
        }
        Player player = this.watch.player;
        if (this.watch.target instanceof Player) player = (Player) this.watch.target;
        this.text.setValue(this.entry.getName());
        this.caught0 = CaptureStats.getTotalNumberOfPokemobCaughtBy(player.getUUID(), this.entry);
        this.hatched0 = EggStats.getTotalNumberOfPokemobHatchedBy(player.getUUID(), this.entry);
        this.killed0 = KillStats.getTotalNumberOfPokemobKilledBy(player.getUUID(), this.entry);

        final MutableComponent captureLine = TComponent.translatable("pokewatch.progress.mob.caught", this.caught0,
                this.entry);
        final MutableComponent killLine = TComponent.translatable("pokewatch.progress.mob.killed", this.killed0,
                this.entry);
        final MutableComponent hatchLine = TComponent.translatable("pokewatch.progress.mob.hatched", this.hatched0,
                this.entry);

        final AABB centre = this.watch.player.getBoundingBox();
        final AABB bb = centre.inflate(PokecubeCore.getConfig().maxSpawnRadius, 5,
                PokecubeCore.getConfig().maxSpawnRadius);
        final List<Entity> otherMobs = this.watch.player.getLevel().getEntities(this.watch.player, bb, input -> {
            IPokemob pokemob;
            if (!(input instanceof Animal && (pokemob = PokemobCaps.getPokemobFor(input)) != null)) return false;
            return pokemob.getPokedexEntry() == PerMobProgress.this.entry;
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
