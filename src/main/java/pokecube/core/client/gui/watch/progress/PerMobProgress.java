package pokecube.core.client.gui.watch.progress;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.widget.TextFieldWidget;
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
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.database.stats.EggStats;
import pokecube.core.database.stats.KillStats;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.network.packets.PacketPokedex;
import thut.core.common.ThutCore;

public class PerMobProgress extends Progress
{
    TextFieldWidget text;
    PokedexEntry    entry = null;

    public PerMobProgress(final GuiPokeWatch watch)
    {
        super(new TranslationTextComponent("pokewatch.progress.mob.title"), watch);
    }

    @Override
    public boolean keyPressed(final int keyCode, final int b, final int c)
    {
        if (keyCode == GLFW.GLFW_KEY_TAB)
        {
            final String text = this.text.getText();
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
            Collections.sort(ret, (o1, o2) ->
            {
                if (o1.startsWith("'") && !o2.startsWith("'")) return 1;
                else if (o2.startsWith("'") && !o1.startsWith("'")) return -1;
                return o1.compareToIgnoreCase(o2);
            });
            ret.replaceAll(t ->
            {
                if (t.startsWith("'") && t.endsWith("'")) t = t.substring(1, t.length() - 1);
                return t;
            });
            if (!ret.isEmpty()) this.text.setText(ret.get(0));
            return true;
        }
        else if (keyCode == GLFW.GLFW_KEY_ENTER)
        {
            final PokedexEntry newEntry = Database.getEntry(this.text.getText());
            if (newEntry != null)
            {
                this.text.setText(newEntry.getName());
                PacketPokedex.updateWatchEntry(newEntry);
                this.entry = newEntry;
                this.onPageOpened();
            }
            else this.text.setText(this.entry.getName());
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
        this.text = new TextFieldWidget(this.font, x, y - 30, 140, 10, new StringTextComponent(""));
        this.addButton(this.text);
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
        PlayerEntity player = this.watch.player;
        if (this.watch.target instanceof PlayerEntity) player = (PlayerEntity) this.watch.target;
        this.text.setText(this.entry.getName());
        this.caught0 = CaptureStats.getTotalNumberOfPokemobCaughtBy(player.getUniqueID(), this.entry);
        this.hatched0 = EggStats.getTotalNumberOfPokemobHatchedBy(player.getUniqueID(), this.entry);
        this.killed0 = KillStats.getTotalNumberOfPokemobKilledBy(player.getUniqueID(), this.entry);

        final TranslationTextComponent captureLine = new TranslationTextComponent("pokewatch.progress.mob.caught",
                this.caught0, this.entry);
        final TranslationTextComponent killLine = new TranslationTextComponent("pokewatch.progress.mob.killed",
                this.killed0, this.entry);
        final TranslationTextComponent hatchLine = new TranslationTextComponent("pokewatch.progress.mob.hatched",
                this.hatched0, this.entry);

        final AxisAlignedBB centre = this.watch.player.getBoundingBox();
        final AxisAlignedBB bb = centre.grow(PokecubeCore.getConfig().maxSpawnRadius, 5, PokecubeCore
                .getConfig().maxSpawnRadius);
        final List<Entity> otherMobs = this.watch.player.getEntityWorld().getEntitiesInAABBexcluding(this.watch.player,
                bb, input ->
                {
                    IPokemob pokemob;
                    if (!(input instanceof AnimalEntity && (pokemob = CapabilityPokemob.getPokemobFor(input)) != null))
                        return false;
                    return pokemob.getPokedexEntry() == PerMobProgress.this.entry;
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
