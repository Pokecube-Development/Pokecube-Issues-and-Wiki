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
        from.addAdditionalSaveData(tag);
        to.readAdditionalSaveData(tag);
    }

    public static void copyRotations(final Entity to, final Entity from)
    {
        to.xRot = from.xRot;
        to.tickCount = from.tickCount;
        to.yRot = from.yRot;
        to.setYHeadRot(from.getYHeadRot());
        to.xRotO = from.xRotO;
        to.yRotO = from.yRotO;
    }

    public static void copyPositions(final Entity to, final Entity from)
    {
        to.xOld = from.xOld;
        to.yOld = from.yOld;
        to.zOld = from.zOld;

        to.xo = from.xo;
        to.yo = from.yo;
        to.zo = from.zo;

        to.xChunk = from.xChunk;
        to.yChunk = from.yChunk;
        to.zChunk = from.zChunk;

        to.setPos(from.getX(), from.getY(), from.getZ());
        to.setDeltaMovement(from.getDeltaMovement());
    }

    public static void copyEntityTransforms(final LivingEntity to, final LivingEntity from)
    {
        EntityTools.copyRotations(to, from);

        to.yHeadRotO = from.yHeadRotO;
        to.yBodyRotO = from.yBodyRotO;
        to.yBodyRot = from.yBodyRot;

        to.animationSpeedOld = from.animationSpeedOld;
        to.animationPosition = from.animationPosition;
        to.animationSpeed = from.animationSpeed;

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
                    final ClassInheritanceMultiMap<Entity> mobs = chunk.getEntitySections()[y];
                    toFill.addAll(mobs);
                }
            }
        toFill.removeIf(valid.negate());
    }
}
