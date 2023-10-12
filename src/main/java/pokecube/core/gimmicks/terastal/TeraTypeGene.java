package pokecube.core.gimmicks.terastal;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
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
import pokecube.core.PokecubeItems;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.network.pokemobs.PacketSyncGene;
import thut.api.ThutCaps;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;
import thut.api.entity.genetics.GeneRegistry;
import thut.api.entity.genetics.IMobGenetics;
import thut.core.common.ThutCore;
import thut.wearables.EnumWearable;
import thut.wearables.ThutWearables;
import thut.wearables.inventory.IWearableInventory;

/**
 * Implementation of the Terastallizing mechanic. This is tracked per pokemob
 * via genes, and this class contains the required code for attaching the genes,
 * managing the types, etc. This is all arranged via calling
 * {@link TeraTypeGene#init(FMLLoadCompleteEvent)}, and via the
 * EventBusSubscriber
 *
 */
@Mod.EventBusSubscriber(bus = Bus.MOD, modid = PokecubeCore.MODID)
public class TeraTypeGene implements Gene<TeraTypeGene.TeraType>
{
    /**
     * Silly hats worn by terastallized pokemobs
     */
    public static final Map<PokeType, ItemStack> SILLY_HATS = new HashMap<>();

    /**
     * Setup and register tera type stuff.
     */
    @SubscribeEvent
    public static void init(FMLLoadCompleteEvent event)
    {
        // Register the genes
        GeneRegistry.register(TeraTypeGene.class);
        // Add listener for adding the STAB bonus when we are used.
        PokecubeAPI.MOVE_BUS.addListener(EventPriority.LOW, false, TeraTypeGene::duringPreMoveUse);
        // Add listener for removing tera when recalled
        PokecubeAPI.POKEMOB_BUS.addListener(TeraTypeGene::onRecall);
    }

    /**
     * Ensure that the SILLY_HATS map is populated, by default we fill it with
     * bling hats with custom models specified, and alpha of 196.
     */
    private static void checkHats()
    {
        if (SILLY_HATS.isEmpty())
        {
            for (PokeType type : PokeType.values())
            {
                ItemStack HAT = PokecubeItems.getStack(new ResourceLocation("thut_bling:bling_hat"));
                String name = type.name.equals("???") ? "unknown" : type.name;
                ItemStack ZCRYSTAL = PokecubeItems.getStack("pokecube_legends:z_" + name);
                CompoundTag tag = new CompoundTag();
                tag.put("gemTag", ZCRYSTAL.serializeNBT());
                tag.putInt("alpha", 196);
                tag.putString("model", "pokecube:models/worn/tera_hats/tera-" + name);
                tag.putString("tex", "pokecube:textures/worn/tera_hats/tera-hat-" + name + ".png");
                HAT.setTag(tag);
                if (HAT.getItem() instanceof DyeableLeatherItem dye) dye.setColor(HAT, type.colour);
                SILLY_HATS.put(type, HAT);
            }
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

    /**
     * Holder for the mob's terastallize information, holds type and present
     * state.
     *
     */
    public static class TeraType implements INBTSerializable<CompoundTag>
    {

        /**
         * Whether we are presently terastallized
         */
        public boolean isTera = false;
        /**
         * The type we terastallize to
         */
        public PokeType teraType = PokeType.unknown;

        @Override
        public CompoundTag serializeNBT()
        {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("s", isTera);
            tag.putString("t", teraType.name);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt)
        {
            this.isTera = nbt.getBoolean("s");
            this.teraType = PokeType.getType(nbt.getString("t"));
        }
    }

    Random rand = ThutCore.newRandom();
    private TeraType value = new TeraType();
    private IPokemob holder = null;
    private IWearableInventory worn = null;
    private boolean hadHat = false;

    @Override
    public ResourceLocation getKey()
    {
        return GeneticsManager.TERAGENE;
    }

    @Override
    public TeraType getValue()
    {
        return value;
    }

    @Override
    protected Gene<TeraType> clone()
    {
        Gene<TeraType> gene = new TeraTypeGene();
        gene.getValue().teraType = this.getValue().teraType;
        return gene;
    }

    /**
     * We interpolate by just selecting a random one, us or other, and making a
     * copy.
     */
    @Override
    public Gene<TeraType> interpolate(Gene<TeraType> other)
    {
        Gene<TeraType> type2 = other;
        if (other instanceof TeraTypeGene gene) type2 = gene.clone();
        return rand.nextBoolean() ? this.clone() : type2;
    }

    @Override
    public void load(CompoundTag tag)
    {
        this.getValue().deserializeNBT(tag);
    }

    /**
     * On update, we check the following each second (20ticks):<br>
     * <br>
     * - If terastallized, we ensure the user is monotype our type<br>
     * - if terastallized and client side, we ensure user is wearing a silly
     * hat.<br>
     */
    @Override
    public void onUpdateTick(Entity entity)
    {
        if (entity.tickCount % 20 != 0) return;

        if (this.getValue().isTera)
        {
            if (holder == null)
            {
                holder = PokemobCaps.getPokemobFor(entity);
            }
            if (holder != null)
            {
                holder.setType1(this.getValue().teraType);
                holder.setType2(PokeType.unknown);
            }
        }
        if (entity.getLevel().isClientSide() && entity instanceof LivingEntity living)
        {
            if (worn == null)
            {
                worn = ThutWearables.getWearables(living);
            }
            if (worn != null)
            {
                checkHats();
                ItemStack HAT = SILLY_HATS.get(this.getValue().teraType);
                if (!HAT.isEmpty())
                {
                    if (this.getValue().isTera)
                    {
                        worn.setWearable(EnumWearable.HAT, HAT, 0);
                        hadHat = true;
                    }
                    else if (hadHat)
                    {
                        worn.setWearable(EnumWearable.HAT, ItemStack.EMPTY);
                    }
                }
            }
        }
    }

    @Override
    public Gene<TeraType> mutate()
    {
        TeraType mut = new TeraType();
        if (PokeType.values().length > 1)
            mut.teraType = PokeType.values()[1 + rand.nextInt(PokeType.values().length - 1)];
        TeraTypeGene type = new TeraTypeGene();
        type.setValue(mut);
        return type;
    }

    @Override
    public CompoundTag save()
    {
        return this.getValue().serializeNBT();
    }

    @Override
    public void setValue(TeraType value)
    {
        this.value = value;
    }
}