package pokecube.core.database.moves.json;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gson.Gson;

import net.minecraft.resources.ResourceLocation;
import pokecube.api.PokecubeAPI;
import pokecube.api.moves.Move_Base;
import pokecube.core.database.moves.MovesParser;
import pokecube.core.database.resources.PackFinder;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.zmoves.GZMoveManager;
import thut.api.util.JsonUtil;
import thut.core.common.ThutCore;

public class JsonMoves
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

    public static class FieldApplicator
    {
        Field field;
        IValueFixer fixer = input -> input;

        public FieldApplicator(final Field field)
        {
            this.field = field;
        }

        public FieldApplicator(final Field field, final IValueFixer ValueFixer)
        {
            this.field = field;
            this.fixer = ValueFixer;
        }

        public void apply(final String value, final Object obj)
        {
            try
            {
                this.field.set(obj, this.fixer.fix(value));
            }
            catch (IllegalArgumentException | IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static interface IValueFixer
    {
        String fix(String input);
    }

    public static class MoveJsonEntry
    {
        public String name;
        public String readableName;
        public String type;
        public String category;

        public String pp;
        public String pwr;
        public String acc;

        public String battleEffect;
        public String secondaryEffect;
        public String inDepthEffect;
        public String detailedEffect;

        public String effectRate;

        public String zMovesTo;
        public int zMovePower = -1;
        public String zEffect;
        public String zMove;
        public String zEntry;

        public String gMove;
        public String gMoveTo;
        public String gmaxEntry;
        public int gMovePower = -1;

        public String tmNum;
        public String speedPriority;
        public String target;

        public String contact;
        public String soundType;
        public String punchType;
        public String snatchable;

        public String defrosts;
        public String wideArea;
        public String magiccoat;
        public String protect;
        public String mirrormove;

        public String zVersion;

        public String defaultanimation;
        public String soundEffectSource;
        public String soundEffectTarget;

        public boolean multiTarget = false;
        public boolean interceptable = true;
        public String preset;
        public boolean ohko = false;
        public boolean protectionMoves = false;
        public int extraInfo = -1;

        public float cooldown_scale = 1.0f;

        public String customSize = null;

        public List<AnimationJson> animations;
    }

    public static class MovesJson implements Comparable<MovesJson>
    {
        public Integer priority = 100;

        public List<MoveJsonEntry> moves = new ArrayList<>();

        public void init()
        {
            this.moves.forEach(e -> e.name = JsonMoves.convertMoveName(e.name));
        }

        public void finish()
        {
            this.moves.sort((o1, o2) -> o1.name.compareTo(o2.name));
            GZMoveManager.init(this);
        }

        @Override
        public int compareTo(final MovesJson o)
        {
            return this.priority.compareTo(o.priority);
        }
    }

    private static Gson gson = JsonUtil.gson;

    public static MovesJson moves = new MovesJson();

    private static String convertMoveName(final String old)
    {
        String ret = "";
        final String name = ThutCore.trim(old);
        final String[] args = name.split(" ");
        for (final String arg : args) ret += arg;
        return ret;
    }

    private static MovesJson loadMoves(final ResourceLocation file)
    {
        try
        {
            final BufferedReader reader = PackFinder.getReader(file);
            if (reader == null) throw new FileNotFoundException(file.toString());
            MovesJson moves = JsonMoves.gson.fromJson(reader, MovesJson.class);
            moves.init();
            reader.close();
            return moves;
        }
        catch (final FileNotFoundException e)
        {
            PokecubeAPI.LOGGER.debug("No Moves File: {}", file);
        }
        catch (final Exception e)
        {
            PokecubeAPI.LOGGER.error("Error reading moves file " + file, e);
        }
        return null;
    }

    public static MovesJson merge(final ResourceLocation animationFile, final ResourceLocation movesFile)
    {
        MovesJson loaded = JsonMoves.loadMoves(movesFile);
        if (loaded == null)
        {
            PokecubeAPI.LOGGER.error("Error reading moves file " + movesFile);
            return null;
        }
        try
        {
            final BufferedReader reader = PackFinder.getReader(animationFile);
            final AnimsJson animations = JsonMoves.gson.fromJson(reader, AnimsJson.class);
            reader.close();
            final List<AnimationsJson> movesList = new ArrayList<>(animations.moves);
            for (final AnimationsJson anim : movesList)
                if (anim.defaultanimation == null && anim.animations.isEmpty()) animations.moves.remove(anim);
                else if (anim.defaultanimation != null)
            {
                final AnimationJson animation = new AnimationJson();
                animation.preset = anim.defaultanimation;
                anim.defaultanimation = null;
                anim.animations.add(animation);
            }
            animations.moves.sort((arg0, arg1) -> arg0.name.compareTo(arg1.name));
            final Map<String, AnimationsJson> animsMap = Maps.newHashMap();
            for (final AnimationsJson anims : animations.moves)
            {
                animsMap.put(JsonMoves.convertMoveName(anims.name), anims);
            }
            for (final MoveJsonEntry entry : loaded.moves)
            {
                String name = JsonMoves.convertMoveName(entry.name);
                if (animsMap.containsKey(name))
                {
                    AnimationsJson anims = animsMap.get(name);
                    entry.defaultanimation = anims.defaultanimation;
                    entry.animations = anims.animations;
                    entry.soundEffectSource = anims.soundEffectSource;
                    entry.soundEffectTarget = anims.soundEffectTarget;
                    anims.name = entry.name;
                }
            }
        }
        catch (final FileNotFoundException e)
        {
            PokecubeAPI.LOGGER.debug("No animation File: {}", animationFile);
        }
        catch (final Exception e)
        {
            PokecubeAPI.LOGGER.error("Error reading moves animation file " + animationFile, e);
        }
        return loaded;
    }

    public static void postProcess()
    {
        try
        {
            JsonMoves.moves.finish();
            MovesParser.load(JsonMoves.moves);
            final Collection<Move_Base> moves = MovesUtils.getKnownMoves();
            final Map<String, MoveJsonEntry> entryMap = Maps.newHashMap();
            for (final MoveJsonEntry entry : JsonMoves.moves.moves)
            {
                entryMap.put(entry.name, entry);
            }
            for (final Move_Base move : moves)
            {
                final MoveJsonEntry entry = entryMap.get(move.name);
                if (entry != null) move.move.baseEntry = entry;
                else if (!move.name.startsWith("pokemob.status")) PokecubeAPI.LOGGER.error("No Entry for " + move.name);
            }
            PokecubeAPI.LOGGER.debug("Processed " + JsonMoves.moves.moves.size() + " Moves.");
        }
        catch (IOException e)
        {
            PokecubeAPI.LOGGER.error("Error processing moves", e);
        }
    }
}
