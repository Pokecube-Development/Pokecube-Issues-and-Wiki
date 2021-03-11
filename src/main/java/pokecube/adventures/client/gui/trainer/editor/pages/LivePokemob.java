package pokecube.adventures.client.gui.trainer.editor.pages;

import pokecube.adventures.client.gui.trainer.editor.EditorGui;

public class LivePokemob extends Pokemob
{

    public LivePokemob(final EditorGui parent)
    {
        super(parent);
        this.pokemob = parent.pokemob;
        this.closeCallback = () -> this.onClose();
    }

}
