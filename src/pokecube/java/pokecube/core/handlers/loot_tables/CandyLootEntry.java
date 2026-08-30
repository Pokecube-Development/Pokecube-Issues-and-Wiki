package pokecube.core.handlers.loot_tables;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import pokecube.core.PokecubeItems;

import java.util.List;
import java.util.function.Consumer;

public class CandyLootEntry extends LootPoolSingletonContainer
{
    public CandyLootEntry(int weight, int quality, List<LootItemCondition> conditions, List<LootItemFunction> functions)
    {
        super(weight, quality, conditions, functions);
    }

    @Override
    protected void createItemStack(Consumer<ItemStack> stackConsumer, LootContext lootContext)
    {
        // Don't validate the candy if it isn't actually being added to a level
        if (lootContext.getLevel() == null)
        {
            ItemStack candy = PokecubeItems.getStack("candy");
            candy.set(DataComponents.ITEM_NAME, Component.translatable("item.pokecube.candy.rare"));
            stackConsumer.accept(candy);
            return;
        }
        ItemStack candy = PokecubeItems.makeCandyStack();
        stackConsumer.accept(candy);
    }

    @Override
    public LootPoolEntryType getType()
    {
        return PokecubeLoot.CANDY_LOOT.get();
    }

    // This is placed as a constant in EntityLootEntry.
    public static final MapCodec<CandyLootEntry> CODEC =
            RecordCodecBuilder.mapCodec(inst->singletonFields(inst).apply(inst, CandyLootEntry::new));
}
