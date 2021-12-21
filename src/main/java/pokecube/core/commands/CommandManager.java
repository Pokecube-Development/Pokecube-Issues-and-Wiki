package pokecube.core.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import pokecube.core.utils.PermNodes;
import pokecube.core.utils.PermNodes.DefaultPermissionLevel;
import pokecube.core.utils.Tools;

public class CommandManager
{
    public static void register(final CommandDispatcher<CommandSourceStack> commandDispatcher)
    {
        final String perm = "command.pokecube";
        PermNodes.registerNode(perm, DefaultPermissionLevel.ALL,
                "Is the player allowed to use the root pokecube command.");
        final LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("pokecube").requires(Tools.hasPerm(
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
