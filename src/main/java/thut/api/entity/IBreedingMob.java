package thut.api.entity;

import javax.annotation.Nullable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.player.Player;

/**
 * This interface is used for mobs which can breed with other mobs.
 *
 * @author Thutmose
 */
public interface IBreedingMob
{
    AgeableMob getEntity();

    default boolean canMate(final AgeableMob AnimalEntity)
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

    default void setReadyToMate(@Nullable final Player cause)
    {
    }

    @Nullable
    default ServerPlayer getCause()
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
