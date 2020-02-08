package thut.core.client.render.animation;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import org.w3c.dom.NamedNodeMap;

import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import thut.core.client.render.animation.AnimationRegistry.IPartRenamer;
import thut.core.common.xml.AnimationXML.Phase;

/** Container for Tabula animations.
 *
 * @author Gegy1000
 * @since 0.1.0 */
@OnlyIn(Dist.CLIENT)
public class Animation
{
    public final UUID                                     id         = UUID.randomUUID();

    public String                                         name       = "";
    public String                                         identifier = "";
    public int                                            length     = -1;
    /** This is used for sorting animations for determining which components
     * should take priority when multiple animations are specified for a single
     * part. */
    public int                                            priority   = 10;

    public boolean                                        loops      = true;

    private final Set<String>                             checked    = Sets.newHashSet();

    public TreeMap<String, ArrayList<AnimationComponent>> sets = new TreeMap<>(Ordering.natural());

    public ArrayList<AnimationComponent> getComponents(final String key)
    {
        if (!this.checked.contains(key))
        {
            ArrayList<AnimationComponent> comps = null;
            for (final String s : this.sets.keySet())
                if (s.startsWith("*") && key.matches(s.substring(1)))
                {
                    comps = this.sets.get(s);
                    break;
                }
            if (comps != null) this.sets.put(key, comps);
            this.checked.add(key);
        }
        return this.sets.get(key);
    }

    public int getLength()
    {
        if (this.length == -1) this.initLength();
        return this.length;
    }

    public Animation init(final NamedNodeMap map, @Nullable final IPartRenamer renamer)
    {
        return this;
    }

    public Animation init(final Phase tag, @Nullable final IPartRenamer renamer)
    {
        return this;
    }

    protected String get(final Phase phase, final QName value)
    {
        return phase.values.getOrDefault(value, "");
    }

    protected String get(final Phase phase, final String value)
    {
        return phase.values.getOrDefault(new QName(value), "");
    }

    public void initLength()
    {
        this.length = -1;
        for (final Entry<String, ArrayList<AnimationComponent>> entry : this.sets.entrySet())
            for (final AnimationComponent component : entry.getValue())
                this.length = Math.max(this.length, component.startKey + component.length);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Animation) return this.id.equals(((Animation) obj).id);
        return false;
    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }

    @Override
    public String toString()
    {
        return this.name + "|" + this.identifier + "|" + this.loops + "|" + this.getLength();
    }
}
