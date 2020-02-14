package pokecube.core.client.gui.helper;

public interface INotifiedEntry
{
    void preRender(final int slotIndex, final int x, final int y, final int listWidth, final int slotHeight,
            final int mouseX, final int mouseY, final boolean isSelected, final float partialTicks);
}
