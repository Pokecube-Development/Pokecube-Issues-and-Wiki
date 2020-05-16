package pokecube.mobs.client.smd.impl;

import java.util.ArrayList;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.Matrix3f;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Vector4f;
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

    private final net.minecraft.client.renderer.Vector3f dummy3 = new net.minecraft.client.renderer.Vector3f();
    private final Vector4f                               dummy4 = new Vector4f();

    /**
     * Add the face for GL rendering
     *
     * @param buffer
     * @param mat
     * @param smoothShading
     *            - if false, this will render entire face with constant
     *            normal.
     */
    public void addForRender(final MatrixStack mat, final IVertexBuilder buffer, final int[] rgbabro,
            final double[] uvShift, final boolean smoothShading)
    {
        if (!smoothShading) this.normal = this.calculateNormal();

        final int red = rgbabro[0];
        final int green = rgbabro[1];
        final int blue = rgbabro[2];
        final int alpha = rgbabro[3];
        final int lightmapUV = rgbabro[4];
        final int overlayUV = rgbabro[5];
        final MatrixStack.Entry matrixstack$entry = mat.getLast();
        final Matrix4f pos = matrixstack$entry.getMatrix();
        final Matrix3f norms = matrixstack$entry.getNormal();
        final Vector4f dp = this.dummy4;
        final net.minecraft.client.renderer.Vector3f dn = this.dummy3;

        for (int i = 0; i < 3; i++)
        {
            final MutableVertex vert = this.verts[i];
            final float x = vert.x;
            final float y = vert.y;
            final float z = vert.z;

            final float u = this.uvs[i].u + (float) uvShift[0];
            final float v = this.uvs[i].v + (float) uvShift[1];

            final float nx = smoothShading ? vert.xn : this.normal.x;
            final float ny = smoothShading ? vert.yn : this.normal.y;
            final float nz = smoothShading ? vert.zn : this.normal.z;

            dp.set(x, y, z, 1);
            dp.transform(pos);
            dn.set(nx, ny, nz);
            dn.transform(norms);

            buffer//@formatter:off
            .pos(dp.getX(), dp.getY(), dp.getZ())
            .color(red, green, blue, alpha)
            .tex(u, v)
            .overlay(overlayUV)
            .lightmap(lightmapUV)
            .normal(dn.getX(), dn.getY(), dn.getZ())
            .endVertex();
            //@formatter:on
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