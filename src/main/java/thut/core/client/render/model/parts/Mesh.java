package thut.core.client.render.model.parts;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;

import net.minecraft.client.Minecraft;
import thut.api.maths.vecmath.Vec3f;
import thut.core.client.render.model.Vertex;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.texturing.TextureCoordinate;
import thut.core.common.ThutCore;

public abstract class Mesh
{
    public static boolean debug = false;

    protected final boolean hasTextures;
    public Vertex[] vertices;
    public Vertex[] normals;
    public TextureCoordinate[] textureCoordinates;
    public Integer[] order;
    Material material;
    public String name;
    public boolean overrideColour = false;
    private final double[] uvShift =
    { 0, 0 };
    final int GL_FORMAT;
    final Vertex[] normalList;
    Vector4f centre = new Vector4f();

    public int[] rgbabro = new int[6];

    private boolean same_mat = false;

    public final Mode vertexMode;

    Vertex min = new Vertex(0, 0);
    Vertex max = new Vertex(0, 0);

    final int iter;

    static double sum;
    static long n;

    public static Vector4f METRIC = new Vector4f(1, 1, 1, 0);

    public Mesh(final Integer[] order, final Vertex[] vert, final Vertex[] norm, final TextureCoordinate[] tex,
            final int GL_FORMAT)
    {
        this.order = order;
        this.vertices = vert;
        this.normals = norm;
        this.textureCoordinates = tex;
        this.hasTextures = tex != null;
        this.GL_FORMAT = GL_FORMAT;
        this.normalList = new Vertex[this.order.length];
        Vertex vertex;
        Vertex normal;
        this.iter = GL_FORMAT == GL11.GL_TRIANGLES ? 3 : 4;

        vertexMode = GL_FORMAT == GL11.GL_TRIANGLES ? Mode.TRIANGLES : Mode.QUADS;

        Vec3f mins = new Vec3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        Vec3f maxs = new Vec3f(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);

        // Calculate the normals for each triangle.
        for (int i = 0; i < this.order.length; i += iter)
        {
            Vec3f v1, v2, v3;
            vertex = this.vertices[this.order[i]];
            v1 = new Vec3f(vertex.x, vertex.y, vertex.z);
            vertex = this.vertices[this.order[i + 1]];
            v2 = new Vec3f(vertex.x, vertex.y, vertex.z);
            vertex = this.vertices[this.order[i + 2]];
            v3 = new Vec3f(vertex.x, vertex.y, vertex.z);

            if (v1.x < mins.x) mins.x = v1.x;
            if (v1.y < mins.y) mins.y = v1.y;
            if (v1.z < mins.z) mins.z = v1.z;
            if (v2.x < mins.x) mins.x = v2.x;
            if (v2.y < mins.y) mins.y = v2.y;
            if (v2.z < mins.z) mins.z = v2.z;
            if (v3.x < mins.x) mins.x = v3.x;
            if (v3.y < mins.y) mins.y = v3.y;
            if (v3.z < mins.z) mins.z = v3.z;

            if (v1.x > maxs.x) maxs.x = v1.x;
            if (v1.y > maxs.y) maxs.y = v1.y;
            if (v1.z > maxs.z) maxs.z = v1.z;
            if (v2.x > maxs.x) maxs.x = v2.x;
            if (v2.y > maxs.y) maxs.y = v2.y;
            if (v2.z > maxs.z) maxs.z = v2.z;
            if (v3.x > maxs.x) maxs.x = v3.x;
            if (v3.y > maxs.y) maxs.y = v3.y;
            if (v3.z > maxs.z) maxs.z = v3.z;

            centre.add(v1.x, v1.y, v1.z, 0);
            centre.add(v2.x, v2.y, v2.z, 0);
            centre.add(v3.x, v3.y, v3.z, 0);
            if (iter == 4)
            {
                vertex = this.vertices[this.order[i + 3]];
                Vec3f v4 = new Vec3f(vertex.x, vertex.y, vertex.z);
                centre.add(v4.x, v4.y, v4.z, 0);
                if (v4.x < mins.x) mins.x = v4.x;
                if (v4.y < mins.y) mins.y = v4.y;
                if (v4.z < mins.z) mins.z = v4.z;

                if (v4.x > maxs.x) maxs.x = v4.x;
                if (v4.y > maxs.y) maxs.y = v4.y;
                if (v4.z > maxs.z) maxs.z = v4.z;
            }

            final Vec3f a = new Vec3f(v2);
            a.sub(v1);
            final Vec3f b = new Vec3f(v3);
            b.sub(v1);
            final Vec3f c = new Vec3f();
            c.cross(a, b);
            c.normalize();
            normal = new Vertex(c.x, c.y, c.z);
            if (Double.isNaN(normal.x))
            {
                normal.x = 0;
                normal.y = 0;
                normal.z = 1;
            }
            this.normalList[i] = normal;
            this.normalList[i + 1] = normal;
            this.normalList[i + 2] = normal;
            if (iter == 4) this.normalList[i + 3] = normal;
        }

        if (this.normals == null) this.normals = this.normalList;

        centre.mul(1.0f / order.length);

        min.set(mins);
        max.set(maxs);

        // Initialize a "default" material for us
        this.material = new Material("auto:" + this.name);
    }

