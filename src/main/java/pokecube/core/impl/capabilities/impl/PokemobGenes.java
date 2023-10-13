package pokecube.core.impl.capabilities.impl;

import java.util.Random;

import net.minecraft.server.level.ServerLevel;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityManager;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.Nature;
import pokecube.api.utils.TagNames;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.entity.pokemobs.genetics.epigenes.EVsGene;
import pokecube.core.entity.pokemobs.genetics.epigenes.MovesGene;
import pokecube.core.entity.pokemobs.genetics.genes.AbilityGene;
import pokecube.core.entity.pokemobs.genetics.genes.AbilityGene.AbilityObject;
import pokecube.core.entity.pokemobs.genetics.genes.ColourGene;
import pokecube.core.entity.pokemobs.genetics.genes.IVsGene;
import pokecube.core.entity.pokemobs.genetics.genes.NatureGene;
import pokecube.core.entity.pokemobs.genetics.genes.ShinyGene;
import pokecube.core.entity.pokemobs.genetics.genes.SizeGene;
import pokecube.core.entity.pokemobs.genetics.genes.SpeciesGene;
import pokecube.core.entity.pokemobs.genetics.genes.SpeciesGene.SpeciesInfo;
import pokecube.core.network.pokemobs.PacketChangeForme;
import pokecube.core.network.pokemobs.PacketSyncGene;
import thut.api.entity.IMobColourable;
import thut.api.entity.genetics.Alleles;
import thut.core.common.ThutCore;

public abstract class PokemobGenes extends PokemobSided implements IMobColourable
{
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

    private boolean _shinyCache = false;

    @Override
    public Ability getAbility()
    {
        if (this.genesAbility == null) this.initAbilityGene();
        final AbilityGene gene = this.genesAbility.getExpressed();
        final AbilityObject obj = gene.getValue();
        if (this.inCombat()) return this.moveInfo.battleAbility;
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
        if (this.genesAbility == null) this.initAbilityGene();
        final AbilityGene gene = this.genesAbility.getExpressed();
        final AbilityObject obj = gene.getValue();
        return obj.abilityIndex;
    }

    @Override
    public byte[] getEVs()
    {
        if (this.genesEVs == null)
        {
            if (this.genes == null) throw new RuntimeException("This should not be called here");
            this.genesEVs = this.genes.getAlleles(GeneticsManager.EVSGENE);
            if (this.genesEVs == null)
            {
                this.genesEVs = new Alleles<>();
                this.genes.getAlleles().put(GeneticsManager.EVSGENE, this.genesEVs);
            }
            if (this.genesEVs.getAllele(0) == null || this.genesEVs.getAllele(1) == null)
            {
                EVsGene evs = new EVsGene();
                this.genesEVs.setAllele(0,
                        evs.getMutationRate() > this.getEntity().getRandom().nextFloat() ? (EVsGene) evs.mutate()
                                : evs);
                this.genesEVs.setAllele(1,
                        evs.getMutationRate() > this.getEntity().getRandom().nextFloat() ? (EVsGene) evs.mutate()
                                : evs);
                this.genesEVs.refreshExpressed();
                evs = this.genesEVs.getExpressed();
                evs.setValue(new EVsGene().getValue());
            }
        }
        final EVsGene evs = this.genesEVs.getExpressed();
        return evs.getValue();
    }

    @Override
    public byte[] getIVs()
    {
        if (this.genesIVs == null)
        {
            if (this.genes == null) throw new RuntimeException("This should not be called here");
            this.genesIVs = this.genes.getAlleles(GeneticsManager.IVSGENE);
            if (this.genesIVs == null)
            {
                this.genesIVs = new Alleles<>();
                this.genes.getAlleles().put(GeneticsManager.IVSGENE, this.genesIVs);
            }
            if (this.genesIVs.getAllele(0) == null || this.genesIVs.getAllele(1) == null)
            {
                final IVsGene gene = new IVsGene();
                this.genesIVs.setAllele(0,
                        gene.getMutationRate() > this.getEntity().getRandom().nextFloat() ? (IVsGene) gene.mutate()
                                : gene);
                this.genesIVs.setAllele(1,
                        gene.getMutationRate() > this.getEntity().getRandom().nextFloat() ? (IVsGene) gene.mutate()
                                : gene);
                this.genesIVs.refreshExpressed();
            }
        }
        final IVsGene gene = this.genesIVs.getExpressed();
        return gene.getValue();
    }

