package thut.core.client.render.wrappers;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thut.api.AnimatedCaps;
import thut.api.ModelHolder;
import thut.api.entity.IAnimated.IAnimationHolder;
import thut.api.entity.IMobColourable;
import thut.api.entity.animation.Animation;
import thut.api.entity.animation.IAnimationChanger;
import thut.api.maths.Vector3;
import thut.core.client.render.animation.AnimationHelper;
import thut.core.client.render.animation.AnimationXML.Mat;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelCustom;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.model.ModelFactory;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.texturing.IRetexturableModel;
import thut.core.client.render.texturing.TextureHelper;
import thut.core.common.ThutCore;
import thut.core.common.mobs.DefaultColourable;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = ThutCore.MODID, value = Dist.CLIENT)
public class ModelWrapper<T extends Entity> extends EntityModel<T> implements IModel
{
    private static final Set<ModelWrapper<?>> WRAPPERS = Sets.newHashSet();

    @SubscribeEvent
    public static void onTextureReload(final TextureStitchEvent.Post event)
    {
        ModelWrapper.WRAPPERS.forEach(w -> w.imodel = null);
    }

    public final ModelHolder model;
    public final IModelRenderer<?> renderer;
    public IModel imodel;
    private T entityIn;
    protected float rotationPointX = 0, rotationPointY = 0, rotationPointZ = 0;
    protected float rotateAngleX = 0, rotateAngleY = 0, rotateAngleZ = 0, rotateAngle = 0;

    public long lastInit = -1;
    public boolean debugMode = false;

    private final int[] tmp = new int[4];

    public ModelWrapper(final ModelHolder model, final IModelRenderer<?> renderer)
    {
        this.model = model;
        this.renderer = renderer;
        ModelWrapper.WRAPPERS.add(this);
        Arrays.fill(this.tmp, 255);
    }

    public void setEntity(final T entity)
    {
        this.entityIn = entity;
    }

    @Override
    public void applyAnimation(final Entity entity, final IModelRenderer<?> renderer, final float partialTicks,
            final float limbSwing)
    {
        if (!this.isLoaded()) return;
        this.imodel.applyAnimation(entity, renderer, partialTicks, limbSwing);
    }

    @Override
    public Set<String> getHeadParts()
    {
        if (this.imodel == null) return Collections.emptySet();
        return this.imodel.getHeadParts();
    }

    @Override
    public Map<String, IExtendedModelPart> getParts()
    {
        if (!this.isLoaded()) return Collections.emptyMap();
        return this.imodel.getParts();
    }

    @Override
    public boolean isValid()
    {
        // Wait for the imodel before claiming to be invalid
        if (this.imodel == null) return true;
        return this.imodel.isValid();
    }

    @Override
    public boolean isLoaded()
    {
        // If we have no model, obviously not loaded yet
        if (this.imodel == null) return false;
        // Otherwise ask the model
        return this.imodel.isLoaded();
    }

    @Override
    public void preProcessAnimations(final Collection<Animation> collection)
    {
        if (!this.isLoaded()) return;
        this.imodel.preProcessAnimations(collection);
    }

    private void initColours(final IExtendedModelPart parent, final T entity, final int brightness, final int overlay)
    {
        if (debugMode) return;
        int red = 255, green = 255, blue = 255;
        int alpha = 255;
        final IMobColourable poke = entity == null ? null
                : entity.getCapability(DefaultColourable.CAPABILITY).orElse(null);

        if (poke != null)
        {
            red = poke.getRGBA()[0];
            green = poke.getRGBA()[1];
            blue = poke.getRGBA()[2];
            alpha = poke.getRGBA()[3];
        }
        final IAnimationChanger animChanger = this.renderer.getAnimationChanger();
        if (animChanger != null && animChanger.modifyColourForPart(parent.getName(), entity, this.tmp))
        {
            red = this.tmp[0];
            green = this.tmp[1];
            blue = this.tmp[2];
            alpha = this.tmp[3];
        }
        parent.setRGBABrO(red, green, blue, alpha, brightness, overlay);
        for (final String partName : parent.getRenderOrder())
        {
            final IExtendedModelPart part = parent.getSubParts().get(partName);
            this.initColours(part, entity, brightness, overlay);
        }
    }

    @Override
    public void setupAnim(final T entityIn, final float limbSwing, final float limbSwingAmount, final float ageInTicks,
            final float netHeadYaw, final float headPitch)
    {
        if (!this.isLoaded()) return;
        this.setEntity(entityIn);
        IPartTexturer texer = this.renderer.getTexturer();
        if (texer != null) texer.bindObject(this.entityIn);
        this.renderer.getAnimationHolder().initHeadInfoAndMolangs(entityIn, limbSwing, limbSwingAmount, ageInTicks,
                netHeadYaw, headPitch);
        final IAnimationChanger animChanger = this.renderer.getAnimationChanger();
        this.imodel.getParts().forEach((partName, part) -> {
            if (animChanger != null) animChanger.isPartHidden(partName, entityIn, false);
        });
        if (this.imodel instanceof IRetexturableModel m) m.setTexturer(texer);
    }

