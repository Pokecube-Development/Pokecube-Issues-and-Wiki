package pokecube.api.entity.pokemob;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.PokedexEntry.EvolutionData;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityManager;
import pokecube.api.entity.pokemob.IPokemob.HappinessType;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.events.pokemobs.EvolveEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.eventhandlers.PokemobEventsHandler.EvoTicker;
import pokecube.core.eventhandlers.PokemobEventsHandler.MegaEvoTicker;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.animations.EntityMoveUse;
import pokecube.core.network.pokemobs.PacketSyncNewMoves;
import pokecube.core.network.pokemobs.PokemobPacketHandler.MessageServer;
import pokecube.core.utils.EntityTools;
import thut.api.item.ItemList;
import thut.core.common.network.EntityUpdate;
import thut.lib.TComponent;

public interface ICanEvolve extends IHasEntry, IHasOwner
{
    public static final ResourceLocation EVERSTONE = new ResourceLocation("pokecube:everstone");

    /**
     * Shedules mega evolution for a few ticks later
     *
     * @param evolver the mob to schedule to evolve
     * @param newForm the form to evolve to
     * @param message the message to send on completion
     */
    public static void setDelayedMegaEvolve(final IPokemob evolver, final PokedexEntry newForm, final Component message)
    {
        ICanEvolve.setDelayedMegaEvolve(evolver, newForm, message, false);
    }

