package pokecube.core.client.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;

public class GuiArranger extends Screen
{

    public static boolean toggle = false;
    boolean[]             held   = new boolean[4];

    protected GuiArranger(final ITextComponent titleIn)
    {
        super(titleIn);
        // TODO arranger gui.
    }
}
