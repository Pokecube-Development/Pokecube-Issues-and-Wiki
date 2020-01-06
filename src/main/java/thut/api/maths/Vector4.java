package thut.api.maths;

import org.lwjgl.opengl.GL11;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;

public class Vector4
{
    public static Vector4 fromAngles(float x, float y, float z)
    {
        Vector4 angle = null;
        if (z != 0) angle = new Vector4(0, 0, 1, z);
        if (x != 0) if (angle != null) angle = angle.addAngles(new Vector4(1, 0, 0, x));
        else angle = new Vector4(1, 0, 0, x);
        if (y != 0) if (angle != null) angle = angle.addAngles(new Vector4(0, 1, 0, y));
        else angle = new Vector4(0, 1, 0, y);
        if (angle == null) angle = new Vector4();
        return angle;
    }

    public float x, y, z, w;

    public Vector4()
    {
        this.y = this.z = this.w = 0;
        this.x = 1;
    }

    public Vector4(CompoundNBT nbt)
    {
        this();
        this.x = nbt.getFloat("x");
        this.y = nbt.getFloat("y");
        this.z = nbt.getFloat("z");
        this.w = nbt.getFloat("w");
    }

    public Vector4(double posX, double posY, double posZ, float w)
    {
        this.x = (float) posX;
        this.y = (float) posY;
        this.z = (float) posZ;
        this.w = w;
    }

    public Vector4(Entity e)
    {
        this(e.posX, e.posY, e.posZ, e.dimension.getId());
    }

    public Vector4(String toParse)
    {
        final String[] vals = toParse.split(" ");
        if (vals.length == 4)
        {
            this.x = Float.parseFloat(vals[0]);
            this.y = Float.parseFloat(vals[1]);
            this.z = Float.parseFloat(vals[2]);
            this.w = Float.parseFloat(vals[3]);
        }
    }

    public Vector4 add(Vector4 b)
    {
        final Vector4 quat = new Vector4();

        quat.w = this.w + b.w;
        quat.x = this.x + b.x;
        quat.y = this.y + b.y;
        quat.z = this.z + b.z;

        return quat;
    }

    public Vector4 addAngles(Vector4 toAdd)
    {
        final Vector4 ret = this.copy();
        final Vector4 temp = toAdd.copy();

        if (Float.isNaN(temp.x) || Float.isNaN(temp.y) || Float.isNaN(temp.z) || Float.isNaN(temp.w))
        {
            System.out.println(temp + " " + toAdd);
            new Exception().printStackTrace();
            temp.x = 0;
            temp.y = 0;
            temp.z = 1;
            temp.w = 0;
        }
        temp.toQuaternion();
        ret.toQuaternion();
        ret.mul(ret.copy(), temp);
        return ret.toAxisAngle();
    }

    public Vector4 copy()
    {
        return new Vector4(this.x, this.y, this.z, this.w);
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof Vector4)
        {
            final Vector4 v = (Vector4) o;
            return v.x == this.x && v.y == this.y && v.z == this.z && v.w == this.w;
        }

        return super.equals(o);
    }

    public void glRotate()
    {
        GL11.glRotatef(this.w, this.x, this.y, this.z);
    }

    public void glRotateMinus()
    {
        GL11.glRotatef(-this.w, this.x, this.y, this.z);
    }

    public boolean isEmpty()
    {
        return this.x == 0 && this.z == 0 && this.y == 0;
    }

    public final void mul(Vector4 q1, Vector4 q2)
    {
        this.x = q1.x * q2.w + q1.y * q2.z - q1.z * q2.y + q1.w * q2.x;
        this.y = -q1.x * q2.z + q1.y * q2.w + q1.z * q2.x + q1.w * q2.y;
        this.z = q1.x * q2.y - q1.y * q2.x + q1.z * q2.w + q1.w * q2.z;
        this.w = -q1.x * q2.x - q1.y * q2.y - q1.z * q2.z + q1.w * q2.w;
    }

    public Vector4 normalize()
    {
        float s = this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w;
        s = (float) Math.sqrt(s);
        this.x /= s;
        this.y /= s;
        this.z /= s;
        this.w /= s;

        return this;
    }

    public Vector4 scalarMult(float scalar)
    {
        final Vector4 ret = new Vector4(this.x, this.y, this.z, this.w);
        ret.w = this.w * scalar;
        return ret;
    }

    public Vector4 set(float x, float y, float z, float w)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    public Vector4 subtractAngles(Vector4 toAdd)
    {
        final Vector4 temp = new Vector4(toAdd.x, toAdd.y, toAdd.z, -toAdd.w);
        return this.addAngles(temp);
    }

    /**
     * The default is axis angle for use with openGL
     *
     * @return
     */
    public Vector4 toAxisAngle()
    {
        final float qw = this.w;
        final float qx = this.x;
        final float qy = this.y;
        final float qz = this.z;

        if (this.w == 0)
        {
            this.x = 1;
            this.y = 0;
            this.z = 0;

            return this;
        }

        this.w = (float) Math.toDegrees(2 * Math.acos(qw));
        final float s = (float) Math.sqrt(1 - qw * qw);

        if (s == 0)
        {
            // System.err.println("Error "+this);
            // new Exception().printStackTrace();
        }

        if (s > 0.001f)
        {
            this.x = qx / s;
            this.y = qy / s;
            this.z = qz / s;
        }
        final float rad = (float) Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);

        this.x = this.x / rad;
        this.y = this.y / rad;
        this.z = this.z / rad;

        return this;
    }

    public String toIntString()
    {
        return "x:" + MathHelper.floor(this.x) + " y:" + MathHelper.floor(this.y) + " z:" + MathHelper.floor(this.z)
                + " w:" + MathHelper.floor(this.w);
    }

    public Vector4 toQuaternion()
    {
        final double a = Math.toRadians(this.w);
        final float ax = this.x;
        final float ay = this.y;
        final float az = this.z;

        this.w = (float) Math.cos(a / 2);
        this.x = (float) (ax * Math.sin(a / 2));
        this.y = (float) (ay * Math.sin(a / 2));
        this.z = (float) (az * Math.sin(a / 2));

        return this.normalize();
    }

    @Override
    public String toString()
    {
        return "x:" + this.x + " y:" + this.y + " z:" + this.z + " w:" + this.w;
    }

    public boolean withinDistance(float distance, Vector4 toCheck)
    {
        if ((int) this.w == (int) toCheck.w && toCheck.x >= this.x - distance && toCheck.z >= this.z - distance
                && toCheck.y >= this.y - distance && toCheck.y <= this.y + distance && toCheck.x <= this.x + distance
                && toCheck.z <= this.z + distance) return true;

        return false;
    }

    public void writeToNBT(CompoundNBT nbt)
    {
        nbt.putFloat("x", this.x);
        nbt.putFloat("y", this.y);
        nbt.putFloat("z", this.z);
        nbt.putFloat("w", this.w);
    }
}
