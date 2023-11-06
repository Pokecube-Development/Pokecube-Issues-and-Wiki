package pokecube.core.ai.logic;

import java.util.Collection;
import java.util.List;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.IOngoingAffected;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.pokemob.ai.LogicStates;
import pokecube.api.items.IPokemobUseable;
import pokecube.api.moves.Battle;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.IMoveNames;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.impl.entity.impl.PersistantStatusEffect;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

/**
 * This applies ongoing moves, applies status effects, and manages sounds when
 * explosion moves are used. It also deals with setting/resetting the
 * transformed target accordingly, as well as ticking the abilities, and
 * activating the held item (like berries) if it should be used.
 */
public class LogicMovesUpdates extends LogicBase
{
    Vector3 v = new Vector3();
    int index = -1;
    int statusTick = 0;

    public LogicMovesUpdates(final IPokemob entity)
    {
        super(entity);
    }

    private void doExplosionChecks()
    {
        this.pokemob.getMoveStats().lastActiveTime = this.pokemob.getMoveStats().timeSinceIgnited;

        final int i = this.pokemob.getExplosionState();

        if (i > 0 && this.pokemob.getMoveStats().timeSinceIgnited == 0)
            this.entity.playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 0.5F);
        this.pokemob.getMoveStats().timeSinceIgnited += i;

