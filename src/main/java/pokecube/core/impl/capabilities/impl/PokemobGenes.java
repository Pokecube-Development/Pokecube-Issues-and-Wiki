package pokecube.core.impl.capabilities.impl;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityManager;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.Nature;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.utils.TagNames;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.entity.genetics.GeneticsManager;
import pokecube.core.entity.genetics.epigenes.EVsGene;
import pokecube.core.entity.genetics.epigenes.MovesGene;
import pokecube.core.entity.genetics.genes.AbilityGene;
import pokecube.core.entity.genetics.genes.AbilityGene.AbilityObject;
import pokecube.core.entity.genetics.genes.ColourGene;
import pokecube.core.entity.genetics.genes.IVsGene;
import pokecube.core.entity.genetics.genes.NatureGene;
import pokecube.core.entity.genetics.genes.ShinyGene;
import pokecube.core.entity.genetics.genes.SizeGene;
import pokecube.core.entity.genetics.genes.SpeciesGene;
import pokecube.core.entity.genetics.genes.SpeciesGene.SpeciesInfo;
import pokecube.core.network.pokemobs.PacketChangeForme;
import pokecube.core.network.pokemobs.PacketSyncGene;
import thut.api.entity.IMobColourable;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;
import thut.core.common.ThutCore;

public abstract class PokemobGenes extends PokemobSided implements IMobColourable, Consumer<Gene<?>>
{
    public static Consumer<LivingEntity> GENE_PROVIDER = living -> {
        IPokemob pokemob = PokemobCaps.getPokemobFor(living);
        if (!(pokemob instanceof PokemobGenes hasGenes)) return;
        hasGenes.initGenes();
    };

    static
    {
        GeneticsManager.registerGeneProvider(GENE_PROVIDER);
    }

    // Here we have all of the genes currently used.
    Alleles<Float, SizeGene> genesSize;
    Alleles<byte[], IVsGene> genesIVs;
    Alleles<byte[], EVsGene> genesEVs;
    Alleles<String[], MovesGene> genesMoves;
    Alleles<Nature, NatureGene> genesNature;
    Alleles<AbilityObject, AbilityGene> genesAbility;
    Alleles<int[], ColourGene> genesColour;
    Alleles<Boolean, ShinyGene> genesShiny;
    Alleles<SpeciesInfo, SpeciesGene> genesSpecies;

    private SpeciesGene _speciesCache = null;

    private boolean changing = false;

    private Boolean _shinyCache = null;
    private boolean _movesChanged = false;
    private boolean _sizeChanged = false;
    private boolean _abilityChanged = false;

    @Override
    public void accept(Gene<?> t)
    {
        if (t.getKey().equals(GeneticsManager.SPECIESGENE))
        {
            genesSpecies = this.getGenes().getAlleles(t.getKey());
            _speciesCache = genesSpecies.getExpressed();
            _sizeChanged = true;
        }
        else if (t.getKey().equals(GeneticsManager.SIZEGENE))
        {
            genesSize = this.getGenes().getAlleles(t.getKey());
            _sizeChanged = true;
        }
        else if (t.getKey().equals(GeneticsManager.SHINYGENE))
        {
            genesShiny = this.getGenes().getAlleles(t.getKey());
            this._shinyCache = null;
        }
        else if (t.getKey().equals(GeneticsManager.MOVESGENE))
        {
            genesMoves = this.getGenes().getAlleles(t.getKey());
            _movesChanged = true;
        }
        else if (t.getKey().equals(GeneticsManager.NATUREGENE))
        {
            genesNature = this.getGenes().getAlleles(t.getKey());
        }
        else if (t.getKey().equals(GeneticsManager.IVSGENE))
        {
            genesIVs = this.getGenes().getAlleles(t.getKey());
        }
        else if (t.getKey().equals(GeneticsManager.EVSGENE))
        {
            genesEVs = this.getGenes().getAlleles(t.getKey());
        }
        else if (t.getKey().equals(GeneticsManager.ABILITYGENE))
        {
            this.genesAbility = this.getGenes().getAlleles(t.getKey());
            _abilityChanged = true;
        }
        PacketSyncGene.syncGeneToTracking(this.getEntity(), this.getGenes().getAlleles(t.getKey()));
    }

