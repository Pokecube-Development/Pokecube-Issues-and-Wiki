package pokecube.gimmicks.terastal;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.ICanEvolve;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.pokemob.commandhandlers.ChangeFormHandler;
import pokecube.api.entity.pokemob.commandhandlers.ChangeFormHandler.IChangeHandler;
import pokecube.api.events.init.PokemakeArgumentEvent;
import pokecube.api.events.pokemobs.ChangeForm;
import pokecube.api.events.pokemobs.HealEvent;
import pokecube.api.events.pokemobs.RecallEvent;
import pokecube.api.events.pokemobs.combat.MoveUse;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.raids.RaidManager;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.entity.genetics.GeneticsManager;
import pokecube.core.eventhandlers.PokemobEventsHandler.MegaEvoTicker;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.network.pokemobs.PacketSyncGene;
import pokecube.gimmicks.terastal.TeraTypeGene.TeraType;
import pokecube.mixin.accessors.AttributeMaxAccessor;
import thut.api.ThutCaps;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;
import thut.api.entity.genetics.GeneRegistry;
import thut.api.entity.genetics.IMobGenetics;
import thut.lib.TComponent;

/**
 * Implementation of the Terastallizing mechanic. This is tracked per pokemob
 * via genes, and this class contains the required code for attaching the genes,
 * managing the types, etc. This is all arranged via calling
 * {@link TerastalMechanic#init(FMLLoadCompleteEvent)}, and via the
 * EventBusSubscriber
 *
 */
@Mod.EventBusSubscriber(bus = Bus.MOD, modid = PokecubeCore.MODID)
public class TerastalMechanic
{

    /**
     * Setup and register tera type stuff.
     */
    @SubscribeEvent
    public static void init(FMLLoadCompleteEvent event)
    {
        // Register the genes
        GeneRegistry.register(TeraTypeGene.class);
        // Add listener for adding the STAB bonus when we are used.
        PokecubeAPI.MOVE_BUS.addListener(EventPriority.LOW, false, TerastalMechanic::duringPreMoveUse);
        // Add listener for removing tera when recalled
        PokecubeAPI.POKEMOB_BUS.addListener(TerastalMechanic::onRecall);
        // Add listener for removing tera cooldown when healed at a pokecenter
        PokecubeAPI.POKEMOB_BUS.addListener(TerastalMechanic::onHeal);
        // Add listener for removing tera cooldown when healed at a pokecenter
        PokecubeAPI.POKEMOB_BUS.addListener(TerastalMechanic::onPokemake);
        // Register a change handler for terastallizing
        ChangeFormHandler.addChangeHandler(new Terastallizer());
        // Register a raid type
        RaidManager.registerBossType(new TerastalRaid());

        var attr = Attributes.MAX_HEALTH;
        if (attr instanceof AttributeMaxAccessor acc && acc.maxValue() < 1e8) acc.setMaxValue(1e8);
    }

    private static class Terastallizer implements IChangeHandler
    {

        @Override
        public boolean handleChange(IPokemob pokemob)
        {
            return tryToggleTera(pokemob);
        }

        @Override
        public int getPriority()
        {
            return 200;
        }

        @Override
        public void onFail(IPokemob pokemob)
        {
            final LivingEntity owner = pokemob.getOwner();
            if (owner instanceof ServerPlayer player)
            {
                CompoundTag data = PokecubePlayerDataHandler.getCustomDataTag(pokemob.getOwnerId());
                int teraCooldown = data.getInt("pokecube:tera_cooldown");
                if (teraCooldown == 0) thut.lib.ChatHelper.sendSystemMessage(player,
                        TComponent.translatable("pokecube.mega.noring", pokemob.getDisplayName()));
                else if (teraCooldown > 0) thut.lib.ChatHelper.sendSystemMessage(player,
                        TComponent.translatable("pokemob.terastal.on_cooldown"));
                else thut.lib.ChatHelper.sendSystemMessage(player,
                        TComponent.translatable("pokemob.terastal.not_yet", pokemob.getDisplayName()));
            }
        }

        @Override
        public String changeKey()
        {
            return "terastal";
        }

    }

    /**
     * @param mob - to get TeraType for
     * @return the TeraType for the mob, null if not present.
     */
    @Nullable
    public static TeraType getTera(Entity mob)
    {
        Alleles<TeraType, Gene<TeraType>> genes = getTeraGenes(mob);
        if (genes == null) return null;
        return genes.getExpressed().getValue();
    }

