package pokecube.core.entity.pokemobs.genetics.genes;

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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.events.pokemobs.RecallEvent;
import pokecube.api.events.pokemobs.combat.MoveUse;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeItems;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
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

public class TeraTypeGene implements Gene<TeraTypeGene.TeraType>
{
    public static final Map<PokeType, ItemStack> SILLY_HATS = new HashMap<>();

    public static void init()
    {
        // Register the genes
        GeneRegistry.register(TeraTypeGene.class);
        // Add listner for adding the teratypes to the mobs
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, EventPriority.LOWEST,
                TeraTypeGene::postCapabilityAttach);
        // Add listener for adding the STAB bonus when we are used.
        PokecubeAPI.MOVE_BUS.addListener(EventPriority.LOW, false, TeraTypeGene::duringPreMoveUse);
        // Add listener for removing tera when recalled
        PokecubeAPI.POKEMOB_BUS.addListener(TeraTypeGene::onRecall);
    }

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
                tag.putInt("alpha", 128);
                tag.putString("model", "pokecube:worn/tera_hats/" + name);
                HAT.setTag(tag);
                if (HAT.getItem() instanceof DyeableLeatherItem dye) dye.setColor(HAT, type.colour);
                SILLY_HATS.put(type, HAT);
            }
        }
    }

    @Nullable
    public static TeraType getTera(Entity mob)
    {
        final IMobGenetics genes = mob.getCapability(ThutCaps.GENETICS_CAP, null).orElse(null);
        if (genes == null) return null;
        if (!genes.getKeys().contains(GeneticsManager.TERAGENE))
        {
            // Initialise it for the mob here.
            Alleles<TeraType, Gene<TeraType>> alleles = new Alleles<>();
            alleles.setAllele(0, new TeraTypeGene().mutate());
            alleles.setAllele(1, new TeraTypeGene().mutate());
            alleles.getExpressed();
            genes.getAlleles().put(GeneticsManager.TERAGENE, alleles);
            if (mob.getLevel() instanceof ServerLevel) PacketSyncGene.syncGeneToTracking(mob, alleles);
        }
        try
        {
            Alleles<TeraType, Gene<TeraType>> alleles = genes.getAlleles(GeneticsManager.TERAGENE);
            if (alleles == null) return null;
            Gene<TeraType> gene = alleles.getExpressed();
            return gene.getValue();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private static final void onRecall(RecallEvent.Post event)
    {
        TeraType type = getTera(event.recalled.getEntity());
        if (type != null) type.isTera = false;
    }

    private static final void postCapabilityAttach(AttachCapabilitiesEvent<Entity> event)
    {
        IMobGenetics genes = null;
        IPokemob pokemob = null;
        for (var cap : event.getCapabilities().values())
        {
            if (genes == null)
            {
                Object o = cap.getCapability(ThutCaps.GENETICS_CAP, null).orElse(null);
                if (o instanceof IMobGenetics g)
                {
                    genes = g;
                }
            }
            if (pokemob == null)
            {
                Object o1 = cap.getCapability(PokemobCaps.POKEMOB_CAP, null).orElse(null);
                if (o1 instanceof IPokemob g)
                {
                    pokemob = g;
                }
            }
            if (genes != null && pokemob != null) break;
        }
        if (genes == null && pokemob == null) return;
        if (genes != null && !genes.getKeys().contains(GeneticsManager.TERAGENE))
        {
            // Initialise it for the mob here.
            Alleles<TeraType, Gene<TeraType>> alleles = new Alleles<>();
            alleles.setAllele(0, new TeraTypeGene().mutate());
            alleles.setAllele(1, new TeraTypeGene().mutate());
            alleles.getExpressed();
            genes.getAlleles().put(GeneticsManager.TERAGENE, alleles);
        }
    }

    private static final void duringPreMoveUse(MoveUse.DuringUse.Pre evt)
    {
        final MoveApplication move = evt.getPacket();
        final IPokemob attacker = move.getUser();
        TeraType tera = getTera(attacker.getEntity());
        if (tera.isTera)
        {
            boolean originalType = move.type == attacker.originalType1() || move.type == attacker.originalType2();
            if (originalType) move.stab = true;
            if (attacker.isType(move.type)) move.stabFactor = 2.0f;
            if (move.pwr < 60) move.pwr = 60;
        }
    }

    public static class TeraType implements INBTSerializable<CompoundTag>
    {
        public boolean isTera = false;
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
    public Gene<TeraType> interpolate(Gene<TeraType> other)
    {
        return rand.nextBoolean() ? this : other;
    }

    @Override
    public void load(CompoundTag tag)
    {
        this.getValue().deserializeNBT(tag);
    }

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