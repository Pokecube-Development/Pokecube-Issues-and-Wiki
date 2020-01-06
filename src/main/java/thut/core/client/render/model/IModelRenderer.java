package thut.core.client.render.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.core.client.render.animation.Animation;
import thut.core.client.render.animation.AnimationHelper;
import thut.core.client.render.animation.CapabilityAnimation.IAnimationHolder;
import thut.core.client.render.animation.IAnimationChanger;
import thut.core.client.render.animation.ModelHolder;
import thut.core.client.render.texturing.IPartTexturer;

public interface IModelRenderer<T extends MobEntity>
{
    public static class Vector5
    {
        public Vector4 rotations;
        public int     time;

        public Vector5()
        {
            this.time = 0;
            this.rotations = new Vector4();
        }

        public Vector5(Vector4 rotation, int time)
        {
            this.rotations = rotation;
            this.time = time;
        }

        public Vector5 interpolate(Vector5 v, float time, boolean wrap)
        {
            if (v.time == 0) return this;

            if (Double.isNaN(this.rotations.x)) this.rotations = new Vector4();
            Vector4 rotDiff = this.rotations.copy();

            if (this.rotations.x == this.rotations.z && this.rotations.z == this.rotations.y
                    && this.rotations.y == this.rotations.w && this.rotations.w == 0) this.rotations.x = 1;

            if (!v.rotations.equals(this.rotations))
            {
                rotDiff = v.rotations.subtractAngles(this.rotations);

                rotDiff = this.rotations.addAngles(rotDiff.scalarMult(time));
            }
            if (Double.isNaN(rotDiff.x)) rotDiff = new Vector4(0, 1, 0, 0);
            final Vector5 ret = new Vector5(rotDiff, v.time);
            return ret;
        }

        @Override
        public String toString()
        {
            return "|r:" + this.rotations + "|t:" + this.time;
        }
    }

    public static final String DEFAULTPHASE = "idle";

    static final Vector3 DEFAULTSCALE = Vector3.getNewVector().set(1);

    void doRender(T entity, double d, double d1, double d2, float f, float partialTick);

    default String getAnimation(Entity entityIn)
    {
        final IAnimationHolder holder = AnimationHelper.getHolder(entityIn);
        if (holder != null) return holder.getPendingAnimation();
        return "idle";
    }

    IAnimationChanger getAnimationChanger();

    HashMap<String, List<Animation>> getAnimations();

    default Vector3 getRotationOffset()
    {
        return Vector3.empty;
    }

    @Nullable
    default Vector5 getRotations()
    {
        return null;
    }

    default Vector3 getScale()
    {
        return IModelRenderer.DEFAULTSCALE;
    }

    IPartTexturer getTexturer();

    boolean hasAnimation(String phase, Entity entity);

    void renderStatus(T entity, double d, double d1, double d2, float f, float partialTick);

    void scaleEntity(Entity entity, IModel model, float partialTick);

    default void setAnimation(String phase, Entity entity)
    {
        final IAnimationHolder holder = AnimationHelper.getHolder(entity);
        if (holder != null) holder.setPendingAnimation(phase);
    }

    void setAnimationChanger(IAnimationChanger changer);

    void setRotationOffset(Vector3 offset);

    void setRotations(Vector5 rotations);

    void setScale(Vector3 scale);

    void setTexturer(IPartTexturer texturer);

    void updateModel(HashMap<String, ArrayList<Vector5>> phaseList, ModelHolder model);
}
