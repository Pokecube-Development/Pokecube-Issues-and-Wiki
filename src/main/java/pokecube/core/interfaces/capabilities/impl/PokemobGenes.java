package pokecube.core.interfaces.capabilities.impl;

import java.util.Random;

import net.minecraft.world.server.ServerWorld;
import pokecube.core.PokecubeCore;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.abilities.Ability;
import pokecube.core.database.abilities.AbilityManager;
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
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Nature;
import pokecube.core.network.pokemobs.PacketChangeForme;
import pokecube.core.network.pokemobs.PacketSyncGene;
import pokecube.core.utils.Tools;
import thut.api.entity.IMobColourable;
import thut.api.entity.genetics.Alleles;

public abstract class PokemobGenes extends PokemobSided implements IMobColourable
{
    private boolean changing = false;

    @Override
    public Ability getAbility()
    {
        if (this.genesAbility == null) this.initAbilityGene();
        final AbilityObject obj = this.genesAbility.getExpressed().getValue();
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
        return obj.abilityObject;
    }

    @Override
    public int getAbilityIndex()
    {
        if (this.genesAbility == null) this.initAbilityGene();
        final AbilityObject obj = this.genesAbility.getExpressed().getValue();
        return obj.abilityIndex;
    }

    @Override
    public byte[] getEVs()
    {
        if (this.genesEVs == null)
        {
            if (this.genes == null) throw new RuntimeException("This should not be called here");
            this.genesEVs = this.genes.getAlleles().get(GeneticsManager.EVSGENE);
            if (this.genesEVs == null)
            {
                this.genesEVs = new Alleles();
                this.genes.getAlleles().put(GeneticsManager.EVSGENE, this.genesEVs);
            }
            if (this.genesEVs.getAlleles()[0] == null || this.genesEVs.getAlleles()[1] == null)
            {
                final EVsGene ivs = new EVsGene();
                this.genesEVs.getAlleles()[0] = ivs.getMutationRate() > this.rand.nextFloat() ? ivs.mutate() : ivs;
                this.genesEVs.getAlleles()[1] = ivs.getMutationRate() > this.rand.nextFloat() ? ivs.mutate() : ivs;
                this.genesEVs.refreshExpressed();
                this.genesEVs.getExpressed().setValue(new EVsGene().getValue());
            }
        }
        return this.genesEVs.getExpressed().getValue();
    }

    @Override
    public byte[] getIVs()
    {
        if (this.genesIVs == null)
        {
            if (this.genes == null) throw new RuntimeException("This should not be called here");
            this.genesIVs = this.genes.getAlleles().get(GeneticsManager.IVSGENE);
            if (this.genesIVs == null)
            {
                this.genesIVs = new Alleles();
                this.genes.getAlleles().put(GeneticsManager.IVSGENE, this.genesIVs);
            }
            if (this.genesIVs.getAlleles()[0] == null || this.genesIVs.getAlleles()[1] == null)
            {
                final IVsGene gene = new IVsGene();
                this.genesIVs.getAlleles()[0] = gene.getMutationRate() > this.rand.nextFloat() ? gene.mutate() : gene;
                this.genesIVs.getAlleles()[1] = gene.getMutationRate() > this.rand.nextFloat() ? gene.mutate() : gene;
                this.genesIVs.refreshExpressed();
            }
        }
        return this.genesIVs.getExpressed().getValue();
    }

    @Override
    public String[] getMoves()
    {
        final String[] moves = this.getMoveStats().moves;
        if (this.genesMoves == null)
        {
            if (this.genes == null) throw new RuntimeException("This should not be called here");
            this.genesMoves = this.genes.getAlleles().get(GeneticsManager.MOVESGENE);
            if (this.genesMoves == null)
            {
                this.genesMoves = new Alleles();
                this.genes.getAlleles().put(GeneticsManager.MOVESGENE, this.genesMoves);
            }
            if (this.genesMoves.getAlleles()[0] == null || this.genesMoves.getAlleles()[1] == null)
            {
                final MovesGene gene = new MovesGene();
                gene.setValue(moves);
                this.genesMoves.getAlleles()[0] = gene.getMutationRate() > this.rand.nextFloat() ? gene.mutate() : gene;
                this.genesMoves.getAlleles()[1] = gene.getMutationRate() > this.rand.nextFloat() ? gene.mutate() : gene;
                this.genesMoves.refreshExpressed();
            }
        }
        return this.getMoveStats().moves = this.genesMoves.getExpressed().getValue();
    }

