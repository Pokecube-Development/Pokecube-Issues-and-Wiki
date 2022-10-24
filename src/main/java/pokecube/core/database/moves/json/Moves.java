package pokecube.core.database.moves.json;

import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;

import pokecube.core.database.moves.json.Animations.AnimationJson;
import pokecube.core.database.pokedex.PokedexEntryLoader.IMergeable;

public class Moves
{
    public class Move implements Comparable<Move>, IMergeable<Move>
    {
        public boolean replace = false;
        public boolean remove = false;
        public int sort_order = 100;

        public String name;
        public int power = -1;
        public int pp;
        public int priority;
        public int drain;
        public int healing;
        public String type;
        public int accuracy;
        public String target;
        public String damage_class;
        public String move_category;
        public int flinch_chance;
        public int crit_rate;
        public String effect_text_extend;
        public String effect_text_simple;
        public int effect_chance;
        public String preset;

        public String sound_effect_source;
        public String sound_effect_target;

        @Override
        public int compareTo(Move o)
        {
            return Integer.compare(sort_order, o.sort_order);
        }

        @Override
        public Move mergeFrom(Move other)
        {
            if (this.remove) return this;
            if (other.replace || other.remove)
            {
                return other;
            }
            mergeBasic(other);
            return this;
        }
    }

    public class Animation implements Comparable<Animation>, IMergeable<Animation>
    {
        public boolean replace = false;
        public boolean remove = false;
        public int sort_order = 100;

        public String name;
        public List<AnimationJson> animations = Lists.newArrayList();

        @Override
        public int compareTo(Animation o)
        {
            return Integer.compare(sort_order, o.sort_order);
        }

        @Override
        public Animation mergeFrom(Animation other)
        {
            if (this.remove) return this;
            if (other.replace || other.remove)
            {
                return other;
            }
            mergeBasic(other);
            return this;
        }
    }

    public static List<MoveHolder> ALL_MOVES = Lists.newArrayList();

    public static class MoveHolder
    {
        public Move move;
        public Animation animation;

        public boolean _multi_target = false;
        public boolean _interceptable = true;
        public boolean _ohko = false;
        public boolean _protects = false;
        public boolean _infatuates = false;
        public int _effect_index = -1;
        public String _preset;

        public String _effect_text_extend = "";
        public String _effect_text_simple = "";

        public float _status_chance = 0;
        public float _stat_chance = 0;

        public int _status_effects = 0;
        public int[] _stat_effects =
        { 0, 0, 0, 0, 0, 0, 0, 0 };

        public void preParse()
        {
            // These can be null if the move does not specify them.
            if (move.effect_text_extend != null)
                this._effect_text_extend = move.effect_text_extend.toLowerCase(Locale.ROOT);
            if (move.effect_text_simple != null)
                this._effect_text_simple = move.effect_text_simple.toLowerCase(Locale.ROOT);
            this._preset = move.preset;
        }
    }
}
