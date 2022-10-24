package pokecube.core.moves.implementations;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import pokecube.api.PokecubeAPI;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.IMoveAnimation;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.IMoveWorldEffect;
import pokecube.core.database.moves.MovesDatabases;
import pokecube.core.eventhandlers.MoveEventsHandler;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.animations.AnimationMultiAnimations;
import pokecube.core.moves.animations.MoveAnimationHelper;
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
                if (PokecubeMod.debug)
                    PokecubeAPI.LOGGER.info(move.name + ": animations: " + move.root_entry.animation.animations);
                move.setAnimation(new AnimationMultiAnimations(move));
                continue;
            }
            String anim = move.animDefault;
            if (anim == null || anim.equals("none")) continue;
            if (!move.animDefault.endsWith(":~" + move.name)) move.animDefault = move.animDefault + ":~" + move.name;
            anim = move.animDefault;
            if (PokecubeMod.debug) PokecubeAPI.LOGGER.info(move.name + ": preset animation: " + move.animDefault);
            final IMoveAnimation animation = MoveAnimationHelper.getAnimationPreset(anim);
            if (animation != null) move.setAnimation(animation);
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
        try
        {
            for (final Class<?> candidateClass : foundClasses)
                if (IMoveWorldEffect.class.isAssignableFrom(candidateClass)
                        && candidateClass.getEnclosingClass() == null)
            {
                final IMoveWorldEffect move = (IMoveWorldEffect) candidateClass.getConstructor().newInstance();
                MoveEventsHandler.register(move);
            }
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        foundClasses.clear();

        List<MoveEntry> moves = Lists.newArrayList();
        // Next do move packages
        try
        {
            int num = 0;

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