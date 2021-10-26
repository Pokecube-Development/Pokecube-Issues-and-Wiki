package thut.core.client.render.model.parts;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;

import thut.api.maths.vecmath.Vector3f;
import thut.core.client.render.model.Vertex;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.texturing.TextureCoordinate;
import thut.core.common.ThutCore;

public abstract class Mesh
{
    protected final boolean    hasTextures;
    public Vertex[]            vertices;
    public Vertex[]            normals;
    public TextureCoordinate[] textureCoordinates;
    public Integer[]           order;
    public int[]               rgbabro;
    Material                   material;
    public String              name;
    private final double[]     uvShift = { 0, 0 };
    final int                  GL_FORMAT;
    final Vertex[]             normalList;

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
        final int iter = GL_FORMAT == GL11.GL_TRIANGLES ? 3 : 4;
        // Calculate the normals for each triangle.
        for (int i = 0; i < this.order.length; i += iter)
        {
            Vector3f v1, v2, v3;
            vertex = this.vertices[this.order[i]];
            v1 = new Vector3f(vertex.x, vertex.y, vertex.z);
            vertex = this.vertices[this.order[i + 1]];
            v2 = new Vector3f(vertex.x, vertex.y, vertex.z);
            vertex = this.vertices[this.order[i + 2]];
            v3 = new Vector3f(vertex.x, vertex.y, vertex.z);
            final Vector3f a = new Vector3f(v2);
            a.sub(v1);
            final Vector3f b = new Vector3f(v3);
            b.sub(v1);
            final Vector3f c = new Vector3f();
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

        // Initialize a "default" material for us
        this.material = new Material("auto:" + this.name);
    }

    private final com.mojang.math.Vector3f dummy3 = new com.mojang.math.Vector3f();
    private final Vector4f                                dummy4 = new Vector4f();

    protected void doRender(final PoseStack mat, final VertexConsumer buffer, final IPartTexturer texturer)
    {
        Vertex vertex;
        Vertex normal;

        TextureCoordinate textureCoordinate = new TextureCoordinate(0, 0);
        final boolean flat = this.material.flat;
        final float red = this.rgbabro[0] / 255f;
        final float green = this.rgbabro[1] / 255f;
        final float blue = this.rgbabro[2] / 255f;
        final float alpha = this.material.alpha * this.rgbabro[3] / 255f;
        final int lightmapUV = this.rgbabro[4];
        final int overlayUV = this.rgbabro[5];
        int n = 0;
        final PoseStack.Pose matrixstack$entry = mat.last();
        final Matrix4f pos = matrixstack$entry.pose();
        final Matrix3f norms = matrixstack$entry.normal();
        final Vector4f dp = this.dummy4;
        final com.mojang.math.Vector3f dn = this.dummy3;

        if (this.order.length % 3 != 0)
        {
            ThutCore.LOGGER.error("Mesh with illegal size! " + this.name);
            return;
        }

        for (final Integer i : this.order)
        {
            if (this.hasTextures) textureCoordinate = this.textureCoordinates[i];
            vertex = this.vertices[i];
            normal = this.normals[i];
            if (flat) normal = this.normalList[n];

            final float x = vertex.x;
            final float y = vertex.y;
            final float z = vertex.z;

            final float nx = normal.x;
            final float ny = normal.y;
            final float nz = normal.z;

            final float u = textureCoordinate.u + (float) this.uvShift[0];
            final float v = textureCoordinate.v + (float) this.uvShift[1];

            dp.set(x, y, z, 1);
            dp.transform(pos);
            dn.set(nx, ny, nz);
            dn.transform(norms);

            // We use the default Item format, since that is what mobs use.
            // This means we need these in this order!
            buffer.vertex(
            //@formatter:off
                dp.x(), dp.y(), dp.z(),
                red, green, blue, alpha,
                u, v,
                overlayUV, lightmapUV,
                dn.x(), dn.y(), dn.z());
            //@formatter:on
            n++;
        }
    }

    public void renderShape(final PoseStack mat, VertexConsumer buffer, final IPartTexturer texturer)
    {
        // Apply Texturing.
        if (texturer != null)
        {
            final boolean sameName = this.name.equals(this.material.name);
            texturer.shiftUVs(this.material.name, this.uvShift);
            if (texturer.isHidden(this.material.name)) return;
            if (!sameName && texturer.isHidden(this.name)) return;
            texturer.modifiyRGBA(this.material.name, this.rgbabro);
            if (!sameName) texturer.modifiyRGBA(this.name, this.rgbabro);
        }
        buffer = this.material.preRender(mat, buffer);
        if (this.material.emissiveMagnitude > 0)
        {
            final int j = (int) (this.material.emissiveMagnitude * 15);
            this.rgbabro[4] = j << 20 | j << 4;
        }
        this.doRender(mat, buffer, texturer);
    }

    public void setMaterial(final Material material)
    {
        this.material = material;
        this.name = material.name;
    }
}
