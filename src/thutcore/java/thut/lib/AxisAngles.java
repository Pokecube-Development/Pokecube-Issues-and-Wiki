package thut.lib;

import org.joml.Quaternionf;

import com.mojang.math.Axis;

public class AxisAngles
{
    public static Axis XN = Axis.XN;
    public static Axis XP = Axis.XP;
    public static Axis YN = Axis.YN;
    public static Axis YP = Axis.YP;
    public static Axis ZN = Axis.ZN;
    public static Axis ZP = Axis.ZP;

    public static Quaternionf MODEL_ROTATE = new Quaternionf();
    static
    {
        MODEL_ROTATE.rotateXYZ((float) Math.PI / 2, 0, (float) Math.PI);
    }
}