    @Override
    public String[] getMoves()
    {
        if (this.genesMoves == null)
        {
            if (this.genes == null) throw new RuntimeException("This should not be called here");
            this.genesMoves = this.genes.getAlleles(GeneticsManager.MOVESGENE);
            if (this.genesMoves == null)
            {
                this.genesMoves = new Alleles<>();
                this.genes.getAlleles().put(GeneticsManager.MOVESGENE, this.genesMoves);
            }
            if (this.genesMoves.getAllele(0) == null || this.genesMoves.getAllele(1) == null)
            {
                final MovesGene gene = new MovesGene();
                gene.setValue(this.getMoveStats().getBaseMoves());
                this.genesMoves.setAllele(0,
                        gene.getMutationRate() > this.getEntity().getRandom().nextFloat() ? (MovesGene) gene.mutate()
                                : gene);
                this.genesMoves.setAllele(1,
                        gene.getMutationRate() > this.getEntity().getRandom().nextFloat() ? (MovesGene) gene.mutate()
                                : gene);
                this.genesMoves.refreshExpressed();
            }
        }
        final MovesGene gene = this.genesMoves.getExpressed();
        if (gene.getValue() != this.getMoveStats().getBaseMoves())
        {
            this.getMoveStats().setBaseMoves(gene.getValue());
            this.getMoveStats().reset();
        }
        return this.getMoveStats().getMovesToUse();
    }

    @Override
    public Nature getNature()
    {
        if (this.genesNature == null)
        {
            if (this.genes == null) throw new RuntimeException("This should not be called here");
            this.genesNature = this.genes.getAlleles(GeneticsManager.NATUREGENE);
            if (this.genesNature == null)
            {
                this.genesNature = new Alleles<>();
                this.genes.getAlleles().put(GeneticsManager.NATUREGENE, this.genesNature);
            }
            if (this.genesNature.getAllele(0) == null || this.genesNature.getAllele(1) == null)
            {
                final NatureGene gene = new NatureGene();
                this.genesNature.setAllele(0,
                        gene.getMutationRate() > this.getEntity().getRandom().nextFloat() ? (NatureGene) gene.mutate()
                                : gene);
                this.genesNature.setAllele(1,
                        gene.getMutationRate() > this.getEntity().getRandom().nextFloat() ? (NatureGene) gene.mutate()
                                : gene);
                this.genesNature.refreshExpressed();
            }
        }
        final NatureGene gene = this.genesNature.getExpressed();
        return gene.getValue();
    }

    @Override
    public PokedexEntry getPokedexEntry()
    {
        if (this.genesSpecies == null)
        {
            if (this.genes == null) throw new RuntimeException("This should not be called here");
            this.genesSpecies = this.genes.getAlleles(GeneticsManager.SPECIESGENE);
            if (this.genesSpecies == null)
            {
                this.genesSpecies = new Alleles<>();
                this.genes.getAlleles().put(GeneticsManager.SPECIESGENE, this.genesSpecies);
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
                this.genesSpecies.refreshExpressed();
                _speciesCache = this.genesSpecies.getExpressed();
                // Set the expressed gene to the info made above, this is to
                // override the gene from merging parents which results in the
                // child state.
                _speciesCache.setValue(info);
            }
            info = _speciesCache.getValue();
            info.setEntry(info.getEntry().getForGender(info.getSexe()));
        }
        if (this._speciesCache == null)
        {
            this._speciesCache = this.genesSpecies.getExpressed();
        }
        return _speciesCache.getValue().getTmpEntry();
    }

