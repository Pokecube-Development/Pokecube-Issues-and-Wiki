package thut.core.client.render.animation.prefab;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Lists;

import thut.core.client.render.animation.Animation;
import thut.core.client.render.animation.AnimationComponent;
import thut.core.client.render.animation.AnimationRegistry.IPartRenamer;
import thut.core.common.xml.AnimationXML.Phase;

public class QuadWalkAnimation extends Animation
{
    public QuadWalkAnimation()
    {
        this.loops = true;
        this.name = "walking";
    }

    @Override
    public Animation init(final Phase map, final IPartRenamer renamer)
    {
        final HashSet<String> hl = new HashSet<>();
        final HashSet<String> hr = new HashSet<>();
        final HashSet<String> fl = new HashSet<>();
        final HashSet<String> fr = new HashSet<>();
        int quadwalkdur = 0;
        float walkAngle1 = 20;
        float walkAngle2 = 20;
        final String[] lh = this.get(map, "leftHind").split(":");
        final String[] rh = this.get(map, "rightHind").split(":");
        final String[] lf = this.get(map, "leftFront").split(":");
        final String[] rf = this.get(map, "rightFront").split(":");

        if (renamer != null)
        {
            renamer.convertToIdents(lh);
            renamer.convertToIdents(rh);
            renamer.convertToIdents(lf);
            renamer.convertToIdents(rf);
        }
        for (final String s : lh)
            if (s != null) hl.add(s);
        for (final String s : rh)
            if (s != null) hr.add(s);
        for (final String s : rf)
            if (s != null) fr.add(s);
        for (final String s : lf)
            if (s != null) fl.add(s);
        if (!this.get(map, "angle").isEmpty()) walkAngle1 = Float.parseFloat(this.get(map, "angle"));
        if (!this.get(map, "frontAngle").isEmpty()) walkAngle2 = Float.parseFloat(this.get(map, "frontAngle"));
        else walkAngle2 = walkAngle1;
        quadwalkdur = Integer.parseInt(this.get(map, "duration"));

        this.init(hl, hr, fl, fr, quadwalkdur, walkAngle1, walkAngle2);
        return this;
    }

    /**
     * Swings legs and arms in opposite directions. Only the parts directly
     * childed to the body need to be added to these sets, any parts childed to
     * them will also be swung by the parent/child system.
     *
     * @param hl
     *            - left hind legs
     * @param hr
     *            - right hind legs
     * @param fl
     *            - left front legs
     * @param fr
     *            - right front legs
     * @param duration
     *            - time taken for animation
     * @param legAngle
     *            - half - angle covered by hind legs.
     * @param armAngle
     *            - half - angle covered by front legs.
     * @return
     */
    public QuadWalkAnimation init(final Set<String> hl, final Set<String> hr, final Set<String> fl,
            final Set<String> fr, int duration, final float legAngle, final float armAngle)
    {
        duration = duration + duration % 4;
        for (final String s : hr)
        {
            final String ident = "";
            final AnimationComponent component1 = new AnimationComponent();
            component1.length = duration / 4;
            component1.name = ident + "1";
            component1.identifier = ident + "1";
            component1.startKey = 0;
            component1.rotChange[0] = legAngle;

            final AnimationComponent component2 = new AnimationComponent();
            component2.length = duration / 2;
            component2.name = ident + "2";
            component2.identifier = ident + "2";
            component2.startKey = duration / 4;
            component2.rotChange[0] = -2 * legAngle;

            final AnimationComponent component3 = new AnimationComponent();
            component3.length = duration / 4;
            component3.name = ident + "3";
            component3.identifier = ident + "3";
            component3.startKey = 3 * duration / 4;
            component3.rotChange[0] = legAngle;

            final ArrayList<AnimationComponent> set = Lists.newArrayList();

            set.add(component1);
            set.add(component2);
            set.add(component3);
            this.sets.put(s, set);
        }
        for (final String s : hl)
        {
            final String ident = "";
            final AnimationComponent component1 = new AnimationComponent();
            component1.length = duration / 4;
            component1.name = ident + "1";
            component1.identifier = ident + "1";
            component1.startKey = 0;
            component1.rotChange[0] = -legAngle;

            final AnimationComponent component2 = new AnimationComponent();
            component2.length = duration / 2;
            component2.name = ident + "2";
            component2.identifier = ident + "2";
            component2.startKey = duration / 4;
            component2.rotChange[0] = 2 * legAngle;

            final AnimationComponent component3 = new AnimationComponent();
            component3.length = duration / 4;
            component3.name = ident + "3";
            component3.identifier = ident + "3";
            component3.startKey = 3 * duration / 4;
            component3.rotChange[0] = -legAngle;

            final ArrayList<AnimationComponent> set = Lists.newArrayList();

            set.add(component1);
            set.add(component2);
            set.add(component3);
            this.sets.put(s, set);
        }
        for (final String s : fr)
        {
            final String ident = "";
            final AnimationComponent component1 = new AnimationComponent();
            component1.length = duration / 4;
            component1.name = ident + "1";
            component1.identifier = ident + "1";
            component1.startKey = 0;
            component1.rotChange[0] = armAngle;

            final AnimationComponent component2 = new AnimationComponent();
            component2.length = duration / 2;
            component2.name = ident + "2";
            component2.identifier = ident + "2";
            component2.startKey = duration / 4;
            component2.rotChange[0] = -2 * armAngle;

            final AnimationComponent component3 = new AnimationComponent();
            component3.length = duration / 4;
            component3.name = ident + "3";
            component3.identifier = ident + "3";
            component3.startKey = 3 * duration / 4;
            component3.rotChange[0] = armAngle;

            final ArrayList<AnimationComponent> set = Lists.newArrayList();

            set.add(component1);
            set.add(component2);
            set.add(component3);
            this.sets.put(s, set);
        }
        for (final String s : fl)
        {
            final String ident = "";
            final AnimationComponent component1 = new AnimationComponent();
            component1.length = duration / 4;
            component1.name = ident + "1";
            component1.identifier = ident + "1";
            component1.startKey = 0;
            component1.rotChange[0] = -armAngle;

            final AnimationComponent component2 = new AnimationComponent();
            component2.length = duration / 2;
            component2.name = ident + "2";
            component2.identifier = ident + "2";
            component2.startKey = duration / 4;
            component2.rotChange[0] = 2 * armAngle;

            final AnimationComponent component3 = new AnimationComponent();
            component3.length = duration / 4;
            component3.name = ident + "3";
            component3.identifier = ident + "3";
            component3.startKey = 3 * duration / 4;
            component3.rotChange[0] = -armAngle;

            final ArrayList<AnimationComponent> set = Lists.newArrayList();

            set.add(component1);
            set.add(component2);
            set.add(component3);
            this.sets.put(s, set);
        }
        for (final ArrayList<AnimationComponent> set : this.sets.values())
            for (final AnimationComponent c : set)
                c.limbBased = true;
        return this;
    }
}
