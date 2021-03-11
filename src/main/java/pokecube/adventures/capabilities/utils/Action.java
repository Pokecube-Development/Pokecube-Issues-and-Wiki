package pokecube.adventures.capabilities.utils;

import net.minecraft.command.Commands;
import net.minecraft.entity.LivingEntity;

public class Action
{
    final String command;

    public Action(final String command)
    {
        this.command = command;
    }

    public boolean doAction(final ActionContext action)
    {
        if (this.command == null || this.command.trim().isEmpty()) return false;
        final String[] commands = this.command.split("``");
        final LivingEntity target = action.target;
        for (final String command : commands)
        {
            String editedCommand = command;
            editedCommand = editedCommand.replace("@p", target.getName().getString());
            editedCommand = editedCommand.replace("'x'", target.getX() + "");
            editedCommand = editedCommand.replace("'y'", target.getY() + 1 + "");
            editedCommand = editedCommand.replace("'z'", target.getZ() + "");
            final Commands c = target.getServer().getCommands();
            c.performCommand(target.getServer().createCommandSourceStack(), editedCommand);
        }
        return commands.length > 0;
    }

    public String getCommand()
    {
        return this.command;
    }
}
