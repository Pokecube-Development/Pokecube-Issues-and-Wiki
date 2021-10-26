package pokecube.nbtedit.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.TextComponent;

public class TextFieldWidget2 extends EditBox
{
    private final boolean allowSection;

    public TextFieldWidget2(final Font font, final int x, final int y, final int w, final int h,
            final boolean allowSection)
    {
        super(font, x, y, w, h, new TextComponent(""));
        this.allowSection = allowSection;
    }

    /** replaces selected text, or inserts text at the position on the cursor */
    @Override
    public void insertText(final String textToWrite)
    {
        String s = "";
        final String s1 = CharacterFilter.filterAllowedCharacters(textToWrite, this.allowSection);
        final int i = this.cursorPos < this.highlightPos ? this.cursorPos : this.highlightPos;
        final int j = this.cursorPos < this.highlightPos ? this.highlightPos : this.cursorPos;
        final int k = this.maxLength - this.value.length() - (i - j);
        if (!this.value.isEmpty()) s = s + this.value.substring(0, i);

        int l;
        if (k < s1.length())
        {
            s = s + s1.substring(0, k);
            l = k;
        }
        else
        {
            s = s + s1;
            l = s1.length();
        }

        if (!this.value.isEmpty() && j < this.value.length()) s = s + this.value.substring(j);

        if (this.filter.test(s))
        {
            this.value = s;
            this.setCursorPosition(i + l);
            this.setHighlightPos(this.cursorPos);
            this.onValueChange(this.value);
        }
    }
}
