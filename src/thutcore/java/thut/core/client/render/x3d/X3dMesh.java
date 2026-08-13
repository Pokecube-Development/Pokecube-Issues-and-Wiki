package thut.core.client.render.x3d;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import thut.core.client.render.model.parts.Mesh;

public class X3dMesh extends Mesh
{
    public X3dMesh(final Integer[] order, final Vector3f[] vert, final Vector3f[] norm, final Vector2f[] tex)
    {
        super(order, vert, norm, tex, GL11.GL_TRIANGLES);
    }
}
