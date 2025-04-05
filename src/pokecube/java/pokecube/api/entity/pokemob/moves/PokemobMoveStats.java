package pokecube.api.entity.pokemob.moves;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.network.pokemobs.PacketSyncNewMoves;

public class PokemobMoveStats
{

    private static final PokemobMoveStats defaults = new PokemobMoveStats();
    private static final Set<String> IGNORE = Sets.newHashSet();
    static
    {
        PokemobMoveStats.IGNORE.add("baseMoves");
        PokemobMoveStats.IGNORE.add("movesToUse");
        PokemobMoveStats.IGNORE.add("newMoves");
        PokemobMoveStats.IGNORE.add("num");
        PokemobMoveStats.IGNORE.add("exp");
    }

    public Entity infatuateTarget;

    // Timers used for various move types.
    public int TOXIC_COUNTER = 0;
    public int ROLLOUTCOUNTER = 0;
    public int FURYCUTTERCOUNTER = 0;
    public int DEFENSECURLCOUNTER = 0;

    public boolean Exploding = false;
    public int boomState = -1;

    public int SPECIALCOUNTER = 0;
    /** Used for cooldown of crit chance moves */
    public int SPECIALTYPE = 0;

    /** Used for moves such as bide/counter/mirror coat */
    public int PHYSICALDAMAGETAKENCOUNTER = 0;
    public int SPECIALDAMAGETAKENCOUNTER = 0;

    /** Number of times detect, protect or similar has worked. */
    public int BLOCKCOUNTER = 0;
    public int blockTimer = 0;
    public boolean blocked = false;

    public boolean biding = false;

    public float substituteHP = 0;

    public int changes = IMoveConstants.CHANGE_NONE;

    /**
     * Time when this creeper was last in an active state (Messed up code here,
     * probably causes creeper animation to go weird)
     */
    public int lastActiveTime;

    /**
     * The amount of time since the creeper was close enough to the player to
     * ignite
     */
    public int timeSinceIgnited;
    public int fuseTime = 30;

    /** The Previous lvl, used to determine which moves to try to learn. */
    public int oldLevel = 0;

    /** Moves it is trying to learn. */
    public List<String> newMoves = Lists.newArrayList();
    /** Index of new move to learn from newMoves. */
    public int num = 0;
    /** The last move we used. */
    public String lastMove;
    /** Storing exp in here as well. */
    public int exp = 0;
    /** Cache of currently selected move */
    public MoveEntry selectedMove;
    /** The moves we are currently using */
    public List<MoveApplication> movesInProgress = Lists.newArrayList();
    public boolean targettingSelf = false;
    /**
     * This is the ability to apply in battle, out of battle it will be reset to
     * whatever the mob's normal ability was.
     */
    public Ability battleAbility = null;

    // Index in battle of targetted ally, owner is always last in the battle,
    // even if not in battle
    public int allyIndex = 0;
    // Index in battle of targetted enemy.
    public int enemyIndex = 0;

    public LivingEntity targetEnemy = null;
    public LivingEntity targetAlly = null;

    public int transformId = -1;
    /** The array of moves. */
    private String[] baseMoves = new String[4];
    /** The array of moves. */
    private String[] movesToUse = new String[4];

    public void reset()
    {
        System.arraycopy(baseMoves, 0, movesToUse, 0, movesToUse.length);
        for (final Field f : this.getClass().getFields()) try
        {
            if (!PokemobMoveStats.IGNORE.contains(f.getName())) f.set(this, f.get(PokemobMoveStats.defaults));
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    public void checkMovesInProgress(IPokemob user)
    {
        targettingSelf = false;
        synchronized (movesInProgress)
        {
            movesInProgress.removeIf(s -> s.isFinished());
            for (var move : movesInProgress)
            {
                if (move.getTarget() == user.getEntity())
                {
                    targettingSelf = true;
                    break;
                }
            }
        }
    }

    public void changeMovesUser(IPokemob newUser)
    {
        PokemobMoveStats from = newUser.getMoveStats();
        synchronized (movesInProgress)
        {
            synchronized (from.movesInProgress)
            {
                from.movesInProgress.addAll(this.movesInProgress);
                from.movesInProgress.forEach(m -> m.setUser(newUser));
            }
        }
    }

    public void addMoveInProgress(IPokemob user, MoveApplication application)
    {
        this.targettingSelf |= application.getTarget() == user.getEntity();
        synchronized (movesInProgress)
        {
            this.movesInProgress.add(application);
        }
    }

    public boolean isExecutingMoves()
    {
        return !movesInProgress.isEmpty();
    }

    public boolean addPendingMove(String move, IPokemob notify)
    {
        if (move == null) return false;
        if (newMoves.contains(move)) return false;
        newMoves.add(move);
        newMoves.sort(null);
        if (notify != null) PacketSyncNewMoves.sendUpdatePacket(notify);
        return true;
    }

    public void removePendingMove(String move)
    {
        this.newMoves.remove(move);
    }

    public boolean hasLearningMove()
    {
        return !this.newMoves.isEmpty();
    }

    public String getLearningMove()
    {
        if (this.newMoves.isEmpty()) return null;
        if (this.num < 0) this.num = this.newMoves.size() - 1;
        this.num = this.num % this.newMoves.size();
        return newMoves.get(this.num);
    }

    public String[] getMovesToUse()
    {
        return movesToUse;
    }

    public String[] setMovesToUse(String[] movesToUse)
    {
        if (baseMoves == movesToUse) Thread.dumpStack();
        this.movesToUse = movesToUse;
        return movesToUse;
    }

    public String[] getBaseMoves()
    {
        return baseMoves;
    }

    public String[] setBaseMoves(String[] baseMoves)
    {
        if (baseMoves == movesToUse) Thread.dumpStack();
        this.baseMoves = baseMoves;
        return baseMoves;
    }
}
