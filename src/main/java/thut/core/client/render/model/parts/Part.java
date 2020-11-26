package thut.core.client.render.model.parts;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.ResourceLocation;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.core.client.render.animation.AnimationXML.Mat;
import thut.core.client.render.animation.CapabilityAnimation.IAnimationHolder;
import thut.core.client.render.animation.IAnimationChanger;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.Vertex;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.texturing.IRetexturableModel;
import thut.core.common.ThutCore;

public abstract class Part implements IExtendedModelPart, IRetexturableModel
{
    private final HashMap<String, IExtendedModelPart> parts = new HashMap<>();

    private final List<String> order  = Lists.newArrayList();
    private final List<Mesh>   shapes = Lists.newArrayList();

    private final String name;

    private IExtendedModelPart parent = null;

    IPartTexturer     texturer;
    IAnimationChanger changer;
    IAnimationHolder  currentHolder = null;

    public Vector4 preRot    = new Vector4();
    public Vector4 postRot   = new Vector4();
    public Vector3 preTrans  = Vector3.getNewVector();
    public Vector3 postTrans = Vector3.getNewVector();
    public Vertex  preScale  = new Vertex(1, 1, 1);

    public Vector3 offset    = Vector3.getNewVector();
    public Vector4 rotations = new Vector4();
    public Vertex  scale     = new Vertex(1, 1, 1);

    public int red = 255, green = 255, blue = 255, alpha = 255;

    public int brightness = 15728640;
    public int overlay    = 655360;

    private final int[] rgbabro = new int[6];

    private boolean hidden = false;

    private final List<Material> materials = Lists.newArrayList();
    private final Set<Material>  matcache  = Sets.newHashSet();

    public Part(final String name)
    {
        this.name = name;
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
        if (this.matcache.add(shape.material)) this.materials.add(shape.material);
    }

    public void setShapes(final List<Mesh> shapes)
    {
        this.shapes.clear();
        this.shapes.addAll(shapes);
        for (final Mesh shape : shapes)
        {
            if (shape.material == null) continue;
            if (this.matcache.add(shape.material)) this.materials.add(shape.material);
        }
    }

    @Override
    public void applyTexture(final IRenderTypeBuffer bufferIn, final ResourceLocation tex, final IPartTexturer texer)
    {
        for (final Mesh shape : this.shapes)
        {
            ResourceLocation tex_1 = tex;
            // System.out.println(shape + " " + shape.material + " " +
            // shape.material.flat);
            // Apply material only, we make these if defined anyay.
            if (texer.hasMapping(shape.material.name)) tex_1 = texer.getTexture(shape.material.name, tex);
            shape.material.makeVertexBuilder(tex_1, bufferIn);
        }
    }

