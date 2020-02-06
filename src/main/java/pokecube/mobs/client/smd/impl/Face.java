package pokecube.mobs.client.smd.impl;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import thut.api.maths.vecmath.Vector3f;
import thut.core.client.render.model.Vertex;
import thut.core.client.render.texturing.TextureCoordinate;

/**
 * A group of vertices, these get moved around by animations on bones, this
 * just holds them
 */
public class Face
{
    public MutableVertex[]     verts;
    public TextureCoordinate[] uvs;
    public Vertex              normal;

    Vector3f a = new Vector3f();
    Vector3f b = new Vector3f();
    Vector3f c = new Vector3f();

    public Face(final Face face, final ArrayList<MutableVertex> verts)
    {
        this.verts = new MutableVertex[face.verts.length];
        for (int i = 0; i < this.verts.length; i++)
            this.verts[i] = verts.get(face.verts[i].ID);
        this.uvs = new TextureCoordinate[face.uvs.length];
        System.arraycopy(face.uvs, 0, this.uvs, 0, this.uvs.length);
        if (face.normal != null) this.normal = face.normal;
    }

    public Face(final MutableVertex[] xyz, final TextureCoordinate[] uvs)
    {
        this.verts = xyz;
        this.uvs = uvs;
    }

    /** Add the face for GL rendering
     *
     * @param buffer
     * @param mat
     * @param smoothShading
     *            - if false, this will render entire face with constant
     *            normal. */
    public void addForRender(final MatrixStack mat, final IVertexBuilder buffer, final int[] rgbabro,
            final boolean smoothShading)
    {
        if (!smoothShading) this.normal = this.calculateNormal();
        for (int i = 0; i < 3; i++)
        {
            GL11.glTexCoord2f(this.uvs[i].u, this.uvs[i].v);
            if (!smoothShading) GL11.glNormal3f(this.normal.x, this.normal.y, this.normal.z);
            else GL11.glNormal3f(this.verts[i].xn, this.verts[i].yn, this.verts[i].zn);
            GL11.glVertex3d(this.verts[i].x, this.verts[i].y, this.verts[i].z);
        }
    }

    public Vertex calculateNormal()
    {
        this.a.set(this.verts[1].x - this.verts[0].x, this.verts[1].y - this.verts[0].y, this.verts[1].z
                - this.verts[0].z);
        this.b.set(this.verts[2].x - this.verts[0].x, this.verts[2].y - this.verts[0].y, this.verts[2].z
                - this.verts[0].z);
        this.c.cross(this.a, this.b);
        this.c.normalize();
        if (this.normal == null) this.normal = new Vertex(this.c.x, this.c.y, this.c.z);
        else
        {
            this.normal.x = this.c.x;
            this.normal.y = this.c.y;
            this.normal.z = this.c.z;
        }
        return this.normal;
    }
}