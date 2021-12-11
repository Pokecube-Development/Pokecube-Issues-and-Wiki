package thut.api.maths;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import com.mojang.authlib.GameProfile;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.entity.PartEntity;
import thut.core.common.ThutCore;

/** @author Thutmose */
public class Vector3
{
    public static final class MutableBlockPos extends BlockPos
    {
        /** Mutable X Coordinate */
        public int x;
        /** Mutable Y Coordinate */
        public int y;
        /** Mutable Z Coordinate */
        public int z;

        public MutableBlockPos(final int x_, final int y_, final int z_)
        {
            super(0, 0, 0);
            this.x = x_;
            this.y = y_;
            this.z = z_;
        }

        MutableBlockPos(final int p_i46025_1_, final int p_i46025_2_, final int p_i46025_3_, final Object p_i46025_4_)
        {
            this(p_i46025_1_, p_i46025_2_, p_i46025_3_);
        }

        /** Get the X coordinate */
        @Override
        public int getX()
        {
            return this.x;
        }

        /** Get the Y coordinate */
        @Override
        public int getY()
        {
            return this.y;
        }

        /** Get the Z coordinate */
        @Override
        public int getZ()
        {
            return this.z;
        }

        void setTo(final Vector3 vector)
        {
            this.x = vector.intX();
            this.y = vector.intY();
            this.z = vector.intZ();
        }

        public void set(final int x0, final int y0, final int z0)
        {
            this.x = x0;
            this.y = y0;
            this.z = z0;
        }

        @Override
        public BlockPos immutable()
        {
            return new BlockPos(this);
        }
    }

    public static final Vector3 secondAxis = Vector3.getNewVector().set(0, 1, 0);
    public static final Vector3 secondAxisNeg = Vector3.getNewVector().set(0, -1, 0);
    public static final Vector3 firstAxis = Vector3.getNewVector().set(1, 0, 0);
    public static final Vector3 firstAxisNeg = Vector3.getNewVector().set(-1, 0, 0);
    public static final Vector3 thirdAxis = Vector3.getNewVector().set(0, 0, 1);
    public static final Vector3 thirdAxisNeg = Vector3.getNewVector().set(0, 0, -1);

    public static final Vector3 empty = Vector3.getNewVector();
    public static final int length = 3;
    public static Vector3 vecMult = Vector3.getNewVector();
    public static double[][] rotBox = new double[3][3];

    static Vector3 move1 = Vector3.getNewVector();

    static Vector3 move2 = Vector3.getNewVector();

    private static FakePlayer USEDFORRAYTRACECONTEXT = null;

    private static final UUID PLAYERID = new UUID(1234567, 7324156);
    private static final GameProfile FAKEPLAYER = new GameProfile(Vector3.PLAYERID, "raytrace-context");

    public static Vector3 entity(final Entity e)
    {
        if (e != null) return Vector3.getNewVector().set(e.getX(), e.getY() + e.getEyeHeight(), e.getZ());
        return null;
    }

    public static Vector3 findMidPoint(final List<Vector3> points)
    {
        final Vector3 mid = Vector3.getNewVector();
        for (final Vector3 point : points) mid.addTo(point);
        if (points.size() > 0) mid.scalarMultBy(1 / (double) points.size());
        return mid;
    }

    /**
     * Locates the first solid block in the line indicated by the direction
     * vector, starting from the source if range is given as 0, it will check
     * out to 320 blocks.
     *
     * @param world
     * @param source
     * @param direction
     * @param range
     * @return
     */
    public static Vector3 findNextSolidBlock(final BlockGetter world, final Vector3 source, Vector3 direction,
            final double range)
    {
        direction = direction.normalize();
        double xprev = source.x, yprev = source.y, zprev = source.z;
        double dx, dy, dz;
        final Vector3 test = Vector3.getNewVector();
        for (double i = 0; i < range; i += 1)
        {
            dx = i * direction.x;
            dy = i * direction.y;
            dz = i * direction.z;

            final double xtest = source.x + dx, ytest = source.y + dy, ztest = source.z + dz;

            if (ytest > world.getMaxBuildHeight() || ytest < world.getMinBuildHeight()) return null;

            if (!(Vector3.Int(xtest) == Vector3.Int(xprev) && Vector3.Int(ytest) == Vector3.Int(yprev)
                    && Vector3.Int(ztest) == Vector3.Int(zprev)))
            {
                test.set(xtest, ytest, ztest);
                final boolean clear = test.isClearOfBlocks(world);

                if (!clear)
                    return Vector3.getNewVector().set(Vector3.Int(xtest), Vector3.Int(ytest), Vector3.Int(ztest));
            }

            yprev = ytest;
            xprev = xtest;
            zprev = ztest;
        }
        return null;
    }

    public static Vector3 getNewVector()
    {
        return new Vector3();
    }

    /**
     * determines whether the source can see out as far as range in the given
     * direction.
     *
     * @param world
     * @param source
     * @param direction
     * @param range
     * @return
     */
    public static Vector3 getNextSurfacePoint(final BlockGetter world, final Vector3 source, Vector3 direction,
            final double range)
    {
        direction = direction.normalize();

        double dx, dy, dz;
        final MutableBlockPos pos = new MutableBlockPos(0, 0, 0);

        for (double i = 0; i < range; i += 0.0625)
        {
            dx = i * direction.x;
            dy = i * direction.y;
            dz = i * direction.z;
            final double xtest = source.x + dx, ytest = source.y + dy, ztest = source.z + dz;
            boolean check = Vector3.isPointClearBlocks_internal(xtest, ytest - dy, ztest - dz, world, pos);
            check = check && Vector3.isPointClearBlocks_internal(xtest - dx, ytest, ztest - dz, world, pos);
            check = check && Vector3.isPointClearBlocks_internal(xtest - dx, ytest - dy, ztest, world, pos);
            if (!check) return Vector3.getNewVector().set(xtest, ytest, ztest);
        }
        return null;
    }

