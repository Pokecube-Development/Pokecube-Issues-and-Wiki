package thut.core.client.render.animation.prefab;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import thut.api.entity.animation.Animation;
import thut.api.entity.animation.AnimationComponent;
import thut.api.entity.animation.Animators;
import thut.api.entity.animation.Animators.KeyframeAnimator;
import thut.core.client.render.animation.AnimationXML.Phase;
import thut.core.common.ThutCore;

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
        for (final String s : lh) if (s != null) hl.add(ThutCore.trim(s));
        for (final String s : rh) if (s != null) hr.add(ThutCore.trim(s));
        for (final String s : rf) if (s != null) fr.add(ThutCore.trim(s));
        for (final String s : lf) if (s != null) fl.add(ThutCore.trim(s));
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
     * @param hl       - left hind legs
     * @param hr       - right hind legs
     * @param fl       - left front legs
     * @param fr       - right front legs
     * @param duration - time taken for animation
     * @param legAngle - half - angle covered by hind legs.
     * @param armAngle - half - angle covered by front legs.
     * @return
     */
    public QuadWalkAnimation init(final Set<String> hl, final Set<String> hr, final Set<String> fl,
            final Set<String> fr, int duration, final float legAngle, final float armAngle)
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
