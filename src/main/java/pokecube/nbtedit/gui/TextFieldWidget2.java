package pokecube.nbtedit.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.StringTextComponent;

public class TextFieldWidget2 extends TextFieldWidget
{
    private final boolean allowSection;

    public TextFieldWidget2(final FontRenderer font, final int x, final int y, final int w, final int h,
            final boolean allowSection)
    {
        super(font, x, y, w, h, new StringTextComponent(""));
        this.allowSection = allowSection;
    }

    /** replaces selected text, or inserts text at the position on the cursor */
    @Override
    public void writeText(final String textToWrite)
    {
        String s = "";
        final String s1 = CharacterFilter.filterAllowedCharacters(textToWrite, this.allowSection);
        final int i = this.cursorPosition < this.selectionEnd ? this.cursorPosition : this.selectionEnd;
        final int j = this.cursorPosition < this.selectionEnd ? this.selectionEnd : this.cursorPosition;
        final int k = this.maxStringLength - this.text.length() - (i - j);
        if (!this.text.isEmpty()) s = s + this.text.substring(0, i);

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

        if (!this.text.isEmpty() && j < this.text.length()) s = s + this.text.substring(j);

        if (this.validator.test(s))
        {
            this.text = s;
            this.clampCursorPosition(i + l);
            this.setSelectionPos(this.cursorPosition);
            this.onTextChanged(this.text);
        }
    }
}
