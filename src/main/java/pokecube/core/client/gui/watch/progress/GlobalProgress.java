package pokecube.core.client.gui.watch.progress;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
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
        super(new TranslationTextComponent("pokewatch.progress.global.title"), watch);
    }

    @Override
    public void onPageOpened()
    {
        this.lines.clear();

        PlayerEntity player = this.watch.player;
        if (this.watch.target instanceof PlayerEntity) player = (PlayerEntity) this.watch.target;

        this.caught0 = CaptureStats.getNumberUniqueCaughtBy(player.getUniqueID());
        this.caught1 = CaptureStats.getTotalNumberCaughtBy(player.getUniqueID());

        this.hatched0 = EggStats.getNumberUniqueHatchedBy(player.getUniqueID());
        this.hatched1 = EggStats.getTotalNumberHatchedBy(player.getUniqueID());

        this.killed0 = KillStats.getNumberUniqueKilledBy(player.getUniqueID());
        this.killed1 = KillStats.getTotalNumberKilledBy(player.getUniqueID());

        final TranslationTextComponent captureLine = new TranslationTextComponent("pokewatch.progress.global.caught",
                this.caught1, this.caught0);
        final TranslationTextComponent killLine = new TranslationTextComponent("pokewatch.progress.global.killed",
                this.killed1, this.killed0);
        final TranslationTextComponent hatchLine = new TranslationTextComponent("pokewatch.progress.global.hatched",
                this.hatched1, this.hatched0);

        final AxisAlignedBB centre = this.watch.player.getBoundingBox();
        final AxisAlignedBB bb = centre.grow(PokecubeCore.getConfig().maxSpawnRadius, 5, PokecubeCore
                .getConfig().maxSpawnRadius);
        final List<Entity> otherMobs = this.watch.player.getEntityWorld().getEntitiesInAABBexcluding(this.watch.player,
                bb, input -> input instanceof AnimalEntity && CapabilityPokemob.getPokemobFor(input) != null);
        final TranslationTextComponent nearbyLine = new TranslationTextComponent("pokewatch.progress.global.nearby",
                otherMobs.size());

        final int x = this.watch.width / 2;
        final int y = this.watch.height / 2 - 5;

        final ITextComponent inspect = new TranslationTextComponent("pokewatch.progress.inspect");

        final TexButton inspectBtn = this.addButton(new TexButton(x - 50, y + 25, 100, 12, inspect, b ->
        {
            PacketPokedex.sendInspectPacket(true, Minecraft.getInstance().getLanguageManager().getCurrentLanguage()
                    .getCode());
        }).setTex(GuiPokeWatch.getWidgetTex()).setRender(new UVImgRender(0, 72, 100, 12)));

        inspectBtn.setFGColor(0x444444);

        if (player != this.watch.player) inspectBtn.visible = false;

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
