package thut.bling.data;

import java.util.UUID;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

public record SmallBagData(UUID uuid, String bagName, String boxName, boolean showInTooltip) implements TooltipProvider
{

    public static final Codec<SmallBagData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("uuid").forGetter(SmallBagData::uuid),
            Codec.STRING.fieldOf("bagName").forGetter(SmallBagData::bagName),
            Codec.STRING.fieldOf("boxName").forGetter(SmallBagData::boxName),
            Codec.BOOL.optionalFieldOf("show_in_tooltip", Boolean.TRUE).forGetter(SmallBagData::showInTooltip))
            .apply(instance, SmallBagData::new));
    public static final StreamCodec<ByteBuf, SmallBagData> STREAM_CODEC = StreamCodec.composite(UUIDUtil.STREAM_CODEC,
            SmallBagData::uuid, ByteBufCodecs.STRING_UTF8, SmallBagData::bagName, ByteBufCodecs.STRING_UTF8,
            SmallBagData::boxName, ByteBufCodecs.BOOL, SmallBagData::showInTooltip, SmallBagData::new);

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag)
    {
        if (this.showInTooltip)
        {
            if (tooltipFlag.isAdvanced())
            {
                tooltipAdder.accept(Component.translatable("bling.bag.uuid", uuid.toString())
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }
            else
            {
                tooltipAdder.accept(Component.translatable("bling.bag.named", bagName, boxName)
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }
        }
    }

    public SmallBagData withTooltip(boolean showInTooltip)
    {
        return new SmallBagData(uuid, bagName, boxName, showInTooltip);
    }
}
