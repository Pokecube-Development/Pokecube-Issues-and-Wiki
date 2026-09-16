package pokecube.api.effects;

import java.util.function.Consumer;

public class ParticleEffects
{
    /**
     * Adds the MovePacketInfo for rendering client side. This is set in the ClientMod, so as to
     * not upset the server for trying to load in any client side code for rendering, etc
     */
    public static Consumer<IMoveAnimation.MovePacketInfo> ADD_FOR_RENDER = info->{};
    /**
     * Adds the MovePacketInfo for any server side processing needed, this is presently not implemented.
     */
    public static Consumer<IMoveAnimation.MovePacketInfo> ADD_FOR_SERVER = info->{};

    public static void init()
    {
        VectorPositionSource.init();
        IMoveAnimation.TaggedEntityTracker.init();
    }
}
