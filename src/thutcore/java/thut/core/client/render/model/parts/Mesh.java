package thut.core.client.render.model.parts;

import java.util.Arrays;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;

import thut.api.maths.vecmath.Vec3f;
import thut.core.client.render.model.Vertex;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.texturing.TextureCoordinate;

public abstract class Mesh
{
    public static boolean debug = false;

    public static float windowScale = 1;
    public static int verts = 0;
    public static double modelCullThreshold = 0;

    private static final float inv_255 = 1 / 255f;

    public final Vertex[] vertices;
    public final Vertex[] normals;
    public final TextureCoordinate[] textureCoordinates;
    public final int[] order;
    Material material;
    public String name;
    public boolean overrideColour = false;
    private final double[] uvShift =
    { 0, 0 };
    final int GL_FORMAT;
    final Vertex[] normalList;

    public int[] rgbabro = new int[6];

    private boolean same_mat = false;

    public final Mode vertexMode;

    Vertex min = new Vertex(0, 0);
    Vertex max = new Vertex(0, 0);

    final int iter;

    private final float len;
    public float cullScale = 1;
    public float renderScale = 1;

    private final TextureCoordinate dummyTex = new TextureCoordinate(0, 0);
    public static Vector4f METRIC = new Vector4f(1, 1, 1, 0);

    private static void clip(Vec3f bound, Vec3f point, boolean up)
    {
        if (up)
        {
            if (point.x > bound.x) bound.x = point.x;
            if (point.y > bound.y) bound.y = point.y;
            if (point.z > bound.z) bound.z = point.z;
        }
        else
        {
            if (point.x < bound.x) bound.x = point.x;
            if (point.y < bound.y) bound.y = point.y;
            if (point.z < bound.z) bound.z = point.z;
        }
    }

