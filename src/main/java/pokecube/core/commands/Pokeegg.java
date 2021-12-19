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

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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

    private static int execute(final CommandSourceStack source, final String name, final List<Object> args)
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
                while (entry.isLegendary() || entry.isMega()) entry = iterator.next();
            }
            else if (name.equalsIgnoreCase("random_all"))
            {
                entry = iterator.next();
                while (!entry.base) entry = iterator.next();
            }
            else if (name.equalsIgnoreCase("random_legend"))
            {
                entry = iterator.next();
                while (!entry.isLegendary() || !entry.base) entry = iterator.next();
            }
        }
        final Entity mob = PokecubeCore.createPokemob(entry, source.getLevel());
        if (mob == null)
        {
            CommandTools.sendError(source, "pokecube.command.makeinvalid");
            return 1;
        }
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        Player owner = null;

        if (!args.isEmpty() && args.get(0) instanceof Player) owner = (Player) args.remove(0);
        else owner = source.getPlayerOrException();

        final List<String> newArgs = Lists.newArrayList();
        for (final Object o : args)
        {
            final String[] split = o.toString().split(" ");
            for (final String s : split) newArgs.add(s);
        }
        final Vector3 offset = Vector3.getNewVector().set(0, 1, 0);
        Pokemake.setToArgs(newArgs.toArray(new String[0]), pokemob, 0, offset);

        final ItemStack stack = ItemPokemobEgg.getEggStack(pokemob);

        Tools.giveItem(owner, stack);

        final String text = ChatFormatting.GREEN + "Spawned " + pokemob.getDisplayName().getString();
        final Component message = Component.Serializer.fromJson("[\"" + text + "\"]");
        source.sendSuccess(message, true);
        return 0;
    }

    private static SuggestionProvider<CommandSourceStack> SUGGEST_OTHERS = (ctx,
            sb) -> net.minecraft.commands.SharedSuggestionProvider
                    .suggest(Lists.newArrayList("random_normal", "random_all", "random_legend"), sb);

    private static SuggestionProvider<CommandSourceStack> SUGGEST_POKEMOB = (ctx,
            sb) -> net.minecraft.commands.SharedSuggestionProvider.suggest(Database.getSortedFormNames(), sb);

    public static void register(final CommandDispatcher<CommandSourceStack> commandDispatcher)
    {
        final String perm = "command.pokeegg";
        PermissionAPI.registerNode(perm, DefaultPermissionLevel.OP, "Is the player allowed to use /pokeegg");

        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("pokeegg")
                .requires(cs -> CommandTools.hasPerm(cs, perm));
        // Plain command, no args besides name.
        command = command.then(Commands.argument("mob", StringArgumentType.string()).suggests(Pokeegg.SUGGEST_POKEMOB)
                .executes(ctx -> Pokeegg.execute(ctx.getSource(), StringArgumentType.getString(ctx, "mob"),
                        Lists.newArrayList())));
        commandDispatcher.register(command);

        command = Commands.literal("pokeegg").requires(cs -> CommandTools.hasPerm(cs, perm));
        // command with player and no arguments
        command = command.then(Commands.argument("mob", StringArgumentType.string()).suggests(Pokeegg.SUGGEST_POKEMOB)
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> Pokeegg.execute(ctx.getSource(), StringArgumentType.getString(ctx, "mob"),
                                Lists.newArrayList(EntityArgument.getPlayer(ctx, "player"))))));
        commandDispatcher.register(command);

        command = Commands.literal("pokeegg").requires(cs -> CommandTools.hasPerm(cs, perm));
        // Command with player then string arguments
        command = command.then(Commands.argument("mob", StringArgumentType.string()).suggests(Pokeegg.SUGGEST_POKEMOB)
                .then(Commands.argument("player", EntityArgument.player()).then(Commands
                        .argument("args", StringArgumentType.greedyString())
                        .executes(ctx -> Pokeegg.execute(ctx.getSource(), StringArgumentType.getString(ctx, "mob"),
                                Lists.newArrayList(EntityArgument.getPlayer(ctx, "player"),
                                        StringArgumentType.getString(ctx, "args")))))));
        commandDispatcher.register(command);

        command = Commands.literal("pokeegg").requires(cs -> CommandTools.hasPerm(cs, perm));
        // Command string arguments
        command = command.then(Commands.argument("mob", StringArgumentType.string()).suggests(Pokeegg.SUGGEST_POKEMOB)
                .then(Commands.argument("args", StringArgumentType.greedyString())
                        .executes(ctx -> Pokeegg.execute(ctx.getSource(), StringArgumentType.getString(ctx, "mob"),
                                Lists.newArrayList(StringArgumentType.getString(ctx, "args"))))));
        commandDispatcher.register(command);

        // Random Pokeegg
        final String perm2 = "command.pokeeggrand";
        PermissionAPI.registerNode(perm2, DefaultPermissionLevel.OP, "Is the player allowed to use pokeeggrand");

        // Set a permission
        command = Commands.literal("pokeeggrand").requires(cs -> CommandTools.hasPerm(cs, perm2));
        // Plain command, no args besides name.
        command = command.then(Commands.argument("mode", StringArgumentType.string()).suggests(Pokeegg.SUGGEST_OTHERS)
                .executes(ctx -> Pokeegg.execute(ctx.getSource(), StringArgumentType.getString(ctx, "mode"),
                        Lists.newArrayList())));
        commandDispatcher.register(command);

        // Set a permission
        command = Commands.literal("pokeeggrand").requires(cs -> CommandTools.hasPerm(cs, perm2));
        // command with player an no arguments
        command = command.then(Commands.argument("mode", StringArgumentType.string()).suggests(Pokeegg.SUGGEST_OTHERS)
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> Pokeegg.execute(ctx.getSource(), StringArgumentType.getString(ctx, "mode"),
                                Lists.newArrayList(EntityArgument.getPlayer(ctx, "player"))))));
        commandDispatcher.register(command);

        // Set a permission
        command = Commands.literal("pokeeggrand").requires(cs -> CommandTools.hasPerm(cs, perm2));
        // Command with player then string arguments
        command = command.then(Commands.argument("mode", StringArgumentType.string()).suggests(Pokeegg.SUGGEST_OTHERS)
                .then(Commands.argument("player", EntityArgument.player()).then(Commands
                        .argument("args", StringArgumentType.greedyString())
                        .executes(ctx -> Pokeegg.execute(ctx.getSource(), StringArgumentType.getString(ctx, "mode"),
                                Lists.newArrayList(EntityArgument.getPlayer(ctx, "player"),
                                        StringArgumentType.getString(ctx, "args")))))));
        commandDispatcher.register(command);

        // Set a permission
        command = Commands.literal("pokeeggrand").requires(cs -> CommandTools.hasPerm(cs, perm2));
        // Command string arguments
        command = command.then(Commands.argument("mode", StringArgumentType.string()).suggests(Pokeegg.SUGGEST_OTHERS)
                .then(Commands.argument("args", StringArgumentType.greedyString())
                        .executes(ctx -> Pokeegg.execute(ctx.getSource(), StringArgumentType.getString(ctx, "mode"),
                                Lists.newArrayList(StringArgumentType.getString(ctx, "args"))))));
        commandDispatcher.register(command);

    }
}
