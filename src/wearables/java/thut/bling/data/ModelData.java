package thut.bling.data;

import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

public record ModelData(String tex, String model, String tex2, boolean showInTooltip) implements TooltipProvider
{
    public static final Codec<ModelData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("tex").forGetter(ModelData::tex),
            Codec.STRING.fieldOf("model").forGetter(ModelData::model),
            Codec.STRING.fieldOf("tex2").forGetter(ModelData::tex2),
            Codec.BOOL.optionalFieldOf("show_in_tooltip", Boolean.valueOf(true)).forGetter(ModelData::showInTooltip))
            .apply(instance, ModelData::new));
    public static final StreamCodec<ByteBuf, ModelData> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8,
            ModelData::tex, ByteBufCodecs.STRING_UTF8, ModelData::model, ByteBufCodecs.STRING_UTF8, ModelData::tex2,
            ByteBufCodecs.BOOL, ModelData::showInTooltip, ModelData::new);

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag)
    {
        if (this.showInTooltip)
        {
            if (tooltipFlag.isAdvanced())
            {
                tooltipAdder.accept(Component.translatable("bling.model.adv").withStyle(ChatFormatting.GRAY,
                        ChatFormatting.ITALIC));
            }
            else
            {
                tooltipAdder.accept(
                        Component.translatable("bling.model").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }
        }
    }

    public ModelData withTooltip(boolean showInTooltip)
    {
        return new ModelData(tex, model, tex2, showInTooltip);
    }
}
