package thut.api;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

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
    private Vector3f loadedOffset = new Vector3f(0);

    private Vector3f loadedScale = new Vector3f(1);

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
        this(model, null, ResourceLocation.fromNamespaceAndPath(model.getNamespace(), model.getPath() + ".xml"), model.getPath());
    }

    public Vector3f getLoadedOffset()
    {
        return loadedOffset;
    }

    public void setLoadedOffset(Vector3f loadedOffset)
    {
        this.loadedOffset = loadedOffset;
    }

    public Vector3f getLoadedScale()
    {
        return loadedScale;
    }

    public void setLoadedScale(Vector3f loadedScale)
    {
        this.loadedScale = loadedScale;
    }
}
