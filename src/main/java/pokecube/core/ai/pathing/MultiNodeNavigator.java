package pokecube.core.ai.pathing;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.pathing.node.MultiNodeWrapper;

public class MultiNodeNavigator extends PathNavigator2
{
    private static final Field POINTMAP;
    static
    {
        POINTMAP = MultiNodeNavigator.initField();
    }

    private static Field initField()
    {
        for (final Field field : NodeProcessor.class.getDeclaredFields())
            if (field.getName().equals("c") || field.getName().equals("field_176167_b") || field.getName().equals(
                    "pointMap")) try
            {
                field.setAccessible(true);
                final Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                return field;
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.warn("Error setting field access: " + field.getName(), e);
            }
        return null;
    }

    private static void setFinal(final Object objTo, final Object objFrom) throws Exception
    {
        MultiNodeNavigator.POINTMAP.set(objTo, MultiNodeNavigator.POINTMAP.get(objFrom));
    }

    public final NodeProcessor a;
    public final NodeProcessor b;

    public MultiNodeNavigator(final MobEntity entityIn, final World worldIn, final NodeProcessor a,
            final NodeProcessor b, final boolean canFly)
    {
        super(entityIn, worldIn);
        this.a = a;
        this.b = b;
        try
        {
            MultiNodeNavigator.setFinal(b, a);
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.warn("Error copying point maps for pathing", e);
        }
    }

    @Override
    protected boolean canNavigate()
    {
        return true;
    }

    @Override
    protected Vec3d getEntityPosition()
    {
        return new Vec3d(this.entity.posX, this.entity.posY, this.entity.posZ);
    }

    @Override
    public PathFinder getPathFinder(final int num)
    {
        this.nodeProcessor = new MultiNodeWrapper(this);
        this.nodeProcessor.setCanEnterDoors(true);
        return new PathFinder(this.nodeProcessor, num);
    }

    /**
     * Checks if the specified entity can safely walk to the specified location.
     */
    @Override
    protected boolean isDirectPathBetweenPoints(final Vec3d posVec31, final Vec3d posVec32, int sizeX, final int sizeY,
            int sizeZ)
    {
        int i = MathHelper.floor(posVec31.x);
        int j = MathHelper.floor(posVec31.z);
        double d0 = posVec32.x - posVec31.x;
        double d1 = posVec32.z - posVec31.z;
        final double d2 = d0 * d0 + d1 * d1;
        if (d2 < 1.0E-8D) return false;
        else
        {
            final double d3 = 1.0D / Math.sqrt(d2);
            d0 = d0 * d3;
            d1 = d1 * d3;
            sizeX = sizeX + 2;
            sizeZ = sizeZ + 2;
            if (!this.isSafeToStandAt(i, MathHelper.floor(posVec31.y), j, sizeX, sizeY, sizeZ, posVec31, d0, d1))
                return false;
            else
            {
                sizeX = sizeX - 2;
                sizeZ = sizeZ - 2;
                final double d4 = 1.0D / Math.abs(d0);
                final double d5 = 1.0D / Math.abs(d1);
                double d6 = i - posVec31.x;
                double d7 = j - posVec31.z;
                if (d0 >= 0.0D) ++d6;

                if (d1 >= 0.0D) ++d7;

                d6 = d6 / d0;
                d7 = d7 / d1;
                final int k = d0 < 0.0D ? -1 : 1;
                final int l = d1 < 0.0D ? -1 : 1;
                final int i1 = MathHelper.floor(posVec32.x);
                final int j1 = MathHelper.floor(posVec32.z);
                int k1 = i1 - i;
                int l1 = j1 - j;

                while (k1 * k > 0 || l1 * l > 0)
                {
                    if (d6 < d7)
                    {
                        d6 += d4;
                        i += k;
                        k1 = i1 - i;
                    }
                    else
                    {
                        d7 += d5;
                        j += l;
                        l1 = j1 - j;
                    }

                    if (!this.isSafeToStandAt(i, MathHelper.floor(posVec31.y), j, sizeX, sizeY, sizeZ, posVec31, d0,
                            d1)) return false;
                }

                return true;
            }
        }
    }

    /**
     * Returns true if an entity does not collide with any solid blocks at the
     * position.
     */
    private boolean isPositionClear(final int x, final int y, final int z, final int sizeX, final int sizeY,
            final int sizeZ, final Vec3d p_179692_7_, final double p_179692_8_, final double p_179692_10_)
    {
        for (final BlockPos blockpos : BlockPos.getAllInBoxMutable(new BlockPos(x, y, z), new BlockPos(x + sizeX - 1, y
                + sizeY - 1, z + sizeZ - 1)))
        {
            final double d0 = blockpos.getX() + 0.5D - p_179692_7_.x;
            final double d1 = blockpos.getZ() + 0.5D - p_179692_7_.z;
            if (!(d0 * p_179692_8_ + d1 * p_179692_10_ < 0.0D) && !this.world.getBlockState(blockpos).allowsMovement(
                    this.world, blockpos, PathType.LAND)) return false;
        }

        return true;
    }

    /**
     * Returns true when an entity could stand at a position, including solid
     * blocks under the entire entity.
     */
    private boolean isSafeToStandAt(final int x, final int y, final int z, final int sizeX, final int sizeY,
            final int sizeZ, final Vec3d vec31, final double p_179683_8_, final double p_179683_10_)
    {
        final int i = x - sizeX / 2;
        final int j = z - sizeZ / 2;
        if (!this.isPositionClear(i, y, j, sizeX, sizeY, sizeZ, vec31, p_179683_8_, p_179683_10_)) return false;
        else
        {
            for (int k = i; k < i + sizeX; ++k)
                for (int l = j; l < j + sizeZ; ++l)
                {
                    final double d0 = k + 0.5D - vec31.x;
                    final double d1 = l + 0.5D - vec31.z;
                    if (!(d0 * p_179683_8_ + d1 * p_179683_10_ < 0.0D))
                    {
                        PathNodeType pathnodetype = this.nodeProcessor.getPathNodeType(this.world, k, y - 1, l,
                                this.entity, sizeX, sizeY, sizeZ, true, true);
                        if (pathnodetype == PathNodeType.WATER) return false;

                        if (pathnodetype == PathNodeType.LAVA) return false;

                        if (pathnodetype == PathNodeType.OPEN) return false;

                        pathnodetype = this.nodeProcessor.getPathNodeType(this.world, k, y, l, this.entity, sizeX,
                                sizeY, sizeZ, true, true);
                        final float f = this.entity.getPathPriority(pathnodetype);
                        if (f < 0.0F || f >= 8.0F) return false;

                        if (pathnodetype == PathNodeType.DAMAGE_FIRE || pathnodetype == PathNodeType.DANGER_FIRE
                                || pathnodetype == PathNodeType.DAMAGE_OTHER) return false;
                    }
                }

            return true;
        }
    }
}
