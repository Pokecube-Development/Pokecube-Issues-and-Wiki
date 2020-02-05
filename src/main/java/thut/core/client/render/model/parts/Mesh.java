package thut.core.client.render.model.parts;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import thut.api.maths.vecmath.Vector3f;
import thut.core.client.render.model.Vertex;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.texturing.TextureCoordinate;

public abstract class Mesh
{
    private int                meshId  = 0;
    protected final boolean    hasTextures;
    public Vertex[]            vertices;
    public Vertex[]            normals;
    public TextureCoordinate[] textureCoordinates;
    public Integer[]           order;
    public int[]               rgbabro;
    private Material           material;
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
        // Calculate the normals for each triangle.
        for (int i = 0; i < this.order.length; i += 3)
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
            this.normalList[i] = normal;
            this.normalList[i + 1] = normal;
            this.normalList[i + 2] = normal;
        }
    }

    protected void doRender(final MatrixStack mat, final IVertexBuilder buffer, final IPartTexturer texturer)
    {
        Vertex vertex;
        Vertex normal;
        TextureCoordinate textureCoordinate = new TextureCoordinate(0, 0);
        boolean flat = true;
        if (texturer != null) flat = texturer.isFlat(this.name);
        final int red = this.rgbabro[0];
        final int green = this.rgbabro[1];
        final int blue = this.rgbabro[2];
        final int alpha = this.rgbabro[3];
        final int overlayUV = this.rgbabro[4];
        final int lightmapUV = this.rgbabro[5];
        int n = 0;
        for (final Integer i : this.order)
        {
            if (this.hasTextures) textureCoordinate = this.textureCoordinates[i];
            vertex = this.vertices[i];
            normal = this.normals[i];
            if (flat) normal = this.normalList[n];
            // We use the default Item format, since that is what mobs use.
            // This means we need these in this order!
            buffer.vertex(vertex.x, vertex.y, vertex.z, red, green, blue, alpha, textureCoordinate.u,
                    textureCoordinate.v, overlayUV, lightmapUV, normal.x, normal.y, normal.z);
            n++;
        }
    }

    private void compileList(final MatrixStack mat, final IVertexBuilder buffer, final IPartTexturer texturer)
    {
        if (!GL11.glIsList(this.meshId))
        {
            if (this.material != null && texturer != null && !texturer.hasMapping(this.material.name)
                    && this.material.texture != null)
                texturer.addMapping(this.material.name, this.material.texture);
            this.meshId = GL11.glGenLists(1);
            GL11.glNewList(this.meshId, GL11.GL_COMPILE);
            this.doRender(mat, buffer, texturer);
            GL11.glEndList();
        }
    }

    public void renderShape(final MatrixStack mat, final IVertexBuilder buffer, final IPartTexturer texturer)
    {
        // Compiles the list if the meshId is invalid.
        this.compileList(mat, buffer, texturer);
        boolean textureShift = false;
        // Apply Texturing.
        if (texturer != null)
        {
            texturer.applyTexture(this.name);
            if (textureShift = texturer.shiftUVs(this.name, this.uvShift))
            {
                GL11.glMatrixMode(GL11.GL_TEXTURE);
                mat.translate(this.uvShift[0], this.uvShift[1], 0.0F);
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
            }
        }
        if (this.material != null) this.material.preRender(mat, buffer);
        // Call the list
        GL11.glCallList(this.meshId);
        GL11.glFlush();

        // Reset Texture Matrix if changed.
        if (textureShift)
        {
            GL11.glMatrixMode(GL11.GL_TEXTURE);
            GL11.glLoadIdentity();
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
        }
    }

    public void setMaterial(final Material material)
    {
        this.material = material;
        this.name = material.name;
    }
}
