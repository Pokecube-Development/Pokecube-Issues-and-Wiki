package thut.api.entity.blockentity;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import thut.api.TickHandler;

public class BlockEntityUpdater
{
    public static boolean autoBlacklist = false;

    public static boolean isWhitelisted(final TileEntity tile)
    {
        final ResourceLocation id = TileEntityType.getId(tile.getType());
        return id == null ? true : !IBlockEntity.TEBLACKLIST.contains(id.toString());
    }

    final IBlockEntity  blockEntity;
    final Entity        theEntity;
    List<AxisAlignedBB> blockBoxes = Lists.newArrayList();
    Set<TileEntity>     erroredSet = Sets.newHashSet();
    VoxelShape          totalShape = VoxelShapes.empty();

    public BlockEntityUpdater(final IBlockEntity rocket)
    {
        this.blockEntity = rocket;
        this.theEntity = (Entity) rocket;
    }

    public VoxelShape buildShape()
    {
        final int sizeX = this.blockEntity.getBlocks().length;
        final int sizeY = this.blockEntity.getBlocks()[0].length;
        final int sizeZ = this.blockEntity.getBlocks()[0][0].length;
        final Entity mob = (Entity) this.blockEntity;
        this.totalShape = VoxelShapes.empty();
        final BlockPos.Mutable pos = new BlockPos.Mutable();
        final BlockPos min = this.blockEntity.getMin();
        final BlockPos origin = mob.getPosition();
        final IBlockReader world = this.blockEntity.getFakeWorld();

        final int xMin = MathHelper.floor(this.blockEntity.getMin().getX());
        final int xMax = MathHelper.floor(this.blockEntity.getMax().getX());
        final int zMin = MathHelper.floor(this.blockEntity.getMin().getZ());
        final int zMax = MathHelper.floor(this.blockEntity.getMax().getZ());

        final double dx = (xMax - xMin) / 2 + 0.5;
        final double dz = (zMax - zMin) / 2 + 0.5;

        for (int i = 0; i < sizeX; i++)
            for (int j = 0; j < sizeY; j++)
                for (int k = 0; k < sizeZ; k++)
                {
                    pos.setPos(i, j, k);
                    final BlockState state = this.blockEntity.getBlocks()[i][j][k];
                    pos.setPos(i + min.getX() + origin.getX(), j + min.getY() + origin.getY(), k + min.getZ() + origin
                            .getZ());
                    VoxelShape shape;
                    if (state == null || (shape = state.getShape(world, pos)) == null) continue;
                    if (shape.isEmpty()) continue;
                    shape = shape.withOffset(mob.getPosX() + i - dx, mob.getPosY() + j + min.getY(), mob.getPosZ() + k
                            - dz);
                    this.totalShape = VoxelShapes.combineAndSimplify(this.totalShape, shape, IBooleanFunction.OR);
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

        if (min_min && MathHelper.epsilonEquals(minB, minC)) min_min = false;
        if (max_max && MathHelper.epsilonEquals(maxB, maxC)) max_max = false;
        if (max_min && MathHelper.epsilonEquals(maxB, minC)) max_min = false;
        if (min_max && MathHelper.epsilonEquals(minB, maxC)) min_max = false;

        // Also no intersection here.
        if (!(min_min || max_max || max_min || min_max)) return 0;
        double intersectAmount = 0;

        if (min_min) intersectAmount = dmax_min;
        if (max_max) intersectAmount = dmin_max;

        if (max_min) intersectAmount = -dmax_max;
        if (min_max) intersectAmount = -dmin_min;

        return intersectAmount;
    }

    private static double getIntersect(final Axis axis, final AxisAlignedBB boxA, final AxisAlignedBB boxB,
            final AxisAlignedBB boxC)
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
    public static boolean intersectsOrAdjacent(final AxisAlignedBB boxA, final AxisAlignedBB boxB)
    {
        return boxA.minX <= boxB.maxX && boxA.maxX >= boxB.minX && boxA.minY <= boxB.maxY && boxA.maxY >= boxB.minY
                && boxA.minZ <= boxB.maxZ && boxA.maxZ >= boxB.minZ;
    }

    public void applyEntityCollision(final Entity entity)
    {
        // TODO instead of this, apply appropriate transformation to the
        // entity's box, and then collide off that, then apply appropriate
        // inverse transformation before actually applying collision to entity.
        if ((this.theEntity.rotationYaw + 360) % 90 > 5 || this.theEntity.isPassenger(entity)) return;

        boolean serverSide = entity.getEntityWorld().isRemote;
        final boolean isPlayer = entity instanceof PlayerEntity;
        if (isPlayer) serverSide = entity instanceof ServerPlayerEntity;

        double dx = 0, dz = 0, dy = 0;
        final Vec3d motion_a = this.theEntity.getMotion();
        Vec3d motion_b = entity.getMotion();
        final AxisAlignedBB boundingBox = entity.getBoundingBox();
        if (isPlayer && serverSide)
        {
            final ServerPlayerEntity player = (ServerPlayerEntity) entity;
            dx = player.chasingPosX - player.prevChasingPosX;
            dy = player.chasingPosY - player.prevChasingPosY;
            dz = player.chasingPosZ - player.prevChasingPosZ;
            motion_b = new Vec3d(dx, dy, dz).scale(0.5);
        }
        final Vec3d diffV = motion_a.subtract(motion_b);
        /** Expanded box by velocities to test for collision with. */
        final AxisAlignedBB testBox = boundingBox.expand(diffV.x, diffV.y, diffV.z);// .grow(0.1);

        this.blockBoxes.clear();
        // Used to select which boxes to consider for collision
        final AxisAlignedBB hitTest = testBox.grow(0.1 + diffV.length());
        this.buildShape().forEachBox((x0, y0, z0, x1, y1, z1) ->
        {
            final AxisAlignedBB box = new AxisAlignedBB(x0, y0, z0, x1, y1, z1);
            if (BlockEntityUpdater.intersectsOrAdjacent(box, hitTest)) this.blockBoxes.add(box);
        });

        boolean colX = false;
        boolean colY = false;
        boolean colZ = false;

        dx = 0;
        dy = 0;
        dz = 0;

        AxisAlignedBB toUse = testBox;
        final AxisAlignedBB orig = toUse;
        // System.out.println("_____________________________");

        for (final AxisAlignedBB aabb : this.blockBoxes)
        {
            double dx1 = 0, dy1 = 0, dz1 = 0;
            // Only use ones that actually intersect for this loop
            if (!BlockEntityUpdater.intersectsOrAdjacent(aabb, toUse)) continue;

            final AxisAlignedBB inter = toUse.intersect(aabb);

            // This is the floor of the box, so mark it as collided
            if (inter.getYSize() == 0 && inter.minY == aabb.maxY) colY = true;

            // This means we don't actually intersect as far as the below checks
            // are concerned
            if (inter.getXSize() == 0 || inter.getYSize() == 0 || inter.getZSize() == 0) continue;

            // System.out.println("X");
            dx1 = BlockEntityUpdater.getIntersect(Axis.X, inter, toUse, aabb);
            // System.out.println("Y");
            dy1 = BlockEntityUpdater.getIntersect(Axis.Y, inter, toUse, aabb);
            // System.out.println("Z");
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
                if (dy >= 0 && dy < entity.stepHeight)
                {
                    boolean valid = true;
                    // check if none of the other boxes disagree with the step
                    for (final AxisAlignedBB aabb2 : this.blockBoxes)
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

            toUse = toUse.offset(dx1, dy1, dz1);
        }

        dx = toUse.minX - orig.minX;
        dy = toUse.minY - orig.minY;
        dz = toUse.minZ - orig.minZ;

        final boolean collided = colX || colY || colZ;

        // If entity has collided, adjust motion accordingly.
        if (collided)
        {
            motion_b = entity.getMotion();
            if (colY)
            {
                final Vec3d motion = new Vec3d(0, dy, 0);
                entity.move(MoverType.SELF, motion);
                dy = motion_a.y;
            }
            else dy = motion_b.y;
            if (colX)
            {
                final Vec3d motion = new Vec3d(dx, 0, 0);
                entity.move(MoverType.SELF, motion);
                dx = motion_a.x;
            }
            else dx = 0.9 * motion_b.x;
            if (colZ)
            {
                final Vec3d motion = new Vec3d(0, 0, dz);
                entity.move(MoverType.SELF, motion);
                dz = motion_a.z;
            }
            else dz = 0.9 * motion_b.z;
            entity.setMotion(dx, dy, dz);

            if (colY)
            {
                entity.onGround = true;
                entity.onLivingFall(entity.fallDistance, 0);
                entity.fallDistance = 0;
            }
        }

        // Extra stuff to do with players, apply these regardless of collision.
        // This is done to prevent "flying on server" kicks when the craft is
        // moving down
        if (isPlayer && (collided || motion_a.y < 0))
        {
            final PlayerEntity player = (PlayerEntity) entity;

            if (serverSide)
            {
                final ServerPlayerEntity serverplayer = (ServerPlayerEntity) player;
                // Meed to set floatingTickCount to prevent being kicked
                serverplayer.connection.vehicleFloatingTickCount = 0;
                serverplayer.connection.floatingTickCount = 0;
            }

            if (!serverSide && (Minecraft.getInstance().gameSettings.viewBobbing || TickHandler.playerTickTracker
                    .containsKey(player.getUniqueID())))
            { // This fixes jitter, need a better way to handle this.
                TickHandler.playerTickTracker.put(player.getUniqueID(), (int) (System.currentTimeMillis() % 2000));
                Minecraft.getInstance().gameSettings.viewBobbing = false;
            }
            /** This is for clearing jump values on client. */
            if (!serverSide) player.getPersistentData().putInt("lastStandTick", player.ticksExisted);

        }
    }

    public void onSetPosition()
    {
        double xMin, yMin, zMin, xMax, yMax, zMax;
        xMin = this.theEntity.posX + this.blockEntity.getMin().getX() - 0.5;
        yMin = this.theEntity.posY + this.blockEntity.getMin().getY();
        zMin = this.theEntity.posZ + this.blockEntity.getMin().getZ() - 0.5;
        xMax = this.theEntity.posX + this.blockEntity.getMax().getX() + 0.5;
        yMax = this.theEntity.posY + this.blockEntity.getMax().getY() + 1;
        zMax = this.theEntity.posZ + this.blockEntity.getMax().getZ() + 0.5;
        this.theEntity.setBoundingBox(new AxisAlignedBB(xMin, yMin, zMin, xMax, yMax, zMax));
    }

    public void onUpdate()
    {
        if (this.blockEntity.getBlocks() == null) return;
        final double wMax = this.theEntity.getEntityWorld().getMaxEntityRadius();
        double uMax = -1;
        if (wMax < this.blockEntity.getBlocks().length) uMax = this.blockEntity.getBlocks().length;
        if (wMax < this.blockEntity.getBlocks()[0].length) uMax = this.blockEntity.getBlocks()[0].length;
        if (wMax < this.blockEntity.getBlocks()[0][0].length) uMax = this.blockEntity.getBlocks()[0][0].length;
        this.theEntity.getEntityWorld().increaseMaxEntityRadius(uMax);
        EntitySize size = this.theEntity.getSize(this.theEntity.getPose());
        size = EntitySize.fixed(1 + this.blockEntity.getMax().getX() - this.blockEntity.getMin().getX(),
                this.blockEntity.getMax().getY());
        this.blockEntity.setSize(size);
        double y;
        if (this.theEntity.getMotion().y == 0 && this.theEntity.getPosY() != (y = Math.round(this.theEntity.getPosY())))
            this.theEntity.setPosition(this.theEntity.getPosX(), y, this.theEntity.getPosZ());
        final BlockPos.Mutable pos = new BlockPos.Mutable();
        final int xMin = this.blockEntity.getMin().getX();
        final int zMin = this.blockEntity.getMin().getZ();
        final int yMin = this.blockEntity.getMin().getY();
        final int sizeX = this.blockEntity.getTiles().length;
        final int sizeY = this.blockEntity.getTiles()[0].length;
        final int sizeZ = this.blockEntity.getTiles()[0][0].length;

        final World world = this.blockEntity.getFakeWorld() instanceof World ? (World) this.blockEntity.getFakeWorld()
                : this.theEntity.getEntityWorld();

        for (int i = 0; i < sizeX; i++)
            for (int j = 0; j < sizeY; j++)
                for (int k = 0; k < sizeZ; k++)
                {
                    pos.setPos(i + xMin + this.theEntity.getPosX(), j + yMin + this.theEntity.getPosY(), k + zMin
                            + this.theEntity.getPosZ());

                    // TODO rotate here by entity rotation.
                    final TileEntity tile = this.blockEntity.getTiles()[i][j][k];
                    if (tile != null) tile.setWorldAndPos(world, pos.toImmutable());
                    if (tile instanceof ITickableTileEntity)
                    {
                        if (this.erroredSet.contains(tile) || !BlockEntityUpdater.isWhitelisted(tile)) continue;
                        try
                        {
                            ((ITickableTileEntity) tile).tick();
                        }
                        catch (final Throwable e)
                        {
                            e.printStackTrace();
                            System.err.println("Error with Tile Entity " + tile);
                            this.erroredSet.add(tile);
                            if (BlockEntityUpdater.autoBlacklist && TileEntityType.getId(tile.getType()) != null)
                                IBlockEntity.TEBLACKLIST.add(TileEntityType.getId(tile.getType()).toString());
                        }
                    }
                }
    }
}
