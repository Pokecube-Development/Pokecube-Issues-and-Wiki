package pokecube.core.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import pokecube.core.PokecubeCore;
import pokecube.core.handlers.events.EventsHandler;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.Tools;

public class Reset
{

    public static int execute(final CommandSourceStack source, final ServerPlayer target) throws CommandSyntaxException
    {
        PokecubeSerializer.getInstance().setHasStarter(target, false);
        EventsHandler.sendInitInfo(target);
        source.sendSuccess(new TranslatableComponent("pokecube.command.reset", target.getDisplayName()), true);
        target.sendMessage(new TranslatableComponent("pokecube.command.canchoose"), Util.NIL_UUID);
        PokecubeCore.LOGGER.info("Reset Starter for {}", target.getGameProfile());
        return 0;
    }

    public static void register(final LiteralArgumentBuilder<CommandSourceStack> command)
    {
        final String perm = "command.pokecube.reset";
        PermissionAPI.registerNode(perm, DefaultPermissionLevel.OP,
                "Is the player allowed to reset the starter status of a player");
        command.then(Commands.literal("reset").requires(Tools.hasPerm(perm)).then(Commands.argument("target_player",
                EntityArgument.player()).executes((ctx) -> Reset.execute(ctx.getSource(), EntityArgument.getPlayer(ctx,
                        "target_player")))));
    }
}
