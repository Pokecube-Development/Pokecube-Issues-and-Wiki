package pokecube.core.handlers.loot_tables;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import pokecube.core.PokecubeItems;

import java.util.List;

public class CandyLootFunction extends LootItemConditionalFunction
{
    public static final MapCodec<CandyLootFunction> CODEC =
            RecordCodecBuilder.mapCodec(inst->commonFields(inst).apply(inst, CandyLootFunction::new));

    protected CandyLootFunction(List<LootItemCondition> predicates)
    {
        super(predicates);
    }

    @Override
    public LootItemFunctionType<? extends LootItemConditionalFunction> getType()
    {
        return PokecubeLoot.VALIDATE_CANDY.get();
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context)
    {
        // Don't validate the candy if it isn't actually being added to a level
        if (context.getLevel() == null)
        {
            stack.set(DataComponents.ITEM_NAME, Component.translatable("item.pokecube.candy.rare"));
            return stack;
        }
        stack.setCount(1);
        PokecubeItems.makeStackValid(stack);
        return stack;
    }
}
