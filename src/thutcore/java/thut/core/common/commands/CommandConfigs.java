package thut.core.common.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import thut.api.util.PermNodes;
import thut.api.util.PermNodes.DefaultPermissionLevel;
import thut.core.common.ThutCore;
import thut.core.common.config.Config.ConfigData;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class CommandConfigs
{
    protected static int execute(final ConfigData data, final CommandSourceStack source, final String field)
            throws CommandSyntaxException
    {
        try
        {
            final Field f = data.getClass().getField(field);
            final Object value = f.get(data);
            source.sendSuccess(() -> Component.translatableEscape("thutcore.command.settings.check", field, value), true);
        }
        catch (final Exception e)
        {
            throw new SimpleCommandExceptionType(Component.literal("Error with field name " + field)).create();
        }

        return 0;
    }

    protected static int execute(final ConfigData data, final CommandSourceStack source, final String field,
            final String message) throws CommandSyntaxException
    {
        Field f;
        Object value;
        try
        {
            f = data.getClass().getField(field);
            value = f.get(data);
        }
        catch (final Exception e)
        {
            ThutCore.LOGGER.error(e);
            throw new SimpleCommandExceptionType(Component.literal("Error with field name " + field)).create();
        }
        final String[] args = message.split(" ");
        StringBuilder val = new StringBuilder(args[0]);
        switch (val.toString())
        {
        case "!set" ->
        {
            CommandConfigs.handleSet(data, args, value, f);
            source.sendSuccess(() -> Component.translatableEscape("thutcore.command.settings.array.set", field, value),
                    true);
            return 0;
        }
        case "!add" ->
        {
            CommandConfigs.handleAdd(data, args, value, f);
            source.sendSuccess(() -> Component.translatableEscape("thutcore.command.settings.array.add", field, value),
                    true);
            return 0;
        }
        case "!remove" ->
        {
            CommandConfigs.handleRemove(data, args, value, f);
            source.sendSuccess(() -> Component.translatableEscape("thutcore.command.settings.array.remove", field, value),
                    true);
            return 0;
        }
        }

        if (args.length > 1) for (int i = 1; i < args.length; i++) val.append(" ").append(args[i]);
        try
        {
            Object finalValue3 = data.updateField(f, val.toString());
            source.sendSuccess(() -> Component.translatableEscape("thutcore.command.settings.set", field, finalValue3),
                    true);
        }
        catch (final Exception e)
        {
            throw new SimpleCommandExceptionType(Component.literal("Error with setting field name " + field)).create();
        }
        return 0;
    }

    static void handleAdd(final ConfigData data, final String[] args, final Object o, final Field field)
            throws CommandSyntaxException
    {
        StringBuilder value = new StringBuilder(args[1]);
        for (int i = 3; i < args.length; i++) value.append(" ").append(args[i]);
        Object toSet;
        if (o instanceof String[])
        {
            final int len = ((String[]) o).length;
            toSet = Arrays.copyOf((String[]) o, len + 1);
            ((String[]) toSet)[len] = value.toString();
        }
        else if (o instanceof int[])
        {
            final int len = ((int[]) o).length;
            toSet = Arrays.copyOf((int[]) o, len + 1);
            ((int[]) toSet)[len] = CommandConfigs.parseInt(value.toString());
        }
        else throw new SimpleCommandExceptionType(Component.literal("This can only by done for arrays.")).create();
        try
        {
            data.updateField(field, toSet);
        }
        catch (final Exception e)
        {
            throw new SimpleCommandExceptionType(Component.literal("Error with setting field name " + field)).create();
        }
    }

    static void handleRemove(final ConfigData data, final String[] args, final Object o, final Field field)
            throws CommandSyntaxException
    {
        StringBuilder value = new StringBuilder(args[1]);
        for (int i = 3; i < args.length; i++) value.append(" ").append(args[i]);
        Object toSet = null;
        if (o instanceof String[] arr)
        {
            final List<String> values = Lists.newArrayList(arr);
            final int index = values.indexOf(value.toString());
            if (index != -1) values.remove(index);
            toSet = values.toArray(new String[0]);
        }
        else if (o instanceof int[] arr)
        {
            final int arg = CommandConfigs.parseInt(value.toString());
            final List<Integer> values = Lists.newArrayList();
            for (final int element : arr) values.add(element);
            final int index = values.indexOf(arg);
            if (index != -1) values.remove(index);
            toSet = arr = new int[values.size()];
            for (int i = 0; i < values.size(); i++) arr[i] = values.get(i);
        }
        else throw new SimpleCommandExceptionType(Component.literal("This can only by done for arrays.")).create();
        try
        {
            data.updateField(field, toSet);
        }
        catch (final Exception e)
        {
            throw new SimpleCommandExceptionType(Component.literal("Error with setting field name " + field)).create();
        }
    }

    static void handleSet(final ConfigData data, final String[] args, final Object o, final Field field)
            throws CommandSyntaxException
    {
        final int num = CommandConfigs.parseInt(args[1]);
        StringBuilder value = new StringBuilder(args[2]);
        for (int i = 4; i < args.length; i++) value.append(" ").append(args[i]);
        Object toSet = null;
        if (o instanceof String[] arr)
        {
            arr[num] = value.toString();
            toSet = arr.clone();
        }
        else if (o instanceof int[] arr)
        {
            arr[num] = CommandConfigs.parseInt(value.toString());
            toSet = arr.clone();
        }
        else throw new SimpleCommandExceptionType(Component.literal("This can only by done for arrays.")).create();
        try
        {
            data.updateField(field, toSet);
        }
        catch (final Exception e)
        {
            throw new SimpleCommandExceptionType(Component.literal("Error with setting field name " + field)).create();
        }
    }

    public static SuggestionProvider<CommandSourceStack> MakeProvider(final ConfigData data)
    {
        final List<String> values = Lists.newArrayList();
        for (final Field f : data.commonValues.keySet()) values.add(f.getName());
        for (final Field f : data.serverValues.keySet()) values.add(f.getName());
        for (final Field f : data.clientValues.keySet()) values.add(f.getName());
        return (ctx, sb) -> net.minecraft.commands.SharedSuggestionProvider.suggest(values, sb);
    }

    public static int parseInt(final String input) throws CommandSyntaxException
    {
        try
        {
            return Integer.parseInt(input);
        }
        catch (final NumberFormatException var2)
        {
            throw new SimpleCommandExceptionType(
                    Component.translatableEscape("commands.generic.num.invalid", new Object[] { input })).create();
        }
    }

    public static void register(final ConfigData data, final CommandDispatcher<CommandSourceStack> commandDispatcher,
            final String prefix)
    {
        String name = "";
        name = prefix;
        final String perm1 = "command." + name + ".check";
        PermNodes.registerBooleanNode(ThutCore.MODID, perm1, DefaultPermissionLevel.ALL,
                "Is the player allowed to check configs for " + data.MODID);

        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(name)
                .requires(cs -> CommandTools.hasPerm(cs, perm1))
                .then(Commands.argument("option", StringArgumentType.string())
                        .suggests(CommandConfigs.MakeProvider(data)).executes(
                                ctx -> CommandConfigs.execute(data, ctx.getSource(),
                                        StringArgumentType.getString(ctx, "option"))));
        commandDispatcher.register(command);

        final String perm2 = "command." + name + ".set";
        PermNodes.registerBooleanNode(ThutCore.MODID, perm2, DefaultPermissionLevel.OP,
                "Is the player allowed to set configs for " + data.MODID);

        command = Commands.literal(name).then(Commands.argument("option", StringArgumentType.string())
                .suggests(CommandConfigs.MakeProvider(data))
                .then(Commands.argument("value", StringArgumentType.greedyString())
                        .requires(cs -> CommandTools.hasPerm(cs, perm2)).executes(
                                ctx -> CommandConfigs.execute(data, ctx.getSource(),
                                        StringArgumentType.getString(ctx, "option"),
                                        StringArgumentType.getString(ctx, "value")))));
        commandDispatcher.register(command);
    }
}
