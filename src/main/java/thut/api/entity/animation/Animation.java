package thut.api.entity.animation;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import com.google.common.collect.Ordering;

import thut.core.client.render.animation.AnimationXML.Phase;

public class Animation
{
    /**
     * Used to convert from part names to identifiers if needed.
     *
     * @author Thutmose
     */
    public static interface IPartRenamer
    {
        void convertToIdents(String[] names);
    }

    private UUID id;

    public UUID _uuid = UUID.randomUUID();

    public String name       = "";
    public String identifier = "";
    public int    length     = -1;
    /**
     * This is used for sorting animations for determining which components
     * should take priority when multiple animations are specified for a single
     * part.
     */
    public int    priority   = 10;

    public boolean loops = true;

    public boolean hasLimbBased = false;

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
        this.hasLimbBased = false;
        for (final Entry<String, ArrayList<AnimationComponent>> entry : this.sets.entrySet())
            for (final AnimationComponent component : entry.getValue())
            {
                this.length = Math.max(this.length, component.startKey + component.length);
                this.hasLimbBased = this.hasLimbBased || component.limbBased;
            }
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this.id == null)
        {
            if (this.identifier.isEmpty()) this.identifier = this.name;
            this.id = new UUID(this.identifier.hashCode(), (this.identifier.hashCode() << 16) + this.getLength());
        }
        if (obj instanceof Animation) return ((Animation) obj).id.equals(this.id);
        return super.equals(obj);
    }

    @Override
    public int hashCode()
    {
        if (this.id == null)
        {
            if (this.identifier.isEmpty()) this.identifier = this.name;
            this.id = new UUID(this.identifier.hashCode(), (this.identifier.hashCode() << 16) + this.getLength());
        }
        return this.id.hashCode();
    }

    @Override
    public String toString()
    {
        this.length = this.getLength();
        return this.name + "|" + this.identifier + "|" + this.loops + "|" + this.length;
    }
}
