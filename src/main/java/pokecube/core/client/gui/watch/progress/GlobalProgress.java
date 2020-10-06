package pokecube.core.client.gui.watch.progress;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.PokecubeCore;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.database.stats.EggStats;
import pokecube.core.database.stats.KillStats;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.network.packets.PacketPokedex;

public class GlobalProgress extends Progress
{
    Button button;

    public GlobalProgress(final GuiPokeWatch watch)
    {
        super(new TranslationTextComponent("pokewatch.progress.global.title"), watch);
    }

    @Override
    public void onPageOpened()
    {
        this.lines.clear();
        this.caught0 = CaptureStats.getNumberUniqueCaughtBy(this.watch.player.getUniqueID());
        this.caught1 = CaptureStats.getTotalNumberCaughtBy(this.watch.player.getUniqueID());

        this.hatched0 = EggStats.getNumberUniqueHatchedBy(this.watch.player.getUniqueID());
        this.hatched1 = EggStats.getTotalNumberHatchedBy(this.watch.player.getUniqueID());

        this.killed0 = KillStats.getNumberUniqueKilledBy(this.watch.player.getUniqueID());
        this.killed1 = KillStats.getTotalNumberKilledBy(this.watch.player.getUniqueID());

        final String captureLine = I18n.format("pokewatch.progress.global.caught", this.caught1, this.caught0);
        final String killLine = I18n.format("pokewatch.progress.global.killed", this.killed1, this.killed0);
        final String hatchLine = I18n.format("pokewatch.progress.global.hatched", this.hatched1, this.hatched0);

        final AxisAlignedBB centre = this.watch.player.getBoundingBox();
        final AxisAlignedBB bb = centre.grow(PokecubeCore.getConfig().maxSpawnRadius, 5, PokecubeCore
                .getConfig().maxSpawnRadius);
        final List<Entity> otherMobs = this.watch.player.getEntityWorld().getEntitiesInAABBexcluding(this.watch.player,
                bb, input -> input instanceof AnimalEntity && CapabilityPokemob.getPokemobFor(input) != null);
        final String nearbyLine = I18n.format("pokewatch.progress.global.nearby", otherMobs.size());

        final int x = this.watch.width / 2;
        final int y = this.watch.height / 2 - 5;
        this.addButton(this.button = new Button(x - 50, y + 57, 100, 12, new TranslationTextComponent("pokewatch.progress.inspect"), b ->
        {
            PacketPokedex.sendInspectPacket(true, Minecraft.getInstance().getLanguageManager().getCurrentLanguage()
                    .getCode());
        }));

        for (final String line : this.font.listFormattedStringToWidth(captureLine, 120))
            this.lines.add(line);
        this.lines.add("");
        for (final String line : this.font.listFormattedStringToWidth(killLine, 120))
            this.lines.add(line);
        this.lines.add("");
        for (final String line : this.font.listFormattedStringToWidth(hatchLine, 120))
            this.lines.add(line);
        this.lines.add("");
        for (final String line : this.font.listFormattedStringToWidth(nearbyLine, 120))
            this.lines.add(line);
    }

}
