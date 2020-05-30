package pokecube.core.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import pokecube.core.handlers.events.EventsHandler;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.Tools;

public class Reset
{

    public static int execute(final CommandSource source, final ServerPlayerEntity target) throws CommandSyntaxException
    {
        PokecubeSerializer.getInstance().setHasStarter(target, false);
        EventsHandler.sendInitInfo(target);
        source.sendFeedback(new TranslationTextComponent("pokecube.command.reset", target.getDisplayName()), true);
        target.sendMessage(new TranslationTextComponent("pokecube.command.canchoose"));
        return 0;
    }

    public static void register(final LiteralArgumentBuilder<CommandSource> command)
    {
        final String perm = "command.pokecube.reset";
        PermissionAPI.registerNode(perm, DefaultPermissionLevel.OP,
                "Is the player allowed to reset the starter status of a player");
        command.then(Commands.literal("reset").requires(Tools.hasPerm(perm)).then(Commands.argument("target_player",
                EntityArgument.player()).executes((ctx) -> Reset.execute(ctx.getSource(), EntityArgument.getPlayer(ctx,
                        "target_player")))));
    }
}
