package pokecube.mobs.client.smd;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import pokecube.mobs.client.smd.impl.Bone;
import pokecube.mobs.client.smd.impl.Helpers;
import pokecube.mobs.client.smd.impl.Model;
import thut.api.maths.vecmath.Matrix4f;
import thut.core.client.render.animation.Animation;
import thut.core.client.render.animation.CapabilityAnimation.IAnimationHolder;
import thut.core.client.render.animation.IAnimationChanger;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelCustom;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.model.parts.Material;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.texturing.IRetexturableModel;

public class SMDModel implements IModelCustom, IModel, IRetexturableModel, IFakeExtendedPart
{
    private final HashMap<String, IExtendedModelPart> nullPartsMap = Maps.newHashMap();
    private final HashMap<String, IExtendedModelPart> subPartsMap  = Maps.newHashMap();
    private final Set<String>                         nullHeadSet  = Sets.newHashSet();
    private final Set<String>                         animations   = Sets.newHashSet();
    private final HeadInfo                            info         = new HeadInfo();
    private final List<Material>                      mats         = Lists.newArrayList();
    private boolean                                   valid        = false;
    Model                                             wrapped;
    IPartTexturer                                     texturer;
    IAnimationChanger                                 changer;
    public int                                        red          = 255, green = 255, blue = 255, alpha = 255;
    public int                                        brightness   = 15728640;
    public int                                        overlay      = 655360;
    private final int[]                               rgbabro      = new int[6];

    public SMDModel()
    {
        this.nullPartsMap.put(this.getName(), this);
    }

    public SMDModel(final ResourceLocation model)
    {
        this();
        try
        {
            this.wrapped = new Model(model);
            this.wrapped.usesMaterials = true;
            this.animations.addAll(this.wrapped.anims.keySet());
            this.mats.addAll(this.wrapped.body.matsToFaces.keySet());
            this.valid = true;
        }
        catch (final Exception e)
        {
            // We failed to load, so not valid!
        }
    }

    @Override
    public void applyAnimation(final Entity entity, final IAnimationHolder animate, final IModelRenderer<?> renderer,
            final float partialTicks, final float limbSwing)
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
    public void preProcessAnimations(final Collection<List<Animation>> collection)
    {
        // TODO figure out animations for this.
    }

    @Override
    public void renderAllExcept(final MatrixStack mat, final IVertexBuilder buffer, final IModelRenderer<?> renderer,
            final String... excludedGroupNames)
    {
        // SMD Renders whole thing at once, so no part rendering.
        this.render(mat, buffer, renderer);
    }

    public void render(final MatrixStack mat, final IVertexBuilder buffer, final IModelRenderer<?> renderer)
    {
        if (this.wrapped != null)
        {
            this.wrapped.body.setTexturer(this.texturer);
            this.wrapped.body.setAnimationChanger(this.changer);
            // Scaling factor for model.
            mat.scale(0.165f, 0.165f, 0.165f);
            // Makes model face correct way.
            mat.rotate(Vector3f.YP.rotationDegrees(180));

            // only increment frame if a tick has passed.
            if (this.wrapped.body.currentAnim != null && this.wrapped.body.currentAnim.frameCount() > 0)
                this.wrapped.body.currentAnim
                .setCurrentFrame(this.info.currentTick % this.wrapped.body.currentAnim.frameCount());
            // Check head parts for rendering rotations of them.
            for (final String s : this.getHeadParts())
            {
                final Bone bone = this.wrapped.body.getBone(s);
                if (bone != null)
                {
                    // Cap and convert pitch and yaw to radians.
                    float yaw = Math.max(Math.min(this.info.headYaw, this.info.yawCapMax), this.info.yawCapMin);
                    yaw = (float) Math.toRadians(yaw) * this.info.yawDirection;
                    float pitch = -Math.max(Math.min(this.info.headPitch, this.info.pitchCapMax),
                            this.info.pitchCapMin);
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
            this.wrapped.renderAll(mat, buffer, this.getRGBABrO());
        }
    }

    @Override
    public void setAnimationChanger(final IAnimationChanger changer)
    {
        this.changer = changer;
    }

    @Override
    public void setTexturer(final IPartTexturer texturer)
    {
        this.texturer = texturer;
    }

    @Override
    public List<Material> getMaterials()
    {
        return this.mats;
    }

    @Override
    public void applyTexture(final IRenderTypeBuffer bufferIn, final ResourceLocation tex, final IPartTexturer texer)
    {
        for (final Material mat : this.mats)
        {
            final ResourceLocation tex_1 = texer.getTexture(mat.name, tex);
            mat.makeVertexBuilder(tex_1, bufferIn);
        }
    }

    @Override
    public int[] getRGBABrO()
    {
        this.rgbabro[0] = this.red;
        this.rgbabro[1] = this.green;
        this.rgbabro[2] = this.blue;
        this.rgbabro[3] = this.alpha;
        this.rgbabro[4] = this.brightness;
        this.rgbabro[5] = this.overlay;
        return this.rgbabro;
    }

    @Override
    public void setRGBABrO(final int r, final int g, final int b, final int a, final int br, final int o)
    {
        this.red = r;
        this.green = g;
        this.blue = b;
        this.alpha = a;
        this.brightness = br;
        this.overlay = o;
    }
}
