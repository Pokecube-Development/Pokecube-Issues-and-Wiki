package pokecube.core.utils;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.entity.PartEntity;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import thut.api.entity.ICopyMob;
import thut.api.item.ItemList;

public class EntityTools
{
    private static Set<Capability<?>> CACHED_CAPS = new ObjectLinkedOpenHashSet<>();

    public static boolean isCached(Capability<?> cap)
    {
        return CACHED_CAPS.contains(cap);
    }

    public static void registerCachedCap(Capability<?> cap)
    {
        CACHED_CAPS.add(cap);
    }

    public static boolean is(ResourceLocation key, @Nullable LivingEntity entity, @Nullable IPokemob mob)
    {
        return ItemList.is(key, entity);
    }

    public static boolean is(ResourceLocation key, @Nullable IPokemob mob)
    {
        return is(key, mob != null ? mob.getEntity() : null, mob);
    }

    public static boolean is(ResourceLocation key, @Nullable LivingEntity mob)
    {
        return is(key, mob, PokemobCaps.getPokemobFor(mob));
    }

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
        if (in instanceof PartEntity<?> part) return part.getParent();
        return in;
    }

    public static LivingEntity getCoreLiving(final Entity in)
    {
        if (in instanceof PartEntity<?> part)
        {
            final Entity mob = part.getParent();
            if (mob instanceof LivingEntity living) return living;
        }
        return in instanceof LivingEntity living ? living : null;
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
