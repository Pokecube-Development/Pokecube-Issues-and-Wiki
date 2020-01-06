package pokecube.adventures.capabilities.utils;

import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;

public class Action
{
    final String command;

    public Action(final String command)
    {
        this.command = command;
    }

    public void doAction(final PlayerEntity target)
    {
        if (this.command == null || this.command.trim().isEmpty()) return;
        final String[] commands = this.command.split("``");
        for (final String command : commands)
        {
            String editedCommand = command;
            editedCommand = editedCommand.replace("@p", target.getGameProfile().getName());
            editedCommand = editedCommand.replace("'x'", target.posX + "");
            editedCommand = editedCommand.replace("'y'", target.posY + 1 + "");
            editedCommand = editedCommand.replace("'z'", target.posZ + "");
            final Commands c = target.getServer().getCommandManager();
            c.handleCommand(target.getServer().getCommandSource(), editedCommand);
        }
    }

    public String getCommand()
    {
        return this.command;
    }
}
