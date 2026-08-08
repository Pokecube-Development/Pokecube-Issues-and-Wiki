package thut.core.client.render.model;

import org.joml.Vector3f;

public class Vertex extends Vector3f
{
    public Vertex(final float x, final float y)
    {
        this(x, y, 0F);
    }

    public Vertex(final float x, final float y, final float z)
    {
        super(x, y, z);
    }

    public Vertex() {}
}
