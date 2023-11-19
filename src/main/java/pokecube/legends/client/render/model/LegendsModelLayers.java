package pokecube.legends.client.render.model;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import pokecube.legends.Reference;

public class LegendsModelLayers
{
    public static final ModelLayerLocation IMPRISONMENT_ARMOR_INNER = register("imprisonmemt_armor", "inner");
    public static final ModelLayerLocation IMPRISONMENT_ARMOR_OUTER = register("imprisonmemt_armor", "outer");

    private static ModelLayerLocation register(String name, String location)
    {
        return new ModelLayerLocation(new ResourceLocation(Reference.ID, name), location);
    }
}
