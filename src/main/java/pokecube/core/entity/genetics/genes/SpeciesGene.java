package pokecube.core.entity.genetics.genes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.FormeHolder;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.genes.Mutations.Mutation;
import pokecube.core.database.genes.Mutations.MutationHolder;
import pokecube.core.database.tags.Tags;
import pokecube.core.entity.genetics.GeneticsManager;
import pokecube.core.entity.genetics.genes.SpeciesGene.SpeciesInfo;
import pokecube.core.network.pokemobs.PacketSyncGene;
import thut.api.ThutCaps;
import thut.api.entity.ICopyMob;
import thut.api.entity.event.CopySetEvent;
import thut.api.entity.genetics.Gene;
import thut.core.common.ThutCore;

public class SpeciesGene implements Gene<SpeciesInfo>
{
    public static class SpeciesInfo
    {
        private byte value;
        public PokedexEntry entry;
        private PokedexEntry tmpEntry;
        @Nullable
        private FormeHolder forme;
        @Nullable
        private FormeHolder tmpForme;

        @Override
        public SpeciesInfo clone()
        {
            final SpeciesInfo info = new SpeciesInfo();
            info.value = this.value;
            info.entry = this.entry;
            info.forme = this.forme;
            return info;
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (!(obj instanceof SpeciesInfo info)) return false;
            return this.getSexe() == info.getSexe()
                    && (this.getEntry() == null ? true : this.getEntry().equals(info.getEntry()));
        }

        void load(final CompoundTag tag)
        {
            this.setSexe(tag.getByte("G"));
            this.entry = Database.getEntry(tag.getString("E"));
            if (tag.contains("TE")) this.tmpEntry = Database.getEntry(tag.getString("TE"));
            if (tag.contains("F"))
            {
                this.forme = FormeHolder.load(tag.getCompound("F"));
                if (forme != null && forme._entry == Database.missingno) forme.setEntry(this.entry);
            }
            if (tag.contains("TF"))
            {
                this.tmpForme = FormeHolder.load(tag.getCompound("TF"));
                if (tmpForme != null && tmpForme._entry == Database.missingno && this.tmpEntry != null)
                    tmpForme.setEntry(this.tmpEntry);
            }
        }

        CompoundTag save()
        {
            final CompoundTag tag = new CompoundTag();
            tag.putByte("G", value);
            if (this.entry != null) tag.putString("E", this.entry.getName());
            if (this.tmpEntry != null) tag.putString("TE", this.tmpEntry.getName());
            if (forme != null) tag.put("F", forme.save());
            if (tmpForme != null) tag.put("TF", tmpForme.save());
            return tag;
        }

        @Override
        public String toString()
        {
            return this.getEntry() + " " + this.getSexe();
        }

        public PokedexEntry getBaseEntry()
        {
            // this inits the forme's entry if it exists.
            getForme();
            return this.forme != null ? this.forme._entry : this.entry;
        }

        public PokedexEntry getEntry()
        {
            var forme = this.getForme();
            if (forme != null) return forme._entry;
            return entry;
        }

        public void setEntry(PokedexEntry entry)
        {
            this.entry = entry;
        }

        public byte getSexe()
        {
            return value;
        }

        public void setSexe(byte value)
        {
            this.value = value;
        }

        public @Nullable FormeHolder getForme()
        {
            if (this.forme != null && this.forme._entry == null) this.forme.setEntry(entry);
            if (this.tmpForme != null) return tmpForme;
            return forme;
        }

        public void setForme(@Nullable FormeHolder forme)
        {
            this.forme = forme;
        }

        public PokedexEntry getTmpEntry()
        {
            if (tmpEntry == null) return getEntry();
            return tmpEntry;
        }

        public void setTmpEntry(PokedexEntry tmpEntry)
        {
            this.tmpEntry = tmpEntry;
            if (tmpEntry != this.entry && this.getForme() == this.entry.getModel(getSexe()))
            {
                this.setTmpForme(tmpEntry.getModel(getSexe()));
            }
            else if (tmpEntry == this.entry && this.getForme() == tmpEntry.getModel(getSexe()))
            {
                this.tmpEntry = null;
                this.tmpForme = null;
            }
        }

        public @Nullable FormeHolder getTmpForme()
        {
            return tmpForme;
        }

        public void setTmpForme(@Nullable FormeHolder forme)
        {
            this.tmpForme = forme;
        }
    }

    public static byte getSexe(final int baseValue, final Random random)
    {
        if (baseValue == 255) return IPokemob.NOSEXE;
        if (random.nextInt(255) >= baseValue) return IPokemob.MALE;
        return IPokemob.FEMALE;
    }

    SpeciesInfo info = new SpeciesInfo();

    Random rand = ThutCore.newRandom();

    /** The value here is of format {gender, ratio}. */
    public SpeciesGene()
    {
        this.info.setSexe((byte) 0);
    }

    @Override
    public float getEpigeneticRate()
    {
        return GeneticsManager.mutationRates.get(this.getKey());
    }

    @Override
    public ResourceLocation getKey()
    {
        return GeneticsManager.SPECIESGENE;
    }

    @Override
    public float getMutationRate()
    {
        return 1;
    }

    @Override
    public SpeciesInfo getValue()
    {
        return this.info;
    }