    @Override
    public int[] getRGBA()
    {
        if (this.genesColour == null)
        {
            if (this.genes == null) throw new RuntimeException("This should not be called here");
            this.genesColour = this.genes.getAlleles(GeneticsManager.COLOURGENE);
            if (this.genesColour == null)
            {
                this.genesColour = new Alleles<>();
                this.genes.getAlleles().put(GeneticsManager.COLOURGENE, this.genesColour);
            }
            if (this.genesColour.getAllele(0) == null)
            {
                final ColourGene gene = new ColourGene();
                this.genesColour.setAllele(0,
                        gene.getMutationRate() > this.getEntity().getRandom().nextFloat() ? (ColourGene) gene.mutate()
                                : gene);
                this.genesColour.setAllele(1,
                        gene.getMutationRate() > this.getEntity().getRandom().nextFloat() ? (ColourGene) gene.mutate()
                                : gene);
                this.genesColour.refreshExpressed();
            }
        }
        if (this.genes == null)
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
        if (this.genesSize == null)
        {
            if (this.genes == null) throw new RuntimeException("This should not be called here");
            this.genesSize = this.genes.getAlleles(GeneticsManager.SIZEGENE);
            if (this.genesSize == null)
            {
                this.genesSize = new Alleles<>();
                this.genes.getAlleles().put(GeneticsManager.SIZEGENE, this.genesSize);
            }
            if (this.genesSize.getAllele(0) == null || this.genesSize.getAllele(1) == null)
            {
                SizeGene gene = new SizeGene();
                this.genesSize.setAllele(0,
                        gene.getMutationRate() > this.getEntity().getRandom().nextFloat() ? (SizeGene) gene.mutate()
                                : gene);
                this.genesSize.setAllele(1,
                        gene.getMutationRate() > this.getEntity().getRandom().nextFloat() ? (SizeGene) gene.mutate()
                                : gene);
                this.genesSize.refreshExpressed();
                gene = this.genesSize.getExpressed();
                this.setSize(gene.getValue());
            }
        }
        final SizeGene gene = this.genesSize.getExpressed();
        Float size = gene.getValue();

        if (size <= 0 || Float.isNaN(size))
        {
            PokecubeAPI.LOGGER.error("Error with pokemob size! " + size);
            size = 1f;
            gene.setValue(size);
        }
        return size;
    }

    @Override
    public float getSize()
    {
        return (float) (this.getSizeRaw() * PokecubeCore.getConfig().scalefactor);
    }

    private void initAbilityGene()
    {
        if (this.genesAbility == null)
        {
            if (this.genes == null) throw new RuntimeException("This should not be called here");
            this.genesAbility = this.genes.getAlleles(GeneticsManager.ABILITYGENE);
            if (this.genesAbility == null)
            {
                this.genesAbility = new Alleles<>();
                this.genes.getAlleles().put(GeneticsManager.ABILITYGENE, this.genesAbility);
            }
            if (this.genesAbility.getAllele(0) == null)
            {
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
                this.genesAbility.setAllele(0, gene);
                this.genesAbility.setAllele(1, gene);
                this.genesAbility.refreshExpressed();
            }
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
            this.setAbilityRaw(this.getAbility());
        }
    }

