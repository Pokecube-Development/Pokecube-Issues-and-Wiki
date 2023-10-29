package pokecube.core.client.gui.pokemob.tabs;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.AbstractSelectionList.Entry;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.api.entity.pokemob.commandhandlers.ChangeFormHandler;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.pokemob.GuiPokemob;
import pokecube.core.network.pokemobs.PacketAIRoutine;
import pokecube.core.network.pokemobs.PacketPokemobGui;
import pokecube.core.network.pokemobs.PacketUpdateAI;
import pokecube.core.utils.Resources;
import thut.lib.TComponent;

public class AI extends Tab
{
    private static final ResourceLocation CHECK_TEX = new ResourceLocation("textures/gui/checkbox.png");

    private static record AIButton(Button button, AIRoutine routine)
    {
    }

    private static class AIEntry extends Entry<AIEntry>
    {
        final IPokemob pokemob;
        final AIButton[] buttons;
        final AI parent;
        int top;
        // This is used to prevent the 1 frame where the buttons render before
        // properly being moved.
        boolean added = false;

        public AIEntry(final AI parent, final IPokemob pokemob, AIButton... buttons)
        {
            this.buttons = buttons;
            this.pokemob = pokemob;
            this.top = buttons[0].button().y;
            this.parent = parent;
        }

        @Override
        public void render(final PoseStack mat, final int slotIndex, final int y, final int x, final int listWidth,
                final int slotHeight, final int mouseX, final int mouseY, final boolean isSelected,
                final float partialTicks)
        {
            int dx = 0;
            int texW = 10;
            boolean rendered = false;
            for (var holder : this.buttons)
            {
                var button = holder.button();
                AIRoutine routine = holder.routine();
                button.visible = false;
                button.active = false;

                if (y > this.top && y < this.top + parent.list.height)
                {
                    button.x = x + dx;
                    button.y = y;
                    button.visible = true;
                    button.active = true;

                    rendered = true;

                    dx += button.getWidth();
                    final boolean state = this.pokemob.isRoutineEnabled(routine);

                    RenderSystem.setShader(GameRenderer::getPositionTexShader);
                    RenderSystem.setShaderTexture(0, CHECK_TEX);

                    mat.pushPose();
                    float s = 10f / 80f;
                    int sx = x + dx;
                    int sy = y + 0;
                    mat.translate(sx, sy, 0);
                    mat.scale(s, s, s);
                    mat.translate(-sx, -sy, 0);
                    int tx = 0;
                    int ty = state ? 80 : 0;
                    parent.parent.blit(mat, x + dx, y, tx, ty, 80, 80);
                    mat.popPose();
                    dx += texW;
                }
            }

            // Actually add the button if it was supposed to render.
            if (rendered && !added)
            {
                added = true;
                for (var holder : this.buttons)
                {
                    parent.addRenderableWidget(holder.button());
                }
            }
        }

    }

    ScrollGui<AIEntry> list;
    EditBox megaMode;
    Button back;
    Button fwd;

    public AI(GuiPokemob parent)
    {
        super(parent, "ai");
        this.icon = Resources.TAB_ICON_AI;
    }

    @Override
    public void setEnabled(boolean active)
    {
        super.setEnabled(active);
        if (!active)
        {
            this.parent.children.remove(this.list);
            this.parent.removeWidget(back);
            this.parent.removeWidget(fwd);
            this.parent.removeWidget(megaMode);
        }
        else
        {
            this.parent.children.add(this.list);
            this.menu.setMode(PacketPokemobGui.AI);
        }
    }

