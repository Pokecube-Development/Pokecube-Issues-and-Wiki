package pokecube.nbtedit.gui;

import net.minecraft.SharedConstants;
import pokecube.nbtedit.NBTStringHelper;

public class CharacterFilter
{
    public static String filterAllowedCharacters(final String str, final boolean section)
    {
        final StringBuilder sb = new StringBuilder();
        final char[] arr = str.toCharArray();
        for (final char c : arr)
            if (SharedConstants.isAllowedChatCharacter(c) || section && (c == NBTStringHelper.SECTION_SIGN || c == '\n')) sb
                    .append(c);
        return sb.toString();
    }
}
