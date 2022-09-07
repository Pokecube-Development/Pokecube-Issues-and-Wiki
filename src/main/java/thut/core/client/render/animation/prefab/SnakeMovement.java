package thut.core.client.render.animation.prefab;

import java.util.ArrayList;

import org.nfunk.jep.JEP;

import thut.api.entity.animation.Animation;
import thut.api.entity.animation.Animators.FunctionAnimation;
import thut.core.client.render.animation.AnimationXML.Phase;
import thut.core.common.ThutCore;

public class SnakeMovement extends Animation
{
    public SnakeMovement()
    {
        this.loops = true;
        this.name = "walking";
    }

    public SnakeMovement init(final ArrayList<String> parts, int duration, final float maxAngle, final int axis)
    {
        final int num = parts.size();
        double dphi = Math.PI / num;
        String phase = "0.5*%f*sin(t*0.001*%d + %f)";

        for (int i = 0; i < parts.size(); i++)
        {
            final String s = parts.get(i);

            JEP[] rots = new JEP[3];
            rots[axis] = new JEP();
            rots[axis].addStandardFunctions();
            rots[axis].addStandardConstants();
            rots[axis].addVariable("t", 0);
            try
            {
                String exp = phase.formatted(maxAngle, duration, dphi * i);
                rots[axis].parseExpression(exp);
                this.sets.put(s, new FunctionAnimation(rots));
                continue;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
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

        for (final String s : partsArr) if (s != null) parts.add(ThutCore.trim(s));

        if (!this.get(map, "angle").isEmpty()) maxAngle = Float.parseFloat(this.get(map, "angle"));
        if (!this.get(map, "axis").isEmpty()) axis = Integer.parseInt(this.get(map, "axis"));
        if (!this.get(map, "length").isEmpty()) duration = Integer.parseInt(this.get(map, "length"));
        if (!this.get(map, "duration").isEmpty()) duration = Integer.parseInt(this.get(map, "duration"));
        this.init(parts, duration, maxAngle, axis);
        return this;
    }
}
