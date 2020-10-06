package pokecube.core.items.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.ResourceLocation;
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
        public MakeBerry deserialize(final JsonObject object, final JsonDeserializationContext deserializationContext,
                final ILootCondition[] conditionsIn)
        {
            final String arg = object.get("type").getAsString();
            return new MakeBerry(conditionsIn, arg);
        }

        @Override
        public void serialize(final JsonObject object, final MakeBerry functionClazz, final JsonSerializationContext serializationContext)
        {
            object.addProperty("type", functionClazz.arg);
        }
    }

    final String arg;

    protected MakeBerry(final ILootCondition[] conditionsIn, final String arg)
    {
        super(conditionsIn);
        this.arg = arg;
    }

    @Override
    public ItemStack doApply(final ItemStack stack, final LootContext context)
    {
        final ItemStack berry = new ItemStack(BerryManager.getBerryItem(this.arg));
        if (berry.isEmpty()) PokecubeCore.LOGGER.error("Error making berry for " + this.arg);
        else return berry;
        return stack;
    }
}
