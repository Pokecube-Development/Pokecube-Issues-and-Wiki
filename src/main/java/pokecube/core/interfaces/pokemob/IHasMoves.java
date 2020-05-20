package pokecube.core.interfaces.pokemob;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.eventbus.api.Event;
import pokecube.core.PokecubeCore;
import pokecube.core.events.pokemob.combat.MoveUse;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityAffected;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.entity.IOngoingAffected;
import pokecube.core.interfaces.entity.IOngoingAffected.IOngoingEffect;
import pokecube.core.interfaces.entity.impl.NonPersistantStatusEffect;
import pokecube.core.interfaces.entity.impl.NonPersistantStatusEffect.Effect;
import pokecube.core.interfaces.pokemob.IHasCommands.Command;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.commandhandlers.SwapMovesHandler;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.interfaces.pokemob.moves.PokemobMoveStats;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.animations.EntityMoveUse;
import pokecube.core.network.pokemobs.PacketCommand;
import pokecube.core.network.pokemobs.PacketSyncNewMoves;
import thut.api.maths.Vector3;
import thut.core.common.commands.CommandTools;

public interface IHasMoves extends IHasStats
{
    /**
     * Changes: {@link IMoveConstants#CHANGE_CONFUSED} for example. The set can
     * fail because the mob is immune against this change or because it already
     * has the change. If so, the method returns false.
     *
     * @param change
     *            the change to add
     * @return whether the change has actually been added
     */
    default boolean addChange(final int change)
    {
        final int old = this.getMoveStats().changes;
        this.getMoveStats().changes |= change;
        return this.getMoveStats().changes != old;
    }

    /**
     * Used by Gui Pokedex. Exchange the two moves.
     *
     * @param moveIndex0
     *            index of 1st move
     * @param moveIndex1
     *            index of 2nd move
     */
    default void exchangeMoves(final int moveIndex0, final int moveIndex1)
    {
        if (!this.getEntity().isServerWorld() && this.getGeneralState(GeneralStates.TAMED))
        {
            final String[] moves = this.getMoves();
            if (moveIndex0 >= moves.length && moveIndex1 >= moves.length) this.getMoveStats().num++;
            try
            {
                PacketCommand.sendCommand((IPokemob) this, Command.SWAPMOVES, new SwapMovesHandler((byte) moveIndex0,
                        (byte) moveIndex1));
            }
            catch (final Exception ex)
            {
                ex.printStackTrace();
            }
        }
        else
        {
            final String[] moves = this.getMoves();

            if (moveIndex0 >= moves.length && moveIndex1 >= moves.length) this.getMoveStats().num++;
            else if (moveIndex0 >= moves.length || moveIndex1 >= moves.length)
            {
                final int index = Math.min(moveIndex0, moveIndex1);
                if (this.getMove(4) == null || index > 3) return;
                final String move = this.getMove(4);
                this.getMoveStats().newMoves.remove(move);
                this.setMove(index, move);
                PacketSyncNewMoves.sendUpdatePacket((IPokemob) this);
            }
            else
            {
                final String move0 = moves[moveIndex0];
                final String move1 = moves[moveIndex1];
                if (move0 != null && move1 != null)
                {
                    moves[moveIndex0] = move1;
                    moves[moveIndex1] = move0;
                }
                this.setMoves(moves);
            }
        }
    }

    /**
     * Called by attackEntity(Entity entity, float f). Executes the move it's
     * supposed to do according to his trainer command or a random one if it's
     * wild.
     *
     * @param target
     *            the Entity to attack
     * @param f
     *            the float parameter of the attackEntity method
     */
    void executeMove(Entity target, Vector3 targetLocation, float f);

    /** @return Current move we have being executed. */
    EntityMoveUse getActiveMove();

    /**
     * Cooldown for attacks, if this is greater than 0, we shouldn't be able to
     * use any moves.
     *
     * @return
     */
    int getAttackCooldown();

    /**
     * Changes: {@link IMoveConstants#CHANGE_CONFUSED} for example.
     *
     * @return the change state
     */
    default int getChanges()
    {
        return this.getMoveStats().changes;
    }

    /**
     * If this is greater than 0, the move is considered disabled.
     *
     * @param index
     *            - The move index to check
     * @return - ticks still disabled for
     */
    int getDisableTimer(int index);

