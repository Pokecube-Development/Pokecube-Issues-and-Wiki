package pokecube.core.client.gui.pokemob;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
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
    public static class HungerBar extends AbstractWidget
    {
        public IHungrymob mob;
        public float      value = 0;

        public HungerBar(final int xIn, final int yIn, final int widthIn, final int heightIn, final IHungrymob mob)
        {
            super(xIn, yIn, widthIn, heightIn, new TranslatableComponent("pokemob.gui.hungerbar"));
            this.mob = mob;
        }

        @Override
        public void playDownSound(final SoundManager p_playDownSound_1_)
        {
        }

        @Override
        public void renderButton(final PoseStack mat, final int mx, final int my, final float tick)
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
            this.value = Math.max(0, this.value);

            int col = 0xFF555555;
            // Fill the background
            GuiComponent.fill(mat, this.x, this.y, this.x + this.width, this.y + this.height, col);
            col = 0xFFFFFFFF;
            int col1 = 0xFF000000;
            int greenness = (int) (2 * (current - 0.35) * 0xFF);
            int redness = (int) ((1 - current) * 2 * 0xFF);
            redness = Math.min(redness, 0xFF);
            greenness = Math.min(greenness, 0xFF);
            greenness = Math.max(0, greenness);
            col1 |= redness << 16 | greenness << 8;
            // Fill the bar
            this.fillGradient(mat, this.x, this.y, this.x + (int) (this.width * current), this.y + this.height, col, col1);
        }

        @Override
        public void updateNarration(final NarrationElementOutput p_169152_)
        {
            // TODO Auto-generated method stub

        }

    }

    Button sit;
    Button stay;
    Button guard;

    HungerBar bar;

    public GuiPokemob(final ContainerPokemob container, final Inventory inv)
    {
        super(container, inv);
        container.setMode(PacketPokemobGui.MAIN);
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

        this.addRenderableWidget(this.sit = new Button(this.width / 2 - xOffset, this.height / 2 - yOffset + 00, w, h,
                new TranslatableComponent("pokemob.gui.sit"), c -> PacketCommand.sendCommand(this.menu.pokemob,
                        Command.STANCE, new StanceHandler(!this.menu.pokemob.getLogicState(LogicStates.SITTING),
                                StanceHandler.SIT))));
        this.addRenderableWidget(this.stay = new Button(this.width / 2 - xOffset, this.height / 2 - yOffset + 10, w, h,
                new TranslatableComponent("pokemob.gui.stay"), c -> PacketCommand.sendCommand(this.menu.pokemob,
                        Command.STANCE, new StanceHandler(!this.menu.pokemob.getGeneralState(
                                GeneralStates.STAYING), StanceHandler.STAY))));
        this.addRenderableWidget(this.guard = new Button(this.width / 2 - xOffset, this.height / 2 - yOffset + 20, w, h,
                new TranslatableComponent("pokemob.gui.guard"), c -> PacketCommand.sendCommand(
                        this.menu.pokemob, Command.STANCE, new StanceHandler(!this.menu.pokemob
                                .getCombatState(CombatStates.GUARDING), StanceHandler.GUARD))));
        // Bar width
        w = 89;
        // Bar height
        h = 5;
        // Bar positioning
        final int i = 9, j = 48;
        this.addRenderableWidget(this.bar = new HungerBar(this.width / 2 - i, this.height / 2 - j, w, h, this.menu.pokemob));

        xOffset = 10;
        yOffset = 77;
        w = 30;
        h = 10;
        this.addRenderableWidget(new Button(this.width / 2 - xOffset + 60, this.height / 2 - yOffset, w, h,
                new TranslatableComponent("pokemob.gui.ai"), c -> PacketPokemobGui.sendPagePacket(
                        PacketPokemobGui.AI, this.menu.pokemob.getEntity().getId())));
        this.addRenderableWidget(new Button(this.width / 2 - xOffset + 30, this.height / 2 - yOffset, w, h,
                new TranslatableComponent("pokemob.gui.storage"), c -> PacketPokemobGui.sendPagePacket(
                        PacketPokemobGui.STORAGE, this.menu.pokemob.getEntity().getId())));
        this.addRenderableWidget(new Button(this.width / 2 - xOffset + 00, this.height / 2 - yOffset, w, h,
                new TranslatableComponent("pokemob.gui.routes"), c -> PacketPokemobGui.sendPagePacket(
                        PacketPokemobGui.ROUTES, this.menu.pokemob.getEntity().getId())));
    }

    /** Draws the screen and all the components in it. */
    @Override
    public void render(final PoseStack mat, final int x, final int y, final float z)
    {
        super.render(mat, x, y, z);
        final List<String> text = Lists.newArrayList();
        if (this.menu.pokemob == null) return;

        final boolean guarding = this.menu.pokemob.getCombatState(CombatStates.GUARDING);
        final boolean sitting = this.menu.pokemob.getLogicState(LogicStates.SITTING);
        final boolean staying = this.menu.pokemob.getGeneralState(GeneralStates.STAYING);

        this.guard.setFGColor(guarding ? 0xFF00FF00 : 0xFFFF0000);
        this.sit.setFGColor(sitting ? 0xFF00FF00 : 0xFFFF0000);
        this.stay.setFGColor(staying ? 0xFF00FF00 : 0xFFFF0000);

        if (this.guard.isMouseOver(x, y)) if (guarding) text.add(I18n.get("pokemob.stance.guard"));
        else text.add(I18n.get("pokemob.stance.no_guard"));
        if (this.stay.isMouseOver(x, y)) if (staying) text.add(I18n.get("pokemob.stance.stay"));
        else text.add(I18n.get("pokemob.stance.follow"));
        if (this.sit.isMouseOver(x, y)) if (sitting) text.add(I18n.get("pokemob.stance.sit"));
        else text.add(I18n.get("pokemob.stance.no_sit"));
        if (this.bar.isMouseOver(x, y)) text.add(I18n.get("pokemob.bar.value", this.bar.value));
        final List<Component> msgs = new ArrayList<>();
        for(final String s: text) msgs.add(new TextComponent(s));
        if (!text.isEmpty()) this.renderComponentToolTip(mat, msgs, x, y, this.font);
        this.renderTooltip(mat, x, y);
    }
}
