package pokecube.adventures.events;

import net.minecraftforge.eventbus.api.Event;
import thut.api.entity.genetics.IMobGenetics;

public class GeneEditEvent extends Event
{

    public static enum EditType
    {
        SPLICE, EXTRACT, OTHER;
    }

    public final IMobGenetics resultGenes;
    public final EditType     reason;

    public GeneEditEvent(final IMobGenetics resultGenes, final EditType reason)
    {
        this.resultGenes = resultGenes;
        this.reason = reason;
    }
}
