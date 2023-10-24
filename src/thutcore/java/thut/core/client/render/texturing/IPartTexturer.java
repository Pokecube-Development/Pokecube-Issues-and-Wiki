package thut.core.client.render.texturing;

import net.minecraft.resources.ResourceLocation;
import thut.core.client.render.animation.AnimationXML.CustomTex;
import thut.core.client.render.animation.AnimationXML.Phase;

public interface IPartTexturer
{
    default void reset()
    {
    }

    /**
     * Adds mapping for a custom state's texture
     *
     * @param part
     *            - Part or Material name
     * @param state
     *            - State to be mapped, either AI state, or an integer.
     * @param tex
     *            - Texture being mapped.
     */
    default void addCustomMapping(final String part, final String state, final String tex)
    {

    }

    /**
     * Adds a mapping of part texture.
     *
     * @param part
     *            - The part or material to be textured
     * @param tex
     *            - The name of the texture.
     */
    default void addMapping(final String part, final String tex)
    {

    }

    void init(CustomTex tex);

    default void applyTexturePhase(final Phase phase)
    {
        // Do nothing by default
    }

    default boolean isHidden(final String part)
    {
        return false;
    }

    /**
     * Applies the texture for the part.<br>
     * This method will bind the texture to render engine for the part.
     *
     * @param part
     */
    ResourceLocation getTexture(String part, ResourceLocation default_);

    /**
     * Binds the object under consideration.
     *
     * @param thing
     */
    default void bindObject(final Object thing)
    {

    }

    /**
     * Is there a mapping already for this part - used for material specific
     * textures.
     *
     * @param part
     * @return
     */
    boolean hasMapping(String part);

    /**
     * @param part
     * @return
     */
    default void modifiyRGBA(final String part, final int[] rgbaIn)
    {

    }

    /**
     * Shifts the UVs for the texture animation
     *
     * @param part
     * @param toFill
     * @return
     */
    boolean shiftUVs(String part, double[] toFill);
}
