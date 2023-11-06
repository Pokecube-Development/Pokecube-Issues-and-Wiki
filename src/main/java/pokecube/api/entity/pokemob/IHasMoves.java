package pokecube.api.entity.pokemob;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.IOngoingAffected;
import pokecube.api.entity.IOngoingAffected.IOngoingEffect;
import pokecube.api.entity.pokemob.IHasCommands.Command;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.pokemob.commandhandlers.SwapMovesHandler;
import pokecube.api.entity.pokemob.moves.PokemobMoveStats;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.core.PokecubeCore;
import pokecube.core.impl.entity.impl.NonPersistantStatusEffect;
import pokecube.core.impl.entity.impl.NonPersistantStatusEffect.Effect;
import pokecube.core.moves.MovesUtils;
import pokecube.core.network.pokemobs.PacketCommand;
import pokecube.core.network.pokemobs.PacketSyncNewMoves;
import thut.api.maths.Vector3;
import thut.core.common.commands.CommandTools;
import thut.lib.TComponent;

public interface IHasMoves extends IHasStats
{
    /**
     * Changes: {@link IMoveConstants#CHANGE_CONFUSED} for example. The set can
     * fail because the mob is immune against this change or because it already
     * has the change. If so, the method returns false.
     *
     * @param change the change to add
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
     * @param moveIndex0 index of 1st move
     * @param moveIndex1 index of 2nd move
     */
    default void exchangeMoves(final int moveIndex0, final int moveIndex1)
    {
        final String[] moves = this.getMoves();

        if (moveIndex0 >= moves.length && moveIndex1 >= moves.length)
        {
            boolean up = moveIndex0 >= moveIndex1;
            this.getMoveStats().num += up ? 1 : -1;
            this.getMoveStats().getLearningMove();
        }

        if (!this.getEntity().isEffectiveAi() && this.getGeneralState(GeneralStates.TAMED))
        {
            try
            {
                PacketCommand.sendCommand((IPokemob) this, Command.SWAPMOVES,
                        new SwapMovesHandler((byte) moveIndex0, (byte) moveIndex1));
            }
            catch (final Exception ex)
            {
                ex.printStackTrace();
            }
        }
        else
        {
            if (moveIndex0 >= moves.length && moveIndex1 >= moves.length)
            {}
            else if (moveIndex0 >= moves.length || moveIndex1 >= moves.length)
            {
                final int index = Math.min(moveIndex0, moveIndex1);
                if (this.getMove(4) == null || index > 3) return;
                final String move = this.getMove(4);
                this.getMoveStats().removePendingMove(move);
                String oldMove = this.getMove(index);
                if (!PokecubeCore.getConfig().movesForgottenWhenOverriden)
                    this.getMoveStats().addPendingMove(oldMove, null);
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
     * @param target the Entity to attack
     * @param f      the float parameter of the attackEntity method
     */
    void executeMove(LivingEntity target, Vector3 targetLocation, float f);

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
     * @param index - The move index to check
     * @return - ticks still disabled for
     */
    int getDisableTimer(int index);

    /** @return Name of the last move we used. */
    default String getLastMoveUsed()
    {
        return this.getMoveStats().lastMove;
    }

    /**
     * 
     * @return a cached selected move to reduce number of times AI classes, etc
     *         need to look this up.
     */
    @Nonnull
    default MoveEntry getSelectedMove()
    {
        String name = this.getMove(this.getMoveIndex());
        if (this.getMoveStats().selectedMove == null || !this.getMoveStats().selectedMove.name.equals(name))
        {
            this.getMoveStats().selectedMove = MovesUtils.getMove(name);
            if (this.getMoveStats().selectedMove == null)
                this.getMoveStats().selectedMove = MovesUtils.getMove(IMoveConstants.DEFAULT_MOVE);
        }
        return this.getMoveStats().selectedMove;
    }

    /**
     * Gets the {@link String} id of the specified move.
     *
     * @param i from 0 to {@link #getMovesCount()}-1
     * @return the String name of the move
     */
    @Nullable
    default String getMove(final int index)
    {
        final IPokemob to = PokemobCaps.getPokemobFor(this.getTransformedTo());
        if (to != null && this.getTransformedTo() == null) return to.getMove(index);

        final String[] moves = this.getMoves();

        if (index >= 0 && index < 4) return moves[index];
        if (index == 4 && this.getMoveStats().hasLearningMove()) return this.getMoveStats().getLearningMove();

        if (index == 5) return IMoveConstants.MOVE_NONE;
        return null;
    }

    /**
     * Returns the index of the move to be executed in executeMove method.
     *
     * @return the index from 0 to 3;
     */
    int getMoveIndex();

    /**
     * Defaults to 4
     * 
     * @return number of moves we can know at a time.
     */
    default int getMovesCount()
    {
        return 4;
    }

    /**
     * Returns all the 4 available moves name.
     *
     * @return an array of 4 {@link String}
     */
    String[] getMoves();

    /**
     * @return PokemobMoveStats object that contains all of our info about
     *         combat for moves, tracks things like toxic counters, etc
     */
    PokemobMoveStats getMoveStats();

    /**
     * Updates trackers for target ID, ally ID, enemy number, ally number, etc.
     */
    void updateBattleInfo();

    /**
     * @return number of enemies in the battle, mostly used for tracking in guis
     */
    int getEnemyNumber();

    /**
     * 
     * @return number of allies in the battle, mostly used for tracking in guis
     */
    int getAllyNumber();

    /**
     * @return entityId of our target.
     */
    int getTargetID();

    /**
     * 
     * @return ID of ally to target for single target moves when fighting
     *         multiples
     */
    int getAllyID();

    /**
     * @param id - new entityId of target, -1 for no target.
     */
    void setTargetID(int id);

    /**
     * @param id - new entityId of target, -1 for no target.
     */
    void setAllyID(int id);

    /** @return Mob we are transformed into, null for no mob. */
    LivingEntity getTransformedTo();

    /** Sets all changes back to none. */
    default void healChanges()
    {
        this.getMoveStats().changes = 0;
        final IOngoingAffected affected = PokemobCaps.getAffected(this.getEntity());
        if (affected != null) affected.removeEffects(NonPersistantStatusEffect.ID);
    }

    default boolean knowsMove(String moveName)
    {
        if (getMoveStats().newMoves.contains(moveName)) return true;
        final String[] moves = this.getMoves();
        for (final String move : moves) if (moveName.equals(move)) return true;
        return false;
    }

    /**
     * The pokemob learns the specified move. It will be set to an available
     * position or erase an existing one if non are available.
     *
     * @param moveName an existing move (registered in {@link MovesUtils})
     */
    default void learn(final String moveName)
    {
        if (moveName == null || this.getEntity().getLevel() == null || this.getEntity().getLevel().isClientSide) return;
        if (!MovesUtils.isMoveImplemented(moveName)) return;
        final LivingEntity thisEntity = this.getEntity();
        final IPokemob thisMob = PokemobCaps.getPokemobFor(thisEntity);
        // check it's not already known or forgotten
        for (int i = 0; i < this.getMovesCount(); i++) if (moveName.equals(this.getMove(i))) return;

        boolean learned = false;
        for (int i = 0; i < this.getMovesCount(); i++)
        {
            if (this.getMove(i) == null)
            {
                this.setMove(i, moveName);
                learned = true;
                if (thisMob.getOwner() != null && thisEntity.isAlive())
                {
                    final Component move = TComponent.translatable(MovesUtils.getUnlocalizedMove(moveName));
                    final Component mess = TComponent.translatable("pokemob.move.notify.learn",
                            thisMob.getDisplayName(), move);
                    thisMob.displayMessageToOwner(mess);
                }
                break;
            }
        }

        if (!learned && this.getGeneralState(GeneralStates.TAMED))
        {
            final Component mess = CommandTools.makeTranslatedMessage("pokemob.move.notify.learn", "",
                    thisMob.getDisplayName().getString(),
                    TComponent.translatable(MovesUtils.getUnlocalizedMove(moveName)));
            thisMob.displayMessageToOwner(mess);
            this.getMoveStats().addPendingMove(moveName, (IPokemob) this);
            return;
        }
        else if (!learned)
        {
            final int index = thisEntity.getRandom().nextInt(4);
            this.setMove(index, moveName);
        }
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
     * @param force  - if true will clear tracked target for ai.
     */
    void onSetTarget(LivingEntity entity, boolean force);

    /**
     * @param change the changes to set
     */
    default void removeChange(final int change)
    {
        this.getMoveStats().changes -= change;
        final IOngoingAffected affected = PokemobCaps.getAffected(this.getEntity());
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
     * See {@link IHasMoves#getAttackCooldown()}
     *
     * @param timer
     */
    void setAttackCooldown(int timer);

    /**
     * Marks the move as disabled for a certain time.
     *
     * @param index - The move index to disable
     * @param timer - How many ticks to disable for
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
     * @param i        from 0 to 3
     * @param moveName
     */
    void setMove(int i, String moveName);

    /**
     * Sets the move index.
     *
     * @param i must be a value from 0 to 3
     */
    public void setMoveIndex(int i);

    /**
     * Statuses: {@link IMoveConstants#STATUS_PSN} for example. The set can fail
     * because the mob is immune against this status (a fire-type Pokemon can't
     * be burned for example) or because it already have a status. If so, the
     * method returns false.
     *
     * @param status the status to set
     * @return whether the status has actually been set
     */
    default boolean setStatus(IPokemob source, int status)
    {
        return this.setStatus(source, status, -1);
    }

    /**
     * Same as {@link IHasMoves#setStatus(byte)} but also specifies the duration
     * for the effect.
     *
     * @param status the status to set
     * @param turns  How many times attackCooldown should the status apply.
     * @return whether the status has actually been set
     */
    boolean setStatus(IPokemob source, int status, int turns);

    /**
     * Sets the initial status timer. The timer will be decreased until 0. The
     * timer for SLP. When reach 0, the mob wakes up.
     *
     * @param timer the initial value to set
     */
    void setStatusTimer(short timer);

    /**
     * The pokemob will render and have moves according to whatever is set here.
     * If null is set, then it will use its own moves.
     *
     * @param to
     */
    void setTransformedTo(LivingEntity to);
}
