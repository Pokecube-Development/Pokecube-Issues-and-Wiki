package pokecube.core.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import thut.core.common.commands.CommandTools;

public class CommandManager
{
    public static void register(final CommandDispatcher<CommandSource> commandDispatcher)
    {
        final String perm = "command.pokecube";
        PermissionAPI.registerNode(perm, DefaultPermissionLevel.ALL,
                "Is the player allowed to use the root pokecube command.");
        final LiteralArgumentBuilder<CommandSource> command = Commands.literal("pokecube").requires(cs -> CommandTools
                .hasPerm(cs, perm));

        KillCommand.register(command);
        CountCommand.register(command);

        commandDispatcher.register(command);
    }

}
