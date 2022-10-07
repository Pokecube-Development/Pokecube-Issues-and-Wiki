package pokecube.legends.init;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.WoodType;
import pokecube.legends.Reference;

public class LegendsWoodType
{
    public static final WoodType AGED = addWoodTypes("aged");
    public static final WoodType CONCRETE = addWoodTypes("concrete");
    public static final WoodType CONCRETE_DENSE = addWoodTypes("concrete_dense");
    public static final WoodType CORRUPTED = addWoodTypes("corrupted");
    public static final WoodType DISTORTIC = addWoodTypes("distortic");
    public static final WoodType INVERTED = addWoodTypes("inverted");
    public static final WoodType MIRAGE = addWoodTypes("mirage");
    public static final WoodType TEMPORAL = addWoodTypes("temporal");

    public static WoodType addWoodTypes(String name)
    {
        return WoodType.register(WoodType.create(new ResourceLocation(Reference.ID, name).toString()));
    }

    public static void register()
    {
        Sheets.addWoodType(AGED);
        Sheets.addWoodType(CONCRETE);
        Sheets.addWoodType(CONCRETE_DENSE);
        Sheets.addWoodType(CORRUPTED);
        Sheets.addWoodType(DISTORTIC);
        Sheets.addWoodType(INVERTED);
        Sheets.addWoodType(MIRAGE);
        Sheets.addWoodType(TEMPORAL);
    }
}