        if (this.pokemob.getMoveStats().timeSinceIgnited < 0) this.pokemob.getMoveStats().timeSinceIgnited = 0;
        if (this.pokemob.getMoveStats().timeSinceIgnited > 50 && !BrainUtils.hasAttackTarget(this.entity))
        {
            this.pokemob.setExplosionState(-1);
            this.pokemob.getMoveStats().timeSinceIgnited--;

            if (this.pokemob.getMoveStats().timeSinceIgnited < 0) this.pokemob.getMoveStats().timeSinceIgnited = 0;
        }
    }

    public boolean hasMove(final String move)
    {
        for (final String s : this.pokemob.getMoves()) if (s != null && s.equalsIgnoreCase(move)) return true;
        return false;
    }

    @Override
    public void tick(final Level world)
    {
        super.tick(world);
        this.v.set(this.entity);

        String[] movesToUse = this.pokemob.getMoveStats().getMovesToUse();

        // Run tasks that only should go on server side.
        if (!world.isClientSide)
        {
            for (int i = 0; i < 4; i++)
            {
                final int timer = this.pokemob.getDisableTimer(i);
                if (timer > 0) this.pokemob.setDisableTimer(i, timer - 1);
            }

            if (this.pokemob.getMoveStats().DEFENSECURLCOUNTER > 0) this.pokemob.getMoveStats().DEFENSECURLCOUNTER--;
            if (this.pokemob.getMoveStats().SPECIALCOUNTER > 0) this.pokemob.getMoveStats().SPECIALCOUNTER--;

            this.updateStatusEffect();
            this.doExplosionChecks();

            // Reset move specific counters if the move index has changed.
            if (this.index != this.pokemob.getMoveIndex())
            {
                this.pokemob.getMoveStats().FURYCUTTERCOUNTER = 0;
                this.pokemob.getMoveStats().PHYSICALDAMAGETAKENCOUNTER = 0;
                this.pokemob.getMoveStats().SPECIALDAMAGETAKENCOUNTER = 0;
            }
            this.index = this.pokemob.getMoveIndex();

            if (this.pokemob.getMove(0) == null)
            {
                String move = IMoveNames.MOVE_TACKLE;
                final List<String> moves = this.pokemob.getPokedexEntry().getMovesForLevel(this.pokemob.getLevel());
                if (!moves.isEmpty()) move = moves.get(ThutCore.newRandom().nextInt(moves.size()));
                this.pokemob.learn(move);
            }

            // Server side if transformed checks

            LivingEntity transformed = this.pokemob.getTransformedTo();

            // Revert transform if not in battle or breeding.
            if (transformed != null)
            {
                // If we are not mating, and we are not in battle, transform
                // back.
                if (!(this.pokemob.getGeneralState(GeneralStates.MATING) || Battle.getBattle(entity) != null))
                {
                    this.pokemob.setTransformedTo(null);
                }
                // Otherwise set the move stats to the correct moves.
                else
                {
                    IPokemob toMob = PokemobCaps.getPokemobFor(transformed);
                    // This side has the appropriate caps for keeping the moves
                    // lists, so we sync this over.
                    if (toMob != null && this.pokemob.getMoveStats().transformId != transformed.getId())
                    {
                        this.pokemob.getMoveStats().transformId = transformed.getId();
                        System.arraycopy(toMob.getMoveStats().getMovesToUse(), 0, movesToUse, 0, movesToUse.length);
                    }
                }
            }
            else if (this.pokemob.getMoveStats().transformId != -1)
            {
                this.pokemob.getMoveStats().transformId = -1;
                System.arraycopy(pokemob.getMoveStats().getBaseMoves(), 0, movesToUse, 0, movesToUse.length);
            }
        }
        // client side only checks
        else
        {
            LivingEntity transformed = this.pokemob.getTransformedTo();
            IPokemob toMob = PokemobCaps.getPokemobFor(transformed);
            // This side has the appropriate caps for keeping the moves
            // lists, so we sync this over.
            if (toMob != null)
            {
                this.pokemob.getMoveStats().transformId = transformed.getId();
                System.arraycopy(toMob.getMoves(), 0, movesToUse, 0, movesToUse.length);
            }
            else if (this.pokemob.getMoveStats().transformId != -1)
            {
                this.pokemob.getMoveStats().transformId = -1;
                System.arraycopy(pokemob.getMoveStats().getBaseMoves(), 0, movesToUse, 0, movesToUse.length);
            }
        }

        // Run tasks that can be on server or client.

        // Update move cooldowns.
        final int num = this.pokemob.getAttackCooldown();

        this.pokemob.getMoveStats().checkMovesInProgress(this.pokemob);

        // Only reduce cooldown if the pokemob does not currently have a
        // move being fired.
        if (num > 0 && !this.pokemob.getMoveStats().isExecutingMoves()) this.pokemob.setAttackCooldown(num - 1);

        // Update abilities.
        if (this.pokemob.getAbility() != null && this.entity.isEffectiveAi())
            this.pokemob.getAbility().onUpdate(this.pokemob);

        // Tick held items.
        final ItemStack held = this.pokemob.getHeldItem();
        final IPokemobUseable usable = IPokemobUseable.getUsableFor(held);
        if (usable != null && this.entity.isAlive())
        {
            final InteractionResultHolder<ItemStack> result = usable.onTick(this.pokemob, held);
            if (result.getResult() == InteractionResult.SUCCESS) this.pokemob.setHeldItem(result.getObject());
            if (this.pokemob.getHeldItem().isEmpty()) this.pokemob.setHeldItem(ItemStack.EMPTY);
        }
    }

    protected void updateStatusEffect()
    {
        final int status = this.pokemob.getStatus();
        if (status == IMoveConstants.STATUS_NON)
        {
            if (this.pokemob.getLogicState(LogicStates.SLEEPING))
            {
                final int duration = 10;
                this.entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, duration * 2, 100));
                this.entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration * 2, 100));
            }
            return;
        }
        else
        {
            /**
             * Heals the status effects if the capability is soemhow removed,
             * yet it still thinks it has a status.
             */
            final IOngoingAffected affected = PokemobCaps.getAffected(this.pokemob.getEntity());
            if (affected == null) return;
            final Collection<?> set = affected.getEffects(PersistantStatusEffect.ID);
            if (set.isEmpty() && this.statusTick++ > 20)
            {
                PokecubeAPI.LOGGER
                        .error("Fixed Broken Status " + this.pokemob.getStatus() + " for " + this.pokemob.getEntity());
                this.statusTick = 0;
                this.pokemob.healStatus();
            }
            else if (!set.isEmpty()) this.statusTick = 0;
        }
    }
}
