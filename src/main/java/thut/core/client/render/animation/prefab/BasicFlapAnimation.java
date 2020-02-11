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

public class BasicFlapAnimation extends Animation
{
    private static final QName leftWing  = new QName("leftWing");
    private static final QName rightWing = new QName("rightWing");
    private static final QName angle     = new QName("angle");
    private static final QName start     = new QName("start");
    private static final QName axis      = new QName("axis");
    private static final QName duration  = new QName("duration");

    public BasicFlapAnimation()
    {
        this.loops = true;
        this.name = "flying";
    }

    @Override
    public Animation init(final Phase map, @Nullable final IPartRenamer renamer)
    {
        final HashSet<String> hl = new HashSet<>();
        final HashSet<String> hr = new HashSet<>();
        int flapdur = 0;
        int flapaxis = 2;
        float walkAngle1 = 20;
        float walkAngle2 = 20;
        flapdur = Integer.parseInt(this.get(map, BasicFlapAnimation.duration));

        final String[] lh = this.get(map, BasicFlapAnimation.leftWing).split(":");
        final String[] rh = this.get(map, BasicFlapAnimation.rightWing).split(":");

        if (renamer != null)
        {
            renamer.convertToIdents(lh);
            renamer.convertToIdents(rh);
        }
        for (final String s : lh)
            if (s != null) hl.add(ThutCore.trim(s));
        for (final String s : rh)
            if (s != null) hr.add(ThutCore.trim(s));
        final String angle = this.get(map, BasicFlapAnimation.angle);
        final String start = this.get(map, BasicFlapAnimation.start);
        final String axis = this.get(map, BasicFlapAnimation.axis);
        if (!angle.isEmpty()) walkAngle1 = Float.parseFloat(angle);
        if (!start.isEmpty()) walkAngle2 = Float.parseFloat(start);
        if (!axis.isEmpty()) flapaxis = Integer.parseInt(axis);

        this.init(hl, hr, flapdur, walkAngle1, walkAngle2, flapaxis);
        return this;
    }

    /**
     * Moves the wings to angle of start, then flaps up to angle, down to
     * -angle and back to start. Only the parts directly childed to the body
     * need to be added to these sets, any parts childed to them will also be
     * swung by the parent/child system.
     *
     * @param lw
     *            - set of left wings
     * @param rw
     *            - set of right wings
     * @param duration
     *            - time taken for entire flap.
     * @param angle
     *            - half - angle flapped over
     * @param start
     *            - initial angle moved to to start flapping
     * @param axis
     *            - axis used for flapping around.
     * @return
     */
    public BasicFlapAnimation init(final Set<String> lw, final Set<String> rw, int duration, final float angle,
            final float start, final int axis)
    {
        duration = duration + duration % 4;
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
            component1.rotChange[axis] = -angle;

            final AnimationComponent component2 = new AnimationComponent();
            component2.length = duration / 2;
            component2.name = ident + "2";
            component2.identifier = ident + "2";
            component2.startKey = duration / 4;
            component2.rotChange[axis] = 2 * angle;

            final AnimationComponent component3 = new AnimationComponent();
            component3.length = duration / 4;
            component3.name = ident + "3";
            component3.identifier = ident + "3";
            component3.startKey = 3 * duration / 4;
            component3.rotChange[axis] = -angle;

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
