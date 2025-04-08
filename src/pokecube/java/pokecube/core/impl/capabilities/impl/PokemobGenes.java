package pokecube.core.impl.capabilities.impl;

import net.minecraft.server.level.ServerLevel;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityManager;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.Nature;
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

import java.util.Random;
import java.util.function.Supplier;

public abstract class PokemobGenes extends PokemobSided implements IMobColourable
{
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
            _sizeChanged = true;
        }
        else if (t.getKey().equals(GeneticsManager.SIZEGENE))
        {
            _sizeChanged = true;
        }
        else if (t.getKey().equals(GeneticsManager.SHINYGENE))
        {
            _shinyCache = null;
        }
        else if (t.getKey().equals(GeneticsManager.MOVESGENE))
        {
            _movesChanged = true;
        }
        else if (t.getKey().equals(GeneticsManager.ABILITYGENE))
        {
            _abilityChanged = true;
        }
        if (!this.getEntity().level().isClientSide())
            PacketSyncGene.syncGeneToTracking(this.getEntity(), this.getGenes().getAlleles(t.getKey()));
    }

    private void initGenes()
    {
        _shinyCache = null;
        _movesChanged = true;
        _sizeChanged = true;
        _abilityChanged = true;

        // Species gene
        var _speciesCache = new SpeciesGene();
        var info = _speciesCache.getValue();
        info.setEntry(PokecubeCore.getEntryFor(this.getEntity().getType()));
        info.setSexe(Tools.getSexe(info.getEntry().getSexeRatio(), ThutCore.newRandom()));
        info.setEntry(info.getEntry().getForGender(info.getSexe()));
        // Generate the basic genes
        var geneA = _speciesCache.getMutationRate() > this.getEntity().getRandom().nextFloat()
                ? (SpeciesGene) _speciesCache.mutate()
                : _speciesCache;
        var geneB = _speciesCache.getMutationRate() > this.getEntity().getRandom().nextFloat()
                ? (SpeciesGene) _speciesCache.mutate()
                : _speciesCache;
        // This triggers a call to accept above, we will undo the
        // results of that call next.
        getGenes().setGenes(geneA, geneB, _speciesCache);

        // Ability gene
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

    @Override
    public Ability getAbility()
    {
        Alleles<AbilityObject, AbilityGene> abilityGene = getGenes().getAlleles(GeneticsManager.ABILITYGENE);
        try
        {
            if (this._abilityChanged)
            {
                final AbilityGene gene = abilityGene.getExpressed();
                final AbilityObject obj = gene.getValue();
                if (obj.abilityObject == null && !obj.searched)
                {
                    if (!obj.ability.isEmpty())
                    {
                        obj.abilityObject = AbilityManager.getAbility(obj.ability);
                    }
                    else obj.abilityObject = this.getPokedexEntry().getAbility(obj.abilityIndex, this);
                    obj.searched = true;
                }
                this.moveInfo.battleAbility = obj.abilityObject;
                this._abilityChanged = false;
                this.setAbilityRaw(this.getAbility());
            }
        }
        catch (Exception e)
        {
            Thread.dumpStack();
            throw new RuntimeException(e);
        }

        if (!(abilityGene.getExpressed() instanceof AbilityGene))
        {
            // what??
            Thread.dumpStack();
            PokecubeAPI.logInfo("???");
        }

        if (this.inCombat()) return this.moveInfo.battleAbility;
        final AbilityGene gene = abilityGene.getExpressed();
        final AbilityObject obj = gene.getValue();
        // not in battle, re-synchronize this.
        this.moveInfo.battleAbility = obj.abilityObject;
        return obj.abilityObject;
    }

    @Override
    public String getAbilityName()
    {
        return this.params.ABILITYNAMEID.get();
    }

    @Override
    public int getAbilityIndex()
    {
        Alleles<AbilityObject, AbilityGene> abilityGene = getGenes().getAlleles(GeneticsManager.ABILITYGENE);
        final AbilityGene gene = abilityGene.getExpressed();
        final AbilityObject obj = gene.getValue();
        return obj.abilityIndex;
    }

    @Override
    public byte[] getEVs()
    {
        Alleles<byte[], EVsGene> genesEVs = getGenes().getAlleles(GeneticsManager.EVSGENE);
        final EVsGene evs = genesEVs.getExpressed();
        return evs.getValue();
    }

    @Override
    public byte[] getIVs()
    {
        Alleles<byte[], IVsGene> genesIVs = getGenes().getAlleles(GeneticsManager.IVSGENE);
        final IVsGene gene = genesIVs.getExpressed();
        return gene.getValue();
    }

    @Override
    public String[] getMoves()
    {
        Alleles<String[], MovesGene> genesMoves = getGenes().getAlleles(GeneticsManager.MOVESGENE);
        if (genesMoves == null)
        {
            Thread.dumpStack();
        }
        final MovesGene gene = genesMoves.getExpressed();
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
        Alleles<Nature, NatureGene> genesNature = getGenes().getAlleles(GeneticsManager.NATUREGENE);
        final NatureGene gene = genesNature.getExpressed();
        return gene.getValue();
    }

    @Override
    public PokedexEntry getPokedexEntry()
    {
        Alleles<SpeciesInfo, SpeciesGene> genesSpecies = getGenes().getAlleles(GeneticsManager.SPECIESGENE);
        if (genesSpecies == null)
        {
            Thread.dumpStack();
            return Database.missingno;
        }
        return genesSpecies.getExpressed().getValue().getTmpEntry();
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
        Alleles<int[], ColourGene> genesColour = getGenes().getAlleles(GeneticsManager.COLOURGENE);
        final ColourGene gene = genesColour.getExpressed();
        return gene.getValue();
    }

    @Override
    public byte getSexe()
    {
        Alleles<SpeciesInfo, SpeciesGene> genesSpecies = getGenes().getAlleles(GeneticsManager.SPECIESGENE);
        final SpeciesGene gene = genesSpecies.getExpressed();
        final SpeciesInfo info = gene.getValue();
        return info.getSexe();
    }

    public float getSizeRaw()
    {
        Alleles<Float, SizeGene> genesSize = getGenes().getAlleles(GeneticsManager.SIZEGENE);
        final SizeGene gene = genesSize.getExpressed();
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
            Alleles<Float, SizeGene> genesSize = getGenes().getAlleles(GeneticsManager.SIZEGENE);
            final SizeGene gene = genesSize.getExpressed();
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
            Alleles<Boolean, ShinyGene> genesShiny = getGenes().getAlleles(GeneticsManager.SHINYGENE);
            final ShinyGene gene = genesShiny.getExpressed();
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
    }

    @Override
    public void setAbilityRaw(final Ability ability)
    {
        Alleles<AbilityObject, AbilityGene> genesAbility = getGenes().getAlleles(GeneticsManager.ABILITYGENE);
        final AbilityGene gene = genesAbility.getExpressed();
        final AbilityObject obj = gene.getValue();
        final Ability oldAbility = obj.abilityObject;
        if (oldAbility != null && oldAbility != ability) oldAbility.destroy(this);
        final Ability defalt = this.getPokedexEntry().getAbility(this.getAbilityIndex(), this);
        obj.abilityObject = ability;
        obj.ability = ability != null
                ? defalt != null && defalt.getName().equals(ability.getName()) ? "" : ability.toString()
                : "";
        String name = ability != null ? ability.getName() : "";
        if (ability != null)
        {
            ability.init(this);
        }
        this.params.ABILITYNAMEID.set(name);
        this.moveInfo.battleAbility = ability;
        PacketSyncGene.syncGeneToTracking(this.getEntity(), genesAbility);
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
                this.params.ABILITYNAMEID.set(ability.getName());
            }
            return;
        }
        this.setAbilityRaw(ability);
    }

    @Override
    public void setAbilityIndex(int ability)
    {
        Alleles<AbilityObject, AbilityGene> genesAbility = getGenes().getAlleles(GeneticsManager.ABILITYGENE);
        if (ability > 2 || ability < 0) ability = 0;
        final AbilityGene gene = genesAbility.getExpressed();
        final AbilityObject obj = gene.getValue();
        obj.abilityIndex = (byte) ability;
        PacketSyncGene.syncGeneToTracking(this.getEntity(), genesAbility);
    }

    @Override
    public void setEVs(final byte[] evs)
    {
        Alleles<byte[], EVsGene> genesEVs = getGenes().getAlleles(GeneticsManager.EVSGENE);
        final EVsGene gene = genesEVs.getExpressed();
        gene.setValue(evs);
        PacketSyncGene.syncGeneToTracking(this.getEntity(), genesEVs);
    }

    @Override
    public void setIVs(final byte[] ivs)
    {
        Alleles<byte[], IVsGene> genesIVs = getGenes().getAlleles(GeneticsManager.IVSGENE);
        final IVsGene gene = genesIVs.getExpressed();
        gene.setValue(ivs);
        PacketSyncGene.syncGeneToTracking(this.getEntity(), genesIVs);
    }

    @Override
    public void setMove(final int i, final String moveName)
    {
        // do not blanket set moves on client, or when transformed.
        if (!(this.getEntity().level() instanceof ServerLevel) || this.getTransformedTo() != null) return;
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
        if (!(this.getEntity().level() instanceof ServerLevel) || this.getTransformedTo() != null) return;
        Alleles<String[], MovesGene> genesMoves = getGenes().getAlleles(GeneticsManager.MOVESGENE);
        if (moves != null && moves.length == 4)
        {
            if (genesMoves == null || genesMoves.getExpressed() == null || this.getMoveStats() == null)
            {
                PokecubeAPI.LOGGER.error("Error in setMoves " + this.getEntity(), new NullPointerException());
                PokecubeAPI.LOGGER.error("AllGenes: " + this.getGenes());
                PokecubeAPI.LOGGER.error("Genes: " + genesMoves);
                if (genesMoves != null) PokecubeAPI.LOGGER.error("Gene: " + genesMoves.getExpressed());
                else PokecubeAPI.LOGGER.error("Gene: " + genesMoves);
                PokecubeAPI.LOGGER.error("stats: " + this.getMoveStats());
                return;
            }
            final MovesGene gene = genesMoves.getExpressed();
            for (int i = 0; i < 4; i++) gene.getValue()[i] = moves[i];
            this.getMoveStats().setBaseMoves(gene.getValue());
        }
        PacketSyncGene.syncGeneToTracking(this.getEntity(), genesMoves);
    }

    @Override
    public void setNature(final Nature nature)
    {
        Alleles<Nature, NatureGene> genesNature = getGenes().getAlleles(GeneticsManager.NATUREGENE);
        final NatureGene gene = genesNature.getExpressed();
        gene.setValue(nature);
        PacketSyncGene.syncGeneToTracking(this.getEntity(), genesNature);
    }

    @Override
    public void setPokedexEntry(PokedexEntry newEntry)
    {
        Alleles<SpeciesInfo, SpeciesGene> genesSpecies = getGenes().getAlleles(GeneticsManager.SPECIESGENE);
        final PokedexEntry entry = this.getPokedexEntry();
        final SpeciesGene gene = genesSpecies.getExpressed();
        final SpeciesInfo info = gene.getValue();
        if (newEntry == null || newEntry == entry) return;
        IPokemob ret = this;
        if (this.changing)
        {
            info.setTmpEntry(newEntry);
            this.changing = false;
            return;
        }
        if (!this.getEntity().isAddedToLevel())
        {
            if (newEntry.generated)
            {
                FormeHolder holder = Database.formeHoldersByKey.get(newEntry.getTrimmedName());
                if (holder != null) info.setForme(holder);
            }
            info.setEntry(newEntry);
            _sizeChanged = true;
            this.changing = false;
            return;
        }
        this.changing = true;

        this.changeForm(newEntry);

        // These need to be set after change form call, as that also does a
        // validation of old entry.
        info.setTmpEntry(newEntry);
        _sizeChanged = true;
        if (info.getTmpForme() == entry.default_holder) info.setTmpForme(newEntry.default_holder);

        ret.setSize(ret.getSize());
        PacketChangeForme.sendPacketToTracking(ret.getEntity(), newEntry);
    }

    @Override
    public void setBasePokedexEntry(PokedexEntry newEntry)
    {
        Alleles<SpeciesInfo, SpeciesGene> genesSpecies = getGenes().getAlleles(GeneticsManager.SPECIESGENE);
        genesSpecies.getExpressed().getValue().entry = newEntry;
        FormeHolder form = Database.formeHoldersByKey.getOrDefault(newEntry.getTrimmedName(),
                newEntry.getModel(this.getSexe()));
        genesSpecies.getExpressed().getValue().setForme(form);
        _sizeChanged = true;
        PacketSyncGene.syncGeneToTracking(this.getEntity(), genesSpecies);

        // Reset the types cache
        this.getModifiers().type1 = null;
        this.getModifiers().type2 = null;
    }

    @Override
    public PokedexEntry getBasePokedexEntry()
    {
        Alleles<SpeciesInfo, SpeciesGene> genesSpecies = getGenes().getAlleles(GeneticsManager.SPECIESGENE);
        return genesSpecies.getExpressed().getValue().getBaseEntry();
    }

    @Override
    public void setRGBA(final int... colours)
    {
        final int[] rgba = this.getRGBA();
        for (int i = 0; i < colours.length && i < rgba.length; i++) rgba[i] = colours[i];
        Alleles<int[], ColourGene> genesColour = getGenes().getAlleles(GeneticsManager.COLOURGENE);
        PacketSyncGene.syncGeneToTracking(this.getEntity(), genesColour);
    }

    @Override
    public void setSexe(final byte sexe)
    {
        Alleles<SpeciesInfo, SpeciesGene> genesSpecies = getGenes().getAlleles(GeneticsManager.SPECIESGENE);
        final SpeciesGene gene = genesSpecies.getExpressed();
        final SpeciesInfo info = gene.getValue();
        if (sexe == IPokemob.NOSEXE || sexe == IPokemob.FEMALE || sexe == IPokemob.MALE
                || sexe == IPokemob.SEXLEGENDARY)
        {
            info.setSexe(sexe);
        }
        else
        {
            System.err.println("Illegal argument. Sexe cannot be " + sexe);
            Thread.dumpStack();
        }
        PacketSyncGene.syncGeneToTracking(this.getEntity(), genesSpecies);
    }

    @Override
    public void setShiny(final boolean shiny)
    {
        Alleles<Boolean, ShinyGene> genesShiny = getGenes().getAlleles(GeneticsManager.SHINYGENE);
        final ShinyGene gene = genesShiny.getExpressed();
        gene.setValue(shiny);
        this._shinyCache = shiny;
        PacketSyncGene.syncGeneToTracking(this.getEntity(), genesShiny);
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
        Alleles<Float, SizeGene> genesSize = getGenes().getAlleles(GeneticsManager.SIZEGENE);
        final SizeGene gene = genesSize.getExpressed();
        _sizeChanged = size != gene.getValue();
        gene.setValue(size);
    }

    @Override
    public void setCustomHolder(FormeHolder holder)
    {
        Alleles<SpeciesInfo, SpeciesGene> genesSpecies = getGenes().getAlleles(GeneticsManager.SPECIESGENE);
        // Ensures the species gene is initialised
        genesSpecies.getExpressed().getValue().setForme(holder);
        PacketSyncGene.syncGeneToTracking(this.getEntity(), genesSpecies);
    }

    @Override
    public FormeHolder getCustomHolder()
    {
        Alleles<SpeciesInfo, SpeciesGene> genesSpecies = getGenes().getAlleles(GeneticsManager.SPECIESGENE);
        // Ensures the species gene is initialised
        var entry = this.getPokedexEntry();
        FormeHolder holder = genesSpecies.getExpressed().getValue().getForme();
        if (holder == null) return entry.getModel(this.getSexe());
        return holder;
    }
}
