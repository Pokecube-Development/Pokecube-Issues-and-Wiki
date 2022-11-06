package pokecube.core.utils;

import java.util.function.Predicate;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.PokecubeCore;
import pokecube.core.init.Config;
import thut.api.util.PermNodes;
import thut.api.util.PermNodes.DefaultPermissionLevel;
import thut.core.common.commands.CommandTools;

/**
 * This is a list of permissions nodes, as well as information about what they
 * do, and what they are for. All of these are registered, after postinit, with
 * the listed default levels.
 */
public class Permissions
{
    /** Can the player ride a pokemob. Default to ALL */
    private static final String RIDEPOKEMOB = "ride";

    /**
     * Can the player ride a specific pokemob, checked after checking
     * RIDEPOKEMOB, only if it is allowed, has format "ride.<trimmed entry
     * name>" Default to ALL
     */
    private static final String RIDESPECIFIC = "ride.specifc";
    /** Can the player surf a pokemob. Default to ALL */
    private static final String SURFPOKEMOB = "surf";

    /**
     * Can the player surf a specific pokemob, checked after checking
     * SURFPOKEMOB, only if it is allowed, has format "surf.<trimmed entry
     * name>" Default to ALL
     */
    private static final String SURFSPECIFIC = "surf.specifc";
    /** Can the player surf a pokemob. Default to ALL */
    private static final String DIVEPOKEMOB = "dive";

    /**
     * Can the player surf a specific pokemob, checked after checking
     * DIVEPOKEMOB, only if it is allowed, has format "dive.<trimmed entry
     * name>" Default to ALL
     */
    private static final String DIVESPECIFIC = "dive.specifc";
    /** Can the player fly a pokemob. Default to ALL */
    private static final String FLYPOKEMOB = "fly";

    /**
     * Can the player fly a specific pokemob, checked after checking FLYPOKEMOB,
     * only if it is allowed, has format "fly.<trimmed entry name>" Default to
     * ALL
     */
    private static final String FLYSPECIFIC = "fly.specifc";
    /**
     * can the player use the specified world action, format is
     * "move.action.<move name>, Default to ALL
     */
    private static final String MOVEWORLDACTION = "move.action";

    /**
     * Can the player catch a pokemob. If not, the pokecube will bounce off,
     * similar to legendary conditions. Default to ALL
     */
    private static final String CATCHPOKEMOB = "catch";

    /**
     * Can the player catch a specific pokemob, checked after checking
     * CATCHPOKEMOB, has format "catch.<trimmed entry name>" Default to ALL
     */
    private static final String CATCHSPECIFIC = "catch.specifc";
    /**
     * Can the player send out pokemobs, if false, it returns the cube to their
     * inventory (or sends to pc). Default to ALL
     */
    private static final String SENDOUTPOKEMOB = "sendout";

    /**
     * Can the player send out specific pokemob, if false, it returns the cube
     * to their inventory (or sends to pc), checked after checking
     * SENDOUTPOKEMOB, has format "sendout.<trimmed entry name>" Default to ALL
     */
    private static final String SENDOUTSPECIFIC = "sendout.specifc";
    /**
     * Can the player hatch a egg, if not, the egg will hatch as a wild pokemob
     * instead. Default to ALL
     */
    private static final String HATCHPOKEMOB = "hatch";

    /**
     * Can the player hatch a specific pokemob, checked after checking
     * HATCHPOKEMOB, has format "hatch.<trimmed entry name>" Default to ALL
     */
    private static final String HATCHSPECIFIC = "hatch.specifc";

    public static void register()
    {
        PermNodes.registerBooleanNode(PokecubeCore.MODID, Permissions.CATCHPOKEMOB, DefaultPermissionLevel.ALL,
                "can catch a mob?");
        PermNodes.registerBooleanNode(PokecubeCore.MODID, Permissions.HATCHPOKEMOB, DefaultPermissionLevel.ALL,
                "can hatch a mob?");
        PermNodes.registerBooleanNode(PokecubeCore.MODID, Permissions.SENDOUTPOKEMOB, DefaultPermissionLevel.ALL,
                "can send out a mob?");

        PermNodes.registerBooleanNode(PokecubeCore.MODID, Permissions.CATCHSPECIFIC, DefaultPermissionLevel.ALL,
                "can catch a specific mob?");
        PermNodes.registerBooleanNode(PokecubeCore.MODID, Permissions.HATCHSPECIFIC, DefaultPermissionLevel.ALL,
                "can hatch a specific mob?");
        PermNodes.registerBooleanNode(PokecubeCore.MODID, Permissions.SENDOUTSPECIFIC, DefaultPermissionLevel.ALL,
                "can send out a specific mob?");

        PermNodes.registerBooleanNode(PokecubeCore.MODID, Permissions.RIDEPOKEMOB, DefaultPermissionLevel.ALL,
                "can ride a mob?");
        PermNodes.registerBooleanNode(PokecubeCore.MODID, Permissions.FLYPOKEMOB, DefaultPermissionLevel.ALL,
                "can fly a mob?");
        PermNodes.registerBooleanNode(PokecubeCore.MODID, Permissions.SURFPOKEMOB, DefaultPermissionLevel.ALL,
                "can surf a mob?");
        PermNodes.registerBooleanNode(PokecubeCore.MODID, Permissions.DIVEPOKEMOB, DefaultPermissionLevel.ALL,
                "can dive a mob?");

        PermNodes.registerBooleanNode(PokecubeCore.MODID, Permissions.RIDESPECIFIC, DefaultPermissionLevel.ALL,
                "can ride a specific mob?");
        PermNodes.registerBooleanNode(PokecubeCore.MODID, Permissions.FLYSPECIFIC, DefaultPermissionLevel.ALL,
                "can fly a specific mob?");
        PermNodes.registerBooleanNode(PokecubeCore.MODID, Permissions.SURFSPECIFIC, DefaultPermissionLevel.ALL,
                "can surf a specific mob?");
        PermNodes.registerBooleanNode(PokecubeCore.MODID, Permissions.DIVESPECIFIC, DefaultPermissionLevel.ALL,
                "can dive a specific mob?");

        PermNodes.registerStringNode(PokecubeCore.MODID, Permissions.MOVEWORLDACTION, DefaultPermissionLevel.ALL,
                "can use moves out of battle", "");
    }

