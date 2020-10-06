package pokecube.core.items.loot.functions;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.ResourceLocation;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;

public class MakeFossil extends LootFunction
{
    public static class Serializer extends LootFunction.Serializer<MakeFossil>
    {
        public Serializer()
        {
            super(new ResourceLocation("pokecube_fossil"), MakeFossil.class);
        }

        @Override
        public MakeFossil deserialize(final JsonObject object, final JsonDeserializationContext deserializationContext,
                final ILootCondition[] conditionsIn)
        {
            final String arg = object.get("type").getAsString();
            return new MakeFossil(conditionsIn, arg);
        }

        @Override
        public void serialize(final JsonObject object, final MakeFossil functionClazz,
                final JsonSerializationContext serializationContext)
        {
            object.addProperty("type", functionClazz.arg);
        }
    }

    private static final Map<String, Integer> fossils = Maps.newHashMap();

    final String arg;

    protected MakeFossil(final ILootCondition[] conditionsIn, final String arg)
    {
        super(conditionsIn);
        this.arg = arg;
    }

    @Override
    public ItemStack doApply(final ItemStack stack, final LootContext context)
    {
        if (MakeFossil.fossils.containsKey(this.arg))
        {
            final ItemStack newStack = PokecubeItems.getStack("fossil_" + this.arg);
            newStack.setCount(stack.getCount());
            newStack.setTag(stack.getTag());
            return newStack;
        }
        else PokecubeCore.LOGGER.error("Error making fossil for " + this.arg);
        return stack;
    }
}