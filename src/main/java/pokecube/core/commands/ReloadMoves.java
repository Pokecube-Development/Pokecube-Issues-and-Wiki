package pokecube.core.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import pokecube.api.moves.MoveEntry;
import pokecube.core.PokecubeCore;
import pokecube.core.database.moves.MovesDatabases;
import pokecube.core.moves.implementations.MovesAdder;
import pokecube.core.utils.Permissions;
import thut.api.util.PermNodes;
import thut.api.util.PermNodes.DefaultPermissionLevel;

public class ReloadMoves
{

    public static void register(final LiteralArgumentBuilder<CommandSourceStack> command)
    {
        final String perm = "command.pokecube.reload_moves";
        PermNodes.registerBooleanNode(PokecubeCore.MODID, perm, DefaultPermissionLevel.OP,
                "Is the player allowed to reload pokecube moves");
        command.then(Commands.literal("reload_moves").requires(Permissions.hasPerm(perm))
                .executes((ctx) -> ReloadMoves.execute(ctx.getSource())));
    }

    public static int execute(final CommandSourceStack source) throws CommandSyntaxException
    {
        MoveEntry.reloading = true;
        MovesDatabases.preInitLoad();
        MovesAdder.setupMoveAnimations();
        MovesDatabases.postInitMoves();
        MoveEntry.reloading = false;
        return 0;
    }

}
