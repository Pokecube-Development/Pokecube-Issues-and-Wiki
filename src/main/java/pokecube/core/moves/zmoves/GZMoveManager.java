package pokecube.core.moves.zmoves;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.collect.Maps;

import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.MoveEntry.PowerProvider;
import pokecube.api.utils.PokeType;
import pokecube.core.database.tags.Tags;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.templates.D_Move_Damage;
import pokecube.core.moves.templates.Z_Move_Damage;

public class GZMoveManager
{
    public static Map<String, String> zmoves_map = Maps.newHashMap();
    public static Map<String, List<String>> z_sig_moves_map = Maps.newHashMap();
    private static Map<String, String> gmoves_map = Maps.newHashMap();
    private static Map<String, String> g_max_moves_map = Maps.newHashMap();

    private static Map<PokeType, MoveEntry> physical_z_moves_by_type = Maps.newHashMap();
    private static Map<PokeType, MoveEntry> special_z_moves_by_type = Maps.newHashMap();
    private static Map<PokeType, MoveEntry> d_moves_by_type = Maps.newHashMap();

    static final Pattern GMAXENTRY = Pattern.compile("(that_gigantamax_)(\\w+)(_use)");

    public static boolean isGZDMove(final MoveEntry entry)
    {
        return GZMoveManager.isZMove(entry) || GZMoveManager.isGMove(entry) || GZMoveManager.isDMove(entry);
    }

    public static boolean isZMove(final MoveEntry entry)
    {
        return Tags.MOVE.isIn("z-move", entry.name);
    }

    public static boolean isDMove(final MoveEntry entry)
    {
        return Tags.MOVE.isIn("d-move", entry.name);
    }

    public static boolean isGMove(final MoveEntry entry)
    {
        return Tags.MOVE.isIn("g-move", entry.name);
    }

    public static PowerProvider getPowerProvider(MoveEntry e)
    {
        return isZMove(e) ? Z_Move_Damage.INSTANCE : D_Move_Damage.INSTANCE;
    }

    public static void process(final MoveEntry move)
    {
        if (isZMove(move))
        {
            move.root_entry._manually_defined = true;
            PokecubeAPI.LOGGER.info("Z-move: {}", move.name);
            if (move.name.endsWith("--special"))
            {
                special_z_moves_by_type.put(move.type, move);
            }
            if (move.name.endsWith("--physical"))
            {
                physical_z_moves_by_type.put(move.type, move);
            }
        }
        if (isDMove(move))
        {
            move.root_entry._manually_defined = true;
            PokecubeAPI.LOGGER.info("D-move: {}", move.name);
            d_moves_by_type.put(move.type, move);
        }
    }

    public static void postProcess()
    {
        // Here we loop over the registered moves, and assign each one a Z or D
        // move accordingly.
        MoveEntry.values().forEach(move -> {
            d_moves_by_type.computeIfPresent(move.type, (type, zmove) -> {
                gmoves_map.put(move.name, zmove.name);
                return zmove;
            });
            switch (move.category)
            {
            case PHYSICAL:
                physical_z_moves_by_type.computeIfPresent(move.type, (type, zmove) -> {
                    zmoves_map.put(move.name, zmove.name);
                    return zmove;
                });
                break;
            default:
                special_z_moves_by_type.computeIfPresent(move.type, (type, zmove) -> {
                    zmoves_map.put(move.name, zmove.name);
                    return zmove;
                });
                break;
            }
        });
    }

    /**
     * Returns a Z move based on the current selected attack of the user, if no
     * zmove available, or not able to use one (ie no crystal held), or the mob
     * is on cooldown for zmoves, then this will return null.
     *
     * @param user
     * @param index - move index to check
     * @return
     */
    public static String getZMove(final IPokemob user, final String base_move)
    {
        if (base_move == null) return null;
        final MoveEntry move = MovesUtils.getMove(base_move);
        if (move == null) return null;
        final ZPower checker = CapabilityZMove.get(user.getEntity());
        if (!checker.canZMove(user, base_move)) return null;

        final String name = user.getPokedexEntry().getTrimmedName();

        // Check if a valid signature Z-move is available.
        if (GZMoveManager.z_sig_moves_map.containsKey(name))
        {
            final List<String> moves = GZMoveManager.z_sig_moves_map.get(name);
            for (final String zmove : moves) if (checker.canZMove(user, zmove)) return zmove;
        }
        // Otherwise just pick the one from the map.
        return GZMoveManager.zmoves_map.get(base_move);
    }

    /**
     * Returns a Z move based on the current selected attack of the user, if no
     * zmove available, or not able to use one (ie no crystal held), or the mob
     * is on cooldown for zmoves, then this will return null.
     *
     * @param user
     * @param gigant
     * @param index  - move index to check
     * @return
     */
    public static String getGMove(final IPokemob user, final String base_move, boolean gigant)
    {
        if (base_move == null) return null;
        if (!user.getCombatState(CombatStates.DYNAMAX)) return null;
        final MoveEntry move = MovesUtils.getMove(base_move);
        if (move == null) return null;
        gigant = gigant && GZMoveManager.g_max_moves_map.containsKey(base_move);
        return gigant ? GZMoveManager.g_max_moves_map.get(base_move) : GZMoveManager.gmoves_map.get(base_move);
    }
}
