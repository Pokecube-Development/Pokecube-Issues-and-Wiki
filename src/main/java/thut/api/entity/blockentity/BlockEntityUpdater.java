package thut.api.entity.blockentity;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockEntityUpdater
{
    public static boolean autoBlacklist = false;

    public static boolean isWhitelisted(final BlockEntity tile)
    {
        final ResourceLocation id = BlockEntityType.getKey(tile.getType());
        return id == null ? true : !IBlockEntity.TEBLACKLIST.contains(id.toString());
    }

    final IBlockEntity blockEntity;
    final Entity theEntity;
    List<AABB> blockBoxes = Lists.newArrayList();
    Set<BlockEntity> erroredSet = Sets.newHashSet();
    VoxelShape totalShape = Shapes.empty();

    Vec3 lastShapePos = Vec3.ZERO;

    public BlockEntityUpdater(final IBlockEntity rocket)
    {
        this.blockEntity = rocket;
        this.theEntity = (Entity) rocket;

        final Vec3 here = this.theEntity.position();
        this.theEntity.setBoundingBox(this.getBoundingBox());
        this.theEntity.setPos(here.x, here.y, here.z);
//        final Vec3 shifted = this.theEntity.position();
    }

    public VoxelShape buildShape()
    {
        final int sizeX = this.blockEntity.getBlocks().length;
        final int sizeY = this.blockEntity.getBlocks()[0].length;
        final int sizeZ = this.blockEntity.getBlocks()[0][0].length;
        final Entity mob = (Entity) this.blockEntity;
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        final BlockPos min = this.blockEntity.getMin();
        final BlockPos origin = mob.blockPosition();
        final BlockGetter world = this.blockEntity.getFakeWorld();

        if (mob.position().distanceToSqr(this.lastShapePos) <= 1e-4) return this.totalShape;
        this.lastShapePos = mob.position();

        final int xMin = this.blockEntity.getMin().getX();
        final int zMin = this.blockEntity.getMin().getZ();

        this.totalShape = Shapes.empty();

        final double dx = xMin;
        final double dz = zMin;

        for (int i = 0; i < sizeX; i++) for (int j = 0; j < sizeY; j++) for (int k = 0; k < sizeZ; k++)
        {
            pos.set(i, j, k);
            final BlockState state = this.blockEntity.getBlocks()[i][j][k];
            pos.set(i + min.getX() + origin.getX(), j + min.getY() + origin.getY(), k + min.getZ() + origin.getZ());
            VoxelShape shape;
            if (state == null || (shape = state.getShape(world, pos)) == null) continue;
            if (shape.isEmpty()) continue;
            shape = shape.move(mob.getX() + i - dx, mob.getY() + j + min.getY(), mob.getZ() + k - dz);
            this.totalShape = Shapes.join(this.totalShape, shape, BooleanOp.OR);
        }
        return this.totalShape;
    }

    private static double getIntersect(final double minA, final double minB, final double minC, final double maxA,
            final double maxB, final double maxC)
    {
        // No actual intersect here.
        if (!(minC == minA || minC == maxA || maxC == minA || maxC == maxA)) return 0;

        double dmax_min, dmin_max, dmax_max, dmin_min;
        boolean max_max, max_min, min_max, min_min;

        dmax_max = maxA - maxB;
        dmax_min = maxA - minB;
        dmin_max = minA - maxB;
        dmin_min = minA - minB;

        max_max = maxA == maxB;
        max_min = maxA == minB;
        min_max = minA == maxB;
        min_min = minA == minB;

        if (min_min && Mth.equal(minB, minC)) min_min = false;
        if (max_max && Mth.equal(maxB, maxC)) max_max = false;
        if (max_min && Mth.equal(maxB, minC)) max_min = false;
        if (min_max && Mth.equal(minB, maxC)) min_max = false;

        // Also no intersection here.
        if (!(min_min || max_max || max_min || min_max)) return 0;
        double intersectAmount = 0;

        if (min_min) intersectAmount = dmax_min;
        if (max_max) intersectAmount = dmin_max;

        if (max_min) intersectAmount = -dmax_max;
        if (min_max) intersectAmount = -dmin_min;

        return intersectAmount;
    }

    private static double getIntersect(final Axis axis, final AABB boxA, final AABB boxB, final AABB boxC)
    {
        switch (axis)
        {
        case X:
            return BlockEntityUpdater.getIntersect(boxA.minX, boxB.minX, boxC.minX, boxA.maxX, boxB.maxX, boxC.maxX);
        case Y:
            return BlockEntityUpdater.getIntersect(boxA.minY, boxB.minY, boxC.minY, boxA.maxY, boxB.maxY, boxC.maxY);
        case Z:
            return BlockEntityUpdater.getIntersect(boxA.minZ, boxB.minZ, boxC.minZ, boxA.maxZ, boxB.maxZ, boxC.maxZ);
        default:
            break;
        }
        return 0;
    }

    /**
     * Checks if the bounding box intersects with another.
     */
    public static boolean intersectsOrAdjacent(final AABB boxA, final AABB boxB)
    {
        return boxA.minX <= boxB.maxX && boxA.maxX >= boxB.minX && boxA.minY <= boxB.maxY && boxA.maxY >= boxB.minY
                && boxA.minZ <= boxB.maxZ && boxA.maxZ >= boxB.minZ;
    }

    public static void fill(final List<AABB> boxes, final AABB textBox, final VoxelShape from)
    {
        boxes.clear();
        // Used to select which boxes to consider for collision
        from.forAllBoxes((x0, y0, z0, x1, y1, z1) -> {
            final AABB box = new AABB(x0, y0, z0, x1, y1, z1);
            if (BlockEntityUpdater.intersectsOrAdjacent(box, textBox)) boxes.add(box);
        });
    }

    public static boolean applyEntityCollision(final Entity entity, final AABB entityBox, final List<AABB> blockBoxes,
            final Vec3 ref_motion)
    {
        if (blockBoxes.isEmpty()) return false;

        double dx = 0, dz = 0, dy = 0;
        Vec3 motion_b = entity.getDeltaMovement();

        boolean serverSide = entity.getCommandSenderWorld().isClientSide;
        final boolean isPlayer = entity instanceof Player;
        if (isPlayer) serverSide = entity instanceof ServerPlayer;

        final Vec3 diffV = ref_motion.subtract(motion_b);
        final AABB boundingBox = entityBox;
        if (isPlayer && serverSide)
        {
            final ServerPlayer player = (ServerPlayer) entity;
            dx = player.xCloak - player.xCloakO;
            dy = player.yCloak - player.yCloakO;
            dz = player.zCloak - player.zCloakO;
            motion_b = new Vec3(dx, dy, dz).scale(0.5);
        }
        /** Expanded box by velocities to test for collision with. */
        final AABB testBox = boundingBox.expandTowards(diffV.x, diffV.y, diffV.z);// .grow(0.1);

        dx = 0;
        dy = 0;
        dz = 0;
        boolean colX = false;
        boolean colY = false;
        boolean colZ = false;

        AABB toUse = testBox;
        final AABB orig = toUse;

        for (final AABB aabb : blockBoxes)
        {
            double dx1 = 0, dy1 = 0, dz1 = 0;
            // Only use ones that actually intersect for this loop
            if (!BlockEntityUpdater.intersectsOrAdjacent(aabb, toUse)) continue;

            final AABB inter = toUse.intersect(aabb);

            // This is the floor of the box, so mark it as collided
            if (inter.getYsize() == 0 && inter.minY == aabb.maxY) colY = true;

            // This means we don't actually intersect as far as the below checks
            // are concerned
            if (inter.getXsize() == 0 || inter.getYsize() == 0 || inter.getZsize() == 0) continue;

            dx1 = BlockEntityUpdater.getIntersect(Axis.X, inter, toUse, aabb);
            dy1 = BlockEntityUpdater.getIntersect(Axis.Y, inter, toUse, aabb);
            dz1 = BlockEntityUpdater.getIntersect(Axis.Z, inter, toUse, aabb);

            // Take the minimum of x and z
            if (dx1 != 0 && dz1 != 0)
            {
                final boolean max = Math.abs(dx1) > Math.abs(dz1);
                if (max) dx1 = 0;
                else dz1 = 0;
            }
            // Take the minimum of y and x
            if (dy1 != 0 && dx1 != 0)
            {
                final boolean max = Math.abs(dx1) > Math.abs(dy1);
                if (max) dx1 = 0;
                else dy1 = 0;
            }
            // Take the minimum of y and z
            if (dy1 != 0 && dz1 != 0)
            {
                final boolean max = Math.abs(dz1) > Math.abs(dy1);
                if (max) dz1 = 0;
                else dy1 = 0;
            }

            // If no y movement, but x or z, see if we should step up instead.
            if (dy1 == 0 && !(dz1 == 0 && dx1 == 0))
            {
                dy = inter.maxY - toUse.minY;
                if (dy >= 0 && dy < entity.maxUpStep)
                {
                    boolean valid = true;
                    // check if none of the other boxes disagree with the step
                    for (final AABB aabb2 : blockBoxes)
                    {
                        if (aabb2 == aabb) continue;
                        if (aabb2.intersects(toUse))
                        {
                            valid = false;
                            break;
                        }
                    }
                    if (valid)
                    {
                        dx1 = 0;
                        dz1 = 0;
                        dy1 = dy;
                    }
                }
            }

            colX = colX || dx1 != 0;
            colY = colY || dy1 != 0;
            colZ = colZ || dz1 != 0;

            toUse = toUse.move(dx1, dy1, dz1);
        }

        dx = toUse.minX - orig.minX;
        dy = toUse.minY - orig.minY;
        dz = toUse.minZ - orig.minZ;

        final boolean collided = colX || colY || colZ;

        // If entity has collided, adjust motion accordingly.
        if (collided)
        {
            motion_b = entity.getDeltaMovement();
            if (colY)
            {
                final Vec3 motion = new Vec3(0, dy, 0);
                entity.move(MoverType.SELF, motion);
                dy = ref_motion.y;
            }
            else dy = motion_b.y;
            if (colX)
            {
                final Vec3 motion = new Vec3(dx, 0, 0);
                entity.move(MoverType.SELF, motion);
                dx = ref_motion.x;
            }
            else dx = 0.9 * motion_b.x;
            if (colZ)
            {
                final Vec3 motion = new Vec3(0, 0, dz);
                entity.move(MoverType.SELF, motion);
                dz = ref_motion.z;
            }
            else dz = 0.9 * motion_b.z;
            entity.setDeltaMovement(dx, dy, dz);

            if (colY)
            {
                entity.setOnGround(true);
                entity.causeFallDamage(entity.fallDistance, 0, DamageSource.GENERIC);
                entity.fallDistance = 0;
            }
        }
        return collided;
    }

    public void applyEntityCollision(final Entity entity)
    {
//        // TODO instead of this, apply appropriate transformation to the
//        // entity's box, and then collide off that, then apply appropriate
//        // inverse transformation before actually applying collision to entity.
//        if ((this.theEntity.yRot + 360) % 90 > 5 || this.theEntity.hasPassenger(entity)) return;
//
//        boolean serverSide = entity.getCommandSenderWorld().isClientSide;
//        final boolean isPlayer = entity instanceof Player;
//        if (isPlayer) serverSide = entity instanceof ServerPlayer;
//
//        double dx = 0, dz = 0, dy = 0;
//        final Vec3 motion_a = this.theEntity.getDeltaMovement();
//        Vec3 motion_b = entity.getDeltaMovement();
//        final AABB boundingBox = entity.getBoundingBox();
//        if (isPlayer && serverSide)
//        {
//            final ServerPlayer player = (ServerPlayer) entity;
//            dx = player.xCloak - player.xCloakO;
//            dy = player.yCloak - player.yCloakO;
//            dz = player.zCloak - player.zCloakO;
//            motion_b = new Vec3(dx, dy, dz).scale(0.5);
//        }
//        final Vec3 diffV = motion_a.subtract(motion_b);
//        /** Expanded box by velocities to test for collision with. */
//        final AABB testBox = boundingBox.expandTowards(diffV.x, diffV.y, diffV.z);// .grow(0.1);
//
//        // Used to select which boxes to consider for collision
//        final AABB hitTest = testBox.inflate(0.1 + diffV.length());
//        BlockEntityUpdater.fill(this.blockBoxes, hitTest, this.buildShape());
//        final boolean collided = BlockEntityUpdater.applyEntityCollision(entity, entity.getBoundingBox(),
//                this.blockBoxes, motion_a);
//
//        // Extra stuff to do with players, apply these regardless of collision.
//        // This is done to prevent "flying on server" kicks when the craft is
//        // moving down
//        if (isPlayer && (collided || motion_a.y < 0))
//        {
//            final Player player = (Player) entity;
//
//            if (serverSide)
//            {
//                final ServerPlayer serverplayer = (ServerPlayer) player;
//                // Meed to set floatingTickCount to prevent being kicked
//                serverplayer.connection.aboveGroundVehicleTickCount = 0;
//                serverplayer.connection.aboveGroundTickCount = 0;
//            }
//
//            if (!serverSide && (Minecraft.getInstance().options.bobView
//                    || TickHandler.playerTickTracker.containsKey(player.getUUID())))
//            { // This fixes jitter, need a better way to handle this.
//                TickHandler.playerTickTracker.put(player.getUUID(), (int) (System.currentTimeMillis() % 2000));
//                Minecraft.getInstance().options.bobView = false;
//            }
//            /** This is for clearing jump values on client. */
//            if (!serverSide) player.getPersistentData().putInt("lastStandTick", player.tickCount);
//
//        }
    }

    public AABB getBoundingBox()
    {
        double xMin, yMin, zMin, xMax, yMax, zMax;
        final BlockPos size = this.blockEntity.getSize();

        final double x0 = this.theEntity.getX();
        final double y0 = this.theEntity.getY();
        final double z0 = this.theEntity.getZ();

        xMin = x0;
        yMin = y0;
        zMin = z0;

        xMax = x0 + size.getX() + 1;
        yMax = y0 + size.getY() + 1;
        zMax = z0 + size.getZ() + 1;

        if (size.getY() <= 1) yMax += 1;

        return new AABB(xMin, yMin, zMin, xMax, yMax, zMax).inflate(0.0);
    }

    public void onUpdate()
    {
        if (this.blockEntity.getBlocks() == null) return;
        final BlockPos dims = this.blockEntity.getSize();
        final double uMax = Math.max(dims.getX(), Math.max(dims.getY(), dims.getZ()));
        this.theEntity.getCommandSenderWorld().increaseMaxEntityRadius(uMax);
        EntityDimensions size = this.theEntity.getDimensions(this.theEntity.getPose());
        if (size.width != dims.getX() + 1)
        {
            size = EntityDimensions.fixed(1 + dims.getX(), this.blockEntity.getMax().getY());
            this.blockEntity.setSize(size);
        }
        double y;
        if (this.theEntity.getDeltaMovement().y == 0
                && this.theEntity.getY() != (y = Math.round(this.theEntity.getY())))
            this.theEntity.setPos(this.theEntity.getX(), y, this.theEntity.getZ());
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        final int sizeX = dims.getX();
        final int sizeY = dims.getY();
        final int sizeZ = dims.getZ();

        final Level world = this.blockEntity.getFakeWorld() instanceof Level ? (Level) this.blockEntity.getFakeWorld()
                : this.theEntity.getCommandSenderWorld();

        for (int i = 0; i < sizeX; i++) for (int j = 0; j < sizeY; j++) for (int k = 0; k < sizeZ; k++)
        {
            pos.set(i + this.theEntity.getX(), j + this.theEntity.getY(), k + this.theEntity.getZ());

            // TODO rotate here by entity rotation.
            final BlockEntity tile = this.blockEntity.getTiles()[i][j][k];
            if (tile != null) tile.setLevel(world);
            if (tile instanceof TickingBlockEntity)
            {
                if (this.erroredSet.contains(tile) || !BlockEntityUpdater.isWhitelisted(tile)) continue;
                try
                {
                    ((TickingBlockEntity) tile).tick();
                }
                catch (final Throwable e)
                {
                    e.printStackTrace();
                    System.err.println("Error with Tile Entity " + tile);
                    this.erroredSet.add(tile);
                    if (BlockEntityUpdater.autoBlacklist && BlockEntityType.getKey(tile.getType()) != null)
                        IBlockEntity.TEBLACKLIST.add(BlockEntityType.getKey(tile.getType()).toString());
                }
            }
        }
    }
}
