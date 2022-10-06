package pokecube.core.client.gui.pokemob.tabs;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import pokecube.api.entity.pokemob.IHasCommands.Command;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.pokemob.ai.LogicStates;
import pokecube.api.entity.pokemob.commandhandlers.StanceHandler;
import pokecube.core.PokecubeCore;
import pokecube.core.client.gui.pokemob.GuiPokemob;
import pokecube.core.network.pokemobs.PacketCommand;
import pokecube.core.network.pokemobs.PacketPokemobGui;
import thut.api.entity.IHungrymob;
import thut.lib.TComponent;

public class Inventory extends Tab
{

    public static class HungerBar extends AbstractWidget
    {
        public IHungrymob mob;
        public float value = 0;

        public HungerBar(final int xIn, final int yIn, final int widthIn, final int heightIn, final IHungrymob mob)
        {
            super(xIn, yIn, widthIn, heightIn, TComponent.translatable("pokemob.gui.hungerbar"));
            this.mob = mob;
        }

        @Override
        public void playDownSound(final SoundManager p_playDownSound_1_)
        {}

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
            this.fillGradient(mat, this.x, this.y, this.x + (int) (this.width * current), this.y + this.height, col,
                    col1);
        }

        @Override
        public void updateNarration(final NarrationElementOutput narate)
        {
            // TODO Auto-generated method stub

        }

    }

    Button sit;
    Button stay;
    Button guard;

    HungerBar bar;

    boolean guarding;
    boolean sitting;
    boolean staying;

    protected EditBox name = new EditBox(null, 1 / 2, 1 / 2, 120, 10, TComponent.literal(""));

    public Inventory(GuiPokemob parent)
    {
        super(parent, "inventory");
    }

    @Override
    public void setEnabled(boolean active)
    {
        super.setEnabled(active);
        if (active)
        {
            this.menu.setMode(PacketPokemobGui.MAIN);
        }
    }

    @Override
    public void init()
    {
        int xOffset = 8;
        int yOffset = 43;
        // Button width
        int w = 89;
        // Button height
        int h = 10;

        this.addRenderableWidget(this.sit = new Button(this.width / 2 - xOffset, this.height / 2 - yOffset + 00, w, h,
                TComponent.translatable("pokemob.gui.sit"),
                c -> PacketCommand.sendCommand(this.menu.pokemob, Command.STANCE,
                        new StanceHandler(!this.menu.pokemob.getLogicState(LogicStates.SITTING), StanceHandler.SIT)),
                (b, pose, x, y) ->
                {
                    Component tooltip = sitting ? TComponent.translatable("pokemob.stance.sit")
                            : TComponent.translatable("pokemob.stance.no_sit");
                    parent.renderTooltip(pose, tooltip, x, y);
                }));
        this.addRenderableWidget(this.stay = new Button(this.width / 2 - xOffset, this.height / 2 - yOffset + 10, w, h,
                TComponent.translatable("pokemob.gui.stay"),
                c -> PacketCommand.sendCommand(this.menu.pokemob, Command.STANCE, new StanceHandler(
                        !this.menu.pokemob.getGeneralState(GeneralStates.STAYING), StanceHandler.STAY)),
                (b, pose, x, y) ->
                {
                    Component tooltip = staying ? TComponent.translatable("pokemob.stance.stay")
                            : TComponent.translatable("pokemob.stance.follow");
                    parent.renderTooltip(pose, tooltip, x, y);
                }));
        this.addRenderableWidget(this.guard = new Button(this.width / 2 - xOffset, this.height / 2 - yOffset + 20, w, h,
                TComponent.translatable("pokemob.gui.guard"),
                c -> PacketCommand.sendCommand(this.menu.pokemob, Command.STANCE, new StanceHandler(
                        !this.menu.pokemob.getCombatState(CombatStates.GUARDING), StanceHandler.GUARD)),
                (b, pose, x, y) ->
                {
                    Component tooltip = guarding ? TComponent.translatable("pokemob.stance.guard")
                            : TComponent.translatable("pokemob.stance.no_guard");
                    parent.renderTooltip(pose, tooltip, x, y);
                }));
        // Bar width
        w = 89;
        // Bar height
        h = 5;
        // Bar positioning
        final int i = 9, j = 48;
        this.addRenderableWidget(
                this.bar = new HungerBar(this.width / 2 - i, this.height / 2 - j, w, h, this.menu.pokemob));

        xOffset = 80;
        yOffset = 77;
        final Component comp = TComponent.literal("");
        this.name = new EditBox(parent.font, this.width / 2 - xOffset, this.height / 2 - yOffset, 69, 10, comp);
        this.name.setTextColor(0xFFFFFFFF);
        this.name.textColorUneditable = 4210752;
        if (this.menu.pokemob != null) this.name.setValue(this.menu.pokemob.getDisplayName().getString());
        this.addRenderableWidget(this.name);
    }

    @Override
    public void renderBg(PoseStack mat, float partialTicks, int mouseX, int mouseY)
    {
        super.renderBg(mat, partialTicks, mouseX, mouseY);
        final int k = (this.width - this.imageWidth) / 2;
        final int l = (this.height - this.imageHeight) / 2;
        // The 5 inventory slots
        parent.blit(mat, k + 79, l + 17, 0, this.imageHeight, 90, 18);
        // The held item slot
        parent.blit(mat, k + 7, l + 35, 0, this.imageHeight + 54, 18, 18);
        // The saddle slot
        parent.blit(mat, k + 7, l + 17, 18, this.imageHeight + 54, 18, 18);
    }

    @Override
    public void renderLabels(PoseStack mat, int mouseX, int mouseY)
    {}

    @Override
    public void render(PoseStack mat, int x, int y, float z)
    {
        final List<String> text = Lists.newArrayList();
        if (this.menu.pokemob == null) return;

        guarding = this.menu.pokemob.getCombatState(CombatStates.GUARDING);
        sitting = this.menu.pokemob.getLogicState(LogicStates.SITTING);
        staying = this.menu.pokemob.getGeneralState(GeneralStates.STAYING);

        this.guard.setFGColor(guarding ? 0xFF00FF00 : 0xFFFF0000);
        this.sit.setFGColor(sitting ? 0xFF00FF00 : 0xFFFF0000);
        this.stay.setFGColor(staying ? 0xFF00FF00 : 0xFFFF0000);

        if (this.bar.isMouseOver(x, y)) text.add(I18n.get("pokemob.bar.value", this.bar.value));
        final List<Component> msgs = new ArrayList<>();
        for (final String s : text) msgs.add(TComponent.literal(s));
        if (!text.isEmpty()) this.parent.renderComponentTooltip(mat, msgs, x, y, this.parent.font);
    }

    @Override
    public boolean keyPressed(int code, int unk1, int unk2)
    {
        if (this.name.isFocused())
        {
            if (code == GLFW.GLFW_KEY_ESCAPE) this.name.setFocused(false);
            else if (code == GLFW.GLFW_KEY_ENTER)
            {
                String var = this.name.getValue();
                if (var.length() > 20)
                {
                    var = var.substring(0, 20);
                    this.name.setValue(var);
                }
                this.menu.pokemob.setPokemonNickname(var);
                return true;
            }
            else if (code != GLFW.GLFW_KEY_BACKSPACE) return true;
        }
        return false;
    }

}