    @Override
    public Gene<SpeciesInfo> interpolate(final Gene<SpeciesInfo> other)
    {
        // Can be null for a blank species gene for a not-pokemob
        if (this.info.entry == null) return other.mutate();

        final SpeciesGene newGene = new SpeciesGene();
        final SpeciesGene otherG = (SpeciesGene) other;
        SpeciesGene mother = this.info.getSexe() == IPokemob.FEMALE ? this : this.info.getSexe() > 0 ? this : otherG;
        if (this.info.getSexe() == otherG.info.getSexe()) mother = this.rand.nextFloat() < 0.5 ? this : otherG;
        final SpeciesGene father = mother == otherG ? this : otherG;
        newGene.setValue(mother.info.clone());
        newGene.info.setEntry(newGene.info.getBaseEntry().getChild(father.info.getBaseEntry()));

        // First get out whatever the default choice was here.
        newGene.mutate();

        final Map<String, MutationHolder> mutations = Tags.GENES.getMutations(this.getKey());
        if (!mutations.isEmpty())
        {
            final List<String> opts = new ArrayList<>();

            // These are the possile combinations of mutations, we will check
            // them in this order.
            opts.add(mother.info.getBaseEntry().getTrimmedName() + "+" + father.info.getBaseEntry().getTrimmedName());
            opts.add(father.info.getBaseEntry().getTrimmedName() + "+" + mother.info.getBaseEntry().getTrimmedName());
            opts.add("null+" + mother.info.getBaseEntry().getTrimmedName());
            opts.add("null+" + father.info.getBaseEntry().getTrimmedName());
            opts.add(mother.info.getBaseEntry().getTrimmedName() + "+null");
            opts.add(father.info.getBaseEntry().getTrimmedName() + "+null");
            opts.add("");

            Collections.shuffle(opts);

            boolean mutated = false;
            for (String s : opts)
            {
                if (!s.isEmpty()) s = s + "->";
                s = s + newGene.info.getBaseEntry().getTrimmedName();
                mutated = mutations.containsKey(s);
                if (mutated)
                {
                    this.applyMutation(mutations.get(s), newGene);
                    break;
                }
            }
        }
        return newGene;
    }

    private void applyMutation(final MutationHolder mutationHolder, final SpeciesGene newGene)
    {
        final Mutation mutation = mutationHolder.getFor(this.rand.nextFloat());

        String[] args = mutation.result.split(":;");

        PokedexEntry value = Database.getEntry(args[0]);

        if (value != null)
        {
            newGene.info.setEntry(value);
            // Ensure gender ratios are correct
            newGene.info.setSexe(SpeciesGene.getSexe(newGene.info.getEntry().getSexeRatio(), this.rand));
            // Also apply the formeholder if present (defaults to null)
            newGene.info.setForme(newGene.info.getEntry().default_holder);
        }
    }

    @Override
    public void load(final CompoundTag tag)
    {
        this.info.load(tag.getCompound("V"));
        this._transformed = tag.getBoolean("T");
    }

    @Override
    public Gene<SpeciesInfo> mutate()
    {
        // Can be null for a blank species gene for a not-pokemob
        if (info.entry == null) return this;

        final SpeciesGene newGene = new SpeciesGene();
        newGene.setValue(this.info.clone());
        // Prevents mobs from hatching with wrong forms.
        newGene.info.setEntry(this.info.getEntry().getChild());
        // Ensure gender ratios are correct
        newGene.info.setSexe(SpeciesGene.getSexe(newGene.info.getEntry().getSexeRatio(), this.rand));
        return newGene;
    }

    @Override
    public CompoundTag save()
    {
        final CompoundTag tag = new CompoundTag();
        tag.put("V", this.info.save());
        if (_transformed) tag.putBoolean("T", true);
        return tag;
    }

    private IPokemob _pokemob = null;
    private ICopyMob _copy = null;
    private boolean _transformed = false;
    private boolean _checked = false;

    @Override
    public void onUpdateTick(Entity entity)
    {
        if (!entity.isAddedToWorld()) return;
        if (!_checked)
        {
            _pokemob = PokemobCaps.getPokemobFor(entity);
            if (_pokemob == null) _copy = ThutCaps.getCopyMob(entity);
            _checked = true;
        }
        if (_copy != null && entity instanceof LivingEntity living)
        {
            var mob = _copy.getCopiedMob();
            if (mob != null && _pokemob == null)
            {
                _pokemob = PokemobCaps.getPokemobFor(mob);
                if (_pokemob != null) _pokemob.setOwner(living);
            }
            if (mob == null && this.info.getEntry() != null)
            {
                var e = PokecubeCore.createPokemob(this.info.getEntry(), entity.level);
                if (e != null)
                {
                    _pokemob = PokemobCaps.getPokemobFor(e);
                    if (_pokemob != null) _pokemob.setOwner(living);
                    ThutCore.FORGE_BUS.post(new CopySetEvent(living, null, e));
                    e.setId(-(living.getId() + 100));
                    _copy.setCopiedMob(e);
                    var genes = ThutCaps.getGenetics(e);
                    if (genes != null && e.getId() < 100)
                    {
                        genes.getAlleles().forEach((key, alleles) -> {
                            alleles.getChangeListeners().add(0, g -> {
                                e.onAddedToWorld();
                            });
                            PacketSyncGene.syncGeneToTracking(living, alleles);
                            alleles.getChangeListeners().add(g -> {
                                e.onRemovedFromWorld();
                            });
                        });
                    }
                    if (living instanceof Player) System.out.println("New Mob");
                    _transformed = true;
                }
            }
            if (this.info.getEntry() == null && mob != null)
            {
                ThutCore.FORGE_BUS.post(new CopySetEvent(living, mob, null));
                _copy.setCopiedMob(null);
                if (living instanceof Player) System.out.println("No Mob");
                entity.refreshDimensions();
                _transformed = false;
            }
        }
    }

    @Override
    public void setValue(final SpeciesInfo value)
    {
        this.info = value;
    }

    @Override
    public String toString()
    {
        return this.info.toString();
    }
}
