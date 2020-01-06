package pokecube.core.items.loot.functions;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootFunction;
import net.minecraft.world.storage.loot.conditions.ILootCondition;
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
        public MakeFossil deserialize(JsonObject object, JsonDeserializationContext deserializationContext,
                ILootCondition[] conditionsIn)
        {
            final String arg = object.get("type").getAsString();
            return new MakeFossil(conditionsIn, arg);
        }

        @Override
        public void serialize(JsonObject object, MakeFossil functionClazz,
                JsonSerializationContext serializationContext)
        {
            object.addProperty("type", functionClazz.arg);
        }
    }

    private static final Map<String, Integer> fossils = Maps.newHashMap();

    final String arg;

    protected MakeFossil(ILootCondition[] conditionsIn, String arg)
    {
        super(conditionsIn);
        this.arg = arg;
    }

    @Override
    public ItemStack doApply(ItemStack stack, LootContext context)
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