    @Override
    public void addChild(final IExtendedModelPart subPart)
    {
        this.parts.put(subPart.getName(), subPart);
        subPart.setParent(this);
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
    public HashMap<String, IExtendedModelPart> getSubParts()
    {
        return this.parts;
    }

    private void postRender(final MatrixStack mat)
    {
        // Pop ours first.
        mat.pop();
        // Then pop all the parent's
        if (this.parent != null) this.parent.unRotateForChild(mat);
    }

    private void preRender(final MatrixStack mat)
    {
        mat.push();
        mat.scale(this.preScale.x, this.preScale.y, this.preScale.z);
        // Translate of offset for rotation.
        mat.translate(this.offset.x, this.offset.y, this.offset.z);
        mat.translate(this.preTrans.x, this.preTrans.y, this.preTrans.z);
        // // Apply initial part rotation
        this.rotations.glRotate(mat);
        // // Apply PreOffset-Rotations.
        this.preRot.glRotate(mat);
        // Translate by post-PreOffset amount.
        mat.translate(this.postTrans.x, this.postTrans.y, this.postTrans.z);

        // Apply postRotation
        this.postRot.glRotate(mat);

        // Scale
        mat.scale(this.scale.x, this.scale.y, this.scale.z);
    }

    public void render(final MatrixStack mat, final IVertexBuilder buffer)
    {
        if (this.hidden) return;
        for (final Mesh s : this.shapes)
        {
            // Fill the int array in here, as the rendering can adjust it.
            s.rgbabro = this.getRGBABrO();
            // Render each Shape
            s.renderShape(mat, buffer, this.texturer);
        }
    }

    @Override
    public void renderAll(final MatrixStack mat, final IVertexBuilder buffer)
    {
        this.renderAllExcept(mat, buffer, "");
    }

    @Override
    public void renderAllExcept(final MatrixStack mat, final IVertexBuilder buffer, final String... excludedGroupNames)
    {
        boolean skip = this.hidden;
        for (final String s1 : excludedGroupNames)
            if (skip = s1.equalsIgnoreCase(this.name)) break;
        if (!skip)
        {
            this.preRender(mat);
            for (final String s : this.order)
            {
                final IExtendedModelPart o = this.parts.get(s);
                o.renderAllExcept(mat, buffer, excludedGroupNames);
            }
            this.render(mat, buffer);
            this.postRender(mat);
        }
    }

    @Override
    public void renderOnly(final MatrixStack mat, final IVertexBuilder buffer, final String... groupNames)
    {
        boolean rendered = false;
        for (final String s1 : groupNames)
            if (rendered = s1.equalsIgnoreCase(this.name))
            {
                this.preRender(mat);
                this.render(mat, buffer);
                this.postRender(mat);
                break;
            }
        if (!rendered)
        {
            this.preRender(mat);
            for (final String s : this.order)
            {
                final IExtendedModelPart o = this.parts.get(s);
                o.renderOnly(mat, buffer, groupNames);
            }
            this.postRender(mat);
        }
    }

    @Override
    public void renderPart(final MatrixStack mat, final IVertexBuilder buffer, final String partName)
    {
        this.renderOnly(mat, buffer, partName);
    }

    @Override
    public void resetToInit()
    {
        // PreRot is normal animations
        this.preRot.set(0, 0, 0, 1);
        // Post rot is head direction
        this.postRot.set(0, 0, 0, 1);
        this.preTrans.clear();
        this.postTrans.clear();
        this.hidden = false;
    }

    @Override
    public void rotateForChild(final MatrixStack mat)
    {
        if (this.parent != null) this.parent.rotateForChild(mat);
    }

    @Override
    public void unRotateForChild(final MatrixStack mat)
    {
        if (this.parent != null) this.parent.unRotateForChild(mat);
    }

    @Override
    public void setAnimationChanger(final IAnimationChanger changer)
    {
        this.changer = changer;
        for (final IExtendedModelPart part : this.parts.values())
            if (part instanceof IRetexturableModel) ((IRetexturableModel) part).setAnimationChanger(changer);
    }

    @Override
    public void setHidden(final boolean hidden)
    {
        this.hidden = hidden;
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
    public void setPreRotations(final Vector4 angles)
    {
        this.preRot = angles;
    }

    @Override
    public void setPreScale(final Vector3 scale)
    {
        this.preScale.x = (float) scale.x;
        this.preScale.y = (float) scale.y;
        this.preScale.z = (float) scale.z;
    }

    @Override
    public void setPreTranslations(final Vector3 point)
    {
        this.preTrans.set(point);
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

    @Override
    public void setTexturer(final IPartTexturer texturer)
    {
        this.texturer = texturer;
        for (final IExtendedModelPart part : this.parts.values())
            if (part instanceof IRetexturableModel) ((IRetexturableModel) part).setTexturer(texturer);
    }

    @Override
    public void updateMaterial(final Mat mat, final Material material)
    {
        final String[] parts = mat.name.split(":");
        for (final String s : parts)
            for (final Mesh mesh : this.shapes)
            {
                if (mesh.name == null) mesh.name = this.getName();
                if (mesh.name.equals(ThutCore.trim(s)) || mesh.name.equals(mat.name)) mesh.setMaterial(material);
            }
        for (final Material m : this.materials)
            if (m.name.equals(mat.name))
            {
                this.matcache.remove(m);
                this.materials.remove(m);
                break;
            }
        this.matcache.add(material);
        this.materials.add(material);
    }

    @Override
    public IAnimationHolder getAnimationHolder()
    {
        return this.currentHolder;
    }

    @Override
    public void setAnimationHolder(final IAnimationHolder holder)
    {
        this.currentHolder = holder;
    }
}
