package thut.core.client.render.model.parts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import thut.api.entity.IAnimated.IAnimationHolder;
import thut.api.entity.animation.IAnimationChanger;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.api.util.JsonUtil;
import thut.core.client.render.animation.AnimationXML.Mat;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.Vertex;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.texturing.IRetexturableModel;
import thut.core.common.ThutCore;
import thut.lib.AxisAngles;

public abstract class Part implements IExtendedModelPart, IRetexturableModel
{
    private final Map<String, IExtendedModelPart> parts = new Object2ObjectOpenHashMap<>();

    private final List<IPartRenderAdder> renderAdders = new ArrayList<>();
    private final List<IExtendedModelPart> order = new ArrayList<>();
    private final List<Mesh> shapes = new ArrayList<>();

    private final String name;

    private IExtendedModelPart parent = null;

    IRetexturableModel.Holder<IAnimationChanger> animChangeHolder = new IRetexturableModel.Holder<>();
    IRetexturableModel.Holder<IAnimationHolder> animHolderHolder = new IRetexturableModel.Holder<>();
    IRetexturableModel.Holder<IPartTexturer> texChangeHolder = new IRetexturableModel.Holder<>();

    public Vector4 preRot = new Vector4();
    public Vector4 postRot = new Vector4();
    public Vector3 preTrans = new Vector3();
    public Vector3 postTrans = new Vector3();
    public Vertex preScale = new Vertex(1, 1, 1);

    public Vector3 offset = new Vector3();
    public Vector4 rotations = new Vector4();
    public Vertex scale = new Vertex(1, 1, 1);

    protected Quaternion _quat = new Quaternion(0, 0, 0, 1);
    protected Vector4 _rot = new Vector4();

    private float ds = 1;
    public float ds0 = 1;
    public float ds1 = 1;
    private float ds2 = 1;

    public float[] colour_scales =
    { 1f, 1f, 1f, 1f };

    Vector3 min = new Vector3();
    Vector3 max = new Vector3();

    public int brightness = 15728640;
    public int overlay = 655360;

    // Set this true to mark animations for this as limb based if they are
    // normal keyframes
    public boolean isOverridenLimb = false;

    private boolean hidden = false;
    private boolean disabled = false;
    private boolean isHead = false;

    private final List<Material> materials = Lists.newArrayList();
    private final Map<String, Material> namedMaterials = new Object2ObjectOpenHashMap<>();
    private final Set<Material> matcache = Sets.newHashSet();

    private Set<String> parentNames = Sets.newHashSet();
    private Set<String> childNames = Sets.newHashSet();

    public Part(final String name)
    {
        this.name = name;
    }

    private void initBounds()
    {
        if (!(this.max.isEmpty() && this.min.isEmpty())) return;
        for (final Mesh shape : this.shapes) for (final Vertex v : shape.vertices)
        {

            this.min.x = Math.min(this.min.x, v.x);
            this.min.y = Math.min(this.min.y, v.y);
            this.min.z = Math.min(this.min.z, v.z);

            this.max.x = Math.max(this.max.x, v.x);
            this.max.y = Math.max(this.max.y, v.y);
            this.max.z = Math.max(this.max.z, v.z);
        }
    }

    @Override
    public Vector3 minBound()
    {
        this.initBounds();
        return this.min;
    }

    @Override
    public Vector3 maxBound()
    {
        this.initBounds();
        return this.max;
    }

    @Override
    public void preProcess()
    {
        this.sort(this.order);
        IExtendedModelPart.super.preProcess();
    }

    public void addShape(final Mesh shape)
    {
        this.shapes.add(shape);
        if (shape.material == null) return;
        if (this.matcache.add(shape.material))
        {
            synchronized (materials)
            {
                this.materials.add(shape.material);
            }
            this.namedMaterials.put(shape.material.name, shape.material);
        }
    }

    public void setShapes(final List<Mesh> shapes)
    {
        this.shapes.clear();
        for (final Mesh shape : shapes) this.addShape(shape);
    }

    @Override
    public void applyTexture(final MultiBufferSource bufferIn, final ResourceLocation tex, final IPartTexturer texer)
    {
        for (final Mesh shape : this.shapes)
        {
            ResourceLocation tex_1 = tex;
            // Apply material only, we make these if defined anyay.
            if (texer.hasMapping(shape.material.name)) tex_1 = texer.getTexture(shape.material.name, tex_1);
            shape.material.makeVertexBuilder(tex_1, bufferIn, shape.vertexMode);
        }
    }

    @Override
    public void addChild(final IExtendedModelPart subPart)
    {
        this.parts.put(subPart.getName(), subPart);
        subPart.setParent(this);
    }

    @Override
    public Set<String> getParentNames()
    {
        return parentNames;
    }

    @Override
    public Set<String> getRecursiveChildNames()
    {
        return this.childNames;
    }