    public static Predicate<CommandSourceStack> hasPerm(final String perm)
    {
        return cs -> CommandTools.hasPerm(cs, perm);
    }

    public static boolean canCatch(IPokemob pokemob, ServerPlayer player)
    {
        final Config config = PokecubeCore.getConfig();
        if (config.permsCapture && !PermNodes.getBooleanPerm(player, CATCHPOKEMOB)) return false;
        if (config.permsCaptureSpecific)
            return PermNodes.hasStringInList(player, CATCHSPECIFIC, pokemob.getPokedexEntry().getTrimmedName());
        return true;
    }

    public static boolean canHatch(IPokemob pokemob, ServerPlayer player)
    {
        final Config config = PokecubeCore.getConfig();
        if (config.permsHatch && !PermNodes.getBooleanPerm(player, HATCHPOKEMOB)) return false;
        if (config.permsHatchSpecific)
            return PermNodes.hasStringInList(player, HATCHSPECIFIC, pokemob.getPokedexEntry().getTrimmedName());
        return true;
    }

    public static boolean canSendOut(PokedexEntry pokemob, ServerPlayer player, boolean specific, boolean general)
    {
        final Config config = PokecubeCore.getConfig();
        if (general && config.permsSendOut && !PermNodes.getBooleanPerm(player, SENDOUTPOKEMOB)) return false;
        if (specific && config.permsSendOutSpecific)
            return PermNodes.hasStringInList(player, SENDOUTSPECIFIC, pokemob.getTrimmedName());
        return true;
    }

    public static boolean canSendOut(PokedexEntry pokemob, ServerPlayer player)
    {
        return canSendOut(pokemob, player, true, true);
    }

    public static boolean canRide(IPokemob pokemob, ServerPlayer player)
    {
        final Config config = PokecubeCore.getConfig();
        if (config.permsRide && !PermNodes.getBooleanPerm(player, RIDEPOKEMOB)) return false;
        if (config.permsRideSpecific)
            return PermNodes.hasStringInList(player, RIDESPECIFIC, pokemob.getPokedexEntry().getTrimmedName());
        return true;
    }

    public static boolean canSurf(IPokemob pokemob, ServerPlayer player)
    {
        final Config config = PokecubeCore.getConfig();
        if (config.permsSurf && !PermNodes.getBooleanPerm(player, SURFPOKEMOB)) return false;
        if (config.permsSurfSpecific)
            return PermNodes.hasStringInList(player, SURFSPECIFIC, pokemob.getPokedexEntry().getTrimmedName());
        return true;
    }

    public static boolean canFly(IPokemob pokemob, ServerPlayer player)
    {
        final Config config = PokecubeCore.getConfig();
        if (config.permsFly && !PermNodes.getBooleanPerm(player, FLYPOKEMOB)) return false;
        if (config.permsFlySpecific)
            return PermNodes.hasStringInList(player, FLYSPECIFIC, pokemob.getPokedexEntry().getTrimmedName());
        return true;
    }

    public static boolean canDive(IPokemob pokemob, ServerPlayer player)
    {
        final Config config = PokecubeCore.getConfig();
        if (config.permsDive && !PermNodes.getBooleanPerm(player, DIVEPOKEMOB)) return false;
        if (config.permsDiveSpecific)
            return PermNodes.hasStringInList(player, DIVESPECIFIC, pokemob.getPokedexEntry().getTrimmedName());
        return true;
    }

    public static boolean canUseWorldAction(ServerPlayer player, String movename)
    {
        final Config config = PokecubeCore.getConfig();
        if (config.permsMoveAction) return PermNodes.hasStringInList(player, MOVEWORLDACTION, movename);
        return true;
    }
}
