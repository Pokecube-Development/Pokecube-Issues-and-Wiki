package thut.api;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.util.ResourceLocation;

public class ModelHolder
{
    public ResourceLocation       model;
    public ResourceLocation       texture;
    public ResourceLocation       animation;
    public List<ResourceLocation> backupAnimations = Lists.newArrayList();
    public List<ResourceLocation> backupModels     = Lists.newArrayList();
    public String                 name;
    // This is set by the model factory.
    public String extension = "";

    public ModelHolder(final ResourceLocation model, final ResourceLocation texture, final ResourceLocation animation,
            final String name)
    {
        this.model = model;
        this.texture = texture;
        this.animation = animation;
        this.name = name;
    }
}
