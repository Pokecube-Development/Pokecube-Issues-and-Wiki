package pokecube.core.impl.capabilities.impl;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.CapabilityAffected;
import pokecube.api.entity.IOngoingAffected;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.entity.pokemob.moves.PokemobMoveStats;
import pokecube.api.events.pokemobs.combat.MoveUse.ActualMoveUse;
import pokecube.api.moves.Battle;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.entity.npc.NpcMob;
import pokecube.core.impl.entity.impl.PersistantStatusEffect;
import pokecube.core.impl.entity.impl.PersistantStatusEffect.Status;
import pokecube.core.init.EntityTypes;
import pokecube.core.moves.MovesUtils;
import pokecube.core.network.pokemobs.PacketSyncMoveUse;
import pokecube.core.utils.AITools;
import thut.api.entity.ICopyMob;
import thut.api.maths.Vector3;
import thut.core.common.commands.CommandTools;
import thut.core.common.network.CapabilitySync;
import thut.lib.RegHelper;

public abstract class PokemobMoves extends PokemobStats
{
    private static final Set<String> TO_SYNC = Sets.newHashSet("thutcore:copymob");

    @Override
    public void executeMove(final LivingEntity target, Vector3 targetLocation, final float f)
    {
        String attack = this.getMove(this.getMoveIndex());
        BrainUtils.clearMoveUseTarget(this.getEntity());

        // If no move selected, just return here.
        if (attack == IMoveConstants.MOVE_NONE || attack == null) return;

        // If no target location selected, set it accordingly.
        if (targetLocation == null) if (target != null) targetLocation = new Vector3().set(target);
        else targetLocation = new Vector3().set(this.getEntity());

        // If all moves are disabled, use struggle instead.
        final int index = this.getMoveIndex();
        if (index < 4 && index >= 0) if (this.getDisableTimer(index) > 0) attack = "struggle";

        final MoveEntry move = MovesUtils.getMove(attack);
        // If the move is somehow null, report it and return early.
        if (move == null)
        {
            PokecubeAPI.LOGGER
                    .error(this.getDisplayName().getString() + " Has Used Unregistered Move: " + attack + " " + index);
            return;
        }

        // Check ranged vs contact and set cooldown accordinly.
        final boolean distanced = move.isRanged(this);
        this.setAttackCooldown(MovesUtils.getAttackDelay(this, attack, distanced, target instanceof Player));
        // Syncs that the move has at least been attempted, this is used for the
        // graphical indicator of move cooldowns
        PacketSyncMoveUse.sendUpdate(this);

        if (!PokecubeAPI.MOVE_BUS.post(new ActualMoveUse.PreMoveStatus(this, move, target)))
        {
            final int statusChange = this.getChanges();
            final IPokemob targetMob = PokemobCaps.getPokemobFor(BrainUtils.getAttackTarget(this.getEntity()));
            if ((statusChange & IMoveConstants.CHANGE_FLINCH) != 0)
            {
                Component mess = CommandTools.makeTranslatedMessage("pokemob.status.flinch", "red",
                        this.getDisplayName());
                this.displayMessageToOwner(mess);
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
                Component mess = CommandTools.makeTranslatedMessage("pokemob.status.confuse.remove", "green",
                        this.getDisplayName());
                if (targetMob != null)
                {
                    mess = CommandTools.makeTranslatedMessage("pokemob.status.confuse.remove", "red",
                            this.getDisplayName());
                    targetMob.displayMessageToOwner(mess);
                }
                this.displayMessageToOwner(mess);
            }
            else if (Math.random() > 0.5)
            {
                MovesUtils.doAttack(MoveEntry.CONFUSED.name, this, this.getEntity());
                Component mess = CommandTools.makeTranslatedMessage("pokemob.status.confusion", "red",
                        this.getDisplayName());
                if (targetMob != null)
                {
                    mess = CommandTools.makeTranslatedMessage("pokemob.status.confusion", "green",
                            this.getDisplayName());
                    targetMob.displayMessageToOwner(mess);
                }
                this.displayMessageToOwner(mess);
                return;
            }

            if (this.getMoveStats().infatuateTarget != null)
                if (!this.getMoveStats().infatuateTarget.isAlive()) this.getMoveStats().infatuateTarget = null;
                else if (Math.random() > 0.5)
            {
                final Component mess = CommandTools.makeTranslatedMessage("pokemob.status.infatuate", "red",
                        this.getDisplayName());
                this.displayMessageToOwner(mess);
                return;
            }
        }

        if (this.here == null) this.here = new Vector3();
        this.here.set(this.getEntity()).addTo(0, this.getEntity().getEyeHeight(), 0);
        MovesUtils.useMove(move, this.getEntity(), target, this.here, targetLocation);
        // clear this if we use a move.
        this.setCombatState(CombatStates.NOITEMUSE, false);
        this.here.set(this.getEntity());
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
    public PokemobMoveStats getMoveStats()
    {
        return this.moveInfo;
    }

    @Override
    public boolean isOnGround()
    {
        return this.getEntity().isOnGround();
    }

    @Override
    public int getStatus()
    {
        final Integer val = this.dataSync().get(this.params.STATUSDW);
        return Math.max(0, val);
    }

    @Override
    public int getEnemyNumber()
    {
        return this.dataSync().get(this.params.ENEMYNUMDW);
    }

    @Override
    public int getAllyNumber()
    {
        return this.dataSync().get(this.params.ALLYNUMDW);
    }

    @Override
    public int getTargetID()
    {
        return this.dataSync.get(this.params.ATTACKTARGETIDDW);
    }

    @Override
    public void setTargetID(final int id)
    {
        this.dataSync.set(this.params.ATTACKTARGETIDDW, Integer.valueOf(id));
    }

    @Override
    public int getAllyID()
    {
        return this.dataSync.get(this.params.ALLYTARGETIDDW);
    }

    @Override
    public void setAllyID(final int id)
    {
        this.dataSync.set(this.params.ALLYTARGETIDDW, Integer.valueOf(id));
    }

    private void setNoBattle(int ownerOffset)
    {
        // Enemy always empty when not in battle
        this.setTargetID(-1);

        // Ally is either us, or owner when not in battle.
        int allyIndex = this.getMoveStats().allyIndex % 2;
        if (allyIndex < 0) allyIndex = ownerOffset;

        if (allyIndex == 1)
        {
            this.setAllyID(this.getOwner().id);
        }
        else this.setAllyID(this.getEntity().id);

        this.dataSync().set(this.params.ENEMYNUMDW, 0);
        this.dataSync().set(this.params.ALLYNUMDW, 1);
    }

    @Override
    public void updateBattleInfo()
    {
        LivingEntity owner = this.getOwner();
        LivingEntity target = null;
        // Only process battle stuff server side.
        battle_check:
        if (!entity.getLevel().isClientSide())
        {
            Battle b = Battle.getBattle(entity);

            if (owner != null)
            {
                Battle b2 = Battle.getBattle(owner);
                if (b2 != b)
                {
                    // If owner has no battle, but we do, owner joins our battle
                    if (b2 == null)
                    {
                        var mobs = b.getEnemies(entity);
                        if (!mobs.isEmpty()) Battle.createOrAddToBattle(owner, mobs.get(0));
                    }
                    else if (b == null)
                    {
                        // If we have no battle, but owner does, we join owner's
                        // battle
                        b = b2;
                        var mobs = b.getEnemies(owner);
                        if (!mobs.isEmpty()) Battle.createOrAddToBattle(entity, mobs.get(0));
                    }
                }
            }
            this.setBattle(b);

            // If no battle, but is in combat, then make a new battle
            if (b == null && this.inCombat())
            {
                target = entity.getTarget();
                if (target != null) Battle.createOrAddToBattle(entity, target);

                b = Battle.getBattle(entity);
                this.setBattle(b);
            }
            this.setCombatState(CombatStates.BATTLING, b != null);

            int ownerOffset = owner != null ? 1 : 0;

            // No battle case
            if (b == null)
            {
                setNoBattle(ownerOffset);
                break battle_check;
            }

            // Battle case

            // Handle the enemies lists first, as if there are none, we can end
            // early.

            List<LivingEntity> mobs = Lists.newArrayList(b.getEnemies(entity));

            // Ensure that the mobs are valid targets.
            mobs.removeIf(t2 -> !AITools.shouldBeAbleToAgro(entity, t2));

            // If no enemies, lets just end the battle.
            if (mobs.isEmpty())
            {
                b.removeFromBattle(entity);
                setNoBattle(ownerOffset);
                this.setCombatState(CombatStates.BATTLING, false);
                break battle_check;
            }

            // Now ensure target index is in range.
            int targetIndex = this.getMoveStats().enemyIndex;
            if (targetIndex < 0) targetIndex = mobs.size() - 1;

            // Update the appropriate number of mobs.
            this.dataSync().set(this.params.ENEMYNUMDW, mobs.size());

            // And set the target.
            target = mobs.isEmpty() ? null : mobs.get(targetIndex % mobs.size());

            // Then also sync attack target in brain.
            var brainTarget = BrainUtils.getAttackTarget(entity);
            brains:
            if (target != brainTarget)
            {
                // Check if the new target is still a combat member, if so, swap
                // over to it
                int i = mobs.indexOf(brainTarget);
                if (i != -1 && target == null)
                {
                    this.getMoveStats().enemyIndex = i;
                    target = brainTarget;
                    break brains;
                }
                BrainUtils.setAttackTarget(entity, target);
            }
            this.setTargetID(target == null ? -1 : target.id);

            // Allies are simple

            mobs = b.getAllies(entity);
            // Update how many allies we have
            this.dataSync().set(this.params.ALLYNUMDW, mobs.size());
            // Get the number for modulo, as we also include owner here if
            // present.
            int allyN = mobs.size() + ownerOffset;

            int allyIndex = (allyN != 0) ? this.getMoveStats().allyIndex % allyN : 0;
            // If less than 0, wrap
            if (allyIndex < 0) allyIndex = mobs.size();
            // If max suze, and have owner, we set it as owner
            if (allyIndex == mobs.size() && ownerOffset > 0)
            {
                // Ally is owner
                if (owner != null) this.setAllyID(owner.id);
            }
            // Otherwise if in bounds, set ally.
            else if (mobs.size() > 0)
            {
                this.setAllyID(mobs.get(allyIndex).id);
            }
            break battle_check;
        }
        // Client side we pull them from the ids.
        else
        {
            Entity e = entity.getLevel().getEntity(this.getTargetID());
            if (e instanceof LivingEntity living) target = living;
        }

        // Then both sides update targetEnemy and targetAlly
        this.getMoveStats().targetEnemy = target;
        Entity e = entity.getLevel().getEntity(this.getAllyID());
        this.getMoveStats().targetAlly = e instanceof LivingEntity living ? living : null;

        // Only owned mobs process beyond here.
        if (owner == null) return;

        // Ensure indeces are in range
        int num = this.getAllyNumber() + 1;
        // Cull down in this case
        if (this.getMoveStats().allyIndex >= num) this.getMoveStats().allyIndex = 0;
        else if (this.getMoveStats().allyIndex < 0) this.getMoveStats().allyIndex = 1;

        num = this.getEnemyNumber();
        // Cull down in this case
        if (this.getMoveStats().enemyIndex >= num) this.getMoveStats().enemyIndex = 0;
        else if (this.getMoveStats().enemyIndex < 0) this.getMoveStats().enemyIndex = num - 1;
    }

    @Override
    public short getStatusTimer()
    {
        return this.dataSync().get(this.params.STATUSTIMERDW);
    }

    @Override
    public LivingEntity getTransformedTo()
    {
        return this.getCopiedMob();
    }

    @Override
    public void healStatus()
    {
        // Ensure max health, etc are correct.
        this.updateHealth();
        // Clear off any persistant effects.
        final IOngoingAffected affected = PokemobCaps.getAffected(this.getEntity());
        if (affected != null) affected.removeEffects(PersistantStatusEffect.ID);
        this.dataSync().set(this.params.STATUSDW, 0);
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
        if (!this.getEntity().isEffectiveAi())
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
            this.getMoveStats().selectedMove = null;
        }
    }

