package thut.core.client.render.model.parts;

import java.util.HashMap;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.api.maths.vecmath.Vector3f;
import thut.core.client.render.animation.AnimationXML.Mat;
import thut.core.client.render.animation.IAnimationChanger;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.Vertex;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.texturing.IRetexturableModel;
import thut.core.common.ThutCore;

public abstract class Part implements IExtendedModelPart, IRetexturableModel
{
    public List<Mesh> shapes = Lists.newArrayList();

    private final HashMap<String, IExtendedModelPart> childParts = new HashMap<>();
    private final String                              name;
    private IExtendedModelPart                        parent     = null;
    IPartTexturer                                     texturer;
    IAnimationChanger                                 changer;

    public Vector4 preRot    = new Vector4();
    public Vector4 postRot   = new Vector4();
    public Vector4 postRot1  = new Vector4();
    public Vector3 preTrans  = Vector3.getNewVector();
    public Vector3 postTrans = Vector3.getNewVector();
    public Vertex  preScale  = new Vertex(1, 1, 1);

    public Vector3 offset    = Vector3.getNewVector();
    public Vector4 rotations = new Vector4();
    public Vertex  scale     = new Vertex(1, 1, 1);

    public int red = 255, green = 255, blue = 255, alpha = 255;

    public int brightness = 15728640;

    private final int[] rgbab = new int[5];

    private boolean hidden = false;

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

    public void addForRender()
    {
        // Set colours.
        GL11.glColor4f(this.red / 255f, this.green / 255f, this.blue / 255f, this.alpha / 255f);
        // Render each Shape
        for (final Mesh s : this.shapes)
            s.renderShape(this.texturer);
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
    public int[] getRGBAB()
    {
        this.rgbab[0] = this.red;
        this.rgbab[1] = this.green;
        this.rgbab[2] = this.blue;
        this.rgbab[3] = this.alpha;
        this.rgbab[4] = this.brightness;
        return this.rgbab;
    }

    @SuppressWarnings("unchecked")
    @Override
    public HashMap<String, IExtendedModelPart> getSubParts()
    {
        return this.childParts;
    }

    private void postRender()
    {
        GL11.glTranslated(-this.offset.x, -this.offset.y, -this.offset.z);
    }

    private void preRender()
    {
        // Rotate to the offset of the parent.
        this.rotateToParent();
        // Translate of offset for rotation.
        GL11.glTranslated(this.offset.x, this.offset.y, this.offset.z);
        // Rotate by this to account for a coordinate difference.
        GL11.glRotatef(90, 1, 0, 0);
        GL11.glTranslated(this.preTrans.x, this.preTrans.y, this.preTrans.z);
        // UnRotate coordinate difference.
        GL11.glRotatef(-90, 1, 0, 0);
        // Apply initial part rotation
        this.rotations.glRotate();
        // Rotate by this to account for a coordinate difference.
        GL11.glRotatef(90, 1, 0, 0);
        // Apply PreOffset-Rotations.
        this.preRot.glRotate();
        // Translate by post-PreOffset amount.
        GL11.glTranslated(this.postTrans.x, this.postTrans.y, this.postTrans.z);
        // UnRotate coordinate difference.
        GL11.glRotatef(-90, 1, 0, 0);

        // Apply first postRotation
        this.postRot.glRotate();
        // Apply second post rotation.
        this.postRot1.glRotate();
        // Scale
        GL11.glScalef(this.scale.x, this.scale.y, this.scale.z);
    }

    public void render()
    {
        if (this.hidden) return;
        this.preRender();
        // Renders the model.
        this.addForRender();
        this.postRender();
    }

    @Override
    public void renderAll()
    {
        GL11.glScalef(this.preScale.x, this.preScale.y, this.preScale.z);
        this.render();
        for (final IExtendedModelPart o : this.childParts.values())
        {
            GL11.glPushMatrix();
            GL11.glTranslated(this.offset.x, this.offset.y, this.offset.z);
            GL11.glScalef(this.scale.x, this.scale.y, this.scale.z);
            o.renderAll();
            GL11.glPopMatrix();
        }
    }

    @Override
    public void renderAllExcept(final String... excludedGroupNames)
    {
        boolean skip = this.hidden;
        for (final String s1 : excludedGroupNames)
            if (skip = s1.equalsIgnoreCase(this.name)) break;
        if (!skip)
        {
            this.render();
            for (final IExtendedModelPart o : this.childParts.values())
            {
                GL11.glPushMatrix();
                GL11.glTranslated(this.offset.x, this.offset.y, this.offset.z);
                GL11.glScalef(this.scale.x, this.scale.y, this.scale.z);
                o.renderAllExcept(excludedGroupNames);
                GL11.glPopMatrix();
            }
        }
    }

    @Override
    public void renderOnly(final String... groupNames)
    {
        boolean rendered = false;
        for (final String s1 : groupNames)
            if (rendered = s1.equalsIgnoreCase(this.name))
            {
                this.preRender();
                this.render();
                this.postRender();
                break;
            }
        if (rendered) for (final IExtendedModelPart o : this.childParts.values())
        {
            GL11.glPushMatrix();
            GL11.glTranslated(this.offset.x, this.offset.y, this.offset.z);
            GL11.glScalef(this.scale.x, this.scale.y, this.scale.z);
            o.renderOnly(groupNames);
            GL11.glPopMatrix();
        }
    }

    @Override
    public void renderPart(final String partName)
    {
        this.renderOnly(partName);
    }

    @Override
    public void resetToInit()
    {
        this.preRot.set(0, 1, 0, 0);
        this.postRot.set(0, 1, 0, 0);
        this.postRot1.set(0, 1, 0, 0);
        this.preTrans.clear();
        this.postTrans.clear();
        this.hidden = false;
    }

    private void rotateToParent()
    {
        if (this.parent != null) if (this.parent instanceof Part)
        {
            final Part parent = (Part) this.parent;
            parent.postRot.glRotate();
            parent.postRot1.glRotate();
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
    public void setRGBAB(final int[] array)
    {
        this.red = array[0];
        this.green = array[1];
        this.blue = array[2];
        this.alpha = array[3];
        this.brightness = array[4];
    }

    @Override
    public void setTexturer(final IPartTexturer texturer)
    {
        this.texturer = texturer;
        for (final IExtendedModelPart part : this.childParts.values())
            if (part instanceof IRetexturableModel) ((IRetexturableModel) part).setTexturer(texturer);
    }

    @Override
    public void updateMaterial(final Mat mat)
    {
        final String mat_name = ThutCore.trim(mat.name);
        final String[] parts = mat.name.split(":");
        final Material material = new Material(mat_name);
        material.diffuseColor = new Vector3f(1, 1, 1);
        material.emissiveColor = new Vector3f(1, 1, 1);
        material.specularColor = new Vector3f(1, 1, 1);
        material.transparency = mat.transluscent ? 1 : 0;
        for (final String s : parts)
            for (final Mesh mesh : this.shapes)
            {
                if (mesh.name == null) mesh.name = this.getName();
                if (mesh.name.equals(ThutCore.trim(s)) || mesh.name.equals(mat_name)) mesh.setMaterial(material);
            }
    }
}
