package pokecube.core.events.pokemob;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.nfunk.jep.JEP;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

/** These events are all fired on the PokecubeCore.POKEMOB_BUS */
public class SpawnEvent extends Event
{

    public static record SpawnContext(@Nullable ServerPlayer player, @Nonnull ServerLevel level,
            @Nonnull PokedexEntry entry, @Nonnull Vector3 location)
    {
        public SpawnContext(@Nonnull IPokemob pokemob_)
        {
            this(pokemob_.getOwner() instanceof ServerPlayer player ? player : null,
                    (ServerLevel) pokemob_.getEntity().level, pokemob_.getPokedexEntry(),
                    Vector3.getNewVector().set(pokemob_.getEntity()));
        }

        public SpawnContext(@Nonnull ServerPlayer player, PokedexEntry entry)
        {
            this(player, (ServerLevel) player.level, entry, Vector3.getNewVector().set(player));
        }

        public SpawnContext(SpawnContext context, PokedexEntry entry)
        {
            this(context.player, context.level, entry, context.location);
        }

        public SpawnContext(SpawnContext context, Vector3 location)
        {
            this(context.player, context.level, context.entry, location);
        }

        public SpawnContext(ServerLevel level, PokedexEntry entry, Vector3 location)
        {
            this(null, level, entry, location);
        }
    }

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
         * Is this even actually for spawning, or just checking if something can
         * spawn, say in pokedex
         */
        public final boolean forSpawn;

        public Check(final SpawnContext context, final boolean forSpawn)
        {
            super(context);
            this.forSpawn = forSpawn;
        }
    }

    @Cancelable
    public static class Despawn extends SpawnEvent
    {
        public final IPokemob pokemob;

        public Despawn(final IPokemob pokemob_)
        {
            super(new SpawnContext(pokemob_));
            this.pokemob = pokemob_;
        }
    }

    public static class Function
    {
        public String dim;
        public String func;
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
    public static class PickLevel extends SpawnEvent
    {
        private int level;
        private final Variance variance;
        private final int original;

        public PickLevel(final SpawnContext context, final int level, final Variance variance)
        {
            super(context);
            this.level = level;
            this.original = level;
            this.variance = variance;
        }

        public PickLevel(IPokemob pokemob, final int level, final Variance variance)
        {
            this(new SpawnContext(pokemob), level, variance);
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

            public Final(SpawnContext context)
            {
                super(context);
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
            public Post(SpawnContext context)
            {
                super(context);
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
            public Pre(SpawnContext context)
            {
                super(context);
            }
        }

        private PokedexEntry pick;
        private Vector3 location;

        public Pick(SpawnContext context)
        {
            super(context);
            this.pick = context.entry();
            location = context.location();
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
            this.location = loc.copy();
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
        public final IPokemob pokemob;
        public final Mob entity;

        public Post(final IPokemob pokemob)
        {
            super(new SpawnContext(pokemob));
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
        public Pre(SpawnContext context)
        {
            super(context);
        }
    }

    /** Called when a pokemob is sent out from the cube. */
    public static class SendOut extends SpawnEvent
    {
        public static class Post extends SendOut
        {
            public Post(final IPokemob pokemob)
            {
                super(pokemob);
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
            public Pre(final IPokemob pokemob)
            {
                super(pokemob);
            }
        }

        public final IPokemob pokemob;

        public final Mob entity;

        protected SendOut(final IPokemob pokemob)
        {
            super(new SpawnContext(pokemob));
            this.pokemob = pokemob;
            this.entity = pokemob.getEntity();
        }
    }

    public static class Variance
    {
        public Variance()
        {}

        public int apply(final int level)
        {
            return level;
        }
    }

    private final SpawnContext context;

    public SpawnEvent(final SpawnContext _context)
    {
        this.context = _context;
    }

    public ServerLevel level()
    {
        return context.level();
    }

    public ServerPlayer player()
    {
        return context.player();
    }

    public PokedexEntry entry()
    {
        return context.entry();
    }

    public Vector3 location()
    {
        return context.location();
    }

    public SpawnContext context()
    {
        return context;
    }
}
