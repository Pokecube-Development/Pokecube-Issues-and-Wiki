package pokecube.core.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import pokecube.core.PokecubeCore;
import pokecube.core.utils.Permissions;
import thut.api.util.PermNodes;
import thut.api.util.PermNodes.DefaultPermissionLevel;

public class CommandManager
{
    public static void register(final CommandDispatcher<CommandSourceStack> commandDispatcher)
    {
        final String perm = "command.pokecube";
        PermNodes.registerBooleanNode(PokecubeCore.MODID, perm, DefaultPermissionLevel.ALL,
                "Is the player allowed to use the root pokecube command.");
        final LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("pokecube")
                .requires(Permissions.hasPerm(perm));

        Kill.register(command);
        Count.register(command);
        Restore.register(command);
        Reset.register(command);
        Reload.register(command);
        ReloadMoves.register(command);

        commandDispatcher.register(command);
        TM.register(commandDispatcher);
        SecretBase.register(commandDispatcher);
        Pokemake.register(commandDispatcher);
        Pokemake2.register(commandDispatcher);
        Meteor.register(commandDispatcher);
        Pokerecall.register(commandDispatcher);
        Pokeegg.register(commandDispatcher);
    }
}
