package thut.core.client.render.animation;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import com.google.common.collect.Ordering;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import thut.core.client.render.animation.AnimationRegistry.IPartRenamer;
import thut.core.client.render.animation.AnimationXML.Phase;

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

    public TreeMap<String, ArrayList<AnimationComponent>> sets = new TreeMap<>(Ordering.natural());

    public ArrayList<AnimationComponent> getComponents(final String key)
    {
        return this.sets.get(key);
    }

    public int getLength()
    {
        if (this.length == -1) this.initLength();
        return this.length;
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
