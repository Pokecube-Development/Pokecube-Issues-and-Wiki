package pokecube.core.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;
import thut.core.common.commands.CommandTools;

public class Pokeegg
{

    private static int execute(final CommandSource source, final String name, final List<Object> args)
            throws CommandSyntaxException
    {
        PokedexEntry entry = Database.getEntry(name);
        if (name.startsWith("random_"))
        {
            final ArrayList<PokedexEntry> entries = Lists.newArrayList(Database.getSortedFormes());
            Collections.shuffle(entries);
            final Iterator<PokedexEntry> iterator = entries.iterator();
            if (name.equalsIgnoreCase("random_normal"))
            {
                entry = iterator.next();
                while (entry.isLegendary() || entry.isMega)
                    entry = iterator.next();
            }
            else if (name.equalsIgnoreCase("random_all"))
            {
                entry = iterator.next();
                while (!entry.base)
                    entry = iterator.next();
            }
            else if (name.equalsIgnoreCase("random_legend"))
            {
                entry = iterator.next();
                while (!entry.isLegendary() || !entry.base)
                    entry = iterator.next();
            }
        }
        final Entity mob = PokecubeCore.createPokemob(entry, source.getWorld());
        if (mob == null)
        {
            CommandTools.sendError(source, "pokecube.command.makeinvalid");
            return 1;
        }
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        PlayerEntity owner = null;

        if (!args.isEmpty() && args.get(0) instanceof PlayerEntity) owner = (PlayerEntity) args.remove(0);
        else owner = source.asPlayer();

        final List<String> newArgs = Lists.newArrayList();
        for (final Object o : args)
        {
            final String[] split = o.toString().split(" ");
            for (final String s : split)
                newArgs.add(s);
        }
        final Vector3 offset = Vector3.getNewVector().set(0, 1, 0);
        Pokemake.setToArgs(newArgs.toArray(new String[0]), pokemob, 0, offset);

        final ItemStack stack = ItemPokemobEgg.getEggStack(pokemob);

        Tools.giveItem(owner, stack);

        final String text = TextFormatting.GREEN + "Spawned " + pokemob.getDisplayName().getFormattedText();
        final ITextComponent message = ITextComponent.Serializer.fromJson("[\"" + text + "\"]");
        source.sendFeedback(message, true);
        return 0;
    }

    private static SuggestionProvider<CommandSource> SUGGEST_OTHERS = (ctx,
            sb) -> net.minecraft.command.ISuggestionProvider.suggest(Lists.newArrayList("random_normal", "random_all",
                    "random_legend"), sb);

    private static SuggestionProvider<CommandSource> SUGGEST_POKEMOB = (ctx,
            sb) -> net.minecraft.command.ISuggestionProvider.suggest(Database.getSortedFormNames(), sb);

    public static void register(final CommandDispatcher<CommandSource> commandDispatcher)
    {
        // Normal pokemake
        PermissionAPI.registerNode("command.pokeegg", DefaultPermissionLevel.OP,
                "Is the player allowed to use /pokeegg");

        final SuggestionProvider<CommandSource> TEMP = (ctx, sb) -> net.minecraft.command.ISuggestionProvider.suggest(
                Lists.newArrayList("args"), sb);

        LiteralArgumentBuilder<CommandSource> command = Commands.literal("pokeegg");
        // Set a permission
        command = command.requires(cs -> CommandTools.hasPerm(cs, "command.pokeegg"));
        // Plain command, no args besides name.
        command = command.then(Commands.argument("mob", StringArgumentType.string()).suggests(Pokeegg.SUGGEST_POKEMOB)
                .executes(ctx -> Pokeegg.execute(ctx.getSource(), StringArgumentType.getString(ctx, "mob"), Lists
                        .newArrayList())));

        // command with player and no arguments
        command = command.then(Commands.argument("mob", StringArgumentType.string()).suggests(Pokeegg.SUGGEST_POKEMOB)
                .then(Commands.argument("player", EntityArgument.player()).executes(ctx -> Pokeegg.execute(ctx
                        .getSource(), StringArgumentType.getString(ctx, "mob"), Lists.newArrayList(EntityArgument
                                .getPlayer(ctx, "player"))))));

        // Command with player then string arguments
        command = command.then(Commands.argument("mob", StringArgumentType.string()).suggests(Pokeegg.SUGGEST_POKEMOB)
                .then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("args:",
                        StringArgumentType.string()).suggests(TEMP).then(Commands.argument("arg1", StringArgumentType
                                .greedyString()).executes(ctx -> Pokeegg.execute(ctx.getSource(), StringArgumentType
                                        .getString(ctx, "mob"), Lists.newArrayList(EntityArgument.getPlayer(ctx,
                                                "player"), StringArgumentType.getString(ctx, "arg1"))))))));
        // Command string arguments
        command = command.then(Commands.argument("mob", StringArgumentType.string()).suggests(Pokeegg.SUGGEST_POKEMOB)
                .then(Commands.argument("args:", StringArgumentType.string()).suggests(TEMP).then(Commands.argument(
                        "arg1", StringArgumentType.greedyString()).executes(ctx -> Pokeegg.execute(ctx.getSource(),
                                StringArgumentType.getString(ctx, "mob"), Lists.newArrayList(StringArgumentType
                                        .getString(ctx, "arg1")))))));

        commandDispatcher.register(command);

        // Random pokemake
        PermissionAPI.registerNode("command.pokeeggrand", DefaultPermissionLevel.OP,
                "Is the player allowed to use /pokeeggrand");

        command = Commands.literal("pokeeggrand");
        // Set a permission
        command = command.requires(cs -> CommandTools.hasPerm(cs, "command.pokeeggrand"));
        // Plain command, no args besides name.
        command = command.then(Commands.argument("mode", StringArgumentType.string()).suggests(Pokeegg.SUGGEST_OTHERS)
                .executes(ctx -> Pokeegg.execute(ctx.getSource(), StringArgumentType.getString(ctx, "mode"), Lists
                        .newArrayList())));

        // command with player an no arguments
        command = command.then(Commands.argument("mode", StringArgumentType.string()).suggests(Pokeegg.SUGGEST_OTHERS)
                .then(Commands.argument("player", EntityArgument.player()).executes(ctx -> Pokeegg.execute(ctx
                        .getSource(), StringArgumentType.getString(ctx, "mode"), Lists.newArrayList(EntityArgument
                                .getPlayer(ctx, "player"))))));

        // Command with player then string arguments
        command = command.then(Commands.argument("mode", StringArgumentType.string()).suggests(Pokeegg.SUGGEST_OTHERS)
                .then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("arg1",
                        StringArgumentType.greedyString()).executes(ctx -> Pokeegg.execute(ctx.getSource(),
                                StringArgumentType.getString(ctx, "mode"), Lists.newArrayList(EntityArgument.getPlayer(
                                        ctx, "player"), StringArgumentType.getString(ctx, "mob")))))));
        // Command string arguments
        command = command.then(Commands.argument("mode", StringArgumentType.string()).suggests(Pokeegg.SUGGEST_OTHERS)
                .then(Commands.argument("arg1", StringArgumentType.greedyString()).executes(ctx -> Pokeegg.execute(ctx
                        .getSource(), StringArgumentType.getString(ctx, "mode"), Lists.newArrayList(StringArgumentType
                                .getString(ctx, "arg1"))))));

        commandDispatcher.register(command);

    }
}
