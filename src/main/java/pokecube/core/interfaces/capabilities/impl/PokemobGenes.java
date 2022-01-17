package pokecube.core.interfaces.capabilities.impl;

import java.util.Random;

import net.minecraft.server.level.ServerLevel;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.abilities.Ability;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.entity.pokemobs.genetics.epigenes.EVsGene;
import pokecube.core.entity.pokemobs.genetics.epigenes.MovesGene;
import pokecube.core.entity.pokemobs.genetics.genes.AbilityGene;
import pokecube.core.entity.pokemobs.genetics.genes.AbilityGene.AbilityObject;
import pokecube.core.entity.pokemobs.genetics.genes.ColourGene;
import pokecube.core.entity.pokemobs.genetics.genes.DynamaxGene;
import pokecube.core.entity.pokemobs.genetics.genes.DynamaxGene.DynaObject;
import pokecube.core.entity.pokemobs.genetics.genes.IVsGene;
import pokecube.core.entity.pokemobs.genetics.genes.NatureGene;
import pokecube.core.entity.pokemobs.genetics.genes.ShinyGene;
import pokecube.core.entity.pokemobs.genetics.genes.SizeGene;
import pokecube.core.entity.pokemobs.genetics.genes.SpeciesGene;
import pokecube.core.entity.pokemobs.genetics.genes.SpeciesGene.SpeciesInfo;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Nature;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.network.pokemobs.PacketChangeForme;
import pokecube.core.network.pokemobs.PacketSyncGene;
import pokecube.core.utils.Tools;
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

    private Alleles<DynaObject, DynamaxGene> genesDynamax;

    private boolean changing = false;

    @Override
    public Ability getAbility()
    {
        if (this.inCombat()) return this.moveInfo.battleAbility;
        if (this.genesAbility == null) this.initAbilityGene();
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
                        evs.getMutationRate() > this.rand.nextFloat() ? (EVsGene) evs.mutate() : evs);
                this.genesEVs.setAllele(1,
                        evs.getMutationRate() > this.rand.nextFloat() ? (EVsGene) evs.mutate() : evs);
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
                        gene.getMutationRate() > this.rand.nextFloat() ? (IVsGene) gene.mutate() : gene);
                this.genesIVs.setAllele(1,
                        gene.getMutationRate() > this.rand.nextFloat() ? (IVsGene) gene.mutate() : gene);
                this.genesIVs.refreshExpressed();
            }
        }
        final IVsGene gene = this.genesIVs.getExpressed();
        return gene.getValue();
    }

    @Override
    public String[] getMoves()
    {
        final String[] moves = this.getMoveStats().moves;
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
                gene.setValue(moves);
                this.genesMoves.setAllele(0,
                        gene.getMutationRate() > this.rand.nextFloat() ? (MovesGene) gene.mutate() : gene);
                this.genesMoves.setAllele(1,
                        gene.getMutationRate() > this.rand.nextFloat() ? (MovesGene) gene.mutate() : gene);
                this.genesMoves.refreshExpressed();
            }
        }
        final MovesGene gene = this.genesMoves.getExpressed();
        return this.getMoveStats().moves = gene.getValue();
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
                        gene.getMutationRate() > this.rand.nextFloat() ? (NatureGene) gene.mutate() : gene);
                this.genesNature.setAllele(1,
                        gene.getMutationRate() > this.rand.nextFloat() ? (NatureGene) gene.mutate() : gene);
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
            SpeciesGene gene;
            SpeciesInfo info;
            if (this.genesSpecies.getAllele(0) == null
                    || (info = (gene = this.genesSpecies.getExpressed()).getValue()).entry == null)
            {
                gene = new SpeciesGene();
                info = gene.getValue();
                info.entry = PokecubeCore.getEntryFor(this.getEntity().getType());
                info.value = Tools.getSexe(info.entry.getSexeRatio(), ThutCore.newRandom());
                info.entry = info.entry.getForGender(info.value);
                info = info.clone();
                // Generate the basic genes
                this.genesSpecies.setAllele(0,
                        gene.getMutationRate() > this.rand.nextFloat() ? (SpeciesGene) gene.mutate() : gene);
                this.genesSpecies.setAllele(1,
                        gene.getMutationRate() > this.rand.nextFloat() ? (SpeciesGene) gene.mutate() : gene);
                this.genesSpecies.refreshExpressed();
                gene = this.genesSpecies.getExpressed();
                // Set the expressed gene to the info made above, this is to
                // override the gene from merging parents which results in the
                // child state.
                gene.setValue(info);
            }
            info = gene.getValue();
            info.entry = this.entry = info.entry.getForGender(info.value);
        }
        if (this.entry != null) return this.entry;
        final SpeciesGene gene = this.genesSpecies.getExpressed();
        final SpeciesInfo info = gene.getValue();
        assert info.entry != null;
        return this.entry = info.entry;
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
                        gene.getMutationRate() > this.rand.nextFloat() ? (ColourGene) gene.mutate() : gene);
                this.genesColour.setAllele(1,
                        gene.getMutationRate() > this.rand.nextFloat() ? (ColourGene) gene.mutate() : gene);
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
        return info.value;
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
                        gene.getMutationRate() > this.rand.nextFloat() ? (SizeGene) gene.mutate() : gene);
                this.genesSize.setAllele(1,
                        gene.getMutationRate() > this.rand.nextFloat() ? (SizeGene) gene.mutate() : gene);
                this.genesSize.refreshExpressed();
                gene = this.genesSize.getExpressed();
                this.setSize(gene.getValue());
            }
        }
        final SizeGene gene = this.genesSize.getExpressed();
        Float size = gene.getValue();

        if (size <= 0 || Float.isNaN(size))
        {
            PokecubeCore.LOGGER.error("Error with pokemob size! " + size);
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
                        gene.getMutationRate() > this.rand.nextFloat() ? (ShinyGene) gene.mutate() : gene);
                this.genesShiny.setAllele(1,
                        gene.getMutationRate() > this.rand.nextFloat() ? (ShinyGene) gene.mutate() : gene);
                this.genesShiny.refreshExpressed();
            }
        }
        final ShinyGene gene = this.genesShiny.getExpressed();
        boolean shiny = gene.getValue();
        if (shiny && !this.getPokedexEntry().hasShiny)
        {
            shiny = false;
            gene.setValue(false);
        }
        return shiny;
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

        this.refreshDynaGene();

        // Refresh the datamanager for moves.
        this.setMoves(this.getMoves());
        // Refresh the datamanager for evs
        this.setEVs(this.getEVs());

        this.setSize(this.getSizeRaw());
    }

    private void refreshDynaGene()
    {
        this.genesDynamax = null;
        final boolean wasGigant = this.getCombatState(CombatStates.GIGANTAMAX);
        this.genesDynamax = this.genes.getAlleles(GeneticsManager.GMAXGENE);
        if (this.getGenesDynamax() == null)
            this.genes.getAlleles().put(GeneticsManager.GMAXGENE, this.genesDynamax = new Alleles<>());
        this.getGenesDynamax();
        if (wasGigant) this.getGenesDynamax().getExpressed().getValue().gigantamax = true;
    }

    public Alleles<DynaObject, DynamaxGene> getGenesDynamax()
    {
        if (this.genesDynamax != null
                && (this.genesDynamax.getAllele(0) == null || this.genesDynamax.getAllele(1) == null))
        {
            this.genesDynamax.setAllele(0, new DynamaxGene());
            this.genesDynamax.setAllele(1, new DynamaxGene());
            this.genes.getAlleles().put(GeneticsManager.GMAXGENE, this.genesDynamax);
        }
        return this.genesDynamax;
    }

    @Override
    public void setAbilityRaw(final Ability ability)
    {
        if (this.genesAbility == null) this.initAbilityGene();
        final AbilityGene gene = this.genesAbility.getExpressed();
        final AbilityObject obj = gene.getValue();
        final Ability oldAbility = obj.abilityObject;
        if (oldAbility != null && oldAbility != ability) oldAbility.destroy();
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

        final String[] moves = this.getMoves();
        moves[i] = moveName;
        this.setMoves(moves);
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
                PokecubeCore.LOGGER.error("Error in setMoves " + this.getEntity(), new NullPointerException());
                PokecubeCore.LOGGER.error("AllGenes: " + this.genes);
                PokecubeCore.LOGGER.error("Genes: " + this.genesMoves);
                if (this.genesMoves != null) PokecubeCore.LOGGER.error("Gene: " + this.genesMoves.getExpressed());
                else PokecubeCore.LOGGER.error("Gene: " + this.genesMoves);
                PokecubeCore.LOGGER.error("stats: " + this.getMoveStats());
                return;
            }
            final MovesGene gene = this.genesMoves.getExpressed();
            gene.setValue(this.getMoveStats().moves = moves);
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
    public IPokemob setPokedexEntry(final PokedexEntry newEntry)
    {
        final PokedexEntry entry = this.getPokedexEntry();
        final SpeciesGene gene = this.genesSpecies.getExpressed();
        final SpeciesInfo info = gene.getValue();
        if (newEntry == null || newEntry == entry) return this;
        IPokemob ret = this;
        if (this.changing || !this.getEntity().isAddedToWorld())
        {
            this.entry = newEntry;
            info.entry = newEntry;
            return ret;
        }
        this.changing = true;
        ret = this.megaEvolve(newEntry);

        // These need to be set after mega evolve call, as that also does a
        // validation of old entry.
        this.entry = newEntry;
        info.entry = newEntry;

        if (this.getEntity().getLevel() != null) ret.setSize(ret.getSize());
        if (this.getEntity().getLevel() != null && this.getEntity().isEffectiveAi())
            PacketChangeForme.sendPacketToTracking(ret.getEntity(), newEntry);
        return ret;
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
            info.value = sexe;
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
        this.genesSpecies.getExpressed().getValue().forme = holder;
    }

    @Override
    public FormeHolder getCustomHolder()
    {
        // Ensures the species gene is initialised
        this.getPokedexEntry();
        FormeHolder holder = this.genesSpecies.getExpressed().getValue().forme;
        if (holder == null) return this.getPokedexEntry().getModel(this.getSexe());
        if (Database.formeToEntry.getOrDefault(holder.key, this.getPokedexEntry()) != this.getPokedexEntry())
        {
            this.genesSpecies.getExpressed().getValue().forme = null;
            return this.getPokedexEntry().getModel(this.getSexe());
        }
        return holder;
    }
}
