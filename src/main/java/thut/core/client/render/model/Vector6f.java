package thut.core.client.render.model;

import javax.vecmath.Vector3f;

public class Vector6f
{
    public Vector3f vector1;
    /** when used for rotation is Euler angles in radians */
    public Vector3f vector2;

    public Vector6f(float x, float y, float z, float x1, float y1, float z1)
    {
        this.vector1 = new Vector3f(x, y, z);
        this.vector2 = new Vector3f(x1, y1, z1);
    }

    public void clean()
    {
        if (Math.abs(this.vector1.x) < 1e-6) this.vector1.x = 0;
        if (Math.abs(this.vector1.y) < 1e-6) this.vector1.y = 0;
        if (Math.abs(this.vector1.z) < 1e-6) this.vector1.z = 0;
        if (Math.abs(this.vector2.x) < 1e-6) this.vector2.x = 0;
        if (Math.abs(this.vector2.y) < 1e-6) this.vector2.y = 0;
        if (Math.abs(this.vector2.z) < 1e-6) this.vector2.z = 0;
    }

    @Override
    public String toString()
    {
        return this.vector1 + " " + this.vector2;
    }
}
