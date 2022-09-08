package pokecube.api.entity.pokemob.moves;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.events.pokemobs.combat.AttackEvent;
import pokecube.api.moves.IMoveConstants;
import pokecube.api.moves.Move_Base;
import pokecube.api.utils.PokeType;
import pokecube.core.moves.MovesUtils;

public class MovePacket
{
    public IPokemob attacker;
    public LivingEntity attacked;
    public String attack;
    public PokeType attackType;
    public int PWR;
    public int criticalLevel;
    public byte statusChange;
    public byte changeAddition;
    public float stabFactor = 1.5f;
    public float critFactor = 1.5f;
    public boolean stab = false;
    /** Did the move hit the target. */
    public boolean hit = false;
    public int damageDealt = 0;
    /** Is the move packet before of after damage is done */
    public final boolean pre;
    /** Detect, Protect, wonder guard will set this true. */
    public boolean canceled = false;
    /** Did the move crit */
    public boolean didCrit = false;
    /** False swipe, sturdy ability and focus items would set this true. */
    public boolean noFaint = false;
    /**
     * Used in the protection moves, accounts their accuracy via this variable
     */
    public boolean failed = false;
    /**
     * Move has failed for some unspecified reason, will not give failure
     * message, will not process past preAttack
     */
    public boolean denied = false;
    /** does target get infatuated */
    public boolean infatuateTarget = false;
    /** does attacker get infatuated */
    public boolean infatuateAttacker = false;
    /**
     * Whether or not to apply ongoing, this can be set to false to use these
     * during ongoing effects
     */
    public boolean applyOngoing = true;
    /** Stat modifications for target */
    public int[] attackedStatModification;
    /** Stat modifications for attacker */
    public int[] attackerStatModification;
    /** Stat modifications chance for target */
    public float attackedStatModProb;
    /** Stat modifications chance for attacker */
    public float attackerStatModProb;
    /** modifies supereffectiveness */
    public float superEffectMult = 1;
    /** Stat multpliers */
    public float[] statMults =
    { 1, 1, 1, 1, 1, 1, 1, 1, 1 };

    private static Map<String, Field> _FIELDS = Maps.newHashMap();
    private static Set<String> _FLAGS = Sets.newHashSet();
    private static Map<String, Predicate<MovePacket>> _CUSTOMFLAGS = Maps.newHashMap();

    static
    {
        for (Field f : MovePacket.class.getDeclaredFields())
        {
            if (!f.getName().startsWith("_"))
            {
                _FIELDS.put(f.getName(), f);
                if (f.getType() == boolean.class) _FLAGS.add(f.getName());
            }
        }

        _CUSTOMFLAGS.put("is_contact",
                t -> (t.getMove().getAttackCategory(t.attacker) & IMoveConstants.CATEGORY_CONTACT) != 0);
        _CUSTOMFLAGS.put("is_self",
                t -> (t.getMove().getAttackCategory(t.attacker) & IMoveConstants.CATEGORY_SELF) != 0);
        _CUSTOMFLAGS.put("is_distance",
                t -> (t.getMove().getAttackCategory(t.attacker) & IMoveConstants.CATEGORY_DISTANCE) != 0);

        _CUSTOMFLAGS.put("causes_brn", t -> (t.statusChange & IMoveConstants.STATUS_BRN) != 0);
        _CUSTOMFLAGS.put("causes_psn", t -> (t.statusChange & IMoveConstants.STATUS_PSN) != 0);
        _CUSTOMFLAGS.put("causes_par", t -> (t.statusChange & IMoveConstants.STATUS_PAR) != 0);
        _CUSTOMFLAGS.put("causes_frz", t -> (t.statusChange & IMoveConstants.STATUS_FRZ) != 0);

        _CUSTOMFLAGS.put("self_attacker", t -> (t.attacker.getEntity() == t.attacked));
    }

    public static boolean getFlag(String key, MovePacket t)
    {
        if (_FLAGS.contains(key)) try
        {
            return _FIELDS.get(key).getBoolean(t);
        }
        catch (IllegalArgumentException | IllegalAccessException e)
        {
            PokecubeAPI.LOGGER.error("Error checking flag {} for a move packet! {}", key, e);
        }
        return false;
    }

    public static void setFlag(String key, MovePacket t, boolean set)
    {
        if (_FLAGS.contains(key)) try
        {
            _FIELDS.get(key).setBoolean(t, set);
        }
        catch (IllegalArgumentException | IllegalAccessException e)
        {
            PokecubeAPI.LOGGER.error("Error setting flag {} for a move packet! {}", key, e);
        }
    }

    public MovePacket(IPokemob attacker, LivingEntity attacked, Move_Base move)
    {
        this(attacker, attacked, move.name, move.getType(attacker), move.getPWR(), move.move.crit,
                move.move.statusChange, move.move.change);
    }

    public MovePacket(IPokemob attacker, LivingEntity attacked, String attack, PokeType type, int PWR,
            int criticalLevel, byte statusChange, byte changeAddition)
    {
        this(attacker, attacked, attack, type, PWR, criticalLevel, statusChange, changeAddition, true);
    }

    public MovePacket(IPokemob attacker, LivingEntity attacked, String attack, PokeType type, int PWR,
            int criticalLevel, byte statusChange, byte changeAddition, boolean pre)
    {
        this.attacker = attacker;
        this.attacked = attacked;
        this.attack = attack;
        this.attackType = type;
        this.PWR = PWR;
        this.criticalLevel = criticalLevel;
        this.statusChange = statusChange;
        this.changeAddition = changeAddition;
        this.pre = pre;
        final Move_Base move = this.getMove();
        this.attackedStatModification = move.move.attackedStatModification.clone();
        this.attackerStatModification = move.move.attackerStatModification.clone();
        this.attackedStatModProb = move.move.attackedStatModProb;
        this.attackerStatModProb = move.move.attackerStatModProb;

        PokecubeAPI.MOVE_BUS.post(new AttackEvent(this));
    }

    public Move_Base getMove()
    {
        return MovesUtils.getMoveFromName(this.attack);
    }
}
