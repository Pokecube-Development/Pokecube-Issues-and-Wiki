package pokecube.core.client.gui.helper;

import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class ListHelper
{

    public static String removeTextColorsIfConfigured(final String text, final boolean forceColor)
    {
        return !forceColor && !Minecraft.getInstance().gameSettings.chatColor ? TextFormatting
                .getTextWithoutFormattingCodes(text) : text;
    }

    public static void addSiblings(final ITextComponent base, final List<IFormattableTextComponent> toAdd)
    {
        IFormattableTextComponent us = null;
        if (base instanceof IFormattableTextComponent) us = (IFormattableTextComponent) base;
        else
        {
            us = new StringTextComponent(base.getUnformattedComponentText());
            us.setStyle(base.getStyle());
        }
        toAdd.add(us);
        for (final ITextComponent sib : base.getSiblings())
            ListHelper.addSiblings(sib, toAdd);
    }

    public static List<IFormattableTextComponent> splitText(final IFormattableTextComponent textComponent,
            final int maxTextLenght, final FontRenderer fontRendererIn, final boolean trimSpace)
    {
        int i = 0;
        IFormattableTextComponent remainder = new StringTextComponent("");
        final List<IFormattableTextComponent> list = Lists.newArrayList();
        final List<IFormattableTextComponent> list1 = Lists.newArrayList();
        ListHelper.addSiblings(textComponent, list1);
        for (int j = 0; j < list1.size(); ++j)
        {
            final IFormattableTextComponent itextcomponent1 = list1.get(j);
            // This gets the raw copy, without siblings, etc
            String s = itextcomponent1.copyRaw().getString();
            Style style = itextcomponent1.getStyle();

            // This means it has arguments, that might have styles themselves!
            if (itextcomponent1 instanceof TranslationTextComponent)
            {
                final TranslationTextComponent comp = (TranslationTextComponent) itextcomponent1;
                boolean hasClick = comp.getStyle().getClickEvent() != null;
                boolean hasHover = comp.getStyle().getHoverEvent() != null;
                for (final Object o : comp.getFormatArgs())
                {
                    if (!(o instanceof ITextComponent)) continue;
                    final ITextComponent sub = (ITextComponent) o;
                    hasClick = sub.getStyle().getClickEvent() != null;
                    if (hasClick) style = style.setClickEvent(sub.getStyle().getClickEvent());
                    hasHover = sub.getStyle().getHoverEvent() != null;
                    if (hasHover) style = style.setHoverEvent(sub.getStyle().getHoverEvent());
                }
            }
            boolean flag = false;
            if (s.contains("\n"))
            {
                final int k = s.indexOf(10);
                final String s1 = s.substring(k + 1);
                s = s.substring(0, k + 1);
                final IFormattableTextComponent itextcomponent2 = new StringTextComponent(s1).setStyle(style);
                list1.add(j + 1, itextcomponent2);
                flag = true;
            }

            final String s4 = s;
            final String s5 = s4.endsWith("\n") ? s4.substring(0, s4.length() - 1) : s4;
            int i1 = fontRendererIn.getStringWidth(s5);
            IFormattableTextComponent itextcomponent3 = new StringTextComponent(s5).setStyle(itextcomponent1
                    .getStyle());
            if (i + i1 > maxTextLenght)
            {
                String s2 = ListHelper.trimStringToWidth(fontRendererIn, s4, maxTextLenght - i, false);
                String s3 = s2.length() < s4.length() ? s4.substring(s2.length()) : null;
                if (s3 != null && !s3.isEmpty())
                {
                    int l = s3.charAt(0) != ' ' ? s2.lastIndexOf(32) : s2.length();
                    if (l >= 0 && fontRendererIn.getStringWidth(s4.substring(0, l)) > 0)
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
                    final IFormattableTextComponent itextcomponent4 = new StringTextComponent(s3).setStyle(style);
                    list1.add(j + 1, itextcomponent4);
                }

                i1 = fontRendererIn.getStringWidth(s2);
                itextcomponent3 = new StringTextComponent(s2);
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
                remainder = new StringTextComponent("");
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
                final TextFormatting textformatting = ListHelper.fromFormattingCode(stringIn.charAt(i + 1));
                if (textformatting != null)
                {
                    if (!textformatting.isFancyStyling()) stringbuilder.setLength(0);

                    if (textformatting != TextFormatting.RESET) stringbuilder.append(textformatting);
                }
            }

        return stringbuilder.toString();
    }

    public static TextFormatting fromFormattingCode(final char formattingCodeIn)
    {
        final char c0 = Character.toString(formattingCodeIn).toLowerCase(Locale.ROOT).charAt(0);

        for (final TextFormatting textformatting : TextFormatting.values())
            if (textformatting.formattingCode == c0) return textformatting;

        return null;
    }

    public static String trimStringToWidth(final FontRenderer fontRendererIn, final String text, final int width,
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
                final TextFormatting textformatting = TextFormatting.fromFormattingCode(c0);
                if (textformatting == TextFormatting.BOLD) flag1 = true;
                else if (textformatting != null && !textformatting.isFancyStyling()) flag1 = false;
            }
            else if (c0 == 167) flag = true;
            else
            {
                f += fontRendererIn.getStringWidth(c0 + "");
                if (flag1) ++f;
            }

            if (f > width) break;

            if (reverse) stringbuilder.insert(0, c0);
            else stringbuilder.append(c0);
        }

        return stringbuilder.toString();
    }
}
