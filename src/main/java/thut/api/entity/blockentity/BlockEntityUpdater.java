package thut.api.entity.blockentity;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;
import thut.api.TickHandler;
import thut.api.entity.blockentity.world.IBlockEntityWorld;
import thut.api.maths.Matrix3;
import thut.api.maths.vecmath.Vector3f;

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

    public BlockEntityUpdater(final IBlockEntity rocket)
    {
        this.blockEntity = rocket;
        this.theEntity = (Entity) rocket;
    }

    public void applyEntityCollision(final Entity entity)
    {
        // TODO instead of this, apply appropriate transformation to the
        // entity's box, and then collide off that, then apply appropriate
        // inverse transformation before actually applying collision to entity.
        if ((this.theEntity.rotationYaw + 360) % 90 > 5 || this.theEntity.isPassenger(entity)) return;

        this.blockBoxes.clear();
        final int sizeX = this.blockEntity.getBlocks().length;
        final int sizeY = this.blockEntity.getBlocks()[0].length;
        final int sizeZ = this.blockEntity.getBlocks()[0][0].length;
        final BlockPos.Mutable pos = new BlockPos.Mutable();
        final int xMin = this.blockEntity.getMin().getX();
        final int yMin = this.blockEntity.getMin().getY();
        final int zMin = this.blockEntity.getMin().getZ();
        final BlockPos origin = this.theEntity.getPosition();

        final double minX = entity.getBoundingBox().minX;
        final double minY = entity.getBoundingBox().minY;
        final double minZ = entity.getBoundingBox().minZ;
        final double maxX = entity.getBoundingBox().maxX;
        final double maxY = entity.getBoundingBox().maxY;
        final double maxZ = entity.getBoundingBox().maxZ;
        double dx, dz, dy, r;
        final Vec3d motion_a = this.theEntity.getMotion();
        final Vec3d motion_b = entity.getMotion();
        final Vector3f diffs = new Vector3f((float) (motion_a.x - motion_b.x), (float) (motion_a.y - motion_b.y),
                (float) (motion_a.z - motion_b.z));
        final IBlockEntityWorld fakeworld = this.blockEntity.getFakeWorld();
        final AxisAlignedBB boundingBox = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
        /** Expanded box by velocities to test for collision with. */
        final AxisAlignedBB testBox = boundingBox.expand(-diffs.x, -diffs.y, -diffs.z);

        // testBox = testBox.grow(0.5);
        for (int i = 0; i < sizeX; i++)
            for (int j = 0; j < sizeY; j++)
                for (int k = 0; k < sizeZ; k++)
                {
                    pos.setPos(i + xMin + origin.getX(), j + yMin + origin.getY(), k + zMin + origin.getZ());
                    final BlockState state = fakeworld.getBlockState(pos);
                    // TODO see if we needed any other state here?
                    final VoxelShape shape = state.getCollisionShape(fakeworld, pos);
                    final List<AxisAlignedBB> toAdd = shape.toBoundingBoxList();
                    for (final AxisAlignedBB blockBox : toAdd)
                        if (blockBox != null)
                        {
                            final float dx2 = (float) (this.theEntity.posX - origin.getX()) - 0.5f;
                            final float dy2 = (float) (this.theEntity.posY - origin.getY());
                            final float dz2 = (float) (this.theEntity.posZ - origin.getZ()) - 0.5f;
                            final AxisAlignedBB box = blockBox.offset(dx2 + pos.getX(), dy2 + pos.getY(), dz2 + pos
                                    .getZ());
                            if (box.intersects(testBox)) this.blockBoxes.add(box);
                        }
                }

        // No boxes, no need to process further.
        if (this.blockBoxes.isEmpty()) return;

        pos.setPos(this.theEntity.getPosition());
        final Vector3f temp1 = new Vector3f();

        final boolean merge = true;

        // Here we merge the boxes into less boxes, by taking any boxes with
        // shared faces and merging them.
        if (merge) Matrix3.mergeAABBs(this.blockBoxes, 0, 0, 0);

        /** Positions adjusted for velocity. */
        final double lastTickMinY = minY + diffs.y;
        final double nextTickMinY = minY - diffs.y;

        final double lastTickMaxY = maxY + diffs.y;
        final double nextTickMaxY = maxY - diffs.y;

        final double lastTickMinX = minX + diffs.x;
        final double nextTickMinX = minX - diffs.x;

        final double lastTickMaxX = maxX + diffs.x;
        final double nextTickMaxX = maxX - diffs.x;

        final double lastTickMinZ = minZ + diffs.z;
        final double nextTickMinZ = minZ - diffs.z;

        final double lastTickMaxZ = maxZ + diffs.z;
        final double nextTickMaxZ = maxZ - diffs.z;

        boolean colX = false;
        boolean colY = false;
        boolean colZ = false;
        float stepY = 0;
        // for each box, compute collision.
        for (final AxisAlignedBB aabb : this.blockBoxes)
        {
            dx = 10e3;
            dz = 10e3;
            dy = 10e3;

            final boolean fromAbove = lastTickMinY >= aabb.maxY && nextTickMinY <= aabb.maxY;
            final boolean fromBelow = nextTickMaxY >= aabb.minY && lastTickMaxY <= aabb.minY;
            final boolean yPos = minY <= aabb.maxY && minY >= aabb.minY;
            final boolean yNeg = maxY <= aabb.maxY && maxY >= aabb.minY;

            final boolean fromXPos = lastTickMinX >= aabb.maxX && nextTickMinX <= aabb.maxX;
            final boolean fromXNeg = nextTickMaxX >= aabb.minX && lastTickMaxX <= aabb.minX;
            final boolean xPos = minX <= aabb.maxX && minX >= aabb.minX;
            final boolean xNeg = maxX <= aabb.maxX && maxX >= aabb.minX;

            final boolean fromZPos = lastTickMinZ >= aabb.maxZ && nextTickMinZ <= aabb.maxZ;
            final boolean fromZNeg = nextTickMaxZ >= aabb.minZ && lastTickMaxZ <= aabb.minZ;
            final boolean zPos = minZ <= aabb.maxZ && minZ >= aabb.minZ;
            final boolean zNeg = maxZ <= aabb.maxZ && maxZ >= aabb.minZ;

            final boolean collidesXPos = xPos && zPos && zNeg;
            final boolean collidesXNeg = xNeg && zPos && zNeg;

            final boolean collidesZPos = zPos && xPos && xNeg;
            final boolean collidesZNeg = zNeg && xPos && xNeg;

            final boolean collidesYNeg = yNeg && (xPos || xNeg || zPos || zNeg);
            boolean collidesYPos = yPos && (xPos || xNeg || zPos || zNeg);

            boolean collided = false;
            /** Collides with top of box, is standing on it. */
            if (!collided && fromAbove)
            {
                temp1.y = (float) Math.max(aabb.maxY - diffs.y - nextTickMinY, temp1.y);
                collided = true;
                colY = true;
            }
            /** Collides with bottom of box, is under it. */
            if (!collided && fromBelow)
            {
                temp1.y = (float) Math.min(aabb.minY - diffs.y - nextTickMaxY, temp1.y);
                collided = true;
                colY = true;
            }

            /** Collides with middle of +x face. */
            if (!collided && (fromXPos || collidesXPos))
            {
                r = Math.max(aabb.maxX - boundingBox.minX, temp1.x);
                dx = Math.min(dx, r);
                collided = true;
                colX = true;
            }
            /** Collides with middle of -x face. */
            if (!collided && (fromXNeg || collidesXNeg))
            {
                r = Math.min(aabb.minX - boundingBox.maxX, temp1.x);
                dx = Math.min(dx, r);
                collided = true;
                colX = true;
            }
            /** Collides with middle of +z face. */
            if (!collided && (fromZPos || collidesZPos))
            {
                r = Math.max(aabb.maxZ - boundingBox.minZ, temp1.z);
                dz = Math.min(dz, r);
                collided = true;
                colZ = true;
            }
            /** Collides with middle of -z face. */
            if (!collided && (fromZNeg || collidesZNeg))
            {
                r = Math.min(aabb.minZ - boundingBox.maxZ, temp1.z);
                dz = Math.min(dz, r);
                collided = true;
                colZ = true;
            }
            /** Collides with +x, +z corner. */
            if (!collided && xPos && zPos)
            {
                r = Math.max(aabb.maxZ - boundingBox.minZ, temp1.z);
                dz = Math.min(dz, r);
                r = Math.max(aabb.maxX - boundingBox.minX, temp1.x);
                dx = Math.min(dx, r);
                collided = true;
                colZ = true;
                colX = true;
            }
            /** Collides with +x, -z corner. */
            if (!collided && xPos && zNeg)
            {
                r = Math.min(aabb.minZ - boundingBox.maxZ, temp1.z);
                dz = Math.min(dz, r);
                r = Math.max(aabb.maxX - boundingBox.minX, temp1.x);
                dx = Math.min(dx, r);
                collided = true;
                colZ = true;
                colX = true;
            }
            /** Collides with -x, -z corner. */
            if (!collided && xNeg && zNeg)
            {
                r = Math.min(aabb.minZ - boundingBox.maxZ, temp1.z);
                dz = Math.min(dz, r);
                r = Math.min(aabb.minX - boundingBox.maxX, temp1.x);
                dx = Math.min(dx, r);
                collided = true;
                colZ = true;
                colX = true;
            }
            /** Collides with -x, +z corner. */
            if (!collided && xNeg && zPos)
            {
                r = Math.max(aabb.maxZ - boundingBox.minZ, temp1.z);
                dz = Math.min(dz, r);
                r = Math.min(aabb.minX - boundingBox.maxX, temp1.x);
                dx = Math.min(dx, r);
                collided = true;
                colZ = true;
                colX = true;
            }

            if ((colX || colZ) && motion_a.y == 0)
            {
                final float boxTopDist = (float) Math.max(aabb.maxY - diffs.y - nextTickMinY, temp1.y);
                if (boxTopDist <= entity.stepHeight)
                {
                    collidesYPos = true;
                    stepY = boxTopDist;
                }
            }

            if (collidesYNeg)
            {
                r = (float) Math.min(aabb.minY - diffs.y - nextTickMaxY, temp1.y);
                dy = Math.min(r, dy);
                colY = true;
            }
            else if (collidesYPos)
            {
                r = (float) Math.max(aabb.maxY - diffs.y - nextTickMinY, temp1.y);
                dy = Math.min(r, dy);
                colY = true;
            }

            final double dy1 = Math.abs(dy);
            final double dz1 = Math.abs(dz);
            final double dx1 = Math.abs(dx);

            /** y minimum penetration. */
            if (dy1 < dx1 && dy1 < dz1) temp1.y = (float) dy;
            else if (dx1 < dy1 && dx1 < dz1) temp1.x = (float) dx;
            else if (dz < 10e2) temp1.z = (float) dz;
        }

        temp1.y += stepY;

        // Extra stuff to do with players.
        if (entity instanceof PlayerEntity)
        {
            final PlayerEntity player = (PlayerEntity) entity;

            if (player.getEntityWorld().isRemote) // This fixes jitter, need
                // a
                // better way to handle
                // this.
                if (Minecraft.getInstance().gameSettings.viewBobbing || TickHandler.playerTickTracker.containsKey(player
                        .getUniqueID()))
                {
                    TickHandler.playerTickTracker.put(player.getUniqueID(), (int) (System.currentTimeMillis() % 2000));
                    Minecraft.getInstance().gameSettings.viewBobbing = false;
                }
            /** This is for clearing jump values on client. */
            if (player.getEntityWorld().isRemote) player.getPersistentData().putInt("lastStandTick",
                    player.ticksExisted);
            if (!player.abilities.isFlying)
            {
                entity.onGround = true;
                entity.onLivingFall(entity.fallDistance, 0);
                entity.fallDistance = 0;
            }
            // Meed to set floatingTickCount to prevent being kicked for
            // flying.
            if (!player.abilities.isCreativeMode && !player.getEntityWorld().isRemote)
            {
                final ServerPlayerEntity serverplayer = (ServerPlayerEntity) player;
                serverplayer.connection.floatingTickCount = 0;
            }
        }

        // If entity has collided, adjust motion accordingly.
        if (colX || colY || colZ)
        {

            if (temp1.y >= 0)
            {
                entity.onGround = true;
                entity.onLivingFall(entity.fallDistance, 0);
                entity.fallDistance = 0;
            }
            if (temp1.length() > 0.001)
            {
                final Vec3d motion = new Vec3d(temp1.x, temp1.y, temp1.z);
                entity.move(MoverType.SELF, motion);
            }
            dx = motion_b.x;
            dy = motion_b.y;
            dz = motion_b.z;

            if (colX) dx = motion_a.x;
            if (colY) dy = motion_a.y;
            if (colZ) dz = motion_a.z;

            dx *= 0.9;
            dz *= 0.9;

            entity.setMotion(dx, dy, dz);

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
        if (this.theEntity.getMotion().y == 0) this.theEntity.setPosition(this.theEntity.posX, Math.round(
                this.theEntity.posY), this.theEntity.posZ);
        final BlockPos.Mutable pos = new BlockPos.Mutable();
        final int xMin = this.blockEntity.getMin().getX();
        final int zMin = this.blockEntity.getMin().getZ();
        final int yMin = this.blockEntity.getMin().getY();
        final int sizeX = this.blockEntity.getTiles().length;
        final int sizeY = this.blockEntity.getTiles()[0].length;
        final int sizeZ = this.blockEntity.getTiles()[0][0].length;
        for (int i = 0; i < sizeX; i++)
            for (int j = 0; j < sizeY; j++)
                for (int k = 0; k < sizeZ; k++)
                {
                    pos.setPos(i + xMin + this.theEntity.posX, j + yMin + this.theEntity.posY, k + zMin
                            + this.theEntity.posZ);

                    // TODO rotate here by entity rotation.
                    final TileEntity tile = this.blockEntity.getTiles()[i][j][k];
                    if (tile != null) tile.setWorldAndPos((World) this.blockEntity.getFakeWorld(), pos.toImmutable());
                    if (tile instanceof ITickable)
                    {
                        if (this.erroredSet.contains(tile) || !BlockEntityUpdater.isWhitelisted(tile)) continue;
                        try
                        {
                            ((ITickable) tile).tick();
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
