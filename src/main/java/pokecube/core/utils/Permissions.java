package pokecube.core.utils;

import java.util.Map;

import com.google.common.collect.Maps;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.PermNodes.DefaultPermissionLevel;

/**
 * This is a list of permissions nodes, as well as information about what they
 * do, and what they are for. All of these are registered, after postinit, with
 * the listed default levels.
 */
public class Permissions
{
    /** Can the player ride a pokemob. Default to ALL */
    public static final String RIDEPOKEMOB = "pokecube.ride";

    /**
     * Can the player ride a specific pokemob, checked after checking
     * RIDEPOKEMOB, only if it is allowed, has format "pokecube.ride.<trimmed
     * entry name>" Default to ALL
     */
    public static final Map<PokedexEntry, String> RIDESPECIFIC = Maps.newHashMap();
    /** Can the player surf a pokemob. Default to ALL */
    public static final String                    SURFPOKEMOB  = "pokecube.surf";

    /**
     * Can the player surf a specific pokemob, checked after checking
     * SURFPOKEMOB, only if it is allowed, has format "pokecube.surf.<trimmed
     * entry name>" Default to ALL
     */
    public static final Map<PokedexEntry, String> SURFSPECIFIC = Maps.newHashMap();
    /** Can the player surf a pokemob. Default to ALL */
    public static final String                    DIVEPOKEMOB  = "pokecube.dive";

    /**
     * Can the player surf a specific pokemob, checked after checking
     * DIVEPOKEMOB, only if it is allowed, has format "pokecube.dive.<trimmed
     * entry name>" Default to ALL
     */
    public static final Map<PokedexEntry, String> DIVESPECIFIC = Maps.newHashMap();
    /** Can the player fly a pokemob. Default to ALL */
    public static final String                    FLYPOKEMOB   = "pokecube.fly";

    /**
     * Can the player fly a specific pokemob, checked after checking
     * FLYPOKEMOB, only if it is allowed, has format "pokecube.fly.<trimmed
     * entry name>" Default to ALL
     */
    public static final Map<PokedexEntry, String> FLYSPECIFIC     = Maps.newHashMap();
    /**
     * can the player use the specified world action, format is
     * "pokecube.move.action.<move name>, Default to ALL
     */
    public static final Map<String, String>       MOVEWORLDACTION = Maps.newHashMap();

    /**
     * Can the player catch a pokemob. If not, the pokecube will bounce off,
     * similar to legendary conditions. Default to ALL
     */
    public static final String CATCHPOKEMOB = "pokecube.catch";

    /**
     * Can the player catch a specific pokemob, checked after checking
     * CATCHPOKEMOB, has format "pokecube.catch.<trimmed entry name>" Default to
     * ALL
     */
    public static final Map<PokedexEntry, String> CATCHSPECIFIC  = Maps.newHashMap();
    /**
     * Can the player send out pokemobs, if false, it returns the cube to their
     * inventory (or sends to pc). Default to ALL
     */
    public static final String                    SENDOUTPOKEMOB = "pokecube.sendout";

    /**
     * Can the player send out specific pokemob, if false, it returns the cube
     * to their inventory (or sends to pc), checked after checking
     * SENDOUTPOKEMOB, has format "pokecube.sendout.<trimmed entry name>"
     * Default to ALL
     */
    public static final Map<PokedexEntry, String> SENDOUTSPECIFIC = Maps.newHashMap();
    /**
     * Can the player hatch a egg, if not, the egg will hatch as a wild pokemob
     * instead. Default to ALL
     */
    public static final String                    HATCHPOKEMOB    = "pokecube.hatch";

    /**
     * Can the player hatch a specific pokemob, checked after checking
     * HATCHPOKEMOB, has format "pokecube.hatch.<trimmed entry name>" Default to
     * ALL
     */
    public static final Map<PokedexEntry, String> HATCHSPECIFIC = Maps.newHashMap();

    public static void register()
    {
        PermNodes.registerNode(Permissions.CATCHPOKEMOB, DefaultPermissionLevel.ALL, "can catch a mob?");
        PermNodes.registerNode(Permissions.HATCHPOKEMOB, DefaultPermissionLevel.ALL, "can hatch a mob?");
        PermNodes.registerNode(Permissions.SENDOUTPOKEMOB, DefaultPermissionLevel.ALL, "can send out a mob?");

        PermNodes.registerNode(Permissions.RIDEPOKEMOB, DefaultPermissionLevel.ALL, "can ride a mob?");
        PermNodes.registerNode(Permissions.FLYPOKEMOB, DefaultPermissionLevel.ALL, "can fly a mob?");
        PermNodes.registerNode(Permissions.SURFPOKEMOB, DefaultPermissionLevel.ALL, "can surf a mob?");
        PermNodes.registerNode(Permissions.DIVEPOKEMOB, DefaultPermissionLevel.ALL, "can dive a mob?");

        for (final PokedexEntry entry : Database.getSortedFormes())
        {
            final String catcha = Permissions.CATCHPOKEMOB + "." + entry.getTrimmedName();
            final String hatcha = Permissions.HATCHPOKEMOB + "." + entry.getTrimmedName();
            final String senda = Permissions.SENDOUTPOKEMOB + "." + entry.getTrimmedName();
            final String ridea = Permissions.RIDEPOKEMOB + "." + entry.getTrimmedName();
            final String flya = Permissions.FLYPOKEMOB + "." + entry.getTrimmedName();
            final String surfa = Permissions.SURFPOKEMOB + "." + entry.getTrimmedName();
            final String divea = Permissions.DIVEPOKEMOB + "." + entry.getTrimmedName();

            PermNodes.registerNode(catcha, DefaultPermissionLevel.ALL, "can catch a " + entry + "?");
            PermNodes.registerNode(hatcha, DefaultPermissionLevel.ALL, "can hatch a " + entry + "?");
            PermNodes.registerNode(senda, DefaultPermissionLevel.ALL, "can send out a " + entry + "?");

            PermNodes.registerNode(ridea, DefaultPermissionLevel.ALL, "can ride a " + entry + "?");
            PermNodes.registerNode(flya, DefaultPermissionLevel.ALL, "can fly a " + entry + "?");
            PermNodes.registerNode(surfa, DefaultPermissionLevel.ALL, "can surf a " + entry + "?");
            PermNodes.registerNode(divea, DefaultPermissionLevel.ALL, "can dive a " + entry + "?");

            Permissions.CATCHSPECIFIC.put(entry, catcha);
            Permissions.CATCHSPECIFIC.put(entry, hatcha);
            Permissions.CATCHSPECIFIC.put(entry, senda);

            Permissions.RIDESPECIFIC.put(entry, ridea);
            Permissions.FLYSPECIFIC.put(entry, flya);
            Permissions.SURFSPECIFIC.put(entry, surfa);
            Permissions.DIVESPECIFIC.put(entry, divea);
        }

        for (final String s : MovesUtils.getKnownMoveNames())
        {
            final String move = "pokecube.move.action." + s;
            PermNodes.registerNode(move, DefaultPermissionLevel.ALL, "can use " + move + " out of battle?");
        }
    }

}
