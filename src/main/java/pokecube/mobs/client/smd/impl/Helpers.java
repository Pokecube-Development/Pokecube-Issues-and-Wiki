package pokecube.mobs.client.smd.impl;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import thut.api.maths.vecmath.Mat4f;
import thut.api.maths.vecmath.Vec3f;
import thut.core.client.render.model.VectorMath;

/** Misc helper methods. */
public class Helpers
{
    static final Vec3f X_AXIS = new Vec3f(1.0F, 0.0F, 0.0F);
    static final Vec3f Y_AXIS = new Vec3f(0.0F, 1.0F, 0.0F);
    static final Vec3f Z_AXIS = new Vec3f(0.0F, 0.0F, 1.0F);

    /**
     * Ensures that the given index will fit in the list.
     *
     * @param list
     *            array to ensure has capacity
     * @param i
     *            index to check.
     */
    public static void ensureFits(final ArrayList<?> list, final int index)
    {
        while (list.size() <= index)
            list.add(null);
    }

    @OnlyIn(Dist.CLIENT)
    /**
     * Gets an input stream for the given resourcelocation.
     *
     * @param resloc
     * @return
     */
    public static BufferedInputStream getStream(final ResourceLocation resloc)
    {
        try
        {
            return new BufferedInputStream(Minecraft.getInstance().getResourceManager().getResource(resloc)
                    .getInputStream());
        }
        catch (final FileNotFoundException nofile)
        {
            return null;
        }
        catch (final IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Makes a new matrix4f for the given values This works as follows: A blank
     * matrix4f is made via new Matrix4f(), then the matrix is translated by x1,
     * y1, z1, and then it is rotated by zr, yr and xr, in that order, along
     * their respective axes.
     */
    public static Mat4f makeMatrix(final float xl, final float yl, final float zl, final float xr, final float yr,
            final float zr)
    {
        return VectorMath.fromVector6f(xl, yl, zl, xr, yr, zr);
    }
}