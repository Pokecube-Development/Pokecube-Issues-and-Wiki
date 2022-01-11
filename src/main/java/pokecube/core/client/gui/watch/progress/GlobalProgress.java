package pokecube.core.client.gui.watch.progress;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import pokecube.core.PokecubeCore;
import pokecube.core.client.gui.helper.ListHelper;
import pokecube.core.client.gui.helper.TexButton;
import pokecube.core.client.gui.helper.TexButton.UVImgRender;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.database.stats.EggStats;
import pokecube.core.database.stats.KillStats;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.network.packets.PacketPokedex;

public class GlobalProgress extends Progress
{

    public GlobalProgress(final GuiPokeWatch watch)
    {
        super(new TranslatableComponent("pokewatch.progress.global.title"), watch);
    }

    @Override
    public void onPageOpened()
    {
        this.lines.clear();

        Player player = this.watch.player;
        if (this.watch.target instanceof Player) player = (Player) this.watch.target;

        this.caught0 = CaptureStats.getNumberUniqueCaughtBy(player.getUUID());
        this.caught1 = CaptureStats.getTotalNumberCaughtBy(player.getUUID());

        this.hatched0 = EggStats.getNumberUniqueHatchedBy(player.getUUID());
        this.hatched1 = EggStats.getTotalNumberHatchedBy(player.getUUID());

        this.killed0 = KillStats.getNumberUniqueKilledBy(player.getUUID());
        this.killed1 = KillStats.getTotalNumberKilledBy(player.getUUID());

        final TranslatableComponent captureLine = new TranslatableComponent("pokewatch.progress.global.caught",
                this.caught1, this.caught0);
        final TranslatableComponent killLine = new TranslatableComponent("pokewatch.progress.global.killed",
                this.killed1, this.killed0);
        final TranslatableComponent hatchLine = new TranslatableComponent("pokewatch.progress.global.hatched",
                this.hatched1, this.hatched0);

        final AABB centre = this.watch.player.getBoundingBox();
        final AABB bb = centre.inflate(PokecubeCore.getConfig().maxSpawnRadius, 5, PokecubeCore
                .getConfig().maxSpawnRadius);
        final List<Entity> otherMobs = this.watch.player.getLevel().getEntities(this.watch.player,
                bb, input -> input instanceof Animal && CapabilityPokemob.getPokemobFor(input) != null);
        final TranslatableComponent nearbyLine = new TranslatableComponent("pokewatch.progress.global.nearby",
                otherMobs.size());

        final int x = this.watch.width / 2;
        final int y = this.watch.height / 2 - 5;

        final Component inspect = new TranslatableComponent("pokewatch.progress.inspect");

        final TexButton inspectBtn = this.addRenderableWidget(new TexButton(x - 50, y + 25, 100, 12, inspect, b ->
        {
            PacketPokedex.sendInspectPacket(true, Minecraft.getInstance().getLanguageManager().getSelected()
                    .getCode());
        }).setTex(GuiPokeWatch.getWidgetTex()).setRender(new UVImgRender(0, 72, 100, 12)));

        inspectBtn.setFGColor(0x444444);

        if (player != this.watch.player) inspectBtn.visible = false;

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
