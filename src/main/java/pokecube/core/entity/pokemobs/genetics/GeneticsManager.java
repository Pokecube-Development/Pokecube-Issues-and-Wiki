package pokecube.core.entity.pokemobs.genetics;

import java.util.List;
import java.util.Map;

import org.nfunk.jep.JEP;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pokecube.core.PokecubeItems;
import pokecube.core.entity.pokemobs.genetics.epigenes.EVsGene;
import pokecube.core.entity.pokemobs.genetics.epigenes.MovesGene;
import pokecube.core.entity.pokemobs.genetics.genes.AbilityGene;
import pokecube.core.entity.pokemobs.genetics.genes.ColourGene;
import pokecube.core.entity.pokemobs.genetics.genes.IVsGene;
import pokecube.core.entity.pokemobs.genetics.genes.NatureGene;
import pokecube.core.entity.pokemobs.genetics.genes.ShinyGene;
import pokecube.core.entity.pokemobs.genetics.genes.SizeGene;
import pokecube.core.entity.pokemobs.genetics.genes.SpeciesGene;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.api.entity.genetics.GeneRegistry;
import thut.api.entity.genetics.IMobGenetics;

@Mod.EventBusSubscriber
public class GeneticsManager
{
    public static class GeneticsProvider implements ICapabilityProvider, INBTSerializable<CompoundNBT>
    {
        public final IMobGenetics                wrapped = GeneRegistry.GENETICS_CAP.getDefaultInstance();
        private final LazyOptional<IMobGenetics> holder  = LazyOptional.of(() -> this.wrapped);

        @Override
        public void deserializeNBT(CompoundNBT tag)
        {
            final INBT nbt = tag.get("V");
            GeneRegistry.GENETICS_CAP.getStorage().readNBT(GeneRegistry.GENETICS_CAP, this.holder.orElse(null), null,
                    nbt);
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
        {
            return GeneRegistry.GENETICS_CAP.orEmpty(cap, this.holder);
        }

        @Override
        public CompoundNBT serializeNBT()
        {
            final INBT nbt = GeneRegistry.GENETICS_CAP.getStorage().writeNBT(GeneRegistry.GENETICS_CAP, this.holder
                    .orElse(null), null);
            final CompoundNBT tag = new CompoundNBT();
            tag.put("V", nbt);
            return tag;
        }
    }

    public static String epigeneticFunction = "rand()*(((2*v + 256) * 31) / 512)";

    public static JEP                    epigeneticParser = new JEP();
    public static final ResourceLocation POKECUBEGENETICS = new ResourceLocation(PokecubeMod.ID, "genetics");

    public static final String           GENES       = "Genes";
    public static final ResourceLocation ABILITYGENE = new ResourceLocation(PokecubeMod.ID, "ability");
    public static final ResourceLocation COLOURGENE  = new ResourceLocation(PokecubeMod.ID, "colour");
    public static final ResourceLocation SIZEGENE    = new ResourceLocation(PokecubeMod.ID, "size");
    public static final ResourceLocation NATUREGENE  = new ResourceLocation(PokecubeMod.ID, "nature");
    public static final ResourceLocation SHINYGENE   = new ResourceLocation(PokecubeMod.ID, "shiny");
    public static final ResourceLocation MOVESGENE   = new ResourceLocation(PokecubeMod.ID, "moves");
    public static final ResourceLocation IVSGENE     = new ResourceLocation(PokecubeMod.ID, "ivs");
    public static final ResourceLocation EVSGENE     = new ResourceLocation(PokecubeMod.ID, "evs");

    public static final ResourceLocation SPECIESGENE = new ResourceLocation(PokecubeMod.ID, "species");

    public static Map<ResourceLocation, Float> mutationRates = Maps.newHashMap();

    static
    {
        GeneticsManager.mutationRates.put(GeneticsManager.ABILITYGENE, 0.1f);
        GeneticsManager.mutationRates.put(GeneticsManager.COLOURGENE, 0.01f);
        GeneticsManager.mutationRates.put(GeneticsManager.SIZEGENE, 0.1f);
        GeneticsManager.mutationRates.put(GeneticsManager.NATUREGENE, 0.05f);
        GeneticsManager.mutationRates.put(GeneticsManager.SHINYGENE, 1 / 96f);
        GeneticsManager.mutationRates.put(GeneticsManager.MOVESGENE, 0.0f);
        GeneticsManager.mutationRates.put(GeneticsManager.IVSGENE, 0.1f);
        GeneticsManager.mutationRates.put(GeneticsManager.EVSGENE, 0.1f);
        GeneticsManager.mutationRates.put(GeneticsManager.SPECIESGENE, 0.1f);
        GeneticsManager.initJEP();
        GeneticsManager.init();
    }

    @SubscribeEvent
    public static void attachItemCapability(AttachCapabilitiesEvent<ItemStack> event)
    {
        if (PokecubeItems.is(PokecubeItems.POKEMOBEGG, event.getObject()) && !event.getCapabilities().containsKey(
                GeneticsManager.POKECUBEGENETICS)) event.addCapability(GeneticsManager.POKECUBEGENETICS,
                        new GeneticsProvider());
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

    public static void handleEpigenetics(IPokemob pokemob)
    {
        // pokemob.onGenesChanged();
    }

    public static void handleLoad(IPokemob pokemob)
    {
        final Entity mob = pokemob.getEntity();
        final IMobGenetics genes = mob.getCapability(GeneRegistry.GENETICS_CAP, null).orElse(null);
        if (!genes.getAlleles().isEmpty()) return;
        GeneticsManager.initMob(mob);
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
    }

    public static void initEgg(IMobGenetics eggs, IMobGenetics mothers, IMobGenetics fathers)
    {
        if (eggs == null || mothers == null || fathers == null) return;
        eggs.setFromParents(mothers, fathers);
    }

    public static void initFromGenes(IMobGenetics genes, IPokemob pokemob)
    {
        final Entity mob = pokemob.getEntity();
        final IMobGenetics mobs = mob.getCapability(GeneRegistry.GENETICS_CAP, null).orElse(null);
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

    public static void initMob(Entity mob)
    {
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        pokemob.onGenesChanged();
    }
}
