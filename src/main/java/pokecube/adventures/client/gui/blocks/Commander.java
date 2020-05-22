package pokecube.adventures.client.gui.blocks;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.commander.CommanderTile;
import pokecube.adventures.network.PacketCommander;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.pokemob.IHasCommands.Command;

public class Commander extends Screen
{
    protected TextFieldWidget command;
    protected TextFieldWidget args;
    BlockPos                  pos;
    CommanderTile             tile;
    int                       index = 0;

    public Commander(final BlockPos tilePos)
    {
        super(new TranslationTextComponent("pokecube_adventures.commander.gui"));
        this.pos = tilePos;
        this.tile = (CommanderTile) PokecubeCore.proxy.getWorld().getTileEntity(this.pos);
    }

    @Override
    public boolean keyPressed(final int key, final int unk1, final int unk2)
    {
        if (key == GLFW.GLFW_KEY_ENTER) this.sendChooseToServer();
        return super.keyPressed(key, unk1, unk2);
    }

    @Override
    public void render(final int a, final int b, final float c)
    {
        this.renderBackground();
        super.render(a, b, c);
    }

    @Override
    protected void init()
    {
        super.init();
        final int xOffset = 0;
        final int yOffset = -11;

        final List<Command> types = Lists.newArrayList(Command.values());
        final List<String> names = Lists.newArrayList();
        for (final Command command : types)
            names.add(command.name());
        names.add("");

        this.addButton(new Button(this.width / 2 - xOffset + 64, this.height / 2 - yOffset - 85, 20, 20, "\u25b2", b ->
        {
            if (this.index < names.size() - 1) this.index++;
            else this.index = 0;
            this.command.setText(names.get(this.index));
        }));

        this.addButton(new Button(this.width / 2 - xOffset + 64, this.height / 2 - yOffset - 65, 20, 20, "\u25bc", b ->
        {
            if (this.index > 0) this.index--;
            else this.index = names.size() - 1;
            this.command.setText(names.get(this.index));
        }));

        this.command = new TextFieldWidget(this.font, this.width / 2 - 50, this.height / 4 + 20 + yOffset, 100, 10, "");
        this.command.setText(this.tile.getCommand() == null ? "" : "" + this.tile.getCommand());

        for (this.index = 0; this.index < names.size(); this.index++)
            if (this.command.getText().equals(names.get(this.index))) break;

        this.args = new TextFieldWidget(this.font, this.width / 2 - 50, this.height / 4 + 40 + yOffset, 100, 10, "");
        this.args.setText(this.tile.args);

        this.addButton(this.command);
        this.addButton(this.args);
    }

    @Override
    public void onClose()
    {
        this.sendChooseToServer();
        super.onClose();
    }

    private void sendChooseToServer()
    {
        final CompoundNBT tag = new CompoundNBT();
        tag.putString("biome", this.command.getText());
        final PacketCommander mess = new PacketCommander();
        mess.data.putInt("x", this.tile.getPos().getX());
        mess.data.putInt("y", this.tile.getPos().getY());
        mess.data.putInt("z", this.tile.getPos().getZ());
        mess.data.putString("C", this.command.getText());
        mess.data.putString("A", this.args.getText());
        PokecubeAdv.packets.sendToServer(mess);
    }
}