    @Override
    public boolean isShiny()
    {
        if (this.genesShiny == null)
        {
            if (this.genes == null) throw new RuntimeException("This should not be called here");
            this.genesShiny = this.genes.getAlleles(GeneticsManager.SHINYGENE);
            if (this.genesShiny == null)
            {
                this.genesShiny = new Alleles<>();
                this.genes.getAlleles().put(GeneticsManager.SHINYGENE, this.genesShiny);
            }
            if (this.genesShiny.getAllele(0) == null || this.genesShiny.getAllele(1) == null)
            {
                final ShinyGene gene = new ShinyGene();
                this.genesShiny.setAllele(0,
                        gene.getMutationRate() > this.getEntity().getRandom().nextFloat() ? (ShinyGene) gene.mutate()
                                : gene);
                this.genesShiny.setAllele(1,
                        gene.getMutationRate() > this.getEntity().getRandom().nextFloat() ? (ShinyGene) gene.mutate()
                                : gene);
                this.genesShiny.refreshExpressed();
            }
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

        this.genesSpecies = null;
        this.getPokedexEntry();
        this.genesSize = null;
        this.getSizeRaw();
        this.genesIVs = null;
        this.getIVs();
        this.genesEVs = null;
        this.getEVs();
        this.genesMoves = null;
        this.getMoves();
        this.genesNature = null;
        this.getNature();
        this.genesAbility = null;
        this.getAbility();
        this.genesShiny = null;
        this.isShiny();
        this.genesColour = null;
        this.getRGBA();

        // Refresh the datamanager for moves.
        this.setMoves(this.getMoveStats().getBaseMoves());
        // Refresh the datamanager for evs
        this.setEVs(this.getEVs());

        this.setSize(this.getSizeRaw());

        // Ensure this is in persistent data for client side tooltip
        this.entity.getPersistentData().putByte(TagNames.SEXE, this.getSexe());
    }

    @Override
    public void setAbilityRaw(final Ability ability)
    {
        if (this.genesAbility == null) this.initAbilityGene();
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
        if (this.genesAbility == null) this.initAbilityGene();
        if (ability > 2 || ability < 0) ability = 0;
        final AbilityGene gene = this.genesAbility.getExpressed();
        final AbilityObject obj = gene.getValue();
        obj.abilityIndex = (byte) ability;
    }

    @Override
    public void setEVs(final byte[] evs)
    {
        if (this.genesEVs == null) this.getEVs();
        if (this.genesEVs != null)
        {
            final EVsGene gene = this.genesEVs.getExpressed();
            gene.setValue(evs);
        }
        PacketSyncGene.syncGeneToTracking(this.getEntity(), this.genesEVs);
    }

    @Override
    public void setIVs(final byte[] ivs)
    {
        if (this.genesIVs == null) this.getIVs();
        if (this.genesIVs != null)
        {
            final IVsGene gene = this.genesIVs.getExpressed();
            gene.setValue(ivs);
        }
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
            if (this.genesMoves == null) this.getMoves();
            if (this.genesMoves == null || this.genesMoves.getExpressed() == null || this.getMoveStats() == null)
            {
                PokecubeAPI.LOGGER.error("Error in setMoves " + this.getEntity(), new NullPointerException());
                PokecubeAPI.LOGGER.error("AllGenes: " + this.genes);
                PokecubeAPI.LOGGER.error("Genes: " + this.genesMoves);
                if (this.genesMoves != null) PokecubeAPI.LOGGER.error("Gene: " + this.genesMoves.getExpressed());
                else PokecubeAPI.LOGGER.error("Gene: " + this.genesMoves);
                PokecubeAPI.LOGGER.error("stats: " + this.getMoveStats());
                return;
            }
            final MovesGene gene = this.genesMoves.getExpressed();
            gene.setValue(this.getMoveStats().setBaseMoves(moves));
        }
        PacketSyncGene.syncGeneToTracking(this.getEntity(), this.genesMoves);
    }

    @Override
    public void setNature(final Nature nature)
    {
        if (this.genesNature == null) this.getNature();
        if (this.genesNature != null)
        {
            final NatureGene gene = this.genesNature.getExpressed();
            gene.setValue(nature);
        }
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
            this.changing = false;
            return ret;
        }
        this.changing = true;
        ret = this.changeForm(newEntry);

        // These need to be set after change form call, as that also does a
        // validation of old entry.
        info.setTmpEntry(newEntry);

        if (this.getEntity().getLevel() != null) ret.setSize(ret.getSize());
        if (this.getEntity().getLevel() != null && this.getEntity().isEffectiveAi())
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
    }

    @Override
    public void setSexe(final byte sexe)
    {
        if (this.genesSpecies == null) this.getPokedexEntry();
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
    }

    @Override
    public void setShiny(final boolean shiny)
    {
        if (this.genesShiny == null) this.isShiny();
        final ShinyGene gene = this.genesShiny.getExpressed();
        gene.setValue(shiny);
        PacketSyncGene.syncGeneToTracking(this.getEntity(), this.genesShiny);
    }

    @Override
    public void setSize(float size)
    {
        if (this.genesSize == null) this.getSizeRaw();
        float a = 1, b = 1, c = 1;
        final PokedexEntry entry = this.getPokedexEntry();
        if (entry != null)
        {
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
            this.getEntity().getDimensions(this.getEntity().getPose()).scale(size);
        }
        final SizeGene gene = this.genesSize.getExpressed();
        gene.setValue(size);
    }

    @Override
    public void setCustomHolder(FormeHolder holder)
    {
        if (holder != null) holder = Database.formeHolders.getOrDefault(holder.key, holder);
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
