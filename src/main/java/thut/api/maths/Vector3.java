package thut.api.maths;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import com.mojang.authlib.GameProfile;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction8;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.QuartPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
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
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.lighting.LevelLightEngine;
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
import thut.lib.RegHelper;

/** @author Thutmose */
public class Vector3
{
    public static final Vector3 secondAxis = new Vector3().set(0, 1, 0);
    public static final Vector3 secondAxisNeg = new Vector3().set(0, -1, 0);
    public static final Vector3 firstAxis = new Vector3().set(1, 0, 0);
    public static final Vector3 firstAxisNeg = new Vector3().set(-1, 0, 0);
    public static final Vector3 thirdAxis = new Vector3().set(0, 0, 1);
    public static final Vector3 thirdAxisNeg = new Vector3().set(0, 0, -1);

    public static final Vector3 empty = new Vector3();
    public static final int length = 3;
    public static Vector3 vecMult = new Vector3();
    public static double[][] rotBox = new double[3][3];

    static Vector3 move1 = new Vector3();

    static Vector3 move2 = new Vector3();

    private static FakePlayer USEDFORRAYTRACECONTEXT = null;

    private static final UUID PLAYERID = new UUID(1234567, 7324156);
    private static final GameProfile FAKEPLAYER = new GameProfile(Vector3.PLAYERID, "raytrace-context");

    public static Vector3 entity(final Entity e)
    {
        if (e != null) return new Vector3().set(e.getX(), e.getY() + e.getEyeHeight(), e.getZ());
        return null;
    }

    public static Vector3 findMidPoint(final List<Vector3> points)
    {
        final Vector3 mid = new Vector3();
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
        final Vector3 test = new Vector3();
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

                if (!clear) return new Vector3().set(Vector3.Int(xtest), Vector3.Int(ytest), Vector3.Int(ztest));
            }

