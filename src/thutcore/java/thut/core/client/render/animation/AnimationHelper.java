package thut.core.client.render.animation;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.world.entity.Entity;
import thut.api.ThutCaps;
import thut.api.entity.IAnimated.IAnimationHolder;
import thut.api.entity.animation.Animation;
import thut.api.entity.animation.Animators.IAnimator;
import thut.api.entity.animation.CapabilityAnimation;
import thut.core.client.render.model.IExtendedModelPart;

/**
 * This class applies the tabula style animations to models consisting of
 * IExtendedModelPart parts.
 *
 * @author Thutmose
 */
public class AnimationHelper
{
    private final static Map<UUID, IAnimationHolder> holderMap = Maps.newHashMap();

    public static boolean animate(final Animation animation, final IAnimationHolder animate,
            final IExtendedModelPart part)
    {
        final IAnimator animator = animation.getComponents(part.getName());
        if (animator == null) return false;
        return animator.animate(animation, animate, part);
    }

    public static boolean doAnimation(List<Animation> list, IAnimationHolder holder, final IExtendedModelPart part)
    {
        boolean animate = false;
        if (holder != null)
        {
            for (final Animation animation : list)
            {
                holder.preRunAnim(animation);
                animate = AnimationHelper.animate(animation, holder, part);
                holder.postRunAnim(animation);
                if(animate) break;
            }
        }
        return animate;
    }

    public static IAnimationHolder getHolder(final Entity mob)
    {
        final IAnimationHolder cap = ThutCaps.getAnimationHolder(mob);
        if (cap != null) return cap;
        if (AnimationHelper.holderMap.containsKey(mob.getUUID())) return AnimationHelper.holderMap.get(mob.getUUID());
        else
        {
            final CapabilityAnimation.DefaultImpl holder = new CapabilityAnimation.DefaultImpl();
            AnimationHelper.holderMap.put(mob.getUUID(), holder);
            return holder;
        }
    }
}
