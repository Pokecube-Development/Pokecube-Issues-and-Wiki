package pokecube.core.interfaces.pokemob.moves;

import net.minecraft.entity.Entity;
import pokecube.core.PokecubeCore;
import pokecube.core.events.pokemob.combat.AttackEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.PokeType;

public class MovePacket
{
    public IPokemob      attacker;
    public Entity        attacked;
    public String        attack;
    public PokeType      attackType;
    public int           PWR;
    public int           criticalLevel;
    public byte          statusChange;
    public byte          changeAddition;
    public float         stabFactor        = 1.5f;
    public float         critFactor        = 1.5f;
    public boolean       stab              = false;
    public boolean       hit               = false;
    public int           damageDealt       = 0;
    /** Is the move packet before of after damage is done */
    public final boolean pre;
    /** Detect, Protect, wonder guard will set this true. */
    public boolean       canceled          = false;
    /** Did the move crit */
    public boolean       didCrit           = false;
    /** False swipe, sturdy ability and focus items would set this true. */
    public boolean       noFaint           = false;
    /**
     * Used in the protection moves, accounts their accuracy via this
     * variable
     */
    public boolean       failed            = false;
    /**
     * Move has failed for some unspecified reason, will not give failure
     * message, will not process past preAttack
     */
    public boolean       denied            = false;
    /** does target get infatuated */
    public boolean       infatuateTarget   = false;
    /** does attacker get infatuated */
    public boolean       infatuateAttacker = false;
    /**
     * Whether or not to apply ongoing, this can be set to false to use
     * these during ongoing effects
     */
    public boolean       applyOngoing      = true;
    /** Stat modifications for target */
    public int[]         attackedStatModification;
    /** Stat modifications for attacker */
    public int[]         attackerStatModification;
    /** Stat modifications chance for target */
    public float         attackedStatModProb;
    /** Stat modifications chance for attacker */
    public float         attackerStatModProb;
    /** modifies supereffectiveness */
    public float         superEffectMult   = 1;
    /** Stat multpliers */
    public float[]       statMults         = { 1, 1, 1, 1, 1, 1, 1, 1, 1 };

    public MovePacket(IPokemob attacker, Entity attacked, Move_Base move)
    {
        this(attacker, attacked, move.name, move.getType(attacker), move.getPWR(), move.move.crit,
                move.move.statusChange, move.move.change);
    }

    public MovePacket(IPokemob attacker, Entity attacked, String attack, PokeType type, int PWR, int criticalLevel,
            byte statusChange, byte changeAddition)
    {
        this(attacker, attacked, attack, type, PWR, criticalLevel, statusChange, changeAddition, true);
    }

    public MovePacket(IPokemob attacker, Entity attacked, String attack, PokeType type, int PWR, int criticalLevel,
            byte statusChange, byte changeAddition, boolean pre)
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

        PokecubeCore.MOVE_BUS.post(new AttackEvent(this));
    }

    public Move_Base getMove()
    {
        return MovesUtils.getMoveFromName(this.attack);
    }
}
