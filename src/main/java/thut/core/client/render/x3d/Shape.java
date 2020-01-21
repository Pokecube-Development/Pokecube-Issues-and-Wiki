package thut.core.client.render.x3d;

import thut.api.maths.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import thut.core.client.render.model.Vertex;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.texturing.TextureCoordinate;

public class Shape
{
    private int                meshId  = 0;
    private final boolean      hasTextures;
    public Vertex[]            vertices;
    public Vertex[]            normals;
    public TextureCoordinate[] textureCoordinates;
    public Integer[]           order;
    private Material           material;
    public String              name;
    private final double[]     uvShift = { 0, 0 };

    public Shape(Integer[] order, Vertex[] vert, Vertex[] norm, TextureCoordinate[] tex)
    {
        this.order = order;
        this.vertices = vert;
        this.normals = norm;
        this.textureCoordinates = tex;
        this.hasTextures = tex != null;
    }

    void addTris(IPartTexturer texturer)
    {
        Vertex vertex;
        Vertex normal;
        TextureCoordinate textureCoordinate;
        final Vector3f[] normalList = new Vector3f[this.order.length];
        boolean flat = true;
        if (texturer != null) flat = texturer.isFlat(this.name);
        if (flat)
        {
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
                normalList[i] = c;
                normalList[i + 1] = c;
                normalList[i + 2] = c;
            }
            GL11.glShadeModel(GL11.GL_FLAT);
        }
        else GL11.glShadeModel(GL11.GL_SMOOTH);

        if (!this.hasTextures) GlStateManager.disableTexture();

        GL11.glBegin(GL11.GL_TRIANGLES);
        int n = 0;
        for (final Integer i : this.order)
        {
            if (this.hasTextures)
            {
                textureCoordinate = this.textureCoordinates[i];
                GL11.glTexCoord2d(textureCoordinate.u, textureCoordinate.v);
            }
            vertex = this.vertices[i];
            if (flat)
            {
                final Vector3f norm = normalList[n];
                GL11.glNormal3f(norm.x, norm.y, norm.z);
            }
            else
            {
                normal = this.normals[i];
                GL11.glNormal3f(normal.x, normal.y, normal.z);
            }
            n++;
            GL11.glVertex3f(vertex.x, vertex.y, vertex.z);
        }
        GL11.glEnd();

        if (!flat) GL11.glShadeModel(GL11.GL_FLAT);

        if (!this.hasTextures) GlStateManager.enableTexture();
    }

    private void compileList(IPartTexturer texturer)
    {
        if (!GL11.glIsList(this.meshId))
        {
            if (this.material != null && texturer != null && !texturer.hasMapping(this.material.name)
                    && this.material.texture != null) texturer.addMapping(this.material.name, this.material.texture);
            this.meshId = GL11.glGenLists(1);
            GL11.glNewList(this.meshId, GL11.GL_COMPILE);
            this.addTris(texturer);
            GL11.glEndList();
        }
    }

    public void renderShape(IPartTexturer texturer)
    {
        // Compiles the list if the meshId is invalid.
        this.compileList(texturer);
        boolean textureShift = false;
        // Apply Texturing.
        if (texturer != null)
        {
            texturer.applyTexture(this.name);
            if (textureShift = texturer.shiftUVs(this.name, this.uvShift))
            {
                GL11.glMatrixMode(GL11.GL_TEXTURE);
                GL11.glTranslated(this.uvShift[0], this.uvShift[1], 0.0F);
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
            }
        }
        if (this.material != null) this.material.preRender();
        // Call the list
        GL11.glCallList(this.meshId);
        GL11.glFlush();
        if (this.material != null) this.material.postRender();

        // Reset Texture Matrix if changed.
        if (textureShift)
        {
            GL11.glMatrixMode(GL11.GL_TEXTURE);
            GL11.glLoadIdentity();
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
        }
    }

    public void setMaterial(Material material)
    {
        this.material = material;
        this.name = material.name;
    }
}
