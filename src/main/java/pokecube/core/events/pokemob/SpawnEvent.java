package pokecube.core.events.pokemob;

import org.nfunk.jep.JEP;

import net.minecraft.entity.MobEntity;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

/** These events are all fired on the PokecubeCore.POKEMOB_BUS */
public class SpawnEvent extends Event
{
    /**
     * Called before the pokemob is spawned into the world, during the checks
     * for a valid location. <br>
     * Cancelling this will prevent the spawn.
     */
    @Cancelable
    @HasResult
    public static class Check extends SpawnEvent
    {
        /**
         * Is this even actually for spawning, or just checking if something
         * can spawn, say in pokedex
         */
        public final boolean forSpawn;

        public Check(final PokedexEntry entry, final Vector3 location, final IWorld world, final boolean forSpawn)
        {
            super(entry, location, world);
            this.forSpawn = forSpawn;
        }
    }

    @Cancelable
    public static class Despawn extends SpawnEvent
    {
        public final IPokemob pokemob;

        public Despawn(final Vector3 location, final IWorld world, final IPokemob pokemob_)
        {
            super(pokemob_.getPokedexEntry(), location, world);
            this.pokemob = pokemob_;
        }

    }

    public static class Function
    {
        public String  dim;
        public String  func;
        public boolean radial;
        public boolean central;
    }

    public static class FunctionVariance extends Variance
    {
        final JEP parser;

        public FunctionVariance(final String function)
        {
            this.parser = new JEP();
            this.parser.initFunTab(); // clear the contents of the function
                                      // table
            this.parser.addStandardFunctions();
            this.parser.initSymTab(); // clear the contents of the symbol table
            this.parser.addStandardConstants();
            this.parser.addComplex(); // among other things adds i to the symbol
            // table
            this.parser.addVariable("x", 0);
            this.parser.parseExpression(function);
        }

        @Override
        public int apply(final int level)
        {
            this.parser.setVarValue("x", level);
            return (int) this.parser.getValue();
        }
    }

    /**
     * Called after spawn lvl for a mob is chosen, use setLevel if you wish to
     * change the level that it spawns at.
     */
    public static class Level extends SpawnEvent
    {
        private int            level;
        private final Variance variance;
        private final int      original;

        public Level(final PokedexEntry entry_, final Vector3 location_, final IWorld world, final int level,
                final Variance variance)
        {
            super(entry_, location_, world);
            this.level = level;
            this.original = level;
            this.variance = variance;
        }

        public Variance getExpectedVariance()
        {
            return this.variance;
        }

        public int getInitialLevel()
        {
            return this.original;
        }

        public int getLevel()
        {
            return this.level;
        }

        public void setLevel(final int level)
        {
            this.level = level;
        }

    }

    public static class LevelRange extends Variance
    {
        int[] nums;

        public LevelRange(final int[] vars)
        {
            this.nums = vars.clone();
            if (this.nums[0] <= 0 || this.nums[1] <= 0) this.nums[1] = this.nums[0] = 1;
            if (this.nums[0] == this.nums[1]) this.nums[1]++;
        }

        @Override
        public int apply(final int level)
        {
            return this.nums[0] + ThutCore.newRandom().nextInt(this.nums[1] - this.nums[0]);
        }
    }

    public static class Pick extends SpawnEvent
    {
        /**
         * This is used to edit the pokedex entry directly before the mob is
         * constructed. It allows bypassing all of the rest of the spawn
         */
        public static class Final extends Pick
        {
            private String args = "";

            public Final(final PokedexEntry entry_, final Vector3 location_, final World worldObj_)
            {
                super(entry_, location_, worldObj_);
            }

            public String getSpawnArgs()
            {
                return this.args;
            }

            public void setSpawnArgs(String args)
            {
                if (args == null) args = "";
                this.args = args;
            }
        }

        /**
         * This is called after Pre is called, but only if the result from Pre
         * was not null. This one allows modifying the spawn based on the spawn
         * that was chosen before.
         */
        public static class Post extends Pick
        {
            public Post(final PokedexEntry entry_, final Vector3 location_, final World world_)
            {
                super(entry_, location_, world_);
            }
        }

        /**
         * Called when a location is initially chosen for spawn. The initial
         * entry handed here will be null, it will be filled in by Pokecube with
         * an appropriate spawn (if is chosen), with event priority of HIGHEST.
         * anything that sets this afterwards will override default pick.
         */
        public static class Pre extends Pick
        {
            public Pre(final PokedexEntry entry_, final Vector3 location_, final World world_)
            {
                super(entry_, location_, world_);
            }
        }

        private PokedexEntry pick;

        public Pick(final PokedexEntry entry_, final Vector3 location_, final World world_)
        {
            super(entry_, location_, world_);
            this.pick = entry_;
        }

        public Vector3 getLocation()
        {
            return this.location;
        }

        public PokedexEntry getPicked()
        {
            return this.pick;
        }

        public void setLocation(final Vector3 loc)
        {
            this.location.set(loc);
        }

        public void setPick(final PokedexEntry toPick)
        {
            this.pick = toPick;
        }
    }

    /**
     * Called right before the pokemob is spawned into the world. Cancelling
     * this does nothing.<br>
     * pokemob is the pokemob entity which is about to spawn.
     */
    public static class Post extends SpawnEvent
    {
        public final IPokemob  pokemob;
        public final MobEntity entity;

        public Post(final PokedexEntry entry, final Vector3 location, final World world, final IPokemob pokemob)
        {
            super(entry, location, world);
            this.pokemob = pokemob;
            this.entity = pokemob.getEntity();
        }
    }

    /**
     * Called before the pokemob is spawned into the world, during the checks
     * for a valid location. <br>
     * Cancelling this will prevent the spawn.
     */
    @Cancelable
    public static class Pre extends SpawnEvent
    {
        public Pre(final PokedexEntry entry, final Vector3 location, final World world)
        {
            super(entry, location, world);
        }
    }

    /** Called when a pokemob is sent out from the cube. */
    public static class SendOut extends SpawnEvent
    {
        public static class Post extends SendOut
        {
            public Post(final PokedexEntry entry, final Vector3 location, final World world, final IPokemob pokemob)
            {
                super(entry, location, world, pokemob);
            }
        }

        /**
         * Called before sending out, cancelling this will result in the cube
         * either sitting on the ground, or trying to return to sender's
         * inventory. This is called right before spawning the pokemob into the
         * world.
         */
        @Cancelable
        public static class Pre extends SendOut
        {
            public Pre(final PokedexEntry entry, final Vector3 location, final World world, final IPokemob pokemob)
            {
                super(entry, location, world, pokemob);
            }
        }

        public final IPokemob pokemob;

        public final MobEntity entity;

        protected SendOut(final PokedexEntry entry, final Vector3 location, final World world, final IPokemob pokemob)
        {
            super(entry, location, world);
            this.pokemob = pokemob;
            this.entity = pokemob.getEntity();
        }
    }

    public static class Variance
    {
        public Variance()
        {
        }

        public int apply(final int level)
        {
            return level;
        }
    }

    public final PokedexEntry entry;

    public final Vector3 location;

    public final IWorld world;

    public SpawnEvent(final PokedexEntry entry_, final Vector3 location_, final IWorld world)
    {
        this.entry = entry_;
        this.location = location_;
        this.world = world;
    }
}
