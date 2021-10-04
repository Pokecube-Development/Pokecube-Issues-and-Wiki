package pokecube.core.client.gui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class GuiArranger extends Screen
{

    public static boolean toggle = false;
    boolean[]             held   = new boolean[4];

    protected GuiArranger(final Component titleIn)
    {
        super(titleIn);
        // TODO arranger gui.
    }
}