    public static boolean isPointClearBlocks_internal(final double x, final double y, final double z,
            final BlockGetter world, final MutableBlockPos pos)
    {
        final int x0 = Mth.floor(x), y0 = Mth.floor(y), z0 = Mth.floor(z);
        pos.set(x0, y0, z0);
        final BlockState state = world.getBlockState(pos);
        if (state == null) return true;
        final VoxelShape shape = state.getCollisionShape(world, pos);
        final List<AABB> aabbs = shape.toAabbs();
        for (final AABB aabb : aabbs) if (aabb != null) if (aabb.contains(x - x0, y - y0, z - z0)) return false;
        return true;
    }

    public static int Int(final double x)
    {
        return Mth.floor(x);
    }

    public static boolean isPointClearBlocks(final double x, final double y, final double z, final BlockGetter world)
    {
        final int x0 = Mth.floor(x), y0 = Mth.floor(y), z0 = Mth.floor(z);
        BlockPos pos;
        final BlockState state = world.getBlockState(pos = new BlockPos(x0, y0, z0));
        if (state == null) return true;
        final VoxelShape shape = state.getCollisionShape(world, pos);
        final List<AABB> aabbs = shape.toAabbs();
        for (final AABB aabb : aabbs) if (aabb != null) if (aabb.contains(x - x0, y - y0, z - z0)) return false;
        return true;
    }

    public static boolean isVisibleEntityFromEntity(final Entity looker, final Entity target)
    {
        if (looker == null || target == null) return false;
        if (looker instanceof LivingEntity) return ((LivingEntity) looker).hasLineOfSight(target);
        // TODO consider other raytrace here.
        return false;
    }

    /**
     * determines whether the source can see out as far as range in the
     * givenhasLineOfSight direction.
     *
     * @param world
     * @param source
     * @param direction
     * @param range
     * @return
     */
    public static boolean isVisibleRange(final BlockGetter world, final Vector3 source, Vector3 direction,
            final double range)
    {
        direction = direction.normalize();

        if (world instanceof ServerLevel)
        {
            final Vec3 start = source.toVec3d();
            final Vec3 end = direction.scalarMultBy(range).addTo(source).toVec3d();
            if (Vector3.USEDFORRAYTRACECONTEXT == null)
                Vector3.USEDFORRAYTRACECONTEXT = FakePlayerFactory.get((ServerLevel) world, Vector3.FAKEPLAYER);
            else Vector3.USEDFORRAYTRACECONTEXT.setLevel((ServerLevel) world);
            final ClipContext context = new ClipContext(start, end, ClipContext.Block.COLLIDER, Fluid.NONE,
                    Vector3.USEDFORRAYTRACECONTEXT);
            final BlockHitResult result = world.clip(context);
            return result.getType() == HitResult.Type.MISS;
        }

        double dx, dy, dz;
        for (double i = 0; i < range; i += 0.0625)
        {
            dx = i * direction.x;
            dy = i * direction.y;
            dz = i * direction.z;

            final double xtest = source.x + dx, ytest = source.y + dy, ztest = source.z + dz;

            final boolean check = Vector3.isPointClearBlocks(xtest, ytest, ztest, world);
            if (!check) return false;
        }
        return true;
    }

    public static Vector3 readFromBuff(final ByteBuf dat)
    {
        final Vector3 ret = Vector3.getNewVector();
        ret.x = dat.readDouble();
        ret.y = dat.readDouble();
        ret.z = dat.readDouble();
        return ret;
    }

    public static Vector3 readFromNBT(final CompoundTag nbt, final String tag)
    {
        if (!nbt.contains(tag + "x")) return null;

        final Vector3 ret = Vector3.getNewVector();
        ret.x = nbt.getDouble(tag + "x");
        ret.y = nbt.getDouble(tag + "y");
        ret.z = nbt.getDouble(tag + "z");
        return ret;
    }

    public static void rotateAboutAngles(final Vector3[] points, final double pitch, final double yaw,
            final Vector3 temp, final Vector3 temp1)
    {
        for (int i = 0; i < points.length; i++) points[i] = points[i].rotateAboutAngles(pitch, yaw, temp, temp1);
    }

    /**
     * Returns the dot (scalar) product of the two vectors
     *
     * @param vector1
     * @param vector2
     * @return
     */
    public static double vectorDot(final Vector3 vector1, final Vector3 vector2)
    {
        double dot = 0;
        for (int i = 0; i < Vector3.length; i++) dot += vector1.get(i) * vector2.get(i);
        return dot;
    }

    public double x;

    public double y;

    public double z;

    MutableBlockPos pos;

    private Vector3()
    {
        this.x = this.y = this.z = 0;
    }

    /**
     * This takes degrees then converts to radians, as it seems most people like
     * to work with degrees.
     *
     * @param pitch
     * @param yaw
     */
    private Vector3(final double pitch, final double yaw)
    {
        this.x = 1;
        this.y = Math.toRadians(pitch);
        this.z = Math.toRadians(yaw);
    }

