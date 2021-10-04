package pokecube.nbtedit;

import org.apache.logging.log4j.Level;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import pokecube.nbtedit.packets.PacketHandler;
import thut.core.common.handlers.PlayerDataHandler;

public class CommandNBTEdit// extends CommandBase
{
    private static SuggestionProvider<CommandSourceStack> SUGGEST_TYPES = (ctx,
            sb) -> net.minecraft.commands.SharedSuggestionProvider.suggest(PlayerDataHandler.getDataIDs(), sb);

    private static int execute(final CommandSourceStack source, final BlockPos pos) throws CommandSyntaxException
    {
        final ServerPlayer player = source.getPlayerOrException();
        NBTEdit.log(Level.TRACE, source.getTextName() + " issued command \"/pcedit " + pos + "\"");
        PacketHandler.sendTile(player, pos);
        return 0;
    }

    private static int execute(final CommandSourceStack source, final Entity target) throws CommandSyntaxException
    {
        final ServerPlayer player = source.getPlayerOrException();
        NBTEdit.log(Level.TRACE, source.getTextName() + " issued command \"/pcedit " + target.getId() + "\"");
        PacketHandler.sendEntity(player, target.getId());
        return 0;
    }

    private static int execute(final CommandSourceStack source, final ServerPlayer target, final String value)
            throws CommandSyntaxException
    {
        final ServerPlayer player = source.getPlayerOrException();
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

    public static void register(final CommandDispatcher<CommandSourceStack> commandDispatcher)
    {
        final LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("pcedit").requires(cs -> NBTEdit.proxy
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
