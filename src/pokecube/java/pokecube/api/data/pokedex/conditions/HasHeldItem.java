package pokecube.api.data.pokedex.conditions;

import com.google.gson.JsonObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.utils.Tools;

/**
 * This class matches a pokemob with the specified held or evolution item<br>
 * <br>
 * Matcher key: "item" <br> Json keys: <br> "item" - JsonObject, optional, recipe format json for the item "tag" -
 * String, optional, tag for the item
 */
@Condition(name = "item")
public class HasHeldItem extends PokemobCondition
{
    public JsonObject item = null;
    public String tag = "";
    public ItemStack _value = ItemStack.EMPTY;
    public TagKey<Item> _tag = null;

    @Override
    public boolean matches(IPokemob mobIn)
    {
        boolean heldMatched = false;

        if (_tag != null && mobIn.getHeldItem().is(_tag)) return true;
        if (!this._value.isEmpty())
        {
            heldMatched = Tools.isSameStack(this._value, mobIn.getHeldItem(), true);
        }
        if (heldMatched) return true;

        if (_tag != null && mobIn.getEvolutionStack().is(_tag)) return true;
        if (!this._value.isEmpty())
        {
            return Tools.isSameStack(this._value, mobIn.getEvolutionStack(), true);
        }
        return false;
    }

    @Override
    public void init(HolderLookup.Provider registries)
    {
        if (item != null)
        {
            if (item.has("item")) item.add("id", item.get("item"));
            if (item.has("n")) item.add("count", item.get("n"));
            item.remove("item");
            item.remove("n");
        }
        check:
        if (item != null && item.has("id"))
        {
            var element = item.get("id");
            if (!element.isJsonPrimitive()) break check;
            String id = element.getAsString();
            if (id.contains("#"))
            {
                this.tag = id;
                this.item = null;
            }
            else
            {
                ResourceLocation loc = ResourceLocation.parse(id);
                if (!BuiltInRegistries.ITEM.containsKey(loc))
                {
                    this.item = null;
                    this.tag = id;
                }
            }
        }
        if (item != null)
        {
            _value = Tools.getStack(item);
        }
        if (!tag.isEmpty())
        {
            _tag = TagKey.create(Registries.ITEM, ResourceLocation.parse(tag));
        }
    }

    @Override
    public Component makeDescription()
    {
        Component message = null;
        if (!_value.isEmpty()) message = Component.translatableEscape("pokemob.description.evolve.item",
                this._value.getHoverName().getString());
        else if (_tag != null)
        {
            var opt = BuiltInRegistries.ITEM.getTag(_tag).stream().findFirst();
            if (opt.isPresent())
            {
                ItemStack stack = new ItemStack(opt.get().get(0));
                message = Component.translatableEscape("pokemob.description.evolve.item", stack.getHoverName().getString());
            }
        }
        return message;
    }
}