    private Vector3(final double x, final double y, final double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    private Vector3(final Entity e, final boolean bool)
    {
        if (e != null && bool)
        {
            this.x = e.getX();
            this.y = e.getY() + e.getBbHeight() / 2;
            this.z = e.getZ();
        }
        else if (e != null)
        {
            this.x = e.getX();
            this.y = e.getY() + e.getEyeHeight();
            this.z = e.getZ();
        }
    }

    //
    private Vector3(final Object a)
    {
        this();
        this.set(a);
    }

    private Vector3(final Object a, final Object b)
    {
        this();
        final Vector3 A = Vector3.getNewVector().set(a);
        final Vector3 B = Vector3.getNewVector().set(b);
        this.set(B.subtract(A));
    }

    private Vector3(final Vec3 vec)
    {
        this.x = vec.x;
        this.y = vec.y;
        this.z = vec.z;
    }

    public Vector3 add(final double i, final double j, final double k)
    {
        return Vector3.getNewVector().set(this.x + i, j + this.y, k + this.z);
    }

    public void add(final int i, final double j)
    {
        if (i == 0) this.x += j;
        else if (i == 1) this.y += j;
        else if (i == 2) this.z += j;
    }

    /**
     * Adds vectorA to vectorB
     *
     * @param vectorA
     * @param vectorB
     * @return
     */
    public Vector3 add(final Vector3 vectorB)
    {
        final Vector3 vectorC = Vector3.getNewVector();
        for (int i = 0; i < 3; i++) vectorC.set(i, this.get(i) + vectorB.get(i));
        return vectorC;
    }

    public Vector3 addTo(final double i, final double j, final double k)
    {
        this.x += i;
        this.y += j;
        this.z += k;
        return this;
    }

    /**
     * Adds vectorA to vectorB
     *
     * @param vectorA
     * @param vectorB
     * @return
     */
    public Vector3 addTo(final Vector3 b)
    {
        if (b == null) return this;
        this.x += b.x;
        this.y += b.y;
        this.z += b.z;
        return this;
    }

    public void addVelocities(final Entity e)
    {
        e.push(this.x, this.y, this.z);
    }

    public List<Entity> allEntityLocationExcluding(final int range, final double size, Vector3 direction,
            final Vector3 source, final Level world, final Entity excluded)
    {
        direction = direction.normalize();
        final List<Entity> ret = new ArrayList<>();
        Predicate<Entity> predicate = e -> e != excluded;
        predicate = predicate.and(EntitySelector.NO_SPECTATORS);
        final double ds = range;
        final Vec3 vec3 = source.toVec3d();
        final Vec3 vec31 = direction.toVec3d();
        final Vec3 vec32 = vec3.add(vec31.x * ds, vec31.y * ds, vec31.z * ds);
        final float f = 0.5F;
        final AABB aabb = this.getAABB().expandTowards(vec31.x * ds, vec31.y * ds, vec31.z * ds).inflate(f, f, f);
        final List<Entity> mobs = world.getEntities(excluded, aabb, predicate);
        PartEntity<?>[] parts = null;
        for (final Entity entity1 : mobs) if ((parts = entity1.getParts()) != null && parts.length > 0) partcheck:
        for (final PartEntity<?> part : parts)
        {
            final AABB axisalignedbb = part.getBoundingBox().inflate(0.3F);
            final Optional<Vec3> optional = axisalignedbb.clip(vec3, vec32);
            if (optional.isPresent())
            {
                ret.add(entity1);
                break partcheck;
            }
        }
        else
        {
            final AABB axisalignedbb = entity1.getBoundingBox().inflate(0.3F);
            final Optional<Vec3> optional = axisalignedbb.clip(vec3, vec32);
            if (optional.isPresent()) ret.add(entity1);
        }
        return ret;
    }

    public List<Entity> allEntityLocationExcluding(final int range, final double size, Vector3 direction,
            final Vector3 source, final Level world, final Entity excluded, Predicate<Entity> predicate)
    {
        direction = direction.normalize();
        final List<Entity> ret = new ArrayList<>();
        if (predicate == null) predicate = EntitySelector.NO_SPECTATORS;
        final double ds = range;
        final Vec3 vec3 = source.toVec3d();
        final Vec3 vec31 = direction.toVec3d();
        final Vec3 vec32 = vec3.add(vec31.x * ds, vec31.y * ds, vec31.z * ds);
        final float f = 1F;
        final AABB aabb = this.getAABB().expandTowards(vec31.x * ds, vec31.y * ds, vec31.z * ds).inflate(f, f, f);
        final List<Entity> mobs = world.getEntities(excluded, aabb, predicate);
        PartEntity<?>[] parts = null;
        for (final Entity entity1 : mobs) if ((parts = entity1.getParts()) != null && parts.length > 0) partcheck:
        for (final PartEntity<?> part : parts)
        {
            final AABB axisalignedbb = part.getBoundingBox().inflate(size);
            final Optional<Vec3> optional = axisalignedbb.clip(vec3, vec32);
            if (optional.isPresent())
            {
                ret.add(entity1);
                break partcheck;
            }
        }
        else
        {
            final AABB axisalignedbb = entity1.getBoundingBox().inflate(size);
            final Optional<Vec3> optional = axisalignedbb.clip(vec3, vec32);
            if (optional.isPresent()) ret.add(entity1);
        }
        return ret;
    }

    public int blockCount(final BlockGetter world, final Block block, final int range)
    {
        int ret = 0;
        final Vector3 v = this.copy();
        for (int i = -range; i <= range; i++)
            for (int j = -range; j <= range; j++) for (int k = -range; k <= range; k++)
        {
            final Vector3 test = v.set(this).addTo(i, j, k);
            if (test.getBlock(world) == block) ret++;
        }

        return ret;
    }

    public int blockCount2(final LevelAccessor world, final Block block, final int range)
    {
        int ret = 0;
        final Vector3 v = this.copy();
        final ChunkAccess chunk = world.getChunk(new BlockPos(this.intX(), 0, this.intZ()));
        Block testBlock;
        for (int i = -range / 2; i <= range / 2; i++)
            for (int j = -range / 2; j <= range / 2; j++) for (int k = -range / 2; k <= range / 2; k++)
        {
            final int i1 = Mth.floor(this.intX() / 16.0D);
            final int k1 = Mth.floor(this.intZ() / 16.0D);
            final int j1 = Mth.floor(this.intY() / 16.0D);

            final int i2 = Mth.floor((this.intX() + i) / 16.0D);
            final int k2 = Mth.floor((this.intZ() + k) / 16.0D);
            final int j2 = Mth.floor((this.intY() + j) / 16.0D);

            if (!(i1 == i2 && k1 == k2 && j1 == j2)) continue;
            v.set(this);
            final Vector3 test = v.addTo(i, j, k);
            testBlock = chunk.getBlockState(test.getPos()).getBlock();
            if (testBlock == block) ret++;
        }

        return ret;
    }

    public void breakBlock(final Level world, final boolean drops)
    {
        world.destroyBlock(this.getPos(), drops);
    }

    public boolean canSeeSky(final LevelAccessor world)
    {
        if (world.canSeeSky(this.getPos())) return true;
        return world.getHeight(Types.OCEAN_FLOOR, this.intX(), this.intZ()) <= this.y;
    }

    public Vector3 clear()
    {
        return this.set(0, 0, 0);
    }

    public Vector3 copy()
    {
        final Vector3 newVector = Vector3.getNewVector().set(this.x, this.y, this.z);
        return newVector;
    }

    public double distanceTo(final Vector3 vec)
    {
        return this.subtract(vec).mag();
    }

    public double distTo(final Vector3 pointB)
    {
        return this.subtract(pointB).mag();
    }

    public double distToEntity(final Entity e)
    {
        return this.distanceTo(Vector3.entity(e));
    }

    public double distToSq(final Vector3 pointB)
    {
        return this.subtract(pointB).magSq();
    }

    /**
     * Returns the dot (scalar) product of the two vectors
     *
     * @param vector1
     * @param vector2
     * @return
     */
    public double dot(final Vector3 vector2)
    {
        double dot = 0;
        for (int i = 0; i < 3; i++) dot += this.get(i) * vector2.get(i);
        return dot;
    }

    @Override
    public boolean equals(final Object vec)
    {
        if (!(vec instanceof Vector3)) return false;
        final Vector3 v = (Vector3) vec;

        return v.x == this.x && v.y == this.y && v.z == this.z;// sameBlock(v);
    }

    @SuppressWarnings("unchecked")
    public Vector3 findClosestVisibleObject(final BlockGetter world, final boolean water, final int sightDistance,
            final Object matching)
    {
        final int size = Math.min(sightDistance, 30);
        List<Object> list = new ArrayList<>();
        Block seekingBlock = null;
        Class<?> seekingClass = null;
        final boolean predicate = matching instanceof Predicate<?>;
        Predicate<Object> matcher = null;
        if (predicate) matcher = (Predicate<Object>) matching;
        boolean isInterface = false;
        boolean blockList = false;
        boolean predicateList = false;
        final Vector3 temp = Vector3.getNewVector();
        if (matching instanceof Block) seekingBlock = (Block) matching;
        if (matching instanceof Class)
        {
            seekingClass = (Class<?>) matching;
            if (seekingClass.isInterface()) isInterface = true;
        }
        if (matching instanceof Collection<?> && !((Collection<?>) matching).isEmpty())
        {
            if (matching instanceof Collection<?> && ((Collection<?>) matching).toArray()[0] instanceof Block)
            {
                blockList = true;
                list.addAll((Collection<?>) matching);
            }
            if (matching instanceof Collection<?> && ((Collection<?>) matching).toArray()[0] instanceof Predicate<?>)
            {
                predicateList = true;
                list.addAll((Collection<?>) matching);
            }
        }
        double rMag;
        final Vector3 r = Vector3.getNewVector(), rAbs = Vector3.getNewVector(), rHat = Vector3.getNewVector();
        Vector3 rTest = Vector3.getNewVector();
        final Vector3 rTestPrev = Vector3.getNewVector(), rTestAbs = Vector3.getNewVector(),
                ret = Vector3.getNewVector();

        final HashMap<Class<?>, List<Object>> interfaces = new HashMap<>();

        loop:
        for (int i = 0; i < size * size * size; i++)
        {

            Cruncher.indexToVals(i, r);
            rAbs.set(r).addTo(this);
            rHat.set(temp.set(r).norm());
            double rm;
            if ((rm = r.mag()) > size || rm > sightDistance) continue;

            if (rAbs.isAir(world) && !r.isEmpty()) continue;

            rTest.clear();
            rTestPrev.clear();
            rMag = r.mag();
            final float dj = 1;
            for (float j = 0F; j <= rMag; j += dj)
            {
                rTest = temp.set(rHat).scalarMultBy(j);
                if (!rTest.sameBlock(rTestPrev))
                {
                    rTestAbs.set(rTest).addTo(this);
                    final BlockState state = rTestAbs.getBlockState(world);
                    if (state == null) continue loop;
                    final Block b = state.getBlock();
                    if (predicateList) for (final Object o : list) if (((Predicate<BlockState>) o).test(state))
                    {
                        ret.set(rTestAbs);
                        return ret;
                    }
                    if (isInterface)
                    {
                        List<Object> tempList;
                        if ((tempList = interfaces.get(b.getClass())) != null)
                        {}
                        else
                        {
                            tempList = new ArrayList<>();
                            interfaces.put(b.getClass(), tempList);
                            for (final Object o : b.getClass().getInterfaces()) tempList.add(o);
                        }
                        list = tempList;
                    }
                    if (matcher != null && matcher.test(state))
                    {
                        ret.set(rTestAbs);
                        return ret;
                    }
                    else if (seekingBlock != null && b == seekingBlock)
                    {
                        ret.set(rTestAbs);
                        return ret;
                    }
                    else if (!isInterface && seekingClass != null && b.getClass().isAssignableFrom(seekingClass))
                    {
                        ret.set(rTestAbs);
                        return ret;
                    }
                    else if (seekingClass != null && list.contains(seekingClass))
                    {
                        ret.set(rTestAbs);
                        return ret;
                    }
                    else if (blockList && list.contains(b))
                    {
                        ret.set(rTestAbs);
                        return ret;
                    }
                    else if (!rTestAbs.isClearOfBlocks(world)) continue loop;
                    else if (!water && state.getMaterial() == Material.WATER) continue loop;
                }
            }
        }
        return null;
    }

    public Vector3 findNextSolidBlock(final BlockGetter world, final Vector3 direction, final double range)
    {
        return Vector3.findNextSolidBlock(world, this, direction, range);
    }

    public Entity firstEntityExcluding(final double range, final Vec3 vec31, final Level world, final Entity entity,
            Predicate<Entity> predicate)
    {
        Entity pointedEntity = null;
        if (predicate == null) predicate = EntitySelector.NO_SPECTATORS;
        double ds = range;
        final Vec3 vec3 = this.toVec3d();
        final Vec3 vec32 = vec3.add(vec31.x * ds, vec31.y * ds, vec31.z * ds);
        final float f = 2.5F;
        final AABB aabb = this.getAABB().expandTowards(vec31.x * ds, vec31.y * ds, vec31.z * ds).inflate(f, f, f);
        final List<Entity> mobs = world.getEntities(entity, aabb, predicate);
        ds *= ds;
        PartEntity<?>[] parts = null;
        for (final Entity entity1 : mobs) if ((parts = entity1.getParts()) != null && parts.length > 0) partcheck:
        for (final PartEntity<?> part : parts)
        {
            final AABB axisalignedbb = part.getBoundingBox().inflate(0.3F);
            final Optional<Vec3> optional = axisalignedbb.clip(vec3, vec32);
            if (optional.isPresent())
            {
                final double d1 = vec3.distanceToSqr(optional.get());
                if (d1 < ds)
                {
                    pointedEntity = entity1;
                    ds = d1;
                    break partcheck;
                }
            }
        }
        else
        {
            final AABB axisalignedbb = entity1.getBoundingBox().inflate(0.3F);
            final Optional<Vec3> optional = axisalignedbb.clip(vec3, vec32);
            if (optional.isPresent())
            {
                final double d1 = vec3.distanceToSqr(optional.get());
                if (d1 < ds)
                {
                    pointedEntity = entity1;
                    ds = d1;
                }
            }
        }
        return pointedEntity;
    }

    public Vec3 toVec3d()
    {
        return new Vec3(this.x, this.y, this.z);
    }

    public List<Entity> firstEntityLocationExcluding(final int range, final double size, Vector3 direction,
            final Vector3 source, final Level world, final Entity excluded)
    {
        direction = direction.normalize();
        double dx, dy, dz;

        for (double i = 0; i < range; i += 0.0625)
        {
            dx = i * direction.x;
            dy = i * direction.y;
            dz = i * direction.z;

            final double xtest = source.x + dx, ytest = source.y + dy, ztest = source.z + dz;

            final boolean check = Vector3.isPointClearBlocks(xtest, ytest, ztest, world);

            if (!check) break;

            final double x0 = xtest > 0 ? (int) xtest : (int) xtest - 1, y0 = ytest > 0 ? (int) ytest : (int) ytest - 1,
                    z0 = ztest > 0 ? (int) ztest : (int) ztest - 1;
            final List<Entity> targets = world.getEntities(excluded,
                    new AABB(x0 - size, y0 - size, z0 - size, x0 + size, y0 + size, z0 + size));
            if (targets != null && targets.size() > 0)
            {
                final List<Entity> ret = new ArrayList<>();
                for (final Entity e : targets) if (e instanceof Mob) ret.add(e);
                if (ret.size() > 0) return ret;
            }

        }

        return null;
    }

    public double get(final int i)
    {
        assert i < 3;
        return i == 0 ? this.x : i == 1 ? this.y : this.z;
    }

    public AABB getAABB()
    {
        return Matrix3.getAABB(this.x, this.y, this.z, this.x, this.y, this.z);
    }

    public Biome getBiome(final LevelAccessor world)
    {
        return world.getBiome(new BlockPos(this.intX(), this.intY(), this.intZ()));
    }

    public Block getBlock(final BlockGetter worldMap)
    {
        final BlockState state = worldMap.getBlockState(this.getPos());
        if (state == null) return Blocks.AIR;
        return state.getBlock();
    }

    public Block getBlock(final BlockGetter world, final Direction side)
    {
        final Vector3 other = this.offset(side);
        final Block ret = other.getBlock(world);
        return ret;
    }

    public Material getBlockMaterial(final BlockGetter world)
    {
        final BlockState state = world.getBlockState(this.getPos());
        if (state == null || state.getBlock() == null) return Material.AIR;
        return state.getMaterial();
    }

    public BlockState getBlockState(final BlockGetter world)
    {
        return world.getBlockState(this.getPos());
    }

    public float getExplosionResistance(final Explosion boom, final LevelReader world)
    {
        final BlockState state = this.getBlockState(world);
        if (state == null || state.isAir()) return 0;
        final float res = state.getExplosionResistance(world, this.pos, boom);
        return res;
    }

    public int getLightValue(final Level world)
    {
        return world.getMaxLocalRawBrightness(this.getPos());
    }

    public int getMaxY(final LevelAccessor world)
    {
        return this.getMaxY(world, this.intX(), this.intZ());
    }

    public int getMaxY(final LevelAccessor world, final int x, final int z)
    {
        final ChunkAccess chunk = world.getChunk(this.getPos());
        final int y1 = chunk.getHeight(Types.OCEAN_FLOOR, this.intX() & 15, this.intZ() & 15);
        final int y2 = chunk.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, this.intX() & 15, this.intZ() & 15);
        return Math.min(y1, y2);
    }

