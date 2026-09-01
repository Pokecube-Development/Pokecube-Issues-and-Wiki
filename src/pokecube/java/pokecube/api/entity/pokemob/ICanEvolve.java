package pokecube.api.entity.pokemob;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
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
import pokecube.core.entity.genetics.GeneticsManager;
import pokecube.core.entity.genetics.genes.AbilityGene;
import pokecube.core.entity.genetics.genes.SpeciesGene;
import pokecube.core.eventhandlers.PokemobEventsHandler.EvoTicker;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.damage.attributes.PokecubeAttributes;
import pokecube.core.network.pokemobs.PokemobPacketHandler.MessageServer;
import pokecube.core.utils.EntityTools;
import thut.api.entity.genetics.Alleles;
import thut.api.item.ItemList;
import thut.core.common.ThutCore;
import thut.core.common.network.EntityUpdate;

public interface ICanEvolve extends IHasEntry, IHasOwner
{
    public static final ResourceLocation EVERSTONE = ResourceLocation.parse("pokecube:everstone");

    /**
     * Cancels the current evoluton for the pokemob, sends appropriate message to owner.
     */
    default void cancelEvolve()
    {
        if (!this.isEvolving()) return;
        final LivingEntity entity = this.getEntity();
        if (this.getEntity().level().isClientSide)
        {
            final MessageServer message = new MessageServer(MessageServer.CANCELEVOLVE, entity.getId());
            PokecubeCore.packets.sendToServer(message);
            return;
        }
        this.setEvolutionTicks(-1);
        this.setGeneralState(GeneralStates.EVOLVING, false);
        this.displayMessageToOwner(Component.translatableEscape("pokemob.evolution.cancel",
                PokemobCaps.getPokemobFor(entity).getDisplayName()));
    }