    private void initGenes()
    {
        if (this.getGenes() == null) throw new RuntimeException("This should not be called here");

        this.genesSpecies = this.getGenes().getAlleles(GeneticsManager.SPECIESGENE);
        this.genesShiny = this.getGenes().getAlleles(GeneticsManager.SHINYGENE);
        this.genesEVs = this.getGenes().getAlleles(GeneticsManager.EVSGENE);
        this.genesSize = this.getGenes().getAlleles(GeneticsManager.SIZEGENE);
        this.genesIVs = this.getGenes().getAlleles(GeneticsManager.IVSGENE);
        this.genesMoves = this.getGenes().getAlleles(GeneticsManager.MOVESGENE);
        this.genesNature = this.getGenes().getAlleles(GeneticsManager.NATUREGENE);
        this.genesColour = this.getGenes().getAlleles(GeneticsManager.COLOURGENE);
        this.genesAbility = this.getGenes().getAlleles(GeneticsManager.ABILITYGENE);

        // Species gene
        if (this.genesSpecies == null)
        {
            this.genesSpecies = new Alleles<>(this.getGenes());
            this.getGenes().getAlleles().put(GeneticsManager.SPECIESGENE, this.genesSpecies);
        }
        SpeciesInfo info;
        if (this.genesSpecies.getAllele(0) == null
                || (info = (_speciesCache = this.genesSpecies.getExpressed()).getValue()).getEntry() == null)
        {
            _speciesCache = new SpeciesGene();
            info = _speciesCache.getValue();
            info.setEntry(PokecubeCore.getEntryFor(this.getEntity().getType()));
            info.setSexe(Tools.getSexe(info.getEntry().getSexeRatio(), ThutCore.newRandom()));
            info.setEntry(info.getEntry().getForGender(info.getSexe()));
            // Generate the basic genes
            this.genesSpecies.setAllele(0,
                    _speciesCache.getMutationRate() > this.getEntity().getRandom().nextFloat()
                            ? (SpeciesGene) _speciesCache.mutate()
                            : _speciesCache);
            this.genesSpecies.setAllele(1,
                    _speciesCache.getMutationRate() > this.getEntity().getRandom().nextFloat()
                            ? (SpeciesGene) _speciesCache.mutate()
                            : _speciesCache);
            // This triggers a call to accept above, we will undo the
            // results of that call next.
            this.genesSpecies.getExpressed();
            // Set the expressed gene to the info made above, this is to
            // override the gene from merging parents which results in the
            // child state.
            _speciesCache.setValue(info);
        }
        info = _speciesCache.getValue();
        info.setEntry(info.getEntry().getForGender(info.getSexe()));

        // Shiny gene
        if (this.genesShiny == null)
        {
            GeneticsManager.initGene(GeneticsManager.SHINYGENE, getEntity(), getGenes(), ShinyGene::new);
        }

        // EVs gene
        if (this.genesEVs == null)
        {
            GeneticsManager.initGene(GeneticsManager.EVSGENE, getEntity(), getGenes(), EVsGene::new);
        }

        // IVs gene
        if (this.genesIVs == null)
        {
            GeneticsManager.initGene(GeneticsManager.IVSGENE, getEntity(), getGenes(), IVsGene::new);
        }

        // Moves Gene
        if (this.genesMoves == null)
        {
            GeneticsManager.initGene(GeneticsManager.MOVESGENE, getEntity(), getGenes(), MovesGene::new);
        }

        // Nature gene
        if (this.genesNature == null)
        {
            GeneticsManager.initGene(GeneticsManager.NATUREGENE, getEntity(), getGenes(), NatureGene::new);
        }

        // Colour gene
        if (this.genesColour == null)
        {
            GeneticsManager.initGene(GeneticsManager.COLOURGENE, getEntity(), getGenes(), ColourGene::new);
        }

        // Ability gene
        if (this.genesAbility == null)
        {
            Supplier<Gene<?>> generator = () -> {
                final Random random = new Random(this.getRNGValue());
                final PokedexEntry entry = this.getPokedexEntry();
                int abilityIndex = random.nextInt(100) % 2;
                if (entry.getAbility(abilityIndex, this) == null) if (abilityIndex != 0) abilityIndex = 0;
                else abilityIndex = 1;
                final Ability ability = entry.getAbility(abilityIndex, this);
                final AbilityGene gene = new AbilityGene();
                final AbilityObject obj = gene.getValue();
                obj.ability = "";
                obj.abilityObject = ability;
                obj.abilityIndex = (byte) abilityIndex;
                return gene;
            };
            GeneticsManager.initGene(GeneticsManager.ABILITYGENE, getEntity(), getGenes(), generator);

        }

        // Size gene
        if (this.genesSize == null)
        {
            GeneticsManager.initGene(GeneticsManager.SIZEGENE, getEntity(), getGenes(), SizeGene::new);
        }
    }

