package pokecube.mobs.client.smd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import pokecube.mobs.client.smd.impl.Bone;
import pokecube.mobs.client.smd.impl.Face;
import pokecube.mobs.client.smd.impl.Helpers;
import pokecube.mobs.client.smd.impl.Model;
import pokecube.mobs.client.smd.impl.MutableVertex;
import thut.api.entity.IAnimated.HeadInfo;
import thut.api.entity.IAnimated.IAnimationHolder;
import thut.api.entity.animation.Animation;
import thut.api.entity.animation.IAnimationChanger;
import thut.api.maths.Vector3;
import thut.api.maths.vecmath.Mat4f;
import thut.core.client.render.animation.AnimationXML.Mat;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelCustom;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.model.parts.Material;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.texturing.IRetexturableModel;
import thut.core.common.ThutCore;
import thut.lib.AxisAngles;
import thut.lib.ResourceHelper;

public class SMDModel implements IModelCustom, IModel, IRetexturableModel, IFakeExtendedPart
{
    public static class Loader implements Runnable
    {
        final SMDModel toLoad;

        final ResourceLocation res;

        public Loader(final SMDModel model, final ResourceLocation res)
        {
            this.toLoad = model;
            this.res = res;
        }

        @Override
        public void run()
        {
            try
            {
                this.toLoad.wrapped = new Model(this.res);
                this.toLoad.initBounds();
                this.toLoad.wrapped.usesMaterials = true;
                this.toLoad.animations.addAll(this.toLoad.wrapped.anims.keySet());
                this.toLoad.mats.addAll(this.toLoad.wrapped.body.matsToFaces.keySet());
                // Flag as loaded before running the callback
                this.toLoad.loaded = true;
                if (this.toLoad.callback != null) this.toLoad.callback.run(this.toLoad);
                this.toLoad.callback = null;
            }
            catch (final Exception e)
            {
                this.toLoad.loaded = true;
                this.toLoad.valid = false;
            }

        }
    }

    private final HashMap<String, IExtendedModelPart> nullPartsMap = Maps.newHashMap();
    private final HashMap<String, IExtendedModelPart> subPartsMap = Maps.newHashMap();

    private final List<IExtendedModelPart> order = Lists.newArrayList();

    private final Set<String> nullHeadSet = Sets.newHashSet();
    private final Set<String> animations = Sets.newHashSet();
    private final List<Material> mats = Lists.newArrayList();

    protected boolean valid = true;
    protected boolean loaded = false;

    Vector3 min = new Vector3();
    Vector3 max = new Vector3();

    Model wrapped;

    IRetexturableModel.Holder<IAnimationChanger> animChangeHolder = new IRetexturableModel.Holder<>();
    IRetexturableModel.Holder<IAnimationHolder> animHolderHolder = new IRetexturableModel.Holder<>();
    IRetexturableModel.Holder<IPartTexturer> texChangeHolder = new IRetexturableModel.Holder<>();

    private final int[] rgbabro = new int[6];

    protected IModelCallback callback = null;

    public SMDModel()
    {
        this.nullPartsMap.put(this.getName(), this);
    }

    public SMDModel(final ResourceLocation model)
    {
        this();
        try
        {
            // Check if the model even exists
            if (!ResourceHelper.exists(model, Minecraft.getInstance().getResourceManager()))
            {
                this.valid = false;
                return;
            }
            // If it did exist, then lets schedule load on another thread
            final Thread loader = new Thread(new Loader(this, model));
            loader.setName("ThutCore: SMD Load: " + model);
            if (ThutCore.conf.asyncModelLoads) loader.start();
            else loader.run();
        }
        catch (final Exception e)
        {
            // We failed to load, so not valid!
            this.valid = false;
        }
    }

    private void initBounds()
    {
        if (!(this.max.isEmpty() && this.min.isEmpty())) return;
        for (final MutableVertex v : this.wrapped.body.verts)
        {
            this.min.x = Math.min(this.min.x, v.x);
            this.min.y = Math.min(this.min.y, v.y);
            this.min.z = Math.min(this.min.z, v.z);

            this.max.x = Math.max(this.max.x, v.x);
            this.max.y = Math.max(this.max.y, v.y);
            this.max.z = Math.max(this.max.z, v.z);
        }
        // Seems we have some differing scale requirements, so lets do this to
        // account for that.
        this.min.scalarMultBy(0.2);
        this.max.scalarMultBy(0.2);
    }

    @Override
    public Vector3 minBound()
    {
        return this.min;
    }

    @Override
    public Vector3 maxBound()
    {
        return this.max;
    }

    @Override
    public IModel init(final IModelCallback callback)
    {
        if (this.loaded && this.isValid()) callback.run(this);
        else this.callback = callback;
        return this;
    }

    @Override
    public boolean isLoaded()
    {
        return this.loaded;
    }

    @Override
    public void applyAnimation(final Entity entity, final IModelRenderer<?> renderer, final float partialTicks,
            final float limbSwing)
    {
        this.wrapped.setAnimation(renderer.getAnimation(entity));
    }

