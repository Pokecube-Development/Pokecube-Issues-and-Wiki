package pokecube.core.gimmicks.terastal;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.events.pokemobs.RecallEvent;
import pokecube.api.events.pokemobs.combat.MoveUse;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.gimmicks.terastal.TeraTypeGene.TeraType;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.network.pokemobs.PacketSyncGene;
import thut.api.ThutCaps;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;
import thut.api.entity.genetics.GeneRegistry;
import thut.api.entity.genetics.IMobGenetics;

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

        final IMobGenetics genes = entity.getCapability(ThutCaps.GENETICS_CAP, null).orElse(null);
        if (genes == null) return null;
        if (!genes.getKeys().contains(GeneticsManager.TERAGENE))
        {
            // Initialise it for the mob here.
            Alleles<TeraType, Gene<TeraType>> alleles = new Alleles<>();
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

    /**
     * Marks the use of terastallization. This sets the pokemob's owner's
     * cooldown for using tera to 1, meaning they need to reset it somehow, say
     * via healing at a pokecenter, sleeping in a bed, or whatever else we set
     * to reset it.
     * 
     * @param pokemob - the pokemob trying to terastallize
     * @return whether we did terastallize
     */
    public static boolean tryTera(IPokemob pokemob)
    {
        if (!(pokemob.getEntity().getLevel() instanceof ServerLevel)) return false;
        Alleles<TeraType, Gene<TeraType>> genes = getTeraGenes(pokemob.getEntity());
        if (genes == null) return false;
        boolean canTera = !pokemob.isPlayerOwned();
        if (!canTera)
        {
            CompoundTag data = PokecubePlayerDataHandler.getCustomDataTag(pokemob.getOwnerId());
            canTera = data.getInt("pokecube:tera_cooldown") == 0;

            if (canTera)
            {
                data.putInt("pokecube:tera_cooldown", 1);
                PokecubePlayerDataHandler.saveCustomData(pokemob.getOwnerId().toString());
            }
        }
        if (canTera)
        {
            genes.getExpressed().getValue().isTera = true;
            PacketSyncGene.syncGeneToTracking(pokemob.getEntity(), genes);
        }
        return canTera;
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
        if (tera.isTera)
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
