package pokecube.core.moves.implementations;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.moves.MoveEntry;
import pokecube.core.handlers.events.MoveEventsHandler;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IMoveAnimation;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.animations.AnimationMultiAnimations;
import pokecube.core.moves.animations.MoveAnimationHelper;
import pokecube.core.moves.templates.Move_AOE;
import pokecube.core.moves.templates.Move_Basic;
import pokecube.core.moves.templates.Move_Doublehit;
import pokecube.core.moves.templates.Move_Explode;
import pokecube.core.moves.templates.Move_MultiHit;
import pokecube.core.moves.templates.Move_Ongoing;
import pokecube.core.moves.templates.Move_Terrain;
import pokecube.core.moves.templates.Z_Move_Basic;
import pokecube.core.moves.zmoves.GZMoveManager;
import thut.lib.CompatParser.ClassFinder;

public class MovesAdder implements IMoveConstants
{
    public static Map<String, Class<? extends Move_Base>> presetMap = Maps.newHashMap();
    public static Set<Package>                            packages  = Sets.newHashSet();

    static
    {
        MovesAdder.packages.add(MovesAdder.class.getPackage());
        MovesAdder.presetMap.put("ongoing", Move_Ongoing.class);
        MovesAdder.presetMap.put("explode", Move_Explode.class);
        MovesAdder.presetMap.put("terrain", Move_Terrain.class);
        MovesAdder.presetMap.put("aoe", Move_AOE.class);
        MovesAdder.presetMap.put("multihit", Move_MultiHit.class);
        MovesAdder.presetMap.put("doublehit", Move_Doublehit.class);
    }

    public static void postInitMoves()
    {
        for (final Move_Base move : MovesUtils.getKnownMoves())
        {
            if (move.move.baseEntry != null && move.move.baseEntry.animations != null && !move.move.baseEntry.animations
                    .isEmpty())
            {
                if (PokecubeMod.debug) PokecubeCore.LOGGER.info(move.move.name + ": animations: "
                        + move.move.baseEntry.animations);
                move.setAnimation(new AnimationMultiAnimations(move.move));
                continue;
            }
            String anim = move.move.animDefault;
            if (anim == null || anim.equals("none")) continue;
            if (!move.move.animDefault.endsWith(":~" + move.name)) move.move.animDefault = move.move.animDefault + ":~"
                    + move.name;
            anim = move.move.animDefault;
            if (PokecubeMod.debug) PokecubeCore.LOGGER.info(move.move.name + ": preset animation: "
                    + move.move.animDefault);
            final IMoveAnimation animation = MoveAnimationHelper.getAnimationPreset(anim);
            if (animation != null) move.setAnimation(animation);
        }
    }

    // Finds all Move_Basics inside this package and registers them.
    static void registerAutodetect()
    {
        List<Class<?>> foundClasses;
        // Register moves.
        if (PokecubeMod.debug) PokecubeCore.LOGGER.info("Autodecting Moves...");
        try
        {
            int num = 0;
            for (final Package pack : MovesAdder.packages)
            {
                if (pack == null) continue;
                foundClasses = ClassFinder.find(pack.getName());
                for (final Class<?> candidateClass : foundClasses)
                    if (Move_Basic.class.isAssignableFrom(candidateClass) && candidateClass.getEnclosingClass() == null)
                        try
                        {
                            final Move_Basic move = (Move_Basic) candidateClass.getConstructor().newInstance();
                            if (MovesUtils.isMoveImplemented(move.name))
                            {
                                PokecubeCore.LOGGER.info("Error, Double registration of " + move.name
                                        + " Replacing old entry with new one.");
                                num--;
                            }
                            num++;
                            MovesAdder.registerMove(move);
                        }
                        catch (final Exception e)
                        {
                            PokecubeCore.LOGGER.error("Skipping Move Class {}", candidateClass, e);
                        }
            }
            PokecubeCore.LOGGER.debug("Registered " + num + " Custom Moves");
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        // Register Move Actions.
        try
        {
            for (final Package pack : MovesAdder.packages)
            {
                if (pack == null) continue;
                foundClasses = ClassFinder.find(pack.getName());
                for (final Class<?> candidateClass : foundClasses)
                    if (IMoveAction.class.isAssignableFrom(candidateClass) && candidateClass
                            .getEnclosingClass() == null)
                    {
                        final IMoveAction move = (IMoveAction) candidateClass.getConstructor().newInstance();
                        MoveEventsHandler.register(move);
                    }
            }
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void registerMove(final Move_Base move_Base)
    {
        MovesUtils.registerMove(move_Base);
    }

    public static void registerMoves()
    {
        // Initialize the databases
        Database.preInitMoves();

        MovesAdder.registerAutodetect();
        MovesAdder.registerRemainder();
        // Reload the moves databases to apply the animations to the newly added
        // moves.
        Database.preInitMoves();
    }

    /**
     * Only registers contact and self, as distances moves usually should have
     * some effect.
     */
    public static void registerRemainder()
    {
        int num = 0;
        for (final MoveEntry e : MoveEntry.values())
            if (!MovesUtils.isMoveImplemented(e.name))
            {
                boolean doesSomething = GZMoveManager.isZMove(e.baseEntry);

                doesSomething |= e.baseEntry.preset != null;
                doesSomething |= e.change != 0;
                doesSomething |= e.power != -2;
                doesSomething |= e.statusChange != 0;
                doesSomething |= e.selfDamage != 0;
                doesSomething |= e.selfHealRatio != 0;
                doesSomething |= e.baseEntry.extraInfo != -1;
                if (!doesSomething) for (int i = 0; i < e.attackedStatModification.length; i++)
                {
                    doesSomething |= e.attackedStatModification[i] != 0;
                    doesSomething |= e.attackerStatModification[i] != 0;
                }

                if (doesSomething)
                {
                    Class<? extends Move_Base> moveClass = e.baseEntry.preset != null ? MovesAdder.presetMap.get(
                            e.baseEntry.preset) : Move_Basic.class;
                    if (moveClass == null) moveClass = Move_Basic.class;
                    if (GZMoveManager.isZMove(e.baseEntry)) moveClass = Z_Move_Basic.class;

                    Move_Base toAdd;
                    try
                    {
                        toAdd = moveClass.getConstructor(String.class).newInstance(e.name);
                        MovesAdder.registerMove(toAdd);
                        num++;
                    }
                    catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                            | InvocationTargetException | NoSuchMethodException | SecurityException e1)
                    {
                        e1.printStackTrace();
                    }
                }
            }
        if (PokecubeMod.debug) PokecubeCore.LOGGER.info("Registered " + num + " Database Moves");
    }
}