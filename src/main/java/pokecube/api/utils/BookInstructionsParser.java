package pokecube.api.utils;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gson.JsonObject;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;
import thut.api.maths.Vector3;
import thut.api.util.JsonUtil;

public class BookInstructionsParser
{
    @Nullable
    public static Vector3 vectorFromInstruction(String instruction)
    {
        try
        {
            if (instruction.contains(":"))
            {
                var arr = instruction.split(":");
                instruction = arr[arr.length - 1];
            }
            var args = instruction.contains(",") ? instruction.split(",") : instruction.split(" ");
            if (args.length == 3)
            {
                double x = 0;
                double y = 0;
                double z = 0;
                x = Double.parseDouble(args[0]);
                y = Double.parseDouble(args[1]);
                z = Double.parseDouble(args[2]);
                return new Vector3(x, y, z);
            }
        }
        catch (Exception e)
        {
            // players may type something silly, so just ignore it and return
            // null, the caller should deal with the null appropriately.
        }
        return null;
    }

    @Nullable
    public static BlockPos blockPosFromInstruction(String instruction)
    {
        try
        {
            if (instruction.contains(":"))
            {
                var arr = instruction.split(":");
                instruction = arr[arr.length - 1];
            }
            var args = instruction.contains(",") ? instruction.split(",") : instruction.split(" ");
            if (args.length == 3)
            {
                int x = 0;
                int y = 0;
                int z = 0;
                x = Integer.parseInt(args[0]);
                y = Integer.parseInt(args[1]);
                z = Integer.parseInt(args[2]);
                return new BlockPos(x, y, z);
            }
        }
        catch (Exception e)
        {
            // players may type something silly, so just ignore it and return
            // null, the caller should deal with the null appropriately.
        }
        return null;
    }

    @Nonnull
    public static List<String> getInstructions(ItemStack source, String start, boolean includeHeader)
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
                    try
                    {
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
                    catch (Exception e)
                    {
                        // Some items may have funny nbt tags added, which can cause this.
                    }
                }
            });
            if (!includeHeader && !instructions.isEmpty()) instructions.remove(0);
        }
        return instructions;
    }
}
