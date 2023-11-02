package pokecube.core.entity.genetics.genes;

import java.util.Random;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.SharedAttributes;
import pokecube.core.entity.genetics.GeneticsManager;
import thut.api.entity.genetics.Gene;
import thut.core.common.ThutCore;
import thut.core.common.genetics.genes.GeneFloat;

public class SizeGene extends GeneFloat
{
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
        if (this._last_set != this.value && entity instanceof LivingEntity living)
        {
            living.getAttribute(SharedAttributes.MOB_SIZE_SCALE.get()).setBaseValue(this.value);
            living.refreshDimensions();
            this._last_set = this.value;
        }
    }
}