    public void scale(float scale)
    {
        for (Vertex v : this.vertices) v.scale(scale);
    }

    private final Vector3f dummy3 = new Vector3f();
    private final Vector3f dummy_1 = new Vector3f();
    private final Vector4f dummy4 = new Vector4f();
    private final TextureCoordinate dummyTex = new TextureCoordinate(0, 0);

    protected void doRender(final PoseStack mat, final VertexConsumer buffer, final IPartTexturer texturer)
    {
        Vertex vertex;
        Vertex normal;

        TextureCoordinate textureCoordinate = dummyTex;
        final boolean flat = this.material.flat;
        float red = material.rgbabro[0] / 255f;
        float green = material.rgbabro[1] / 255f;
        float blue = material.rgbabro[2] / 255f;
        float alpha = this.material.alpha * material.rgbabro[3] / 255f;
        int lightmapUV = material.rgbabro[4];
        int overlayUV = material.rgbabro[5];

        if (debug || overrideColour)
        {
            red = this.rgbabro[0] / 255f;
            green = this.rgbabro[1] / 255f;
            blue = this.rgbabro[2] / 255f;
            alpha = this.material.alpha * this.rgbabro[3] / 255f;
            lightmapUV = this.rgbabro[4];
            overlayUV = this.rgbabro[5];
        }

        final PoseStack.Pose matrixstack$entry = mat.last();
        final Matrix4f pos = matrixstack$entry.pose();
        final Matrix3f norms = matrixstack$entry.normal();
        final Vector4f dp = this.dummy4;
        final com.mojang.math.Vector3f dn = this.dummy3;

        float x, y, z, nx, ny, nz, u, v;
        if (ThutCore.getConfig().modelCullThreshold > 0)
        {
            float a = Minecraft.getInstance().getWindow().getScreenHeight()
                    * Minecraft.getInstance().getWindow().getScreenWidth() * 1e-3f;
            a = (float) Math.sqrt(a);

            dummy_1.set(max.x - min.x, max.y - min.y, max.z - min.z);
            float len = (float) Math.sqrt(dummy_1.dot(dummy_1));
            dp.set(len, len, len, 0);
            dp.transform(pos);
            dp.mul(a);
            double dr2_us = dp.dot(dp);

            dp.set(0, 0, 0, 1);
            dp.transform(pos);
            double dr2_2 = dp.dot(dp);

            boolean size_cull = (dr2_us / dr2_2) < ThutCore.getConfig().modelCullThreshold;

            if (size_cull) return;
        }
        // Loop over this rather than the array directly, so that we can skip by
        // more than 1 if culling.
        for (int i0 = 0; i0 < this.order.length; i0++)
        {
            Integer i = this.order[i0];

            if (flat) normal = this.normalList[i0];
            else normal = this.normals[i];

            // Normals first, as they define culling.
            nx = normal.x;
            ny = normal.y;
            nz = normal.z;

            dn.set(nx, ny, nz);
            dn.transform(norms);

            // Next we can pull out the coordinates if not culled.
            if (this.hasTextures) textureCoordinate = this.textureCoordinates[i];
            vertex = this.vertices[i];

            x = vertex.x;
            y = vertex.y;
            z = vertex.z;

            dp.set(x, y, z, 1);
            dp.transform(pos);

            u = textureCoordinate.u + (float) this.uvShift[0];
            v = textureCoordinate.v + (float) this.uvShift[1];

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
