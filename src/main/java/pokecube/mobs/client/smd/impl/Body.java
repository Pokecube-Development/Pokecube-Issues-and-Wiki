package pokecube.mobs.client.smd.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.util.ResourceLocation;
import thut.api.maths.vecmath.Vector3f;
import thut.core.client.render.animation.IAnimationChanger;
import thut.core.client.render.model.parts.Material;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.texturing.IRetexturableModel;
import thut.core.client.render.texturing.TextureCoordinate;
import thut.core.common.ThutCore;

/** Body, Made of Bones, Faces, and Materials. */
public class Body implements IRetexturableModel
{
    public final Model              parent;
    public ArrayList<Face>          faces = Lists.newArrayList();
    public ArrayList<MutableVertex> verts = Lists.newArrayList();
    public ArrayList<Bone>          bones = Lists.newArrayList();
    // Used to idenfify which bones are the neck.
    public HashMap<String, Bone>              namesToBones = Maps.newHashMap();
    public HashMap<String, Material>          namesToMats;
    public HashMap<Material, ArrayList<Face>> matsToFaces;
    public Animation                          currentAnim;
    private int                               nextVertexID = 0;
    protected boolean                         partOfGroup;
    public Bone                               root;
    IPartTexturer                             texturer;
    IAnimationChanger                         changer;
    private final double[]                    uvShift      = { 0, 0 };

    public Body(final Body body, final Model parent)
    {
        this.parent = parent;
        this.partOfGroup = body.partOfGroup;
        for (final Face face : body.faces)
        {
            final MutableVertex[] vertices = new MutableVertex[face.verts.length];
            for (int i = 0; i < vertices.length; i++)
            {
                final MutableVertex d = new MutableVertex(face.verts[i]);
                Helpers.ensureFits(this.verts, d.ID);
                this.verts.set(d.ID, d);
            }
        }
        for (final Face face : body.faces)
            this.faces.add(new Face(face, this.verts));
        for (int i = 0; i < body.bones.size(); i++)
        {
            final Bone b = body.bones.get(i);
            this.bones.add(new Bone(b, null, this));
        }
        for (int i = 0; i < body.bones.size(); i++)
        {
            final Bone b = body.bones.get(i);
            b.copy.setChildren(b, this.bones);
        }
        this.root = body.root.copy;
        parent.syncBones(this);
    }

    public Body(final Model parent, final ResourceLocation resloc) throws Exception
    {
        this.parent = parent;
        this.partOfGroup = false;
        this.loadModel(resloc, null);
        this.initBoneChildren();
        this.determineRoot();
        parent.syncBones(this);
    }

    private void determineRoot()
    {
        for (final Bone b : this.bones)
            if (b.parent == null && !b.children.isEmpty())
            {
                this.root = b;
                break;
            }
        if (this.root == null) for (final Bone b : this.bones)
            if (!b.name.equals("blender_implicit"))
            {
                this.root = b;
                break;
            }
    }

    public Bone getBone(final int id)
    {
        return id < this.bones.size() && id >= 0 ? this.bones.get(id) : null;
    }

    public Bone getBone(final String name)
    {
        for (final Bone b : this.bones)
            if (b.name.equals(name)) return b;
        return null;
    }

    public Frame getCurrentFrame()
    {
        return this.currentAnim == null ? null : this.currentAnim.getCurrentFrame();
    }

    private MutableVertex getExisting(final float x, final float y, final float z)
    {
        for (final MutableVertex v : this.verts)
            if (v.equals(x, y, z)) return v;
        return null;
    }

    private void initBoneChildren()
    {
        Bone theBone;
        for (int i = 0; i < this.bones.size(); i++)
        {
            theBone = this.bones.get(i);
            for (final Bone child : this.bones)
                if (child.parent == theBone) theBone.addChild(child);
        }
    }

