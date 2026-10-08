package pokecube.mobs.client.smd.impl;

import java.util.ArrayList;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.util.FastColor;

/**
 * A group of vertices, these get moved around by animations on bones, this just
 * holds them
 */
public class Face
{
    private static final Vector3f ZP = (new Vector3f(0.0F, 0.0F, 1.0F)).normalize();

    public MutableVertex[] verts;
    public Vector2f[] uvs;
    public Vector3f normal;

    Vector3f a = new Vector3f();
    Vector3f b = new Vector3f();
    Vector3f c = new Vector3f();

    public Face(final Face face, final ArrayList<MutableVertex> verts)
    {
        this.verts = new MutableVertex[face.verts.length];
        for (int i = 0; i < this.verts.length; i++) this.verts[i] = verts.get(face.verts[i].ID);
        this.uvs = new Vector2f[face.uvs.length];
        System.arraycopy(face.uvs, 0, this.uvs, 0, this.uvs.length);
        if (face.normal != null) this.normal = face.normal;
    }

    public Face(final MutableVertex[] xyz, final Vector2f[] uvs)
    {
        this.verts = xyz;
        this.uvs = uvs;
    }

    private final Vector3f dummy3 = new Vector3f();
    private final Vector4f dummy4 = new Vector4f();

    /**
     * Add the face for GL rendering
     *
     * @param buffer
     * @param mat
     * @param smoothShading - if false, this will render entire face with
     *                      constant normal.
     */
    public void addForRender(final PoseStack mat, final VertexConsumer buffer, final int[] rgbabro,
            final double[] uvShift, final boolean smoothShading)
    {
        if (!smoothShading) this.normal = this.calculateNormal();

        final int red = rgbabro[0];
        final int green = rgbabro[1];
        final int blue = rgbabro[2];
        final int alpha = rgbabro[3];
        final int lightmapUV = rgbabro[4];
        final int overlayUV = rgbabro[5];
        final PoseStack.Pose matrixstack$entry = mat.last();
        final Matrix4f pos = matrixstack$entry.pose();
        final Matrix3f norms = matrixstack$entry.normal();
        final Vector4f dp = this.dummy4;
        final Vector3f dn = this.dummy3;
        int argb = FastColor.ARGB32.color(alpha, red, green, blue);

        for (int i = 0; i < 3; i++)
        {
            final MutableVertex vert = this.verts[i];

            final float nx = smoothShading ? vert.xn : this.normal.x;
            final float ny = smoothShading ? vert.yn : this.normal.y;
            final float nz = smoothShading ? vert.zn : this.normal.z;

            dn.set(nx, ny, nz);
            dn.mul(norms);

            dp.set(vert, 1);
            dp.mul(pos);

            buffer.addVertex(
            //@formatter:off
                dp.x(), dp.y(), dp.z(),
                argb,
                this.uvs[i].x + (float) uvShift[0], this.uvs[i].y + (float) uvShift[1],
                overlayUV, lightmapUV,
                dn.x(), dn.y(), dn.z());
            //@formatter:on
        }
    }

    public Vector3f calculateNormal()
    {
        this.a.set(this.verts[1].x - this.verts[0].x, this.verts[1].y - this.verts[0].y,
                this.verts[1].z - this.verts[0].z);
        this.b.set(this.verts[2].x - this.verts[0].x, this.verts[2].y - this.verts[0].y,
                this.verts[2].z - this.verts[0].z);
        this.a.cross(this.b, this.c);
        this.c.normalize();
        if (this.normal == null) this.normal = new Vector3f(this.c.x, this.c.y, this.c.z);
        else
        {
            this.normal.x = this.c.x;
            this.normal.y = this.c.y;
            this.normal.z = this.c.z;
        }
        return this.normal;
    }
}