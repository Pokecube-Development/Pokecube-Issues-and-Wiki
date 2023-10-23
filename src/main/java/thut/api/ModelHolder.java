package thut.api;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.resources.ResourceLocation;
import thut.api.maths.Vector3;

public class ModelHolder
{
    public ResourceLocation model;
    public ResourceLocation texture;
    public ResourceLocation animation;
    public String name;

    public List<ResourceLocation> backupAnimations = Lists.newArrayList();
    public List<ResourceLocation> backupModels = Lists.newArrayList();
    // This is set by the model factory.
    public String extension = "";

    // These are set by the AnimationLoader for if the model is loaded without a
    // renderer
    private Vector3 loadedOffset = null;

    private Vector3 loadedScale = null;

    public ModelHolder(final ResourceLocation model, final ResourceLocation texture, final ResourceLocation animation,
            final String name)
    {
        this.model = model;
        this.texture = texture;
        this.animation = animation;
        this.name = name;
    }

    public ModelHolder(ResourceLocation model)
    {
        this(model, null, new ResourceLocation(model.getNamespace(), model.getPath() + ".xml"), model.getPath());
    }

    public Vector3 getLoadedOffset()
    {
        return loadedOffset;
    }

    public void setLoadedOffset(Vector3 loadedOffset)
    {
        this.loadedOffset = loadedOffset;
    }

    public Vector3 getLoadedScale()
    {
        return loadedScale;
    }

    public void setLoadedScale(Vector3 loadedScale)
    {
        this.loadedScale = loadedScale;
    }
}
