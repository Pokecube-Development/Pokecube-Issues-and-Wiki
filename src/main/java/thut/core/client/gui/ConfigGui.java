package thut.core.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
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
        this.addButton(new Button(this.width / 2 - 50, this.height / 2 + 20, 100, 20, new TranslationTextComponent(
                "gui.done"), w ->
        {
            this.minecraft.setScreen(this.parent);
        }));
    }

    @Override
    public void render(final MatrixStack mat, final int x, final int y, final float partialTicks)
    {
        this.renderBackground(mat);
        AbstractGui.drawCenteredString(mat, this.font, this.title.getString(), this.width / 2, this.height / 3,
                16777215);
        super.render(mat, x, y, partialTicks);
    }
}
