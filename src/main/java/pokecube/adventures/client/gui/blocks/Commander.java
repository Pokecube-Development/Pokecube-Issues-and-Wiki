package pokecube.adventures.client.gui.blocks;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.commander.CommanderTile;
import pokecube.adventures.network.PacketCommander;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.pokemob.IHasCommands.Command;

public class Commander extends Screen
{
    protected EditBox command;
    protected EditBox args;
    BlockPos                  pos;
    CommanderTile             tile;
    int                       index = 0;

    public Commander(final BlockPos tilePos)
    {
        super(new TranslatableComponent("pokecube_adventures.commander.gui"));
        this.pos = tilePos;
        this.tile = (CommanderTile) PokecubeCore.proxy.getWorld().getBlockEntity(this.pos);
    }

    @Override
    public boolean keyPressed(final int key, final int unk1, final int unk2)
    {
        if (key == GLFW.GLFW_KEY_ENTER) this.sendChooseToServer();
        return super.keyPressed(key, unk1, unk2);
    }

    @Override
    public void render(final PoseStack mat, final int a, final int b, final float c)
    {
        this.renderBackground(mat);
        super.render(mat, a, b, c);
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

        this.addRenderableWidget(new Button(this.width / 2 - xOffset + 64, this.height / 2 - yOffset - 85, 20, 20,
                new TextComponent("\u25b2"), b ->
                {
                    if (this.index < names.size() - 1) this.index++;
                    else this.index = 0;
                    this.command.setValue(names.get(this.index));
                }));

        this.addRenderableWidget(new Button(this.width / 2 - xOffset + 64, this.height / 2 - yOffset - 65, 20, 20,
                new TextComponent("\u25bc"), b ->
                {
                    if (this.index > 0) this.index--;
                    else this.index = names.size() - 1;
                    this.command.setValue(names.get(this.index));
                }));

        this.command = new EditBox(this.font, this.width / 2 - 50, this.height / 4 + 74 + yOffset, 100, 10,
                new TextComponent(""));
        final String init = this.tile.getCommand() == null ? "ATTACKLOCATION" : "" + this.tile.getCommand();
        this.command.setValue(init);

        for (this.index = 0; this.index < names.size(); this.index++)
            if (init.equals(names.get(this.index))) break;

        this.args = new EditBox(this.font, this.width / 2 - 50, this.height / 4 + 94 + yOffset, 100, 10,
                new TextComponent(""));
        this.args.setValue(this.tile.args);

        this.addRenderableWidget(this.command);
        this.addRenderableWidget(this.args);
    }

    @Override
    public void removed()
    {
        this.sendChooseToServer();
        super.removed();
    }

    private void sendChooseToServer()
    {
        final CompoundTag tag = new CompoundTag();
        tag.putString("biome", this.command.getValue());
        final PacketCommander mess = new PacketCommander();
        mess.data.putInt("x", this.tile.getBlockPos().getX());
        mess.data.putInt("y", this.tile.getBlockPos().getY());
        mess.data.putInt("z", this.tile.getBlockPos().getZ());
        mess.data.putString("C", this.command.getValue());
        mess.data.putString("A", this.args.getValue());
        PokecubeAdv.packets.sendToServer(mess);
    }
}
