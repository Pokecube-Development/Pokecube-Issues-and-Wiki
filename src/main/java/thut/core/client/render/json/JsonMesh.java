package thut.core.client.render.json;

import org.lwjgl.opengl.GL11;

import thut.core.client.render.model.Vertex;
import thut.core.client.render.model.parts.Mesh;
import thut.core.client.render.texturing.TextureCoordinate;

public class JsonMesh extends Mesh
{
    public JsonMesh(Integer[] order, Vertex[] vert, TextureCoordinate[] tex)
    {
        super(order, vert, null, tex, GL11.GL_QUADS);
    }
}
