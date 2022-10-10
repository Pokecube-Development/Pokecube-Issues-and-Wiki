/**
 *
 */
package pokecube.core.client;

import net.minecraft.resources.ResourceLocation;
import pokecube.core.impl.PokecubeMod;

/**
 * @author Manchou
 */
public class Resources
{

    public final static String TEXTURE_FOLDER = "textures/";
    public final static String TEXTURE_GUI_FOLDER = Resources.TEXTURE_FOLDER + "gui/";
    public final static String TEXTURE_GUI_ICON_FOLDER = Resources.TEXTURE_GUI_FOLDER + "icons/";

    public final static String TEXTURE_PARTICLES = Resources.TEXTURE_FOLDER + "particles.png";

    public final static ResourceLocation GUI_POKEDEX;
    public final static ResourceLocation GUI_BATTLE;
    public final static ResourceLocation GUI_HEAL_TABLE;
    public final static ResourceLocation GUI_POKEMOB;

    public final static ResourceLocation STATUS_PAR;
    public final static ResourceLocation STATUS_FRZ;

    public final static ResourceLocation PARTICLES;

    public final static ResourceLocation SLOT_ICON_CUBE;
    public final static ResourceLocation SLOT_ICON_TM;

    public final static ResourceLocation TAB_ICON_AI;
    public final static ResourceLocation TAB_ICON_INVENTORY;
    public final static ResourceLocation TAB_ICON_ROUTES;
    public final static ResourceLocation TAB_ICON_STORAGE;

    public final static ResourceLocation WIDGETS;
    public final static ResourceLocation WIDGETS_NM;

    static
    {

        GUI_POKEDEX = new ResourceLocation(PokecubeMod.ID, Resources.TEXTURE_GUI_FOLDER + "pokedexgui.png");

        GUI_BATTLE = new ResourceLocation(PokecubeMod.ID, Resources.TEXTURE_GUI_FOLDER + "battlegui.png");

        GUI_HEAL_TABLE = new ResourceLocation(PokecubeMod.ID, Resources.TEXTURE_GUI_FOLDER + "pokecentergui.png");

        STATUS_PAR = new ResourceLocation(PokecubeMod.ID, Resources.TEXTURE_FOLDER + "par.png");

        STATUS_FRZ = new ResourceLocation(PokecubeMod.ID, Resources.TEXTURE_FOLDER + "frz.png");

        GUI_POKEMOB = new ResourceLocation(PokecubeMod.ID, Resources.TEXTURE_GUI_FOLDER + "pokemob.png");

        PARTICLES = new ResourceLocation(PokecubeMod.ID, Resources.TEXTURE_FOLDER + "particles.png");

        SLOT_ICON_CUBE = new ResourceLocation(PokecubeMod.ID, Resources.TEXTURE_GUI_ICON_FOLDER + "slot_cube");
        SLOT_ICON_TM = new ResourceLocation(PokecubeMod.ID, Resources.TEXTURE_GUI_ICON_FOLDER + "slot_tm");

        TAB_ICON_AI = new ResourceLocation(PokecubeMod.ID, Resources.TEXTURE_GUI_ICON_FOLDER + "tab_ai.png");
        TAB_ICON_INVENTORY = new ResourceLocation(PokecubeMod.ID,
                Resources.TEXTURE_GUI_ICON_FOLDER + "tab_inventory.png");
        TAB_ICON_ROUTES = new ResourceLocation(PokecubeMod.ID, Resources.TEXTURE_GUI_ICON_FOLDER + "tab_routes.png");
        TAB_ICON_STORAGE = new ResourceLocation(PokecubeMod.ID, Resources.TEXTURE_GUI_ICON_FOLDER + "tab_storage.png");

        WIDGETS = new ResourceLocation(PokecubeMod.ID, Resources.TEXTURE_GUI_FOLDER + "widgets/widgets.png");
        WIDGETS_NM = new ResourceLocation(PokecubeMod.ID, Resources.TEXTURE_GUI_FOLDER + "widgets/widgets_nm.png");
    }
}
