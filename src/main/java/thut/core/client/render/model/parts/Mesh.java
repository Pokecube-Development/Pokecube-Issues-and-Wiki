package thut.core.client.render.model.parts;

import org.lwjgl.opengl.GL11;

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
    private Material           material;
    public String              name;
    private final double[]     uvShift = { 0, 0 };

    public Mesh(final Integer[] order, final Vertex[] vert, final Vertex[] norm, final TextureCoordinate[] tex)
    {
        this.order = order;
        this.vertices = vert;
        this.normals = norm;
        this.textureCoordinates = tex;
        this.hasTextures = tex != null;
    }

    protected abstract void doRender(IPartTexturer texturer);

    private void compileList(final IPartTexturer texturer)
    {
        if (!GL11.glIsList(this.meshId))
        {
            if (this.material != null && texturer != null && !texturer.hasMapping(this.material.name)
                    && this.material.texture != null) texturer.addMapping(this.material.name, this.material.texture);
            this.meshId = GL11.glGenLists(1);
            GL11.glNewList(this.meshId, GL11.GL_COMPILE);
            this.doRender(texturer);
            GL11.glEndList();
        }
    }

    public void renderShape(final IPartTexturer texturer)
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

    public void setMaterial(final Material material)
    {
        this.material = material;
        this.name = material.name;
    }
}
