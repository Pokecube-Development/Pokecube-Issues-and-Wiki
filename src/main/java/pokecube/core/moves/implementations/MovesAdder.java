package pokecube.core.moves.implementations;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import pokecube.api.PokecubeAPI;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.Move_Base;
import pokecube.api.moves.utils.IMoveAnimation;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.IMoveWorldEffect;
import pokecube.core.database.moves.MovesDatabases;
import pokecube.core.eventhandlers.MoveEventsHandler;
import pokecube.core.impl.PokecubeMod;
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
import pokecube.core.moves.zmoves.GZMoveManager;
import thut.lib.CompatParser.ClassFinder;

public class MovesAdder implements IMoveConstants
{
    public static Map<String, Class<? extends Move_Base>> presetMap = Maps.newHashMap();
    public static Set<Package> packages = Sets.newHashSet();

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

    public static void setupMoveAnimations()
    {
        for (final Move_Base move : MovesUtils.getKnownMoves())
        {
            if (move.move.root_entry != null && move.move.root_entry.animation != null
                    && !move.move.root_entry.animation.animations.isEmpty())
            {
                if (PokecubeMod.debug) PokecubeAPI.LOGGER
                        .info(move.move.name + ": animations: " + move.move.root_entry.animation.animations);
                move.setAnimation(new AnimationMultiAnimations(move.move));
                continue;
            }
            String anim = move.move.animDefault;
            if (anim == null || anim.equals("none")) continue;
            if (!move.move.animDefault.endsWith(":~" + move.name))
                move.move.animDefault = move.move.animDefault + ":~" + move.name;
            anim = move.move.animDefault;
            if (PokecubeMod.debug)
                PokecubeAPI.LOGGER.info(move.move.name + ": preset animation: " + move.move.animDefault);
            final IMoveAnimation animation = MoveAnimationHelper.getAnimationPreset(anim);
            if (animation != null) move.setAnimation(animation);
        }
    }

    // Finds all Move_Basics inside this package and registers them.
    static void registerAutodetect()
    {
        final List<Class<?>> foundClasses = Lists.newArrayList();

        for (final Package pack : MovesAdder.packages)
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

        // Register moves.
        PokecubeAPI.LOGGER.debug("Autodecting Moves...");
        try
        {
            int num = 0;
            for (final Class<?> candidateClass : foundClasses)
                if (Move_Basic.class.isAssignableFrom(candidateClass) && candidateClass.getEnclosingClass() == null) try
            {
                final Move_Basic move = (Move_Basic) candidateClass.getConstructor().newInstance();
                if (MovesUtils.isMoveImplemented(move.name))
                {
                    PokecubeAPI.LOGGER
                            .info("Error, Double registration of " + move.name + " Replacing old entry with new one.");
                    num--;
                }
                num++;
                boolean registered = MovesUtils.registerMove(move);
                if (!registered) PokecubeAPI.LOGGER.error("Failed to register move for Class {}", candidateClass);
            }
                catch (final Exception e)
            {
                PokecubeAPI.LOGGER.error("Skipping Move Class {}", candidateClass, e);
            }
            PokecubeAPI.LOGGER.debug("Registered " + num + " Custom Moves");
        }
        catch (final Exception e)
        {
            e.printStackTrace();
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
    }

    public static void registerMoves()
    {
        // Initialize the databases
        MovesDatabases.preInitLoad();

        MovesAdder.registerAutodetect();
        MovesAdder.registerRemainder();

        // Finally setup the animations for the moves Later we might sync these
        // from server to client, and run setup for animations again there.
        MovesAdder.setupMoveAnimations();
    }

    /**
     * Only registers contact and self, as distances moves usually should have
     * some effect.
     */
    public static void registerRemainder()
    {
        int num = 0;
        for (final MoveEntry e : MoveEntry.values()) if (!MovesUtils.isMoveImplemented(e.name))
        {
            boolean doesSomething = GZMoveManager.isGZDMove(e);

            String preset = e.root_entry._preset;

            doesSomething |= preset != null;
            doesSomething |= e.change != 0;
            doesSomething |= e.power != -2;
            doesSomething |= e.statusChange != 0;
            doesSomething |= e.selfDamage != 0;
            doesSomething |= e.selfHealRatio != 0;
            doesSomething |= e.root_entry._effect_index != -1;
            if (!doesSomething) for (int i = 0; i < e.attackedStatModification.length; i++)
            {
                doesSomething |= e.attackedStatModification[i] != 0;
                doesSomething |= e.attackerStatModification[i] != 0;
            }

            if (doesSomething)
            {
                Class<? extends Move_Base> moveClass = preset != null ? MovesAdder.presetMap.get(preset)
                        : Move_Basic.class;
                if (moveClass == null) moveClass = Move_Basic.class;

                if (GZMoveManager.isGZDMove(e))
                {
                    e.powerp = GZMoveManager.getPowerProvider(e);
                }

                Move_Base toAdd;
                try
                {
                    toAdd = moveClass.getConstructor(String.class).newInstance(e.name);
                    boolean registered = MovesUtils.registerMove(toAdd);
                    if (!registered) PokecubeAPI.LOGGER.error("Failed to register move for {} {}", e.name, moveClass);
                    num++;
                }
                catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException | NoSuchMethodException | SecurityException e1)
                {
                    e1.printStackTrace();
                }
            }
            else
            {
                PokecubeAPI.LOGGER.debug("Ignoring move {}, as could not figure out what it does...", e.name);
            }
        }
        PokecubeAPI.LOGGER.debug("Registered " + num + " Database Moves");
    }
}