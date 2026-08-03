package pokecube.core.entity.genetics.genes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import pokecube.api.entity.SharedAttributes;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.PokecubeCore;
import pokecube.core.entity.genetics.GeneticsManager;
import pokecube.core.network.pokemobs.PacketSyncModifier;
import thut.api.entity.genetics.Gene;
import thut.core.common.ThutCore;
import thut.core.common.genetics.genes.GeneFloat;

import java.util.Random;

public class SizeGene extends GeneFloat
{
    private static final ResourceLocation SIZE_GENE = ResourceLocation.parse("pokecube:size_gene");

    public static void setScale(IPokemob pokemob, float size)
    {
        var gene = pokemob.getGenes().getAlleles(GeneticsManager.SIZEGENE).getExpressed();
        gene.setValue(size);
        // The gene will then call the below setScale when it next ticks.
    }

    public static double setScale(LivingEntity entity, double size)
    {
        double before = SharedAttributes.getScale(entity);

        double baseSizeMax = Math.max(entity.getType().getHeight(), entity.getType().getWidth());
        double baseSizeMin = Math.min(entity.getType().getHeight(), entity.getType().getWidth());

        double maxSize = PokecubeCore.getConfig().maxMobSize / baseSizeMax;
        double minSize = PokecubeCore.getConfig().minMobSize / baseSizeMin;
        size = Math.max(minSize, Math.min(maxSize, size));

        entity.getAttributes().getInstance(SharedAttributes.MOB_SIZE_SCALE).addOrReplacePermanentModifier(
                new AttributeModifier(SIZE_GENE, size - 1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        if (SharedAttributes.getScale(entity) != before)
        {
            entity.refreshDimensions();
            PacketSyncModifier.sendUpdate(entity);
        }
        return size;
    }

    public static float getScale(IPokemob pokemob)
    {
        return (float) pokemob.getGenes().getAlleles(GeneticsManager.SIZEGENE).getExpressed().getValue();
    }

    public static float scaleFactor = 0.075f;
    Random rand = ThutCore.newRandom();
    float _last_set = -1;

    public SizeGene()
    {
        this.value = 1f;
    }

    @Override
    public ResourceLocation getKey()
    {
        return GeneticsManager.SIZEGENE;
    }

    @Override
    public float getMutationRate()
    {
        return GeneticsManager.mutationRates.get(this.getKey());
    }

    @Override
    public Gene<Float> interpolate(final Gene<Float> other)
    {
        final SizeGene newGene = new SizeGene();
        final SizeGene otherG = (SizeGene) other;
        newGene.value = this.rand.nextBoolean() ? otherG.value : this.value;
        return newGene;
    }

    @Override
    public Gene<Float> mutate()
    {
        final SizeGene newGene = new SizeGene();
        final float factor = SizeGene.scaleFactor * (this.value > 1 ? 1 / this.value : this.value);
        newGene.value = this.value + factor * (float) ThutCore.newRandom().nextGaussian();
        newGene.value = Math.abs(newGene.value);
        return newGene;
    }

    @Override
    public void onUpdateTick(Entity entity)
    {
        if (value < 0.01f) value = 0.01f;
        if (value > 100f) value = 100f;
        if (entity instanceof LivingEntity living && _last_set != this.value)
        {
            value = (float) setScale(living, value);
            this._last_set = this.value;
        }
    }
}
