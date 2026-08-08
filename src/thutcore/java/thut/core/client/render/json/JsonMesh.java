package thut.core.client.render.json;

import org.joml.Vector2f;
import org.lwjgl.opengl.GL11;

import thut.core.client.render.model.Vertex;
import thut.core.client.render.model.parts.Mesh;

public class JsonMesh extends Mesh
{
    public JsonMesh(Integer[] order, Vertex[] vert, Vector2f[] tex)
    {
        super(order, vert, null, tex, GL11.GL_QUADS);
    }
}
