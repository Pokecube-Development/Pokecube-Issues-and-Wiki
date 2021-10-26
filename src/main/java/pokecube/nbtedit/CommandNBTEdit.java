package pokecube.nbtedit;

import org.apache.logging.log4j.Level;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import pokecube.nbtedit.packets.PacketHandler;
import thut.core.common.handlers.PlayerDataHandler;

public class CommandNBTEdit// extends CommandBase
{
    private static SuggestionProvider<CommandSource> SUGGEST_TYPES = (ctx,
            sb) -> net.minecraft.command.ISuggestionProvider.suggest(PlayerDataHandler.getDataIDs(), sb);

    private static int execute(final CommandSource source, final BlockPos pos) throws CommandSyntaxException
    {
        final ServerPlayerEntity player = source.getPlayerOrException();
        NBTEdit.log(Level.TRACE, source.getTextName() + " issued command \"/pcedit " + pos + "\"");
        PacketHandler.sendTile(player, pos);
        return 0;
    }

    private static int execute(final CommandSource source, final Entity target) throws CommandSyntaxException
    {
        final ServerPlayerEntity player = source.getPlayerOrException();
        NBTEdit.log(Level.TRACE, source.getTextName() + " issued command \"/pcedit " + target.getId() + "\"");
        PacketHandler.sendEntity(player, target.getId());
        return 0;
    }

    private static int execute(final CommandSource source, final ServerPlayerEntity target, final String value)
            throws CommandSyntaxException
    {
        final ServerPlayerEntity player = source.getPlayerOrException();
        NBTEdit.log(Level.TRACE, source.getTextName() + " issued command \"/pcedit " + target.getName()
                .getContents() + " " + value + "\"");
        PacketHandler.sendCustomTag(player, target.getId(), value);
        return 0;
    }

    // private static int execute(CommandSource source) throws
    // CommandSyntaxException
    // {
    // ServerPlayerEntity player = source.asPlayer();
    // NBTEdit.log(Level.TRACE, source.getName() + " issued command
    // \"/pcedit\"");
    // PacketHandler.INSTANCE.sendTo(new MouseOverPacket(), player);
    // return 0;
    // }

    public static void register(final CommandDispatcher<CommandSource> commandDispatcher)
    {
        final LiteralArgumentBuilder<CommandSource> command = Commands.literal("pcedit").requires(cs -> NBTEdit.proxy
                .checkPermission(cs)).then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(
                        ctx -> CommandNBTEdit.execute(ctx.getSource(), BlockPosArgument.getOrLoadBlockPos(ctx, "pos")))).then(
                                Commands.argument("target", EntityArgument.entity()).executes(ctx -> CommandNBTEdit
                                        .execute(ctx.getSource(), EntityArgument.getEntity(ctx, "target")))).then(
                                                Commands.argument("target", EntityArgument.player()).then(Commands
                                                        .argument("type", StringArgumentType.string()).suggests(
                                                                CommandNBTEdit.SUGGEST_TYPES)).executes(
                                                                        ctx -> CommandNBTEdit.execute(ctx.getSource(),
                                                                                EntityArgument.getPlayer(ctx, "target"),
                                                                                StringArgumentType.getString(ctx,
                                                                                        "type"))));
        commandDispatcher.register(command);
    }
}
