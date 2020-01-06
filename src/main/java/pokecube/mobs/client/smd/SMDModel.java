package pokecube.mobs.client.smd;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.vecmath.Matrix4f;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import pokecube.mobs.client.smd.impl.Bone;
import pokecube.mobs.client.smd.impl.Helpers;
import pokecube.mobs.client.smd.impl.Model;
import thut.core.client.render.animation.Animation;
import thut.core.client.render.animation.CapabilityAnimation.IAnimationHolder;
import thut.core.client.render.animation.IAnimationChanger;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelCustom;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.texturing.IRetexturableModel;

public class SMDModel implements IModelCustom, IModel, IRetexturableModel, IFakeExtendedPart
{
    private final HashMap<String, IExtendedModelPart> nullPartsMap = Maps.newHashMap();
    private final HashMap<String, IExtendedModelPart> subPartsMap  = Maps.newHashMap();
    private final Set<String>                         nullHeadSet  = Sets.newHashSet();
    private final Set<String>                         animations   = Sets.newHashSet();
    private final HeadInfo                            info         = new HeadInfo();
    private boolean                                   valid        = false;
    Model                                             wrapped;
    IPartTexturer                                     texturer;
    IAnimationChanger                                 changer;

    public SMDModel()
    {
        this.nullPartsMap.put(this.getName(), this);
    }

    public SMDModel(ResourceLocation model)
    {
        this();
        try
        {
            this.wrapped = new Model(model);
            this.wrapped.usesMaterials = true;
            this.animations.addAll(this.wrapped.anims.keySet());
            this.valid = true;
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void applyAnimation(Entity entity, IAnimationHolder animate, IModelRenderer<?> renderer, float partialTicks,
            float limbSwing)
    {
        this.wrapped.setAnimation(renderer.getAnimation(entity));
    }

    @Override
    public Set<String> getBuiltInAnimations()
    {
        return this.animations;
    }

    @Override
    public HeadInfo getHeadInfo()
    {
        return this.info;
    }

    @Override
    public Set<String> getHeadParts()
    {
        return this.nullHeadSet;
    }

    @Override
    public String getName()
    {
        return "main";
    }

    @Override
    public HashMap<String, IExtendedModelPart> getParts()
    {
        // SMD Renders whole thing at once, so no part rendering.
        return this.nullPartsMap;
    }

    @Override
    public HashMap<String, IExtendedModelPart> getSubParts()
    {
        return this.subPartsMap;
    }

    @Override
    public String getType()
    {
        return "smd";
    }

    @Override
    public boolean isValid()
    {
        return this.valid;
    }

    @Override
    public void preProcessAnimations(Collection<List<Animation>> collection)
    {
        // TODO figure out animations for this.
    }

    public void render(IModelRenderer<?> renderer)
    {
        if (this.wrapped != null)
        {
            this.wrapped.body.setTexturer(this.texturer);
            this.wrapped.body.setAnimationChanger(this.changer);
            // Scaling factor for model.
            GL11.glScaled(0.165, 0.165, 0.165);
            // Makes model face correct way.
            GL11.glRotated(180, 0, 1, 0);

            // only increment frame if a tick has passed.
            if (this.wrapped.body.currentAnim != null && this.wrapped.body.currentAnim.frameCount() > 0)
                this.wrapped.body.currentAnim.setCurrentFrame(this.info.currentTick % this.wrapped.body.currentAnim
                        .frameCount());
            // Check head parts for rendering rotations of them.
            for (final String s : this.getHeadParts())
            {
                final Bone bone = this.wrapped.body.getBone(s);
                if (bone != null)
                {
                    // Cap and convert pitch and yaw to radians.
                    float yaw = Math.max(Math.min(this.info.headYaw, this.info.yawCapMax), this.info.yawCapMin);
                    yaw = (float) Math.toRadians(yaw) * this.info.yawDirection;
                    float pitch = Math.max(Math.min(this.info.headPitch, this.info.pitchCapMax), this.info.pitchCapMin);
                    pitch = (float) Math.toRadians(pitch) * this.info.pitchDirection;

                    // Head rotation matrix
                    Matrix4f headRot = new Matrix4f();

                    float xr = 0, yr = 0, zr = 0;

                    switch (this.info.yawAxis)
                    {
                    case 2:
                        zr = yaw;
                        break;
                    case 1:
                        yr = yaw;
                        break;
                    case 0:
                        xr = yaw;
                        break;
                    }
                    headRot = Helpers.makeMatrix(0, 0, 0, xr, yr, zr);
                    // Apply the rotation.
                    bone.applyTransform(headRot);

                    xr = 0;
                    yr = 0;
                    zr = 0;

                    switch (this.info.pitchAxis)
                    {
                    case 2:
                        zr = pitch;
                        break;
                    case 1:
                        yr = pitch;
                        break;
                    case 0:
                        xr = pitch;
                        break;
                    }
                    headRot = Helpers.makeMatrix(0, 0, 0, xr, yr, zr);
                    // Apply the rotation.
                    bone.applyTransform(headRot);
                }
            }
            this.wrapped.animate();
            this.wrapped.renderAll();
        }
    }

    @Override
    public void renderAll(IModelRenderer<?> renderer)
    {
        this.render(renderer);
    }

    @Override
    public void renderAllExcept(IModelRenderer<?> renderer, String... excludedGroupNames)
    {
        // SMD Renders whole thing at once, so no part rendering.
        this.render(renderer);
    }

    @Override
    public void renderOnly(IModelRenderer<?> renderer, String... groupNames)
    {
        // SMD Renders whole thing at once, so no part rendering.
        this.render(renderer);
    }

    @Override
    public void renderPart(IModelRenderer<?> renderer, String partName)
    {
        // SMD Renders whole thing at once, so no part rendering.
        this.render(renderer);
    }

    @Override
    public void setAnimationChanger(IAnimationChanger changer)
    {
        this.changer = changer;
    }

    @Override
    public void setTexturer(IPartTexturer texturer)
    {
        this.texturer = texturer;
    }
}
