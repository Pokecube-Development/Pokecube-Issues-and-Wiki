package pokecube.gimmicks.mega.conditions;

import com.google.gson.JsonObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.MoveEntry;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeItems;
import pokecube.core.items.ItemTM;

public class ItemMoveLearn implements MegaCondition
{
    public JsonObject item_mainhand = null;
    public JsonObject item_offhand = null;
    public String tag_mainhand = "";
    public String tag_offhand = "";
    public ItemStack _value_mainhand = ItemStack.EMPTY;
    public ItemStack _value_offhand = ItemStack.EMPTY;
    public TagKey<Item> _tag_mainhand = null;
    public TagKey<Item> _tag_offhand = null;
    public String moveLearnt = "";
    public MoveEntry _moveLearnt = null;

    @Override
    public boolean matches(IPokemob mobIn, PokedexEntry entryTo)
    {
        // If both the main item and offhand are there, learn the move, consume both items required
        boolean correct_mainhand = ItemStack.isSameItem(mobIn.getHeldItem(), _value_mainhand);
        boolean correct_offhand = ItemStack.isSameItem(mobIn.getEntity().getOffhandItem(), _value_offhand);
        if (correct_mainhand && correct_offhand)
        {
            ItemStack heldItem = mobIn.getHeldItem();
            ItemStack offhandItem = mobIn.getEntity().getOffhandItem();
            ItemStack learntMoveTM = new ItemStack(PokecubeItems.TM.get());

            ItemTM.addMoveToStack(learntMoveTM, _moveLearnt.getName());
            if (ItemTM.feedToPokemob(learntMoveTM, mobIn.getEntity())) {

               if (heldItem.getCount() > 1) heldItem.grow(-1);
               else mobIn.setHeldItem(ItemStack.EMPTY);

               if (offhandItem.getCount() > 1) offhandItem.grow(-1);
               else mobIn.getEntity().setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            }
            else
                PokecubeAPI.logInfo(_moveLearnt.getName() + " was not taught to " + mobIn.getDisplayName().getString() + " although the conditions were met");
        }

        return true; // Always returns true to allow the mega to commence.
    }

    @Override
    public void init(HolderLookup.Provider registries)
    { // Populates fields starting with _ from the data provided in a json
        if (moveLearnt != null)
            _moveLearnt = MoveEntry.get(moveLearnt);

        //set item_mainhand/offhand and tag_mainhand/offhand
        var element_main = item_mainhand.get("id");
        var element_off = item_offhand.get("id");
        tag_mainhand = element_main.getAsString();
        tag_offhand = element_off.getAsString();

        if (item_mainhand != null && item_offhand != null)
        {
            _value_mainhand = Tools.getStack(item_mainhand);
            _value_offhand = Tools.getStack(item_offhand);
        }
        if (!tag_mainhand.isEmpty() && !tag_offhand.isEmpty())
        {
            _tag_mainhand = TagKey.create(Registries.ITEM, ResourceLocation.parse(tag_mainhand));
            _tag_offhand = TagKey.create(Registries.ITEM, ResourceLocation.parse(tag_offhand));
        }
    }
}
