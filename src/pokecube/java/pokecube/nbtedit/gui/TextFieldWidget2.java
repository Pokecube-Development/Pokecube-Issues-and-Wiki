package pokecube.nbtedit.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class TextFieldWidget2 extends EditBox
{
    private final boolean allowSection;

    public TextFieldWidget2(final Font font, final int x, final int y, final int w, final int h,
            final boolean allowSection)
    {
        super(font, x, y, w, h, Component.literal(""));
        this.allowSection = allowSection;
    }

    /** replaces selected text, or inserts text at the position on the cursor */
    @Override
    public void insertText(final String textToWrite)
    {
        String s = "";
        final String s1 = CharacterFilter.filterAllowedCharacters(textToWrite, this.allowSection);
        final int i = Math.min(this.getCursorPosition(), this.highlightPos);
        final int j = Math.max(this.getCursorPosition(), this.highlightPos);
        final int k = this.getCursorPosition();
        if (!this.getValue().isEmpty()) s = s + this.getValue().substring(0, i);

        int l;
        if (s.isEmpty())
        {
            s = textToWrite;
            l = textToWrite.length();
        }
        else if (k < s1.length())
        {
            s = s + s1.substring(0, k);
            l = k;
        }
        else
        {
            s = s + s1;
            l = s1.length();
        }

        if (!this.getValue().isEmpty() && j < this.getValue().length()) s = s + this.getValue().substring(j);

        if (this.filter.test(s))
        {
            this.value = s;
            this.setCursorPosition(i + l);
            this.setHighlightPos(this.getCursorPosition());
            this.onValueChange(this.value);
        }
    }
}
