package pokecube.api.data.trainers;

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TradeData
{
    public static class Trade
    {
        public String custom;
        public String type = "preset";
        public JsonElement sell;
        public int maxUses = Integer.MAX_VALUE;
        public int exp = 1;
        public int demand = 0;
        public float multiplier = 0.05f;
        public int count = -1;

        public final List<JsonElement> buys = new ArrayList<>();

        public Map<String, JsonElement> values = new HashMap<>();
    }

    public static class TradeEntry
    {
        public String template = "default";

        public final List<Trade> trades = new ArrayList<>();
    }

    public static class ProfiessionStage
    {
        public int level;
        public boolean clear_old = false;

        public final List<Trade> trades = new ArrayList<>();
    }

    public static class ProfessionEntry
    {
        public String profession;
        public String type = "";

        public final List<ProfiessionStage> stages = new ArrayList<>();
    }

    public static class TradeDatabase
    {
        public final List<TradeEntry> trades = new ArrayList<>();
        public final List<ProfessionEntry> professions = new ArrayList<>();
    }
}