    @Override
    public Nature getNature()
    {
        if (this.genesNature == null)
        {
            if (this.genes == null) throw new RuntimeException("This should not be called here");
            this.genesNature = this.genes.getAlleles().get(GeneticsManager.NATUREGENE);
            if (this.genesNature == null)
            {
                this.genesNature = new Alleles();
                this.genes.getAlleles().put(GeneticsManager.NATUREGENE, this.genesNature);
            }
            if (this.genesNature.getAlleles()[0] == null || this.genesNature.getAlleles()[1] == null)
            {
                final NatureGene gene = new NatureGene();
                this.genesNature.getAlleles()[0] = gene.getMutationRate() > this.rand.nextFloat() ? gene.mutate()
                        : gene;
                this.genesNature.getAlleles()[1] = gene.getMutationRate() > this.rand.nextFloat() ? gene.mutate()
                        : gene;
                this.genesNature.refreshExpressed();
            }
        }
        return this.genesNature.getExpressed().getValue();
    }

    @Override
    public PokedexEntry getPokedexEntry()
    {
        if (this.genesSpecies == null)
        {
            if (this.genes == null) throw new RuntimeException("This should not be called here");
            this.genesSpecies = this.genes.getAlleles().get(GeneticsManager.SPECIESGENE);
            if (this.genesSpecies == null)
            {
                this.genesSpecies = new Alleles();
                this.genes.getAlleles().put(GeneticsManager.SPECIESGENE, this.genesSpecies);
            }
            SpeciesGene gene;
            SpeciesInfo info;
            if (this.genesSpecies.getAlleles()[0] == null || (info = (gene = this.genesSpecies.getExpressed())
                    .getValue()).entry == null)
            {
                gene = new SpeciesGene();
                info = gene.getValue();
                info.entry = PokecubeCore.getEntryFor(this.getEntity().getType());
                info.value = Tools.getSexe(info.entry.getSexeRatio(), new Random());
                info.entry = info.entry.getForGender(info.value);
                info = info.clone();
                // Generate the basic genes
                this.genesSpecies.getAlleles()[0] = gene.getMutationRate() > this.rand.nextFloat() ? gene.mutate()
                        : gene;
                this.genesSpecies.getAlleles()[1] = gene.getMutationRate() > this.rand.nextFloat() ? gene.mutate()
                        : gene;
                this.genesSpecies.refreshExpressed();
                // Set the expressed gene to the info made above, this is to
                // override the gene from merging parents which results in the
                // child state.
                this.genesSpecies.getExpressed().setValue(info);
            }
            info = this.genesSpecies.getExpressed().getValue();
            info.entry = this.entry = info.entry.getForGender(info.value);
        }
        if (this.entry != null) return this.entry;
        final SpeciesInfo info = this.genesSpecies.getExpressed().getValue();
        assert info.entry != null;
        return this.entry = info.entry;
    }

