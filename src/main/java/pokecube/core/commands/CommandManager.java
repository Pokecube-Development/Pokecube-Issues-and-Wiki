package pokecube.core.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import pokecube.core.utils.Tools;

public class CommandManager
{
    public static void register(final CommandDispatcher<CommandSource> commandDispatcher)
    {
        final String perm = "command.pokecube";
        PermissionAPI.registerNode(perm, DefaultPermissionLevel.ALL,
                "Is the player allowed to use the root pokecube command.");
        final LiteralArgumentBuilder<CommandSource> command = Commands.literal("pokecube").requires(Tools.hasPerm(
                perm));

        Kill.register(command);
        Count.register(command);
        Restore.register(command);
        Reset.register(command);
        Reload.register(command);

        commandDispatcher.register(command);
        TM.register(commandDispatcher);
        SecretBase.register(commandDispatcher);
        Pokemake.register(commandDispatcher);
        Meteor.register(commandDispatcher);
        Pokerecall.register(commandDispatcher);
        Pokeegg.register(commandDispatcher);
    }
}
