package pokecube.core.moves.zmoves;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import pokecube.core.PokecubeCore;
import pokecube.core.database.moves.json.JsonMoves.MoveJsonEntry;
import pokecube.core.database.moves.json.JsonMoves.MovesJson;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.PokeType;
import thut.core.common.ThutCore;

public class GZMoveManager
{
    private static Map<String, String> zmoves_map      = Maps.newHashMap();
    private static Map<String, String> gmoves_map      = Maps.newHashMap();
    private static Map<String, String> g_max_moves_map = Maps.newHashMap();

    public static boolean isZMove(final MoveJsonEntry entry)
    {
        final String battleEffect = ThutCore.trim(entry.battleEffect);
        return battleEffect != null && battleEffect.contains("z-power");
    }

    public static void init(final MovesJson moves)
    {
        final Map<PokeType, String> g_type_map = Maps.newHashMap();
        final Map<String, MoveJsonEntry> z_moves = Maps.newHashMap();
        final List<String> g_max_moves = Lists.newArrayList();
        int num = 0;
        int num2 = 0;
        // Initial processing to determine the moves.
        for (final MoveJsonEntry entry : moves.moves)
        {
            entry.gMove = ThutCore.trim(entry.gMove);
            entry.zMove = ThutCore.trim(entry.zMove);
            if ("yes".equals(entry.gMove))
            {
                num++;
                final PokeType type = PokeType.getType(entry.type);
                if (entry.name.startsWith("gmax"))
                {
                    if (entry.gmaxEntry != null) num2++;
                    if (entry.gmaxEntry != null)
                    {
                        g_max_moves.add(entry.name);
                        GZMoveManager.g_max_moves_map.put(entry.name, ThutCore.trim(entry.gmaxEntry));
                    }
                }
                else g_type_map.put(type, entry.name);
            }
            if (GZMoveManager.isZMove(entry)) z_moves.put(entry.name, entry);
        }
        PokecubeCore.LOGGER.debug("Found {} G or Z Moves, of which {} are G-Max moves", num, num2);
        // Second pass to map on alternates.
        for (final MoveJsonEntry entry : moves.moves)
        {
            // Do not map these onto anything.
            if (g_type_map.containsKey(entry.name) || g_max_moves.contains(entry.name) || z_moves.containsKey(
                    entry.name)) continue;
            final String name = ThutCore.trim(entry.name);
            final String z_to = ThutCore.trim(entry.zMovesTo);
            String g_to = ThutCore.trim(entry.gMoveTo);

            // Manual mapping
            if (z_to != null && z_moves.containsKey(z_to)) GZMoveManager.zmoves_map.put(name, z_to);
            else
            {
                // Auto allocate.
                // TODO decide on z-move auto-allocation
            }
            // Manual mapping
            if (g_to != null && g_type_map.containsValue(z_to)) GZMoveManager.gmoves_map.put(name, g_to);
            else
            {
                final PokeType type = PokeType.getType(entry.type);
                g_to = g_type_map.get(type);
                if (g_to != null) GZMoveManager.gmoves_map.put(name, g_to);
                else PokecubeCore.LOGGER.warn("No Max Move For Type {}, when allocating for {}", type.name, entry.name);
            }
        }
    }

    /**
     * Returns a Z move based on the current selected attack of the user, if no
     * zmove available, or not able to use one (ie no crystal held), or the mob
     * is on cooldown for zmoves, then this will return null.
     *
     * @param user
     * @param index
     *            - move index to check
     * @return
     */
    public static String getZMove(final IPokemob user, final String base_move)
    {
        if (base_move == null) return null;
        if (user.getCombatState(CombatStates.USEDZMOVE)) return null;
        final Move_Base move = MovesUtils.getMoveFromName(base_move);
        if (move == null) return null;

        // TODO do a check in here for z-crystal, etc.

        return GZMoveManager.zmoves_map.get(base_move);
    }

    /**
     * Returns a Z move based on the current selected attack of the user, if no
     * zmove available, or not able to use one (ie no crystal held), or the mob
     * is on cooldown for zmoves, then this will return null.
     *
     * @param user
     * @param gigant
     * @param index
     *            - move index to check
     * @return
     */
    public static String getGMove(final IPokemob user, final String base_move, final boolean gigant)
    {
        if (base_move == null) return null;
        if (!user.getCombatState(CombatStates.DYNAMAX)) return null;
        final Move_Base move = MovesUtils.getMoveFromName(base_move);
        if (move == null) return null;
        return gigant ? GZMoveManager.g_max_moves_map.get(base_move) : GZMoveManager.gmoves_map.get(base_move);
    }
}
