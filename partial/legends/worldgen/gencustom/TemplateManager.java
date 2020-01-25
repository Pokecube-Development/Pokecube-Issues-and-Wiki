/*package pokecube.legends.worldgen.gencustom;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import com.google.common.collect.Lists;

import pokecube.core.database.Database;
import pokecube.core.world.gen.template.PokecubeTemplates;

public class TemplateManager
{
    public static List<String> legends_templates = Lists.newArrayList();

    static
    {
    	//legends Buildings
        legends_templates.add("space_temple.nbt");
        legends_templates.add("celebi_temple.nbt");
        legends_templates.add("hooh_tower.nbt");
        legends_templates.add("kyogre_temple.nbt");
        legends_templates.add("groudon_temple.nbt");
        legends_templates.add("regis_temple.nbt");
        legends_templates.add("xerneas_place.nbt");
        legends_templates.add("yveltal_place.nbt");
        legends_templates.add("nature_place.nbt");
        legends_templates.add("keldeo_place.nbt");
        legends_templates.add("lugia_tower.nbt");
        legends_templates.add("portal_temple.nbt");
        legends_templates.add("victini_place.nbt");
        legends_templates.add("zacian_temple.nbt");
        legends_templates.add("zamazenta_temple.nbt");
    }

    private static boolean copyFile(String fileName, boolean overwrite) throws IOException
    {
        String defaults = "/assets/pokecube_legends/structures/";
        String name = defaults + fileName;
        File file = new File(PokecubeTemplates.TEMPLATES, fileName);
        if (file.exists() && !overwrite) return false;
        InputStream res = (Database.class).getResourceAsStream(name);
        Files.copy(res, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return true;
    }

    public static void initTemplates() throws IOException
    {
        boolean copied = false;

        for (String struct : legends_templates)
        {
            boolean copy = copyFile(struct, false);
            copied = copy || copied;
        }
        // This was first run, or something changed, so we want to overwrite
        // worldgen
        if (copied)
        {
            copyFile("worldgen.json", true);
        }
    }
}
*/