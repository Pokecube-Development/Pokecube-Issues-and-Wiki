package pokecube.core.interfaces.capabilities.impl;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import pokecube.core.PokecubeCore;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.moves.MoveEntry;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.capabilities.CapabilityAffected;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.entity.IOngoingAffected;
import pokecube.core.interfaces.entity.impl.PersistantStatusEffect;
import pokecube.core.interfaces.entity.impl.PersistantStatusEffect.Status;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.moves.PokemobMoveStats;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.animations.EntityMoveUse;
import pokecube.core.moves.zmoves.GZMoveManager;
import pokecube.core.network.pokemobs.PacketSyncMoveUse;
import pokecube.core.utils.PokeType;
import thut.api.maths.Vector3;
import thut.core.common.commands.CommandTools;

public abstract class PokemobMoves extends PokemobSexed
{

    @Override
    public void executeMove(final Entity target, Vector3 targetLocation, final float f)
    {
        String attack = this.getMove(this.getMoveIndex());
        // If no move selected, just return here.
        if (attack == IMoveConstants.MOVE_NONE || attack == null) return;

        // If no target location selected, set it accordingly.
        if (targetLocation == null) if (target != null) targetLocation = Vector3.getNewVector().set(target);
        else targetLocation = Vector3.getNewVector().set(this.getEntity());

        // If all moves are disabled, use struggle instead.
        final int index = this.getMoveIndex();
        if (index < 4 && index >= 0) if (this.getDisableTimer(index) > 0) attack = "struggle";

        final Move_Base move = MovesUtils.getMoveFromName(attack);
        // If the move is somehow null, report it and return early.
        if (move == null || move.move == null)
        {
            PokecubeCore.LOGGER.error(this.getDisplayName().getFormattedText() + " Has Used Unregistered Move: "
                    + attack + " " + index, new IllegalArgumentException());
            return;
        }

        // Check ranged vs contact and set cooldown accordinly.
        final boolean distanced = (move.getAttackCategory() & IMoveConstants.CATEGORY_DISTANCE) > 0;
        this.setAttackCooldown(MovesUtils.getAttackDelay(this, attack, distanced, target instanceof PlayerEntity));
        // Syncs that the move has at least been attempted, this is used for the
        // graphical indicator of move cooldowns
        PacketSyncMoveUse.sendUpdate(this);

        if (target != this.getEntity())
        {
            if (target instanceof MobEntity)
            {
                final MobEntity t = (MobEntity) target;
                if (t.getAttackTarget() != this.getEntity()) t.setAttackTarget(this.getEntity());
            }
            if (target instanceof LivingEntity) if (((LivingEntity) target).getRevengeTarget() != this.getEntity())
            {
                ((LivingEntity) target).setRevengeTarget(this.getEntity());
                this.getEntity().setRevengeTarget((LivingEntity) target);
            }
        }
        final int statusChange = this.getChanges();
        if ((statusChange & IMoveConstants.CHANGE_FLINCH) != 0)
        {
            ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.status.flinch", "red", this
                    .getDisplayName());
            this.displayMessageToOwner(mess);
            final IPokemob targetMob = CapabilityPokemob.getPokemobFor(this.getEntity().getAttackTarget());
            if (targetMob != null)
            {
                mess = CommandTools.makeTranslatedMessage("pokemob.status.flinch", "green", this.getDisplayName());
                targetMob.displayMessageToOwner(mess);
            }
            this.removeChange(IMoveConstants.CHANGE_FLINCH);
            return;
        }

