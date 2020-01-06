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

public class MakeHeldItem extends LootFunction
{
    public static class Serializer extends LootFunction.Serializer<MakeHeldItem>
    {
        public Serializer()
        {
            super(new ResourceLocation("pokecube_held"), MakeHeldItem.class);
        }

        @Override
        public MakeHeldItem deserialize(JsonObject object, JsonDeserializationContext deserializationContext,
                ILootCondition[] conditionsIn)
        {
            final String arg = object.get("type").getAsString();
            return new MakeHeldItem(conditionsIn, arg);
        }

        @Override
        public void serialize(JsonObject object, MakeHeldItem functionClazz,
                JsonSerializationContext serializationContext)
        {
            object.addProperty("type", functionClazz.arg);
        }
    }

    private static final Map<String, Integer> nameMap = Maps.newHashMap();

    final String arg;

    protected MakeHeldItem(ILootCondition[] conditionsIn, String arg)
    {
        super(conditionsIn);
        this.arg = arg;
    }

    @Override
    public ItemStack doApply(ItemStack stack, LootContext context)
    {
        if (MakeHeldItem.nameMap.containsKey(this.arg))
        {
            final ItemStack newStack = PokecubeItems.getStack(this.arg);
            newStack.setCount(stack.getCount());
            newStack.setTag(stack.getTag());
            return newStack;
        }
        else PokecubeCore.LOGGER.error("Error making held item for " + this.arg);
        return stack;
    }
}