    @Override
    public void renderToBuffer(final PoseStack mat, final VertexConsumer buffer, final int packedLightIn,
            final int packedOverlayIn, final float red, final float green, final float blue, final float alpha)
    {
        if (this.entityIn == null) return;
        if (this.imodel == null) this.imodel = ModelFactory.createWithRenderer(this.model, this.renderer);
        if (!this.isLoaded()) return;
        mat.pushPose();
        this.transformGlobal(mat, buffer, this.renderer.getAnimation(this.entityIn), this.entityIn,
                Minecraft.getInstance().getFrameTime());
        final Set<String> excluded = Sets.newHashSet();
        this.imodel.getParts().forEach((partName, part) -> {
            if (part.isHidden())
            {
                excluded.add(partName);
                excluded.addAll(part.getRecursiveChildNames());
            }
            else for (var source : renderer.getParticleSources()) source.onRender(mat, part);
            if (part.getParent() == null)
            {
                this.initColours(part, this.entityIn, packedLightIn, packedOverlayIn);
            }
        });
        if (this.imodel instanceof IModelCustom cmodel)
        {
            cmodel.renderAllExcept(mat, buffer, excluded);
        }
        else
        {
            for (final String partName : this.imodel.getRenderOrder())
            {
                final IExtendedModelPart part = this.imodel.getParts().get(partName);
                if (part == null) continue;
                if (part.getParent() == null)
                {
                    mat.pushPose();
                    this.initColours(part, this.entityIn, packedLightIn, packedOverlayIn);
                    part.renderAllExcept(mat, buffer, this.renderer, excluded);
                    mat.popPose();
                }
            }
        }
        mat.popPose();
    }

    protected void rotate(final PoseStack mat)
    {
        final Vector3f axis = new Vector3f(this.rotateAngleX, this.rotateAngleY, this.rotateAngleZ);
        mat.mulPose(new Quaternion(axis, this.rotateAngle, true));
    }

    public void setMob(final T entity, final MultiBufferSource bufferIn, ResourceLocation default_)
    {
        Object lock = this.imodel == null ? this.renderer : this.imodel;
        synchronized (lock)
        {
            final IPartTexturer texer = this.renderer.getTexturer();
            if (texer != null)
            {
                texer.bindObject(entity);
                if (texer instanceof TextureHelper helper) default_ = helper.default_tex;
                ResourceLocation defs = default_;
                this.getParts().forEach((n, p) -> {
                    p.applyTexture(bufferIn, defs, texer);
                });
            }
            this.setEntity(entity);
        }
    }

    /**
     * setLivingAnimations <br>
     * <br>
     * Used for easily adding entity-dependent animations. The second and third
     * float params here are the same second and third as in the
     * setRotationAngles method.
     */
    @Override
    public void prepareMobModel(final T entityIn, final float limbSwing, final float limbSwingAmount,
            final float partialTickTime)
    {
        if (this.imodel == null) this.imodel = ModelFactory.createWithRenderer(this.model, this.renderer);
        if (!this.isLoaded()) return;
        this.setEntity(entityIn);
        final IAnimationHolder holder = AnimationHelper.getHolder(entityIn);
        this.renderer.setAnimationHolder(holder);
        this.renderer.setAnimation(entityIn, partialTickTime);
        holder.setContext(AnimatedCaps.getAnimated(entityIn));
        holder.preRunAll();
        this.applyAnimation(entityIn, this.renderer, partialTickTime, limbSwing);
        holder.postRunAll();
    }

    @Override
    public void setOffset(final Vector3 point)
    {
        this.setRotationPoint((float) point.x, (float) point.y, (float) point.z);
    }

    public void setRotationPoint(final float par1, final float par2, final float par3)
    {
        this.rotationPointX = par1;
        this.rotationPointY = par2;
        this.rotationPointZ = par3;
    }

    protected void transformGlobal(final PoseStack mat, final VertexConsumer buffer, final String currentPhase,
            final Entity entity, final float partialTick)
    {
        this.setOffset(this.renderer.getRotationOffset());
        this.imodel.globalFix(mat, this.rotationPointX, this.rotationPointY, this.rotationPointZ);
        this.translate(mat);
        this.renderer.scaleEntity(mat, entity, this, partialTick);
    }

    private void translate(final PoseStack mat)
    {
        mat.translate(this.rotationPointX, this.rotationPointY, this.rotationPointZ);
    }

    @Override
    public void updateMaterial(final Mat mat)
    {
        this.imodel.updateMaterial(mat);
        IModel.super.updateMaterial(mat);
    }

    @Override
    public List<String> getRenderOrder()
    {
        return imodel.getRenderOrder();
    }

    @Override
    public void initBuiltInAnimations(IModelRenderer<?> renderer, List<Animation> tblAnims)
    {
        this.imodel.initBuiltInAnimations(renderer, tblAnims);
    }
}