        if ((statusChange & IMoveConstants.CHANGE_CONFUSED) != 0) if (Math.random() > 0.75)
        {
            this.removeChange(IMoveConstants.CHANGE_CONFUSED);
            ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.status.confuse.remove", "green", this
                    .getDisplayName());
            final IPokemob targetMob = CapabilityPokemob.getPokemobFor(this.getEntity().getAttackTarget());
            if (targetMob != null)
            {
                mess = CommandTools.makeTranslatedMessage("pokemob.status.confuse.remove", "red", this
                        .getDisplayName());
                targetMob.displayMessageToOwner(mess);
            }
            this.displayMessageToOwner(mess);
        }
        else if (Math.random() > 0.5)
        {
            MovesUtils.doAttack(MoveEntry.CONFUSED.name, this, this.getEntity());
            ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.status.confusion", "red", this
                    .getDisplayName());
            final IPokemob targetMob = CapabilityPokemob.getPokemobFor(this.getEntity().getAttackTarget());
            if (targetMob != null)
            {
                mess = CommandTools.makeTranslatedMessage("pokemob.status.confusion", "green", this.getDisplayName());
                targetMob.displayMessageToOwner(mess);
            }
            this.displayMessageToOwner(mess);
            return;
        }

        if (this.getMoveStats().infatuateTarget != null) if (!this.getMoveStats().infatuateTarget.isAlive()) this
                .getMoveStats().infatuateTarget = null;
        else if (Math.random() > 0.5)
        {
            final ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.status.infatuate", "red", this
                    .getDisplayName());
            this.displayMessageToOwner(mess);
            return;
        }
        if (this.here == null) this.here = Vector3.getNewVector();
        this.here.set(this.getEntity()).addTo(0, this.getEntity().getEyeHeight(), 0);
        MovesUtils.useMove(move, this.getEntity(), target, this.here, targetLocation);
        this.here.set(this.getEntity());
    }

    @Override
    public EntityMoveUse getActiveMove()
    {
        final int id = this.dataSync().get(this.params.ACTIVEMOVEID);
        if (id == -1) return null;
        if (this.activeMove == null || this.activeMove.getEntityId() != id)
        {
            final Entity move = this.getEntity().getEntityWorld().getEntityByID(id);
            if (move instanceof EntityMoveUse) this.activeMove = (EntityMoveUse) move;
        }
        return this.activeMove;
    }

    @Override
    public int getAttackCooldown()
    {
        return this.dataSync().get(this.params.ATTACKCOOLDOWN);
    }

    @Override
    public int getDisableTimer(final int index)
    {
        return this.dataSync().get(this.params.DISABLE[index]);
    }

    @Override
    public int getExplosionState()
    {
        return this.moveInfo.boomState;
    }

    @Override
    public int getMoveIndex()
    {
        final byte ret = this.dataSync().get(this.params.MOVEINDEXDW);
        return Math.max(0, ret);
    }

    @Override
    public String[] getMoves()
    {
        final IPokemob transformed = CapabilityPokemob.getPokemobFor(this.getTransformedTo());
        if (transformed != null && transformed.getTransformedTo() == null)
        {
            final IPokemob to = transformed;
            if (to != this) return to.getMoves();
        }
        return super.getMoves();
    }

    @Override
    public String[] getGZMoves()
    {
        // We can do processing here to see what moves to supply.
        final String[] g_z_moves = super.getGZMoves();
        final String[] moves = this.getMoves();
        boolean gigant = this.getCombatState(CombatStates.DYNAMAX) && this.getPokedexEntry().getTrimmedName().contains(
                "_gigantamax");
        for (int i = 0; i < 4; i++)
        {
            final String gmove = GZMoveManager.getGMove(this, moves[i], gigant);
            if (gmove != null)
            {
                if (gmove.startsWith("gmax")) gigant = false;
                g_z_moves[i] = gmove;
                continue;
            }
            final String zmove = GZMoveManager.getZMove(this, moves[i]);
            g_z_moves[i] = zmove;
        }
        return g_z_moves;
    }

    @Override
    public PokemobMoveStats getMoveStats()
    {
        return this.moveInfo;
    }

    @Override
    public boolean isOnGround()
    {
        return this.getEntity().onGround;
    }

    @Override
    public byte getStatus()
    {
        final Byte val = this.dataSync().get(this.params.STATUSDW);
        return (byte) Math.max(0, val);
    }

    @Override
    public short getStatusTimer()
    {
        return this.dataSync().get(this.params.STATUSTIMERDW);
    }

    @Override
    public Entity getTransformedTo()
    {
        final int id = this.dataSync().get(this.params.TRANSFORMEDTODW);
        if (id == -1) return null;
        Entity to = this.getMoveStats().transformedTo;
        if (to != null && id == to.getEntityId()) return to;
        to = this.getEntity().getEntityWorld().getEntityByID(id);
        this.setTransformedTo(to);
        return to;
    }

    @Override
    public void healStatus()
    {
        // Ensure max health, etc are correct.
        this.updateHealth();
        // Clear off any persistant effects.
        final IOngoingAffected affected = CapabilityAffected.getAffected(this.getEntity());
        if (affected != null) affected.removeEffects(PersistantStatusEffect.ID);
        this.dataSync().set(this.params.STATUSDW, (byte) 0);
    }

    @Override
    public void setActiveMove(final EntityMoveUse move)
    {
        this.activeMove = move;
        final int id = move == null ? -1 : move.getEntityId();
        this.dataSync().set(this.params.ACTIVEMOVEID, id);
    }

    @Override
    public void setAttackCooldown(final int timer)
    {
        this.dataSync().set(this.params.ATTACKCOOLDOWN, timer);
    }

    @Override
    public void setDisableTimer(final int index, final int timer)
    {
        this.dataSync().set(this.params.DISABLE[index], timer);
    }

    @Override
    public void setExplosionState(final int i)
    {
        if (i >= 0) this.moveInfo.Exploding = true;
        this.moveInfo.boomState = i;
    }

    @Override
    public void setMoveIndex(final int moveIndex)
    {
        if (!this.getEntity().isServerWorld())
        {
            // Do nothing, packet should be handled by gui handler, not us.
        }
        else
        {
            if (moveIndex == this.getMoveIndex() || this.getCombatState(CombatStates.NOMOVESWAP)) return;
            if (this.getMove(moveIndex) == null) this.setMoveIndex(5);
            this.moveInfo.ROLLOUTCOUNTER = 0;
            this.moveInfo.FURYCUTTERCOUNTER = 0;
            this.moveInfo.BLOCKCOUNTER = 0;
            this.moveInfo.blocked = false;
            this.moveInfo.blockTimer = 0;
            this.dataSync().set(this.params.MOVEINDEXDW, (byte) moveIndex);
        }
    }

    @Override
    public boolean setStatus(byte status, int turns)
    {
        non:
        if (this.getStatus() != IMoveConstants.STATUS_NON)
        {
            // Check if we actually have a status, if we do not, then we can
            // apply one.
            final IOngoingAffected affected = CapabilityAffected.getAffected(this.getEntity());
            if (affected != null) if (affected.getEffects(PersistantStatusEffect.ID) == null) break non;
            return false;
        }
        else if (status == IMoveConstants.STATUS_NON)
        {
            final IOngoingAffected affected = CapabilityAffected.getAffected(this.getEntity());
            affected.removeEffects(PersistantStatusEffect.ID);
            this.dataSync().set(this.params.STATUSDW, status);
            return true;
        }
        final Status actual = Status.getStatus(status);
        if (actual == null)
        {
            final List<Status> options = Lists.newArrayList();
            for (final Status temp : Status.values())
                if ((temp.getMask() & status) != 0) options.add(temp);
            if (options.isEmpty()) return false;
            if (options.size() > 1) Collections.shuffle(options);
            status = options.get(0).getMask();
        }
        if (status == IMoveConstants.STATUS_BRN && this.isType(PokeType.getType("fire"))) return false;
        if (status == IMoveConstants.STATUS_PAR && this.isType(PokeType.getType("electric"))) return false;
        if (status == IMoveConstants.STATUS_FRZ && this.isType(PokeType.getType("ice"))) return false;
        if ((status == IMoveConstants.STATUS_PSN || status == IMoveConstants.STATUS_PSN2) && (this.isType(PokeType
                .getType("poison")) || this.isType(PokeType.getType("steel")))) return false;
        this.dataSync().set(this.params.STATUSDW, status);
        if ((status == IMoveConstants.STATUS_SLP || status == IMoveConstants.STATUS_FRZ) && turns == -1) turns = 5;
        final short timer = (short) (turns == -1 ? PokecubeCore.getConfig().attackCooldown * 5
                : turns * PokecubeCore.getConfig().attackCooldown);
        this.setStatusTimer(timer);
        PersistantStatusEffect statusEffect;
        try
        {
            statusEffect = new PersistantStatusEffect(status, turns);
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.warn("Error making status effect!");
            return false;
        }
        return CapabilityAffected.addEffect(this.getEntity(), statusEffect);
    }

    @Override
    public void setStatusTimer(final short timer)
    {
        this.dataSync().set(this.params.STATUSTIMERDW, (int) timer);
    }

    @Override
    public void setTransformedTo(final Entity to)
    {
        final int id = to == null ? -1 : to.getEntityId();
        PokedexEntry newEntry = this.getPokedexEntry();
        if (id != -1)
        {
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(to);
            if (pokemob != null) newEntry = pokemob.getPokedexEntry();
        }
        this.getMoveStats().transformedTo = to;
        this.setType1(newEntry.getType1());
        this.setType2(newEntry.getType2());
        this.dataSync().set(this.params.TRANSFORMEDTODW, id);
    }

}
