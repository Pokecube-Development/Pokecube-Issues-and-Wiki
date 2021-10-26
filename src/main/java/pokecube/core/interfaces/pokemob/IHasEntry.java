package pokecube.core.interfaces.pokemob;

import javax.annotation.Nullable;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
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
     * @param entityIn
     *            Sets the vanilla entity for this pokemob
     */
    void setEntity(Mob entityIn);

    /** @return the {@link PokedexEntry} of the species of this Pokemob */
    IPokemob setPokedexEntry(PokedexEntry newEntry);

    // Mirror some methods from IBreedingMob here

    /** resets the status of being in love */
    default void resetLoveStatus()
    {
    }

    default boolean canBreed()
    {
        return false;
    }

    default void tickBreedDelay(final int tickAmount)
    {

    }

    default void setReadyToMate(@Nullable final Player cause)
    {
    }

    default void mateWith(final IBreedingMob male)
    {
    }
}
