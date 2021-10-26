package pokecube.core.client.gui.helper;

import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class ListHelper
{

    public static String removeTextColorsIfConfigured(final String text, final boolean forceColor)
    {
        return !forceColor && !Minecraft.getInstance().options.chatColors ? ChatFormatting
                .stripFormatting(text) : text;
    }

    public static void addSiblings(final Component base, final List<MutableComponent> toAdd)
    {
        MutableComponent us = null;
        if (base instanceof MutableComponent) us = (MutableComponent) base;
        else
        {
            us = new TextComponent(base.getContents());
            us.setStyle(base.getStyle());
        }
        toAdd.add(us);
        for (final Component sib : base.getSiblings())
            ListHelper.addSiblings(sib, toAdd);
    }

    public static List<MutableComponent> splitText(final MutableComponent textComponent,
            final int maxTextLenght, final Font fontRendererIn, final boolean trimSpace)
    {
        int i = 0;
        MutableComponent remainder = new TextComponent("");
        final List<MutableComponent> list = Lists.newArrayList();
        final List<MutableComponent> list1 = Lists.newArrayList();
        ListHelper.addSiblings(textComponent, list1);
        for (int j = 0; j < list1.size(); ++j)
        {
            final MutableComponent itextcomponent1 = list1.get(j);
            // This gets the raw copy, without siblings, etc
            String s = itextcomponent1.plainCopy().getString();
            Style style = itextcomponent1.getStyle();

            // This means it has arguments, that might have styles themselves!
            if (itextcomponent1 instanceof TranslatableComponent)
            {
                final TranslatableComponent comp = (TranslatableComponent) itextcomponent1;
                boolean hasClick = comp.getStyle().getClickEvent() != null;
                boolean hasHover = comp.getStyle().getHoverEvent() != null;
                for (final Object o : comp.getArgs())
                {
                    if (!(o instanceof Component)) continue;
                    final Component sub = (Component) o;
                    hasClick = sub.getStyle().getClickEvent() != null;
                    if (hasClick) style = style.withClickEvent(sub.getStyle().getClickEvent());
                    hasHover = sub.getStyle().getHoverEvent() != null;
                    if (hasHover) style = style.withHoverEvent(sub.getStyle().getHoverEvent());
                }
            }
            boolean flag = false;
            if (s.contains("\n"))
            {
                final int k = s.indexOf(10);
                final String s1 = s.substring(k + 1);
                s = s.substring(0, k + 1);
                final MutableComponent itextcomponent2 = new TextComponent(s1).setStyle(style);
                list1.add(j + 1, itextcomponent2);
                flag = true;
            }

            final String s4 = s;
            final String s5 = s4.endsWith("\n") ? s4.substring(0, s4.length() - 1) : s4;
            int i1 = fontRendererIn.width(s5);
            MutableComponent itextcomponent3 = new TextComponent(s5).setStyle(itextcomponent1
                    .getStyle());
            if (i + i1 > maxTextLenght)
            {
                String s2 = ListHelper.trimStringToWidth(fontRendererIn, s4, maxTextLenght - i, false);
                String s3 = s2.length() < s4.length() ? s4.substring(s2.length()) : null;
                if (s3 != null && !s3.isEmpty())
                {
                    int l = s3.charAt(0) != ' ' ? s2.lastIndexOf(32) : s2.length();
                    if (l >= 0 && fontRendererIn.width(s4.substring(0, l)) > 0)
                    {
                        s2 = s4.substring(0, l);
                        if (trimSpace) ++l;

                        s3 = s4.substring(l);
                    }
                    else if (i > 0 && !s4.contains(" "))
                    {
                        s2 = "";
                        s3 = s4;
                    }
                    s3 = ListHelper.getFormatString(s2) + s3;
                    final MutableComponent itextcomponent4 = new TextComponent(s3).setStyle(style);
                    list1.add(j + 1, itextcomponent4);
                }

                i1 = fontRendererIn.width(s2);
                itextcomponent3 = new TextComponent(s2);
                itextcomponent3.setStyle(style);
                flag = true;
            }

            if (i + i1 <= maxTextLenght)
            {
                i += i1;
                remainder.append(itextcomponent3);
            }
            else flag = true;

            if (flag)
            {
                list.add(remainder);
                i = 0;
                remainder = new TextComponent("");
            }
        }
        list.add(remainder);
        return list;
    }

    public static String getFormatString(final String stringIn)
    {
        final StringBuilder stringbuilder = new StringBuilder();
        int i = -1;
        final int j = stringIn.length();

        while ((i = stringIn.indexOf(167, i + 1)) != -1)
            if (i < j - 1)
            {
                final ChatFormatting textformatting = ListHelper.fromFormattingCode(stringIn.charAt(i + 1));
                if (textformatting != null)
                {
                    if (!textformatting.isFormat()) stringbuilder.setLength(0);

                    if (textformatting != ChatFormatting.RESET) stringbuilder.append(textformatting);
                }
            }

        return stringbuilder.toString();
    }

    public static ChatFormatting fromFormattingCode(final char formattingCodeIn)
    {
        final char c0 = Character.toString(formattingCodeIn).toLowerCase(Locale.ROOT).charAt(0);

        for (final ChatFormatting textformatting : ChatFormatting.values())
            if (textformatting.code == c0) return textformatting;

        return null;
    }

    public static String trimStringToWidth(final Font fontRendererIn, final String text, final int width,
            final boolean reverse)
    {
        final StringBuilder stringbuilder = new StringBuilder();
        float f = 0.0F;
        final int i = reverse ? text.length() - 1 : 0;
        final int j = reverse ? -1 : 1;
        boolean flag = false;
        boolean flag1 = false;

        for (int k = i; k >= 0 && k < text.length() && f < width; k += j)
        {
            final char c0 = text.charAt(k);
            if (flag)
            {
                flag = false;
                final ChatFormatting textformatting = ChatFormatting.getByCode(c0);
                if (textformatting == ChatFormatting.BOLD) flag1 = true;
                else if (textformatting != null && !textformatting.isFormat()) flag1 = false;
            }
            else if (c0 == 167) flag = true;
            else
            {
                f += fontRendererIn.width(c0 + "");
                if (flag1) ++f;
            }

            if (f > width) break;

            if (reverse) stringbuilder.insert(0, c0);
            else stringbuilder.append(c0);
        }

        return stringbuilder.toString();
    }
}
