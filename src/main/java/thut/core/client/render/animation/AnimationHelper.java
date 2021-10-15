package thut.core.client.render.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.minecraft.world.entity.Entity;
import thut.api.ThutCaps;
import thut.api.entity.IAnimated.IAnimationHolder;
import thut.api.entity.animation.Animation;
import thut.api.entity.animation.AnimationComponent;
import thut.api.entity.animation.CapabilityAnimation;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
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

    public static boolean animate(final Animation animation, final IAnimationHolder animate, final String partName,
            final IExtendedModelPart part, final float partialTick, final float limbSwing, final int tick)
    {
        final ArrayList<AnimationComponent> components = animation.getComponents(partName);
        if (components == null) return false;
        boolean animated = false;
        final Vector3 temp = Vector3.getNewVector();
        float x = 0, y = 0, z = 0;
        float sx = 1, sy = 1, sz = 1;
        int aniTick = tick;
        float time1 = aniTick;
        float time2 = 0;
        int animationLength = animation.getLength();
        animationLength = Math.max(1, animationLength);
        final float limbSpeedFactor = 3f;
        time1 = (time1 + partialTick) % animationLength;
        time2 = limbSwing * limbSpeedFactor % animationLength;
        aniTick = (int) time1;
        for (final AnimationComponent component : components)
        {
            final float time = component.limbBased ? time2 : time1;
            if (component.limbBased) aniTick = (int) time2;
            // if (partName.equals("body"))
            // System.out.println(Arrays.toString(component.posChange));
            if (time >= component.startKey)
            {
                animated = true;
                float componentTimer = time - component.startKey;
                if (componentTimer > component.length) componentTimer = component.length;
                final int length = component.length == 0 ? 1 : component.length;
                final float ratio = componentTimer / length;
                temp.addTo(component.posChange[0] * ratio + component.posOffset[0], component.posChange[1] * ratio
                        + component.posOffset[1], component.posChange[2] * ratio + component.posOffset[2]);
                x += (float) (component.rotChange[0] * ratio + component.rotOffset[0]);
                y += (float) (component.rotChange[1] * ratio + component.rotOffset[1]);
                z += (float) (component.rotChange[2] * ratio + component.rotOffset[2]);

                sx += (float) (component.scaleChange[0] * ratio + component.scaleOffset[0]);
                sy += (float) (component.scaleChange[1] * ratio + component.scaleOffset[1]);
                sz += (float) (component.scaleChange[2] * ratio + component.scaleOffset[2]);

                // Apply hidden like this so last hidden state is kept
                part.setHidden(component.hidden);
            }
        }
        animate.setStep(animation, aniTick + 2);
        if (animated)
        {
            part.setPreTranslations(temp);
            part.setPreScale(temp.set(sx, sy, sz));
            final Quaternion quat = new Quaternion(0, 0, 0, 1);
            if (z != 0) quat.mul(Vector3f.YN.rotationDegrees(z));
            if (x != 0) quat.mul(Vector3f.XP.rotationDegrees(x));
            if (y != 0) quat.mul(Vector3f.ZP.rotationDegrees(y));
            part.setPreRotations(new Vector4(quat));
        }
        return animated;
    }

    public static boolean doAnimation(List<Animation> list, final Entity entity, final String partName,
            final IExtendedModelPart part, float partialTick, float limbSwing)
    {
        boolean animate = false;
        final IAnimationHolder holder = part.getAnimationHolder();
        if (holder != null)
        {
            if (!entity.canUpdate())
            {
                partialTick = 0;
                limbSwing = 0;
            }
            list = Lists.newArrayList(holder.getPlaying());
            for (final Animation animation : list)
                animate = AnimationHelper.animate(animation, holder, partName, part, partialTick, limbSwing,
                        entity.tickCount) || animate;
        }
        return animate;
    }

    public static IAnimationHolder getHolder(final Entity mob)
    {
        final IAnimationHolder cap = mob.getCapability(ThutCaps.ANIMCAP).orElse(null);
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
