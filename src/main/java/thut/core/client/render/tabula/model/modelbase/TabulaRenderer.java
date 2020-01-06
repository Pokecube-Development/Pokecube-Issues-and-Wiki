package thut.core.client.render.tabula.model.modelbase;

import java.util.HashMap;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.ModelBox;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.core.client.render.animation.IAnimationChanger;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.Vertex;
import thut.core.client.render.tabula.components.CubeInfo;
import thut.core.client.render.tabula.components.ModelJson;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.texturing.IRetexturableModel;

@OnlyIn(Dist.CLIENT)
public class TabulaRenderer implements IExtendedModelPart, IRetexturableModel
{
    static final float ratio = 180f / (float) Math.PI;

    public final List<ModelBox>                   cubeList   = Lists.newArrayList();
    private final HashMap<String, TabulaRenderer> childParts = new HashMap<>();
    public String                                 name;
    private IExtendedModelPart                    parent     = null;

    IPartTexturer     texturer;
    IAnimationChanger changer;

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

    private boolean compiled;
    private int     displayList;
    public String   identifier;

    final ModelJson<?>    model;
    public final CubeInfo info;

    double[] texOffsets   = { 0, 0 };
    boolean  shouldoffset = true;

    boolean rotate = true;

    boolean translate = true;

    boolean shouldScale = true;

    public boolean transluscent = false;

    public TabulaRenderer(final ModelJson<?> modelBase, final TabulaRenderer parent, final CubeInfo cubeInfo)
    {
        // super(modelBase, x, y);
        this.model = modelBase;

        this.info = cubeInfo;
        this.name = cubeInfo.name;
        this.identifier = cubeInfo.identifier;
        this.offset.set(cubeInfo.position).scalarMultBy(0.0625);

        this.rotations = Vector4.fromAngles((float) cubeInfo.rotation[0], (float) cubeInfo.rotation[1],
                (float) cubeInfo.rotation[2]);

        // Allows scaling the cube with the cubeinfo scale.
        this.scale.x = (float) cubeInfo.scale[0];
        this.scale.y = (float) cubeInfo.scale[1];
        this.scale.z = (float) cubeInfo.scale[2];

        if (cubeInfo.metadata != null) for (final String s : cubeInfo.metadata)
            if (s.equalsIgnoreCase("trans")) this.transluscent = true;
        this.hidden = cubeInfo.hidden;

        final RendererModel dummy = new RendererModel(this.model, cubeInfo.txOffset[0], cubeInfo.txOffset[1]);
        // Use cubeInfo.mcScale as the scale, this lets it work properly.
        this.cubeList.add(new ModelBox(dummy, cubeInfo.txOffset[0], cubeInfo.txOffset[1], (float) cubeInfo.offset[0],
                (float) cubeInfo.offset[1], (float) cubeInfo.offset[2], cubeInfo.dimensions[0], cubeInfo.dimensions[1],
                cubeInfo.dimensions[2], (float) cubeInfo.mcScale));
    }

    @Override
    public void addChild(final IExtendedModelPart renderer)
    {
        // super.addChild((RendererModel) renderer);
        this.childParts.put(renderer.getName(), (TabulaRenderer) renderer);
        if (renderer instanceof TabulaRenderer) ((TabulaRenderer) renderer).setParent(this);
    }

    public void addForRender()
    {
        if (!this.compiled) this.compileDisplayList(0.0625F);
        // Apply Colour
        boolean animateTex = false;
        // Apply Texture
        if (this.texturer != null)
        {
            this.texturer.applyTexture(this.name);
            this.texturer.shiftUVs(this.name, this.texOffsets);
            if (this.texOffsets[0] != 0 || this.texOffsets[1] != 0) animateTex = true;
            if (animateTex)
            {
                GL11.glMatrixMode(GL11.GL_TEXTURE);
                GL11.glLoadIdentity();
                GL11.glTranslated(this.texOffsets[0], this.texOffsets[1], 0.0F);
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
            }
        }
        GL11.glCallList(this.displayList);
        GL11.glFlush();
        if (animateTex)
        {
            GL11.glMatrixMode(GL11.GL_TEXTURE);
            GL11.glLoadIdentity();
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void compileDisplayList(final float scale)
    {
        this.displayList = GLAllocation.generateDisplayLists(1);
        GL11.glNewList(this.displayList, GL11.GL_COMPILE);
        for (final Object object : this.cubeList)
            ((ModelBox) object).render(Tessellator.getInstance().getBuffer(), scale);
        GL11.glEndList();
        this.compiled = true;
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

    /** Returns the parent of this RendererModel */
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
    public HashMap<String, TabulaRenderer> getSubParts()
    {
        return this.childParts;
    }

    @Override
    public String getType()
    {
        return "tbl";
    }

    private void postRender()
    {
        GL11.glPopMatrix();
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
        // Undo pre-translate offset.
        GL11.glTranslated(-this.offset.x, -this.offset.y, -this.offset.z);
        GL11.glPushMatrix();
        // Translate to Offset.
        GL11.glTranslated(this.offset.x, this.offset.y, this.offset.z);

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
        boolean skip = false;
        for (final String s1 : excludedGroupNames)
            if (skip = s1.equalsIgnoreCase(this.name)) break;
        if (!skip) this.render();
        for (final IExtendedModelPart o : this.childParts.values())
        {
            GL11.glPushMatrix();
            GL11.glTranslated(this.offset.x, this.offset.y, this.offset.z);
            GL11.glScalef(this.scale.x, this.scale.y, this.scale.z);
            o.renderAllExcept(excludedGroupNames);
            GL11.glPopMatrix();
        }
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

    private void rotateToParent()
    {
        if (this.parent != null) if (this.parent instanceof TabulaRenderer)
        {
            final TabulaRenderer parent = (TabulaRenderer) this.parent;
            parent.postRot.glRotate();
            parent.postRot1.glRotate();
        }
    }

    @Override
    public void setAnimationChanger(final IAnimationChanger changer)
    {
        this.changer = changer;
        for (final TabulaRenderer part : this.childParts.values())
            part.setAnimationChanger(changer);
    }

    /** Set the initialization pose to the current pose */
    public void setInitValuesToCurrentPose()
    {
    }

    /** Sets the parent of this RendererModel */
    @Override
    public void setParent(final IExtendedModelPart modelRenderer)
    {
        this.parent = modelRenderer;
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
        for (final TabulaRenderer part : this.childParts.values())
            part.setTexturer(texturer);
    }
}
