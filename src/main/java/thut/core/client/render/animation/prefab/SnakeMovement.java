package thut.core.client.render.animation.prefab;

import java.util.ArrayList;
import java.util.Locale;

import thut.api.entity.animation.Animation;
import thut.api.entity.animation.AnimationComponent;
import thut.api.entity.animation.Animators.KeyframeAnimator;
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
        final int mod = num > 6 ? 3 : 2;
        double dphi = mod * Math.PI / num;
        String phase = "0.5*%f*sin(t*0.001*%d + %f)";

        for (int i = 0; i < parts.size(); i++)
        {
            final String s = parts.get(i);

            AnimationComponent comp = new AnimationComponent();
            String[] rots = comp._rotFunctions;
            try
            {
                String exp = String.format(Locale.ROOT, phase, maxAngle, duration, dphi * i);
                rots[axis] = exp;
                this.sets.put(s, new KeyframeAnimator(comp));
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
