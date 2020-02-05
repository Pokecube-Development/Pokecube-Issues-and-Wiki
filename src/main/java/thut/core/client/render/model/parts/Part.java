package thut.core.client.render.model.parts;

import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.Vector3f;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.core.client.render.animation.IAnimationChanger;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.Vertex;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.texturing.IRetexturableModel;

public abstract class Part implements IExtendedModelPart, IRetexturableModel
{
    private final HashMap<String, IExtendedModelPart> childParts = new HashMap<>();
    public List<Mesh>                                 shapes     = Lists.newArrayList();

    private final String                              name;
    private IExtendedModelPart                        parent     = null;
    IPartTexturer                                     texturer;
    IAnimationChanger                                 changer;

    public Vector4                                    preRot     = new Vector4();
    public Vector4                                    postRot    = new Vector4();
    public Vector4                                    postRot1   = new Vector4();
    public Vector3                                    preTrans   = Vector3.getNewVector();
    public Vector3                                    postTrans  = Vector3.getNewVector();
    public Vertex                                     preScale   = new Vertex(1, 1, 1);

    public Vector3                                    offset     = Vector3.getNewVector();
    public Vector4                                    rotations  = new Vector4();
    public Vertex                                     scale      = new Vertex(1, 1, 1);

    public int                                        red        = 255, green = 255, blue = 255, alpha = 255;
    public int                                        brightness = 15728640;
    public int                                        overlay    = 0;

    private final int[]                               rgbabro    = new int[6];

    private boolean                                   hidden     = false;

    public Part(final String name)
    {
        this.name = name;
    }

    @Override
    public void addChild(final IExtendedModelPart subPart)
    {
        this.childParts.put(subPart.getName(), subPart);
        subPart.setParent(this);
    }

    public void addForRender(final MatrixStack mat, final IVertexBuilder buffer)
    {
        // Fill the int array
        this.getRGBABrO();
        for (final Mesh s : this.shapes)
        {
            s.rgbabro = this.rgbabro;
            // Render each Shape
            s.renderShape(mat, buffer, this.texturer);
        }
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

    @SuppressWarnings("unchecked")
    @Override
    public HashMap<String, IExtendedModelPart> getSubParts()
    {
        return this.childParts;
    }

    private void postRender(final MatrixStack mat, final IVertexBuilder buffer)
    {
        mat.pop();
    }

    private void preRender(final MatrixStack mat, final IVertexBuilder buffer)
    {
        // Rotate to the offset of the parent.
        this.rotateToParent(mat, buffer);
        // Translate of offset for rotation.
        mat.translate(this.offset.x, this.offset.y, this.offset.z);
        // Rotate by this to account for a coordinate difference.
        mat.rotate(Vector3f.XP.rotationDegrees(90));
        mat.translate(this.preTrans.x, this.preTrans.y, this.preTrans.z);
        // UnRotate coordinate difference.
        mat.rotate(Vector3f.XN.rotationDegrees(90));
        // Apply initial part rotation
        this.rotations.glRotate(mat);
        // Rotate by this to account for a coordinate difference.
        mat.rotate(Vector3f.XP.rotationDegrees(90));
        // Apply PreOffset-Rotations.
        this.preRot.glRotate(mat);
        // Translate by post-PreOffset amount.
        mat.translate(this.postTrans.x, this.postTrans.y, this.postTrans.z);
        // UnRotate coordinate difference.
        mat.rotate(Vector3f.XN.rotationDegrees(90));
        // Undo pre-translate offset.
        mat.translate(-this.offset.x, -this.offset.y, -this.offset.z);
        mat.push();
        // Translate to Offset.
        mat.translate(this.offset.x, this.offset.y, this.offset.z);

        // Apply first postRotation
        this.postRot.glRotate(mat);
        // Apply second post rotation.
        this.postRot1.glRotate(mat);
        // Scale
        mat.scale(this.scale.x, this.scale.y, this.scale.z);
    }

    public void render(final MatrixStack mat, final IVertexBuilder buffer)
    {
        if (this.hidden) return;
        this.preRender(mat, buffer);
        // Renders the model.
        this.addForRender(mat, buffer);
        this.postRender(mat, buffer);
    }

    @Override
    public void renderAll(final MatrixStack mat, final IVertexBuilder buffer)
    {
        mat.scale(this.preScale.x, this.preScale.y, this.preScale.z);
        this.render(mat, buffer);
        for (final IExtendedModelPart o : this.childParts.values())
        {
            mat.push();
            mat.translate(this.offset.x, this.offset.y, this.offset.z);
            mat.scale(this.scale.x, this.scale.y, this.scale.z);
            o.renderAll(mat, buffer);
            mat.pop();
        }
    }

    @Override
    public void renderAllExcept(final MatrixStack mat, final IVertexBuilder buffer, final String... excludedGroupNames)
    {
        boolean skip = false;
        for (final String s1 : excludedGroupNames)
            if (skip = s1.equalsIgnoreCase(this.name)) break;
        if (!skip) this.render(mat, buffer);
        for (final IExtendedModelPart o : this.childParts.values())
        {
            mat.push();
            mat.translate(this.offset.x, this.offset.y, this.offset.z);
            mat.scale(this.scale.x, this.scale.y, this.scale.z);
            o.renderAllExcept(mat, buffer, excludedGroupNames);
            mat.pop();
        }
    }

    @Override
    public void renderOnly(final MatrixStack mat, final IVertexBuilder buffer, final String... groupNames)
    {
        boolean rendered = false;
        for (final String s1 : groupNames)
            if (rendered = s1.equalsIgnoreCase(this.name))
            {
                this.render(mat, buffer);
                break;
            }
        if (!rendered)
        {
            this.preRender(mat, buffer);
            this.postRender(mat, buffer);
        }
        for (final IExtendedModelPart o : this.childParts.values())
        {
            mat.push();
            mat.translate(this.offset.x, this.offset.y, this.offset.z);
            mat.scale(this.scale.x, this.scale.y, this.scale.z);
            o.renderOnly(mat, buffer, groupNames);
            mat.pop();
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
        this.preRot.set(0, 1, 0, 0);
        this.postRot.set(0, 1, 0, 0);
        this.postRot1.set(0, 1, 0, 0);
        this.preTrans.clear();
        this.postTrans.clear();
    }

    private void rotateToParent(final MatrixStack mat, final IVertexBuilder buffer)
    {
        if (this.parent != null) if (this.parent instanceof Part)
        {
            final Part parent = (Part) this.parent;
            parent.postRot.glRotate(mat);
            parent.postRot1.glRotate(mat);
        }
    }

    @Override
    public void setAnimationChanger(final IAnimationChanger changer)
    {
        this.changer = changer;
        for (final IExtendedModelPart part : this.childParts.values())
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
    public void setPostRotations2(final Vector4 rotations)
    {
        this.postRot1 = rotations;
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
        this.alpha = br;
        this.brightness = o;
    }

    @Override
    public void setTexturer(final IPartTexturer texturer)
    {
        this.texturer = texturer;
        for (final IExtendedModelPart part : this.childParts.values())
            if (part instanceof IRetexturableModel) ((IRetexturableModel) part).setTexturer(texturer);
    }
}
