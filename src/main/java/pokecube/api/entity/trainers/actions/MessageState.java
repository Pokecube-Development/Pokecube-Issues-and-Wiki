package pokecube.api.entity.trainers.actions;

public enum MessageState
{
    /**
     * A general right click interaction
     */
    INTERACT,
    /**
     * A right click interaction which results in an accepted battle
     */
    INTERACT_YESBATTLE,
    /**
     * A right click interaction which results in a declined battle
     */
    INTERACT_NOBATTLE,
    /**
     * An attack/harmed interaction
     */
    HURT,
    /**
     * We have set a new combat target
     */
    AGRESS,
    /**
     * We are about to send out a pokemob
     */
    ABOUTSEND,
    /**
     * We have just sent out a pokemob
     */
    SENDOUT,
    /**
     * We have been defeated
     */
    DEFEAT,
    /**
     * We have given up trying to fight
     */
    DEAGRESS,
    /**
     * We are giving a reward item
     */
    GIVEITEM;

    private MessageState()
    {}
}