    public BlockPos getPos()
    {
        if (this.pos == null) this.pos = new MutableBlockPos(this.intX(), this.intY(), this.intZ());
        else this.pos.setTo(this);
        return this.pos;
    }

    public BlockEntity getTileEntity(final BlockGetter world)
    {
        return world.getBlockEntity(this.getPos());
    }

    public BlockEntity getTileEntity(final BlockGetter world, final Direction side)
    {
        final Vector3 other = this.offset(side);
        final BlockEntity ret = other.getTileEntity(world);
        return ret;
    }

    public Vector3 getTopBlockPos(final Level world)
    {
        final int y = this.getTopBlockY(world);
        return Vector3.getNewVector().set(this.intX(), y, this.intZ());
    }

    public int getTopBlockY(final BlockGetter world)
    {
        int ret = world.getMaxBuildHeight();
        {
            for (ret = world.getMaxBuildHeight(); ret > world.getMinBuildHeight(); ret--)
            {
                final BlockState state = world.getBlockState(new BlockPos(this.intX(), ret, this.intZ()));
                if (state == null) continue;
                if (state.getMaterial().isSolid()) return ret;
            }
        }
        return ret;
    }

    @Override
    public int hashCode()
    {
        return super.hashCode();
    }

    public Vector3 horizonalPerp()
    {
        final Vector3 vectorH = Vector3.getNewVector().set(-this.z, 0, this.x);
        return vectorH.norm();
    }