    public Mesh(final Integer[] order, final Vertex[] vert, final Vertex[] norm, final TextureCoordinate[] tex,
            final int GL_FORMAT)
    {
        this.order = new int[order.length];
        this.vertices = vert;
        this.normalList = new Vertex[this.order.length];
        this.normals = norm != null ? new Vertex[this.order.length] : this.normalList;
        this.textureCoordinates = tex != null ? tex : new TextureCoordinate[order.length];
        this.GL_FORMAT = GL_FORMAT;
        Vertex vertex;
        Vertex normal;
        this.iter = GL_FORMAT == GL11.GL_TRIANGLES ? 3 : 4;

        vertexMode = GL_FORMAT == GL11.GL_TRIANGLES ? Mode.TRIANGLES : Mode.QUADS;

        Vec3f mins = new Vec3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        Vec3f maxs = new Vec3f(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
        final Vec3f c = new Vec3f();

        // In this case, just fill all with dummy tex.
        if (tex == null) Arrays.fill(textureCoordinates, dummyTex);
        // Fill the order array first.
        for (int i = 0; i < order.length; i++) this.order[i] = order[i];

        int i_1, i_2, i_3, i_4 = 0;
        // Calculate the normals for each triangle.
        for (int i = 0; i < this.order.length; i += iter)
        {
            i_1 = this.order[i + 0];
            i_2 = this.order[i + 1];
            i_3 = this.order[i + 2];

            Vec3f v1, v2, v3;
            vertex = this.vertices[i_1];
            v1 = new Vec3f(vertex.x, vertex.y, vertex.z);
            vertex = this.vertices[i_2];
            v2 = new Vec3f(vertex.x, vertex.y, vertex.z);
            vertex = this.vertices[i_3];
            v3 = new Vec3f(vertex.x, vertex.y, vertex.z);

            clip(mins, v1, false);
            clip(mins, v2, false);
            clip(mins, v3, false);

            clip(maxs, v1, true);
            clip(maxs, v2, true);
            clip(maxs, v3, true);

            if (iter == 4)
            {
                i_4 = this.order[i + 3];
                vertex = this.vertices[i_4];
                Vec3f v4 = new Vec3f(vertex.x, vertex.y, vertex.z);

                clip(mins, v4, false);
                clip(maxs, v4, true);
            }

            final Vec3f a = new Vec3f(v2);
            a.sub(v1);
            final Vec3f b = new Vec3f(v3);
            b.sub(v1);
            c.cross(a, b);
            c.normalize();
            if (Double.isNaN(c.x))
            {
                c.x = 0;
                c.y = 0;
                c.z = 1;
            }
            normal = new Vertex(c.x, c.y, c.z);
            for (int j = i; j < i + iter; j++)
            {
                int i_0 = this.order[j];
                this.normalList[j] = normal;
                if (norm != null) this.normals[j] = norm[i_0];
            }
        }

        min.set(mins);
        max.set(maxs);

        dummy_1.set(max.x - min.x, max.y - min.y, max.z - min.z);
        len = (float) Math.sqrt(dummy_1.dot(dummy_1));

        // Initialize a "default" material for us
        this.material = new Material("auto:" + this.name);
        this.material.vertexMode = this.vertexMode;
    }

    private final Vector3f dummy3 = new Vector3f();
    private final Vector3f dummy_1 = new Vector3f();
    private final Vector4f dummy4 = new Vector4f();

    protected void doRender(final PoseStack mat, final VertexConsumer buffer, final IPartTexturer texturer)
    {
        final PoseStack.Pose matrixstack$entry = mat.last();
        final Matrix4f pos = matrixstack$entry.pose();
        final Vector4f dp = this.dummy4;

        float x, y, z, nx, ny, nz, u, v;
        if (modelCullThreshold > 0)
        {
            float a = windowScale;
            float s = len * cullScale;

            dp.set(s, s, s, 0);
            dp.transform(pos);
            dp.mul(a);
            double dr2_us = dp.dot(dp);

            dp.set(0, 0, 0, 1);
            dp.transform(pos);
            double dr2_2 = dp.dot(dp);

            boolean size_cull = modelCullThreshold * dr2_2 >= dr2_us;

            if (size_cull) return;
        }

        float red = material.rgbabro[0] * inv_255;
        float green = material.rgbabro[1] * inv_255;
        float blue = material.rgbabro[2] * inv_255;
        float alpha = this.material.alpha * material.rgbabro[3] * inv_255;
        int lightmapUV = material.rgbabro[4];
        int overlayUV = material.rgbabro[5];

        if (debug || overrideColour)
        {
            red = this.rgbabro[0] * inv_255;
            green = this.rgbabro[1] * inv_255;
            blue = this.rgbabro[2] * inv_255;
            alpha = this.material.alpha * this.rgbabro[3] * inv_255;
            lightmapUV = this.rgbabro[4];
            overlayUV = this.rgbabro[5];
        }

        final boolean flat = this.material.flat;
        Vertex[] normals = flat ? this.normalList : this.normals;
        final Vector3f dn = this.dummy3;
        final Matrix3f norms = matrixstack$entry.normal();

        Vertex vertex;
        Vertex normal;
        TextureCoordinate textureCoordinate;

        float du = (float) this.uvShift[0];
        float dv = (float) this.uvShift[1];
        float su = 1;
        float sv = 1;

        if (this.material.getTexture() != null)
        {
            float[] ouv = this.material.getTexture().getTexOffset();
            float[] suv = this.material.getTexture().getTexScale();
            du += ouv[0];
            dv += ouv[1];

            su *= suv[0];
            sv *= suv[1];
        }

        if (this.renderScale != 1)
        {
            float dx = (max.x - min.x) / 2;
            float mx = min.x + dx;

            float dy = (max.y - min.y) / 2;
            float my = min.y + dy;

            float dz = (max.z - min.z) / 2;
            float mz = min.z + dz;

            // This loop is copied here vs below for performance reasons, we
            // can't guarentee compiler flags are set properly.
            for (int i0 = 0; i0 < this.order.length; i0++)
            {
                int i = this.order[i0];

                verts++;

                normal = normals[i0];

                // Normals first, as they define culling.
                nx = normal.x;
                ny = normal.y;
                nz = normal.z;

                dn.set(nx, ny, nz);
                dn.transform(norms);

                // Next we can pull out the coordinates if not culled.
                textureCoordinate = this.textureCoordinates[i];
                vertex = this.vertices[i];

                x = Math.fma(this.renderScale, (vertex.x - mx), mx);
                y = Math.fma(this.renderScale, (vertex.y - my), my);
                z = Math.fma(this.renderScale, (vertex.z - mz), mz);

                dp.set(x, y, z, 1);
                dp.transform(pos);

                // This results in u * su + du
                u = Math.fma(textureCoordinate.u, su, du);
                v = Math.fma(textureCoordinate.v, sv, dv);

                // We use the default mob format, since that is what mobs use.
                // This means we need these in this order!
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
        else for (int i0 = 0; i0 < this.order.length; i0++)
        {
            int i = this.order[i0];

            verts++;

            normal = normals[i0];

            // Normals first, as they define culling.
            nx = normal.x;
            ny = normal.y;
            nz = normal.z;

            dn.set(nx, ny, nz);
            dn.transform(norms);

            // Next we can pull out the coordinates if not culled.
            textureCoordinate = this.textureCoordinates[i];
            vertex = this.vertices[i];

            x = vertex.x;
            y = vertex.y;
            z = vertex.z;

            dp.set(x, y, z, 1);
            dp.transform(pos);

            // This results in u * su + du
            u = Math.fma(textureCoordinate.u, su, du);
            v = Math.fma(textureCoordinate.v, sv, dv);

            // We use the default mob format, since that is what mobs use.
            // This means we need these in this order!
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

    public void renderShape(final PoseStack mat, VertexConsumer buffer, final IPartTexturer texturer)
    {
        // Apply Texturing.
        if (texturer != null)
        {
            texturer.shiftUVs(this.material.name, this.uvShift);
            if (texturer.isHidden(this.material.name)) return;
            if (!same_mat && texturer.isHidden(this.name)) return;
            texturer.modifiyRGBA(this.material.name, material.rgbabro);
            if (!same_mat) texturer.modifiyRGBA(this.name, material.rgbabro);
        }
        buffer = this.material.preRender(mat, buffer, this.vertexMode);
        if (this.material.emissiveMagnitude > 0)
        {
            final int j = (int) (this.material.emissiveMagnitude * 15);
            material.rgbabro[4] = j << 20 | j << 4;
        }
        this.doRender(mat, buffer, texturer);
    }

    public void setMaterial(final Material material)
    {
        this.material = material;
        this.name = material.name;
        same_mat = true;
    }
}
