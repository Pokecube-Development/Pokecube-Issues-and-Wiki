package pokecube.api.effects;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ParticleEffects
{
    /**
     * Adds the MovePacketInfo for rendering client side. This is set in the ClientMod, so as to
     * not upset the server for trying to load in any client side code for rendering, etc
     */
    public static Consumer<IAnimatedEffects.EffectPacketInfo> ADD_FOR_RENDER = info->{};
    /**
     * Adds the MovePacketInfo for any server side processing needed, this is presently not implemented.
     */
    public static Consumer<IAnimatedEffects.EffectPacketInfo> ADD_FOR_SERVER = info->{};

    /**
     * List of locators for evolution effect to anchor to, it picks first in the list that matches
     */
    public static List<String> EVO_ANCHORS = new ArrayList<>();

    static
    {
        // Put this in as a default.
        EVO_ANCHORS.add("body");
    }

    public static void init()
    {
        VectorPositionSource.init();
        IAnimatedEffects.TaggedEntityTracker.init();
    }
}
