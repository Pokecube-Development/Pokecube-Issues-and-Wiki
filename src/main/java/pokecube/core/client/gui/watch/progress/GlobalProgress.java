package pokecube.core.client.gui.watch.progress;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.stats.CaptureStats;
import pokecube.api.stats.EggStats;
import pokecube.api.stats.KillStats;
import pokecube.core.PokecubeCore;
import pokecube.core.client.gui.helper.TexButton;
import pokecube.core.client.gui.helper.TexButton.UVImgRender;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.network.packets.PacketPokedex;
import thut.lib.TComponent;

public class GlobalProgress extends Progress
{

    public GlobalProgress(final GuiPokeWatch watch)
    {
        super(TComponent.translatable("pokewatch.progress.global.title"), watch);
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

        final MutableComponent captureLine = TComponent.translatable("pokewatch.progress.global.caught", this.caught1,
                this.caught0);
        final MutableComponent killLine = TComponent.translatable("pokewatch.progress.global.killed", this.killed1,
                this.killed0);
        final MutableComponent hatchLine = TComponent.translatable("pokewatch.progress.global.hatched", this.hatched1,
                this.hatched0);

        final AABB centre = this.watch.player.getBoundingBox();
        final AABB bb = centre.inflate(PokecubeCore.getConfig().maxSpawnRadius, 5,
                PokecubeCore.getConfig().maxSpawnRadius);
        final List<Entity> otherMobs = this.watch.player.getLevel().getEntities(this.watch.player, bb,
                input -> input instanceof Animal && PokemobCaps.getPokemobFor(input) != null);
        final MutableComponent nearbyLine = TComponent.translatable("pokewatch.progress.global.nearby",
                otherMobs.size());

        final int x = this.watch.width / 2;
        final int y = this.watch.height / 2 - 5;

        final Component inspect = TComponent.translatable("pokewatch.progress.inspect");

        final TexButton inspectBtn = this.addRenderableWidget(new TexButton(x - 50, y + 38, 100, 12, inspect, b -> {
            PacketPokedex.sendInspectPacket(true, Minecraft.getInstance().getLanguageManager().getSelected().getCode());
        }).setTex(GuiPokeWatch.getWidgetTex()).setRender(new UVImgRender(0, 72, 100, 12)));

        inspectBtn.setFGColor(0x444444);

        if (player != this.watch.player) inspectBtn.visible = false;

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
