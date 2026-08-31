package pokecube.api.utils;

public interface TagNames
{
    public static final String GENESCAP = "pokecube:genetics";

    /** The NBTTag name for the root of info */
    public static final String VERSION    = "version";
    // Sub tags under POKEMOBTAG
    public static final String BREEDINGTAG = "sexe_tag";

    public static final String OWNERSHIPTAG = "ownership_tag";
    public static final String STATSTAG     = "stats_tag";

    public static final String MOVESTAG     = "moves_tag";
    public static final String VISUALSTAG   = "visuals_tag";
    public static final String AITAG        = "ai_tag";
    public static final String INVENTORYTAG = "inventory_tag";
    public static final String MISCTAG      = "misc_tag";
    // Tag names for Pokemob Information
    public static final String OT    = "OTUUID";
    public static final String OWNER = "OwnerID";

    public static final String UID          = "pokemobUID";
    public static final String RNGVAL       = "personalityValue";
    public static final String FORME        = "forme";
    public static final String WASSHADOW    = "wasShadow";
    public static final String SPECIALTAG   = "specialInfo";
    public static final String NEWMOVES     = "newMoves";
    public static final String MOVEINDEX    = "index";
    public static final String EXP          = "exp";
    public static final String SEXETIME     = "loveTimer";
    public static final String POKEDEXNB    = "pokedexNb";
    public static final String TEAM         = "team";
    public static final String HAPPY        = "bonusHappiness";
    public static final String NICKNAME     = "nickname";
    public static final String COOLDOWN     = "cooldown";
    public static final String DISABLED     = "disabled";
    public static final String ABILITY      = "ability";
    public static final String FLAVOURSTAG  = "flavours";
    public static final String POKECUBE     = "pokecube";
    public static final String LOGICSTATE   = "logicState";
    public static final String GENERALSTATE = "generalState";
    public static final String COMBATSTATE  = "combatState";
    public static final String MODELHOLDER  = "customModel";
    public static final String COMBATTIME   = "timeSinceCombat";
    public static final String SPAWN_EXP    = "spawnExp";

    public static final String AIROUTINES = "aiRoutines";
    public static final String HUNGER     = "hunger";
    public static final String ITEMS      = "items";
    // Tag names for Pokecubes
    public static final String POKEMOB  = "Pokemob";

    public static final String SHEARTIME = "pokecube:shearedtime";

    public static final String REMOVED = "pokecube:removed";
    public static final String REVIVED = "pokecube:revived";
    public static final String HATCHED = "pokecube:hatched";
    public static final String NOPOOF  = "pokecube:nodespawn";
    public static final String NODROP  = "pokecube:nodrop";
    public static final String CLONED  = "pokecube:cloned";

    public static final String CAPTURING = "pokecube:capturing";
}