    /**
     * Called when give item. to override when the pokemob evolve with a stone.
     *
     * @param stack the shifted index of the item
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
    default boolean evolve(final boolean delayed)
    {
        final IPokemob thisMob = (IPokemob) this;
        return this.evolve(delayed, thisMob.getHeldItem());
    }

    /**
     * Evolve the pokemob.
     *
     * @param delayed true if we want to display the evolution animation
     * @param stack   the itemstack to check for evolution.
     * @return the evolution or null if the evolution failed, or this if the evolution succeeded, but delayed.
     */
    default boolean evolve(final boolean delayed, final ItemStack stack)
    {
        final LivingEntity thisEntity = this.getEntity();
        final IPokemob thisMob = (IPokemob) this;
        if (thisEntity == null) return false;
        // Do not evolve can't evolve.
        if(!this.getPokedexEntry().canEvolve()) return false;
        // Do not evolve delayed if dead
        if (delayed && !thisEntity.isAlive()) return false;

        PokedexEntry evol;
        EvolutionData data;

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
        if (select_from.isEmpty()) return false;
        if (select_from.size() > 1)
        {
            Collections.shuffle(select_from);
            select_from.sort(null);
        }
        data = select_from.getFirst();
        evol = data.evolution;

        if (evol == null) return false;
        EvolveEvent evt = new EvolveEvent.Pre(thisMob, evol, data);
        ThutCore.FORGE_BUS.post(evt);
        if (evt.isCanceled()) return false;
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
                    Component.translatableEscape("pokemob.evolution.start", thisMob.getDisplayName()));
            return true;
        }
        // Evolve the mob.
        boolean evolved = this.changeForm(((EvolveEvent.Pre) evt).forme, true, true);
        if (evolved)
        {
            // Clear held item if used for evolving.
            if (needed_items.contains(data) && ItemStack.isSameItem(stack, thisMob.getHeldItem()))
                thisMob.setHeldItem(ItemStack.EMPTY);
            // Lean any moves that should are supposed to have just learnt.
            thisMob.levelUp(thisMob.getLevel());

            thisMob.setBasePokedexEntry(evol);
            thisMob.setPokedexEntry(evol);
            thisMob.setCustomHolder(data.data.getForme(thisMob.getPokedexEntry()));

            // Don't immediately try evolving again, only wild ones
            // should do that.
            thisMob.setEvolutionTicks(-1);
            thisMob.setGeneralState(GeneralStates.EVOLVING, false);

            // Learn evolution moves and update ability.
            for (final String s : data.evoMoves) thisMob.learn(s);
            thisMob.setAbilityRaw(thisMob.getPokedexEntry().getAbility(thisMob.getAbilityIndex(), thisMob));

            // Send post evolve event.
            evt = new EvolveEvent.Post(thisMob);
            ThutCore.FORGE_BUS.post(evt);

            return true;
        }
        return false;
    }

    /** This entry is used for colouring evolution effects. */
    default PokedexEntry getEvolutionEntry()
    {
        return this.getPokedexEntry();
    }

    /**
     * This is the itemstack we are using for evolution, it is stored here for use when evolution actually occurs.
     */
    ItemStack getEvolutionStack();

    /** @return if we are currently evolving */
    default boolean isEvolving()
    {
        return this.getGeneralState(GeneralStates.EVOLVING);
    }

    /**
     * Called when the level is up. Should be overridden to handle level up events like evolution or move learning.
     *
     * @param level the new level
     */
    default void levelUp(final int level)
    {
        final LivingEntity theEntity = this.getEntity();
        final IPokemob theMob = (IPokemob) this;
        final List<String> moves = Database.getLevelUpMoves(theMob.getPokedexEntry(), level,
                theMob.getMoveStats().oldLevel);
        Collections.shuffle(moves);
        if (!theEntity.level().isClientSide)
        {
            final Component mess = Component.translatableEscape("pokemob.info.levelup", theMob.getDisplayName(), level + "");
            theMob.displayMessageToOwner(mess);
        }
        PokecubeAttributes.resetToEntry((IPokemob) this);
        HappinessType.applyHappiness(theMob, HappinessType.LEVEL);
        if (theMob.getGeneralState(GeneralStates.TAMED))
        {
            if (theMob.getMove(theMob.getMovesCount() - 1) != null)
            {
                for (int i = 0; i < theMob.getMovesCount(); i++)
                {
                    String s = theMob.getMove(i);
                    if (s == null) continue;
                    for (final String s1 : moves)
                        if (s.equals(s1))
                        {
                            moves.remove(s1);
                            break;
                        }
                }
                for (final String s : moves)
                {
                    final Component move = Component.translatable(MovesUtils.getUnlocalizedMove(s));
                    final Component mess = Component.translatableEscape("pokemob.move.notify.learn",
                            theMob.getDisplayName(), move);
                    theMob.displayMessageToOwner(mess);
                    theMob.getMoveStats().addPendingMove(s, theMob);
                }
                if(this.getEntity().isAddedToLevel()) EntityUpdate.sendEntityUpdate(this.getEntity());
                return;
            }
        }
        for (final String s : moves) theMob.learn(s);
    }

    default boolean changeForm(final PokedexEntry newEntry)
    {
        if (this.getEntity().level() instanceof ServerLevel level)
            return this.changeForm(newEntry, !level.isHandlingTick(), false);
        return this.changeForm(newEntry, true, false);
    }

    default boolean resetForm(boolean onRecall)
    {
        PokedexEntry entry = this.getPokedexEntry();
        PokecubeAPI.POKEMOB_BUS.post(new ChangeForm.Revert((IPokemob) this, onRecall));
        this.setPokedexEntry(getBasePokedexEntry());
        PokecubeAPI.POKEMOB_BUS.post(new ChangeForm.Post((IPokemob) this));
        return entry != this.getPokedexEntry();
    }

    /**
     * Converts us to the given entry. If this succeeds, the result of getEntity() may differ after
     * the evolution.
     *
     * @param newEntry new pokedex entry to have
     * @return whether we did change.
     */
    default boolean changeForm(PokedexEntry newEntry, boolean immediate, boolean permanent)
    {
        final LivingEntity thisEntity = this.getEntity();
        final IPokemob thisMob = (IPokemob) this;
        if (thisEntity == null) return false;
        LivingEntity evolution = thisEntity;
        final PokedexEntry oldEntry = this.getPokedexEntry();

        Alleles<AbilityGene.AbilityObject, AbilityGene> geneAbility = thisMob.getGenes().getAlleles(GeneticsManager.ABILITYGENE);
        final AbilityGene abilityGene = geneAbility.getExpressed();
        Ability abilityObject = abilityGene.getValue().abilityObject;
        Ability specialAbility = null; // For pokemobs with abilities like battle bond that aren't their normal or hidden abilities

        Alleles<SpeciesGene.SpeciesInfo, SpeciesGene> genesSpecies = thisMob.getGenes().getAlleles(GeneticsManager.SPECIESGENE);
        SpeciesGene.SpeciesInfo speciesGene = genesSpecies.getExpressed().getValue();

        if (abilityObject != null)
        {
            // If the ability is not in the current or new entry, then keep it.
            if (thisEntity.getPersistentData().contains("pokecube:special_ability") || (!oldEntry.abilities.contains(abilityObject.toString()) && !oldEntry.abilitiesHidden.contains(abilityObject.toString())))
            {
                specialAbility = abilityObject;
                if (PokecubeCore.getConfig().debug_ai)
                    PokecubeAPI.logInfo(abilityObject + " on " + thisMob.getDisplayName().getString() + " is a special ability, keeping it");
                thisEntity.getPersistentData().putBoolean("pokecube:special_ability", true);
            }
        }
        else if (PokecubeCore.getConfig().debug_ai)
            PokecubeAPI.logInfo("Skipping special ability checks as ability is null");

        if (newEntry != null && newEntry != oldEntry)
        {
            this.setGeneralState(GeneralStates.EVOLVING, true);
            if (permanent)
            {
                var owner = thisMob.getOwnerId();
                var ownerE = thisMob.getOwner();
                var ownerP = thisMob.isPlayerOwned();
                evolution = PokecubeCore.createPokemob(newEntry, thisEntity.level());
                if (!(evolution instanceof Mob mob))
                {
                    PokecubeAPI.LOGGER.warn("invalid entry for {} during evolution", newEntry);
                    return false;
                }
                // Load raw data first
                if(evolution != thisEntity)
                {
                    evolution.load(thisEntity.saveWithoutId(new CompoundTag()));
                    // Sync attachments
                    mob.copyAttachmentsFrom(thisEntity, false);

                    // Copy transforms over.
                    EntityTools.copyEntityTransforms(evolution, thisEntity);
                }
                this.setEntity(mob, true);

                // Set permanent entry
                speciesGene.setEntry(newEntry);
                // sync these separately, as often are linked to the Tameable itself
                if(ownerE!=null) this.setOwner(ownerE);
                else this.setOwner(owner);
                this.setPlayerOwned(ownerP);
            }
            // Reset nickname if needed.
            if (this.getPokemonNickname().equals(oldEntry.getName())) thisMob.setPokemonNickname("");

            // Flag the mob as evolving.
            thisMob.setGeneralState(GeneralStates.EVOLVING, true);

            // Remove this tag if present.
            evolution.getPersistentData().remove("pokecube:mega_base");

            // Sync ability back, or store old ability.
            if (!permanent)
            {
                if (thisEntity.getPersistentData().contains("pokecube:mega_ability"))
                {
                    final String ability = thisEntity.getPersistentData().getString("pokecube:mega_ability");
                    evolution.getPersistentData().remove("pokecube:mega_ability");
                    if (!ability.isEmpty()) thisMob.setAbilityRaw(AbilityManager.getAbility(ability));
                    if (PokecubeCore.getConfig().debug_ai)
                        PokecubeAPI.logInfo("Un Mega Evolving, changing ability back to " + ability);
                }
                else
                {
                    if (thisMob.getAbility() != null) evolution.getPersistentData()
                            .putString("pokecube:mega_ability", thisMob.getAbility().toString());
                    evolution.getPersistentData().putString("pokecube:mega_base", oldEntry.getTrimmedName());
                    final Ability ability = newEntry.getAbility(0, thisMob);
                    if (PokecubeCore.getConfig().debug_ai)
                        PokecubeAPI.logInfo("Mega Evolving, changing ability to " + ability);
                    if (ability != null) thisMob.setAbilityRaw(ability);
                }

                // Set temporary entry
                speciesGene.setTmpEntry(newEntry);
            }

            final EvolveEvent evt = new EvolveEvent.Post(thisMob);
            PokecubeAPI.POKEMOB_BUS.post(evt);
            // Schedule adding to world.
            if (!evt.isCanceled() && thisEntity.isAddedToLevel())
                EvoTicker.scheduleEvolve(thisEntity, evolution, immediate);
        }
        if (specialAbility != null)
        {
            thisMob.setAbilityRaw(specialAbility);
        }
        return true;
    }

    /**
     * This itemstack will be used to evolve the pokemob after evolutionTicks runs out.
     */
    void setEvolutionStack(ItemStack stack);

    /**
     * The evolution tick will be set when the mob evolves and then is decreased each tick. It is used to render a
     * special effect.
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
