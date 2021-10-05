package pokecube.core.utils;

import java.util.List;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import pokecube.core.interfaces.IPokemob;
import thut.api.entity.ICopyMob;

public class EntityTools
{
    public static void copyEntityData(final LivingEntity to, final LivingEntity from)
    {
        final CompoundTag tag = new CompoundTag();
        from.addAdditionalSaveData(tag);
        to.readAdditionalSaveData(tag);
    }

    public static void copyRotations(final Entity to, final Entity from)
    {
        ICopyMob.copyRotations(to, from);
    }

    public static void copyPositions(final Entity to, final Entity from)
    {
        ICopyMob.copyPositions(to, from);
    }

    public static void copyEntityTransforms(final LivingEntity to, final LivingEntity from)
    {
        ICopyMob.copyEntityTransforms(to, from);
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
    public static void getNearMobsFast(final List<Entity> toFill, final Level world, final BlockPos centre,
            final int range, final Predicate<Entity> valid)
    {
        final AABB box = AABB.ofSize(new Vec3(centre.getX(), centre.getY(), centre.getZ()), range, range, range);
        toFill.addAll(world.getEntities((Entity) null, box, valid));
    }
}
