package thut.api.entity;

import javax.annotation.Nullable;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

/**
 * This interface is used for mobs which can breed with other mobs.
 *
 * @author Thutmose
 */
public interface IBreedingMob
{
    AgeableEntity getEntity();

    default boolean canMate(final AgeableEntity AnimalEntity)
    {
        return false;
    }

    /**
     * Will be called by the mother before she lays to know what baby to put in
     * the egg.
     *
     * @param male
     *            the male
     * @return the pokedex number of the child
     */
    default Object getChild(final IBreedingMob male)
    {
        return null;
    }

    /** @return the byte sexe */
    default byte getSexe()
    {
        return -1;
    }

    default void mateWith(final IBreedingMob male)
    {
    }

    /** resets the status of being in love */
    default void resetLoveStatus()
    {
    }

    default void setReadyToMate(@Nullable final PlayerEntity cause)
    {
    }

    @Nullable
    default ServerPlayerEntity getCause()
    {
        return null;
    }

    /**
     * @param sexe
     *            the byte sexe
     */
    default void setSexe(final byte sexe)
    {
    }

    default void tickBreedDelay(final int tickAmount)
    {

    }

    default boolean canBreed()
    {
        return false;
    }

    default boolean isBreeding()
    {
        return false;
    }
}
