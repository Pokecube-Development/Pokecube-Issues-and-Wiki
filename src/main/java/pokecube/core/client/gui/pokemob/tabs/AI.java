package pokecube.core.client.gui.pokemob.tabs;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractSelectionList.Entry;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.IMoveConstants.AIRoutine;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.pokemob.GuiPokemob;
import pokecube.core.network.pokemobs.PacketAIRoutine;
import pokecube.core.network.pokemobs.PacketPokemobGui;
import thut.lib.TComponent;

public class AI extends Tab
{
    private static class AIEntry extends Entry<AIEntry>
    {
        final IPokemob pokemob;
        final Button wrapped;
        final int index;
        int top;

        public AIEntry(final Button wrapped, final int index, final IPokemob pokemob)
        {
            this.wrapped = wrapped;
            this.pokemob = pokemob;
            this.wrapped.visible = false;
            this.wrapped.active = false;
            this.top = wrapped.y;
            this.index = index;
        }

        @Override
        public void render(final PoseStack mat, final int slotIndex, final int y, final int x, final int listWidth,
                final int slotHeight, final int mouseX, final int mouseY, final boolean isSelected,
                final float partialTicks)
        {
            this.wrapped.visible = false;
            this.wrapped.active = false;

            if (y > this.top && y < this.top + 50)
            {
                final AIRoutine routine = AIRoutine.values()[this.index];
                final boolean state = this.pokemob.isRoutineEnabled(routine);
                GuiComponent.fill(mat, x + 41, y + 1, x + 80, y + 10, state ? 0xFF00FF00 : 0xFFFF0000);
                GuiComponent.fill(mat, x, y + 10, x + 40, y + 11, 0xFF000000);
                this.wrapped.x = x;
                this.wrapped.y = y;
                this.wrapped.visible = true;
                this.wrapped.active = true;
            }
            else
            {
                this.wrapped.visible = false;
                this.wrapped.active = false;
            }
        }

    }

    ScrollGui<AIEntry> list;

    public AI(GuiPokemob parent)
    {
        super(parent, "ai");
    }

    @Override
    public void setEnabled(boolean active)
    {
        super.setEnabled(active);
        if (!active)
        {
            this.parent.children.remove(this.list);
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

        yOffset += 9;
        xOffset += 0;
        this.list = new ScrollGui<>(this.parent, this.parent.minecraft, 90, 50, 10, xOffset, yOffset);

        this.list.scrollBarDx = 2;
        this.list.scrollBarDy = 4;

        this.list.smoothScroll = false;
        for (int i = 0; i < AIRoutine.values().length; i++)
        {
            String name = AIRoutine.values()[i].toString();
            if (!AIRoutine.values()[i].isAllowed(pokemob)) continue;
            if (name.length() > 6) name = name.substring(0, 6);
            final int index = i;
            final Button button = new Button(xOffset, yOffset, 40, 10, TComponent.literal(name), b -> {
                final AIRoutine routine = AIRoutine.values()[index];
                final boolean state = !pokemob.isRoutineEnabled(routine);
                pokemob.setRoutineState(routine, state);
                PacketAIRoutine.sentCommand(pokemob, routine, state);
            });
            this.addRenderableWidget(button);
            this.list.addEntry(new AIEntry(button, index, pokemob));
        }
    }

    @Override
    public void render(PoseStack mat, int x, int y, float f)
    {
        for (AbstractWidget button : ours) button.visible = false;
        this.list.render(mat, x, y, f);
    }

    @Override
    public void renderBg(PoseStack mat, float partialTicks, int mouseX, int mouseY)
    {
        super.renderBg(mat, partialTicks, mouseX, mouseY);
    }

}
