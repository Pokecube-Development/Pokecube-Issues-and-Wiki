package pokecube.api.entity.pokemob;

import javax.annotation.Nullable;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import pokecube.api.data.PokedexEntry;
import thut.api.entity.IBreedingMob;

public interface IHasEntry extends IHasMobAIStates
{
    /** @return the minecraft entity associated with this pokemob */
    Mob getEntity();

    /** @return the {@link PokedexEntry} of the species of this Pokemob */
    PokedexEntry getPokedexEntry();

    /** @return the int pokedex number */
    default Integer getPokedexNb()
    {
        return this.getPokedexEntry().getPokedexNb();
    }

    /** @return is this a shadow pokemob */
    default boolean isShadow()
    {
        return this.getPokedexEntry().isShadowForme;
    }

    /** @return is the pokemob shiny */
    boolean isShiny();

    /**
     * @param entityIn Sets the vanilla entity for this pokemob
     */
    void setEntity(Mob entityIn);

    /**
     * @return the {@link PokedexEntry} of the species of this Pokemob This will
     *         be reset to the value of {@link #getBasePokedexEntry()} when the
     *         pokemob is recalled
     */
    IPokemob setPokedexEntry(PokedexEntry newEntry);

    /**
     * This sets the root entry, which is what is correlated to the Mob Entity
     * itself, or the original entry from having changed forms. Generally use
     * {@link #setPokedexEntry(PokedexEntry)} except during initialisation of
     * the mob.
     * 
     * @param newEntry - entry to set
     */
    void setBasePokedexEntry(PokedexEntry newEntry);

    /**
     * This returns the root pokedex entry, ie the original one before changing
     * forms, etc.
     * 
     * @return root pokedex entry.
     */
    PokedexEntry getBasePokedexEntry();

    // Mirror some methods from IBreedingMob here

    /** resets the status of being in love */
    default void resetLoveStatus()
    {}

    default boolean canBreed()
    {
        return false;
    }

    default void tickBreedDelay(final int tickAmount)
    {

    }

    default void setReadyToMate(@Nullable final Player cause)
    {}

    default void mateWith(final IBreedingMob male)
    {}
}
