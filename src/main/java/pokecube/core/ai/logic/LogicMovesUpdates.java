package pokecube.core.ai.logic;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemobUseable;
import pokecube.core.interfaces.capabilities.CapabilityAffected;
import pokecube.core.interfaces.entity.IOngoingAffected;
import pokecube.core.interfaces.entity.impl.PersistantStatusEffect;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import thut.api.maths.Vector3;

/**
 * This applies ongoing moves, applies status effects, and manages sounds when
 * explosion moves are used. It also deals with setting/resetting the
 * transformed target accordingly, as well as ticking the abilities, and
 * activating the held item (like berries) if it should be used.
 */
public class LogicMovesUpdates extends LogicBase
{
    Vector3 v          = Vector3.getNewVector();
    int     index      = -1;
    int     statusTick = 0;

    public LogicMovesUpdates(final IPokemob entity)
    {
        super(entity);
    }

    private void doExplosionChecks()
    {
        this.pokemob.getMoveStats().lastActiveTime = this.pokemob.getMoveStats().timeSinceIgnited;

        final int i = this.pokemob.getExplosionState();

        if (i > 0 && this.pokemob.getMoveStats().timeSinceIgnited == 0) this.entity.playSound(
                SoundEvents.ENTITY_CREEPER_PRIMED, 1.0F, 0.5F);
        this.pokemob.getMoveStats().timeSinceIgnited += i;

        if (this.pokemob.getMoveStats().timeSinceIgnited < 0) this.pokemob.getMoveStats().timeSinceIgnited = 0;
        if (!BrainUtils.hasAttackTarget(this.entity) && this.pokemob.getMoveStats().timeSinceIgnited > 50) //
        {
            this.pokemob.setExplosionState(-1);
            this.pokemob.getMoveStats().timeSinceIgnited--;

            if (this.pokemob.getMoveStats().timeSinceIgnited < 0) this.pokemob.getMoveStats().timeSinceIgnited = 0;
        }
    }

    public boolean hasMove(final String move)
    {
        for (final String s : this.pokemob.getMoves())
            if (s != null && s.equalsIgnoreCase(move)) return true;
        return false;
    }

    @Override
    public void tick(final World world)
    {
        super.tick(world);
        this.v.set(this.entity);

        // Run tasks that only should go on server side.
        if (!world.isRemote)
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

            if (this.pokemob.getMoves()[0] == null)
            {
                String move = IMoveNames.MOVE_TACKLE;
                final List<String> moves = this.pokemob.getPokedexEntry().getMovesForLevel(this.pokemob.getLevel());
                if (!moves.isEmpty()) move = moves.get(new Random().nextInt(moves.size()));
                this.pokemob.learn(move);
            }
        }

        // Run tasks that can be on server or client.

        // Update move cooldowns.
        final int num = this.pokemob.getAttackCooldown();

        // Check if active move is done, if so, clear it.
        if (this.pokemob.getActiveMove() != null && this.pokemob.getActiveMove().isDone()) this.pokemob.setActiveMove(
                null);

        // Only reduce cooldown if the pokemob does not currently have a
        // move being fired.
        if (num > 0 && this.pokemob.getActiveMove() == null) this.pokemob.setAttackCooldown(num - 1);

        // Revert transform if not in battle or breeding.
        if (this.pokemob.getTransformedTo() != null && !BrainUtils.hasAttackTarget(this.entity) && !this.pokemob
                .getGeneralState(GeneralStates.MATING)) this.pokemob.setTransformedTo(null);

        // Update abilities.
        if (this.pokemob.getAbility() != null && this.entity.isServerWorld()) this.pokemob.getAbility().onUpdate(
                this.pokemob);

        // Tick held items.
        final IPokemobUseable usable = IPokemobUseable.getUsableFor(this.pokemob.getHeldItem());
        if (this.entity.isAlive() && usable != null)
        {
            final ActionResult<ItemStack> result = usable.onTick(this.pokemob, this.pokemob.getHeldItem());
            if (result.getType() == ActionResultType.SUCCESS) this.pokemob.setHeldItem(result.getResult());
            if (this.pokemob.getHeldItem().isEmpty()) this.pokemob.setHeldItem(ItemStack.EMPTY);
        }
    }

    protected void updateStatusEffect()
    {
        final byte status = this.pokemob.getStatus();
        if (status == IMoveConstants.STATUS_NON)
        {
            if (this.pokemob.getLogicState(LogicStates.SLEEPING))
            {
                final int duration = 10;
                this.entity.addPotionEffect(new EffectInstance(Effects.BLINDNESS, duration * 2, 100));
                this.entity.addPotionEffect(new EffectInstance(Effects.WEAKNESS, duration * 2, 100));
            }
            return;
        }
        else
        {
            /**
             * Heals the status effects if the capability is soemhow removed,
             * yet it still thinks it has a status.
             */
            final IOngoingAffected affected = CapabilityAffected.getAffected(this.pokemob.getEntity());
            if (affected == null) return;
            final Collection<?> set = affected.getEffects(PersistantStatusEffect.ID);
            if (set.isEmpty() && this.statusTick++ > 20)
            {
                PokecubeCore.LOGGER.error("Fixed Broken Status " + this.pokemob.getStatus() + " for " + this.pokemob
                        .getEntity());
                this.statusTick = 0;
                this.pokemob.healStatus();
            }
            else if (!set.isEmpty()) this.statusTick = 0;
        }
    }
}