    private void loadModel(final ResourceLocation resloc, final Body body) throws Exception
    {
        final InputStream inputStream = Helpers.getStream(resloc);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String currentLine = null;
        int lineCount = -1;
        try
        {
            lineCount = 0;
            while ((currentLine = reader.readLine()) != null)
            {
                lineCount += 1;
                if (!currentLine.startsWith("version")) if (currentLine.startsWith("nodes"))
                {
                    lineCount += 1;
                    while (!(currentLine = reader.readLine()).startsWith("end"))
                    {
                        lineCount += 1;
                        this.parseBone(currentLine.trim(), lineCount, body);
                    }
                }
                else if (currentLine.startsWith("skeleton"))
                {
                    lineCount += 1;
                    reader.readLine();
                    lineCount += 1;
                    while (!(currentLine = reader.readLine()).startsWith("end"))
                    {
                        lineCount += 1;
                        if (!this.partOfGroup) this.updateBone(currentLine.trim(), lineCount);
                    }
                }
                else if (currentLine.startsWith("triangles"))
                {
                    lineCount += 1;
                    while (!(currentLine = reader.readLine()).startsWith("end"))
                    {
                        final Material mat = this.parent.usesMaterials ? this.parseMaterial(ThutCore.trim(currentLine))
                                : null;
                        final String[] params = new String[3];
                        for (int i = 0; i < 3; i++)
                        {
                            lineCount += 1;
                            params[i] = reader.readLine().trim();
                        }
                        this.parseFace(params, lineCount, mat);
                    }
                }
            }
        }
        catch (final Exception e)
        {
            if (lineCount == -1) throw new Exception("there was a problem opening the model file : " + resloc, e);
            throw new Exception("an error occurred reading the SMD file \"" + resloc + "\" on line #" + lineCount, e);
        }
        finally
        {
            reader.close();
        }
    }

    private void parseBone(final String line, final int lineCount, final Body body)
    {
        final String[] params = line.split("\\s+");
        final int id = Integer.parseInt(params[0]);
        final String boneName = params[1].replaceAll("\"", "");
        Bone theBone = body != null ? body.getBone(boneName) : null;
        if (theBone == null)
        {
            final int parentID = Integer.parseInt(params[2]);
            final Bone parent = parentID >= 0 ? this.bones.get(parentID) : null;
            theBone = new Bone(boneName, id, parent, this);
        }
        Helpers.ensureFits(this.bones, id);
        this.bones.set(id, theBone);
    }

    private void parseFace(final String[] params, final int lineCount, final Material mat)
    {
        final MutableVertex[] faceVerts = new MutableVertex[3];
        final TextureCoordinate[] uvs = new TextureCoordinate[3];
        for (int i = 0; i < 3; i++)
        {
            final String[] values = params[i].split("\\s+");
            /**
             * The negative signs are for differences in default coordinate
             * systems between minecraft and blender.
             */
            final float x = Float.parseFloat(values[1]);
            final float y = -Float.parseFloat(values[2]);
            final float z = -Float.parseFloat(values[3]);
            final float xn = Float.parseFloat(values[4]);
            final float yn = -Float.parseFloat(values[5]);
            final float zn = -Float.parseFloat(values[6]);

            final MutableVertex v = this.getExisting(x, y, z);
            if (v == null)
            {
                faceVerts[i] = new MutableVertex(x, y, z, xn, yn, zn, this.nextVertexID);
                Helpers.ensureFits(this.verts, this.nextVertexID);
                this.verts.set(this.nextVertexID, faceVerts[i]);
                this.nextVertexID += 1;
            }
            else faceVerts[i] = v;
            uvs[i] = new TextureCoordinate(Float.parseFloat(values[7]), 1.0F - Float.parseFloat(values[8]));
            if (values.length > 10) this.weighBones(values, faceVerts[i]);
        }
        final Face face = new Face(faceVerts, uvs);
        face.verts = faceVerts;

        face.uvs = uvs;
        this.faces.add(face);
        if (mat != null)
        {
            if (this.matsToFaces == null) this.matsToFaces = Maps.newHashMap();
            ArrayList<Face> list = this.matsToFaces.get(mat);
            if (list == null) this.matsToFaces.put(mat, list = Lists.newArrayList());
            list.add(face);
        }
    }

