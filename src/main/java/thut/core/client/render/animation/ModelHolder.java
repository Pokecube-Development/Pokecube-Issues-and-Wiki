package thut.core.client.render.animation;

import org.w3c.dom.Node;

import net.minecraft.util.ResourceLocation;

public class ModelHolder
{
    public ResourceLocation model;
    public ResourceLocation texture;
    public ResourceLocation animation;
    public String           name;
    //This is set by the model factory.
    public String           extension = "";

    public ModelHolder(ResourceLocation model, ResourceLocation texture, ResourceLocation animation, String name)
    {
        this.model = model;
        this.texture = texture;
        this.animation = animation;
        this.name = name;
    }

    public void handleCustomTextures(Node node)
    {

    }

}
