package thut.api.block.flowing.functions;

import java.util.List;
import java.util.function.Supplier;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import thut.api.block.flowing.IFlowingBlock;
import thut.core.common.ThutCore;

public class LootLayerFunction extends LootItemConditionalFunction
{
    public static final MapCodec<LootLayerFunction> CODEC = RecordCodecBuilder.mapCodec(
    		instance -> commonFields(instance).apply(instance, LootLayerFunction::new)
        );
	

    public static Supplier<LootItemFunctionType<LootLayerFunction>> TYPE = ThutCore.RegistryEvents.LOOTTYPE
            .register("flowing_layer_loot", () -> new LootItemFunctionType<>(CODEC));

    public static void init()
    {}

    protected LootLayerFunction(List<LootItemCondition> conds)
    {
        super(conds);
    }

    @Override
    public LootItemFunctionType<LootLayerFunction> getType()
    {
        return TYPE.get();
    }

    @Override
    protected ItemStack run(ItemStack stack_in, LootContext context)
    {
        BlockState blockstate = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        if (blockstate != null && blockstate.getBlock() instanceof IFlowingBlock b)
        {
            int amt = (int) Math.ceil(b.getAmount(blockstate) / 2.0f);
            if (amt > 0) stack_in.setCount(amt);
        }
        return stack_in;
    }

    public static LootItemConditionalFunction.Builder<?> make()
    {
        return simpleBuilder((conds) -> {
            return new LootLayerFunction(conds);
        });
    }
}