    @Override
    public int[] getRGBA()
    {
        if (this.genesColour == null)
        {
            if (this.genes == null) throw new RuntimeException("This should not be called here");
            this.genesColour = this.genes.getAlleles().get(GeneticsManager.COLOURGENE);
            if (this.genesColour == null)
            {
                this.genesColour = new Alleles();
                this.genes.getAlleles().put(GeneticsManager.COLOURGENE, this.genesColour);
            }
            if (this.genesColour.getAlleles()[0] == null)
            {
                final ColourGene gene = new ColourGene();
                this.genesColour.getAlleles()[0] = gene.getMutationRate() > this.rand.nextFloat() ? gene.mutate()
                        : gene;
                this.genesColour.getAlleles()[1] = gene.getMutationRate() > this.rand.nextFloat() ? gene.mutate()
                        : gene;
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

        return this.genesColour.getExpressed().getValue();
    }

    @Override
    public byte getSexe()
    {
        if (this.genesSpecies == null) this.getPokedexEntry();
        final SpeciesInfo info = this.genesSpecies.getExpressed().getValue();
        return info.value;
    }

    @Override
    public float getSize()
    {
        if (this.genesSize == null)
        {
            if (this.genes == null) throw new RuntimeException("This should not be called here");
            this.genesSize = this.genes.getAlleles().get(GeneticsManager.SIZEGENE);
            if (this.genesSize == null)
            {
                this.genesSize = new Alleles();
                this.genes.getAlleles().put(GeneticsManager.SIZEGENE, this.genesSize);
            }
            if (this.genesSize.getAlleles()[0] == null || this.genesSize.getAlleles()[1] == null)
            {
                final SizeGene gene = new SizeGene();
                this.genesSize.getAlleles()[0] = gene.getMutationRate() > this.rand.nextFloat() ? gene.mutate() : gene;
                this.genesSize.getAlleles()[1] = gene.getMutationRate() > this.rand.nextFloat() ? gene.mutate() : gene;
                this.genesSize.refreshExpressed();
                this.setSize(this.genesSize.getExpressed().getValue());
            }
        }
        final Float size = this.genesSize.getExpressed().getValue();
        return (float) (size * PokecubeCore.getConfig().scalefactor);
    }

    private void initAbilityGene()
    {
        if (this.genesAbility == null)
        {
            if (this.genes == null) throw new RuntimeException("This should not be called here");
            this.genesAbility = this.genes.getAlleles().get(GeneticsManager.ABILITYGENE);
            if (this.genesAbility == null)
            {
                this.genesAbility = new Alleles();
                this.genes.getAlleles().put(GeneticsManager.ABILITYGENE, this.genesAbility);
            }
            if (this.genesAbility.getAlleles()[0] == null)
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
                this.genesAbility.getAlleles()[0] = gene;
                this.genesAbility.getAlleles()[1] = gene;
                this.genesAbility.refreshExpressed();
            }
            this.setAbility(this.getAbility());
        }
    }

    @Override
    public boolean isShiny()
    {
        if (this.genesShiny == null)
        {
            if (this.genes == null) throw new RuntimeException("This should not be called here");
            this.genesShiny = this.genes.getAlleles().get(GeneticsManager.SHINYGENE);
            if (this.genesShiny == null)
            {
                this.genesShiny = new Alleles();
                this.genes.getAlleles().put(GeneticsManager.SHINYGENE, this.genesShiny);
            }
            if (this.genesShiny.getAlleles()[0] == null || this.genesShiny.getAlleles()[1] == null)
            {
                final ShinyGene gene = new ShinyGene();
                this.genesShiny.getAlleles()[0] = gene.getMutationRate() > this.rand.nextFloat() ? gene.mutate() : gene;
                this.genesShiny.getAlleles()[1] = gene.getMutationRate() > this.rand.nextFloat() ? gene.mutate() : gene;
                this.genesShiny.refreshExpressed();
            }
        }
        boolean shiny = this.genesShiny.getExpressed().getValue();
        if (shiny && !this.getPokedexEntry().hasShiny)
        {
            shiny = false;
            this.genesShiny.getExpressed().setValue(false);
        }
        return shiny;
    }

    @Override
    public void onGenesChanged()
    {
        // Reset this incase gender or shininess changed..
        this.textures = null;

        this.genesSpecies = null;
        this.getPokedexEntry();
        this.genesSize = null;
        this.getSize();
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
        this.setMoves(this.getMoves());
        // Refresh the datamanager for evs
        this.setEVs(this.getEVs());

        this.setSize(this.getSize());
    }

    @Override
    public void setAbility(final Ability ability)
    {
        if (this.genesAbility == null) this.initAbilityGene();
        final AbilityObject obj = this.genesAbility.getExpressed().getValue();
        final Ability oldAbility = obj.abilityObject;
        if (oldAbility != null && oldAbility != ability) oldAbility.destroy();
        final Ability defalt = this.getPokedexEntry().getAbility(this.getAbilityIndex(), this);
        obj.abilityObject = ability;
        obj.ability = ability != null ? defalt != null && defalt.getName().equals(ability.getName()) ? ""
                : ability.toString() : "";
        if (ability != null) ability.init(this);
    }

    @Override
    public void setAbilityIndex(int ability)
    {
        if (this.genesAbility == null) this.initAbilityGene();
        if (ability > 2 || ability < 0) ability = 0;
        final AbilityObject obj = this.genesAbility.getExpressed().getValue();
        obj.abilityIndex = (byte) ability;
    }

    @Override
    public void setEVs(final byte[] evs)
    {
        if (this.genesEVs == null) this.getEVs();
        if (this.genesEVs != null) this.genesEVs.getExpressed().setValue(evs);
        PacketSyncGene.syncGeneToTracking(this.getEntity(), this.genesEVs);
    }

    @Override
    public void setIVs(final byte[] ivs)
    {
        if (this.genesIVs == null) this.getIVs();
        if (this.genesIVs != null) this.genesIVs.getExpressed().setValue(ivs);
    }

    @Override
    public void setMove(final int i, final String moveName)
    {
        // do not blanket set moves on client, or when transformed.
        if (!(this.getEntity().getEntityWorld() instanceof ServerWorld) || this.getTransformedTo() != null) return;

        final String[] moves = this.getMoves();
        moves[i] = moveName;
        this.setMoves(moves);
    }

    @Override
    public void setMoves(final String[] moves)
    {
        // do not blanket set moves on client, or when transformed.
        if (!(this.getEntity().getEntityWorld() instanceof ServerWorld) || this.getTransformedTo() != null) return;
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
            this.genesMoves.getExpressed().setValue(this.getMoveStats().moves = moves);
        }
        PacketSyncGene.syncGeneToTracking(this.getEntity(), this.genesMoves);
    }

