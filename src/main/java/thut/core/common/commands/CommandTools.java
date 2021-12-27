package thut.core.common.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import thut.api.util.PermNodes;

public class CommandTools
{
    public static boolean hasPerm(final CommandSourceStack source, final String permission)
    {
        try
        {
            final ServerPlayer player = source.getPlayerOrException();
            return CommandTools.hasPerm(player, permission);
        }
        catch (final CommandSyntaxException e)
        {
            // TODO decide what to actually do here?
            return true;
        }
    }

    public static boolean hasPerm(final ServerPlayer player, final String permission)
    {
        return PermNodes.getBooleanPerm(player, permission);
    }

    public static Component makeError(final String text)
    {
        return CommandTools.makeTranslatedMessage(text, "red:italic");
    }

    public static Component makeTranslatedMessage(final String key, String formatting, final Object... args)
    {
        if (formatting == null) formatting = "";
        for (int i = 0; i < args.length; i++)
            if (args[i] instanceof String) args[i] = new TranslatableComponent((String) args[i]);
        final TranslatableComponent translated = new TranslatableComponent(key, args);
        if (!formatting.isEmpty())
        {
            final String[] args2 = formatting.split(":");
            final String colour = args2[0].toUpperCase(java.util.Locale.ROOT);
            translated.getStyle().withColor(TextColor.fromLegacyFormat(ChatFormatting.getByName(colour)));
            if (args2.length > 1) for (int i1 = 1; i1 < args2.length; i1++)
            {
                final String arg = args2[i1];
                if (arg.equalsIgnoreCase("italic")) translated.getStyle().withItalic(true);
                if (arg.equalsIgnoreCase("bold")) translated.getStyle().withBold(true);
                if (arg.equalsIgnoreCase("underlined")) translated.getStyle().setUnderlined(true);
                if (arg.equalsIgnoreCase("strikethrough")) translated.getStyle().setStrikethrough(true);
                if (arg.equalsIgnoreCase("obfuscated")) translated.getStyle().setObfuscated(true);
            }
        }
        return translated;
    }

    public static void sendBadArgumentsMissingArg(final CommandSource sender)
    {
        sender.sendMessage(CommandTools.makeError("pokecube.command.invalidmissing"), Util.NIL_UUID);
    }

    public static void sendBadArgumentsTryTab(final CommandSource sender)
    {
        sender.sendMessage(CommandTools.makeError("pokecube.command.invalidtab"), Util.NIL_UUID);
    }

    public static void sendError(final CommandSourceStack sender, final String text)
    {
        sender.sendFailure(CommandTools.makeError(text));
    }

    public static void sendError(final CommandSource sender, final String text)
    {
        sender.sendMessage(CommandTools.makeError(text), Util.NIL_UUID);
    }

    public static void sendMessage(final CommandSource sender, final String text)
    {
        final Component message = CommandTools.makeTranslatedMessage(text, null);
        sender.sendMessage(message, Util.NIL_UUID);
    }

    public static void sendNoPermissions(final CommandSource sender)
    {
        sender.sendMessage(CommandTools.makeError("pokecube.command.noperms"), Util.NIL_UUID);
    }
}
