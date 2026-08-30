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
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.core.database.Database;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;

import java.util.List;
import java.util.function.Consumer;

public class EggLootEntry extends LootPoolSingletonContainer
{
    private final PokedexEntry entry;

    public EggLootEntry(String entry, int weight, int quality, List<LootItemCondition> conditions, List<LootItemFunction> functions)
    {
        super(weight, quality, conditions, functions);
        this.entry = Database.getEntry(entry);
    }

    @Override
    protected void createItemStack(Consumer<ItemStack> stackConsumer, LootContext lootContext)
    {
        try
        {
            ItemStack egg = ItemPokemobEgg.getEggStack(entry, lootContext.getLevel());
            stackConsumer.accept(egg);
        }
        catch (Exception e)
        {
            PokecubeAPI.LOGGER.error("Error making an egg stack", e);
        }
    }

    @Override
    public LootPoolEntryType getType()
    {
        return PokecubeLoot.EGG_LOOT.get();
    }

    // This is placed as a constant in EntityLootEntry.
    public static final MapCodec<EggLootEntry> CODEC = RecordCodecBuilder.mapCodec(inst ->
            // Add our own fields.
            inst.group(Codec.STRING.fieldOf("entry").forGetter(e->e.entry.getTrimmedName())
                    )
                    // Add common fields: weight, display, conditions, and functions.
                    .and(singletonFields(inst))
                    .apply(inst, EggLootEntry::new)
    );
}
