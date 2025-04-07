package pokecube.core.utils;

import net.minecraft.resources.ResourceLocation;
import pokecube.core.impl.PokecubeMod;

/**
 * @author Manchou
 */
public class Resources
{

    public final static String TEXTURE_FOLDER = "textures/";
    public final static String TEXTURE_GUI_FOLDER = "textures/gui/";
    public final static String TEXTURE_GUI_ICON_FOLDER = "icons/";
    public final static String TEXTURE_SLOT_ICON_FOLDER = "gui/sprites/icons/";

    public final static String TEXTURE_PARTICLES = Resources.TEXTURE_FOLDER + "particles.png";

    public final static ResourceLocation GUI_POKEDEX;
    public final static ResourceLocation GUI_BATTLE;
    public final static ResourceLocation GUI_HEAL_TABLE;
    public final static ResourceLocation GUI_POKEMOB;

    public final static ResourceLocation STATUS_PAR;
    public final static ResourceLocation STATUS_FRZ;
    public final static ResourceLocation STATUS_TERA;

    public final static ResourceLocation PARTICLES;

    public final static ResourceLocation SLOT_ICON_CUBE;
    public final static ResourceLocation SLOT_ICON_TM;
    public final static ResourceLocation SLOT_ICON_BOOK;
    public final static ResourceLocation SLOT_ICON_BOTTLE;
    public final static ResourceLocation SLOT_ICON_DNA;
    public final static ResourceLocation SLOT_ICON_EGG;

    public final static ResourceLocation TAB_ICON_AI;
    public final static ResourceLocation TAB_ICON_INVENTORY;
    public final static ResourceLocation TAB_ICON_ROUTES;
    public final static ResourceLocation TAB_ICON_STORAGE;

    public final static ResourceLocation WIDGETS;
    public final static ResourceLocation WIDGETS_NM;
    public final static ResourceLocation WIDGETS_POKEDEX;

    public static final ResourceLocation ICONS_GUI_SHEET;
    public static final ResourceLocation ICONS_GUI_ATLAS;

    public static final ResourceLocation ICONS_MOB_SHEET;
    public static final ResourceLocation ICONS_MOB_ATLAS;

    static
    {
        ICONS_GUI_SHEET = ResourceLocation.fromNamespaceAndPath("pokecube", "textures/atlas/icons.png");
        ICONS_GUI_ATLAS = ResourceLocation.fromNamespaceAndPath("pokecube", "gui_icons");

        ICONS_MOB_SHEET = ResourceLocation.fromNamespaceAndPath("pokecube_mobs", "textures/atlas/icons.png");
        ICONS_MOB_ATLAS = ResourceLocation.fromNamespaceAndPath("pokecube_mobs", "mob_icons");

//        POKE_ICONS_ATLAS = ResourceLocation.fromNamespaceAndPath("pokecube", "gui_icons");

        GUI_POKEDEX = ResourceLocation.fromNamespaceAndPath(PokecubeMod.ID, TEXTURE_GUI_FOLDER + "pokedexgui.png");

        GUI_BATTLE = ResourceLocation.fromNamespaceAndPath(PokecubeMod.ID, TEXTURE_GUI_FOLDER + "battlegui.png");

        GUI_HEAL_TABLE = ResourceLocation.fromNamespaceAndPath(PokecubeMod.ID, TEXTURE_GUI_FOLDER + "pokecenter_gui.png");

        STATUS_PAR = ResourceLocation.fromNamespaceAndPath(PokecubeMod.ID, TEXTURE_FOLDER + "par.png");

        STATUS_FRZ = ResourceLocation.fromNamespaceAndPath(PokecubeMod.ID, TEXTURE_FOLDER + "frz.png");

        STATUS_TERA = ResourceLocation.fromNamespaceAndPath(PokecubeMod.ID, TEXTURE_FOLDER + "tera.png");

        GUI_POKEMOB = ResourceLocation.fromNamespaceAndPath(PokecubeMod.ID, TEXTURE_GUI_FOLDER + "pokemob.png");

        PARTICLES = ResourceLocation.fromNamespaceAndPath(PokecubeMod.ID, TEXTURE_FOLDER + "particles.png");

        SLOT_ICON_CUBE = ResourceLocation.fromNamespaceAndPath(PokecubeMod.ID, "slot_cube");
        SLOT_ICON_TM = ResourceLocation.fromNamespaceAndPath(PokecubeMod.ID, "slot_tm");
        SLOT_ICON_BOOK = ResourceLocation.fromNamespaceAndPath(PokecubeMod.ID, "slot_book");
        SLOT_ICON_BOTTLE = ResourceLocation.fromNamespaceAndPath(PokecubeMod.ID, "slot_bottle");
        SLOT_ICON_DNA = ResourceLocation.fromNamespaceAndPath(PokecubeMod.ID, "slot_dna");
        SLOT_ICON_EGG = ResourceLocation.fromNamespaceAndPath(PokecubeMod.ID, "slot_egg");

        TAB_ICON_AI = ResourceLocation.fromNamespaceAndPath(PokecubeMod.ID, TEXTURE_GUI_ICON_FOLDER + "tab_ai");
        TAB_ICON_INVENTORY = ResourceLocation.fromNamespaceAndPath(PokecubeMod.ID, TEXTURE_GUI_ICON_FOLDER + "tab_inventory");
        TAB_ICON_ROUTES = ResourceLocation.fromNamespaceAndPath(PokecubeMod.ID, TEXTURE_GUI_ICON_FOLDER + "tab_routes");
        TAB_ICON_STORAGE = ResourceLocation.fromNamespaceAndPath(PokecubeMod.ID, TEXTURE_GUI_ICON_FOLDER + "tab_storage");

        WIDGETS = ResourceLocation.fromNamespaceAndPath(PokecubeMod.ID, TEXTURE_GUI_FOLDER + "widgets/widgets.png");
        WIDGETS_NM = ResourceLocation.fromNamespaceAndPath(PokecubeMod.ID, TEXTURE_GUI_FOLDER + "widgets/widgets_nm.png");
        WIDGETS_POKEDEX = ResourceLocation.fromNamespaceAndPath(PokecubeMod.ID, TEXTURE_GUI_FOLDER + "widgets/pokedex_widgets.png");
    }
}