    // */
    public boolean inAABB(final AABB aabb)
    {
        if (this.y >= aabb.maxY || this.y <= aabb.minY) return false;
        if (this.z >= aabb.maxZ || this.z <= aabb.minZ) return false;
        if (this.x >= aabb.maxX || this.x <= aabb.minX) return false;

        return true;
    }

    public boolean inMatBox(final Matrix3 box)
    {
        final Vector3 min = box.get(0);
        final Vector3 max = box.get(1);
        boolean ycheck = false, xcheck = false, zcheck = false;

        if (this.y <= max.y && this.y >= min.y) ycheck = true;
        if (this.z <= max.z && this.z >= min.z) zcheck = true;
        if (this.x <= max.x && this.x >= min.x) xcheck = true;

        return ycheck && zcheck && xcheck;
    }

    public int intX()
    {
        return Mth.floor(this.x);
    }

    public int intY()
    {
        return Mth.floor(this.y);
    }

    public int intZ()
    {
        return Mth.floor(this.z);
    }

    public boolean isAir(final BlockGetter world)
    {
        Material m;
        if (world instanceof Level)
        {
            final BlockState state = world.getBlockState(this.getPos());
            return state.getBlock() == null || (m = this.getBlockMaterial(world)) == null || m == Material.AIR
                    || state.isAir();
        }
        return (m = this.getBlockMaterial(world)) == null || m == Material.AIR;
    }

