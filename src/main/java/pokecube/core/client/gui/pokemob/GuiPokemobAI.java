package pokecube.core.client.gui.pokemob;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.ExtendedList.AbstractListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.entity.pokemobs.ContainerPokemob;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.network.pokemobs.PacketAIRoutine;
import pokecube.core.network.pokemobs.PacketPokemobGui;
import pokecube.nbtedit.gui.TextFieldWidget2;

public class GuiPokemobAI extends GuiPokemobBase
{
    private static class Entry extends AbstractListEntry<Entry>
    {
        final IPokemob pokemob;
        final Button   wrapped;
        final int      index;
        int            top;

        public Entry(final Button wrapped, final int index, final IPokemob pokemob)
        {
            this.wrapped = wrapped;
            this.pokemob = pokemob;
            this.wrapped.visible = false;
            this.wrapped.active = false;
            this.top = wrapped.y;
            this.index = index;
        }

        @Override
        public void render(final MatrixStack mat, final int slotIndex, final int y, final int x, final int listWidth,
                final int slotHeight, final int mouseX, final int mouseY, final boolean isSelected,
                final float partialTicks)
        {
            this.wrapped.visible = false;
            this.wrapped.active = false;
            if (y > this.top && y < this.top + 50)
            {
                final AIRoutine routine = AIRoutine.values()[this.index];
                final boolean state = this.pokemob.isRoutineEnabled(routine);
                AbstractGui.fill(mat, x + 41, y + 1, x + 80, y + 10, state ? 0xFF00FF00 : 0xFFFF0000);
                AbstractGui.fill(mat, x, y + 10, x + 40, y + 11, 0xFF000000);
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

    final PlayerInventory playerInventory;
    final IInventory      pokeInventory;
    final IPokemob        pokemob;
    final Entity          entity;
    ScrollGui<Entry>      list;

    final List<TextFieldWidget2> textInputs = Lists.newArrayList();

    public GuiPokemobAI(final ContainerPokemob container, final PlayerInventory inventory)
    {
        super(container, inventory);
        this.pokemob = container.pokemob;
        this.playerInventory = inventory;
        this.pokeInventory = this.pokemob.getInventory();
        this.entity = this.pokemob.getEntity();
        container.setMode(PacketPokemobGui.AI);
    }

    @Override
    public void init()
    {
        super.init();
        int xOffset = this.width / 2 - 10;
        int yOffset = this.height / 2 - 77;
        this.addButton(new Button(xOffset + 60, yOffset, 30, 10, new TranslationTextComponent("pokemob.gui.inventory"),
                b -> PacketPokemobGui.sendPagePacket(PacketPokemobGui.MAIN, this.entity.getEntityId())));
        this.addButton(new Button(xOffset + 30, yOffset, 30, 10, new TranslationTextComponent("pokemob.gui.storage"),
                b -> PacketPokemobGui.sendPagePacket(PacketPokemobGui.STORAGE, this.entity.getEntityId())));
        this.addButton(new Button(xOffset + 00, yOffset, 30, 10, new TranslationTextComponent("pokemob.gui.routes"),
                b -> PacketPokemobGui.sendPagePacket(PacketPokemobGui.ROUTES, this.entity.getEntityId())));
        yOffset += 9;
        xOffset += 2;
        this.list = new ScrollGui<>(this, this.minecraft, 90, 50, 10, xOffset, yOffset);
        this.list.smoothScroll = false;
        int n = 0;
        for (int i = 0; i < AIRoutine.values().length; i++)
        {
            String name = AIRoutine.values()[i].toString();
            if (!AIRoutine.values()[i].isAllowed(this.pokemob)) continue;
            if (name.length() > 6) name = name.substring(0, 6);
            final int index = n++;
            final Button button = new Button(xOffset, yOffset, 40, 10, new StringTextComponent(name), b ->
            {
                final AIRoutine routine = AIRoutine.values()[index];
                final boolean state = !this.pokemob.isRoutineEnabled(routine);
                this.pokemob.setRoutineState(routine, state);
                PacketAIRoutine.sentCommand(this.pokemob, routine, state);
            });
            this.addButton(button);
            this.list.addEntry(new Entry(button, index, this.pokemob));
        }
        this.children.add(this.list);
    }

    @Override
    public void render(final MatrixStack mat, final int x, final int y, final float f)
    {
        super.render(mat, x, y, f);
        for (int i = 3; i < this.buttons.size(); i++)
            this.buttons.get(i).visible = false;
        this.list.render(mat, x, y, f);
        this.renderHoveredTooltip(mat, x, y);
    }
}
