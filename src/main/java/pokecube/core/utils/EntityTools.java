package pokecube.core.utils;

import java.util.List;
import java.util.function.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.entity.PartEntity;
import pokecube.core.interfaces.IPokemob;

public class EntityTools
{
    public static void copyEntityData(final LivingEntity to, final LivingEntity from)
    {
        final CompoundNBT tag = new CompoundNBT();
        from.writeAdditional(tag);
        to.readAdditional(tag);
    }

    public static void copyRotations(final Entity to, final Entity from)
    {
        to.rotationPitch = from.rotationPitch;
        to.ticksExisted = from.ticksExisted;
        to.rotationYaw = from.rotationYaw;
        to.setRotationYawHead(from.getRotationYawHead());
        to.prevRotationPitch = from.prevRotationPitch;
        to.prevRotationYaw = from.prevRotationYaw;
    }

    public static void copyPositions(final Entity to, final Entity from)
    {
        to.lastTickPosX = from.lastTickPosX;
        to.lastTickPosY = from.lastTickPosY;
        to.lastTickPosZ = from.lastTickPosZ;

        to.prevPosX = from.prevPosX;
        to.prevPosY = from.prevPosY;
        to.prevPosZ = from.prevPosZ;

        to.chunkCoordX = from.chunkCoordX;
        to.chunkCoordY = from.chunkCoordY;
        to.chunkCoordZ = from.chunkCoordZ;

        to.setPosition(from.getPosX(), from.getPosY(), from.getPosZ());
        to.setMotion(from.getMotion());
    }

    public static void copyEntityTransforms(final LivingEntity to, final LivingEntity from)
    {
        EntityTools.copyRotations(to, from);

        to.prevRotationYawHead = from.prevRotationYawHead;
        to.prevRenderYawOffset = from.prevRenderYawOffset;
        to.renderYawOffset = from.renderYawOffset;

        to.prevLimbSwingAmount = from.prevLimbSwingAmount;
        to.limbSwing = from.limbSwing;
        to.limbSwingAmount = from.limbSwingAmount;

        to.setOnGround(from.isOnGround());
    }

    public static void copyPokemobData(final IPokemob from, final IPokemob to)
    {
        to.read(from.write());
    }

    public static Entity getCoreEntity(final Entity in)
    {
        if (in instanceof PartEntity<?>) return ((PartEntity<?>) in).getParent();
        return in;
    }

    public static LivingEntity getCoreLiving(final Entity in)
    {
        if (in instanceof PartEntity<?>)
        {
            final Entity mob = ((PartEntity<?>) in).getParent();
            if (mob instanceof LivingEntity) return (LivingEntity) mob;
        }
        return in instanceof LivingEntity ? (LivingEntity) in : null;
    }

    /**
     * This returns all nearby mobs, but using a quick lookup, note that this
     * will miss large mobs which are centred on chunks at the edge of the given
     * range, but outside the chunk looked, it is meant to be used for cases
     * where quick lookup are needed, and precision is not needed.
     *
     * @param toFill
     * @param centre
     * @param range
     * @param valid
     */
    public static void getNearMobsFast(final List<Entity> toFill, final World world, final BlockPos centre,
            final int range, final Predicate<Entity> valid)
    {
        final int r = Math.max(range >> 4, 1);
        // Check surrounding chunks as well
        final int x = centre.getX() >> 4;
        final int y = centre.getY() >> 4;
        final int z = centre.getZ() >> 4;
        for (int i = -r; i <= r; i++)
            for (int j = -r; j <= r; j++)
            {
                // Chunk exists calls this anyway, then does a null check.
                final IChunk c = world.getChunk(x + i, z + j, ChunkStatus.FULL, false);
                if (!(c instanceof Chunk)) continue;
                final Chunk chunk = (Chunk) c;
                for (int k = -r; k <= r; k++)
                {
                    final ClassInheritanceMultiMap<Entity> mobs = chunk.getEntityLists()[y];
                    toFill.addAll(mobs);
                }
            }
        toFill.removeIf(valid.negate());
    }
}
