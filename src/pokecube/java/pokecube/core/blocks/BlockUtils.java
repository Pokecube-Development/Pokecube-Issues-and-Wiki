package pokecube.core.blocks;

import java.util.function.BiFunction;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class BlockUtils
{
    public static <B extends Block, T> MapCodec<B> singleArgumentCodec(
            BiFunction<BlockBehaviour.Properties, T, B> factory, RecordCodecBuilder<B, T> codec)
    {
        return RecordCodecBuilder.mapCodec(inst -> inst.group(BlockBehaviour.propertiesCodec(), codec).apply(inst, factory));
    }
    
}
