package thut.core.client.render.wrappers;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.Entity;
import thut.api.ModelHolder;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.core.client.render.animation.Animation;
import thut.core.client.render.animation.AnimationHelper;
import thut.core.client.render.animation.IAnimationChanger;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.model.IModelRenderer.Vector5;
import thut.core.client.render.model.ModelFactory;
import thut.core.client.render.texturing.IRetexturableModel;

public class ModelWrapper<T extends Entity> extends EntityModel<T> implements IModel
{
    public final ModelHolder       model;
    public final IModelRenderer<?> renderer;
    public IModel                  imodel;
    protected float                rotationPointX = 0, rotationPointY = 0, rotationPointZ = 0;
    protected float                rotateAngleX   = 0, rotateAngleY = 0, rotateAngleZ = 0, rotateAngle = 0;

    private final Vector5 rots = new Vector5();

    public ModelWrapper(final ModelHolder model, final IModelRenderer<?> renderer)
    {
        this.model = model;
        this.renderer = renderer;
    }

    @Override
    public void applyAnimation(final Entity entity, final IModelRenderer<?> renderer, final float partialTicks,
            final float limbSwing)
    {
        this.imodel.applyAnimation(entity, renderer, partialTicks, limbSwing);
    }

    @Override
    public HeadInfo getHeadInfo()
    {
        return this.imodel.getHeadInfo();
    }

    @Override
    public Set<String> getHeadParts()
    {
        return this.imodel.getHeadParts();
    }

    @Override
    public HashMap<String, IExtendedModelPart> getParts()
    {
        return this.imodel.getParts();
    }

    @Override
    public boolean isValid()
    {
        return this.imodel.isValid();
    }

    @Override
    public void preProcessAnimations(final Collection<List<Animation>> collection)
    {
        this.imodel.preProcessAnimations(collection);
    }

    /** Sets the models various rotation angles then renders the model. */
    @Override
    public void render(final Entity entityIn, final float limbSwing, final float limbSwingAmount,
            final float ageInTicks, final float netHeadYaw, final float headPitch, final float scale)
    {
        if (this.imodel == null) this.imodel = ModelFactory.create(this.model);
        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        final HeadInfo info = this.imodel.getHeadInfo();
        if (info != null) info.currentTick = entityIn.ticksExisted;
        final IAnimationChanger animChanger = this.renderer.getAnimationChanger();
        final Set<String> excluded = Sets.newHashSet();
        if (animChanger != null) for (final String partName : this.imodel.getParts().keySet())
            if (animChanger.isPartHidden(partName, entityIn, false)) excluded.add(partName);
        for (final String partName : this.imodel.getParts().keySet())
        {
            final IExtendedModelPart part = this.imodel.getParts().get(partName);
            if (part == null) continue;
            final int[] rgbab = part.getRGBAB();
            if (animChanger != null) animChanger.modifyColourForPart(part.getName(), entityIn, rgbab);
            part.setRGBAB(rgbab);
            try
            {
                if (this.renderer.getTexturer() != null) this.renderer.getTexturer().bindObject(entityIn);
                if (part instanceof IRetexturableModel) ((IRetexturableModel) part).setTexturer(this.renderer
                        .getTexturer());

                if (part.getParent() == null)
                {
                    GlStateManager.pushMatrix();
                    part.renderAllExcept(this.renderer, excluded.toArray(new String[excluded.size()]));
                    GlStateManager.popMatrix();
                }
            }
            catch (final Exception e)
            {
                e.printStackTrace();
            }
        }
        GlStateManager.color4f(1, 1, 1, 1);
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
        if (info != null) info.lastTick = entityIn.ticksExisted;
    }

    protected void rotate()
    {
        GlStateManager.rotatef(this.rotateAngle, this.rotateAngleX, this.rotateAngleY, this.rotateAngleZ);
    }

    /**
     * setLivingAnimations <br>
     * <br>
     * Used for easily adding entity-dependent animations. The second and third
     * float params here are the same second and third as in the
     * setRotationAngles method.
     */
    @Override
    public void setLivingAnimations(final T entityIn, final float limbSwing, final float limbSwingAmount,
            final float partialTickTime)
    {
        if (this.imodel == null) this.imodel = ModelFactory.create(this.model);
        if (this.renderer.getAnimationChanger() != null) this.renderer.setAnimation(entityIn, partialTickTime);
        this.renderer.setAnimationHolder(AnimationHelper.getHolder(entityIn));
        this.applyAnimation(entityIn, this.renderer, partialTickTime, limbSwing);
    }

    @Override
    public void setOffset(final Vector3 point)
    {
        this.setRotationPoint((float) point.x, (float) point.y, (float) point.z);
    }

    /**
     * was setRotationAngles <br>
     * <br>
     * Sets the model's various rotation angles. For bipeds, par1 and par2 are
     * used for animating the movement of arms and legs, where par1 represents
     * the time(so that arms and legs swing back and forth) and par2 represents
     * how "far" arms and legs can swing at most.
     */

    @Override
    public void setRotationAngles(final T entityIn, final float limbSwing, final float limbSwingAmount,
            final float ageInTicks, final float netHeadYaw, final float headPitch, final float scaleFactor)
    {
        if (this.imodel == null) this.imodel = ModelFactory.create(this.model);
        final HeadInfo info = this.imodel.getHeadInfo();
        if (info != null)
        {
            info.headPitch = headPitch;
            info.headYaw = netHeadYaw;
        }
        this.transformGlobal(this.renderer.getAnimation(entityIn), entityIn, Minecraft.getInstance()
                .getRenderPartialTicks(), netHeadYaw, headPitch);
    }

    public void setRotationAngles(final Vector4 rotations)
    {
        this.rotateAngle = rotations.w;
        this.rotateAngleX = rotations.x;
        this.rotateAngleY = rotations.y;
        this.rotateAngleZ = rotations.z;
    }

    public void setRotationPoint(final float par1, final float par2, final float par3)
    {
        this.rotationPointX = par1;
        this.rotationPointY = par2;
        this.rotationPointZ = par3;
    }

    protected void transformGlobal(final String currentPhase, final Entity entity, final float partialTick,
            final float rotationYaw, final float rotationPitch)
    {
        Vector5 rotations = this.renderer.getRotations();
        if (rotations == null) rotations = this.rots;
        this.setRotationAngles(rotations.rotations);
        this.setOffset(this.renderer.getRotationOffset());
        this.rotate();
        this.imodel.globalFix(this.rotationPointX, this.rotationPointY, this.rotationPointZ);
        this.translate();
        this.renderer.scaleEntity(entity, this, partialTick);
    }

    private void translate()
    {
        GlStateManager.translatef(this.rotationPointX, this.rotationPointY, this.rotationPointZ);
    }

}
