package thut.core.client.render.animation;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import javax.annotation.Nullable;

import org.w3c.dom.NamedNodeMap;

import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import thut.core.client.render.animation.AnimationRegistry.IPartRenamer;

/**
 * Container for Tabula animations.
 *
 * @author Gegy1000
 * @since 0.1.0
 */
@OnlyIn(Dist.CLIENT)
public class Animation
{
    public final UUID id = UUID.randomUUID();

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

    private final Set<String> checked = Sets.newHashSet();

    public TreeMap<String, ArrayList<AnimationComponent>> sets = new TreeMap<>(
            Ordering.natural());

    public ArrayList<AnimationComponent> getComponents(String key)
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

    public Animation init(NamedNodeMap map, @Nullable IPartRenamer renamer)
    {
        return this;
    }

    public void initLength()
    {
        this.length = -1;
        for (final Entry<String, ArrayList<AnimationComponent>> entry : this.sets.entrySet())
            for (final AnimationComponent component : entry.getValue())
                this.length = Math.max(this.length, component.startKey + component.length);
    }

    @Override
    public String toString()
    {
        return this.name + "|" + this.identifier + "|" + this.loops + "|" + this.getLength();
    }
}