    @Override
    public Ability getAbility()
    {
        if (this._abilityChanged)
        {
            final AbilityGene gene = this.genesAbility.getExpressed();
            final AbilityObject obj = gene.getValue();
            if (obj.abilityObject == null && !obj.searched)
            {
                if (!obj.ability.isEmpty())
                {
                    final Ability ability = AbilityManager.getAbility(obj.ability);
                    obj.abilityObject = ability;
                }
                else obj.abilityObject = this.getPokedexEntry().getAbility(obj.abilityIndex, this);
                obj.searched = true;
            }
            this.moveInfo.battleAbility = obj.abilityObject;
            this._abilityChanged = false;
            this.setAbilityRaw(this.getAbility());
        }
        if (this.inCombat()) return this.moveInfo.battleAbility;
        final AbilityGene gene = this.genesAbility.getExpressed();
        final AbilityObject obj = gene.getValue();
        // not in battle, re-synchronize this.
        this.moveInfo.battleAbility = obj.abilityObject;
        return obj.abilityObject;
    }

    @Override
    public String getAbilityName()
    {
        return this.dataSync().get(this.params.ABILITYNAMEID);
    }

    @Override
    public int getAbilityIndex()
    {
        final AbilityGene gene = this.genesAbility.getExpressed();
        final AbilityObject obj = gene.getValue();
        return obj.abilityIndex;
    }

    @Override
    public byte[] getEVs()
    {
        final EVsGene evs = this.genesEVs.getExpressed();
        return evs.getValue();
    }

    @Override
    public byte[] getIVs()
    {
        final IVsGene gene = this.genesIVs.getExpressed();
        return gene.getValue();
    }

    @Override
    public String[] getMoves()
    {
        final MovesGene gene = this.genesMoves.getExpressed();
        if (_movesChanged)
        {
            _movesChanged = false;
            this.getMoveStats().setBaseMoves(gene.getValue());
            this.getMoveStats().reset();
        }
        return this.getMoveStats().getMovesToUse();
    }

    @Override
    public Nature getNature()
    {
        final NatureGene gene = this.genesNature.getExpressed();
        return gene.getValue();
    }

    @Override
    public PokedexEntry getPokedexEntry()
    {
        if (this._speciesCache == null)
        {
            this._speciesCache = this.genesSpecies.getExpressed();
        }
        return _speciesCache.getValue().getTmpEntry();
    }

    @Override
    public int[] getRGBA()
    {
        if (this.getGenes() == null)
        {
            final int[] rgba = new int[4];
            rgba[0] = 255;
            rgba[1] = 255;
            rgba[2] = 255;
            rgba[3] = 255;
            return rgba;
        }
        final ColourGene gene = this.genesColour.getExpressed();
        return gene.getValue();
    }

    @Override
    public byte getSexe()
    {
        if (this.genesSpecies == null) this.getPokedexEntry();
        final SpeciesGene gene = this.genesSpecies.getExpressed();
        final SpeciesInfo info = gene.getValue();
        return info.getSexe();
    }

    public float getSizeRaw()
    {
        final SizeGene gene = this.genesSize.getExpressed();
        Float size = gene.getValue();

        if (size <= 0 || Float.isNaN(size))
        {
            PokecubeAPI.LOGGER.error("Error with pokemob size! " + size);
            size = 1f;
            gene.setValue(size);
        }
        return this.getEntity().getScale();
    }

    @Override
    public float getSize()
    {
        float size = this.getSizeRaw();
        if (_sizeChanged)
        {
            final SizeGene gene = this.genesSize.getExpressed();
            size = gene.getValue();
            this.setSize(size);
        }
        return size;
    }

    @Override
    public boolean isShiny()
    {
        if (_shinyCache == null)
        {
            final ShinyGene gene = this.genesShiny.getExpressed();
            boolean shiny = gene.getValue();
            if (shiny && !this.getPokedexEntry().hasShiny)
            {
                shiny = false;
                gene.setValue(false);
            }
            _shinyCache = shiny;
        }
        return _shinyCache;
    }

    @Override
    public void onGenesChanged()
    {
        // Reset this incase gender or shininess changed..
        this.textures = null;
        this.texs.clear();
        this.shinyTexs.clear();

        this.initGenes();

        // Ensure this is in persistent data for client side tooltip
        this.entity.getPersistentData().putByte(TagNames.SEXE, this.getSexe());
    }

