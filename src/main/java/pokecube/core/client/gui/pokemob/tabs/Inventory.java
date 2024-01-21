package pokecube.core.client.gui.pokemob.tabs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

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
import net.minecraft.world.inventory.Slot;
import pokecube.api.entity.pokemob.IHasCommands.Command;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.pokemob.ai.LogicStates;
import pokecube.api.entity.pokemob.commandhandlers.StanceHandler;
import pokecube.core.PokecubeCore;
import pokecube.core.client.gui.helper.TooltipArea;
import pokecube.core.client.gui.pokemob.GuiPokemob;
import pokecube.core.network.pokemobs.PacketCommand;
import pokecube.core.network.pokemobs.PacketPokemobGui;
import pokecube.core.utils.Resources;
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

            int col = 0xFF8B8B8B;
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
            this.fillGradient(mat, this.x, this.y, this.x + (int) (this.width * current), this.y + this.height, col1,
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
        this.icon = Resources.TAB_ICON_INVENTORY;
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
        int xOffset = 6;
        int yOffset = 43;

        // Bar width
        int w = 89;
        // Bar height
        int h = 4;
        // Bar positioning
        final int i = 6, j = 48;
        this.addRenderableWidget(
                this.bar = new HungerBar(this.width / 2 - i, this.height / 2 - j, w, h, this.menu.pokemob));

        // Button width
        w = 89;
        // Button height
        h = 10;
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

        this.guard.setFGColor(guarding ? 0xFF00FF00 : 0xFFFF0000);
        this.sit.setFGColor(sitting ? 0xFF00FF00 : 0xFFFF0000);
        this.stay.setFGColor(staying ? 0xFF00FF00 : 0xFFFF0000);

        final int k = (this.width - this.imageWidth) / 2;
        final int l = (this.height - this.imageHeight) / 2;

        this.addRenderableWidget(
                new TooltipArea(k + 63, l + 18, 16, 16, TComponent.translatable("pokemob.gui.slot.saddle"), (x, y) ->
                {
                    Slot slot = menu.slots.get(0);
                    if (slot.hasItem()) return false;
                    return PokecubeCore.getConfig().pokemobGuiTooltips;
                }, (b, pose, x, y) -> {
                    Component tooltip = b.getMessage();
                    parent.renderTooltip(pose, tooltip, x, y);
                }).noAuto());
        this.addRenderableWidget(
                new TooltipArea(k + 63, l + 36, 16, 16, TComponent.translatable("pokemob.gui.slot.held_item"), (x, y) ->
                {
                    Slot slot = menu.slots.get(1);
                    if (slot.hasItem()) return false;
                    return PokecubeCore.getConfig().pokemobGuiTooltips;
                }, (b, pose, x, y) -> {
                    Component tooltip = b.getMessage();
                    parent.renderTooltip(pose, tooltip, x, y);
                }).noAuto());
        this.addRenderableWidget(
                new TooltipArea(k + 63, l + 54, 16, 16, TComponent.translatable("pokemob.gui.slot.off_hand"), (x, y) ->
                {
                    Slot slot = menu.slots.get(2);
                    if (slot.hasItem()) return false;
                    return PokecubeCore.getConfig().pokemobGuiTooltips;
                }, (b, pose, x, y) -> {
                    Component tooltip = b.getMessage();
                    parent.renderTooltip(pose, tooltip, x, y);
                }).noAuto());

        this.addRenderableWidget(
                new TooltipArea(k + 83, l + 18, 89, 16, TComponent.translatable("pokemob.gui.slot.food_misc"), (x, y) ->
                {
                    // This is done inside here as when the tabs change, it can
                    // re-set the slots lists, thereby invalidating the previous
                    // check!
                    List<Slot> items = Lists.newArrayList();
                    Supplier<Boolean> hasAnyItem = () -> items.stream().allMatch(s -> !s.hasItem());
                    if (items.isEmpty()) for (int m = 3; m < 8; m++) items.add(menu.slots.get(m));
                    if (!hasAnyItem.get()) return false;
                    return PokecubeCore.getConfig().pokemobGuiTooltips;
                }, (b, pose, x, y) -> {
                    Component tooltip = b.getMessage();
                    parent.renderTooltip(pose, tooltip, x, y);
                }).noAuto());

        xOffset = 80;
        yOffset = 77;
        final Component comp = TComponent.literal("");
        this.name = new EditBox(parent.font, this.width / 2 - xOffset, this.height / 2 - yOffset, 69, 10, comp);
        this.name.setTextColor(0xFFFFFFFF);
        this.name.textColorUneditable = 4210752;
        if (this.menu.pokemob != null) this.name.setValue(this.menu.pokemob.getDisplayName().getString());
        this.addRenderableWidget(this.name);

        this.addRenderableWidget(new TooltipArea(name, TComponent.translatable("pokemob.gui.nickname"), (x, y) -> {
            if (this.name.isFocused()) return false;
            return PokecubeCore.getConfig().pokemobGuiTooltips;
        }, (b, pose, x, y) -> {
            Component tooltip = b.getMessage();
            parent.renderTooltip(pose, tooltip, x, y);
        }).noAuto());
    }

    @Override
    public void renderBg(PoseStack mat, float partialTicks, int mouseX, int mouseY)
    {
        super.renderBg(mat, partialTicks, mouseX, mouseY);
        final int k = (this.width - this.imageWidth) / 2;
        final int l = (this.height - this.imageHeight) / 2;
        // The 5 inventory slots
        parent.blit(mat, k + 82, l + 17, 36, this.imageHeight + 72, 90, 18);
        // The saddle slot
        parent.blit(mat, k + 62, l + 17, 18, this.imageHeight + 72, 18, 18);
        // The held item slot
        parent.blit(mat, k + 62, l + 35, 0, this.imageHeight + 72, 18, 18);
        // The off-hand slot
        parent.blit(mat, k + 62, l + 53, 0, this.imageHeight + 72, 18, 18);
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