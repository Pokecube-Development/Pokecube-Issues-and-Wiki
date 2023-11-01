package pokecube.core.entity.genetics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.nfunk.jep.JEP;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.events.pokemobs.ai.BrainInitEvent;
import pokecube.api.utils.TagNames;
import pokecube.core.entity.genetics.epigenes.EVsGene;
import pokecube.core.entity.genetics.epigenes.MovesGene;
import pokecube.core.entity.genetics.genes.AbilityGene;
import pokecube.core.entity.genetics.genes.ColourGene;
import pokecube.core.entity.genetics.genes.IVsGene;
import pokecube.core.entity.genetics.genes.NatureGene;
import pokecube.core.entity.genetics.genes.ShinyGene;
import pokecube.core.entity.genetics.genes.SizeGene;
import pokecube.core.entity.genetics.genes.SpeciesGene;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.network.pokemobs.PacketSyncGene;
import thut.api.ThutCaps;
import thut.api.entity.genetics.Gene;
import thut.api.entity.genetics.GeneRegistry;
import thut.api.entity.genetics.IMobGenetics;
import thut.api.item.ItemList;
import thut.core.common.ThutCore;
import thut.core.common.genetics.DefaultGenetics;

public class GeneticsManager
{
    public static class GeneticsProvider implements ICapabilityProvider, INBTSerializable<CompoundTag>
    {
        public final IMobGenetics wrapped = new DefaultGenetics();
        private final LazyOptional<IMobGenetics> holder = LazyOptional.of(() -> this.wrapped);

        @Override
        public void deserializeNBT(final CompoundTag tag)
        {
            final Tag nbt = tag.get("V");
            this.wrapped.deserializeNBT((ListTag) nbt);
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return ThutCaps.GENETICS_CAP.orEmpty(cap, this.holder);
        }

        @Override
        public CompoundTag serializeNBT()
        {
            final Tag nbt = this.wrapped.serializeNBT();
            final CompoundTag tag = new CompoundTag();
            tag.put("V", nbt);
            return tag;
        }
    }

    public static String epigeneticFunction = "rand()*(((2*v + 256) * 31) / 512)";

    public static JEP epigeneticParser = new JEP();
    public static final ResourceLocation POKECUBEGENETICS = new ResourceLocation(TagNames.GENESCAP);

    public static final String GENES = "Genes";
    public static final ResourceLocation ABILITYGENE = new ResourceLocation(PokecubeMod.ID, "ability");
    public static final ResourceLocation COLOURGENE = new ResourceLocation(PokecubeMod.ID, "colour");
    public static final ResourceLocation SIZEGENE = new ResourceLocation(PokecubeMod.ID, "size");
    public static final ResourceLocation NATUREGENE = new ResourceLocation(PokecubeMod.ID, "nature");
    public static final ResourceLocation SHINYGENE = new ResourceLocation(PokecubeMod.ID, "shiny");
    public static final ResourceLocation MOVESGENE = new ResourceLocation(PokecubeMod.ID, "moves");
    public static final ResourceLocation IVSGENE = new ResourceLocation(PokecubeMod.ID, "ivs");
    public static final ResourceLocation EVSGENE = new ResourceLocation(PokecubeMod.ID, "evs");
    public static final ResourceLocation GMAXGENE = new ResourceLocation(PokecubeMod.ID, "gmax");
    public static final ResourceLocation TERAGENE = new ResourceLocation(PokecubeMod.ID, "tera");

    public static final ResourceLocation SPECIESGENE = new ResourceLocation(PokecubeMod.ID, "species");

    public static List<Consumer<LivingEntity>> GENE_PROVIDERS = new ArrayList<>();
    public static Map<ResourceLocation, Supplier<Gene<?>>> DEFAULT_GENES = new HashMap<>();

    public static Map<ResourceLocation, Float> mutationRates = Maps.newHashMap();

    static
    {
        GeneticsManager.mutationRates.put(GeneticsManager.ABILITYGENE, 0.1f);
        GeneticsManager.mutationRates.put(GeneticsManager.COLOURGENE, 0.25f);
        GeneticsManager.mutationRates.put(GeneticsManager.SIZEGENE, 0.1f);
        GeneticsManager.mutationRates.put(GeneticsManager.NATUREGENE, 0.05f);
        GeneticsManager.mutationRates.put(GeneticsManager.SHINYGENE, 1 / 96f);
        GeneticsManager.mutationRates.put(GeneticsManager.MOVESGENE, 0.0f);
        GeneticsManager.mutationRates.put(GeneticsManager.IVSGENE, 0.1f);
        GeneticsManager.mutationRates.put(GeneticsManager.EVSGENE, 0.1f);
        GeneticsManager.mutationRates.put(GeneticsManager.SPECIESGENE, 0.1f);
        GeneticsManager.mutationRates.put(GeneticsManager.GMAXGENE, 0.001f);
        GeneticsManager.initJEP();
        GeneticsManager.init();
    }

    public static final ResourceLocation GENEHOLDERS = new ResourceLocation("pokecube:dna_holder");

    public static void registerGeneProvider(Consumer<LivingEntity> provider)
    {
        GENE_PROVIDERS.add(provider);
    }

    public static void registerCapabilities(final AttachCapabilitiesEvent<ItemStack> event)
    {
        if (ItemList.is(GENEHOLDERS, event.getObject())
                && !event.getCapabilities().containsKey(GeneticsManager.POKECUBEGENETICS))
        {
            event.addCapability(GeneticsManager.POKECUBEGENETICS, new GeneticsProvider());
        }
    }