    public Material parseMaterial(final String materialName) throws Exception
    {
        if (!this.parent.usesMaterials) return null;
        if (this.namesToMats == null) this.namesToMats = Maps.newHashMap();
        Material result = this.namesToMats.get(materialName);
        if (result != null) return result;
        try
        {
            result = new Material(materialName, materialName, new Vector3f(), new Vector3f(), new Vector3f(), 1, 1, 0);
            this.namesToMats.put(materialName, result);
            return result;
        }
        catch (final Exception e)
        {
            throw new Exception(e);
        }
    }

    public void render()
    {
        GL11.glPushMatrix();
        final boolean smooth = this.texturer == null ? false : !this.texturer.isFlat(null);
        if (!this.parent.usesMaterials)
        {
            GL11.glBegin(GL11.GL_TRIANGLES);
            for (final Face f : this.faces)
                f.addForRender(smooth);
            GL11.glEnd();
        }
        else for (final Map.Entry<Material, ArrayList<Face>> entry : this.matsToFaces.entrySet())
        {
            Material mat;
            if ((mat = entry.getKey()) != null)
            {
                final String tex = mat.name;
                boolean textureShift = false;
                if (this.texturer != null)
                {
                    this.texturer.applyTexture(tex);
                    if (textureShift = this.texturer.shiftUVs(tex, this.uvShift))
                    {
                        GL11.glMatrixMode(GL11.GL_TEXTURE);
                        GL11.glTranslated(this.uvShift[0], this.uvShift[1], 0.0F);
                        GL11.glMatrixMode(GL11.GL_MODELVIEW);
                    }
                }
                mat.preRender();
                this.render(entry, smooth);
                mat.postRender();
                if (textureShift)
                {
                    GL11.glMatrixMode(GL11.GL_TEXTURE);
                    GL11.glLoadIdentity();
                    GL11.glMatrixMode(GL11.GL_MODELVIEW);
                }
            }
        }
        GL11.glPopMatrix();
    }

    private void render(final Map.Entry<Material, ArrayList<Face>> entry, final boolean smooth)
    {
        GL11.glBegin(GL11.GL_TRIANGLES);
        for (final Face face : entry.getValue())
            face.addForRender(smooth);
        GL11.glEnd();
    }

    public void resetVerts()
    {
        for (final MutableVertex v : this.verts)
            v.reset();
    }

    public void setAnimation(final Animation anim)
    {
        this.currentAnim = anim;
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

    private void updateBone(final String line, final int lineCount)
    {
        final String[] params = line.split("\\s+");
        final int id = Integer.parseInt(params[0]);

        final float[] locRots = new float[6];
        for (int i = 1; i < 7; i++)
            locRots[i - 1] = Float.parseFloat(params[i]);
        final Bone theBone = this.bones.get(id);
        /**
         * The negative signs are for differences in default coordinate systems
         * between minecraft and blender.
         */
        theBone.setRest(Helpers.makeMatrix(locRots[0], -locRots[1], -locRots[2], locRots[3], -locRots[4], -locRots[5]));
    }

    private void weighBones(final String[] values, final MutableVertex vert)
    {
        final int links = Integer.parseInt(values[9]);
        final float[] weights = new float[links];
        float sum = 0.0F;
        for (int i = 0; i < links; i++)
        {
            weights[i] = Float.parseFloat(values[i * 2 + 11]);
            sum += weights[i];
        }
        for (int i = 0; i < links; i++)
        {
            final int boneID = Integer.parseInt(values[i * 2 + 10]);
            final float weight = weights[i] / sum;
            this.bones.get(boneID).addVertex(vert, weight);
        }
    }
}