            yprev = ytest;
            xprev = xtest;
            zprev = ztest;
        }
        return null;
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
            if (!check) return new Vector3().set(xtest, ytest, ztest);
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

        if (world instanceof ServerLevel level)
        {
            final Vec3 start = source.toVec3d();
            final Vec3 end = direction.scalarMultBy(range).addTo(source).toVec3d();
            if (Vector3.USEDFORRAYTRACECONTEXT == null)
                Vector3.USEDFORRAYTRACECONTEXT = FakePlayerFactory.get(level, Vector3.FAKEPLAYER);
            else Vector3.USEDFORRAYTRACECONTEXT.setServerLevel(level);
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
        final Vector3 ret = new Vector3();
        ret.x = dat.readDouble();
        ret.y = dat.readDouble();
        ret.z = dat.readDouble();
        return ret;
    }

    public static Vector3 readFromNBT(final CompoundTag nbt, final String tag)
    {
        if (!nbt.contains(tag + "x")) return null;

        final Vector3 ret = new Vector3();
        ret.x = nbt.getDouble(tag + "x");
        ret.y = nbt.getDouble(tag + "y");
        ret.z = nbt.getDouble(tag + "z");
        return ret;
    }

    public double x;
    public double y;
    public double z;

    MutableBlockPos pos;

    public Vector3()
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
    public Vector3(final double pitch, final double yaw)
    {
        this.x = 1;
        this.y = Math.toRadians(pitch);
        this.z = Math.toRadians(yaw);
    }

    public Vector3(final double x, final double y, final double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3(final Entity e, final boolean bool)
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
    public Vector3(final Object a)
    {
        this();
        this.set(a);
    }

    public Vector3(final Object a, final Object b)
    {
        this();
        final Vector3 A = new Vector3().set(a);
        final Vector3 B = new Vector3().set(b);
        this.set(B.subtract(A));
    }

    public Vector3(final Vec3 vec)
    {
        this.x = vec.x;
        this.y = vec.y;
        this.z = vec.z;
    }

    public Vector3 add(final double i, final double j, final double k)
    {
        return new Vector3().set(this.x + i, j + this.y, k + this.z);
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
        final Vector3 vectorC = new Vector3();
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
        final Vector3 newVector = new Vector3().set(this.x, this.y, this.z);
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
        if (!(vec instanceof Vector3 v)) return false;
        return v.x == this.x && v.y == this.y && v.z == this.z;
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
            final AABB axisalignedbb = part.getBoundingBox().inflate(0.01F);
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
            final AABB axisalignedbb = entity1.getBoundingBox().inflate(0.01F);
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

    public double get(final int i)
    {
        assert i < 3;
        return i == 0 ? this.x : i == 1 ? this.y : this.z;
    }

    public AABB getAABB()
    {
        return new AABB(this.x, this.y, this.z, this.x, this.y, this.z);
    }

    public Holder<Biome> getBiomeHolder(final LevelAccessor world)
    {
        return world.getBiome(this.getPos());
    }

    public Biome getBiome(final LevelAccessor world)
    {
        return this.getBiomeHolder(world).value();
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
        else this.pos.set(this.intX(), this.intY(), this.intZ());
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
        return new Vector3().set(this.intX(), y, this.intZ());
    }

    public int getTopBlockY(final BlockGetter world)
    {
        int ret = world.getMaxBuildHeight();
        {
            for (ret = world.getMaxBuildHeight(); ret > world.getMinBuildHeight(); ret--)
            {
                final BlockState state = world.getBlockState(new BlockPos(this.intX(), ret, this.intZ()));
                if (state == null) continue;
                if (state.isSolid()) return ret;
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
        final Vector3 vectorH = new Vector3().set(-this.z, 0, this.x);
        return vectorH.norm();
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
        if (world instanceof Level)
        {
            final BlockState state = world.getBlockState(this.getPos());
            return state.getBlock() == null || state.isAir();
        }
        return false;
    }

    public boolean isClearOfBlocks(final BlockGetter world)
    {
        boolean ret = false;
        final BlockState state = world.getBlockState(this.getPos());
        if (state == null) return true;

        ret = this.isAir(world);
        if (!ret) ret = ret || state.liquid();
        if (!ret) ret = ret || state.canBeReplaced();
        if (!ret) ret = ret || !state.blocksMotion();
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

    public boolean isNaN()
    {
        return Double.isNaN(this.x) || Double.isNaN(this.z) || Double.isNaN(this.y);
    }

    public boolean isVisible(final BlockGetter world, final Vector3 location)
    {
        final Vector3 direction = location.subtract(this);
        final double range = direction.mag();
        return Vector3.isVisibleRange(world, this, direction, range);
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
        final Vector3 vHat = new Vector3();
        if (vmag == 0) return vHat.clear();
        vHat.set(this).scalarMultBy(1 / vmag);
        return vHat;
    }

    public Vector3 offset(final Direction side)
    {
        return this.add(new Vector3().set(side));
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

        if (ret == null) ret = new Vector3();
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
        final Vector3 newVector = new Vector3();
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
        if (o instanceof Entity e)
        {
            this.set(e.getX(), e.getY(), e.getZ());
        }
        else if (o instanceof BlockEntity te)
        {
            this.set(te.getBlockPos());
        }
        else if (o instanceof double[] d)
        {
            this.set(d[0], d[1], d[2]);
        }
        else if (o instanceof Direction side)
        {
            this.set(side);
        }
        else if (o instanceof Direction8 dir)
        {
            this.clear();
            for (Direction side : dir.getDirections()) this.addTo(side.getStepX(), side.getStepY(), side.getStepZ());
        }
        else if (o instanceof Vector3 v) this.set(v);
        else if (o instanceof BlockPos c)
        {
            this.set(c.getX(), c.getY(), c.getZ());
        }
        else if (o instanceof GlobalPos g)
        {
            final BlockPos c = g.pos();
            this.set(c.getX(), c.getY(), c.getZ());
        }
        else if (o instanceof Node p)
        {
            this.set(p.x, p.y, p.z);
        }
        else if (o instanceof Vec3 p)
        {
            this.set(p.x, p.y, p.z);
        }
        else if (o instanceof int[] p)
        {
            this.set(p[0], p[1], p[2]);
        }
        else if (o instanceof byte[] p)
        {
            this.set(p[0], p[1], p[2]);
        }
        else if (o instanceof short[] p)
        {
            this.set(p[0], p[1], p[2]);
        }
        else if (o instanceof float[] p)
        {
            this.set(p[0], p[1], p[2]);
        }
        else if (o instanceof Double d) this.x = this.y = this.z = d;
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
        if (!(world instanceof ServerLevel level))
        {
            ThutCore.LOGGER.error("Called on wrong side, this is server only!");
            return;
        }

        final int x = this.intX();
        final int y = this.intY();
        final int z = this.intZ();

        int qx = QuartPos.fromBlock(x);
        int qy = QuartPos.fromBlock(y);
        int qz = QuartPos.fromBlock(z);

        final ChunkAccess chunk = level.getChunk(this.getPos());

        int i = QuartPos.fromBlock(chunk.getMinBuildHeight());
        int k = i + QuartPos.fromBlock(chunk.getHeight()) - 1;
        int l = Mth.clamp(qy, i, k);
        int j = chunk.getSectionIndex(QuartPos.toBlock(l));

        LevelChunkSection section = chunk.getSections()[j];
        PalettedContainer<Holder<Biome>> biomes = (PalettedContainer<Holder<Biome>>) section.getBiomes();

        Biome old = biomes.get(qx & 3, l & 3, qz & 3).value();
        // No need to run this if we are already the same biome...
        if (old == biome) return;

        ResourceKey<Biome> key = ResourceKey.create(RegHelper.BIOME_REGISTRY, RegHelper.getKey(biome));
        HolderOwner<Biome> registry = level.registryAccess().registryOrThrow(RegHelper.BIOME_REGISTRY).holderOwner();
        Reference<Biome> holder = Holder.Reference.createStandAlone(registry, key);
        holder.bindKey(key);
        holder.bindValue(biome);

        biomes.set(qx & 3, l & 3, qz & 3, holder);

        if (chunk instanceof LevelChunk lchunk)
        {
            ChunkMap map = level.getChunkSource().chunkMap;
            LevelLightEngine lights = level.getLightEngine();
            ClientboundLevelChunkWithLightPacket packet = new ClientboundLevelChunkWithLightPacket(lchunk, lights, null,
                    null);

            // Send the packet to tracking things, this is taken from
            // PacketDistributor.TRACKING_CHUNK
            map.getPlayers(chunk.getPos(), false).forEach(e -> e.connection.send(packet));
        }
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
        final Vector3 vectorC = new Vector3();
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
        final Vector3 vectorSpher = new Vector3();
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