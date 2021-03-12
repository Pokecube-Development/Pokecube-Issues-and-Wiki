package pokecube.core.commands;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.Sets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import pokecube.core.handlers.events.EventsHandler;
import pokecube.core.handlers.events.PCEventsHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.items.pokecubes.EntityPokecubeBase;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.items.pokecubes.helper.SendOutManager;
import pokecube.core.utils.PokemobTracker;
import thut.core.common.commands.CommandTools;

public class Pokerecall
{
    private static SuggestionProvider<CommandSource> SUGGEST_NAMES = (ctx, sb) ->
    {
        final ServerPlayerEntity player = ctx.getSource().getPlayerOrException();
        final Set<String> opts = Sets.newHashSet();
        final List<Entity> mobs = PokemobTracker.getMobs(player, c -> EventsHandler.validRecall(player, c, null, true,
                true));
        for (final Entity e : mobs)
        {
            final IPokemob poke = CapabilityPokemob.getPokemobFor(e);
            if (poke != null) opts.add(e.getDisplayName().getString());
            else if (e instanceof EntityPokecubeBase)
            {
                final EntityPokecubeBase cube = (EntityPokecubeBase) e;
                final Entity mob = PokecubeManager.itemToMob(cube.getItem(), cube.getCommandSenderWorld());
                if (mob != null) opts.add(mob.getDisplayName().getString());
            }
        }
        return net.minecraft.command.ISuggestionProvider.suggest(opts, sb);
    };

    public static int execute(final CommandSource source, final String pokemob) throws CommandSyntaxException
    {
        int num = 0;
        final ServerPlayerEntity player = source.getPlayerOrException();
        for (final Entity e : PCEventsHandler.getOutMobs(player, true))
            if (e.getDisplayName().getString().equals(pokemob))
            {
                final IPokemob poke = CapabilityPokemob.getPokemobFor(e);
                if (poke != null)
                {
                    poke.onRecall();
                    num++;
                }
            }
            else if (e instanceof EntityPokecubeBase)
            {
                final EntityPokecubeBase cube = (EntityPokecubeBase) e;
                final Entity mob = PokecubeManager.itemToMob(cube.getItem(), cube.getCommandSenderWorld());
                if (mob != null && mob.getDisplayName().getString().equals(pokemob))
                {
                    final LivingEntity sent = SendOutManager.sendOut(cube, true, false);
                    IPokemob poke;
                    if (sent != null && (poke = CapabilityPokemob.getPokemobFor(sent)) != null)
                    {
                        System.out.println(poke);
                        poke.onRecall();
                        num++;
                    }
                }
            }
        if (num == 0) source.sendSuccess(new TranslationTextComponent("pokecube.recall.fail"), false);
        else source.sendSuccess(new TranslationTextComponent("pokecube.recall.success", num), false);
        return 0;
    }

    public static int execute(final CommandSource source, final ServerPlayerEntity player, final boolean all,
            final boolean sitting, final boolean staying) throws CommandSyntaxException
    {
        int num = 0;
        for (final Entity e : PCEventsHandler.getOutMobs(player, true))
        {
            IPokemob poke = CapabilityPokemob.getPokemobFor(e);
            if (poke != null) if (all || sitting && poke.getLogicState(LogicStates.SITTING) || staying && poke
                    .getGeneralState(GeneralStates.STAYING))
            {
                poke.onRecall();
                num++;
            }
            else if (e instanceof EntityPokecubeBase)
            {
                final EntityPokecubeBase cube = (EntityPokecubeBase) e;
                final LivingEntity sent = SendOutManager.sendOut(cube, true);
                if (sent != null && (poke = CapabilityPokemob.getPokemobFor(e)) != null)
                {
                    poke.onRecall();
                    num++;
                }
            }
        }
        if (num == 0) source.sendSuccess(new TranslationTextComponent("pokecube.recall.fail"), false);
        else source.sendSuccess(new TranslationTextComponent("pokecube.recall.success", num), false);
        return 0;
    }

    public static void register(final CommandDispatcher<CommandSource> commandDispatcher)
    {
        PermissionAPI.registerNode("command.pokerecall", DefaultPermissionLevel.ALL,
                "Is the player allowed to use /pokerecall");
        PermissionAPI.registerNode("command.pokerecall.other", DefaultPermissionLevel.OP,
                "Is the player allowed to use /pokerecall to recall other people's mobs");

        final Predicate<CommandSource> op_perm = cs -> CommandTools.hasPerm(cs, "command.pokerecall.other");

        // Setup with name and permission
        LiteralArgumentBuilder<CommandSource> command = Commands.literal("pokerecall").requires(cs -> CommandTools
                .hasPerm(cs, "command.pokerecall"));

        // No target argument version
        command = command.then(Commands.argument("name", StringArgumentType.string()).suggests(Pokerecall.SUGGEST_NAMES)
                .executes(ctx -> Pokerecall.execute(ctx.getSource(), StringArgumentType.getString(ctx, "name"))));
        // Target argument version
        command = command.then(Commands.literal("all").executes(ctx -> Pokerecall.execute(ctx.getSource(), ctx
                .getSource().getPlayerOrException(), true, true, true)));
        command = command.then(Commands.literal("sitting").executes(ctx -> Pokerecall.execute(ctx.getSource(), ctx
                .getSource().getPlayerOrException(), false, true, false)));
        command = command.then(Commands.literal("staying").executes(ctx -> Pokerecall.execute(ctx.getSource(), ctx
                .getSource().getPlayerOrException(), false, false, true)));

        // Target with player
        command = command.then(Commands.literal("all").then(Commands.argument("owner", EntityArgument.player())
                .requires(op_perm).executes(ctx -> Pokerecall.execute(ctx.getSource(), EntityArgument.getPlayer(ctx,
                        "owner"), true, true, true))));
        command = command.then(Commands.literal("sitting").then(Commands.argument("owner", EntityArgument.player())
                .requires(op_perm).executes(ctx -> Pokerecall.execute(ctx.getSource(), EntityArgument.getPlayer(ctx,
                        "owner"), false, true, true))));
        command = command.then(Commands.literal("staying").then(Commands.argument("owner", EntityArgument.player())
                .requires(op_perm).executes(ctx -> Pokerecall.execute(ctx.getSource(), EntityArgument.getPlayer(ctx,
                        "owner"), false, false, true))));
        commandDispatcher.register(command);
    }
}
