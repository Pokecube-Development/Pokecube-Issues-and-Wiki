package pokecube.api.data.moves;

import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;

import pokecube.api.data.moves.Animations.AnimationJson;
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
        public int min_turns = -1;
        public int max_turns = -1;
        public int min_hits = -1;
        public int max_hits = -1;
        public String type;
        public String ailment = "none";
        public int accuracy;
        public String target;
        public String damage_class;
        public String move_category = "damage";
        public int flinch_chance;
        public int crit_rate;
        public float cooldown = 1.0f;
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
        private Move move;
        public Animation animation;

        public boolean _aoe = false;
        public boolean _ohko = false;
        public boolean _protects = false;
        public boolean _infatuates = false;
        public boolean _multi_target = false;
        public int _effect_index = -1;
        public String _preset;

        // Additional cooldown for moves after use.
        public float _post_attack_delay_factor = 1;

        public String _effect_text_extend = "";
        public String _effect_text_simple = "";

        public String _sound_effect_source = null;
        public String _sound_effect_target = null;

        public float _status_chance = 0;
        public float _stat_chance = 0;

        public int _status_effects = 0;
        public int[] _stat_effects =
        { 0, 0, 0, 0, 0, 0, 0, 0 };

        public int _drain = 0;
        public int _healing = 0;

        public boolean _manually_defined = false;
        public boolean _implemented = false;
        public String _target_type = "user";

        public int _min_turns = -1;
        public int _max_turns = -1;
        public int _min_hits = -1;
        public int _max_hits = -1;

        public void preParse()
        {
            if (move != null)
            {
                // These can be null if the move does not specify them.
                if (move.effect_text_extend != null)
                    this._effect_text_extend = move.effect_text_extend.toLowerCase(Locale.ROOT).strip();
                if (move.effect_text_simple != null)
                    this._effect_text_simple = move.effect_text_simple.toLowerCase(Locale.ROOT).strip();
                this._preset = move.preset;
                this._drain = move.drain;
                this._healing = move.healing;
                this._target_type = move.target;

                this._sound_effect_source = move.sound_effect_source;
                this._sound_effect_target = move.sound_effect_target;

                this._min_turns = move.min_turns;
                this._max_turns = move.max_turns;
                this._min_hits = move.min_hits;
                this._max_hits = move.max_hits;

                if (this.move.cooldown > 0) this._post_attack_delay_factor = move.cooldown;
            }
        }

        public Move getMove()
        {
            return move;
        }

        public void setMove(Move move)
        {
            this.move = move;
        }
    }
}