    @Override
    public void setNature(final Nature nature)
    {
        if (this.genesNature == null) this.getNature();
        if (this.genesNature != null) this.genesNature.getExpressed().setValue(nature);
    }

    @Override
    public IPokemob setPokedexEntry(final PokedexEntry newEntry)
    {
        final PokedexEntry entry = this.getPokedexEntry();
        final SpeciesInfo info = this.genesSpecies.getExpressed().getValue();
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

        if (this.getEntity().getEntityWorld() != null) ret.setSize((float) (ret.getSize() / PokecubeCore
                .getConfig().scalefactor));
        if (this.getEntity().getEntityWorld() != null && this.getEntity().isServerWorld()) PacketChangeForme
                .sendPacketToTracking(ret.getEntity(), newEntry);
        return ret;
    }

    @Override
    public void setRGBA(final int... colours)
    {
        final int[] rgba = this.getRGBA();
        for (int i = 0; i < colours.length && i < rgba.length; i++)
            rgba[i] = colours[i];
    }

    @Override
    public void setSexe(final byte sexe)
    {
        if (this.genesSpecies == null) this.getPokedexEntry();
        final SpeciesInfo info = this.genesSpecies.getExpressed().getValue();
        if (sexe == IPokemob.NOSEXE || sexe == IPokemob.FEMALE || sexe == IPokemob.MALE
                || sexe == IPokemob.SEXLEGENDARY) info.value = sexe;
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
        this.genesShiny.getExpressed().setValue(shiny);
        PacketSyncGene.syncGeneToTracking(this.getEntity(), this.genesShiny);
    }

    @Override
    public void setSize(float size)
    {
        if (this.genesSize == null) this.getSize();
        float a = 1, b = 1, c = 1;
        final PokedexEntry entry = this.getPokedexEntry();
        if (entry != null)
        {
            a = entry.width * size;
            b = entry.height * size;
            c = entry.length * size;
            // Do not allow them to be smaller than 1/100 of a block.
            if (a < 0.01 || b < 0.01 || c < 0.01)
            {
                final float min = 0.01f / Math.min(a, Math.min(c, b));
                size *= min;
            }
            // Do not allow them to be larger than 20 blocks.
            if (a > 20 || b > 20 || c > 20)
            {
                final float max = 20 / Math.max(a, Math.max(c, b));
                size *= max;
            }
            this.getEntity().getSize(this.getEntity().getPose()).scale(size);
        }
        this.genesSize.getExpressed().setValue(size);
    }
}
