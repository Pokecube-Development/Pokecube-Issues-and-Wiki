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
    public static Map<String, String>       zmoves_map      = Maps.newHashMap();
    public static Map<String, List<String>> z_sig_moves_map = Maps.newHashMap();
    private static Map<String, String>      gmoves_map      = Maps.newHashMap();
    private static Map<String, String>      g_max_moves_map = Maps.newHashMap();

    public static boolean isGZDMove(final MoveJsonEntry entry)
    {
        return GZMoveManager.isZMove(entry) || GZMoveManager.isGMove(entry) || GZMoveManager.isDMove(entry);
    }

    public static boolean isZMove(final MoveJsonEntry entry)
    {
        final String battleEffect = ThutCore.trim(entry.battleEffect);
        return battleEffect != null && battleEffect.contains("z-power");
    }

    public static boolean isDMove(final MoveJsonEntry entry)
    {
        final String battleEffect = ThutCore.trim(entry.battleEffect);
        return battleEffect != null && battleEffect.contains("attack_dynamax_pokmon_use");
    }

    public static boolean isGMove(final MoveJsonEntry entry)
    {
        final String battleEffect = ThutCore.trim(entry.battleEffect);
        return battleEffect != null && battleEffect.contains("attack_that_gigantamax");
    }

    public static void init(final MovesJson moves)
    {
        final Map<PokeType, String> g_type_map = Maps.newHashMap();
        final Map<PokeType, String> z_moves = Maps.newHashMap();
        final List<String> g_max_moves = Lists.newArrayList();
        int num = 0;
        int num2 = 0;
        // Initial processing to determine the moves.
        for (final MoveJsonEntry entry : moves.moves)
        {
            entry.gMove = ThutCore.trim(entry.gMove);
            entry.zMove = ThutCore.trim(entry.zMove);
            final boolean g = GZMoveManager.isGMove(entry);
            final boolean d = GZMoveManager.isDMove(entry);
            final boolean z = GZMoveManager.isZMove(entry);
            if (d || g)
            {
                num++;
                final PokeType type = PokeType.getType(entry.type);
                if (g)
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
            if (z)
            {
                final PokeType type = PokeType.getType(entry.type);
                if (entry.zEntry != null)
                {
                    final String[] vars = entry.zEntry.split(";");
                    for (String s : vars)
                    {
                        s = ThutCore.trim(s);
                        List<String> movesList = GZMoveManager.z_sig_moves_map.get(s);
                        if (movesList == null) GZMoveManager.z_sig_moves_map.put(s, movesList = Lists.newArrayList());
                        movesList.add(entry.name);
                        PokecubeCore.LOGGER.debug("Signature Z-Move {} -> {}", s, entry.name);
                    }
                }
                else z_moves.put(type, entry.name);
            }
        }
        PokecubeCore.LOGGER.debug("Found {} G or Z Moves, of which {} are G-Max moves", num, num2);
        // Second pass to map on alternates.
        for (final MoveJsonEntry entry : moves.moves)
        {
            // Do not map these onto anything.
            if (g_type_map.containsValue(entry.name) || g_max_moves.contains(entry.name) || z_moves.containsValue(
                    entry.name)) continue;
            final String name = ThutCore.trim(entry.name);
            String z_to = ThutCore.trim(entry.zMovesTo);
            String g_to = ThutCore.trim(entry.gMoveTo);

            // Manual mapping
            if (z_to != null && z_moves.containsValue(z_to)) GZMoveManager.zmoves_map.put(name, z_to);
            else
            {
                final PokeType type = PokeType.getType(entry.type);
                z_to = z_moves.get(type);
                if (z_to != null) GZMoveManager.zmoves_map.put(name, z_to);
                else PokecubeCore.LOGGER.warn("No Max Move For Type {}, when allocating for {}", type.name, entry.name);
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
        final Move_Base move = MovesUtils.getMoveFromName(base_move);
        if (move == null) return null;
        final ZPower checker = CapabilityZMove.get(user.getEntity());
        if (!checker.canZMove(user, base_move)) return null;

        final String name = user.getPokedexEntry().getTrimmedName();

        // Check if a valid signature Z-move is available.
        if (GZMoveManager.z_sig_moves_map.containsKey(name))
        {
            final List<String> moves = GZMoveManager.z_sig_moves_map.get(name);
            for (final String zmove : moves)
                if (checker.canZMove(user, zmove)) return zmove;
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
     * @param index
     *            - move index to check
     * @return
     */
    public static String getGMove(final IPokemob user, final String base_move, boolean gigant)
    {
        if (base_move == null) return null;
        if (!user.getCombatState(CombatStates.DYNAMAX)) return null;
        final Move_Base move = MovesUtils.getMoveFromName(base_move);
        if (move == null) return null;
        gigant = gigant && GZMoveManager.g_max_moves_map.containsKey(base_move);
        return gigant ? GZMoveManager.g_max_moves_map.get(base_move) : GZMoveManager.gmoves_map.get(base_move);
    }
}
