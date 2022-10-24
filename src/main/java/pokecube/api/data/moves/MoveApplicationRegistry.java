package pokecube.api.data.moves;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.common.collect.Maps;

import pokecube.api.entity.IOngoingAffected.IOngoingEffect;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.Damage;
import pokecube.api.moves.utils.MoveApplication.OngoingApplier;
import pokecube.api.moves.utils.target_types.All;
import pokecube.api.moves.utils.target_types.AllOther;
import pokecube.api.moves.utils.target_types.Ally;
import pokecube.api.moves.utils.target_types.RandomEnemy;
import pokecube.api.moves.utils.target_types.SelectedTarget;
import pokecube.api.moves.utils.target_types.User;

public class MoveApplicationRegistry
{
    public static enum MergeOrder
    {
        REPLACE, AFTER, BEFORE;
    }

    private static Map<String, Consumer<MoveApplication>> MOVE_MODIFIERS = Maps.newHashMap();
    private static Map<String, Predicate<MoveApplication>> TARGET_REGISTRY = Maps.newHashMap();
    private static Map<String, OngoingApplier> EFFECT_REGISTRY = Maps.newHashMap();

    static
    {
        // "specific-move"?
        // "selected-pokemon-me-first"?
        TARGET_REGISTRY.put("ally", Ally.INSTANCE);
        // "users-field"?
        TARGET_REGISTRY.put("user-or-ally", Ally.INSTANCE.or(User.INSTANCE));
        // "opponents-field"?
        TARGET_REGISTRY.put("user", User.INSTANCE);
        TARGET_REGISTRY.put("random-opponent", RandomEnemy.INSTANCE);
        TARGET_REGISTRY.put("all-other-pokemon", AllOther.INSTANCE);
        TARGET_REGISTRY.put("all-opponents", AllOther.INSTANCE.and(Ally.INSTANCE.negate()));
        TARGET_REGISTRY.put("selected-pokemon", SelectedTarget.INSTANCE);
        // "entire-field"?
        TARGET_REGISTRY.put("user-and-allies", Ally.INSTANCE.or(User.INSTANCE));
        TARGET_REGISTRY.put("all-pokemon", All.INSTANCE);
        TARGET_REGISTRY.put("all-allies", AllOther.INSTANCE.and(Ally.INSTANCE));

    }

    public static void registerOngoingEffect(MoveEntry entry, Function<Damage, IOngoingEffect> provider)
    {
        EFFECT_REGISTRY.put(entry.name, OngoingApplier.fromFunction(provider));
        entry.root_entry._manually_defined = true;

    }

    public static void addMoveModifier(MoveEntry entry, MergeOrder order, Consumer<MoveApplication> apply)
    {
        entry.root_entry._manually_defined = true;
        String name = entry.name;
        Consumer<MoveApplication> previous = MOVE_MODIFIERS.get(name);
        if (previous == null)
        {
            // Simple case, we just add it.
            MOVE_MODIFIERS.put(name, apply);
        }
        else
        {
            // Order based on order.
            switch (order)
            {
            case AFTER:
                MOVE_MODIFIERS.put(name, previous.andThen(apply));
                break;
            case BEFORE:
                MOVE_MODIFIERS.put(name, apply.andThen(previous));
                break;
            case REPLACE:
                MOVE_MODIFIERS.put(name, apply);
                break;
            default:
                break;
            }
        }
    }

    public static Predicate<MoveApplication> getValidator(MoveEntry move)
    {
        return TARGET_REGISTRY.getOrDefault(move.root_entry._target_type, SelectedTarget.INSTANCE);
    }

    public static void apply(MoveApplication moveApplication)
    {
        // Initialise things which may have been changed via addons, etc
        if (MOVE_MODIFIERS.containsKey(moveApplication.getName()))
        {
            MOVE_MODIFIERS.get(moveApplication.getName()).accept(moveApplication);
        }

        // TODO hit rate checker in MoveApplication
        int hits = moveApplication.getMove().root_entry._max_hits;
        if (hits > 0)
        {
            int min = moveApplication.getMove().root_entry._min_hits;
            hits = min + moveApplication.getUser().getEntity().getRandom().nextInt(1 + hits - min);
        }

        do
        {
            // Now let's apply the move for as many hits as we can
            moveApplication.apply();
            // If we missed, reset this.
            if (!moveApplication.hit) hits = -1;
        }
        while (moveApplication.apply_number < hits);

    }
}
