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

    public void doAction(final ActionContext action)
    {
        if (this.command == null || this.command.trim().isEmpty()) return;
        final String[] commands = this.command.split("``");
        final LivingEntity target = action.target;
        for (final String command : commands)
        {
            String editedCommand = command;
            editedCommand = editedCommand.replace("@p", target.getName().getString());
            editedCommand = editedCommand.replace("'x'", target.getPosX() + "");
            editedCommand = editedCommand.replace("'y'", target.getPosY() + 1 + "");
            editedCommand = editedCommand.replace("'z'", target.getPosZ() + "");
            final Commands c = target.getServer().getCommandManager();
            c.handleCommand(target.getServer().getCommandSource(), editedCommand);
        }
    }

    public String getCommand()
    {
        return this.command;
    }
}
