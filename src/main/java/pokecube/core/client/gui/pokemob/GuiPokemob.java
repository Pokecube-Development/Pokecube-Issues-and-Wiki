package pokecube.core.client.gui.pokemob;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import pokecube.core.PokecubeCore;
import pokecube.core.entity.pokemobs.ContainerPokemob;
import pokecube.core.interfaces.pokemob.IHasCommands.Command;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.interfaces.pokemob.commandhandlers.StanceHandler;
import pokecube.core.network.pokemobs.PacketCommand;
import pokecube.core.network.pokemobs.PacketPokemobGui;
import thut.api.entity.IHungrymob;

public class GuiPokemob extends GuiPokemobBase
{
    public static class HungerBar extends Widget
    {
        public IHungrymob mob;
        public float      value = 0;

        public HungerBar(final int xIn, final int yIn, final int widthIn, final int heightIn, final IHungrymob mob)
        {
            super(xIn, yIn, widthIn, heightIn, "pokemob.gui.hungerbar");
            this.mob = mob;
        }

        @Override
        public void playDownSound(final SoundHandler p_playDownSound_1_)
        {
        }

        @Override
        public void render(final int mx, final int my, final float tick)
        {
            super.render(mx, my, tick);
        }

        @Override
        public void renderButton(final int mx, final int my, final float tick)
        {
            // Render the hunger bar for the pokemob.
            // Get the hunger values.
            final float full = PokecubeCore.getConfig().pokemobLifeSpan / 4 + PokecubeCore.getConfig().pokemobLifeSpan;
            float current = -(this.mob.getHungerTime() - PokecubeCore.getConfig().pokemobLifeSpan);
            // Convert to a scale
            final float scale = 100f / full;
            current *= scale / 100f;
            current = Math.min(1, current);
            this.value = (int) (1000 * (1 - current)) / 10f;

            int col = 0xFF555555;
            // Fill the background
            AbstractGui.fill(this.x, this.y, this.x + this.width, this.y + this.height, col);
            col = 0xFFFFFFFF;
            final int col1 = 0xFF00FF77;
            // Fill the bar
            this.fillGradient(this.x, this.y, this.x + (int) (this.width * current), this.y + this.height, col, col1);
        }

    }

    Button sit;
    Button stay;
    Button guard;

    HungerBar bar;

    public GuiPokemob(final ContainerPokemob container, final PlayerInventory inv)
    {
        super(container, inv);
    }

    @Override
    public void init()
    {
        super.init();
        int xOffset = 8;
        int yOffset = 43;
        // Button width
        int w = 89;
        // Button height
        int h = 10;

        this.addButton(this.sit = new Button(this.width / 2 - xOffset, this.height / 2 - yOffset + 00, w, h, I18n
                .format("pokemob.gui.sit"), c -> PacketCommand.sendCommand(this.container.pokemob, Command.STANCE,
                        new StanceHandler(!this.container.pokemob.getLogicState(LogicStates.SITTING),
                                StanceHandler.BUTTONTOGGLESIT))));
        this.addButton(this.stay = new Button(this.width / 2 - xOffset, this.height / 2 - yOffset + 10, w, h, I18n
                .format("pokemob.gui.stay"), c -> PacketCommand.sendCommand(this.container.pokemob, Command.STANCE,
                        new StanceHandler(!this.container.pokemob.getGeneralState(GeneralStates.STAYING),
                                StanceHandler.BUTTONTOGGLESTAY))));
        this.addButton(this.guard = new Button(this.width / 2 - xOffset, this.height / 2 - yOffset + 20, w, h, I18n
                .format("pokemob.gui.guard"), c -> PacketCommand.sendCommand(this.container.pokemob, Command.STANCE,
                        new StanceHandler(!this.container.pokemob.getCombatState(CombatStates.GUARDING),
                                StanceHandler.BUTTONTOGGLEGUARD))));
        // Bar width
        w = 89;
        // Bar height
        h = 5;
        // Bar positioning
        final int i = 9, j = 48;
        this.addButton(this.bar = new HungerBar(this.width / 2 - i, this.height / 2 - j, w, h, this.container.pokemob));

        xOffset = 10;
        yOffset = 77;
        w = 30;
        h = 10;
        this.addButton(new Button(this.width / 2 - xOffset + 60, this.height / 2 - yOffset, w, h, I18n.format(
                "pokemob.gui.ai"), c -> PacketPokemobGui.sendPagePacket(PacketPokemobGui.AI, this.container.pokemob
                        .getEntity().getEntityId())));
        this.addButton(new Button(this.width / 2 - xOffset + 30, this.height / 2 - yOffset, w, h, I18n.format(
                "pokemob.gui.storage"), c -> PacketPokemobGui.sendPagePacket(PacketPokemobGui.STORAGE,
                        this.container.pokemob.getEntity().getEntityId())));
        this.addButton(new Button(this.width / 2 - xOffset + 00, this.height / 2 - yOffset, w, h, I18n.format(
                "pokemob.gui.routes"), c -> PacketPokemobGui.sendPagePacket(PacketPokemobGui.ROUTES,
                        this.container.pokemob.getEntity().getEntityId())));
    }

    /** Draws the screen and all the components in it. */
    @Override
    public void render(final int x, final int y, final float z)
    {
        super.renderBackground();
        super.render(x, y, z);
        final List<String> text = Lists.newArrayList();
        if (this.container.pokemob == null) return;

        final boolean guarding = this.container.pokemob.getCombatState(CombatStates.GUARDING);
        final boolean sitting = this.container.pokemob.getLogicState(LogicStates.SITTING);
        final boolean staying = this.container.pokemob.getGeneralState(GeneralStates.STAYING);

        this.guard.setFGColor(guarding ? 0xFF00FF00 : 0xFFFF0000);
        this.sit.setFGColor(sitting ? 0xFF00FF00 : 0xFFFF0000);
        this.stay.setFGColor(staying ? 0xFF00FF00 : 0xFFFF0000);

        if (this.guard.isMouseOver(x, y)) if (guarding) text.add(I18n.format("pokemob.stance.guard"));
        else text.add(I18n.format("pokemob.stance.no_guard"));
        if (this.stay.isMouseOver(x, y)) if (staying) text.add(I18n.format("pokemob.stance.stay"));
        else text.add(I18n.format("pokemob.stance.follow"));
        if (this.sit.isMouseOver(x, y)) if (sitting) text.add(I18n.format("pokemob.stance.sit"));
        else text.add(I18n.format("pokemob.stance.no_sit"));
        if (this.bar.isMouseOver(x, y)) text.add(I18n.format("pokemob.bar.value", this.bar.value));
        if (!text.isEmpty()) this.renderTooltip(text, x, y);
        this.renderHoveredToolTip(x, y);
    }
}
