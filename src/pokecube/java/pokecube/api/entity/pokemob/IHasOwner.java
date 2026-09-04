package pokecube.api.entity.pokemob;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.core.ai.logic.LogicMountedControl;
import thut.api.attachments.IOwnable;
import thut.api.attachments.Ownable;

public interface IHasOwner extends IHasMobAIStates, IOwnable
{
    Mob getEntity();

    default IOwnable getOwnerHolder()
    {
        return this.getEntity().getData(Ownable.TYPE);
    }

    default void setOwnerHolder(IOwnable holder)
    {
        if(holder instanceof Ownable.IOwnableSerializable serializable)
            this.getEntity().setData(Ownable.TYPE, serializable);
    }

    @Override
    @Nullable
    default LivingEntity getOwner()
    {
        return getOwnerHolder().getOwner();
    }

    @Override
    default UUID getOwnerId()
    {
        return getOwnerHolder().getOwnerId();
    }

    @Override
    default void setOwner(LivingEntity e)
    {
        getOwnerHolder().setOwner(e);
    }

    @Override
    default void setOwner(UUID id)
    {
        getOwnerHolder().setOwner(id);
    }

    @Override
    default void setPlayerOwned(boolean playerOwned)
    {
        getOwnerHolder().setPlayerOwned(playerOwned);
    }
    
    @Override
    default boolean isPlayerOwned()
    {
        return getOwnerHolder().isPlayerOwned();
    }

    /**
     * Displays a message in the console of the owner player (if this pokemob is
     * tamed).
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

    /**
     * @return Team we are on, guarding pokemobs shouldn't attack team members.
     */
    @Nonnull
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
     * @param original trainer's UUID
     */
    void setOriginalOwnerUUID(UUID original);

    /**
     * Sets the pokecube id to know whether its a greatcube, ultracube...
     */
    void setPokecube(ItemStack pokecube);

    /**
     * Sets the team we are on, this is used for things like guarding
     */
    void setPokemobTeam(@Nonnull String team);

    /** Sets the nickname */
    void setPokemonNickname(String nickname);

    /**
     * Sets that we are traded.
     */
    default void setTraded(final boolean trade)
    {
        this.setGeneralState(GeneralStates.TRADED, trade);
    }

    /**
     * Has pokemob been traded
     */
    default boolean traded()
    {
        return this.getGeneralState(GeneralStates.TRADED);
    }
}
