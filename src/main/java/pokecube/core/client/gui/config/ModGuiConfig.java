package pokecube.core.client.gui.config;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.TranslationTextComponent;

public class ModGuiConfig extends Screen
{
    public ModGuiConfig(Screen guiScreen)
    {
        super(new TranslationTextComponent("pokecube.config.screen"));
        // super(guiScreen,
        // ConfigBase.getConfigElements(PokecubeMod.core.getConfig()),
        // PokecubeMod.ID, false, false,
        // GuiConfig.getAbridgedConfigPath(PokecubeMod.core.getConfig().getConfigFile().getAbsolutePath()));
    }
}
