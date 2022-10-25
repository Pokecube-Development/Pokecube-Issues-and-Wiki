package pokecube.core.moves.implementations;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

import org.objectweb.asm.Type;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.forgespi.language.ModFileScanData.AnnotationData;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.moves.IMove;
import pokecube.api.data.moves.LoadedMove;
import pokecube.api.data.moves.MoveApplicationRegistry;
import pokecube.api.data.moves.MoveProvider;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.IMoveWorldEffect;
import pokecube.core.PokecubeCore;
import pokecube.core.database.moves.MovesDatabases;
import pokecube.core.eventhandlers.MoveEventsHandler;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.animations.AnimationMultiAnimations;
import pokecube.core.moves.templates.D_Move_Damage;
import pokecube.core.moves.zmoves.GZMoveManager;
import thut.lib.CompatParser.ClassFinder;

public class MovesAdder implements IMoveConstants
{
    public static Set<Package> worldActionPackages = Sets.newHashSet();
    public static Set<Package> moveRegistryPackages = Sets.newHashSet();

    static
    {
        moveRegistryPackages.add(D_Move_Damage.class.getPackage());
    }

    public static void setupMoveAnimations()
    {
        for (final MoveEntry move : MovesUtils.getKnownMoves())
        {
            if (move.root_entry != null && move.root_entry.animation != null
                    && !move.root_entry.animation.animations.isEmpty())
            {
                if (PokecubeCore.getConfig().debug_moves)
                    PokecubeAPI.LOGGER.info(move.name + ": animations: " + move.root_entry.animation.animations);
                move.setAnimation(new AnimationMultiAnimations(move));
                continue;
            }
        }
    }

    // Finds all Move_Basics inside this package and registers them.
    private static List<MoveEntry> registerAutodetect()
    {
        final List<Class<?>> foundClasses = Lists.newArrayList();

        PokecubeAPI.LOGGER.debug("Autodecting Moves...");

        // start with world action packages.
        for (final Package pack : MovesAdder.worldActionPackages)
        {
            if (pack == null) continue;
            try
            {
                foundClasses.addAll(ClassFinder.find(pack.getName()));
            }
            catch (final Exception e)
            {
                e.printStackTrace();
            }
        }
        // Register Move Actions.
        for (final Class<?> candidateClass : foundClasses)
        {
            if (IMoveWorldEffect.class.isAssignableFrom(candidateClass) && candidateClass.getEnclosingClass() == null)
            {
                try
                {
                    final IMoveWorldEffect move = (IMoveWorldEffect) candidateClass.getConstructor().newInstance();
                    MoveEventsHandler.register(move);
                }
                catch (final Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        // Next do move packages
        foundClasses.clear();

        Type ANNOTE = Type.getType("Lpokecube/api/data/moves/MoveProvider;");
        BiFunction<ModFile, String, Boolean> validClass = (file, name) -> {
            for (final AnnotationData a : file.getScanResult().getAnnotations())
                if (name.equals(a.clazz().getClassName()) && a.annotationType().equals(ANNOTE)) return true;
            return false;
        };

        for (final Package pack : MovesAdder.moveRegistryPackages)
        {
            if (pack == null) continue;
            try
            {
                foundClasses.addAll(ClassFinder.find(pack.getName(), validClass));
            }
            catch (final Exception e)
            {
                e.printStackTrace();
            }
        }

        List<MoveEntry> moves = Lists.newArrayList();
        try
        {
            int num = 0;
            for (final Class<?> candidateClass : foundClasses)
            {
                // Needs annotation
                if (candidateClass.getAnnotations().length == 0) continue;
                final MoveProvider details = candidateClass.getAnnotation(MoveProvider.class);
                if (details != null)
                {
                    // Allow multiple keys if you want to apply same effect to
                    // many moves.
                    for (var key : details.name())
                    {
                        MoveEntry entry = MoveEntry.get(key);
                        if (entry == null)
                        {
                            PokecubeAPI.LOGGER.error(
                                    "Unable to register Custom Move for {}, as entry is not found! make a json file for the entry to fix this!",
                                    key);
                        }
                        else
                        {
                            try
                            {
                                entry.root_entry._manually_defined = true;

                                // Lets see what kind of thing this is.
                                Object thing = candidateClass.getConstructor().newInstance();

                                // first assume it is an IMove.
                                if (thing instanceof IMove move)
                                {
                                    MoveApplicationRegistry.addMoveModifier(entry, details.order(), move);
                                    moves.add(entry);
                                }
                                // Otherwise, it is probably a thing to stuff in
                                // a
                                // LoadedMove
                                else
                                {
                                    LoadedMove move = new LoadedMove();
                                    boolean applied = false;

                                    // We loop over the fields of LoadedMove,
                                    // and
                                    // see if the thing we have is the correct
                                    // type,
                                    // if so, we stuff it in there, and mark as
                                    // applied.
                                    for (Field field : LoadedMove.class.getFields())
                                    {
                                        if (field.getType().isAssignableFrom(candidateClass))
                                        {
                                            field.set(move, thing);
                                            applied = true;
                                            break;
                                        }
                                    }
                                    if (applied)
                                    {
                                        MoveApplicationRegistry.addMoveModifier(entry, details.order(), move);
                                        moves.add(entry);
                                    }
                                    else
                                    {
                                        PokecubeAPI.LOGGER.error(
                                                "Unable to register Custom Move for {}, as was unable to find what the implementor did!",
                                                key);
                                    }

                                }
                            }
                            catch (final Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            PokecubeAPI.LOGGER.debug("Registered " + num + " Custom Moves");
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        return moves;
    }

    public static void registerMoves()
    {
        // Initialize the databases
        MovesDatabases.preInitLoad();

        MovesAdder.registerRemainder(MovesAdder.registerAutodetect());
        // Finally setup the animations for the moves Later we might sync these
        // from server to client, and run setup for animations again there.
        MovesAdder.setupMoveAnimations();
    }

    /**
     * Only registers contact and self, as distances moves usually should have
     * some effect.
     * 
     * @param list
     */
    private static void registerRemainder(List<MoveEntry> list)
    {
        int num = 0;
        List<MoveEntry> unRegged = MoveEntry.values();
        unRegged.removeIf(list::contains);
        for (final MoveEntry e : unRegged)
        {
            boolean doesSomething = GZMoveManager.isGZDMove(e);
            doesSomething |= e.checkValid();
            if (!doesSomething)
            {
                MoveEntry.removeMove(e);
                PokecubeAPI.LOGGER.debug("Ignoring move {}, as could not figure out what it does...", e.name);
            }
            else num++;
        }
        PokecubeAPI.LOGGER.debug("Registered " + num + " Database Moves");
    }
}