    @Override
    public void setAbilityRaw(final Ability ability)
    {
        final AbilityGene gene = this.genesAbility.getExpressed();
        final AbilityObject obj = gene.getValue();
        final Ability oldAbility = obj.abilityObject;
        if (oldAbility != null && oldAbility != ability) oldAbility.destroy(this);
        final Ability defalt = this.getPokedexEntry().getAbility(this.getAbilityIndex(), this);
        obj.abilityObject = ability;
        obj.ability = ability != null
                ? defalt != null && defalt.getName().equals(ability.getName()) ? "" : ability.toString()
                : "";
        if (ability != null)
        {
            ability.init(this);
            this.dataSync().set(this.params.ABILITYNAMEID, ability.getName());
        }
        else this.dataSync().set(this.params.ABILITYNAMEID, "");
        this.moveInfo.battleAbility = ability;
        PacketSyncGene.syncGeneToTracking(this.getEntity(), this.genesAbility);
    }

    @Override
    public void setAbility(final Ability ability)
    {
        if (this.inCombat())
        {
            this.moveInfo.battleAbility = ability;
            if (ability != null)
            {
                ability.init(this);
                this.dataSync().set(this.params.ABILITYNAMEID, ability.getName());
            }
            return;
        }
        this.setAbilityRaw(ability);
    }

    @Override
    public void setAbilityIndex(int ability)
    {
        if (ability > 2 || ability < 0) ability = 0;
        final AbilityGene gene = this.genesAbility.getExpressed();
        final AbilityObject obj = gene.getValue();
        obj.abilityIndex = (byte) ability;
        PacketSyncGene.syncGeneToTracking(this.getEntity(), this.genesAbility);
    }

    @Override
    public void setEVs(final byte[] evs)
    {
        final EVsGene gene = this.genesEVs.getExpressed();
        gene.setValue(evs);
        PacketSyncGene.syncGeneToTracking(this.getEntity(), this.genesEVs);
    }

    @Override
    public void setIVs(final byte[] ivs)
    {
        final IVsGene gene = this.genesIVs.getExpressed();
        gene.setValue(ivs);
        PacketSyncGene.syncGeneToTracking(this.getEntity(), this.genesIVs);
    }

    @Override
    public void setMove(final int i, final String moveName)
    {
        // do not blanket set moves on client, or when transformed.
        if (!(this.getEntity().getLevel() instanceof ServerLevel) || this.getTransformedTo() != null) return;
        // Ensures the gene is synced and valid
        this.getMoves();
        // Then apply it to the base moves
        this.getMoveStats().getBaseMoves()[i] = moveName;
        this.getMoveStats().getMovesToUse()[i] = moveName;
        // Then sync
        this.setMoves(this.getMoveStats().getBaseMoves());
    }

    @Override
    public void setMoves(final String[] moves)
    {
        // do not blanket set moves on client, or when transformed.
        if (!(this.getEntity().getLevel() instanceof ServerLevel) || this.getTransformedTo() != null) return;
        if (moves != null && moves.length == 4)
        {
            if (this.genesMoves == null || this.genesMoves.getExpressed() == null || this.getMoveStats() == null)
            {
                PokecubeAPI.LOGGER.error("Error in setMoves " + this.getEntity(), new NullPointerException());
                PokecubeAPI.LOGGER.error("AllGenes: " + this.getGenes());
                PokecubeAPI.LOGGER.error("Genes: " + this.genesMoves);
                if (this.genesMoves != null) PokecubeAPI.LOGGER.error("Gene: " + this.genesMoves.getExpressed());
                else PokecubeAPI.LOGGER.error("Gene: " + this.genesMoves);
                PokecubeAPI.LOGGER.error("stats: " + this.getMoveStats());
                return;
            }
            final MovesGene gene = this.genesMoves.getExpressed();
            for (int i = 0; i < 4; i++) gene.getValue()[i] = moves[i];
            this.getMoveStats().setBaseMoves(gene.getValue());
        }
        PacketSyncGene.syncGeneToTracking(this.getEntity(), this.genesMoves);
    }

    @Override
    public void setNature(final Nature nature)
    {
        final NatureGene gene = this.genesNature.getExpressed();
        gene.setValue(nature);
        PacketSyncGene.syncGeneToTracking(this.getEntity(), this.genesNature);
    }

    @Override
    public IPokemob setPokedexEntry(PokedexEntry newEntry)
    {
        final PokedexEntry entry = this.getPokedexEntry();
        final SpeciesGene gene = this.genesSpecies.getExpressed();
        final SpeciesInfo info = gene.getValue();
        if (newEntry == null || newEntry == entry) return this;
        IPokemob ret = this;
        if (this.changing)
        {
            info.setTmpEntry(newEntry);
            this.changing = false;
            return this;
        }
        if (!this.getEntity().isAddedToWorld())
        {
            if (newEntry.generated)
            {
                FormeHolder holder = Database.formeHoldersByKey.get(newEntry.getTrimmedName());
                if (holder != null) info.setForme(holder);
            }
            info.setEntry(newEntry);
            _sizeChanged = true;
            this.changing = false;
            return ret;
        }
        this.changing = true;
        ret = this.changeForm(newEntry);

        // These need to be set after change form call, as that also does a
        // validation of old entry.
        info.setTmpEntry(newEntry);
        _sizeChanged = true;
        if (info.getTmpForme() == entry.default_holder) info.setTmpForme(newEntry.default_holder);

        if (this.getEntity().getLevel() != null) ret.setSize(ret.getSize());
        PacketChangeForme.sendPacketToTracking(ret.getEntity(), newEntry);
        return ret;
    }

