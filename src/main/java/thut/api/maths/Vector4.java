package thut.api.maths;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import thut.core.common.ThutCore;

public class Vector4
{
    public float x, y, z, w;
    @OnlyIn(value = Dist.CLIENT)
    private Quaternionf quat;

    public Vector4()
    {
        this.y = this.z = this.x = 0;
        this.w = 1;
    }

    @OnlyIn(value = Dist.CLIENT)
    public Vector4(final Quaternionf quat)
    {
        // TODO: Check this
        this(quat.x(), quat.y(), quat.z(), quat.w());
        this.quat = quat;
    }

    @OnlyIn(value = Dist.CLIENT)
    public Vector4 set(final Quaternionf quat)
    {
        // TODO: Check this
        this.set(quat.x(), quat.y(), quat.z(), quat.w());
        this.quat = quat;
        return this;
    }

    public Vector4(final CompoundTag nbt)
    {
        this();
        this.x = nbt.getFloat("x");
        this.y = nbt.getFloat("y");
        this.z = nbt.getFloat("z");
        this.w = nbt.getFloat("w");
    }

    public Vector4(final double posX, final double posY, final double posZ, final float w)
    {
        this.x = (float) posX;
        this.y = (float) posY;
        this.z = (float) posZ;
        this.w = w;
    }

    public Vector4(final Entity e)
    {
        this(e.getX(), e.getY(), e.getZ(), 0);
    }

    public Vector4(final String toParse)
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

    public Vector4 add(final Vector4 b)
    {
        final Vector4 quat = new Vector4();

        quat.w = this.w + b.w;
        quat.x = this.x + b.x;
        quat.y = this.y + b.y;
        quat.z = this.z + b.z;

        return quat;
    }

    public Vector4 addAngles(final Vector4 toAdd)
    {
        final Vector4 ret = this.copy();
        final Vector4 temp = toAdd.copy();

        if (Float.isNaN(temp.x) || Float.isNaN(temp.y) || Float.isNaN(temp.z) || Float.isNaN(temp.w))
        {
            ThutCore.LOGGER.error(temp + " " + toAdd, new IllegalStateException());
            temp.x = 0;
            temp.y = 0;
            temp.z = 0;
            temp.w = 1;
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
    public boolean equals(final Object o)
    {
        if (o instanceof Vector4 v) return v.x == this.x && v.y == this.y && v.z == this.z && v.w == this.w;
        return super.equals(o);
    }

    @OnlyIn(value = Dist.CLIENT)
    public void glRotate(final PoseStack mat)
    {
        mat.mulPose(this.toMCQ());
    }

    @OnlyIn(value = Dist.CLIENT)
    public void glUnRotate(final PoseStack mat)
    {
        mat.mulPose(this.toMCQInv());
    }

    public boolean isEmpty()
    {
        return this.x == 0 && this.z == 0 && this.y == 0;
    }

    public final void mul(final Vector4 q1, final Vector4 q2)
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

    public Vector4 scalarMult(final float scalar)
    {
        final Vector4 ret = new Vector4(this.x, this.y, this.z, this.w);
        ret.w = this.w * scalar;
        return ret;
    }

    public Vector4 set(final float x, final float y, final float z, final float w)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        this.quat = null;
        return this;
    }

    public Vector4 subtractAngles(final Vector4 toAdd)
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
        return "x:" + Mth.floor(this.x) + " y:" + Mth.floor(this.y) + " z:" + Mth.floor(this.z) + " w:"
                + Mth.floor(this.w);
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

    public boolean withinDistance(final float distance, final Vector4 toCheck)
    {
        if ((int) this.w == (int) toCheck.w)
        {
            final double dss = (this.x - toCheck.x) * (this.x - toCheck.x) + (this.y - toCheck.y) * (this.y - toCheck.y)
                    + (this.z - toCheck.z) * (this.z - toCheck.z);
            return dss < distance * distance;
        }
        return false;
    }

    public void writeToNBT(final CompoundTag nbt)
    {
        nbt.putFloat("x", this.x);
        nbt.putFloat("y", this.y);
        nbt.putFloat("z", this.z);
        nbt.putFloat("w", this.w);
    }

    @OnlyIn(value = Dist.CLIENT)
    public Quaternionf toMCQ()
    {
        // TODO: Check this
        if (this.quat != null) return this.quat;
        return this.quat = new Quaternionf(this.x, this.y, this.z, this.w);
    }

    @OnlyIn(value = Dist.CLIENT)
    public Quaternionf toMCQInv()
    {
        final Quaternionf quat = this.toMCQ();
        quat.conjugate();
        return quat;
    }
}
