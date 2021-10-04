package pokecube.core.interfaces.pokemob;

import java.util.UUID;

import javax.annotation.Nonnull;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import pokecube.core.ai.logic.LogicMountedControl;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import thut.api.IOwnable;

public interface IHasOwner extends IHasMobAIStates, IOwnable
{
    /**
     * Displays a message in the console of the owner player (if this pokemob
     * is tamed).
     *
     * @param message
     */
    void displayMessageToOwner(Component message);

    /** @return the controller object for when this is ridden */
    default LogicMountedControl getController()
    {
        return null;
    }

    /** @return The direction this mob is going, only relevant when ridden. */
    float getHeading();

    /**
     * @return UUID of original Trainer, used to prevent nicknaming of traded
     *         pokemobs
     */
    UUID getOriginalOwnerUUID();

    /**
     * Returns the pokecube id to know whether its a greatcube, ultracube...
     *
     * @return the shifted index of the item
     */
    ItemStack getPokecube();

    @Nonnull
    /**
     * @return Team we are on, guarding pokemobs shouldn't attack team
     *         members.
     */
    String getPokemobTeam();

    /** @return the String nickname */
    String getPokemonNickname();

    /**
     * Sets the direction this mob is going when ridden, if the mob is not
     * ridden, this method should do nothing.
     */
    void setHeading(float heading);

    /**
     * Sets owner uuid
     *
     * @param original
     *            trainer's UUID
     */
    void setOriginalOwnerUUID(UUID original);

    /**
     * Sets the pokecube id to know whether its a greatcube, ultracube...
     *
     * @param pokeballId
     */
    void setPokecube(ItemStack pokecube);

    /**
     * Sets the team we are on, this is used for things like guarding
     *
     * @param team
     */
    void setPokemobTeam(@Nonnull String team);

    /** Sets the nickname */
    void setPokemonNickname(String nickname);

    /**
     * Sets that we are traded.
     *
     * @param trade
     */
    default void setTraded(final boolean trade)
    {
        this.setGeneralState(GeneralStates.TRADED, trade);
    }

    /**
     * Has pokemob been traded
     *
     * @return
     */
    default boolean traded()
    {
        return this.getGeneralState(GeneralStates.TRADED);
    }
}
