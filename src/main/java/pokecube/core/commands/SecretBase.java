package pokecube.core.commands;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.GameProfileArgument;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.bases.BaseTile;
import pokecube.core.world.dimension.SecretBaseDimension;
import thut.api.OwnableCaps;
import thut.api.block.IOwnableTE;
import thut.api.entity.ThutTeleporter;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.core.common.commands.CommandTools;

public class SecretBase
{
    public static Map<UUID, Vector4> pendingBaseLocations = Maps.newHashMap();

    public static int execute(final CommandSource source, final ServerPlayerEntity player,
            final Collection<GameProfile> profiles)
    {
        final GameProfile match = profiles.iterator().next();
        SecretBaseDimension.sendToBase(player, match.getId());
        return 0;
    }

    public static int execute_exit(final CommandSource source, final ServerPlayerEntity player)
    {
        if (player.dimension != SecretBaseDimension.TYPE)
        {
            player.sendMessage(new TranslationTextComponent("pokecube.secretbase.exit.notinbase"));
            return 1;
        }
        final DimensionType targetDim = DimensionType.OVERWORLD;
        final BlockPos pos = SecretBaseDimension.getSecretBaseLoc(player.getUniqueID(), player.getServer(), targetDim);
        final Vector4 dest = new Vector4(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, targetDim.getId());
        ThutTeleporter.transferTo(player, dest);
        player.sendMessage(new TranslationTextComponent("pokecube.secretbase.exit"));
        return 0;
    }

    public static int execute_create(final CommandSource source, final ServerPlayerEntity player, final Vec3d input)
    {
        if (SecretBase.pendingBaseLocations.containsKey(player.getUniqueID()))
        {
            final Vector4 loc = SecretBase.pendingBaseLocations.remove(player.getUniqueID());
            final Vector3 pos = Vector3.getNewVector().set(loc.x, loc.y, loc.z);
            final DimensionType type = DimensionType.getById((int) loc.w);
            if (type == player.dimension && pos.distTo(Vector3.getNewVector().set(input)) < 16)
            {
                final BlockPos base_pos = new BlockPos(input);
                final BlockState original = pos.getBlockState(player.getEntityWorld());
                pos.setBlock(player.getEntityWorld(), PokecubeItems.SECRETBASE.getDefaultState());
                final BaseTile tile = (BaseTile) player.getEntityWorld().getTileEntity(pos.getPos());
                final IOwnableTE ownable = (IOwnableTE) tile.getCapability(OwnableCaps.CAPABILITY).orElse(null);
                ownable.setPlacer(player);
                tile.last_base = base_pos;
                tile.original = original;
                SecretBaseDimension.setSecretBasePoint(player, base_pos, type);
                pos.x = pos.intX();
                pos.y = pos.intY();
                pos.z = pos.intZ();
                final TranslationTextComponent message = new TranslationTextComponent("pokemob.createbase.confirmed",
                        pos);
                player.sendMessage(message);
                return 0;
            }
        }
        return 1;
    }

    private static SuggestionProvider<CommandSource> SUGGEST_EXIT    = (ctx,
            sb) -> net.minecraft.command.ISuggestionProvider.suggest(Lists.newArrayList("exit"), sb);
    private static SuggestionProvider<CommandSource> SUGGEST_CONFIRM = (ctx,
            sb) -> net.minecraft.command.ISuggestionProvider.suggest(Lists.newArrayList("confirm"), sb);

    public static void register(final CommandDispatcher<CommandSource> commandDispatcher)
    {
        PermissionAPI.registerNode("command.pokebase.other", DefaultPermissionLevel.OP,
                "Is the player allowed to use /pokebase to teleport to an arbitrary base");
        PermissionAPI.registerNode("command.pokebase.exit", DefaultPermissionLevel.ALL,
                "Is the player allowed to use /pokebase to exit a secret base");
        PermissionAPI.registerNode("command.pokebase.create", DefaultPermissionLevel.ALL,
                "Is the player allowed to use secret power to make a secret base");
        LiteralArgumentBuilder<CommandSource> command;

        command = Commands.literal("pokebase").requires(cs -> CommandTools.hasPerm(cs, "command.pokebase.exit")).then(
                Commands.argument("exit", StringArgumentType.word()).suggests(SecretBase.SUGGEST_EXIT).executes(
                        ctx -> SecretBase.execute_exit(ctx.getSource(), ctx.getSource().asPlayer())));
        commandDispatcher.register(command);

        command = Commands.literal("pokebase").requires(cs -> CommandTools.hasPerm(cs, "command.pokebase.create")).then(
                Commands.argument("confirm", StringArgumentType.word()).suggests(SecretBase.SUGGEST_CONFIRM).then(
                        Commands.argument("location", Vec3Argument.vec3()).executes(ctx -> SecretBase.execute_create(ctx
                                .getSource(), ctx.getSource().asPlayer(), Vec3Argument.getVec3(ctx, "location")))));
        commandDispatcher.register(command);

        command = Commands.literal("pokebase").requires(cs -> CommandTools.hasPerm(cs, "command.pokebase.other")).then(
                Commands.argument("target", EntityArgument.player()).then(Commands.argument("owner", GameProfileArgument
                        .gameProfile()).executes(ctx -> SecretBase.execute(ctx.getSource(), EntityArgument.getPlayer(
                                ctx, "target"), GameProfileArgument.getGameProfiles(ctx, "owner")))));
        commandDispatcher.register(command);
    }
}