    @Override
    public Set<String> getBuiltInAnimations()
    {
        return this.animations;
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
    public void preProcessAnimations(final Collection<Animation> collection)
    {
        // TODO figure out animations for this.
    }

    @Override
    public void renderAllExcept(final PoseStack mat, final VertexConsumer buffer, final IModelRenderer<?> renderer,
            final Collection<String> excludedGroupNames)
    {
        // SMD Renders whole thing at once, so no part rendering.
        this.render(mat, buffer, renderer);
    }

    public void render(final PoseStack mat, final VertexConsumer buffer, final IModelRenderer<?> renderer)
    {
        if (this.wrapped != null)
        {
            // Scaling factor for model.
            mat.scale(0.165f, 0.165f, 0.165f);
            // Makes model face correct way.
            mat.mulPose(AxisAngles.YP.rotationDegrees(180));

            final HeadInfo info = renderer.getAnimationHolder() != null ? renderer.getAnimationHolder().getHeadInfo()
                    : HeadInfo.DUMMY;

            // only increment frame if a tick has passed.
            if (this.wrapped.body.currentAnim != null && this.wrapped.body.currentAnim.frameCount() > 0)
                this.wrapped.body.currentAnim
                        .setCurrentFrame(info.currentTick % this.wrapped.body.currentAnim.frameCount());
            // Check head parts for rendering rotations of them.
            for (final String s : this.getHeadParts())
            {
                final Bone bone = this.wrapped.body.getBone(s);
                if (bone != null)
                {
                    // Cap and convert pitch and yaw to radians.
                    float yaw = Math.max(Math.min(info.headYaw, info.yawCapMax), info.yawCapMin);
                    yaw = (float) Math.toRadians(yaw) * info.yawDirection;
                    float pitch = -Math.max(Math.min(info.headPitch, info.pitchCapMax), info.pitchCapMin);
                    pitch = (float) Math.toRadians(pitch) * info.pitchDirection;

                    // Head rotation matrix
                    Mat4f headRot = new Mat4f();

                    float xr = 0, yr = 0, zr = 0;

                    switch (info.yawAxis)
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

                    switch (info.pitchAxis)
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
            this.wrapped.renderAll(mat, buffer, rgbabro);
        }
    }

    @Override
    public List<Material> getMaterials()
    {
        return this.mats;
    }

    @Override
    public void applyTexture(final MultiBufferSource bufferIn, final ResourceLocation tex, final IPartTexturer texer)
    {
        for (final Material mat : this.mats)
        {
            final ResourceLocation tex_1 = texer.getTexture(mat.name, tex);
            mat.makeVertexBuilder(tex_1, bufferIn);
        }
    }

    @Override
    public void setRGBABrO(Predicate<Material> material, final int r, final int g, final int b, final int a,
            final int br, final int o)
    {
        this.rgbabro[0] = r;
        this.rgbabro[1] = g;
        this.rgbabro[2] = b;
        this.rgbabro[3] = a;
        this.rgbabro[4] = br;
        this.rgbabro[5] = o;
        if (material != null) this.wrapped.body.namesToMats.forEach((n, m) -> {
            if (material.test(m))
            {
                m.rgbabro[0] = r;
                m.rgbabro[1] = g;
                m.rgbabro[2] = b;
                m.rgbabro[3] = a;
                m.rgbabro[4] = br;
                m.rgbabro[5] = o;
            }
        });
    }

    @Override
    public void updateMaterial(final Mat mat)
    {
        final String mat_name = ThutCore.trim(mat.name);
        final Material material = new Material(mat_name);
        material.diffuseColor = new thut.api.maths.vecmath.Vec3f(1, 1, 1);
        material.emissiveColor = new thut.api.maths.vecmath.Vec3f(1, 1, 1);
        material.specularColor = new thut.api.maths.vecmath.Vec3f(1, 1, 1);
        material.alpha = mat.alpha;
        material.flat = !mat.smooth;
        material.transluscent = mat.transluscent;
        material.cull = mat.cull;
        material.shader = mat.shader;

        final Material old = this.wrapped.body.namesToMats.get(mat_name);
        final ArrayList<Face> faces = this.wrapped.body.matsToFaces.remove(old);

        this.wrapped.body.namesToMats.put(mat_name, material);
        this.wrapped.body.matsToFaces.put(material, faces);

        this.mats.clear();
        this.mats.addAll(this.wrapped.body.namesToMats.values());
    }

    @Override
    public List<IExtendedModelPart> getRenderOrder()
    {
        // TODO see what we need to do for this for wearables support later.
        return this.order;
    }

    @Override
    public void setColorScales(float r, float g, float b, float a)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setPostScale(Vector3 scale)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addPartRenderAdder(IPartRenderAdder adder)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Holder<IAnimationHolder> getAnimationHolder()
    {
        return this.animHolderHolder;
    }

    @Override
    public void setAnimationHolder(Holder<IAnimationHolder> input)
    {
        this.animHolderHolder = input;
        for (var part : this.getRenderOrder()) part.setAnimationHolder(input);
    }

    @Override
    public Holder<IAnimationChanger> getAnimationChanger()
    {
        return this.animChangeHolder;
    }

    @Override
    public void setAnimationChanger(Holder<IAnimationChanger> input)
    {
        this.animChangeHolder = input;
        for (var part : this.getRenderOrder()) if (part instanceof IRetexturableModel p) p.setAnimationChanger(input);
    }

    @Override
    public Holder<IPartTexturer> getTexturerChanger()
    {
        return this.texChangeHolder;
    }

    @Override
    public void setTexturerChanger(Holder<IPartTexturer> input)
    {
        this.texChangeHolder = input;
        for (var part : this.getRenderOrder()) if (part instanceof IRetexturableModel p) p.setTexturerChanger(input);
    }

    @Override
    public void setAnimationHolder(IAnimationHolder holder)
    {
        this.animHolderHolder.set(holder);
    }
}
