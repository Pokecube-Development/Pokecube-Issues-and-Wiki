package thut.core.client.render.animation;

import java.util.HashMap;

import javax.annotation.Nullable;

import org.w3c.dom.NamedNodeMap;

import com.google.common.collect.Maps;

import thut.core.client.render.animation.prefab.AdvancedFlapAnimation;
import thut.core.client.render.animation.prefab.BasicFlapAnimation;
import thut.core.client.render.animation.prefab.BiWalkAnimation;
import thut.core.client.render.animation.prefab.QuadWalkAnimation;
import thut.core.client.render.animation.prefab.SnakeMovement;

/**
 * Used for determining what animation to make when reading from XMLs
 *
 * @author Thutmose
 */
public class AnimationRegistry
{
    /**
     * Used to convert from part names to identifiers if needed.
     *
     * @author Thutmose
     */
    public static interface IPartRenamer
    {
        void convertToIdents(String[] names);
    }

    /** Map of XML node name to animation to read in. */
    public static HashMap<String, Class<? extends Animation>> animations = Maps.newHashMap();

    /**
     * Map of XML name to animation phase, will overwrite animation name with
     * the value.
     */
    public static HashMap<String, String> animationPhases = Maps.newHashMap();

    /** Add in defaults. */
    static
    {
        AnimationRegistry.animations.put("quadWalk", QuadWalkAnimation.class);
        AnimationRegistry.animations.put("biWalk", BiWalkAnimation.class);
        AnimationRegistry.animations.put("flap", BasicFlapAnimation.class);
        AnimationRegistry.animations.put("advFlap", AdvancedFlapAnimation.class);
        AnimationRegistry.animations.put("snakeWalk", SnakeMovement.class);
        AnimationRegistry.animations.put("snakeFly", SnakeMovement.class);
        AnimationRegistry.animationPhases.put("snakeFly", "flying");
        AnimationRegistry.animations.put("snakeIdle", SnakeMovement.class);
        AnimationRegistry.animationPhases.put("snakeIdle", "idle");
        AnimationRegistry.animations.put("snakeSwim", SnakeMovement.class);
        AnimationRegistry.animationPhases.put("snakeSwim", "swimming");
    }

    /**
     * Generates the animation for the given name, and nodemap. Renamer is used
     * to convert to identifiers in the cases where that is needed. <br>
     * <br>
     * This method will also then change the name of the animation to
     * animationName if it is not null.
     *
     * @param name
     * @param map
     * @param renamer
     * @return
     */
    public static Animation make(String name, NamedNodeMap map, @Nullable IPartRenamer renamer)
    {
        Animation ret = null;
        final Class<? extends Animation> toMake = AnimationRegistry.animations.get(name);
        if (toMake != null) try
        {
            ret = toMake.newInstance();
            ret.init(map, renamer);
            if (AnimationRegistry.animationPhases.containsKey(name)) ret.name = AnimationRegistry.animationPhases.get(
                    name);
        }
        catch (final InstantiationException e)
        {
            e.printStackTrace();
        }
        catch (final IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return ret;
    }
}
