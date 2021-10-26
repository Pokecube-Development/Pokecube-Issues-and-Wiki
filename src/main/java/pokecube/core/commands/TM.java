package pokecube.core.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import pokecube.core.items.ItemTM;
import pokecube.core.moves.MovesUtils;
import thut.core.common.commands.CommandTools;

public class TM
{
    private static SuggestionProvider<CommandSourceStack> SUGGEST_TMS = (ctx,
            sb) -> net.minecraft.commands.SharedSuggestionProvider.suggest(MovesUtils.getKnownMoveNames(), sb);

    public static int execute(final CommandSourceStack source, final ServerPlayer serverplayerentity, final String tm)
    {
        final ItemStack itemstack = ItemTM.getTM(tm);
        final boolean flag = serverplayerentity.getInventory().add(itemstack);
        if (flag && itemstack.isEmpty())
        {
            itemstack.setCount(1);
            final ItemEntity itementity1 = serverplayerentity.drop(itemstack, false);
            if (itementity1 != null) itementity1.makeFakeItem();
            serverplayerentity.level.playSound((Player) null, serverplayerentity.getX(), serverplayerentity.getY(),
                    serverplayerentity.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F,
                    ((serverplayerentity.getRandom().nextFloat() - serverplayerentity.getRandom().nextFloat()) * 0.7F + 1.0F)
                            * 2.0F);
            serverplayerentity.inventoryMenu.broadcastChanges();
        }
        else
        {
            final ItemEntity itementity = serverplayerentity.drop(itemstack, false);
            if (itementity != null)
            {
                itementity.setNoPickUpDelay();
                itementity.setOwner(serverplayerentity.getUUID());
            }
        }
        return 0;
    }

    public static int execute(final CommandSourceStack source, final String tm) throws CommandSyntaxException
    {
        final ServerPlayer player = source.getPlayerOrException();
        return TM.execute(source, player, tm);
    }

    public static void register(final CommandDispatcher<CommandSourceStack> commandDispatcher)
    {
        final String perm = "command.poketm";
        PermissionAPI.registerNode(perm, DefaultPermissionLevel.OP, "Is the player allowed to use /poketm");

        // Setup with name and permission
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("poketm").requires(cs -> CommandTools.hasPerm(
                cs, perm));
        // No target argument version
        command = command.then(Commands.argument("tm", StringArgumentType.string()).suggests(TM.SUGGEST_TMS).executes(
                ctx -> TM.execute(ctx.getSource(), StringArgumentType.getString(ctx, "tm"))));
        // Target argument version
        command = command.then(Commands.argument("tm", StringArgumentType.string()).suggests(TM.SUGGEST_TMS).then(
                Commands.argument("player", EntityArgument.player()).executes(ctx -> TM.execute(ctx.getSource(),
                        EntityArgument.getPlayer(ctx, "player"), StringArgumentType.getString(ctx, "tm")))));
        commandDispatcher.register(command);
    }
}
