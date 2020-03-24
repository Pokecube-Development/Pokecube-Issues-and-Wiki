package pokecube.core.commands;

import java.util.Set;

import com.google.common.collect.Sets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import pokecube.core.handlers.events.PCEventsHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import thut.core.common.commands.CommandTools;

public class Pokerecall
{
    private static SuggestionProvider<CommandSource> SUGGEST_NAMES = (ctx, sb) ->
    {
        final ServerPlayerEntity player = ctx.getSource().asPlayer();
        final Set<String> opts = Sets.newHashSet();
        for (final Entity e : PCEventsHandler.getOutMobs(player, true))
            opts.add(e.getDisplayName().getFormattedText());
        return net.minecraft.command.ISuggestionProvider.suggest(opts, sb);
    };

    public static int execute(final CommandSource source, final String pokemob) throws CommandSyntaxException
    {
        int num = 0;
        final ServerPlayerEntity player = source.asPlayer();
        for (final Entity e : PCEventsHandler.getOutMobs(player, true))
            if (e.getDisplayName().getFormattedText().equals(pokemob))
            {
                final IPokemob poke = CapabilityPokemob.getPokemobFor(e);
                if (poke != null)
                {
                    poke.onRecall();
                    num++;
                }
            }
        if (num == 0) source.sendFeedback(new TranslationTextComponent("pokecube.recall.fail"), false);
        else source.sendFeedback(new TranslationTextComponent("pokecube.recall.success", num), false);
        return 0;
    }

    public static int execute(final CommandSource source, final boolean all, final boolean sitting,
            final boolean staying) throws CommandSyntaxException
    {
        int num = 0;
        final ServerPlayerEntity player = source.asPlayer();
        for (final Entity e : PCEventsHandler.getOutMobs(player, true))
        {
            final IPokemob poke = CapabilityPokemob.getPokemobFor(e);
            if (poke != null) if (all || sitting && poke.getLogicState(LogicStates.SITTING) || staying && poke
                    .getGeneralState(GeneralStates.STAYING))
            {
                poke.onRecall();
                num++;
            }
        }
        if (num == 0) source.sendFeedback(new TranslationTextComponent("pokecube.recall.fail"), false);
        else source.sendFeedback(new TranslationTextComponent("pokecube.recall.success", num), false);
        return 0;
    }

    public static void register(final CommandDispatcher<CommandSource> commandDispatcher)
    {
        PermissionAPI.registerNode("command.pokerecall", DefaultPermissionLevel.ALL,
                "Is the player allowed to use /pokerecall");

        // Setup with name and permission
        LiteralArgumentBuilder<CommandSource> command = Commands.literal("pokerecall").requires(cs -> CommandTools
                .hasPerm(cs, "command.pokerecall"));
        // No target argument version
        command = command.then(Commands.argument("name", StringArgumentType.string()).suggests(Pokerecall.SUGGEST_NAMES)
                .executes(ctx -> Pokerecall.execute(ctx.getSource(), StringArgumentType.getString(ctx, "name"))));
        // Target argument version
        command = command.then(Commands.literal("all").executes(ctx -> Pokerecall.execute(ctx.getSource(), true, true,
                true)));
        command = command.then(Commands.literal("sitting").executes(ctx -> Pokerecall.execute(ctx.getSource(), false,
                true, false)));
        command = command.then(Commands.literal("staying").executes(ctx -> Pokerecall.execute(ctx.getSource(), false,
                false, true)));
        commandDispatcher.register(command);
    }
}
