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
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.GameProfileArgument;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.bases.BaseTile;
import pokecube.core.world.dimension.SecretBaseDimension;
import thut.api.ThutCaps;
import thut.api.block.IOwnableTE;
import thut.api.entity.ThutTeleporter;
import thut.api.entity.ThutTeleporter.TeleDest;
import thut.api.maths.Vector3;
import thut.core.common.commands.CommandTools;

public class SecretBase
{
    public static Map<UUID, GlobalPos> pendingBaseLocations = Maps.newHashMap();

    private static int execute(final CommandSource source, final ServerPlayerEntity player,
            final Collection<GameProfile> profiles)
    {
        final GameProfile match = profiles.iterator().next();
        SecretBaseDimension.sendToBase(player, match.getId());
        return 0;
    }

    private static int execute_clean(final CommandSource source, final ServerPlayerEntity player)
    {
        final BlockPos pos = player.blockPosition();
        final AxisAlignedBB box = new AxisAlignedBB(pos.offset(-30, -pos.getY() + 1, -30), pos.offset(30, 256 - pos.getY(),
                30));
        final World world = player.getCommandSenderWorld();
        BlockPos.betweenClosedStream(box).forEach(p ->
        {
            if (p.getY() == 0) return;
            if (world.getBlockState(p).getBlock() == Blocks.BARRIER) world.setBlockAndUpdate(p, Blocks.AIR
                    .defaultBlockState());
        });
        return 0;
    }

    private static int execute_exit(final CommandSource source, final ServerPlayerEntity player)
    {
        if (player.getCommandSenderWorld().dimension() != SecretBaseDimension.WORLD_KEY)
        {
            player.sendMessage(new TranslationTextComponent("pokecube.secretbase.exit.notinbase"), Util.NIL_UUID);
            return 1;
        }
        final GlobalPos pos = SecretBaseDimension.getSecretBaseLoc(player.getUUID(), player.getServer(), false);
        final Vector3 v = Vector3.getNewVector().set(pos).addTo(0.5, 0, 0.5);
        ThutTeleporter.transferTo(player, new TeleDest().setLoc(pos, v), true);
        player.sendMessage(new TranslationTextComponent("pokecube.secretbase.exit"), Util.NIL_UUID);
        return 0;
    }

    private static int execute_create(final CommandSource source, final ServerPlayerEntity player, final Vector3d input)
    {
        if (SecretBase.pendingBaseLocations.containsKey(player.getUUID()))
        {
            final GlobalPos loc = SecretBase.pendingBaseLocations.remove(player.getUUID());
            final Vector3 pos = Vector3.getNewVector().set(loc.pos());
            final RegistryKey<World> type = loc.dimension();
            if (type == player.getCommandSenderWorld().dimension() && pos.distTo(Vector3.getNewVector().set(input)) < 16)
            {
                final BlockPos base_pos = new BlockPos(input);
                final BlockState original = pos.getBlockState(player.getCommandSenderWorld());
                pos.setBlock(player.getCommandSenderWorld(), PokecubeItems.SECRETBASE.get().defaultBlockState());
                final BaseTile tile = (BaseTile) player.getCommandSenderWorld().getBlockEntity(pos.getPos());
                final IOwnableTE ownable = (IOwnableTE) tile.getCapability(ThutCaps.OWNABLE_CAP).orElse(null);
                ownable.setPlacer(player);
                final GlobalPos gpos = GlobalPos.of(loc.dimension(), base_pos);
                tile.last_base = gpos;
                tile.original = original;
                SecretBaseDimension.setSecretBasePoint(player, gpos, type == SecretBaseDimension.WORLD_KEY);
                pos.x = pos.intX();
                pos.y = pos.intY();
                pos.z = pos.intZ();
                final TranslationTextComponent message = new TranslationTextComponent("pokemob.createbase.confirmed",
                        pos);
                player.sendMessage(message, Util.NIL_UUID);
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

        command = Commands.literal("pokebase").then(Commands.argument("exit", StringArgumentType.word()).requires(
                cs -> CommandTools.hasPerm(cs, "command.pokebase.exit")).suggests(SecretBase.SUGGEST_EXIT).executes(
                        ctx -> SecretBase.execute_exit(ctx.getSource(), ctx.getSource().getPlayerOrException())));
        commandDispatcher.register(command);

        command = Commands.literal("pokebase").then(Commands.argument("confirm", StringArgumentType.word()).requires(
                cs -> CommandTools.hasPerm(cs, "command.pokebase.create")).suggests(SecretBase.SUGGEST_CONFIRM).then(
                        Commands.argument("location", Vec3Argument.vec3()).executes(ctx -> SecretBase.execute_create(ctx
                                .getSource(), ctx.getSource().getPlayerOrException(), Vec3Argument.getVec3(ctx, "location")))));
        commandDispatcher.register(command);

        command = Commands.literal("pokebase").then(Commands.argument("target", EntityArgument.player()).requires(
                cs -> CommandTools.hasPerm(cs, "command.pokebase.other")).then(Commands.argument("owner",
                        GameProfileArgument.gameProfile()).executes(ctx -> SecretBase.execute(ctx.getSource(),
                                EntityArgument.getPlayer(ctx, "target"), GameProfileArgument.getGameProfiles(ctx,
                                        "owner")))));
        commandDispatcher.register(command);

        PermissionAPI.registerNode("command.pokebase.clean", DefaultPermissionLevel.ALL,
                "Temporary cleanup command for removing barrier blocks in secret bases!");

        command = Commands.literal("pokebase").then(Commands.argument("clean", StringArgumentType.word()).requires(
                cs -> CommandTools.hasPerm(cs, "command.pokebase.clean")).executes(ctx -> SecretBase.execute_clean(ctx
                        .getSource(), ctx.getSource().getPlayerOrException())));
        commandDispatcher.register(command);
    }
}
