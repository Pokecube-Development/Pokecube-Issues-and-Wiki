package pokecube.core.handlers.loot_tables;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import pokecube.core.items.ItemTM;

import java.util.List;
import java.util.function.Consumer;

public class TMLootEntry extends LootPoolSingletonContainer
{
    private final String move;

    public TMLootEntry(String move, int weight, int quality, List<LootItemCondition> conditions, List<LootItemFunction> functions)
    {
        super(weight, quality, conditions, functions);
        this.move = move;
    }

    @Override
    protected void createItemStack(Consumer<ItemStack> stackConsumer, LootContext lootContext)
    {
        ItemStack tm = ItemTM.getTM(move);
        stackConsumer.accept(tm);
    }

    @Override
    public LootPoolEntryType getType()
    {
        return PokecubeLoot.TM_LOOT.get();
    }

    // This is placed as a constant in EntityLootEntry.
    public static final MapCodec<TMLootEntry> CODEC = RecordCodecBuilder.mapCodec(inst ->
            // Add our own fields.
            inst.group(Codec.STRING.fieldOf("move").forGetter(e->e.move)
                    )
                    // Add common fields: weight, display, conditions, and functions.
                    .and(singletonFields(inst))
                    .apply(inst, TMLootEntry::new)
    );
}
