package pokecube.core.init;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.WoodType;
import pokecube.core.PokecubeCore;

public class BerriesWoodType
{
    public static final WoodType ENIGMA = addWoodTypes("enigma");
    public static final WoodType LEPPA = addWoodTypes("leppa");
    public static final WoodType NANAB = addWoodTypes("nanab");
    public static final WoodType ORAN = addWoodTypes("oran");
    public static final WoodType PECHA = addWoodTypes("pecha");
    public static final WoodType SITRUS = addWoodTypes("sitrus");
    private final String name;

    protected BerriesWoodType(String name) {
        this.name = name;
    }

    public static WoodType addWoodTypes(String name)
    {
        return WoodType.register(WoodType.create(new ResourceLocation(PokecubeCore.MODID, name).toString()));
    }

    public static void register()
    {
        Sheets.addWoodType(ENIGMA);
        Sheets.addWoodType(LEPPA);
        Sheets.addWoodType(ORAN);
        Sheets.addWoodType(PECHA);
        Sheets.addWoodType(SITRUS);
    }
}