    @Override
    public boolean setStatus(IPokemob source, int status, int turns)
    {
        non:
        if (this.getStatus() != IMoveConstants.STATUS_NON)
        {
            // Check if we actually have a status, if we do not, then we can
            // apply one.
            final IOngoingAffected affected = PokemobCaps.getAffected(this.getEntity());
            if (affected != null) if (affected.getEffects(PersistantStatusEffect.ID) == null) break non;
            return false;
        }
        else if (status == IMoveConstants.STATUS_NON)
        {
            final IOngoingAffected affected = PokemobCaps.getAffected(this.getEntity());
            affected.removeEffects(PersistantStatusEffect.ID);
            this.dataSync().set(this.params.STATUSDW, status);
            return true;
        }
        final Status actual = Status.getStatus(status);
        if (actual == null)
        {
            final List<Status> options = Lists.newArrayList();
            for (final Status temp : Status.values()) if ((temp.getMask() & status) != 0) options.add(temp);
            if (options.isEmpty()) return false;
            if (options.size() > 1) Collections.shuffle(options);
            status = options.get(0).getMask();
        }
        if (status == IMoveConstants.STATUS_BRN && this.isType(PokeType.getType("fire"))) return false;
        if (status == IMoveConstants.STATUS_PAR && this.isType(PokeType.getType("electric"))) return false;
        if (status == IMoveConstants.STATUS_FRZ && this.isType(PokeType.getType("ice"))) return false;
        if ((status == IMoveConstants.STATUS_PSN || status == IMoveConstants.STATUS_PSN2)
                && (this.isType(PokeType.getType("poison")) || this.isType(PokeType.getType("steel"))))
            return false;
        this.dataSync().set(this.params.STATUSDW, status);
        if ((status == IMoveConstants.STATUS_SLP || status == IMoveConstants.STATUS_FRZ) && turns == -1) turns = 5;
        final short timer = (short) (turns == -1 ? PokecubeCore.getConfig().attackCooldown * 5
                : turns * PokecubeCore.getConfig().attackCooldown);
        this.setStatusTimer(timer);
        PersistantStatusEffect statusEffect;
        statusEffect = new PersistantStatusEffect(status, turns);
        if (source != null) statusEffect.setSource(source.getEntity().getUUID());
        return CapabilityAffected.addEffect(this.getEntity(), statusEffect);
    }

