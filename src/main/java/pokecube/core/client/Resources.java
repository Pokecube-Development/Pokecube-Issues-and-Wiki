/**
 *
 */
package pokecube.core.client;

import net.minecraft.resources.ResourceLocation;
import pokecube.core.interfaces.PokecubeMod;

/**
 * @author Manchou
 */
public class Resources
{

    public final static String TEXTURE_FOLDER     = "textures/";
    public final static String TEXTURE_GUI_FOLDER = Resources.TEXTURE_FOLDER + "gui/";

    public final static String TEXTURE_PARTICLES = Resources.TEXTURE_FOLDER + "particles.png";

    public final static ResourceLocation GUI_POKEDEX = new ResourceLocation(PokecubeMod.ID, Resources.TEXTURE_GUI_FOLDER
            + "pokedexgui.png");

    public final static ResourceLocation GUI_BATTLE = new ResourceLocation(PokecubeMod.ID, Resources.TEXTURE_GUI_FOLDER
            + "battlegui.png");

    public final static ResourceLocation GUI_HEAL_TABLE = new ResourceLocation(PokecubeMod.ID,
            Resources.TEXTURE_GUI_FOLDER + "pokecentergui.png");

    public final static ResourceLocation STATUS_PAR = new ResourceLocation(PokecubeMod.ID, Resources.TEXTURE_FOLDER
            + "par.png");

    public final static ResourceLocation STATUS_FRZ = new ResourceLocation(PokecubeMod.ID, Resources.TEXTURE_FOLDER
            + "frz.png");

    public final static ResourceLocation GUI_POKEMOB = new ResourceLocation(PokecubeMod.ID, Resources.TEXTURE_GUI_FOLDER
            + "pokemob.png");

    public final static ResourceLocation PARTICLES = new ResourceLocation(PokecubeMod.ID, Resources.TEXTURE_FOLDER
            + "particles.png");

}
