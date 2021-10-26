package thut.core.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import thut.core.common.config.Config.ConfigData;

public class ConfigGui extends Screen
{
    private final Screen parent;
    final ConfigData     data;

    public ConfigGui(final ConfigData data, final Screen parent)
    {
        super(new TranslatableComponent("thutcore.config.not_finished"));
        this.data = data;
        this.parent = parent;
    }

    @Override
    protected void init()
    {
        this.addRenderableWidget(new Button(this.width / 2 - 50, this.height / 2 + 20, 100, 20, new TranslatableComponent(
                "gui.done"), w ->
        {
            this.minecraft.setScreen(this.parent);
        }));
    }

    @Override
    public void render(final PoseStack mat, final int x, final int y, final float partialTicks)
    {
        this.renderBackground(mat);
        GuiComponent.drawCenteredString(mat, this.font, this.title.getString(), this.width / 2, this.height / 3,
                16777215);
        super.render(mat, x, y, partialTicks);
    }
}
