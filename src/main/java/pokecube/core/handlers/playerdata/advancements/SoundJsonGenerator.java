package pokecube.core.handlers.playerdata.advancements;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class SoundJsonGenerator
{
    public static String generateSoundJson()
    {
        final JsonObject soundJson = new JsonObject();
        final List<PokedexEntry> baseFormes = Lists.newArrayList(Database.baseFormes.values());
        Collections.sort(baseFormes, (o1, o2) -> o1.getPokedexNb() - o2.getPokedexNb());
        for (final PokedexEntry entry : baseFormes)
        {
            final JsonObject soundEntry = new JsonObject();
            soundEntry.addProperty("category", "hostile");
            soundEntry.addProperty("subtitle", entry.getUnlocalizedName());
            final JsonArray sounds = new JsonArray();
            for (int i = 0; i < 3; i++)
            {
                final JsonObject sound = new JsonObject();
                sound.addProperty("name", "pokecube:mobs/" + entry.getTrimmedName());
                sound.addProperty("volume", i == 0 ? 0.8 : i == 1 ? 0.9 : 1);
                sound.addProperty("pitch", i == 0 ? 0.9 : i == 1 ? 0.95 : 1);
                sounds.add(sound);
            }
            soundEntry.add("sounds", sounds);
            soundJson.add("mobs." + entry.getTrimmedName(), soundEntry);
        }
        return AdvancementGenerator.GSON.toJson(soundJson);
    }
}
