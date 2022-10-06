package pokecube.core.client.gui.helper;

import java.util.function.Consumer;

import net.minecraft.client.gui.components.AbstractWidget;

public interface INotifiedEntry
{
    void preRender(final int slotIndex, final int x, final int y, final int listWidth, final int slotHeight,
            final int mouseX, final int mouseY, final boolean isSelected, final float partialTicks);

    default void addOrRemove(Consumer<AbstractWidget> remover)
    {

    }
}
