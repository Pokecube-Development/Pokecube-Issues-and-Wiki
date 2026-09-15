package pokecube.gimmicks.dynamax;

import javax.annotation.Nullable;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.util.INBTSerializable;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.PokecubeCore;
import pokecube.core.entity.genetics.GeneticsManager;
import pokecube.core.eventhandlers.PokemobEventsHandler.MegaEvoTicker;
import pokecube.gimmicks.dynamax.DynamaxGene.DynaObject;
import pokecube.gimmicks.zmoves.GZMoveManager;
import thut.api.ThutCaps;
import thut.api.Tracker;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;
import thut.api.entity.genetics.IMobGenetics;
import thut.core.common.ThutCore;

public class DynamaxGene implements Gene<DynaObject>
{
    @Nullable
    public static DynaObject getDyna(Entity mob)
    {
        final IMobGenetics genes = ThutCaps.getGenetics(mob);
        if (genes == null) return null;
        if (!genes.getKeys().contains(GeneticsManager.GMAXGENE))
        {
            // Initialise it for the mob here.
            Alleles<DynaObject, Gene<DynaObject>> alleles = new Alleles<>(genes);
            Gene<DynaObject> gene1 = new DynamaxGene();
            Gene<DynaObject> gene2 = new DynamaxGene();
            genes.setGenes(gene1, gene2);
        }
        try
        {
            Alleles<DynaObject, Gene<DynaObject>> alleles = genes.getAlleles(GeneticsManager.GMAXGENE);
            if (alleles == null) return null;
            Gene<DynaObject> gene = alleles.getExpressed();
            return gene.getValue();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static class DynaObject implements INBTSerializable<CompoundTag>
    {
        public boolean gigantamax = false;
        public int dynaLevel = 0;

        @Override
        public CompoundTag serializeNBT(Provider provider)
        {
            final CompoundTag tag = new CompoundTag();
            tag.putBoolean("gmax", this.gigantamax);
            tag.putInt("lvl", this.dynaLevel);
            return tag;
        }

        @Override
        public void deserializeNBT(Provider provider, final CompoundTag nbt)
        {
            this.gigantamax = nbt.getBoolean("gmax");
            this.dynaLevel = nbt.getInt("lvl");
        }
    }

    // Our actual gene information
    private DynaObject value = new DynaObject();

    private boolean de_dyna = false;
    private boolean was_dyna = false;

    @Override
    public ResourceLocation getKey()
    {
        return GeneticsManager.GMAXGENE;
    }

    @Override
    public float getMutationRate()
    {
        return GeneticsManager.mutationRates.get(this.getKey());
    }

    @Override
    public DynaObject getValue()
    {
        return this.value;
    }

    @Override
    public Gene<DynaObject> interpolate(final Gene<DynaObject> other)
    {
        final DynamaxGene result = new DynamaxGene();
        result.value = ThutCore.newRandom().nextBoolean() ? other.getValue() : this.getValue();
        return result;
    }
    private IPokemob _pokemob = null;
    private boolean _checked = false;

    @Override
    public void onUpdateTick(Entity entity)
    {
        if (!entity.isAddedToLevel()) return;
        if (!_checked)
        {
            _pokemob = PokemobCaps.getPokemobFor(entity);
            _checked = true;
        }
        if(_pokemob==null)return;
        boolean isDyna = DynamaxHelper.isDynamax(_pokemob);
        String[] g_z_moves = _pokemob.getMoveStats().getMovesToUse();
        if (isDyna)
        {
            was_dyna = true;
            boolean isGigant = this.getValue().gigantamax;
            for (int i = 0; i < 4; i++)
            {
                String move = _pokemob.getMoveStats().getBaseMoves()[i];
                final String gmove = GZMoveManager.getGMove(_pokemob, move, isGigant);
                if (gmove != null) g_z_moves[i] = gmove;
            }
        }
        else if (was_dyna)
        {
            was_dyna = false;
            for (int i = 0; i < 4; i++)
            {
                String move = _pokemob.getMoveStats().getBaseMoves()[i];
                g_z_moves[i] = move;
            }
        }

        if (entity.level().isClientSide()) return;
        // check dynamax timer for cooldown.
        // Used for the tick logic below
        long dynatime;
        if (isDyna)
        {
            final long time = Tracker.instance().getTick();
            int dynaEnd = entity.getPersistentData().getInt("pokecube:dynadur");
            dynatime = entity.getPersistentData().getLong("pokecube:dynatime");
            if (!this.de_dyna && time - dynaEnd > dynatime)
            {
                Component mess = Component.translatableEscape("pokemob.dynamax.timeout.revert", _pokemob.getDisplayName());
                _pokemob.displayMessageToOwner(mess);

                final PokedexEntry newEntry = _pokemob.getBasePokedexEntry();
                mess = Component.translatableEscape("pokemob.dynamax.revert", _pokemob.getDisplayName());
                MegaEvoTicker.scheduleRevert(PokecubeCore.getConfig().evolutionTicks / 2, newEntry, _pokemob, mess);
                if (PokecubeCore.getConfig().debug_commands) PokecubeAPI.logInfo("Reverting Dynamax");

                this.de_dyna = true;
            }
        }
        else
        {
            this.de_dyna = false;
        }
    }

    @Override
    public void load(Provider provider, final CompoundTag tag)
    {
        this.value.deserializeNBT(provider, tag);
    }

    @Override
    public Gene<DynaObject> mutate()
    {
        final DynamaxGene gene = new DynamaxGene();
        gene.value.gigantamax = true;
        return gene;
    }

    @Override
    public CompoundTag save(Provider provider)
    {
        return this.value.serializeNBT(provider);
    }

    @Override
    public void setValue(final DynaObject value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        return "" + value.gigantamax;
    }
}
