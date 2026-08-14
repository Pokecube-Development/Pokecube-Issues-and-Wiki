package pokecube.adventures.blocks.genetics.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import io.netty.buffer.ByteBuf;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.registries.DeferredRegister;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSelector.ItemBasedSelector;

public class SelectorImpl
{
    public static SelectorValue defaultSelector = new SelectorValue(0.0f, 0.9f);

    public static class SelectorValue
    {
        public static DataResult<SelectorValue> read(CompoundTag tag)
        {
            try
            {
                return DataResult.success(load(tag));
            }
            catch (ResourceLocationException resourcelocationexception)
            {
                return DataResult.error(
                        () -> "Not a valid itemholder tag: " + tag + " " + resourcelocationexception.getMessage());
            }
        }

        public static SelectorValue load(final CompoundTag tag)
        {
            if (!tag.contains("S") || !tag.contains("D")) return SelectorImpl.defaultSelector;
            return new SelectorValue(tag.getFloat("S"), tag.getFloat("D"));
        }

        public static final Codec<SelectorValue> CODEC = CompoundTag.CODEC
                .<SelectorValue>comapFlatMap(SelectorValue::read, SelectorValue::save).stable();

        public static final StreamCodec<ByteBuf, SelectorValue> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG
                .map(SelectorValue::load, SelectorValue::save);

        public final float selectorDestructChance;

        public final float dnaDestructChance;

        public SelectorValue(final float select, final float dna)
        {
            this.selectorDestructChance = select;
            this.dnaDestructChance = dna;
        }

        @OnlyIn(Dist.CLIENT)
        public void addToTooltip(final List<Component> toolTip)
        {
            toolTip.add(Component.translatableEscape("container.geneselector.tooltip.a", this.selectorDestructChance));
            toolTip.add(Component.translatableEscape("container.geneselector.tooltip.b", this.dnaDestructChance));
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (obj instanceof SelectorValue sel) return sel.selectorDestructChance == this.selectorDestructChance
                    && sel.dnaDestructChance == this.dnaDestructChance;
            return false;
        }

        public CompoundTag save()
        {
            final CompoundTag tag = new CompoundTag();
            tag.putFloat("S", this.selectorDestructChance);
            tag.putFloat("D", this.dnaDestructChance);
            return tag;
        }

        @Override
        public String toString()
        {
            return this.selectorDestructChance + " " + this.dnaDestructChance;
        }
    }

    public static record GeneSelector(IGeneSelector selector, ResourceLocation key)
    {
        public GeneSelector(ResourceLocation key)
        {
            this(null, key);
        }

        public GeneSelector withSelector(ItemStack context)
        {
            IGeneSelector selector = SELECTOR_TYPES.getOrDefault(key, x -> new ItemBasedSelector(x)).apply(context);
            return new GeneSelector(selector, key);
        }

        public static final Codec<GeneSelector> CODEC = ResourceLocation.CODEC
                .<GeneSelector>comapFlatMap(GeneSelector::read, GeneSelector::key).stable();
        public static final StreamCodec<ByteBuf, GeneSelector> STREAM_CODEC = ResourceLocation.STREAM_CODEC
                .map(GeneSelector::new, GeneSelector::key);

        public static DataResult<GeneSelector> read(ResourceLocation key)
        {
            try
            {
                return DataResult.success(new GeneSelector(key));
            }
            catch (ResourceLocationException resourcelocationexception)
            {
                return DataResult.error(() -> "Not a valid linkable storage tag: " + key + " "
                        + resourcelocationexception.getMessage());
            }
        }
    }

    public static final Map<ResourceLocation, Function<ItemStack, IGeneSelector>> SELECTOR_TYPES = new HashMap<>();

    public static final ResourceLocation ID = ResourceLocation.parse("pokecube_adventures:gene_selector");

    public static Supplier<DataComponentType<GeneSelector>> SELECTOR_STORE;
    public static Supplier<DataComponentType<SelectorValue>> VALUE_STORE;

    public static void registerItemData(DeferredRegister<DataComponentType<?>> registry)
    {
        SELECTOR_STORE = registry.register("gene_selector", name -> new DataComponentType.Builder<GeneSelector>()
                .persistent(GeneSelector.CODEC).networkSynchronized(GeneSelector.STREAM_CODEC).build());
        VALUE_STORE = registry.register("gene_selector_value", name -> new DataComponentType.Builder<SelectorValue>()
                .persistent(SelectorValue.CODEC).networkSynchronized(SelectorValue.STREAM_CODEC).build());
    }
}
