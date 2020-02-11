package thut.core.client.render.animation.prefab;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import com.google.common.collect.Lists;

import thut.core.client.render.animation.Animation;
import thut.core.client.render.animation.AnimationComponent;
import thut.core.client.render.animation.AnimationRegistry.IPartRenamer;
import thut.core.client.render.animation.AnimationXML.Phase;
import thut.core.common.ThutCore;

public class AdvancedFlapAnimation extends Animation
{
    private static final QName duration = new QName("duration");

    public AdvancedFlapAnimation()
    {
        this.loops = true;
        this.name = "flying";
    }

    @Override
    public Animation init(final Phase map, @Nullable final IPartRenamer renamer)
    {
        int flapdur = 0;
        float walkAngle2 = 20;

        flapdur = Integer.parseInt(this.get(map, AdvancedFlapAnimation.duration));
        // Can have up to 255 wing segments, more than this would be silly.
        for (int i = 1; i <= 255; i++)
        {
            if (this.get(map, "leftWing" + i).isEmpty()) break;

            int flapaxis = 2;
            final float[] walkAngle1 = { 20, 20 };
            final HashSet<String> hl = new HashSet<>();
            final HashSet<String> hr = new HashSet<>();
            final String[] lh = this.get(map, "leftWing" + i).split(":");
            final String[] rh = this.get(map, "rightWing" + i).split(":");
            if (renamer != null)
            {
                renamer.convertToIdents(lh);
                renamer.convertToIdents(rh);
            }
            for (final String s : lh)
                if (s != null) hl.add(ThutCore.trim(s));
            for (final String s : rh)
                if (s != null) hr.add(ThutCore.trim(s));
            if (this.get(map, "angle" + i) != null)
            {
                final String[] args = this.get(map, "angle" + i).split(",");
                walkAngle1[0] = Float.parseFloat(args[0]);
                walkAngle1[1] = Float.parseFloat(args[1]);
            }
            if (!this.get(map, "start" + i).isEmpty()) walkAngle2 = Float.parseFloat(this.get(map, "start" + i));
            if (!this.get(map, "axis" + i).isEmpty()) flapaxis = Integer.parseInt(this.get(map, "axis" + i));
            this.init(hl, hr, flapdur, walkAngle1, walkAngle2, flapaxis, i > 1);
        }
        return this;
    }

    /**
     * Moves the wings to angle of start, then flaps up to angle, down to
     * -angle and back to start. Only the parts directly childed to the body
     * need to be added to these sets, any parts childed to them will also be
     * swung by the parent/child system. This is the first segment of the wing
     *
     * @param lw
     *            - set of left wings
     * @param rw
     *            - set of right wings
     * @param duration
     *            - time taken for entire flap.
     * @param angle
     *            - angle[0] = first stage movement, angle[1] = second stage
     *            movement.
     * @param start
     *            - initial angle moved to to start flapping
     * @param axis
     *            - axis used for flapping around.
     * @param reverse
     *            - should only be false for the first section of the wing.
     * @return
     */
    public AdvancedFlapAnimation init(final Set<String> lw, final Set<String> rw, int duration, final float[] angle,
            final float start, final int axis, final boolean reverse)
    {
        duration = duration + duration % 4;
        final int dir = reverse ? -1 : 1;

        for (final String s : rw)
        {
            final String ident = "";
            // Sets right wing to -start angle (up), then swings it down by
            // angle.
            final AnimationComponent component1 = new AnimationComponent();
            component1.length = duration / 4;
            component1.name = ident + "1";
            component1.identifier = ident + "1";
            component1.startKey = 0;
            component1.rotOffset[axis] = -start;
            component1.rotChange[axis] = angle[0];
            // Swings the wing from angle up to -angle. Start key is right after
            // end of 1
            final AnimationComponent component2 = new AnimationComponent();
            component2.length = duration / 2;
            component2.name = ident + "2";
            component2.identifier = ident + "2";
            component2.startKey = duration / 4;
            component2.rotChange[axis] = -(angle[1] + angle[0]);
            // Swings the wing from -angle back down to starting angle. Start
            // key is right after end of 2
            final AnimationComponent component3 = new AnimationComponent();
            component3.length = duration / 4;
            component3.name = ident + "3";
            component3.identifier = ident + "3";
            component3.startKey = 3 * duration / 4;
            component3.rotChange[axis] = angle[1];

            final ArrayList<AnimationComponent> set = Lists.newArrayList();

            component1.limbBased = true;
            component2.limbBased = true;
            component3.limbBased = true;
            set.add(component1);
            set.add(component2);
            set.add(component3);
            this.sets.put(s, set);
        }
        // Angles and timing are same numbers for Right Wings, but angles are
        // reversed, as are opposite sides.
        for (final String s : lw)
        {
            final String ident = "";
            final AnimationComponent component1 = new AnimationComponent();
            component1.length = duration / 4;
            component1.name = ident + "1";
            component1.identifier = ident + "1";
            component1.startKey = 0;
            component1.rotOffset[axis] = start;
            component1.rotChange[axis] = dir * -angle[0];

            final AnimationComponent component2 = new AnimationComponent();
            component2.length = duration / 2;
            component2.name = ident + "2";
            component2.identifier = ident + "2";
            component2.startKey = duration / 4;
            component2.rotChange[axis] = dir * (angle[1] + angle[0]);

            final AnimationComponent component3 = new AnimationComponent();
            component3.length = duration / 4;
            component3.name = ident + "3";
            component3.identifier = ident + "3";
            component3.startKey = 3 * duration / 4;
            component3.rotChange[axis] = dir * -angle[1];

            final ArrayList<AnimationComponent> set = Lists.newArrayList();

            component1.limbBased = true;
            component2.limbBased = true;
            component3.limbBased = true;
            set.add(component1);
            set.add(component2);
            set.add(component3);
            this.sets.put(s, set);
        }
        return this;
    }
}
