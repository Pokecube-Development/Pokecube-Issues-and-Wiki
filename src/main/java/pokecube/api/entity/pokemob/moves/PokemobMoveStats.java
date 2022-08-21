package pokecube.api.entity.pokemob.moves;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import pokecube.api.data.abilities.Ability;
import pokecube.api.moves.IMoveConstants;

public class PokemobMoveStats
{

    private static final PokemobMoveStats defaults = new PokemobMoveStats();
    private static final Set<String> IGNORE = Sets.newHashSet();
    static
    {
        PokemobMoveStats.IGNORE.add("moves");
        PokemobMoveStats.IGNORE.add("g_z_moves");
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

    /** Mob transformed into */
    public LivingEntity transformedTo;

    /**
     * The amount of time since the creeper was close enough to the player to
     * ignite
     */
    public int timeSinceIgnited;
    public int fuseTime = 30;

    /** The Previous lvl, used to determine which moves to try to learn. */
    public int oldLevel = 0;

    /** The array of moves. */
    public String[] moves = new String[4];
    /** The array of moves. */
    public String[] g_z_moves = new String[4];
    /** Moves it is trying to learn. */
    public List<String> newMoves = Lists.newArrayList();
    /** Index of new move to learn from newMoves. */
    public int num = 0;
    /** The last move we used. */
    public String lastMove;
    /** Storing exp in here as well. */
    public int exp = 0;

    /**
     * This is the ability to apply in battle, out of battle it will be reset to
     * whatever the mob's normal ability was.
     */
    public Ability battleAbility = null;

    public void reset()
    {
        for (final Field f : this.getClass().getFields()) try
        {
            if (!PokemobMoveStats.IGNORE.contains(f.getName())) f.set(this, f.get(PokemobMoveStats.defaults));
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }
}