    public boolean isClearOfBlocks(final BlockGetter world)
    {
        boolean ret = false;
        final BlockState state = world.getBlockState(this.getPos());
        if (state == null) return true;

        ret = this.isAir(world);
        if (!ret) ret = ret || this.getBlockMaterial(world).isLiquid();
        if (!ret) ret = ret || this.getBlockMaterial(world).isReplaceable();
        if (!ret) ret = ret || !this.getBlockMaterial(world).blocksMotion();
        if (!ret)
        {
            final VoxelShape shape = state.getCollisionShape(world, this.getPos());
            final List<AABB> aabbs = shape.toAabbs();
            if (aabbs.size() == 0) return true;
            for (final AABB aabb : aabbs)
                if (aabb != null && aabb.contains(this.x - this.intX(), this.y - this.intY(), this.z - this.intZ()))
                    return false;
            return true;
        }
        return ret;
    }

    public boolean isEmpty()
    {
        return this.x == 0 && this.z == 0 && this.y == 0;
    }

    public boolean isEntityClearOfBlocks(final BlockGetter world, final Entity e)
    {
        boolean ret = false;
        final EntityDimensions size = e.getDimensions(e.getPose());
        final Vector3 v = Vector3.getNewVector();
        final Vector3 v1 = Vector3.getNewVector();
        v.set(this);
        ret = v.addTo(v1.set(0, size.height, 0)).isClearOfBlocks(world);
        if (!ret) return ret;

        for (int i = -1; i <= 1; i++) for (int j = -1; j <= 1; j++)
            ret = ret && v.set(this).addTo(v1.set(i * size.width / 2, 0, j * size.width / 2)).isClearOfBlocks(world);
        if (!ret) return ret;

        for (int i = -1; i <= 1; i++) for (int j = -1; j <= 1; j++) ret = ret && v.set(this)
                .addTo(v1.set(i * size.width / 2, size.height, j * size.width / 2)).isClearOfBlocks(world);

        return ret;
    }

    /**
     * @param world - world the block is in
     * @return if the block is a liquid
     */
    public boolean isFluid(final Level world)
    {
        return !world.getFluidState(this.getPos()).isEmpty();
    }

    public boolean isNaN()
    {
        return Double.isNaN(this.x) || Double.isNaN(this.z) || Double.isNaN(this.y);
    }

    public boolean isOnSurface(final LevelChunk chunk)
    {
        return chunk.getHeight(Types.MOTION_BLOCKING, this.intX() & 15, this.intZ() & 15) <= this.y;
    }

    public boolean isOnSurface(final Level world)
    {
        return this.getMaxY(world) <= this.y;
    }

    public boolean isOnSurfaceIgnoringDecorationAndWater(final LevelChunk chunk, final BlockGetter world)
    {
        final int h = chunk.getHeight(Types.WORLD_SURFACE_WG, this.intX() & 15, this.intZ() & 15);
        return h <= this.y;
    }

    public boolean isPointClearOfEntity(final double x, final double y, final double z, final Entity e)
    {
        final AABB aabb = e.getBoundingBox();

        if (y <= aabb.maxY && y >= aabb.minY) return false;
        if (z <= aabb.maxZ && z >= aabb.minZ) return false;
        if (x <= aabb.maxX && x >= aabb.minX) return false;

        return true;
    }

    public boolean isVisible(final BlockGetter world, final Vector3 location)
    {
        final Vector3 direction = location.subtract(this);
        final double range = direction.mag();
        return Vector3.isVisibleRange(world, this, direction, range);
    }

    public List<Entity> livingEntityAtPoint(final Level world)
    {
        final int x0 = this.intX(), y0 = this.intY(), z0 = this.intZ();
        final List<Entity> ret = new ArrayList<>();
        final List<Mob> targets = world.getEntitiesOfClass(Mob.class, new AABB(x0, y0, z0, x0 + 1, y0 + 1, z0 + 1));
        for (final Entity e : targets) if (!this.isPointClearOfEntity(this.x, this.y, this.z, e)) ret.add(e);
        return ret;
    }

