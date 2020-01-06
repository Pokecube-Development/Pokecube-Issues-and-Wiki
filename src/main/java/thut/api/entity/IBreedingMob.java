package thut.api.entity;

import java.util.Vector;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;

/**
 * This interface is used for mobs which can breed with other mobs.
 *
 * @author Thutmose
 */
public interface IBreedingMob
{
    boolean canMate(AnimalEntity AnimalEntity);

    /**
     * Will be called by the mother before she lays to know what baby to put in
     * the egg.
     *
     * @param male
     *            the male
     * @return the pokedex number of the child
     */
    Object getChild(IBreedingMob male);

    /**
     * Which entity is this pokemob trying to breed with
     *
     * @return
     */
    Entity getLover();

    /** @return the timer indcating delay between looking for a mate. */
    int getLoveTimer();

    Vector<IBreedingMob> getMalesForBreeding();

    /** @return the byte sexe */
    byte getSexe();

    void mateWith(IBreedingMob male);

    /** resets the status of being in love */
    void resetLoveStatus();

    /**
     * Sets the entity to try to breed with
     *
     * @param lover
     */
    void setLover(Entity lover);

    /**
     * Sets the timer for the delay between looking for a mate.
     *
     * @param value
     */
    void setLoveTimer(int value);

    /**
     * @param sexe
     *            the byte sexe
     */
    void setSexe(byte sexe);

    boolean tryToBreed();
}
