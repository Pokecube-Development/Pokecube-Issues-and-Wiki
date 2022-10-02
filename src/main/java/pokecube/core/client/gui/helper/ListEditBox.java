package pokecube.core.client.gui.helper;

import java.util.function.Consumer;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ListEditBox extends EditBox
{
    public static class ListPreFocusGain implements Consumer<EditBox>
    {
        Screen screen;

        public ListPreFocusGain(Screen parent)
        {
            this.screen = parent;
        }

        @Override
        public void accept(EditBox t)
        {
            screen.children.forEach(s -> {
                if (s instanceof EditBox b && b != t) b.setFocused(false);
            });
        }
    }

    public Consumer<EditBox> preFocusGain;

    public ListEditBox(Font p_94106_, int p_94107_, int p_94108_, int p_94109_, int p_94110_, EditBox p_94111_,
            Component p_94112_)
    {
        super(p_94106_, p_94107_, p_94108_, p_94109_, p_94110_, p_94111_, p_94112_);
    }

    public ListEditBox(Font p_94114_, int p_94115_, int p_94116_, int p_94117_, int p_94118_, Component p_94119_)
    {
        super(p_94114_, p_94115_, p_94116_, p_94117_, p_94118_, p_94119_);
    }

    public ListEditBox registerPreFocus(Consumer<EditBox> preFocusGain)
    {
        this.preFocusGain = preFocusGain;
        return this;
    }

    public ListEditBox registerPreFocus(Screen parent)
    {
        this.preFocusGain = new ListPreFocusGain(parent);
        return this;
    }

    @Override
    public void setFocused(boolean newFocus)
    {
        if (newFocus != this.isFocused() && newFocus && preFocusGain != null)
        {
            preFocusGain.accept(this);
        }
        super.setFocused(newFocus);
    }
}
