package pokecube.legends.client.render.model;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import pokecube.legends.Reference;

public class LegendsModelLayers
{
    public static final ModelLayerLocation IMPRISONMENT_ARMOR_INNER = register("imprisonmemt_armor", "inner");

    private static ModelLayerLocation register(String name, String location)
    {
        return new ModelLayerLocation(new ResourceLocation(Reference.ID, name), location);
    }
}
