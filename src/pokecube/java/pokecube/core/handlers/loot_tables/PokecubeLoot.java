package pokecube.core.handlers.loot_tables;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import pokecube.core.PokecubeCore;

import java.util.function.Supplier;

public class PokecubeLoot
{
    public static final DeferredRegister<LootPoolEntryType> LOOT_POOL_ENTRY_TYPES =
            DeferredRegister.create(Registries.LOOT_POOL_ENTRY_TYPE, PokecubeCore.MODID);
    public static final DeferredRegister<LootItemFunctionType<?>> LOOT_FUNCTION_TYPES =
            DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, PokecubeCore.MODID);

    public static final Supplier<LootPoolEntryType> EGG_LOOT =
            LOOT_POOL_ENTRY_TYPES.register("pokemob_egg", () -> new LootPoolEntryType(EggLootEntry.CODEC));
    public static final Supplier<LootPoolEntryType> TM_LOOT =
            LOOT_POOL_ENTRY_TYPES.register("tm", () -> new LootPoolEntryType(TMLootEntry.CODEC));
    public static final Supplier<LootPoolEntryType> CANDY_LOOT =
            LOOT_POOL_ENTRY_TYPES.register("candy", () -> new LootPoolEntryType(CandyLootEntry.CODEC));
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final Supplier<LootItemFunctionType<CandyLootFunction>> VALIDATE_CANDY =
            LOOT_FUNCTION_TYPES.register("validate_candy", () -> new LootItemFunctionType(CandyLootFunction.CODEC));

    public static void init(IEventBus bus)
    {
        LOOT_POOL_ENTRY_TYPES.register(bus);
        LOOT_FUNCTION_TYPES.register(bus);
    }
}
