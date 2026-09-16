package pokecube.api.data.moves;

import com.google.gson.JsonObject;

public class Animations
{
    public static class AnimationJson
    {
        public String preset;
        public JsonObject preset_values = null;
        public int duration = -1;
        public int starttick = 0;
        public String sound;

        public Boolean soundSource;
        public Boolean soundTarget;

        public Float volume;
        public Float pitch;

        public boolean applyAfter = false;

        @Override
        public String toString()
        {
            return "preset: " + this.preset + " duration:" + this.duration + " starttick:" + this.starttick
                    + " applyAfter:" + this.applyAfter;
        }
    }
}
