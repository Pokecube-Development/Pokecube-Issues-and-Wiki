package pokecube.core.client.gui.config;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.fml.client.IModGuiFactory;

public class ModGuiFactory implements IModGuiFactory
{

    @Override
    public Screen createConfigGui(Screen parentScreen)
    {
        return new ModGuiConfig(parentScreen);
    }

    @Override
    public boolean hasConfigGui()
    {
        return true;
    }

    @Override
    public void initialize(Minecraft minecraftInstance)
    {

    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories()
    {
        return null;
    }
}
