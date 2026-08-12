package thut.core.client.render.model.parts;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;
import org.joml.Vector3f;
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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public abstract class Part implements IExtendedModelPart, IRetexturableModel
{
    protected final Map<String, IExtendedModelPart> parts = new Object2ObjectOpenHashMap<>();

    protected final List<IPartRenderAdder> renderAdders = new ArrayList<>();
    protected final List<IExtendedModelPart> order = new ArrayList<>();
    protected final List<Mesh> shapes = new ArrayList<>();
    protected final List<Mesh> renderShapes = new ArrayList<>();

    private final String name;

    protected IExtendedModelPart parent = null;

    IRetexturableModel.Holder<IAnimationChanger> animChangeHolder = new IRetexturableModel.Holder<>();
    IRetexturableModel.Holder<IAnimationHolder> animHolderHolder = new IRetexturableModel.Holder<>();
    IRetexturableModel.Holder<IPartTexturer> texChangeHolder = new IRetexturableModel.Holder<>();

    public Vector4 preRot = new Vector4();
    public Vector4 postRot = new Vector4();
    public Vector3 preTrans = new Vector3();
    public Vector3 postTrans = new Vector3();
    public Vertex preScale = new Vertex(1, 1, 1);
    public Vertex postScale = new Vertex(1, 1, 1);

    public Vector3 offset = new Vector3();
    public Vector4 rotations = new Vector4();
    public Vertex scale = new Vertex(1, 1, 1);

    protected Quaternionf _quat = new Quaternionf(0, 0, 0, 1);
    protected Vector4 _rot = new Vector4();

    private float ds = 1;
    public float ds0 = 1;
    public float ds1 = 1;
    private float ds2 = 1;

    public float[] colour_scales = { 1f, 1f, 1f, 1f };

    public int brightness = 15728640;
    public int overlay = 655360;

    // Set this true to mark animations for this as limb based if they are
    // normal keyframes
    public boolean isOverridenLimb = false;

    private boolean hidden = false;
    private boolean disabled = false;
    private boolean isHead = false;
    private boolean isAnimated = false;

    protected final PoseInfo renderPose = new PoseInfo();

    protected final List<Material> materials = Lists.newArrayList();
    protected final Map<String, Material> namedMaterials = new Object2ObjectOpenHashMap<>();
    protected final Set<Material> matcache = Sets.newHashSet();

    private Set<String> parentNames = Sets.newHashSet();
    private Set<String> childNames = Sets.newHashSet();

    public Part(final String name)
    {
        this.name = name;
    }

    @Override
    public void tryCombineChildren()
    {
        for(var _p: new ArrayList<>(this.parts.values()))
        {
            // Only our direct children.
            // Only ones not starting with __, as those are special for worn things, etc
            // Only ones with no children
            if(_p.getParent()==this&&!_p.isAnimated()
                &&!_p.getName().startsWith("__")
                &&_p instanceof Part p
                &&p.childNames.isEmpty()
                // TODO later merge rotations and offset properly?
                &&p.offset.magSq()==0
                &&p.rotations.isEmpty()
                )
            {
                // Attempt to merge the part in to us.
                var mats = p.getMaterials().stream().map(m->m.name);
                boolean allMatch = mats.allMatch(this.namedMaterials::containsKey);
                PoseStack stack_there = new PoseStack();
                p.preRender(stack_there);
                if(allMatch)
                {
                    Vector3f pShift = new Vector3f((float)p.offset.x,(float)p.offset.y,(float)p.offset.z);
                    for(var mesh: p.shapes)
                    {
                        Set<Vertex> verts = new HashSet<>(Arrays.asList(mesh.vertices));
                        for(var vert: verts){
                            vert.add(pShift);
                        }
                        this.addShape(mesh);
                    }
                    this.order.remove(p);
                    this.parts.remove(p.name);
                    this.childNames.remove(p.name);

                    p.shapes.clear();
                    p.renderShapes.clear();
                    p.order.clear();
                    p.parts.clear();
                    p.childNames.clear();
                    p.materials.clear();
                }
            }
        }
    }

    @Override
    public void preProcess()
    {
        synchronized (this.order)
        {
            this.order.clear();
            this.order.addAll(this.getSubParts().values());
            IExtendedModelPart.super.preProcess();
            this.renderShapes.clear();
            Map<String, List<Mesh>> allShapes = new HashMap<>();
            for (var mesh : this.shapes)
            {
                var key = mesh.material.name;
                allShapes.computeIfAbsent(key, m -> new ArrayList<>()).add(mesh);
            }
            for (var pair : allShapes.entrySet())
            {
                var meshs = pair.getValue();
                if (meshs.isEmpty()) continue;
                Mesh mesh;
                if (meshs.size() > 1)
                {
                    mesh = Mesh.merge(meshs.toArray(new Mesh[0]));
                }
                else
                {
                    mesh = meshs.getFirst();
                }
                if (mesh != null)
                {
                    renderShapes.add(mesh);
                }
                else
                {
                    renderShapes.addAll(meshs);
                }
            }
        }
    }

    @Override
    public List<Mesh> getRenderMeshes()
    {
        return renderShapes;
    }

    public void addShape(final Mesh shape)
    {
        this.shapes.add(shape);
        shape.texChangeHolder = ()->this.getTexturerChanger().get();
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
        for (final Mesh shape : this.renderShapes)
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
        mat.scale(this.postScale.x, this.postScale.y, this.postScale.z);
    }

    public void render(final PoseStack mat, final VertexConsumer buffer)
    {
        if (this.isDisabled()) return;
        // TODO render adders for new rendering setup
        for (var adder : this.renderAdders) adder.onRender(mat, this);
        this.preRender(mat);
        for (final Mesh s : this.renderShapes)
        {
            s.cullScale = ds / ds2;
            // Render each Shape
            s.setPose(mat);
            s.renderShape(buffer);
        }
        this.postRender(mat);
    }

    @Override
    public void renderLegacy(final PoseStack mat, final VertexConsumer buffer)
    {
        boolean skip = this.isHidden();
        if (skip) return;
        for (var part : this.order) part.render(mat, buffer);
        this.render(mat, buffer);
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
        this.postScale.set(this.scale);
        this.colour_scales[0] = 1;
        this.colour_scales[1] = 1;
        this.colour_scales[2] = 1;
        this.colour_scales[3] = 1;
        this.hidden = false;
        ds0 = ds = 1;

        renderPose.pose().identity();
        renderPose.normal().identity();
    }

    @Override
    public PoseInfo getRenderPose()
    {
        return renderPose;
    }

    @Override
    public void transformForRender()
    {
        // First set to wherever the parent is. Parents should have had this called first.
        if (parent != null)
        {
            renderPose.set(parent.getRenderPose());
        }
        // Now apply the transforms from preRender
        // Translate of offset for rotation.
        renderPose.translate(this.preTrans.x, this.preTrans.y, this.preTrans.z);
        renderPose.scale(this.preScale.x, this.preScale.y, this.preScale.z);
        // // Apply PreOffset-Rotations.
        renderPose.rotate(preRot.toMCQ());
        // Translate by post-PreOffset amount.
        renderPose.translate(this.postTrans.x, this.postTrans.y, this.postTrans.z);
        // Apply postRotation
        renderPose.rotate(postRot.toMCQ());
        // Scale
        renderPose.scale(this.postScale.x, this.postScale.y, this.postScale.z);

        for(var m: this.renderShapes)
        {
            m.hidden = this.isHidden() || this.isDisabled();
            m.poseInfo.set(this.renderPose);
        }
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
    public void markAsAnimated()
    {
        this.isAnimated = true;
    }

    @Override
    public boolean isAnimated()
    {
        return this.isAnimated;
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
            renderShapes.forEach(m -> {
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
        else for (final String s : parts)
            for (final Mesh mesh : this.shapes)
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
    public void updateMaterials(Collection<Material> materials)
    {
        synchronized (this.materials)
        {
            Map<String, Material> _mats = new HashMap<>();
            materials.forEach(mat->_mats.put(mat.render_name, mat));
            this.matcache.clear();
            this.materials.clear();
            this.namedMaterials.clear();
            for (Mesh shape : this.shapes)
            {
                var old = shape.material;
                var key = old.render_name;
                if(_mats.containsKey(key))
                {
                    var mat = _mats.get(key);
                    shape.material = mat;
                    if (this.matcache.add(mat))
                    {
                        this.materials.add(mat);
                        this.namedMaterials.put(mat.name, mat);
                    }
                }
                else
                {
                    System.err.println("Not found in new list! "+old.name);
                }
            }
        }
        this.getSubParts().values().forEach(part->part.updateMaterials(materials));
    }

    @Override
    public List<IExtendedModelPart> getPartsList()
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
    public void setPostScale(Vector3f scale)
    {
        ds2 = scale.length();
        this.postScale.mul(scale);
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
        for (var part : this.getPartsList()) part.setAnimationHolder(input);
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
        for (var part : this.getPartsList()) if (part instanceof IRetexturableModel p) p.setAnimationChanger(input);
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
        for (var part : this.getPartsList()) if (part instanceof IRetexturableModel p) p.setTexturerChanger(input);
    }
}
