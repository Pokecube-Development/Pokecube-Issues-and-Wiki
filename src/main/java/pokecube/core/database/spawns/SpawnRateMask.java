package pokecube.core.database.spawns;

import java.util.Map;
import java.util.Random;

import org.nfunk.jep.JEP;

import com.google.common.collect.Maps;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.SpawnMaskEvent;
import pokecube.core.events.pokemob.SpawnEvent;
import pokecube.core.events.pokemob.SpawnEvent.Function;
import thut.api.maths.Vector3;
import thut.api.util.JsonUtil;

public class SpawnRateMask
{
    public static final Map<PokedexEntry, SpawnRateMask> RATE_MASKS = Maps.newHashMap();

    public static void init()
    {
        RATE_MASKS.clear();
        if (MinecraftForge.EVENT_BUS.post(new SpawnMaskEvent()) || !PokecubeCore.getConfig().applySpawnRateMask) return;
        for (PokedexEntry e : Database.getSortedFormes())
        {
            RATE_MASKS.put(e, new SpawnRateMask(e));
        }
        PokecubeCore.POKEMOB_BUS.addListener(EventPriority.HIGHEST, SpawnRateMask::onRateCheck);
    }

    private static float getMask(PokedexEntry entry, LevelAccessor level, Vector3 location)
    {
        if (!RATE_MASKS.containsKey(entry)) return 1;
        SpawnRateMask mask = RATE_MASKS.get(entry);
        return (float) mask.parse(level, location);
    }

    private static void onRateCheck(SpawnEvent.Check.Rate event)
    {
        if (event.forSpawn)
        {
            float new_rate = event.getRate() * getMask(event.entry(), event.level(), event.location());
            new_rate = Math.min(1, new_rate);
            new_rate = Math.max(0, new_rate);
            event.setRate(new_rate);
        }
    }

    public String function = "{\"dim\":\"the_nether\",\"func\":\"abs((1)*(sin(x*8*10^-3 + px)^3 + sin(y*8*10^-3 + py)^3))\",\"radial\":false,\"central\":false}";

    public double phase_x = 0;
    public double phase_y = 0;
    public double phase_t = 0;

    Function _function;
    JEP _parser;

    public SpawnRateMask()
    {}

    public SpawnRateMask(PokedexEntry entry)
    {
        Random rand = new Random(entry.getTrimmedName().hashCode());
        phase_x = Math.PI * rand.nextDouble();
        phase_y = Math.PI * rand.nextDouble();
        phase_t = Math.PI * rand.nextDouble();
        this.initFunctions();
    }

    private static JEP initJEP(final JEP parser, final String toParse, final boolean radial)
    {
        parser.initFunTab(); // clear the contents of the function table
        parser.addStandardFunctions();
        parser.initSymTab(); // clear the contents of the symbol table
        parser.addStandardConstants();
        parser.addComplex(); // among other things adds i to the symbol
                             // table
        if (!radial)
        {
            parser.addVariable("x", 0);
            parser.addVariable("y", 0);
            parser.addVariable("px", 0);
            parser.addVariable("py", 0);
        }
        else
        {
            parser.addVariable("r", 0);
            parser.addVariable("t", 0);
            parser.addVariable("pt", 0);
        }
        parser.parseExpression(toParse);
        return parser;
    }

    private void initFunctions()
    {
        if (_function != null) return;

        _function = JsonUtil.gson.fromJson(function, Function.class);
        _parser = initJEP(new JEP(), _function.func, _function.radial);
    }

    private void parseExpression(final double xValue, final double yValue, final boolean r)
    {
        if (!r)
        {
            _parser.setVarValue("x", xValue);
            _parser.setVarValue("y", yValue);;
            _parser.setVarValue("px", phase_x);;
            _parser.setVarValue("py", phase_y);
        }
        else
        {
            _parser.setVarValue("r", xValue);
            _parser.setVarValue("t", yValue);
            _parser.setVarValue("pt", phase_t);
        }
    }

    public double parse(final LevelAccessor world, final Vector3 location)
    {
        initFunctions();
        if (!(world instanceof ServerLevel level) || _function == null) return 0;
        // BlockPos p = world.
        final Vector3 spawn = Vector3.getNewVector().set(level.getSharedSpawnPos());
        final boolean r = _function.radial;
        // Central functions are centred on 0,0, not the world spawn
        if (_function.central) spawn.clear();
        if (!r) parseExpression(location.x - spawn.x, location.z - spawn.z, r);
        else
        {
            /**
             * Set y coordinates equal to ensure only radial function in
             * horizontal plane.
             */
            spawn.y = location.y;
            final double d = location.distTo(spawn);
            final double t = Mth.atan2(location.x, location.z);
            parseExpression(d, t, r);
        }
        if (Double.isNaN(_parser.getValue())) return 0;
        return Math.abs(_parser.getValue());
    }
}
