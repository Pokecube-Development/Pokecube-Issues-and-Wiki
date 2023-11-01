package pokecube.core.moves.implementations;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

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
import pokecube.core.eventhandlers.MoveEventsHandler.WrappedAction;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.animations.AnimationMultiAnimations;
import pokecube.core.moves.templates.Move_Explode;
import pokecube.core.moves.world.DefaultElectricAction;
import pokecube.core.moves.world.DefaultFireAction;
import pokecube.core.moves.world.DefaultIceAction;
import pokecube.core.moves.world.DefaultWaterAction;
import thut.lib.CompatParser.ClassFinder;

public class MovesAdder implements IMoveConstants
{
    public static Set<Package> worldActionPackages = Sets.newHashSet();
    public static Set<Package> moveRegistryPackages = Sets.newHashSet();

    public static List<Function<MoveEntry, IMoveWorldEffect>> defaultWorldEffects = new ArrayList<>();
    public static List<Consumer<MoveEntry>> moveProcessors = new ArrayList<>();
    public static List<Predicate<MoveEntry>> moveValidators = new ArrayList<>();

    static
    {
        moveRegistryPackages.add(Move_Explode.class.getPackage());
        defaultWorldEffects.add(DefaultWaterAction::new);
        defaultWorldEffects.add(DefaultIceAction::new);
        defaultWorldEffects.add(DefaultElectricAction::new);
        defaultWorldEffects.add(DefaultFireAction::new);

        moveProcessors.add(move -> move.postInit());
        moveValidators.add(move -> move.checkValid());
    }

    public static void setupMoveAnimations()
    {
        for (final MoveEntry move : MovesUtils.getKnownMoves())
        {
            if (move.root_entry != null && move.root_entry.animation != null
                    && !move.root_entry.animation.animations.isEmpty())
            {
                if (PokecubeCore.getConfig().debug_moves)
                    PokecubeAPI.logInfo(move.name + ": animations: " + move.root_entry.animation.animations);
                move.setAnimation(new AnimationMultiAnimations(move));
                continue;
            }
            // Now register auto-generated actions for moves which were not
            // manually defined.
            if (!MoveEventsHandler.hasAction(move))
            {
                IMoveWorldEffect combined = null;
                for (var func : defaultWorldEffects)
                {
                    if (combined == null) combined = func.apply(move);
                    else combined = new WrappedAction(combined, func.apply(move));
                    if (!combined.isValid()) combined = null;
                }
                if (combined != null) MoveEventsHandler.addOrMergeActions(combined);
            }
        }
    }

    // Finds all Move_Basics inside this package and registers them.
    private static List<MoveEntry> registerAutodetect()
    {
        final List<Class<?>> foundClasses = Lists.newArrayList();

        if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Autodecting Moves...");

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
                                    entry.root_entry._implemented = true;
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
                                            // We don't break here, as quite
                                            // often we can have multiple things
                                            // which match, such as Transform
                                        }
                                    }
                                    if (applied)
                                    {
                                        MoveApplicationRegistry.addMoveModifier(entry, details.order(), move);
                                        moves.add(entry);
                                        entry.root_entry._implemented = true;
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
            if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Registered " + num + " Custom Moves");
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
        MovesDatabases.postInitMoves();

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
            boolean doesSomething = moveValidators.stream().anyMatch(p -> p.test(e));
            if (!doesSomething)
            {
                MoveEntry.removeMove(e);
                if (PokecubeCore.getConfig().debug_data)
                    PokecubeAPI.logInfo("Ignoring move {}, as could not figure out what it does...", e.name);
            }
            else
            {
                e.root_entry._implemented = true;
                num++;
            }
        }
        if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Registered " + num + " Database Moves");
    }
}