    /**
     * Shedules mega evolution for a few ticks later
     *
     * @param evolver    the mob to schedule to evolve
     * @param newForm    the form to evolve to
     * @param message    the message to send on completion
     * @param dynamaxing tif true, will set dynamax flag when completed.
     */
    public static void setDelayedMegaEvolve(final IPokemob evolver, final PokedexEntry newForm, final Component message,
            final boolean dynamaxing)
    {
        if (!(evolver.getEntity().level instanceof ServerLevel)) return;
        new MegaEvoTicker(newForm, PokecubeCore.getConfig().evolutionTicks / 2, evolver, message, dynamaxing);
    }

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
        final LivingEntity thisEntity = this.getEntity();
        final IPokemob thisMob = PokemobCaps.getPokemobFor(thisEntity);
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
        final IPokemob thisMob = PokemobCaps.getPokemobFor(thisEntity);
        // If Init, then don't bother about getting ready for animations and
        // such, just evolve directly.
        if (init)
        {
            boolean neededItem = false;
            PokedexEntry evol = null;
            EvolutionData data = null;
            // Find which evolution to use.
            for (final EvolutionData d : this.getPokedexEntry().getEvolutions()) if (d.shouldEvolve(thisMob, stack))
            {
                evol = d.evolution;
                if (!d.shouldEvolve(thisMob, ItemStack.EMPTY)) neededItem = true;
                data = d;
                break;
            }
            if (evol != null)
            {
                // Send evolve event.
                EvolveEvent evt = new EvolveEvent.Pre(thisMob, evol, data);
                PokecubeAPI.POKEMOB_BUS.post(evt);
                if (evt.isCanceled()) return null;
                // change to new forme.
                final IPokemob evo = this.megaEvolve(((EvolveEvent.Pre) evt).forme);
                // Remove held item if it had one.
                if (neededItem && stack == thisMob.getHeldItem()) evo.setHeldItem(ItemStack.EMPTY);
                // Init things like moves.
                evo.getMoveStats().oldLevel = data.level - 1;
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
            return null;
        }
        // Do not evolve if it is dead, or can't evolve.
        else if (this.getPokedexEntry().canEvolve() && thisEntity.isAlive())
        {
            boolean neededItem = false;
            PokedexEntry evol = null;
            EvolutionData data = null;
            // look for evolution data to use.
            for (final EvolutionData d : this.getPokedexEntry().getEvolutions()) if (d.shouldEvolve(thisMob, stack))
            {
                evol = d.evolution;
                if (!d.shouldEvolve(thisMob, ItemStack.EMPTY) && stack == thisMob.getHeldItem()) neededItem = true;
                data = d;
                break;
            }
            if (evol != null)
            {
                EvolveEvent evt = new EvolveEvent.Pre(thisMob, evol, data);
                MinecraftForge.EVENT_BUS.post(evt);
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
                final IPokemob evo = this.megaEvolve(((EvolveEvent.Pre) evt).forme);
                if (evo != null)
                {
                    // Clear held item if used for evolving.
                    if (neededItem) evo.setHeldItem(ItemStack.EMPTY);
                    evt = new EvolveEvent.Post(evo);
                    MinecraftForge.EVENT_BUS.post(evt);
                    // Lean any moves that should are supposed to have just
                    // learnt.
                    if (delayed) evo.getMoveStats().oldLevel = evo.getLevel() - 1;
                    else if (data != null) evo.getMoveStats().oldLevel = data.level - 1;
                    evo.levelUp(evo.getLevel());

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
        final IPokemob theMob = PokemobCaps.getPokemobFor(theEntity);
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
                final String[] current = theMob.getMoves();
                if (current[3] != null)
                {
                    for (final String s : current)
                    {
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
                        if (!theMob.getMoveStats().newMoves.contains(s))
                        {
                            theMob.getMoveStats().newMoves.add(s);
                            PacketSyncNewMoves.sendUpdatePacket((IPokemob) this);
                        }
                    }
                    EntityUpdate.sendEntityUpdate(this.getEntity());
                    return theMob;
                }
            }
            for (final String s : moves) theMob.learn(s);
        }
        return theMob;
    }

    default IPokemob megaEvolve(final PokedexEntry newEntry)
    {
        if (this.getEntity().getLevel() instanceof ServerLevel level)
            return this.megaEvolve(newEntry, !level.isHandlingTick());
        return this.megaEvolve(newEntry, true);
    }

    default PokedexEntry getMegaBase()
    {
        final PokedexEntry entry = this.getPokedexEntry();
        if (!(this.getCombatState(CombatStates.MEGAFORME) || entry.isMega())) return entry;
        PokedexEntry prev = Database.getEntry(this.getEntity().getPersistentData().getString("pokecube:mega_base"));
        if (prev == null || prev == Database.missingno) prev = entry.getBaseForme();
        if (prev == null) return entry;
        return prev;
    }

    default IPokemob megaRevert()
    {
        if (!(this.getCombatState(CombatStates.MEGAFORME) || this.getPokedexEntry().isMega())) return (IPokemob) this;
        final PokedexEntry entry = this.getPokedexEntry();
        final PokedexEntry prev = this.getMegaBase();
        this.setCombatState(CombatStates.MEGAFORME, false);
        if (prev != entry) return this.megaEvolve(prev);
        return (IPokemob) this;
    }

    /**
     * Converts us to the given entry
     *
     * @param newEntry new pokedex entry to have
     * @return the new pokemob, return this if it fails
     */
    default IPokemob megaEvolve(final PokedexEntry newEntry, final boolean immediate)
    {
        final LivingEntity thisEntity = this.getEntity();
        final IPokemob thisMob = (IPokemob) this;
        LivingEntity evolution = thisEntity;
        IPokemob evoMob = thisMob;
        final PokedexEntry oldEntry = this.getPokedexEntry();
        if (newEntry != null && newEntry != oldEntry)
        {
            this.setGeneralState(GeneralStates.EVOLVING, true);

            evolution = PokecubeCore.createPokemob(newEntry, thisEntity.getLevel());
            if (evolution == null)
            {
                PokecubeAPI.LOGGER.warn("No Entry for " + newEntry);
                return thisMob;
            }
            final int id = evolution.getId();
            final UUID uuid = evolution.getUUID();
            evoMob = PokemobCaps.getPokemobFor(evolution);
            // Reset nickname if needed.
            if (this.getPokemonNickname().equals(oldEntry.getName())) this.setPokemonNickname("");

            // Copy NBT data over
            evolution.load(thisEntity.saveWithoutId(new CompoundTag()));

            // Copy transforms over.
            EntityTools.copyEntityTransforms(evolution, thisEntity);
            evolution.setId(id);
            evolution.setUUID(uuid);

            // Sync over any active moves
            if (thisMob.getActiveMove() != null)
            {
                final EntityMoveUse move = thisMob.getActiveMove();
                evoMob.setActiveMove(move);
                move.setUser(evolution);
            }

            // Flag the mob as evolving.
            evoMob.setGeneralState(GeneralStates.EVOLVING, true);

            GeneticsManager.handleEpigenetics(evoMob);

            evoMob.onGenesChanged();

            // Set entry, this should fix expressed species gene.
            evoMob.setPokedexEntry(newEntry);

            // Remove this tag if present.
            evolution.getPersistentData().remove("pokecube:mega_base");

            // Sync ability back, or store old ability.
            if (evoMob.getPokedexEntry().isMega())
            {
                if (thisMob.getAbility() != null)
                    evolution.getPersistentData().putString("pokecube:mega_ability", thisMob.getAbility().toString());
                evolution.getPersistentData().putString("pokecube:mega_base", oldEntry.getTrimmedName());
                final Ability ability = newEntry.getAbility(0, evoMob);
                PokecubeAPI.LOGGER.debug("Mega Evolving, changing ability to " + ability);

                if (ability != null) evoMob.setAbilityRaw(ability);
            }
            else if (thisEntity.getPersistentData().contains("pokecube:mega_ability"))
            {
                final String ability = thisEntity.getPersistentData().getString("pokecube:mega_ability");
                evolution.getPersistentData().remove("pokecube:mega_ability");
                if (!ability.isEmpty()) evoMob.setAbilityRaw(AbilityManager.getAbility(ability));
                PokecubeAPI.LOGGER.debug("Un Mega Evolving, changing ability back to " + ability);
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
     * This scales the max health of the pokemob when it is dynamaxed or
     * gigantamaxed
     *
     * @return
     */
    float getDynamaxFactor();

    /**
     * This scales the max health of the pokemob when it is dynamaxed or
     * gigantamaxed
     *
     * @return
     */
    void setDynamaxFactor(float factor);

    /**
     * This gets called to notifiy of a dynamax that requires an HP update.
     */
    void updateHealth();
}
