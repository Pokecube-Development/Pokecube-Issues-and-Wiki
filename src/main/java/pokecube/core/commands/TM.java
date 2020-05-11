package pokecube.core.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import pokecube.core.items.ItemTM;
import pokecube.core.moves.MovesUtils;
import thut.core.common.commands.CommandTools;

public class TM
{
    private static SuggestionProvider<CommandSource> SUGGEST_TMS = (ctx,
            sb) -> net.minecraft.command.ISuggestionProvider.suggest(MovesUtils.getKnownMoveNames(), sb);

    public static int execute(final CommandSource source, final ServerPlayerEntity serverplayerentity, final String tm)
    {
        final ItemStack itemstack = ItemTM.getTM(tm);
        final boolean flag = serverplayerentity.inventory.addItemStackToInventory(itemstack);
        if (flag && itemstack.isEmpty())
        {
            itemstack.setCount(1);
            final ItemEntity itementity1 = serverplayerentity.dropItem(itemstack, false);
            if (itementity1 != null) itementity1.makeFakeItem();
            serverplayerentity.world.playSound((PlayerEntity) null, serverplayerentity.posX, serverplayerentity.posY,
                    serverplayerentity.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F,
                    ((serverplayerentity.getRNG().nextFloat() - serverplayerentity.getRNG().nextFloat()) * 0.7F + 1.0F)
                            * 2.0F);
            serverplayerentity.container.detectAndSendChanges();
        }
        else
        {
            final ItemEntity itementity = serverplayerentity.dropItem(itemstack, false);
            if (itementity != null)
            {
                itementity.setNoPickupDelay();
                itementity.setOwnerId(serverplayerentity.getUniqueID());
            }
        }
        return 0;
    }

    public static int execute(final CommandSource source, final String tm) throws CommandSyntaxException
    {
        final ServerPlayerEntity player = source.asPlayer();
        return TM.execute(source, player, tm);
    }

    public static void register(final CommandDispatcher<CommandSource> commandDispatcher)
    {
        final String perm = "command.poketm";
        PermissionAPI.registerNode(perm, DefaultPermissionLevel.OP, "Is the player allowed to use /poketm");

        // Setup with name and permission
        LiteralArgumentBuilder<CommandSource> command = Commands.literal("poketm").requires(cs -> CommandTools.hasPerm(
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