    @Override
    public void setStatusTimer(final short timer)
    {
        this.dataSync().set(this.params.STATUSTIMERDW, (int) timer);
    }

    @Override
    public void setTransformedTo(LivingEntity to)
    {
        final int id = to == null ? -1 : to.getId();

        if (to instanceof ServerPlayer player)
        {
            NpcMob npc = EntityTypes.getNpc().create(to.level);
            npc.playerName = player.getGameProfile().getName();
            npc.setCustomNameVisible(false);
            npc.setCustomName(player.getDisplayName());
            to = npc;
        }

        PokeType type1 = this.getType1();
        PokeType type2 = this.getType2();

        if (id != -1)
        {
            final IPokemob pokemob = PokemobCaps.getPokemobFor(to);
            if (pokemob != null)
            {
                type1 = pokemob.getType1();
                type2 = pokemob.getType2();
            }
        }
        this.setType1(type1);
        this.setType2(type2);

        if (!this.getEntity().level.isClientSide())
        {
            final CompoundTag tag = new CompoundTag();
            if (to != null) to.saveWithoutId(tag);
            this.setCopiedNBT(tag);
        }
        final LivingEntity old = this.getCopiedMob();
        this.setCopiedID(id == -1 ? null : RegHelper.getKey(to));
        this.getCopy().onBaseTick(this.getEntity().level, this.getEntity());
        if (to != old && !this.getEntity().level.isClientSide())
            CapabilitySync.sendUpdate(this.getEntity(), PokemobMoves.TO_SYNC);
    }

