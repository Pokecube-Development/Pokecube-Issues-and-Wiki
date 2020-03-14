package thut.core.client.render.animation.prefab;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import thut.core.client.render.animation.Animation;
import thut.core.client.render.animation.AnimationComponent;
import thut.core.client.render.animation.AnimationRegistry.IPartRenamer;
import thut.core.client.render.animation.AnimationXML.Phase;
import thut.core.common.ThutCore;

public class BiWalkAnimation extends Animation
{
    public BiWalkAnimation()
    {
        this.loops = true;
        this.name = "walking";
    }

    @Override
    public Animation init(final Phase map, @Nullable final IPartRenamer renamer)
    {
        final HashSet<String> hl = new HashSet<>();
        final HashSet<String> hr = new HashSet<>();
        final HashSet<String> fl = new HashSet<>();
        final HashSet<String> fr = new HashSet<>();
        int biwalkdur = 0;
        float walkAngle1 = 20;
        float walkAngle2 = 20;
        final String[] lh = this.get(map, "leftLeg").split(":");
        final String[] rh = this.get(map, "rightLeg").split(":");
        final String[] lf = this.get(map, "leftArm").split(":");
        final String[] rf = this.get(map, "rightArm").split(":");

        if (renamer != null)
        {
            renamer.convertToIdents(lh);
            renamer.convertToIdents(rh);
            renamer.convertToIdents(lf);
            renamer.convertToIdents(rf);
        }
        for (final String s : lh)
            if (s != null) hl.add(ThutCore.trim(s));
        for (final String s : rh)
            if (s != null) hr.add(ThutCore.trim(s));
        for (final String s : rf)
            if (s != null) fr.add(ThutCore.trim(s));
        for (final String s : lf)
            if (s != null) fl.add(ThutCore.trim(s));
        biwalkdur = Integer.parseInt(this.get(map, "duration"));
        int armAxis = 0;
        int legAxis = 0;
        if (!this.get(map, "legAngle").isEmpty()) walkAngle1 = Float.parseFloat(this.get(map, "legAngle"));
        if (!this.get(map, "armAngle").isEmpty()) walkAngle2 = Float.parseFloat(this.get(map, "armAngle"));
        if (!this.get(map, "legAxis").isEmpty()) legAxis = Integer.parseInt(this.get(map, "legAxis"));
        if (!this.get(map, "armAxis").isEmpty()) armAxis = Integer.parseInt(this.get(map, "armAxis"));
        this.init(hl, hr, fl, fr, biwalkdur, walkAngle1, walkAngle2, legAxis, armAxis);
        return this;
    }

    /**
     * Swings legs and arms in opposite directions. Only the parts directly
     * childed to the body need to be added to these sets, any parts childed to
     * them will also be swung by the parent/child system.
     *
     * @param hl
     *            - left legs
     * @param hr
     *            - right legs
     * @param fl
     *            - left arms
     * @param fr
     *            - right arms
     * @param duration
     *            - time taken for animation
     * @param legAngle
     *            - half - angle covered by legs.
     * @param armAngle
     *            - half - angle covered by arms.
     * @return
     */
    public BiWalkAnimation init(final Set<String> hl, final Set<String> hr, final Set<String> fl, final Set<String> fr,
            int duration, final float legAngle, final float armAngle, final int legAxis, final int armAxis)
    {
        duration = duration + duration % 4;
        for (final String s : hr)
        {
            final String ident = "hr";
            final AnimationComponent component1 = new AnimationComponent();
            component1.length = duration / 4;
            component1.name = ident + "1";
            component1.identifier = ident + "1";
            component1.startKey = 0;
            component1.rotChange[legAxis] = legAngle;

            final AnimationComponent component2 = new AnimationComponent();
            component2.length = duration / 2;
            component2.name = ident + "2";
            component2.identifier = ident + "2";
            component2.startKey = duration / 4;
            component2.rotChange[legAxis] = -2 * legAngle;

            final AnimationComponent component3 = new AnimationComponent();
            component3.length = duration / 4;
            component3.name = ident + "3";
            component3.identifier = ident + "3";
            component3.startKey = 3 * duration / 4;
            component3.rotChange[legAxis] = legAngle;

            final ArrayList<AnimationComponent> set = Lists.newArrayList();

            component1.limbBased = true;
            component2.limbBased = true;
            component3.limbBased = true;
            set.add(component1);
            set.add(component2);
            set.add(component3);
            this.sets.put(s, set);
        }
        for (final String s : hl)
        {
            final String ident = "hl";
            final AnimationComponent component1 = new AnimationComponent();
            component1.length = duration / 4;
            component1.name = ident + "1";
            component1.identifier = ident + "1";
            component1.startKey = 0;
            component1.rotChange[legAxis] = -legAngle;

            final AnimationComponent component2 = new AnimationComponent();
            component2.length = duration / 2;
            component2.name = ident + "2";
            component2.identifier = ident + "2";
            component2.startKey = duration / 4;
            component2.rotChange[legAxis] = 2 * legAngle;

            final AnimationComponent component3 = new AnimationComponent();
            component3.length = duration / 4;
            component3.name = ident + "3";
            component3.identifier = ident + "3";
            component3.startKey = 3 * duration / 4;
            component3.rotChange[legAxis] = -legAngle;

            final ArrayList<AnimationComponent> set = Lists.newArrayList();

            component1.limbBased = true;
            component2.limbBased = true;
            component3.limbBased = true;
            set.add(component1);
            set.add(component2);
            set.add(component3);
            this.sets.put(s, set);
        }
        for (final String s : fr)
        {
            final String ident = "fr";
            final AnimationComponent component1 = new AnimationComponent();
            component1.length = duration / 4;
            component1.name = ident + "1";
            component1.identifier = ident + "1";
            component1.startKey = 0;
            component1.rotChange[armAxis] = armAngle;

            final AnimationComponent component2 = new AnimationComponent();
            component2.length = duration / 2;
            component2.name = ident + "2";
            component2.identifier = ident + "2";
            component2.startKey = duration / 4;
            component2.rotChange[armAxis] = -2 * armAngle;

            final AnimationComponent component3 = new AnimationComponent();
            component3.length = duration / 4;
            component3.name = ident + "3";
            component3.identifier = ident + "3";
            component3.startKey = 3 * duration / 4;
            component3.rotChange[armAxis] = armAngle;

            final ArrayList<AnimationComponent> set = Lists.newArrayList();

            component1.limbBased = true;
            component2.limbBased = true;
            component3.limbBased = true;
            set.add(component1);
            set.add(component2);
            set.add(component3);
            this.sets.put(s, set);
        }
        for (final String s : fl)
        {
            final String ident = "fl";
            final AnimationComponent component1 = new AnimationComponent();
            component1.length = duration / 4;
            component1.name = ident + "1";
            component1.identifier = ident + "1";
            component1.startKey = 0;
            component1.rotChange[armAxis] = -armAngle;

            final AnimationComponent component2 = new AnimationComponent();
            component2.length = duration / 2;
            component2.name = ident + "2";
            component2.identifier = ident + "2";
            component2.startKey = duration / 4;
            component2.rotChange[armAxis] = 2 * armAngle;

            final AnimationComponent component3 = new AnimationComponent();
            component3.length = duration / 4;
            component3.name = ident + "3";
            component3.identifier = ident + "3";
            component3.startKey = 3 * duration / 4;
            component3.rotChange[armAxis] = -armAngle;

            final ArrayList<AnimationComponent> set = Lists.newArrayList();

            component1.limbBased = true;
            component2.limbBased = true;
            component3.limbBased = true;
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