    public List<Entity> livingEntityAtPointExcludingEntity(final Level world, final Entity entity)
    {
        final int x0 = this.intX(), y0 = this.intY(), z0 = this.intZ();
        final List<Entity> ret = new ArrayList<>();
        final List<Mob> targets = world.getEntitiesOfClass(Mob.class, new AABB(x0, y0, z0, x0 + 1, y0 + 1, z0 + 1));
        for (final Entity e : targets)
            if (!this.isPointClearOfEntity(this.x, this.y, this.z, e) && e != entity) ret.add(e);
        return ret;
    }

    /**
     * Returns the magnitude of vector
     *
     * @param vector
     * @return
     */
    public double mag()
    {
        final double vmag = Math.sqrt(this.magSq());
        return vmag;
    }

    /**
     * Returns the magnitude of vector squared
     *
     * @param vector
     * @return
     */
    public double magSq()
    {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    /**
     * Left multiplies the Matrix by the Vector
     *
     * @param Matrix
     * @param vector
     * @return
     */
    public Vector3 matrixMult(final Matrix3 Matrix)
    {
        final Vector3 newVect = Vector3.vecMult.clear();
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < Vector3.length; j++) newVect.add(i, Matrix.get(i).get(j) * this.get(j));
        return newVect;
    }

    public void moveEntity(final Entity e)
    {
        e.setPos(this.x, this.y, this.z);
    }

    /**
     * Normalizes this vector.
     *
     * @param vector
     * @return unit vector in direction of vector.
     */
    public Vector3 norm()
    {
        final double vmag = this.mag();
        if (vmag == 0) return this.clear();
        this.scalarMultBy(1 / vmag);
        return this;
    }

    /**
     * Returns the unit vector in with the same direction as vector.
     *
     * @param vector
     * @return unit vector in direction of vector.
     */
    public Vector3 normalize()
    {
        final double vmag = this.mag();
        final Vector3 vHat = Vector3.getNewVector();
        if (vmag == 0) return vHat.clear();
        vHat.set(this).scalarMultBy(1 / vmag);
        return vHat;
    }

    public Vector3 offset(final Direction side)
    {
        return this.add(Vector3.getNewVector().set(side));
    }

    public Vector3 offsetBy(final Direction side)
    {
        return this.addTo(side.getStepX(), side.getStepY(), side.getStepZ());
    }

    public Vector3 reverse()
    {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
        return this;
    }

    /**
     * Rotates the given vector by the given amounts of pitch and yaw.
     *
     * @param vector
     * @param pitch
     * @param yaw
     * @return
     */
    public Vector3 rotateAboutAngles(final double pitch, final double yaw, final Vector3 temp, final Vector3 temp1)
    {
        if (this.isEmpty() || pitch == 0 && yaw == 0) return this;
        temp.set(this);
        if (yaw != 0) this.rotateAboutLine(Vector3.secondAxis, yaw, temp);
        if (pitch != 0) this.rotateAboutLine(this.horizonalPerp(), pitch, temp);

        if (temp.isNaN()) return temp.clear();

        return temp;
    }

    /**
     * Rotates the given vector around the given line by the given angle. This
     * internally normalizes the line incase it is not already normalized
     *
     * @param vectorH
     * @param line
     * @param angle
     * @return
     */
    public Vector3 rotateAboutLine(Vector3 line, final double angle, Vector3 ret)
    {
        if (line.magSq() != 1) line = line.normalize();

        if (ret == null) ret = Vector3.getNewVector();
        final double[][] mat = Vector3.rotBox;

        mat[0][0] = line.get(0) * line.get(0) * (1 - Mth.cos((float) angle)) + Mth.cos((float) angle);
        mat[0][1] = line.get(0) * line.get(1) * (1 - Mth.cos((float) angle)) - line.get(2) * Mth.sin((float) angle);
        mat[0][2] = line.get(0) * line.get(2) * (1 - Mth.cos((float) angle)) + line.get(1) * Mth.sin((float) angle);

        mat[1][0] = line.get(1) * line.get(0) * (1 - Mth.cos((float) angle)) + line.get(2) * Mth.sin((float) angle);
        mat[1][1] = line.get(1) * line.get(1) * (1 - Mth.cos((float) angle)) + Mth.cos((float) angle);
        mat[1][2] = line.get(1) * line.get(2) * (1 - Mth.cos((float) angle)) - line.get(0) * Mth.sin((float) angle);

        mat[2][0] = line.get(2) * line.get(0) * (1 - Mth.cos((float) angle)) - line.get(1) * Mth.sin((float) angle);
        mat[2][1] = line.get(2) * line.get(1) * (1 - Mth.cos((float) angle)) + line.get(0) * Mth.sin((float) angle);
        mat[2][2] = line.get(2) * line.get(2) * (1 - Mth.cos((float) angle)) + Mth.cos((float) angle);

        ret.x = mat[0][0] * this.x + mat[0][1] * this.y + mat[0][2] * this.z;
        ret.y = mat[1][0] * this.x + mat[1][1] * this.y + mat[1][2] * this.z;
        ret.z = mat[2][0] * this.x + mat[2][1] * this.y + mat[2][2] * this.z;

        return ret;
    }

    public boolean sameBlock(final Vector3 vec)
    {
        return this.intX() == vec.intX() && this.intY() == vec.intY() && this.intZ() == vec.intZ();
    }

    /**
     * Multiplies the vector by the constant.
     *
     * @param vector
     * @param constant
     * @return
     */
    public Vector3 scalarMult(final double constant)
    {
        final Vector3 newVector = Vector3.getNewVector();
        for (int i = 0; i < Vector3.length; i++) newVector.set(i, constant * this.get(i));
        return newVector;
    }

    public Vector3 scalarMultBy(final double i)
    {
        this.x = this.x * i;
        this.y = this.y * i;
        this.z = this.z * i;
        return this;
    }

    public Vector3 set(final Direction dir)
    {
        this.x = dir.getStepX();
        this.y = dir.getStepY();
        this.z = dir.getStepZ();
        return this;
    }

