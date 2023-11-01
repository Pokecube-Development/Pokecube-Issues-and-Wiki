package pokecube.api.entity.pokemob;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.PokedexEntry.EvolutionData;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityManager;
import pokecube.api.entity.pokemob.IPokemob.HappinessType;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.events.pokemobs.ChangeForm;
import pokecube.api.events.pokemobs.EvolveEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.eventhandlers.PokemobEventsHandler.EvoTicker;
import pokecube.core.moves.MovesUtils;
import pokecube.core.network.pokemobs.PokemobPacketHandler.MessageServer;
import pokecube.core.utils.EntityTools;
import thut.api.item.ItemList;
import thut.core.common.ThutCore;
import thut.core.common.network.EntityUpdate;
import thut.lib.TComponent;

public interface ICanEvolve extends IHasEntry, IHasOwner
{
    public static final ResourceLocation EVERSTONE = new ResourceLocation("pokecube:everstone");

    /**
     * Cancels the current evoluton for the pokemob, sends appropriate message
     * to owner.
     */
    default void cancelEvolve()
    {
        if (!this.isEvolving()) return;
        final LivingEntity entity = this.getEntity();
        if (this.getEntity().getLevel().isClientSide)
        {
            final MessageServer message = new MessageServer(MessageServer.CANCELEVOLVE, entity.getId());
            PokecubeCore.packets.sendToServer(message);
            return;
        }
        this.setEvolutionTicks(-1);
        this.setGeneralState(GeneralStates.EVOLVING, false);
        this.displayMessageToOwner(TComponent.translatable("pokemob.evolution.cancel",
                PokemobCaps.getPokemobFor(entity).getDisplayName()));
    }

    /**
     * Called when give item. to override when the pokemob evolve with a stone.
     *
     * @param itemId the shifted index of the item
     * @return whether should evolve
     */
    default boolean canEvolve(final ItemStack stack)
    {
        if (ItemList.is(ICanEvolve.EVERSTONE, stack)) return false;
        if (this.getPokedexEntry().canEvolve() && this.getEntity().isEffectiveAi())
            for (final EvolutionData d : this.getPokedexEntry().getEvolutions())
                if (d.shouldEvolve((IPokemob) this, stack)) return true;
        return false;
    }

    /**
     * Evolve the pokemob.
     *
     * @param delayed true if we want to display the evolution animation
     * @return the evolution or this if the evolution failed
     */
    default IPokemob evolve(final boolean delayed, final boolean init)
    {
        final IPokemob thisMob = (IPokemob) this;
        return this.evolve(delayed, init, thisMob.getHeldItem());
    }

