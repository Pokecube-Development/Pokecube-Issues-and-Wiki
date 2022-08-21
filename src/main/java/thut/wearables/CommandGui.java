package thut.wearables;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import thut.api.util.PermNodes;
import thut.api.util.PermNodes.DefaultPermissionLevel;
import thut.core.common.commands.CommandTools;
import thut.lib.TComponent;
import thut.wearables.network.MouseOverPacket;
import thut.wearables.network.PacketGui;

public class CommandGui
{
    public static String PERMWEARABLESCMD = "wearables.open.other.command";
    static
    {
        PermNodes.registerNode(CommandGui.PERMWEARABLESCMD, DefaultPermissionLevel.OP,
                "Whether the player can open the wearables gui of others via the command.");
    }

    public static int execute(final CommandSourceStack commandSource, final LivingEntity target) throws CommandRuntimeException,
            CommandSyntaxException
    {
        final ServerPlayer user = commandSource.getPlayerOrException();
        if (!PermNodes.getBooleanPerm(user, CommandGui.PERMWEARABLESCMD)) throw new CommandRuntimeException(
                TComponent.translatable("wearables.command.fail.noperms"));
        if (target == null) ThutWearables.packets.sendTo(new MouseOverPacket(), user);
        else
        {
            final int id = target.getId();
            final PacketGui packet = new PacketGui();
            packet.data.putInt("w_open_target_", id);
            packet.handleServer(user);
        }
        return 0;

    }

    public static void register(final CommandDispatcher<CommandSourceStack> commandDispatcher)
    {
        PermNodes.registerNode("command.wearables", DefaultPermissionLevel.OP,
                "Is the player allowed to use /wearables");

        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("wearables").requires(cs -> CommandTools
                .hasPerm(cs, "command.wearables")).then(Commands.argument("player", EntityArgument.player()).executes(
                        ctx -> CommandGui.execute(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"))));
        commandDispatcher.register(command);

        command = Commands.literal("wearables").requires(cs -> CommandTools.hasPerm(cs, "command.wearables")).executes(
                ctx -> CommandGui.execute(ctx.getSource(), null));
        commandDispatcher.register(command);
    }

}
