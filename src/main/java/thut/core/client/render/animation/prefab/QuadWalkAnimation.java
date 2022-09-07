package thut.core.client.render.animation.prefab;

import java.util.HashSet;
import java.util.Set;

import org.nfunk.jep.JEP;

import thut.api.entity.animation.Animation;
import thut.api.entity.animation.Animators.FunctionAnimation;
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
        String phase1 = "%f*cos(0.05*t*%d)";
        String phase2 = "-%f*cos(0.05*t*%d)";
        String phase3 = "%f*sin(0.05*t*%d)";
        String phase4 = "-%f*sin(0.05*t*%d)";

        for (final String s : hr)
        {
            JEP[] rots = new JEP[3];
            rots[0] = new JEP();
            rots[0].addStandardFunctions();
            rots[0].addStandardConstants();
            rots[0].addVariable("t", 0);
            try
            {
                String exp = phase1.formatted(legAngle, duration);
                rots[0].parseExpression(exp);
                this.sets.put(s, new FunctionAnimation(rots));
                continue;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        for (final String s : hl)
        {
            JEP[] rots = new JEP[3];
            rots[0] = new JEP();
            rots[0].addStandardFunctions();
            rots[0].addStandardConstants();
            rots[0].addVariable("t", 0);
            try
            {
                rots[0].parseExpression(phase2.formatted(legAngle, duration));
                this.sets.put(s, new FunctionAnimation(rots));
                continue;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        for (final String s : fr)
        {
            JEP[] rots = new JEP[3];
            rots[0] = new JEP();
            rots[0].addStandardFunctions();
            rots[0].addStandardConstants();
            rots[0].addVariable("t", 0);
            try
            {
                rots[0].parseExpression(phase3.formatted(legAngle, duration));
                this.sets.put(s, new FunctionAnimation(rots));
                continue;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        for (final String s : fl)
        {
            JEP[] rots = new JEP[3];
            rots[0] = new JEP();
            rots[0].addStandardFunctions();
            rots[0].addStandardConstants();
            rots[0].addVariable("t", 0);
            try
            {
                rots[0].parseExpression(phase4.formatted(legAngle, duration));
                this.sets.put(s, new FunctionAnimation(rots));
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
