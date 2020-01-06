package pokecube.core.handlers.playerdata.advancements;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.advancements.AdvancementRewards;
import pokecube.core.database.PokedexEntry;

public class BadgeGen
{
    static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static JsonObject fromCriteria(String type)
    {
        final JsonObject critmap = new JsonObject();
        final JsonObject sub = new JsonObject();
        sub.addProperty("trigger", "minecraft:inventory_changed");
        final JsonObject conditions = new JsonObject();
        final JsonArray items = new JsonArray();
        final JsonObject item = new JsonObject();
        item.addProperty("item", "pokecube_adventures:badge");
        item.addProperty("nbt", "{\"type\":\"badge" + type + "\"}");
        items.add(item);
        conditions.add("items", items);
        sub.add("conditions", conditions);
        critmap.add("get_badge", sub);
        return critmap;
    }

    public static JsonObject fromInfo(String type)
    {
        final JsonObject displayJson = new JsonObject();
        final JsonObject icon = new JsonObject();
        icon.addProperty("item", "pokecube_adventures:badge");
        icon.addProperty("nbt", "{\"type\":\"badge" + type + "\"}");
        final JsonObject title = new JsonObject();
        title.addProperty("translate", "achievement.achievement.pokeadv.get.badge" + type);
        final JsonObject description = new JsonObject();
        description.addProperty("translate", "achievement.achievement.pokeadv.get.badge" + type + ".desc");
        displayJson.add("icon", icon);
        displayJson.add("title", title);
        displayJson.add("description", description);
        return displayJson;
    }

    public static String makeJson(String type, String parent)
    {
        final JsonObject json = new JsonObject();
        json.add("display", BadgeGen.fromInfo(type));
        json.add("criteria", BadgeGen.fromCriteria(type));
        if (parent != null) json.addProperty("parent", parent);
        return BadgeGen.GSON.toJson(json);
    }

    public static AdvancementRewards makeReward(PokedexEntry entry)
    {
        return null;
    }
}
