package pokecube.core.items.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootFunction;
import net.minecraft.world.storage.loot.conditions.ILootCondition;
import pokecube.core.PokecubeCore;
import pokecube.core.items.berries.BerryManager;

public class MakeBerry extends LootFunction
{
    public static class Serializer extends LootFunction.Serializer<MakeBerry>
    {
        public Serializer()
        {
            super(new ResourceLocation("pokecube_berry"), MakeBerry.class);
        }

        @Override
        public MakeBerry deserialize(JsonObject object, JsonDeserializationContext deserializationContext,
                ILootCondition[] conditionsIn)
        {
            final String arg = object.get("type").getAsString();
            return new MakeBerry(conditionsIn, arg);
        }

        @Override
        public void serialize(JsonObject object, MakeBerry functionClazz, JsonSerializationContext serializationContext)
        {
            object.addProperty("type", functionClazz.arg);
        }
    }

    final String arg;

    protected MakeBerry(ILootCondition[] conditionsIn, String arg)
    {
        super(conditionsIn);
        this.arg = arg;
    }

    @Override
    public ItemStack doApply(ItemStack stack, LootContext context)
    {
        final ItemStack berry = new ItemStack(BerryManager.getBerryItem(this.arg));
        if (berry.isEmpty()) PokecubeCore.LOGGER.error("Error making berry for " + this.arg);
        else return berry;
        return stack;
    }
}