    @Override
    public void setTargetFinder(final ITargetFinder tracker)
    {
        this.targetFinder = tracker;
    }

    @Override
    public ITargetFinder getTargetFinder()
    {
        if (this.targetFinder == null) return () -> {};
        return this.targetFinder;
    }

    @Override
    public int timeSinceCombat()
    {
        return this.timeSinceCombat;
    }

    @Override
    public void resetCombatTime()
    {
        this.timeSinceCombat = 0;
    }

    @Override
    public void tickTimeSinceCombat()
    {
        final boolean angry = this.getCombatState(CombatStates.BATTLING);
        if (angry && this.timeSinceCombat() < 0 || !angry && this.timeSinceCombat() > 0) this.resetCombatTime();
        if (angry) this.timeSinceCombat++;
        else this.timeSinceCombat--;
    }

    @Override
    public ResourceLocation getCopiedID()
    {
        return this.getCopy().getCopiedID();
    }

    @Override
    public LivingEntity getCopiedMob()
    {
        return this.getCopy().getCopiedMob();
    }

    @Override
    public CompoundTag getCopiedNBT()
    {
        return this.getCopy().getCopiedNBT();
    }

    @Override
    public void setCopiedID(final ResourceLocation id)
    {
        this.getCopy().setCopiedID(id);
    }

    @Override
    public void setCopiedMob(final LivingEntity mob)
    {
        this.getCopy().setCopiedMob(mob);
    }

    @Override
    public void setCopiedNBT(final CompoundTag tag)
    {
        this.getCopy().setCopiedNBT(tag);
    }

    @Override
    public ICopyMob getCopy()
    {
        return this.transformed;
    }

}
