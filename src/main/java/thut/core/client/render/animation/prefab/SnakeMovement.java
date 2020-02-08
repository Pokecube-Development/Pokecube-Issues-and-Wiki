package thut.core.client.render.animation.prefab;

import java.util.ArrayList;

import com.google.common.collect.Lists;

import thut.core.client.render.animation.Animation;
import thut.core.client.render.animation.AnimationComponent;
import thut.core.client.render.animation.AnimationRegistry.IPartRenamer;
import thut.core.common.ThutCore;
import thut.core.common.xml.AnimationXML.Phase;

public class SnakeMovement extends Animation
{
    public SnakeMovement()
    {
        this.loops = true;
        this.name = "walking";
    }

    public SnakeMovement init(final ArrayList<String> parts, int duration, final float maxAngle, final int axis)
    {
        duration = duration + duration % 4;
        float angle = maxAngle;
        final int num = parts.size();
        final int mod = num > 6 ? 3 : 2;
        for (int i = 0; i < parts.size(); i++)
        {
            final String ident = "";
            final String s = parts.get(i);

            angle = (float) (Math.sin(i * Math.PI / (2 * mod) - Math.PI / 2) * maxAngle);

            // Sets right wing to -start angle (up), then swings it down by
            // angle.
            final AnimationComponent component1 = new AnimationComponent();
            component1.length = duration / 4;
            component1.name = ident + "1";
            component1.identifier = ident + "1";
            component1.startKey = 0;
            component1.rotChange[axis] = angle;
            // Swings the wing from angle up to -angle. Start key is right after
            // end of 1
            final AnimationComponent component2 = new AnimationComponent();
            component2.length = duration / 2;
            component2.name = ident + "2";
            component2.identifier = ident + "2";
            component2.startKey = duration / 4;
            component2.rotChange[axis] = -2 * angle;
            // Swings the wing from -angle back down to starting angle. Start
            // key is right after end of 2
            final AnimationComponent component3 = new AnimationComponent();
            component3.length = duration / 4;
            component3.name = ident + "3";
            component3.identifier = ident + "3";
            component3.startKey = 3 * duration / 4;
            component3.rotChange[axis] = angle;

            final ArrayList<AnimationComponent> set = Lists.newArrayList();

            set.add(component1);
            set.add(component2);
            set.add(component3);
            this.sets.put(s, set);
        }
        return this;
    }

    @Override
    public Animation init(final Phase map, final IPartRenamer renamer)
    {
        final ArrayList<String> parts = new ArrayList<>();
        int duration = 40;
        int axis = 1;
        float maxAngle = 10;

        final String[] partsArr = this.get(map, "parts").split(":");

        if (renamer != null) renamer.convertToIdents(partsArr);

        for (final String s : partsArr)
            if (s != null) parts.add(s);

        if (!this.get(map, "angle").isEmpty()) maxAngle = Float.parseFloat(this.get(map, "angle"));
        if (!this.get(map, "axis").isEmpty()) axis = Integer.parseInt(this.get(map, "axis"));
        if (!this.get(map, "length").isEmpty()) duration = Integer.parseInt(this.get(map, "length"));
        if (!this.get(map, "duration").isEmpty()) duration = Integer.parseInt(this.get(map, "duration"));
        this.init(parts, duration, maxAngle, axis);
        return this;
    }
}
