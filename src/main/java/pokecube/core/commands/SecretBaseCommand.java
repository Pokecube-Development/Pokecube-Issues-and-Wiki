package pokecube.core.commands;

import java.util.Collection;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.GameProfileArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import pokecube.core.world.dimension.SecretBaseDimension;
import thut.core.common.commands.CommandTools;

public class SecretBaseCommand
{
    public static int execute(final CommandSource source, final ServerPlayerEntity player,
            final Collection<GameProfile> profiles)
    {
        System.out.println(player);
        System.out.println(profiles);
        final ServerWorld baseWorld = DimensionManager.getWorld(source.getServer(), SecretBaseDimension.TYPE, true,
                true);

        System.out.println(source.getServer().getWorlds());
        System.out.println(baseWorld);
        player.setPosition(8, 200, 8);
        source.getServer().getPlayerList().recreatePlayerEntity(player, SecretBaseDimension.TYPE, true);

        return 0;
    }

    public static void register(final CommandDispatcher<CommandSource> commandDispatcher)
    {
        PermissionAPI.registerNode("command.pokebase", DefaultPermissionLevel.OP,
                "Is the player allowed to use /pokebase");

        final LiteralArgumentBuilder<CommandSource> command = Commands.literal("pokebase").requires(cs -> CommandTools
                .hasPerm(cs, "command.pokebase")).then(Commands.argument("player", EntityArgument.player()).then(
                        Commands.argument("owner", GameProfileArgument.gameProfile()).executes(ctx -> SecretBaseCommand
                                .execute(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), GameProfileArgument
                                        .getGameProfiles(ctx, "owner")))));
        commandDispatcher.register(command);
    }
}