    /**
     * Evolve the pokemob.
     *
     * @param delayed true if we want to display the evolution animation
     * @param init    true if this is called during initialization of the mob
     * @param stack   the itemstack to check for evolution.
     * @return the evolution or null if the evolution failed, or this if the
     *         evolution succeeded, but delayed.
     */
    default IPokemob evolve(final boolean delayed, final boolean init, final ItemStack stack)
    {
        final LivingEntity thisEntity = this.getEntity();
        final IPokemob thisMob = (IPokemob) this;
        if (thisEntity == null) return null;

        PokedexEntry evol = null;
        EvolutionData data = null;

        List<EvolutionData> valid = new ArrayList<>();

        // Find which evolution to use.
        for (final EvolutionData d : this.getPokedexEntry().getEvolutions())
            if (d.shouldEvolve(thisMob, stack)) valid.add(d);

        List<EvolutionData> select_from = new ArrayList<>();
        Set<EvolutionData> needed_items = new HashSet<>();
        // Now lets see if any need the item, but others do not.
        for (final EvolutionData d : valid)
        {
            if (!d.shouldEvolve(thisMob, ItemStack.EMPTY))
            {
                select_from.add(d);
                needed_items.add(d);
            }
        }
        if (select_from.isEmpty()) select_from.addAll(valid);
        if (select_from.isEmpty()) return null;

        if (select_from.size() > 1)
        {
            Collections.shuffle(select_from);
            select_from.sort(null);
        }

        data = select_from.get(0);
        evol = data.evolution;

        if (evol == null) return null;

        // If Init, then don't bother about getting ready for animations and
        // such, just evolve directly.
        if (init)
        {
            // Send evolve event.
            EvolveEvent evt = new EvolveEvent.Pre(thisMob, evol, data);
            PokecubeAPI.POKEMOB_BUS.post(evt);
            if (evt.isCanceled()) return null;

            // Determine if immediate
            boolean immediate = true;
            if (this.getEntity().isAddedToWorld() && this.getEntity().getLevel() instanceof ServerLevel level)
                immediate = !level.isHandlingTick();
            // change to new forme.
            final IPokemob evo = this.changeForm(((EvolveEvent.Pre) evt).forme, immediate, true);
            // Remove held item if it had one.
            if (needed_items.contains(data) && ItemStack.isSame(stack, thisMob.getHeldItem()))
                evo.setHeldItem(ItemStack.EMPTY);
            // Init things like moves.
            evo.getMoveStats().oldLevel = thisMob.getMoveStats().oldLevel;
            evo.levelUp(evo.getLevel());

            evo.setCustomHolder(data.data.getForme(evo.getPokedexEntry()));

            // Learn evolution moves and update ability.
            for (final String s : data.evoMoves) evo.learn(s);
            evo.setAbilityRaw(evo.getPokedexEntry().getAbility(thisMob.getAbilityIndex(), evo));

            // Send post evolve event.
            evt = new EvolveEvent.Post(evo);
            PokecubeAPI.POKEMOB_BUS.post(evt);

            // We are running on init, so we don't want these effects.
            evo.setGeneralState(GeneralStates.EVOLVING, false);
            evo.setEvolutionTicks(-1);
            return evo;
        }
        // Do not evolve if it is dead, or can't evolve.
        else if (this.getPokedexEntry().canEvolve() && thisEntity.isAlive())
        {
            EvolveEvent evt = new EvolveEvent.Pre(thisMob, evol, data);
            ThutCore.FORGE_BUS.post(evt);
            if (evt.isCanceled()) return null;
            if (delayed)
            {
                // If delayed, set the pokemob as starting to evolve, and
                // set the evolution for display effects.
                if (stack != ItemStack.EMPTY) this.setEvolutionStack(stack.copy());
                this.setEvolutionTicks(PokecubeCore.getConfig().evolutionTicks + 50);
                this.setEvolvingEffects(evol);
                this.setGeneralState(GeneralStates.EVOLVING, true);
                // Send the message about evolving, to let user cancel.
                this.displayMessageToOwner(
                        TComponent.translatable("pokemob.evolution.start", thisMob.getDisplayName()));
                return thisMob;
            }
            // Evolve the mob.
            final IPokemob evo = this.changeForm(((EvolveEvent.Pre) evt).forme, true, true);
            if (evo != null)
            {
                // Clear held item if used for evolving.
                if (needed_items.contains(data) && ItemStack.isSame(stack, thisMob.getHeldItem()))
                    evo.setHeldItem(ItemStack.EMPTY);

                evt = new EvolveEvent.Post(evo);
                ThutCore.FORGE_BUS.post(evt);
                // Lean any moves that should are supposed to have just
                // learnt.
                if (delayed) evo.getMoveStats().oldLevel = evo.getLevel() - 1;
                else if (data != null) evo.getMoveStats().oldLevel = thisMob.getMoveStats().oldLevel;
                evo.levelUp(evo.getLevel());

                evo.setBasePokedexEntry(evol);
                evo.setPokedexEntry(evol);
                evo.setCustomHolder(data.data.getForme(evo.getPokedexEntry()));

                // Don't immediately try evolving again, only wild ones
                // should do that.
                evo.setEvolutionTicks(-1);
                evo.setGeneralState(GeneralStates.EVOLVING, false);

                // Learn evolution moves and update ability.
                for (final String s : data.evoMoves) evo.learn(s);
                evo.setAbilityRaw(evo.getPokedexEntry().getAbility(thisMob.getAbilityIndex(), evo));
            }
            return evo;
        }
        return null;
    }

    /** This entry is used for colouring evolution effects. */
    default PokedexEntry getEvolutionEntry()
    {
        return this.getPokedexEntry();
    }

    /**
     * This is the itemstack we are using for evolution, it is stored here for
     * use when evolution actually occurs.
     */
    ItemStack getEvolutionStack();

    /** @return if we are currently evolving */
    default boolean isEvolving()
    {
        return this.getGeneralState(GeneralStates.EVOLVING);
    }

    /**
     * Called when the level is up. Should be overridden to handle level up
     * events like evolution or move learning.
     *
     * @param level the new level
     */
    default IPokemob levelUp(final int level)
    {
        final LivingEntity theEntity = this.getEntity();
        final IPokemob theMob = (IPokemob) this;
        final List<String> moves = Database.getLevelUpMoves(theMob.getPokedexEntry(), level,
                theMob.getMoveStats().oldLevel);
        Collections.shuffle(moves);
        if (!theEntity.getLevel().isClientSide)
        {
            final Component mess = TComponent.translatable("pokemob.info.levelup", theMob.getDisplayName(), level + "");
            theMob.displayMessageToOwner(mess);
        }
        HappinessType.applyHappiness(theMob, HappinessType.LEVEL);
        if (moves != null)
        {
            if (theMob.getGeneralState(GeneralStates.TAMED))
            {
                if (theMob.getMove(theMob.getMovesCount() - 1) != null)
                {
                    for (int i = 0; i < theMob.getMovesCount(); i++)
                    {
                        String s = theMob.getMove(i);
                        if (s == null) continue;
                        for (final String s1 : moves) if (s.equals(s1))
                        {
                            moves.remove(s1);
                            break;
                        }
                    }
                    for (final String s : moves)
                    {
                        final Component move = TComponent.translatable(MovesUtils.getUnlocalizedMove(s));
                        final Component mess = TComponent.translatable("pokemob.move.notify.learn",
                                theMob.getDisplayName(), move);
                        theMob.displayMessageToOwner(mess);
                        theMob.getMoveStats().addPendingMove(s, theMob);
                    }
                    EntityUpdate.sendEntityUpdate(this.getEntity());
                    return theMob;
                }
            }
            for (final String s : moves) theMob.learn(s);
        }
        return theMob;
    }

