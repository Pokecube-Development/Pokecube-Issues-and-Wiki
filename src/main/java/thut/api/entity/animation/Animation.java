package thut.api.entity.animation;

import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import thut.api.entity.animation.Animators.IAnimator;
import thut.api.maths.Vector3;
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

    private UUID id = UUID.randomUUID();;

    public UUID _uuid = id;

    public String name = "";
    public String identifier = "";
    public float length = -1;
    /**
     * This is used for sorting animations for determining which components
     * should take priority when multiple animations are specified for a single
     * part.
     */
    public int priority = 10;

    public boolean loops = true;
    public boolean holdWhenDone = false;

    public boolean hasLimbBased = false;

    public Vector3 _shift = new Vector3();

    public Map<String, IAnimator> sets = new Object2ObjectOpenHashMap<>();

    public IAnimator getComponents(final String key)
    {
        return this.sets.get(key);
    }

    public float getLength()
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
        for (var entry : this.sets.entrySet())
        {
            this.length = Math.max(this.length, entry.getValue().getLength());
            this.hasLimbBased = this.hasLimbBased || entry.getValue().hasLimbBased();
        }
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this.id == null) this.hashCode();
        if (obj instanceof Animation anim) return anim.id.equals(this.id);
        return super.equals(obj);
    }

    @Override
    public int hashCode()
    {
        if (this.id == null)
        {
            if (this.identifier.isEmpty()) this.identifier = this.name;
            this.id = new UUID(this.identifier.hashCode(), (this.identifier.hashCode() << 16) + (int) this.getLength());
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