    @Override
    public void init()
    {
        int xOffset = this.width / 2 - 10;
        int yOffset = this.height / 2 - 77;

        IPokemob pokemob = this.menu.pokemob;

        yOffset += 8;
        xOffset -= 17;
        this.list = new ScrollGui<>(this.parent, this.parent.minecraft, 110, 40, 10, xOffset, yOffset);

        this.list.scrollBarDx = 2;
        this.list.scrollBarDy = 4;

        this.list.smoothScroll = false;

        List<AIButton> thisRow = Lists.newArrayList();

        for (int i = 0; i < AIRoutine.values().length; i++)
        {
            AIRoutine routine = AIRoutine.values()[i];
            String name = routine.toString();
            if (!routine.isAllowed(pokemob)) continue;
            Component tooltip = TComponent.translatable("pokemob.gui.ai." + name.toLowerCase(Locale.ROOT) + ".desc");
            Component nameComp = TComponent.translatable("pokemob.gui.ai." + name.toLowerCase(Locale.ROOT));
            int size = parent.font.width(nameComp);
            if (size > 38)
            {
                name = nameComp.getString();
                FormattedText trimmed = parent.font.substrByWidth(nameComp, 38);
                MutableComponent text = TComponent.literal(trimmed.getString());
                text.setStyle(nameComp.getStyle());
                nameComp = text;
            }
            final Button button = new Button(xOffset, yOffset, 40, 10, nameComp, b -> {
                final boolean state = !pokemob.isRoutineEnabled(routine);
                pokemob.setRoutineState(routine, state);
                PacketAIRoutine.sentCommand(pokemob, routine, state);
            }, (b, pose, x, y) -> {
                parent.renderTooltip(pose, tooltip, x, y);
            });
            button.active = button.visible = false;
            AIButton toAdd = new AIButton(button, routine);
            thisRow.add(toAdd);
            if (thisRow.size() == 2)
            {
                this.list.addEntry(new AIEntry(this, pokemob, thisRow.toArray(new AIButton[2])));
                thisRow.clear();
            }
        }
        if (!thisRow.isEmpty())
        {
            this.list.addEntry(new AIEntry(this, pokemob, thisRow.toArray(new AIButton[0])));
        }
        xOffset += 12;
        yOffset += 45;
        String mode = pokemob.getEntity().getPersistentData().getString("pokecube:mega_mode");
        parent.addRenderableWidget(
                this.megaMode = new EditBox(parent.font, xOffset, yOffset, 80, 10, TComponent.literal(mode)));
        this.megaMode.setValue(mode);
        List<String> modes = new ArrayList<>();
        modes.add("");
        for (var h : ChangeFormHandler.processors) modes.add(h.changeKey());
        parent.addRenderableWidget(back = new Button(xOffset - 10, yOffset, 10, 10, TComponent.literal("<"), b -> {
            int i = modes.indexOf(megaMode.getValue());
            if (i == -1) i = 1;
            i -= 1;
            if (i < 0) i = modes.size() - 1;
            megaMode.setValue(modes.get(i));
            sendUpdate();
        }));
        parent.addRenderableWidget(fwd = new Button(xOffset + 80, yOffset, 10, 10, TComponent.literal(">"), b -> {
            int i = modes.indexOf(megaMode.getValue());
            if (i == -1) i = modes.size();
            i += 1;
            if (i > modes.size() - 1) i = 0;
            megaMode.setValue(modes.get(i));
            sendUpdate();
        }));
    }

    @Override
    public void render(PoseStack mat, int x, int y, float f)
    {
        for (AbstractWidget button : ours) button.visible = false;
        this.list.render(mat, x, y, f);
    }

    @Override
    public boolean charTyped(final char typedChar, final int keyCode)
    {
        final boolean ret = super.charTyped(typedChar, keyCode);
        if (keyCode == GLFW.GLFW_KEY_ENTER)
        {
            this.sendUpdate();
            return true;
        }
        return ret;
    }

    private void sendUpdate()
    {
        String mode = this.megaMode.getValue();
        PacketUpdateAI.sendMegaModePacket(this.menu.pokemob, mode);
        this.menu.pokemob.getEntity().getPersistentData().putString("pokecube:mega_mode", mode);
        // Send status message thingy
        this.parent.minecraft.player.displayClientMessage(TComponent.translatable("pokemob.gui.update.megamode"), true);
    }
}