    default IPokemob changeForm(final PokedexEntry newEntry)
    {
        if (this.getEntity().getLevel() instanceof ServerLevel level)
            return this.changeForm(newEntry, !level.isHandlingTick(), false);
        return this.changeForm(newEntry, true, false);
    }

    default IPokemob resetForm(boolean onRecall)
    {
        PokecubeAPI.POKEMOB_BUS.post(new ChangeForm.Revert((IPokemob) this, onRecall));
        this.setPokedexEntry(getBasePokedexEntry());
        PokecubeAPI.POKEMOB_BUS.post(new ChangeForm.Post((IPokemob) this));
        return (IPokemob) this;
    }

    /**
     * Converts us to the given entry
     *
     * @param newEntry new pokedex entry to have
     * @return the new pokemob, return this if it fails
     */
    default IPokemob changeForm(PokedexEntry newEntry, boolean immediate, boolean permanent)
    {
        final LivingEntity thisEntity = this.getEntity();
        final IPokemob thisMob = (IPokemob) this;
        if (thisEntity == null) return thisMob;
        LivingEntity evolution = thisEntity;
        IPokemob evoMob = thisMob;
        final PokedexEntry oldEntry = this.getPokedexEntry();
        if (newEntry != null && newEntry != oldEntry)
        {
            this.setGeneralState(GeneralStates.EVOLVING, true);
            if (permanent)
            {
                evolution = PokecubeCore.createPokemob(newEntry, thisEntity.getLevel());
                if (evolution == null)
                {
                    PokecubeAPI.LOGGER.warn("No Entry for " + newEntry);
                    return thisMob;
                }
            }
            evoMob = PokemobCaps.getPokemobFor(evolution);
            // Reset nickname if needed.
            if (this.getPokemonNickname().equals(oldEntry.getName())) evoMob.setPokemonNickname("");

            if (permanent)
            {
                final int id = evolution.getId();
                final UUID uuid = evolution.getUUID();
                // Copy NBT data over
                evolution.load(thisEntity.saveWithoutId(new CompoundTag()));
                // Copy transforms over.
                EntityTools.copyEntityTransforms(evolution, thisEntity);
                evolution.setId(id);
                evolution.setUUID(uuid);

                // Sync over any active moves
                thisMob.getMoveStats().changeMovesUser(evoMob);
            }

            // Flag the mob as evolving.
            evoMob.setGeneralState(GeneralStates.EVOLVING, true);

            evoMob.onGenesChanged();

            // Set entry, this should fix expressed species gene.
            evoMob.setPokedexEntry(newEntry);

            // Remove this tag if present.
            evolution.getPersistentData().remove("pokecube:mega_base");

            // Sync ability back, or store old ability.
            if (!permanent)
            {
                if (thisEntity.getPersistentData().contains("pokecube:mega_ability"))
                {
                    final String ability = thisEntity.getPersistentData().getString("pokecube:mega_ability");
                    evolution.getPersistentData().remove("pokecube:mega_ability");
                    if (!ability.isEmpty()) evoMob.setAbilityRaw(AbilityManager.getAbility(ability));
                    if (PokecubeCore.getConfig().debug_ai)
                        PokecubeAPI.logInfo("Un Mega Evolving, changing ability back to " + ability);
                }
                else
                {
                    if (thisMob.getAbility() != null) evolution.getPersistentData().putString("pokecube:mega_ability",
                            thisMob.getAbility().toString());
                    evolution.getPersistentData().putString("pokecube:mega_base", oldEntry.getTrimmedName());
                    final Ability ability = newEntry.getAbility(0, evoMob);
                    if (PokecubeCore.getConfig().debug_ai)
                        PokecubeAPI.logInfo("Mega Evolving, changing ability to " + ability);
                    if (ability != null) evoMob.setAbilityRaw(ability);
                }
            }

            final EvolveEvent evt = new EvolveEvent.Post(evoMob);
            PokecubeAPI.POKEMOB_BUS.post(evt);
            // Schedule adding to world.
            if (!evt.isCanceled() && thisEntity.isAddedToWorld())
                EvoTicker.scheduleEvolve(thisEntity, evolution, immediate);
        }
        return evoMob;
    }

    /**
     * This itemstack will be used to evolve the pokemob after evolutionTicks
     * runs out.
     */
    void setEvolutionStack(ItemStack stack);

    /**
     * The evolution tick will be set when the mob evolves and then is decreased
     * each tick. It is used to render a special effect.
     *
     * @param evolutionTicks the evolutionTicks to set
     */
    void setEvolutionTicks(int evolutionTicks);

    /** Can set a custom entry for use with colouring the evolution effects. */
    default void setEvolvingEffects(final PokedexEntry entry)
    {

    }

    /**
     * This gets called to notifiy of a dynamax that requires an HP update.
     */
    void updateHealth();
}
