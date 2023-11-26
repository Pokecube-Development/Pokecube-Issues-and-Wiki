package thut.core.client.render.animation;

import java.util.HashMap;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import thut.api.entity.animation.Animation;
import thut.api.entity.animation.Animation.IPartRenamer;
import thut.core.client.render.animation.AnimationXML.Phase;
import thut.core.client.render.animation.prefab.AdvancedFlapAnimation;
import thut.core.client.render.animation.prefab.BasicFlapAnimation;
import thut.core.client.render.animation.prefab.BiWalkAnimation;
import thut.core.client.render.animation.prefab.QuadWalkAnimation;
import thut.core.client.render.animation.prefab.SnakeMovement;
import thut.core.common.ThutCore;

/**
 * Used for determining what animation to make when reading from XMLs
 *
 * @author Thutmose
 */
public class AnimationRegistry
{

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
        //Sitting
        AnimationRegistry.animations.put("snakesit", SnakeMovement.class);
        AnimationRegistry.animationPhases.put("snakesit", "sitting");

        //Idle
        AnimationRegistry.animations.put("snakeidle", SnakeMovement.class);
        AnimationRegistry.animationPhases.put("snakeidle", "idle");

        //Walking
        AnimationRegistry.animations.put("quadwalk", QuadWalkAnimation.class);
        AnimationRegistry.animations.put("biwalk", BiWalkAnimation.class);
        AnimationRegistry.animations.put("snakewalk", SnakeMovement.class);

        //Floating
        AnimationRegistry.animations.put("snakefloating", SnakeMovement.class);
        AnimationRegistry.animationPhases.put("snakefloating", "floating");

        //Flying
        AnimationRegistry.animations.put("flap", BasicFlapAnimation.class);
        AnimationRegistry.animations.put("advflap", AdvancedFlapAnimation.class);

        AnimationRegistry.animations.put("snakefly", SnakeMovement.class);
        AnimationRegistry.animationPhases.put("snakefly", "flying");
        AnimationRegistry.animations.put("flywalk", QuadWalkAnimation.class);
        AnimationRegistry.animationPhases.put("flywalk", "flying");

        //Swimming
        AnimationRegistry.animations.put("swimbiwalk", BiWalkAnimation.class);
        AnimationRegistry.animationPhases.put("swimbiwalk", "swimming");
        AnimationRegistry.animations.put("swimwalk", QuadWalkAnimation.class);
        AnimationRegistry.animationPhases.put("swimwalk", "swimming");
        AnimationRegistry.animations.put("snakeswim", SnakeMovement.class);
        AnimationRegistry.animationPhases.put("snakeswim", "swimming");
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
    public static Animation make(final Phase map, @Nullable final IPartRenamer renamer)
    {
        String name = map.name;
        if (name == null) return null;
        name = ThutCore.trim(name);
        Animation ret = null;
        final Class<? extends Animation> toMake = AnimationRegistry.animations.get(name);
        if (toMake != null) try
        {
            ret = toMake.getConstructor().newInstance();
            ret.init(map, renamer);
            if (AnimationRegistry.animationPhases.containsKey(name)) ret.name = AnimationRegistry.animationPhases.get(
                    name);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        return ret;
    }
}
