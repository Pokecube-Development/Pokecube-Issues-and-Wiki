package thut.wearables;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import thut.core.common.commands.CommandTools;
import thut.wearables.network.MouseOverPacket;
import thut.wearables.network.PacketGui;

public class CommandGui
{
    public static String PERMWEARABLESCMD = "wearables.open.other.command";
    static
    {
        PermissionAPI.registerNode(CommandGui.PERMWEARABLESCMD, DefaultPermissionLevel.OP,
                "Whether the player can open the wearables gui of others via the command.");
    }

    public static int execute(final CommandSource commandSource, final LivingEntity target) throws CommandException,
            CommandSyntaxException
    {
        final ServerPlayerEntity user = commandSource.getPlayerOrException();
        if (!PermissionAPI.hasPermission(user, CommandGui.PERMWEARABLESCMD)) throw new CommandException(
                new TranslationTextComponent("wearables.command.fail.noperms"));
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

    public static void register(final CommandDispatcher<CommandSource> commandDispatcher)
    {
        PermissionAPI.registerNode("command.wearables", DefaultPermissionLevel.OP,
                "Is the player allowed to use /wearables");

        LiteralArgumentBuilder<CommandSource> command = Commands.literal("wearables").requires(cs -> CommandTools
                .hasPerm(cs, "command.wearables")).then(Commands.argument("player", EntityArgument.player()).executes(
                        ctx -> CommandGui.execute(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"))));
        commandDispatcher.register(command);

        command = Commands.literal("wearables").requires(cs -> CommandTools.hasPerm(cs, "command.wearables")).executes(
                ctx -> CommandGui.execute(ctx.getSource(), null));
        commandDispatcher.register(command);
    }

}
