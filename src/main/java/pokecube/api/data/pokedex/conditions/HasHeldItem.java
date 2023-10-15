package pokecube.api.data.pokedex.conditions;

import java.util.Map;

import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistries.Keys;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.utils.Tools;
import pokecube.core.database.pokedex.PokedexEntryLoader.Drop;

public class HasHeldItem implements PokemobCondition
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

    public void initFromDrop(Drop drop, boolean isTag)
    {
        Map<String, String> values = drop.getValues();
        if (isTag) tag = drop.id;
        else _value = Tools.getStack(values);
    }

    @Override
    public void init()
    {
        if (item != null) _value = CraftingHelper.getItemStack(item, true, true);
        if (!tag.isEmpty())
        {
            _tag = TagKey.create(Keys.ITEMS, new ResourceLocation(tag));
        }
    }
}