    /** @return Name of the last move we used. */
    default String getLastMoveUsed()
    {
        return this.getMoveStats().lastMove;
    }

    /**
     * Gets the {@link String} id of the specified move.
     *
     * @param i
     *            from 0 to 3
     * @return the String name of the move
     */
    default String getMove(final int index)
    {
        final IPokemob to = CapabilityPokemob.getPokemobFor(this.getTransformedTo());
        if (to != null && this.getTransformedTo() == null) return to.getMove(index);

        final String[] moves = this.getMoves();
        if (this.getCombatState(CombatStates.USINGGZMOVE))
        {
            final String[] gzmoves = this.getGZMoves();
            String move;
            if (index >= 0 && index < 4 && (move = gzmoves[index]) != null)
            {
                gzmoves.toString();
                return move;
            }
        }

        if (index >= 0 && index < 4) return moves[index];
        if (index == 4 && moves[3] != null) if (!this.getMoveStats().newMoves.isEmpty()) return this
                .getMoveStats().newMoves.get(this.getMoveStats().num % this.getMoveStats().newMoves.size());

        if (index == 5) return IMoveConstants.MOVE_NONE;
        return null;
    }

    /**
     * Returns the index of the move to be executed in executeMove method.
     *
     * @return the index from 0 to 3;
     */
    public int getMoveIndex();

    /**
     * Returns all the 4 available moves name.
     *
     * @return an array of 4 {@link String}
     */
    String[] getMoves();

    /**
     * This returns the names of any available Gmax or Z-moves for the pokemob,
     * their indicies correspond directly to the equivalent moves in getMoves()
     *
     * @return an array of 4 {@link String}
     */
    default String[] getGZMoves()
    {
        return this.getMoveStats().g_z_moves;
    }

    /**
     * @return PokemobMoveStats object that contains all of our info about
     *         combat for moves, tracks things like toxic counters, etc
     */
    PokemobMoveStats getMoveStats();

    /** @return entityId of our target. */
    int getTargetID();

    /** @return Mob we are transformed into, null for no mob. */
    Entity getTransformedTo();

    /** Sets all changes back to none. */
    default void healChanges()
    {
        this.getMoveStats().changes = 0;
        final IOngoingAffected affected = CapabilityAffected.getAffected(this.getEntity());
        if (affected != null) affected.removeEffects(NonPersistantStatusEffect.ID);
    }

