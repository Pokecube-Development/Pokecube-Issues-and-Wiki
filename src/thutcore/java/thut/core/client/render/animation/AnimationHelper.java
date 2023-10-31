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
            final IExtendedModelPart part, final float partialTick, final float limbSwing, final int tick)
    {
        final IAnimator animator = animation.getComponents(part.getName());
        if (animator == null) return false;
        return animator.animate(animation, animate, part, partialTick, limbSwing, tick);
    }

    public static boolean doAnimation(List<Animation> list, IAnimationHolder holder, final Entity entity,
            final IExtendedModelPart part, float partialTick, float limbSwing)
    {
        boolean animate = false;
        if (holder != null)
        {
            if (!entity.canUpdate())
            {
                partialTick = 0;
                limbSwing = 0;
            }
            for (final Animation animation : list)
            {
                holder.preRunAnim(animation);
                animate = AnimationHelper.animate(animation, holder, part, partialTick, limbSwing, entity.tickCount);
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
