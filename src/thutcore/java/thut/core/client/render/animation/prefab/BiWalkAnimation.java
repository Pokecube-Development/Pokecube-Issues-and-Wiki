package thut.core.client.render.animation.prefab;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Nullable;

import thut.api.entity.animation.Animation;
import thut.api.entity.animation.AnimationComponent;
import thut.api.entity.animation.Animators;
import thut.api.entity.animation.Animators.KeyframeAnimator;
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
        for (final String s : lh) if (s != null) hl.add(ThutCore.trim(s));
        for (final String s : rh) if (s != null) hr.add(ThutCore.trim(s));
        for (final String s : rf) if (s != null) fr.add(ThutCore.trim(s));
        for (final String s : lf) if (s != null) fl.add(ThutCore.trim(s));
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
     * @param hl       - left legs
     * @param hr       - right legs
     * @param fl       - left arms
     * @param fr       - right arms
     * @param duration - time taken for animation
     * @param legAngle - half - angle covered by legs.
     * @param armAngle - half - angle covered by arms.
     * @return
     */
    public BiWalkAnimation init(final Set<String> hl, final Set<String> hr, final Set<String> fl, final Set<String> fr,
            int duration, final float legAngle, final float armAngle, final int legAxis, final int armAxis)
    {
        String phase1 = "x:%f*cos(0.05*l*%d)";
        String phase2 = "x:-%f*cos(0.05*l*%d)";
        String phase3 = "x:%f*sin(0.05*l*%d)";
        String phase4 = "x:-%f*sin(0.05*l*%d)";

        for (final String s : hr)
        {
            try
            {
                AnimationComponent comp = new AnimationComponent();
                String[] rots = comp._rotFunctions;
                String exp = String.format(Locale.ROOT, phase1, legAngle, duration);
                Animators.fillJEPs(rots, exp);
                this.sets.put(s, new KeyframeAnimator(comp));
                continue;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        for (final String s : hl)
        {
            try
            {
                AnimationComponent comp = new AnimationComponent();
                String[] rots = comp._rotFunctions;
                String exp = String.format(Locale.ROOT, phase2, legAngle, duration);
                Animators.fillJEPs(rots, exp);
                this.sets.put(s, new KeyframeAnimator(comp));
                continue;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        for (final String s : fr)
        {
            try
            {
                AnimationComponent comp = new AnimationComponent();
                String[] rots = comp._rotFunctions;
                String exp = String.format(Locale.ROOT, phase3, legAngle, duration);
                Animators.fillJEPs(rots, exp);
                this.sets.put(s, new KeyframeAnimator(comp));
                continue;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        for (final String s : fl)
        {
            try
            {
                AnimationComponent comp = new AnimationComponent();
                String[] rots = comp._rotFunctions;
                String exp = String.format(Locale.ROOT, phase4, legAngle, duration);
                Animators.fillJEPs(rots, exp);
                this.sets.put(s, new KeyframeAnimator(comp));
                continue;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        for (final var set : this.sets.values()) set.setLimbBased();
        return this;
    }
}
