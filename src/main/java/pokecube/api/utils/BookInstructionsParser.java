package pokecube.api.utils;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;
import thut.api.util.JsonUtil;

public class BookInstructionsParser
{
    public static List<String> getInstructions(ItemStack source, String start)
    {
        List<String> instructions = new ArrayList<>();
        if (source.hasTag() && source.getOrCreateTag().get("pages") instanceof ListTag list && !list.isEmpty())
        {
            start = start.strip();
            String start_key = start.endsWith(":") ? start : start + ":";
            list.forEach(tag -> {
                if (tag instanceof StringTag entry)
                {
                    String string = entry.getAsString();
                    if (!string.startsWith("{")) string = "{\"text\":\"" + string + "\"}";
                    var parsed = JsonUtil.gson.fromJson(string, JsonObject.class);
                    String[] lines = parsed.get("text").getAsString().strip().split("\n");
                    for (String s : lines)
                    {
                        s = s.strip();
                        if (s.isBlank()) continue;
                        // Allow headers, etc in the book
                        if (instructions.isEmpty() && !s.startsWith(start_key)) continue;
                        if (!instructions.isEmpty() && instructions.get(instructions.size() - 1).startsWith("end:"))
                            break;
                        instructions.add(s);
                    }
                }
            });
        }
        return instructions;
    }
}
