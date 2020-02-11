package thut.core.client.render.texturing;

import thut.core.client.render.animation.AnimationXML.CustomTex;
import thut.core.client.render.animation.AnimationXML.Phase;

public interface IPartTexturer
{
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
    void addCustomMapping(String part, String state, String tex);

    /**
     * Adds a mapping of part texture.
     *
     * @param part
     *            - The part or material to be textured
     * @param tex
     *            - The name of the texture.
     */
    void addMapping(String part, String tex);

    void init(CustomTex tex);

    default void applyTexturePhase(final Phase phase)
    {
        // Do nothing by default
    }

    /**
     * Applies the texture for the part.<br>
     * This method will bind the texture to render engine for the part.
     *
     * @param part
     */
    void applyTexture(String part);

    /**
     * Binds the object under consideration.
     *
     * @param thing
     */
    void bindObject(Object thing);

    /**
     * Is there a mapping already for this part - used for material specific
     * textures.
     *
     * @param part
     * @return
     */
    boolean hasMapping(String part);

    /**
     * Should the part use flat shading. Defaults to true
     *
     * @param part
     * @return
     */
    boolean isFlat(String part);

    /**
     * Shifts the UVs for the texture animation
     *
     * @param part
     * @param toFill
     * @return
     */
    boolean shiftUVs(String part, double[] toFill);
}