    /**
     * 
     * @param entity - to get tera genes for
     * @return the tera genes for the entity, null if not present
     */
    @Nullable
    public static Alleles<TeraType, Gene<TeraType>> getTeraGenes(Entity entity)
    {

        final IMobGenetics genes = ThutCaps.getGenetics(entity);
        if (genes == null) return null;
        if (!genes.getKeys().contains(GeneticsManager.TERAGENE))
        {
            // Initialise it for the mob here.
            Alleles<TeraType, Gene<TeraType>> alleles = new Alleles<>(genes);
            Gene<TeraType> gene1 = new TeraTypeGene().mutate();
            Gene<TeraType> gene2 = new TeraTypeGene().mutate();

            IPokemob pokemob = PokemobCaps.getPokemobFor(entity);
            if (pokemob != null)
            {
                gene1.getValue().teraType = pokemob.getType1();
                gene2.getValue().teraType = pokemob.getType2();
                if (gene2.getValue().teraType == PokeType.unknown) gene2.getValue().teraType = pokemob.getType1();
            }
            alleles.setAllele(0, gene1);
            alleles.setAllele(1, gene2);
            alleles.getExpressed();
            genes.getAlleles().put(GeneticsManager.TERAGENE, alleles);
            if (entity.getLevel() instanceof ServerLevel) PacketSyncGene.syncGeneToTracking(entity, alleles);
        }
        try
        {
            Alleles<TeraType, Gene<TeraType>> alleles = genes.getAlleles(GeneticsManager.TERAGENE);
            if (alleles == null) return null;
            return alleles;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static void doTera(IPokemob pokemob)
    {
        Alleles<TeraType, Gene<TeraType>> genes = getTeraGenes(pokemob.getEntity());
        if (pokemob.isPlayerOwned())
        {
            CompoundTag data = PokecubePlayerDataHandler.getCustomDataTag(pokemob.getOwnerId());
            data.putInt("pokecube:tera_cooldown", 1);
            PokecubePlayerDataHandler.saveCustomData(pokemob.getOwnerId().toString());
        }
        Component mess = TComponent.translatable("pokemob.terastal.command.transform", pokemob.getDisplayName());
        pokemob.displayMessageToOwner(mess);
        mess = TComponent.translatable("pokemob.terastal.success", pokemob.getDisplayName());

        MegaEvoTicker.scheduleChange(PokecubeCore.getConfig().evolutionTicks, pokemob.getPokedexEntry(), pokemob, mess,
                () ->
                {
                    // Flag as evolving for animation effects
                    pokemob.setGeneralState(GeneralStates.EVOLVING, true);
                    pokemob.setGeneralState(GeneralStates.EXITINGCUBE, false);
                    pokemob.setEvolutionTicks(PokecubeCore.getConfig().evolutionTicks + 50);
                    pokemob.setEvolutionStack(PokecubeItems.getStack(ICanEvolve.EVERSTONE));
                    PokecubeAPI.POKEMOB_BUS.post(new ChangeForm.Pre(pokemob));
                    if (pokemob.isPlayerOwned())
                    {
                        CompoundTag data = PokecubePlayerDataHandler.getCustomDataTag(pokemob.getOwnerId());
                        data.putInt("pokecube:tera_cooldown", 1);
                        PokecubePlayerDataHandler.saveCustomData(pokemob.getOwnerId().toString());
                    }
                    genes.getExpressed().getValue().isTera = true;
                    PacketSyncGene.syncGeneToTracking(pokemob.getEntity(), genes);
                }, () -> {
                    PokecubeAPI.POKEMOB_BUS.post(new ChangeForm.Post(pokemob));
                    pokemob.setGeneralState(GeneralStates.EVOLVING, false);
                    pokemob.setEvolutionStack(ItemStack.EMPTY);
                });
    }

    /**
     * Marks the use of terastallization. This sets the pokemob's owner's
     * cooldown for using tera to 1, meaning they need to reset it somehow, say
     * via healing at a pokecenter, sleeping in a bed, or whatever else we set
     * to reset it.
     * 
     * @param pokemob - the pokemob trying to terastallize
     * @return whether we did terastallize
     */
    public static boolean tryToggleTera(IPokemob pokemob)
    {
        if (!(pokemob.getEntity().getLevel() instanceof ServerLevel)) return false;
        Alleles<TeraType, Gene<TeraType>> genes = getTeraGenes(pokemob.getEntity());
        if (genes == null) return false;
        if (genes.getExpressed().getValue().isTera)
        {
            Component mess = TComponent.translatable("pokemob.terastal.command.revert", pokemob.getDisplayName());
            pokemob.displayMessageToOwner(mess);
            mess = TComponent.translatable("pokemob.terastal.revert", pokemob.getDisplayName());
            MegaEvoTicker.scheduleChange(PokecubeCore.getConfig().evolutionTicks, pokemob.getPokedexEntry(), pokemob,
                    mess, () ->
                    {
                        // Flag as evolving for animation effects
                        pokemob.setGeneralState(GeneralStates.EVOLVING, true);
                        pokemob.setGeneralState(GeneralStates.EXITINGCUBE, false);
                        pokemob.setEvolutionTicks(PokecubeCore.getConfig().evolutionTicks + 50);
                        pokemob.setEvolutionStack(PokecubeItems.getStack(ICanEvolve.EVERSTONE));
                        PokecubeAPI.POKEMOB_BUS.post(new ChangeForm.Pre(pokemob));
                    }, () -> {
                        genes.getExpressed().getValue().isTera = false;
                        PacketSyncGene.syncGeneToTracking(pokemob.getEntity(), genes);
                        PokecubeAPI.POKEMOB_BUS.post(new ChangeForm.Post(pokemob));
                        pokemob.setGeneralState(GeneralStates.EVOLVING, false);
                        pokemob.setEvolutionStack(ItemStack.EMPTY);
                    });
            return true;
        }
        boolean canTera = !pokemob.isPlayerOwned();
        if (!canTera)
        {
            CompoundTag data = PokecubePlayerDataHandler.getCustomDataTag(pokemob.getOwnerId());
            canTera = data.getInt("pokecube:tera_cooldown") == 0;
        }
        if (canTera)
        {
            doTera(pokemob);
        }
        return canTera;
    }

    /**
     * This handles processing the tera type from the nbt in the pokemake
     * arguments.
     * 
     * @param event
     */
    private static final void onPokemake(PokemakeArgumentEvent event)
    {
        String type = event.getNbt().getString("tera_type");
        var tera = getTera(event.getPokemob().getEntity());
        if (!type.isBlank())
        {
            try
            {
                PokeType tera_type = PokeType.getType(type);
                if (tera != null) tera.teraType = tera_type;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }
        if (event.getNbt().getBoolean("is_tera"))
        {
            tera.isTera = true;
        }
    }

    /**
     * This clears the isTera state for the pokemob, ie breaks the
     * terastallization when recalled.
     */
    private static final void onHeal(HealEvent.Post event)
    {
        if (event.getContext().owner() instanceof ServerPlayer player && event.getContext().fromHealer())
        {
            CompoundTag data = PokecubePlayerDataHandler.getCustomDataTag(player);
            data.putInt("pokecube:tera_cooldown", 0);
            PokecubePlayerDataHandler.saveCustomData(player);
        }
    }

    /**
     * This clears the isTera state for the pokemob, ie breaks the
     * terastallization when recalled.
     */
    private static final void onRecall(RecallEvent.Post event)
    {
        TeraType type = getTera(event.recalled.getEntity());
        if (type != null && type.isTera)
        {
            type.isTera = false;
        }
    }

    /**
     * This applies the bonus STAB, and the damage boost for low powered moves.
     * It also increments a counter for the owner (if present), which is used to
     * determine if the owner can terastallize their pokemob.
     */
    private static final void duringPreMoveUse(MoveUse.DuringUse.Pre evt)
    {
        final MoveApplication move = evt.getPacket();
        final IPokemob attacker = move.getUser();
        TeraType tera = getTera(attacker.getEntity());

        // Here we apply the effects to the move when terastallized.
        if (tera != null && tera.isTera)
        {
            boolean originalType = move.type == attacker.originalType1() || move.type == attacker.originalType2();
            if (originalType) move.stab = true;
            if (attacker.isType(move.type)) move.stabFactor = 2.0f;
            if (move.pwr > 0 && move.pwr < 60) move.pwr = 60;
        }

        // Here we increment the tera counter if it is negative. Raids can set
        // it negative to require a delay before use.
        if (move.pwr > 0 && attacker.isPlayerOwned())
        {
            CompoundTag data = PokecubePlayerDataHandler.getCustomDataTag(attacker.getOwnerId());
            int c;
            if ((c = data.getInt("pokecube:tera_cooldown")) < 0)
            {
                data.putInt("pokecube:tera_cooldown", c + 1);
                PokecubePlayerDataHandler.saveCustomData(attacker.getOwnerId().toString());
            }
        }
    }

}
