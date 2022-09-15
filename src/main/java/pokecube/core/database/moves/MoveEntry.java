package pokecube.core.database.moves;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.google.common.collect.Lists;

import pokecube.api.PokecubeAPI;
import pokecube.api.moves.IMoveConstants;
import pokecube.api.utils.PokeType;
import pokecube.core.database.moves.json.JsonMoves;
import pokecube.core.database.moves.json.JsonMoves.MoveJsonEntry;

public class MoveEntry implements IMoveConstants
{
    public static enum Category
    {
        OTHER, SPECIAL, PHYSICAL;
    }

    protected static HashMap<String, MoveEntry> movesNames = new HashMap<>();
    public static HashSet<String> protectionMoves = new HashSet<>();
    public static HashSet<String> unBlockableMoves = new HashSet<>();
    public static HashSet<String> oneHitKos = new HashSet<>();

    public static int TOTALHP = 1;
    public static int DAMAGEDEALT = 2;
    public static int RELATIVEHP = 4;
    public static int MISS = 8;

    public static int NODAMAGE = -2;
    public static int FULLHP = -1;
    public static int LEVEL = -5;
    public static int SPECIAL = -4;
    public static int FLEE = -3;

    public static final MoveEntry CONFUSED;

    static
    {
        CONFUSED = new MoveEntry("pokemob.status.confusion");
        MoveEntry.CONFUSED.type = PokeType.unknown;
        MoveEntry.CONFUSED.category = IMoveConstants.PHYSICAL;
        MoveEntry.CONFUSED.attackCategory = IMoveConstants.CATEGORY_CONTACT + IMoveConstants.CATEGORY_SELF;
        MoveEntry.CONFUSED.power = 40;
        MoveEntry.CONFUSED.protect = false;
        MoveEntry.CONFUSED.magiccoat = false;
        MoveEntry.CONFUSED.snatch = false;
        MoveEntry.CONFUSED.kingsrock = false;
        MoveEntry.CONFUSED.canHitNonTarget = false;
        MoveEntry.CONFUSED.baseEntry = new MoveJsonEntry();
    }

    public static MoveEntry get(String name)
    {
        if (name.equals(CONFUSED.name)) return CONFUSED;
        // Ensure the passed in name is correctly converted
        name = JsonMoves.convertMoveName(name);
        if (name.equals(CONFUSED.name)) return CONFUSED;
        // Then return or add a new entry, make a warning if no json entry was
        // present, but accept it anyway.
        return MoveEntry.movesNames.computeIfAbsent(name, n -> {
            PokecubeAPI.LOGGER.warn("Warning, auto-generating a move entry for un-registered move " + n);
            return new MoveEntry(n);
        });
    }

    public static List<MoveEntry> values()
    {
        return Lists.newArrayList(MoveEntry.movesNames.values());
    }

    public final String name;
    public PokeType type;

    /** Distance, contact, etc. */
    public int attackCategory;
    public int power = 0;
    public int accuracy;
    public int pp;
    public byte statusChange;
    public float statusChance;
    public byte change = IMoveConstants.CHANGE_NONE;
    public float chanceChance = 0;
    public int[] attackerStatModification =
    { 0, 0, 0, 0, 0, 0, 0, 0 };
    public float attackerStatModProb = 1;
    public int[] attackedStatModification =
    { 0, 0, 0, 0, 0, 0, 0, 0 };
    public float attackedStatModProb = 1;

    public float damageHeal = 0;
    public float selfHealRatio = 0;
    public float targetHealRatio = 0;

    public float[] customSize = null;

    private final boolean multiTarget = false;
    private boolean canHitNonTarget = true;

    public boolean protect;
    public boolean magiccoat;
    public boolean snatch;
    public boolean kingsrock;
    public int crit;
    public boolean soundType = false;
    public boolean isPunch = false;
    public boolean fixed = false;
    public float selfDamage = 0;
    public int selfDamageType;
    public int priority = 0;
    public boolean defrosts = false;
    public boolean mirrorcoated = false;

    /**
     * Scaling factor on cooldown, if not specified in the json, this gets set
     * to 4 for moves like hyperbeam
     */
    public float cooldown_scale = 1.0f;

    /** Status, Special, Physical */

    public byte category = -1;
    public String animDefault = "none";

    public JsonMoves.MoveJsonEntry baseEntry;

    public MoveEntry(final String name)
    {
        this.name = name;
    }

    public boolean isMultiTarget()
    {
        if (this.baseEntry != null) return this.baseEntry.multiTarget;
        return this.multiTarget;
    }

    public boolean notInterceptable()
    {
        if (this.baseEntry != null) return this.baseEntry.interceptable;
        return false;
    }

    public boolean canHitNonTarget()
    {
        return this.canHitNonTarget;
    }

    public void setCanHitNonTarget(final boolean b)
    {
        this.canHitNonTarget = b;
    }

}
