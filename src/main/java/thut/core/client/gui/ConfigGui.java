package thut.core.client.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TranslationTextComponent;
import thut.core.common.config.Config.ConfigData;

public class ConfigGui extends Screen
{
    private final Screen parent;
    final ConfigData     data;

    public ConfigGui(final ConfigData data, final Screen parent)
    {
        super(new TranslationTextComponent("thutcore.config.not_finished"));
        this.data = data;
        this.parent = parent;
    }

    @Override
    protected void init()
    {
        this.addButton(new Button(this.width / 2 - 50, this.height / 2 + 20, 100, 20, I18n.format("gui.done"), w ->
        {
            this.minecraft.displayGuiScreen(this.parent);
        }));
    }

    @Override
    public void render(final int x, final int y, final float partialTicks)
    {
        this.renderBackground();
        this.drawCenteredString(this.font, this.title.getFormattedText(), this.width / 2, this.height / 3, 16777215);
        super.render(x, y, partialTicks);
    }
}
