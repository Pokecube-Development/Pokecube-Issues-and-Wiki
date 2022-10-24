package pokecube.core.database.moves.json;

import java.util.ArrayList;
import java.util.List;

public class Animations
{
    public static class AnimationJson
    {
        public String preset;
        public String duration = "5";
        public String starttick = "0";
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

    public static class AnimationsJson
    {
        public String name;
        public String defaultanimation;
        public String soundEffectSource;
        public String soundEffectTarget;
        public List<AnimationJson> animations = new ArrayList<>();
    }

    public static class AnimsJson
    {
        public List<AnimationsJson> moves = new ArrayList<>();
    }
}
