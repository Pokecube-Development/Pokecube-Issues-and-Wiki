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
import org.joml.Vector4f;
import thut.api.entity.IAnimated.IAnimationHolder;
import thut.api.entity.animation.IAnimationChanger;
import thut.api.maths.Vector4;
import thut.api.util.JsonUtil;
import thut.core.client.render.animation.AnimationXML.Mat;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.texturing.IRetexturableModel;
import thut.core.common.ThutCore;
import thut.lib.AxisAngles;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public abstract class Part implements IExtendedModelPart, IRetexturableModel
{
    public static boolean mergeMeshes = true;

    protected final Map<String, IExtendedModelPart> parts = new Object2ObjectOpenHashMap<>();

    protected final List<IPartRenderAdder> renderAdders = new ArrayList<>();
    protected final List<IExtendedModelPart> order = new ArrayList<>();
    protected final List<Mesh> shapes = new ArrayList<>();
    protected final List<Mesh> renderShapes = new ArrayList<>();

    public String name;

    protected IExtendedModelPart parent = null;

    IRetexturableModel.Holder<IAnimationChanger> animChangeHolder = new IRetexturableModel.Holder<>();
    IRetexturableModel.Holder<IAnimationHolder> animHolderHolder = new IRetexturableModel.Holder<>();
    IRetexturableModel.Holder<IPartTexturer> texChangeHolder = new IRetexturableModel.Holder<>();
    IRetexturableModel.Holder<IPartTexturer> transientTexChangeHolder = new IRetexturableModel.Holder<>();

    public Vector4 preRot = new Vector4();
    public Vector4 postRot = new Vector4();
    public Vector3f preTrans = new Vector3f();
    public Vector3f postTrans = new Vector3f();
    public Vector3f preScale = new Vector3f(1, 1, 1);
    public Vector3f scale = new Vector3f(1, 1, 1);
    public Vector3f postScale = new Vector3f(1, 1, 1);

    public Vector3f offset = new Vector3f();
    public Vector3f meshMid = new Vector3f(), dMid = new Vector3f();
    public Vector4 rotations = new Vector4();

    protected Quaternionf _quat = new Quaternionf(0, 0, 0, 1);
    protected Vector4 _rot = new Vector4();

    private float ds = 1;
    private float ds2 = 1;

    public float[] colour_scales = { 1f, 1f, 1f, 1f };

    public int brightness = 15728640;
    public int overlay = 655360;
    private int depth = 0;

    // Set this true to mark animations for this as limb based if they are
    // normal keyframes
    public boolean isOverridenLimb = false;
    public boolean isCustomColour = false;

    private boolean hidden = false;
    private boolean disabled = false;
    private boolean isHead = false;
    private boolean isAnimated = false;
    private boolean is2D = false;

    protected final PoseInfo renderPose = new PoseInfo();

    protected final List<Material> materials = Lists.newArrayList();
    protected final Map<String, Material> namedMaterials = new Object2ObjectOpenHashMap<>();
    protected final Set<Material> matcache = Sets.newHashSet();

    private final Set<String> parentNames = Sets.newHashSet();
    private final Set<String> childNames = Sets.newHashSet();

    public Part(final String name)
    {
        this.name = name;
    }

    /**
     * This occurs outside the main render loop,
     * synchronized and slow blocks are "fine".
     */
    @Override
    public void tryCombineChildren()
    {
        this.resetToInit();
        for(var _p: new ArrayList<>(this.parts.values()))
        {
            // Only our direct children.
            // Only ones not starting with __, as those are special for worn things, etc
            // Only ones with no children
            if( _p.getParent()==this
                &&!_p.isAnimated()
                &&!_p.getName().startsWith("__")
                &&_p instanceof Part p
                && p.parts.isEmpty()
                )
            {
                // TODO handle these cases properly
                if(p.offset.lengthSquared()!=0) continue;
                if(!p.rotations.isEmpty()) continue;

                // Attempt to merge the part in to us.
                var mats = p.getMaterials().stream().map(m->m.name);
                boolean allMatch = mats.allMatch(this.namedMaterials::containsKey);

                if(allMatch && mergeMeshes)
                {
                    p.resetToInit();
                    p.transformForRender();
                    Vector4f dp = new Vector4f();
                    var norms = p.getRenderPose().normal();
                    var pos = p.getRenderPose().pose();
                    for(var mesh: p.shapes)
                    {
                        Set<Vector3f> process = new HashSet<>(Arrays.asList(mesh.normals));
                        process.addAll(Arrays.asList(mesh.normalList));
                        for(var n: process)
                        {
                            n.mul(norms);
                        }
                        process = new HashSet<>(Arrays.asList(mesh.vertices));
                        for(var v: process)
                        {
                            dp.set(v, 1);
                            dp.mul(pos);
                            v.set(dp.x, dp.y, dp.z);
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
    public int getDepth()
    {
        return depth;
    }

    @Override
    public void setDepth(int n)
    {
        this.depth = n;
    }

    /**
     * This occurs outside the main render loop,
     * synchronized and slow blocks are "fine".
     */
    @Override
    public void preProcess()
    {
        synchronized (this.order)
        {
            this.order.clear();
            this.order.addAll(this.getSubParts().values());
            IExtendedModelPart.super.preProcess();
            this.renderShapes.clear();
            List<List<Mesh>> allMeshes = new ArrayList<>();
            for (var mesh : this.shapes)
            {
                var listOpt = allMeshes.stream()
                        .filter(_list -> _list.stream().anyMatch(m -> m.material.compareTo(mesh.material) == 0))
                        .findFirst();
                var list = listOpt.orElse(new ArrayList<>());
                if (listOpt.isEmpty()) allMeshes.add(list);
                list.add(mesh);
            }
            for (var list : allMeshes) renderShapes.addAll(Mesh.merge(list));
            this.shapes.clear();
            this.shapes.addAll(this.renderShapes);

            this.meshMid.set(0);
            int n = 0;
            this.is2D = true;
            Vector3f norm = null;
            double epsD = 1e-10;
            for (var m : this.renderShapes)
            {
                if (norm == null) norm = m.normalList[0];
                for (var v : m.vertices)
                {
                    n++;
                    this.meshMid.add(v);
                }
                this.is2D &= m.is2D && Math.abs(norm.dot(m.normalList[0]))>1-epsD;
            }
            if (n > 0) this.meshMid.div(n);
        }
    }

    @Override
    public List<Mesh> getRenderMeshes()
    {
        return renderShapes;
    }

    /**
     * This occurs outside the main render loop,
     * synchronized and slow blocks are "fine".
     */
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

    /**
     * This occurs outside the main render loop,
     * synchronized and slow blocks are "fine".
     */
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
            var tex_1 = tex;
            var material = shape.getRenderMaterial();
            // Apply material only, we make these if defined anyay.
            if (texer.hasMapping(material.name)) tex_1 = texer.getTexture(material.name, tex_1);
            material.makeVertexBuilder(tex_1, bufferIn, shape.vertexMode);
        }
    }

    /**
     * This can occur during render thread, mostly for adding layers
     */
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
        mat.scale(this.scale.x, this.scale.y, this.scale.z);
    }

    public void render(final PoseStack mat, final VertexConsumer buffer)
    {
        if (this.isDisabled()) return;
        // TODO render adders for new rendering setup
        Material.startRender();
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
        this.preScale.set(1);
        this.postTrans.set(0);

        this.colour_scales[0] = 1;
        this.colour_scales[1] = 1;
        this.colour_scales[2] = 1;
        this.colour_scales[3] = 1;
        this.hidden = false;
        this.isCustomColour = false;
        ds = ds2 = 1;

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
            if(parent.isHidden()) this.hidden = true;
        }
        // Now apply the transforms from preRender
        // Translate of offset for rotation.
        renderPose.translate(this.preTrans);
        renderPose.scale(this.preScale);
        // // Apply PreOffset-Rotations.
        renderPose.rotate(preRot.toMCQ());
        // Translate by post-PreOffset amount.
        renderPose.translate(this.postTrans);
        // Apply postRotation
        renderPose.rotate(postRot.toMCQ());
        // Finally apply Scale
        renderPose.scale(this.scale);

        boolean hasScale = Math.abs(this.postScale.lengthSquared() - 3) > 1e-4;
        if(hasScale) dMid.set(this.meshMid).div(this.postScale).sub(this.meshMid);

        for(var m: this.renderShapes)
        {
            m.hidden = this.isHidden() || this.isDisabled();
            m.texChangeHolder = this.transientTexChangeHolder;
            m.poseInfo.set(this.renderPose);
            // If we have a custom post scale, we apply it to the mesh directly
            // This prevents it compounding on parent scales, and allows the post scale
            // to expand out from the model. Post scale effects are used for things like
            // status overlays, etc.
            if(hasScale)
            {
                m.poseInfo.scale(this.postScale);
                m.poseInfo.translate(dMid);
            }
        }
        if (hasScale) this.postScale.set(1);
        // Reset transient holder
        this.transientTexChangeHolder = this.texChangeHolder;
    }

    @Override
    public void setHidden(final boolean hidden)
    {
        this.hidden = hidden;
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
    public void setPreScale(final Vector3f scale)
    {
        this.preScale.x = scale.x;
        this.preScale.y = scale.y;
        this.preScale.z = scale.z;
        ds = scale.length();
    }

    @Override
    public void setPreTranslations(final Vector3f point)
    {
        this.preTrans.set(offset).add(point.x, point.y, point.z);
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
        renderShapes.forEach(m -> {
            if (m == null) return;
            var _mat = m.getRenderMaterial();
            if (material != null && !Mesh.debug && !material.test(_mat)) return;
            m.overrideColour = this.isCustomColour;
            var _rgabro = m.rgbabro;
            _rgabro[0] = (int) (r * this.colour_scales[0]);
            _rgabro[1] = (int) (g * this.colour_scales[1]);
            _rgabro[2] = (int) (b * this.colour_scales[2]);
            _rgabro[3] = (int) (a * this.colour_scales[3]);
            _rgabro[4] = this.brightness;
            _rgabro[5] = this.overlay;
        });
    }

    @Override
    public void setColourOverridden()
    {
        this.isCustomColour = true;
    }

    /**
     * This occurs outside the main render loop,
     * synchronized and slow blocks are "fine".
     */
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

    /**
     * This occurs outside the main render loop,
     * synchronized and slow blocks are "fine".
     */
    @Override
    public void updateMaterials(List<Material> materials)
    {
        synchronized (this.materials)
        {
            this.matcache.clear();
            this.materials.clear();
            this.namedMaterials.clear();
            for (Mesh shape : this.shapes)
            {
                var old = shape.material;
                var matOpt = materials.stream().filter(m1->old.compareTo(m1)==0).findFirst();
                var mat = matOpt.orElse(old);
                shape.material = mat;
                shape.renderMaterial = mat;
                if (this.matcache.add(mat))
                {
                    this.materials.add(mat);
                    this.namedMaterials.put(mat.name, mat);
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
    public void mulPostScale(Vector3f scale)
    {
        ds2 *= scale.length();
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

    /**
     * This occurs outside the main render loop,
     * synchronized and slow blocks are "fine".
     */
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
    public void setChangers(Holder<IAnimationChanger> animations, Holder<IPartTexturer> textures)
    {
        this.animChangeHolder = animations;
        this.texChangeHolder = this.transientTexChangeHolder = textures;
        for (var part : this.getPartsList()) if (part instanceof IRetexturableModel p)
        {
            p.setChangers(animations, textures);
        }
    }

    @Override
    public Holder<IPartTexturer> getTexturerChanger()
    {
        return this.transientTexChangeHolder;
    }

    @Override
    public void setTransientTexturerChanger(Holder<IPartTexturer> input)
    {
        this.transientTexChangeHolder = input;
        for (var part : this.getPartsList()) if (part instanceof IRetexturableModel p) p.setTransientTexturerChanger(input);
    }

    @Override
    public boolean is2D()
    {
        return this.is2D;
    }
}