    public static List<String> getMutationConfig()
    {
        final List<String> ret = Lists.newArrayList();
        for (final ResourceLocation key : GeneticsManager.mutationRates.keySet())
        {
            final String var = key + " " + GeneticsManager.mutationRates.get(key);
            ret.add(var);
        }
        return ret;
    }

    public static <T> Gene<T> getOrMutate(Gene<T> gene, LivingEntity owner)
    {
        return gene.getMutationRate() > owner.getRandom().nextFloat() ? gene.mutate() : gene;
    }

    public static void initGene(ResourceLocation key, LivingEntity living, IMobGenetics genes, Supplier<Gene<?>> source)
    {
        var g1 = getOrMutate(source.get(), living);
        var g2 = getOrMutate(source.get(), living);
        genes.setGenes(g1, g2);
    }

    private static void init()
    {
        GeneRegistry.register(AbilityGene.class);
        GeneRegistry.register(ColourGene.class);
        GeneRegistry.register(SpeciesGene.class);
        GeneRegistry.register(IVsGene.class);
        GeneRegistry.register(EVsGene.class);
        GeneRegistry.register(MovesGene.class);
        GeneRegistry.register(NatureGene.class);
        GeneRegistry.register(ShinyGene.class);
        GeneRegistry.register(SizeGene.class);

        ThutCore.FORGE_BUS.addListener(EventPriority.LOW, GeneticsManager::addRegisteredGenes);
        ThutCore.FORGE_BUS.addListener(EventPriority.HIGH, GeneticsManager::onBrainInit);

        // Populate defaults, add-ons can adjust this as needed later.
        DEFAULT_GENES.put(SIZEGENE, SizeGene::new);
        DEFAULT_GENES.put(COLOURGENE, ColourGene::new);
        DEFAULT_GENES.put(NATUREGENE, NatureGene::new);

        registerGeneProvider((living) -> {

            var genes = ThutCaps.getGenetics(living);
            // Only apply if it has genes
            if (genes == null) return;

            // Now make each gene if not present
            DEFAULT_GENES.forEach((key, gene) -> {
                if (genes.getAlleles(key) == null) initGene(key, living, genes, gene);
            });
        });
    }

    public static void initEgg(final IMobGenetics eggs, final IMobGenetics mothers, final IMobGenetics fathers)
    {
        if (eggs == null || mothers == null || fathers == null) return;
        eggs.setFromParents(mothers, fathers);
    }

    public static void initFromGenes(final IMobGenetics genes, final IPokemob pokemob)
    {
        final Entity mob = pokemob.getEntity();
        final IMobGenetics mobs = ThutCaps.getGenetics(mob);
        if (genes != mobs) mobs.getAlleles().putAll(genes.getAlleles());
        pokemob.onGenesChanged();
    }

    public static void initJEP()
    {
        GeneticsManager.epigeneticParser = new JEP();
        GeneticsManager.epigeneticParser.initFunTab();
        GeneticsManager.epigeneticParser.addStandardFunctions();
        GeneticsManager.epigeneticParser.initSymTab(); // clear the contents of
                                                       // the symbol table
        GeneticsManager.epigeneticParser.addStandardConstants();
        GeneticsManager.epigeneticParser.addComplex();
        // table
        GeneticsManager.epigeneticParser.addVariable("v", 0);
        GeneticsManager.epigeneticParser.parseExpression(GeneticsManager.epigeneticFunction);
    }

    private static void onBrainInit(final BrainInitEvent event)
    {
        initMob(event.getEntity());
    }

    private static void addRegisteredGenes(final EntityJoinWorldEvent event)
    {
        initMob(event.getEntity());
    }

    public static void initMob(final Entity mob)
    {
        // We only apply to living entities
        if (!(mob instanceof LivingEntity living)) return;
        IMobGenetics genes = ThutCaps.getGenetics(living);
        // And only ones with genes
        if (genes == null) return;
        // Now apply the genes
        GENE_PROVIDERS.forEach(p -> p.accept(living));
        // If we are server side, and added to world, update clients.
        if (!living.level.isClientSide() && living.isAddedToWorld())
            genes.getAlleles().forEach((key, alleles) -> PacketSyncGene.syncGeneToTracking(living, alleles));
    }

    @Nullable
    public static IMobGenetics getGenes(ItemStack stack)
    {
        if (stack.isEmpty()) return null;
        IMobGenetics genes = ThutCaps.getGenetics(stack);
        if (!stack.hasTag()) return genes;
        // Support old way first.
        if (stack.getTag().contains(GENES))
        {
            var nbt = stack.getTag();
            final Tag _genes = nbt.get(GeneticsManager.GENES);
            if (genes != null)
            {
                genes.deserializeNBT((ListTag) _genes);
                stack.getTag().remove(GENES);
            }
            else
            {
                genes = new DefaultGenetics();
                genes.deserializeNBT((ListTag) _genes);
                if (genes.getAlleles().isEmpty()) genes = null;
            }
            return genes;
        }
        // Next check for if it is a filled cube
        if (PokecubeManager.isFilled(stack))
        {
            var nbt = stack.getTag();
            final CompoundTag poketag = nbt.getCompound(TagNames.POKEMOB);
            if (!poketag.getCompound("ForgeCaps").contains(GeneticsManager.POKECUBEGENETICS.toString())) return null;
            if (!poketag.getCompound("ForgeCaps").getCompound(GeneticsManager.POKECUBEGENETICS.toString())
                    .contains("V"))
                return null;
            final Tag _genes = poketag.getCompound("ForgeCaps").getCompound(GeneticsManager.POKECUBEGENETICS.toString())
                    .get("V");
            genes = new DefaultGenetics();
            genes.deserializeNBT((ListTag) _genes);
            return genes;
        }
        return genes;
    }
}
