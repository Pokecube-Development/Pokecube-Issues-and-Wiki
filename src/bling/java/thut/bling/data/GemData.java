package thut.bling.data;

import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

public record GemData(int alpha, CompoundTag gemTag, ResourceLocation gem, boolean showInTooltip)
        implements TooltipProvider
{
    public GemData(int alpha, ItemStack stack, boolean showInTooltip, HolderLookup.Provider provider)
    {
        this(alpha, (CompoundTag) stack.saveOptional(provider), EMPTY, showInTooltip);
    }

    public static final ResourceLocation EMPTY = ResourceLocation.parse("b:b");

    public static final Codec<GemData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("alpha").forGetter(GemData::alpha),
            CompoundTag.CODEC.fieldOf("gemTag").forGetter(GemData::gemTag),
            ResourceLocation.CODEC.fieldOf("gemTag").forGetter(GemData::gem),
            Codec.BOOL.optionalFieldOf("show_in_tooltip", Boolean.valueOf(true)).forGetter(GemData::showInTooltip))
            .apply(instance, GemData::new));
    public static final StreamCodec<ByteBuf, GemData> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT,
            GemData::alpha, ByteBufCodecs.COMPOUND_TAG, GemData::gemTag, ResourceLocation.STREAM_CODEC, GemData::gem,
            ByteBufCodecs.BOOL, GemData::showInTooltip, GemData::new);

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag)
    {
        if (this.showInTooltip)
        {
            if (tooltipFlag.isAdvanced())
            {
                tooltipAdder.accept(
                        Component.translatable("bling.gem.adv").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }
            else
            {
                tooltipAdder.accept(
                        Component.translatable("bling.gem").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }
        }
    }

    public GemData withToolGem(String gem)
    {
        return new GemData(alpha, gemTag, ResourceLocation.parse(gem), showInTooltip);
    }

    public GemData withTooltip(boolean showInTooltip)
    {
        return new GemData(alpha, gemTag, gem, showInTooltip);
    }
}
