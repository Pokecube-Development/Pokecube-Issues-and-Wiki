package thut.core.client.render.x3d;

import org.lwjgl.opengl.GL11;

import thut.core.client.render.model.Vertex;
import thut.core.client.render.model.parts.Mesh;
import thut.core.client.render.texturing.TextureCoordinate;

public class X3dMesh extends Mesh
{
    public X3dMesh(final Integer[] order, final Vertex[] vert, final Vertex[] norm, final TextureCoordinate[] tex)
    {
        super(order, vert, norm, tex, GL11.GL_TRIANGLES);
    }
}