    @Override
    public void setBasePokedexEntry(PokedexEntry newEntry)
    {
        if (this._speciesCache == null) this.getPokedexEntry();
        this._speciesCache.getValue().entry = newEntry;
        FormeHolder form = Database.formeHoldersByKey.getOrDefault(newEntry.getTrimmedName(),
                newEntry.getModel(this.getSexe()));
        this._speciesCache.getValue().setForme(form);
        _sizeChanged = true;
        PacketSyncGene.syncGeneToTracking(this.getEntity(), this.genesSpecies);

        // Reset the types cache
        this.getModifiers().type1 = null;
        this.getModifiers().type2 = null;
    }

    @Override
    public PokedexEntry getBasePokedexEntry()
    {
        if (this._speciesCache == null) this.getPokedexEntry();
        return this._speciesCache.getValue().getBaseEntry();
    }

    @Override
    public void setRGBA(final int... colours)
    {
        final int[] rgba = this.getRGBA();
        for (int i = 0; i < colours.length && i < rgba.length; i++) rgba[i] = colours[i];
        PacketSyncGene.syncGeneToTracking(this.getEntity(), this.genesColour);
    }

    @Override
    public void setSexe(final byte sexe)
    {
        final SpeciesGene gene = this.genesSpecies.getExpressed();
        final SpeciesInfo info = gene.getValue();
        if (sexe == IPokemob.NOSEXE || sexe == IPokemob.FEMALE || sexe == IPokemob.MALE
                || sexe == IPokemob.SEXLEGENDARY)
        {
            info.setSexe(sexe);
            // Ensure this is in persistent data for client side tooltip
            this.entity.getPersistentData().putByte(TagNames.SEXE, sexe);
        }
        else
        {
            System.err.println("Illegal argument. Sexe cannot be " + sexe);
            new Exception().printStackTrace();
        }
        PacketSyncGene.syncGeneToTracking(this.getEntity(), this.genesSpecies);
    }

    @Override
    public void setShiny(final boolean shiny)
    {
        final ShinyGene gene = this.genesShiny.getExpressed();
        gene.setValue(shiny);
        this._shinyCache = shiny;
        PacketSyncGene.syncGeneToTracking(this.getEntity(), this.genesShiny);
    }

    @Override
    public void setSize(float size)
    {
        final PokedexEntry entry = this.getPokedexEntry();
        if (entry != null && _sizeChanged)
        {
            float a = 1, b = 1, c = 1;
            a = entry.width * size;
            b = entry.height * size;
            c = entry.length * size;

            final double minS = PokecubeCore.getConfig().minMobSize;
            final double maxS = PokecubeCore.getConfig().maxMobSize;

            // Do not allow them to be smaller than the configured min size.
            if (a < minS || b < minS || c < minS)
            {
                final float min = (float) (minS / Math.min(a, Math.min(c, b)));
                size *= min;
            }
            // Do not allow them to be larger than the configured max size
            if (a > maxS || b > maxS || c > maxS)
            {
                final float max = (float) (maxS / Math.max(a, Math.max(c, b)));
                size *= max;
            }
            // Un-set this first, else infinte loop from refresh checking this.
            _sizeChanged = false;
            this.getEntity().refreshDimensions();
        }
        final SizeGene gene = this.genesSize.getExpressed();
        _sizeChanged = size != gene.getValue();
        gene.setValue(size);
    }

    @Override
    public void setCustomHolder(FormeHolder holder)
    {
        // Ensures the species gene is initialised
        this.genesSpecies.getExpressed().getValue().setForme(holder);
        PacketSyncGene.syncGeneToTracking(this.getEntity(), this.genesSpecies);
    }

    @Override
    public FormeHolder getCustomHolder()
    {
        // Ensures the species gene is initialised
        var entry = this.getPokedexEntry();
        FormeHolder holder = this.genesSpecies.getExpressed().getValue().getForme();
        if (holder == null) return entry.getModel(this.getSexe());
        return holder;
    }
}