    @Override
    public List<Material> getMaterials()
    {
        return this.materials;
    }

    @Override
    public Vector4 getDefaultRotations()
    {
        return this.rotations;
    }

    @Override
    public Vector3 getDefaultTranslations()
    {
        return this.offset;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public IExtendedModelPart getParent()
    {
        return this.parent;
    }

    @Override
    public Map<String, IExtendedModelPart> getSubParts()
    {
        return this.parts;
    }

    @Override
    public void postRender(final PoseStack mat)
    {
        // Pop ours first.
        mat.popPose();

        // Then pop all the parent's
        if (this.parent != null)
        {
            this.parent.postRender(mat);
        }
    }

    @Override
    public void preRender(final PoseStack mat)
    {
        if (this.parent != null) parent.preRender(mat);

        mat.pushPose();

        // Translate of offset for rotation.
        mat.translate(this.preTrans.x, this.preTrans.y, this.preTrans.z);
        mat.scale(this.preScale.x, this.preScale.y, this.preScale.z);
        // // Apply PreOffset-Rotations.
        this.preRot.glRotate(mat);
        // Translate by post-PreOffset amount.
        mat.translate(this.postTrans.x, this.postTrans.y, this.postTrans.z);
        // Apply postRotation
        this.postRot.glRotate(mat);
        // Scale
        mat.scale(this.scale.x, this.scale.y, this.scale.z);
    }

    public void render(final PoseStack mat, final VertexConsumer buffer)
    {
        if (this.isDisabled()) return;

        for (var adder : this.renderAdders) adder.onRender(mat, this);

        this.preRender(mat);
        for (final Mesh s : this.shapes)
        {
            s.renderScale = ds2;
            s.cullScale = ds / ds2;
            // Render each Shape
            s.renderShape(mat, buffer, this.texChangeHolder.get());
        }
        this.postRender(mat);
    }

    @Override
    public void renderAll(final PoseStack mat, final VertexConsumer buffer)
    {
        this.renderAllExcept(mat, buffer, Collections.emptySet());
    }

    @Override
    public void renderAllExcept(final PoseStack mat, final VertexConsumer buffer,
            final Collection<String> excludedGroupNames)
    {
        boolean skip = this.isHidden();
        if (skip || excludedGroupNames.contains(this.name)) return;
        if (!skip)
        {
            for (var part : this.order) part.renderAllExcept(mat, buffer, excludedGroupNames);
            this.render(mat, buffer);
        }
    }

    @Override
    public void renderOnly(final PoseStack mat, final VertexConsumer buffer, final Collection<String> groupNames)
    {
        if (groupNames.contains(this.name))
        {
            this.render(mat, buffer);
            return;
        }
        for (var part : this.order)
        {
            if (part instanceof Part p) p.ds = p.ds0 * this.ds;
            part.renderOnly(mat, buffer, groupNames);
        }
    }

    @Override
    public void renderPart(final PoseStack mat, final VertexConsumer buffer, final String partName)
    {
        if (partName.equalsIgnoreCase(this.name))
        {
            this.render(mat, buffer);
            return;
        }
        for (var part : this.order)
        {
            if (part.getName().equalsIgnoreCase(partName)) part.render(mat, buffer);
        }
    }

    @Override
    public void resetToInit()
    {
        // PreRot is normal animations
        this.preRot.set(rotations.x, rotations.y, rotations.z, rotations.w);
        // Post rot is head direction
        this.postRot.set(0, 0, 0, 1);
        this.preTrans.set(offset);
        this.preScale.set(1, 1, 1);
        this.postTrans.clear();
        this.colour_scales[0] = 1;
        this.colour_scales[1] = 1;
        this.colour_scales[2] = 1;
        this.colour_scales[3] = 1;
        this.hidden = false;
        ds0 = ds = ds2 = 1;
    }

    @Override
    public void setHidden(final boolean hidden)
    {
        this.hidden = hidden;
        for (final IExtendedModelPart part : this.parts.values())
        {
            part.setHidden(hidden);
        }
    }

    @Override
    public boolean isHidden()
    {
        return this.hidden;
    }

    @Override
    public void setParent(final IExtendedModelPart parent)
    {
        this.parent = parent;
    }

    @Override
    public void setPostRotations(final Vector4 angles)
    {
        this.postRot = angles;
    }

    @Override
    public void setPostTranslations(final Vector3 point)
    {
        this.postTrans.set(point);
    }

    @Override
    public void setPreRotations(Vector4 angles)
    {
        this.preRot.mul(rotations, angles);
    }

    @Override
    public void setDefaultAngles(float rx, float ry, float rz)
    {
        _quat.set(0, 0, 0, 1);
        if (rz != 0) _quat.mul(AxisAngles.YN.rotationDegrees(rz));
        if (rx != 0) _quat.mul(AxisAngles.XP.rotationDegrees(rx));
        if (ry != 0) _quat.mul(AxisAngles.ZP.rotationDegrees(ry));
        _rot.set(_quat);
        this.preRot.mul(rotations, _rot);
        this.rotations.set(preRot.x, preRot.y, preRot.z, preRot.w);
    }

    @Override
    public void setAnimAngles(float rx, float ry, float rz)
    {
        _quat.set(0, 0, 0, 1);
        if (rz != 0) _quat.mul(AxisAngles.YN.rotationDegrees(rz));
        if (rx != 0) _quat.mul(AxisAngles.XP.rotationDegrees(rx));
        if (ry != 0) _quat.mul(AxisAngles.ZP.rotationDegrees(ry));
        this.setPreRotations(_rot.set(_quat));
    }

    @Override
    public void setPreScale(final Vector3 scale)
    {
        this.preScale.x = (float) scale.x;
        this.preScale.y = (float) scale.y;
        this.preScale.z = (float) scale.z;
        ds0 = ds = (float) scale.mag();
    }

    @Override
    public void setPreTranslations(final Vector3 point)
    {
        this.preTrans.set(offset).addTo(point);
    }

    @Override
    public void setRGBABrO(@Nullable Predicate<Material> material, final int r, final int g, final int b, final int a,
            final int br, final int o)
    {
        if (br != Integer.MIN_VALUE)
        {
            this.brightness = br;
            this.overlay = o;
        }
        if (material != null && !Mesh.debug)
        {
            this.materials.forEach(m -> {
                if (m == null) return;
                if (material.test(m))
                {
                    m.rgbabro[0] = (int) (r * this.colour_scales[0]);
                    m.rgbabro[1] = (int) (g * this.colour_scales[1]);
                    m.rgbabro[2] = (int) (b * this.colour_scales[2]);
                    m.rgbabro[3] = (int) (a * this.colour_scales[3]);
                    m.rgbabro[4] = this.brightness;
                    m.rgbabro[5] = this.overlay;
                }
            });
        }
        else
        {
            shapes.forEach(m -> {
                if (m == null) return;
                m.rgbabro[0] = (int) (r * this.colour_scales[0]);
                m.rgbabro[1] = (int) (g * this.colour_scales[1]);
                m.rgbabro[2] = (int) (b * this.colour_scales[2]);
                m.rgbabro[3] = (int) (a * this.colour_scales[3]);
                m.rgbabro[4] = this.brightness;
                m.rgbabro[5] = this.overlay;
            });
        }
    }

    @Override
    public void updateMaterial(final Mat mat, final Material material)
    {
        if (mat.meshs == null) mat.meshs = "";
        String[] parts = mat.meshs.split(":");
        if (mat.meshs.equals(this.getName()))
        {
            for (final Mesh mesh : this.shapes) mesh.setMaterial(material);
        }
        else for (final String s : parts) for (final Mesh mesh : this.shapes)
        {
            if (mesh.name == null) mesh.name = this.getName();
            if (mesh.name.equals(ThutCore.trim(s)) || mesh.name.equals(mat.name) || this.getName().equals(s))
            {
                mesh.setMaterial(material);
            }
        }
        if (material == null)
        {
            ThutCore.LOGGER.error("Error loading a material, trying to set it to null: {}", JsonUtil.gson.toJson(mat));
            ThutCore.LOGGER.error(new IllegalAccessException());
        }
        synchronized (materials)
        {
            this.matcache.clear();
            this.materials.clear();
            this.namedMaterials.clear();
            for (Mesh shape : this.shapes)
            {
                if (this.matcache.add(shape.material))
                {
                    this.materials.add(shape.material);
                    this.namedMaterials.put(shape.material.name, shape.material);
                }
            }
        }
    }

    @Override
    public List<IExtendedModelPart> getRenderOrder()
    {
        return this.order;
    }

    @Override
    public void setDisabled(boolean disabled)
    {
        this.disabled = disabled;
    }

    @Override
    public boolean isDisabled()
    {
        return disabled;
    }

    @Override
    public void setPostScale(Vector3 scale)
    {
        ds2 = (float) scale.mag();
    }

    @Override
    public void setColorScales(float r, float g, float b, float a)
    {
        r = Math.max(0, Math.min(r, 1));
        g = Math.max(0, Math.min(g, 1));
        b = Math.max(0, Math.min(b, 1));
        a = Math.max(0, Math.min(a, 1));

        this.colour_scales[0] = r;
        this.colour_scales[1] = g;
        this.colour_scales[2] = b;
        this.colour_scales[3] = a;
    }

    @Override
    public void setHeadPart(final boolean isHead)
    {
        this.isHead = isHead;
    }

    @Override
    public boolean isHeadPart()
    {
        return isHead;
    }

    @Override
    public void addPartRenderAdder(IPartRenderAdder adder)
    {
        if (adder.shouldAddTo(this)) this.renderAdders.add(adder);
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
}
