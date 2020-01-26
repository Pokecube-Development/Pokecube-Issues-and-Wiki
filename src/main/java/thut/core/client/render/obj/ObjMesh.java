package thut.core.client.render.obj;

import org.lwjgl.opengl.GL11;

import thut.core.client.render.model.Vertex;
import thut.core.client.render.model.parts.Mesh;
import thut.core.client.render.texturing.TextureCoordinate;

public class ObjMesh extends Mesh
{

    public ObjMesh(final Integer[] order, final Vertex[] vert, final Vertex[] norm, final TextureCoordinate[] tex)
    {
        super(order, vert, norm, tex, GL11.GL_QUADS);
    }
}
