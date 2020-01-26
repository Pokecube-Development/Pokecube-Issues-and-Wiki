package thut.core.client.render.tabula;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import thut.api.maths.vecmath.Vector3f;
import thut.core.client.render.model.Vertex;
import thut.core.client.render.model.parts.Mesh;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.texturing.TextureCoordinate;

public class TblMesh extends Mesh
{

    public TblMesh(final Integer[] order, final Vertex[] vert, final Vertex[] norm, final TextureCoordinate[] tex)
    {
        super(order, vert, norm, tex);
    }

    @Override
    protected void doRender(final IPartTexturer texturer)
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

        GL11.glBegin(GL11.GL_QUADS);
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

}