    public Vector3 set(final double x, final double y, final double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vector3 set(final double[] vec)
    {
        this.x = vec[0];
        this.y = vec[1];
        this.z = vec[2];
        return this;
    }

    public Vector3 set(final Entity e, final boolean b)
    {
        if (e != null && b)
        {
            this.x = e.getX();
            this.y = e.getY() + e.getBbHeight() / 2;
            this.z = e.getZ();
        }
        else if (e != null)
        {
            this.x = e.getX();
            this.y = e.getY() + e.getEyeHeight();
            this.z = e.getZ();
        }
        return this;
    }

    public void set(final int i, final double j)
    {
        if (i == 0) this.x = j;
        else if (i == 1) this.y = j;
        else if (i == 2) this.z = j;
    }

    public Vector3 set(final Object o)
    {
        if (o instanceof Entity)
        {
            final Entity e = (Entity) o;
            this.set(e.getX(), e.getY(), e.getZ());
        }
        else if (o instanceof BlockEntity)
        {
            final BlockEntity te = (BlockEntity) o;
            this.set(te.getBlockPos());
        }
        else if (o instanceof double[])
        {
            final double[] d = (double[]) o;
            this.set(d[0], d[1], d[2]);
        }
        else if (o instanceof Direction)
        {
            final Direction side = (Direction) o;
            this.set(side.getStepX(), side.getStepY(), side.getStepZ());
        }
        else if (o instanceof Vector3) this.set((Vector3) o);
        else if (o instanceof BlockPos)
        {
            final BlockPos c = (BlockPos) o;
            this.set(c.getX(), c.getY(), c.getZ());
        }
        else if (o instanceof GlobalPos)
        {
            final BlockPos c = ((GlobalPos) o).pos();
            this.set(c.getX(), c.getY(), c.getZ());
        }
        else if (o instanceof Node)
        {
            final Node p = (Node) o;
            this.set(p.x, p.y, p.z);
        }
        else if (o instanceof Vec3)
        {
            final Vec3 p = (Vec3) o;
            this.set(p.x, p.y, p.z);
        }
        else if (o instanceof int[])
        {
            final int[] p = (int[]) o;
            this.set(p[0], p[1], p[2]);
        }
        else if (o instanceof byte[])
        {
            final byte[] p = (byte[]) o;
            this.set(p[0], p[1], p[2]);
        }
        else if (o instanceof short[])
        {
            final short[] p = (short[]) o;
            this.set(p[0], p[1], p[2]);
        }
        else if (o instanceof float[])
        {
            final float[] p = (float[]) o;
            this.set(p[0], p[1], p[2]);
        }
        else if (o instanceof Double) this.x = this.y = this.z = (double) o;
        return this;
    }

    public Vector3 set(final Vector3 vec)
    {
        if (vec != null)
        {
            this.x = vec.x;
            this.y = vec.y;
            this.z = vec.z;
        }
        else
        {

        }
        return this;
    }

    public void setAir(final Level world)
    {
        // TODO maybe see if there is a way to find the default "air" for this
        // world
        world.setBlockAndUpdate(this.getPos(), Blocks.AIR.defaultBlockState());
    }

    public void setBiome(final Biome biome, final Level world)
    {
        ThutCore.LOGGER.error("Not supported yet!");
        // final int x = this.intX();
        // final int z = this.intZ();
        // final ChunkAccess chunk = world.getChunk(new BlockPos(x, 0, z));
        // final ChunkBiomeContainer biomes = chunk.getBiomes();
        // final int i = x & ChunkBiomeContainer.HORIZONTAL_MASK;
        // final int j = (int) Mth.clamp(this.y, 0,
        // ChunkBiomeContainer.VERTICAL_MASK);
        // final int k = z & ChunkBiomeContainer.HORIZONTAL_MASK;
        // final int index = j << ChunkBiomeContainer.WIDTH_BITS +
        // ChunkBiomeContainer.WIDTH_BITS
        // | k << ChunkBiomeContainer.WIDTH_BITS | i;
        // Arrays.fill(biomes.biomes, biome);
        // biomes.biomes[index] = biome;
    }

    public void setBlock(final Level world, final BlockState defaultState)
    {
        world.setBlockAndUpdate(this.getPos(), defaultState);
    }

    public Vector3 setToVelocity(final Entity e)
    {
        this.set(e.getDeltaMovement());
        return this;
    }

    public void setVelocities(final Entity e)
    {
        e.setDeltaMovement(this.x, this.y, this.z);
    }

    /**
     * Subtracts vectorB from vectorA
     *
     * @param vectorA
     * @param vectorB
     * @return
     */
    public Vector3 subtract(final Vector3 vectorB)
    {
        final Vector3 vectorC = Vector3.getNewVector();
        for (int i = 0; i < 3; i++) vectorC.set(i, this.get(i) - vectorB.get(i));
        return vectorC;
    }

    /**
     * Subtracts vectorB from vectorA
     *
     * @param vectorA
     * @param vectorB
     * @return
     */
    public Vector3 subtractFrom(final Vector3 b)
    {
        if (b == null) return this;
        this.x -= b.x;
        this.y -= b.y;
        this.z -= b.z;
        return this;
    }

    /**
     * Returns the unit vector in with the same direction as vector.
     *
     * @param vector
     * @return unit vector in direction of vector.
     */
    public Vector3 toSpherical()
    {
        final Vector3 vectorSpher = Vector3.getNewVector();
        vectorSpher.x = this.mag();
        vectorSpher.y = Math.acos(this.get(1) / vectorSpher.x) - Math.PI / 2;
        vectorSpher.z = Math.atan2(this.get(2), this.x);
        return vectorSpher;
    }

    @Override
    public String toString()
    {
        return "x:" + this.x + " y:" + this.y + " z:" + this.z;
    }

    public void writeToBuff(final ByteBuf data)
    {
        data.writeDouble(this.x);
        data.writeDouble(this.y);
        data.writeDouble(this.z);
    }

    public void writeToNBT(final CompoundTag nbt, final String tag)
    {
        nbt.putDouble(tag + "x", this.x);
        nbt.putDouble(tag + "y", this.y);
        nbt.putDouble(tag + "z", this.z);
    }
}