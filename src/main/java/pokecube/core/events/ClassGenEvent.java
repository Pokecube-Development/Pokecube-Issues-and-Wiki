package pokecube.core.events;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import net.minecraftforge.eventbus.api.Event;
import pokecube.core.database.PokedexEntry;

/**
 * This is event is sent for each generic pokemob class generated.<br>
 * It is mostly sent during the pre-init phase, when the main classes are being
 * made.<br>
 * It is sent right after the node has accepted the classwriter.
 */
public class ClassGenEvent extends Event
{
    public final ClassWriter  writer;
    public final ClassNode    node;
    public final PokedexEntry pokedexEntry;

    public ClassGenEvent(ClassWriter cw, ClassNode n, PokedexEntry name)
    {
        this.writer = cw;
        this.pokedexEntry = name;
        this.node = n;
    }

}