    /**
     * The pokemob learns the specified move. It will be set to an available
     * position or erase an existing one if non are available.
     *
     * @param moveName
     *            an existing move (registered in {@link MovesUtils})
     */
    default void learn(final String moveName)
    {
        if (moveName == null || this.getEntity().getEntityWorld() == null || this.getEntity().getEntityWorld().isRemote)
            return;
        if (!MovesUtils.isMoveImplemented(moveName)) return;
        final String[] moves = this.getMoves();
        final LivingEntity thisEntity = this.getEntity();
        final IPokemob thisMob = CapabilityPokemob.getPokemobFor(thisEntity);
        // check it's not already known or forgotten
        for (final String move : moves)
            if (moveName.equals(move)) return;

        if (thisMob.getOwner() != null && thisEntity.isAlive())
        {
            final ITextComponent move = new TranslationTextComponent(MovesUtils.getUnlocalizedMove(moveName));
            final ITextComponent mess = new TranslationTextComponent("pokemob.move.notify.learn", thisMob
                    .getDisplayName(), move);
            thisMob.displayMessageToOwner(mess);
        }
        if (moves[0] == null) this.setMove(0, moveName);
        else if (moves[1] == null) this.setMove(1, moveName);
        else if (moves[2] == null) this.setMove(2, moveName);
        else if (moves[3] == null) this.setMove(3, moveName);
        else if (this.getGeneralState(GeneralStates.TAMED))
        {
            if (moves[3] != null)
            {
                for (final String s : moves)
                {
                    if (s == null) continue;
                    if (s.equals(moveName)) return;
                }
                final ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.move.notify.learn", "", thisMob
                        .getDisplayName().getFormattedText(), new TranslationTextComponent(MovesUtils
                                .getUnlocalizedMove(moveName)));
                thisMob.displayMessageToOwner(mess);
                if (!this.getMoveStats().newMoves.contains(moveName))
                {
                    this.getMoveStats().newMoves.add(moveName);
                    PacketSyncNewMoves.sendUpdatePacket((IPokemob) this);
                }
                return;
            }
        }
        else
        {
            final int index = thisEntity.getRNG().nextInt(4);
            this.setMove(index, moveName);
        }
    }

    /**
     * This is called during move use to both the attacker and the attacked
     * entity, in that order. This can be used to add in abilities, In
     * EntityMovesPokemob, this is used for accounting for moves like curse,
     * detect, protect, etc, moves which either have different effects per
     * pokemon type, or moves that prevent damage.
     *
     * @param move
     */
    default void onMoveUse(final MovePacket move)
    {
        final Event toPost = move.pre ? new MoveUse.DuringUse.Pre(move, move.attacker == this)
                : new MoveUse.DuringUse.Post(move, move.attacker == this);
        PokecubeCore.MOVE_BUS.post(toPost);
    }

    /**
     * Called to notify the pokemob that a new target has been set.
     *
     * @param entity
     */
    default void onSetTarget(final LivingEntity entity)
    {
        this.onSetTarget(entity, false);
    }

    /**
     * Called to notify the pokemob that a new target has been set.
     *
     * @param entity
     * @param force
     *            - if true will clear tracked target for ai.
     */
    void onSetTarget(LivingEntity entity, boolean force);

    /**
     * @param change
     *            the changes to set
     */
    default void removeChange(final int change)
    {
        this.getMoveStats().changes -= change;
        final IOngoingAffected affected = CapabilityAffected.getAffected(this.getEntity());
        if (affected != null)
        {
            final Effect toRemove = Effect.getStatus((byte) change);
            for (final IOngoingEffect effect : affected.getEffects(NonPersistantStatusEffect.ID))
                if (effect instanceof NonPersistantStatusEffect
                        && ((NonPersistantStatusEffect) effect).effect == toRemove)
                {
                    affected.removeEffect(effect);
                    break;
                }
        }
    }

    /**
     * Sets the move we are currently using, having this ensures we don't fire
     * multiple moves before old one lands.
     *
     * @param move
     */
    void setActiveMove(EntityMoveUse move);

    /**
     * See {@link IHasMoves#getAttackCooldown()}
     *
     * @param timer
     */
    void setAttackCooldown(int timer);

    /**
     * Marks the move as disabled for a certain time.
     *
     * @param index
     *            - The move index to disable
     * @param timer
     *            - How many ticks to disable for
     */
    void setDisableTimer(int index, int timer);

    /**
     * Sets the index of the new move to learn from the list of learnable new
     * moves, see {@link PokemobMoveStats#newMoves}
     *
     * @param num
     */
    default void setLeaningMoveIndex(final int num)
    {
        this.getMoveStats().num = num;
    }

    /**
     * Sets the {@link String} id of the specified move.
     *
     * @param i
     *            from 0 to 3
     * @param moveName
     */
    void setMove(int i, String moveName);

    /**
     * Sets the move index.
     *
     * @param i
     *            must be a value from 0 to 3
     */
    public void setMoveIndex(int i);

    /**
     * Statuses: {@link IMoveConstants#STATUS_PSN} for example. The set can
     * fail because the mob is immune against this status (a fire-type Pokemon
     * can't be burned for example) or because it already have a status. If so,
     * the method returns false.
     *
     * @param status
     *            the status to set
     * @return whether the status has actually been set
     */
    default boolean setStatus(final byte status)
    {
        return this.setStatus(status, -1);
    }

    /**
     * Same as {@link IHasMoves#setStatus(byte)} but also specifies the
     * duration for the effect.
     *
     * @param status
     *            the status to set
     * @param turns
     *            How many times attackCooldown should the status apply.
     * @return whether the status has actually been set
     */
    boolean setStatus(byte status, int turns);

    /**
     * Sets the initial status timer. The timer will be decreased until 0. The
     * timer for SLP. When reach 0, the mob wakes up.
     *
     * @param timer
     *            the initial value to set
     */
    void setStatusTimer(short timer);

    /**
     * @param id
     *            - new entityId of target, -1 for no target.
     */
    void setTargetID(int id);

    /**
     * The pokemob will render and have moves according to whatever is set
     * here. If null is set, then it will use its own moves.
     *
     * @param to
     */
    void setTransformedTo(Entity to);
}
