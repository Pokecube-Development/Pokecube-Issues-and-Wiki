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

public class MakeHeldItem extends LootFunction
{
    public static class Serializer extends LootFunction.Serializer<MakeHeldItem>
    {
        public Serializer()
        {
            super(new ResourceLocation("pokecube_held"), MakeHeldItem.class);
        }

        @Override
        public MakeHeldItem deserialize(final JsonObject object, final JsonDeserializationContext deserializationContext,
                final ILootCondition[] conditionsIn)
        {
            final String arg = object.get("type").getAsString();
            return new MakeHeldItem(conditionsIn, arg);
        }

        @Override
        public void serialize(final JsonObject object, final MakeHeldItem functionClazz,
                final JsonSerializationContext serializationContext)
        {
            object.addProperty("type", functionClazz.arg);
        }
    }

    private static final Map<String, Integer> nameMap = Maps.newHashMap();

    final String arg;

    protected MakeHeldItem(final ILootCondition[] conditionsIn, final String arg)
    {
        super(conditionsIn);
        this.arg = arg;
    }

    @Override
    public ItemStack doApply(final ItemStack stack, final LootContext context)
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