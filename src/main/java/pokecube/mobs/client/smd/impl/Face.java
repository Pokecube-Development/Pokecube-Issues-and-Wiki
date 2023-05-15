package pokecube.mobs.client.smd.impl;

import java.util.ArrayList;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;

import thut.api.maths.vecmath.Vec3f;
import thut.core.client.render.model.Vertex;
import thut.core.client.render.model.parts.Mesh;
import thut.core.client.render.texturing.TextureCoordinate;
import thut.core.common.ThutCore;
import thut.lib.AxisAngles;

/**
 * A group of vertices, these get moved around by animations on bones, this just
 * holds them
 */
public class Face
{
    public MutableVertex[] verts;
    public TextureCoordinate[] uvs;
    public Vertex normal;

    Vec3f a = new Vec3f();
    Vec3f b = new Vec3f();
    Vec3f c = new Vec3f();

    public Face(final Face face, final ArrayList<MutableVertex> verts)
    {
        this.verts = new MutableVertex[face.verts.length];
        for (int i = 0; i < this.verts.length; i++) this.verts[i] = verts.get(face.verts[i].ID);
        this.uvs = new TextureCoordinate[face.uvs.length];
        System.arraycopy(face.uvs, 0, this.uvs, 0, this.uvs.length);
        if (face.normal != null) this.normal = face.normal;
    }

    public Face(final MutableVertex[] xyz, final TextureCoordinate[] uvs)
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

        final float red = rgbabro[0] / 255f;
        final float green = rgbabro[1] / 255f;
        final float blue = rgbabro[2] / 255f;
        final float alpha = rgbabro[3] / 255f;
        final int lightmapUV = rgbabro[4];
        final int overlayUV = rgbabro[5];
        final PoseStack.Pose matrixstack$entry = mat.last();
        final Matrix4f pos = matrixstack$entry.pose();
        final Matrix3f norms = matrixstack$entry.normal();
        final Vector4f dp = this.dummy4;
        final Vector3f dn = this.dummy3;

        Vector3f camera_view = AxisAngles.ZP;

        boolean cull = ThutCore.getConfig().modelCullThreshold > 0 && alpha >= 1;
        // TODO ghive this a material to check for culling!
        cull = false;
        if (cull)
        {
            // TODO use face centre instead here!
            dp.set(verts[0].x, verts[0].y, verts[0].z, 1);
            dp.transform(pos);
            double dr2 = Math.abs(dp.dot(Mesh.METRIC));
            if (dr2 < ThutCore.getConfig().modelCullThreshold)
            {
                cull = false;
            }
        }

        for (int i = 0; i < 3; i++)
        {
            final MutableVertex vert = this.verts[i];

            final float nx = smoothShading ? vert.xn : this.normal.x;
            final float ny = smoothShading ? vert.yn : this.normal.y;
            final float nz = smoothShading ? vert.zn : this.normal.z;

            dn.set(nx, ny, nz);
            dn.transform(norms);

            // Similar to Mesh, except we only have to check the 1 face, as we
            // only have 1 face! so only apply on i==0. Then, apply a similar
            // threshold to what is used in Mesh
            final boolean tryCull = cull && i == 0 && dn.dot(camera_view) < (smoothShading ? -0.2 : 0.0);
            if (tryCull) break;

            final float x = vert.x;
            final float y = vert.y;
            final float z = vert.z;

            final float u = this.uvs[i].u + (float) uvShift[0];
            final float v = this.uvs[i].v + (float) uvShift[1];

            dp.set(x, y, z, 1);
            dp.transform(pos);

            buffer.vertex(
            //@formatter:off
                dp.x(), dp.y(), dp.z(),
                red, green, blue, alpha,
                u, v,
                overlayUV, lightmapUV,
                dn.x(), dn.y(), dn.z());
            //@formatter:on
        }
    }

    public Vertex calculateNormal()
    {
        this.a.set(this.verts[1].x - this.verts[0].x, this.verts[1].y - this.verts[0].y,
                this.verts[1].z - this.verts[0].z);
        this.b.set(this.verts[2].x - this.verts[0].x, this.verts[2].y - this.verts[0].y,
                this.verts[2].z - this.verts[0].z);
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