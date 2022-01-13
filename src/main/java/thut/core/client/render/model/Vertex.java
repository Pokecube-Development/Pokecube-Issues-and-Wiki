package thut.core.client.render.model;

import thut.api.maths.vecmath.Vec3f;

public class Vertex extends Vec3f
{
    /**
     *
     */
    private static final long serialVersionUID = -289335306511899715L;

    public Vertex(final float x, final float y)
    {
        this(x, y, 0F);
    }

    public Vertex(final float x, final float y, final float z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
