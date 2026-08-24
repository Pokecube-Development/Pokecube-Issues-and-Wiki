package thut.core.client.render.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;
import thut.api.ModelHolder;
import thut.core.client.render.animation.AnimationLoader;
import thut.core.client.render.bbmodel.BBModel;
import thut.core.client.render.json.JsonModel;
import thut.core.client.render.model.IModel.IModelCallback;
import thut.core.client.render.x3d.X3dModel;
import thut.core.common.ThutCore;

import java.util.List;
import java.util.Map;

public class ModelFactory
{
    public static interface IFactory<T extends IModel>
    {
        T create(ResourceLocation model, IModelCallback callback);
    }

    private static final Map<String, IFactory<?>> modelFactories = Maps.newHashMap();
    public static final List<String> knownExtension = Lists.newArrayList();

    static
    {
        ModelFactory.registerIModel("bbmodel", BBModel::new, false);
        ModelFactory.registerIModel("json", JsonModel::new, false);
        ModelFactory.registerIModel("x3d", X3dModel::new, false);
    }

    public static IModel create(final ResourceLocation location, final ModelHolder model, final IModelCallback callback)
    {
        final String path = location.getPath();
        String ext = path.contains(".") ? path.substring(path.lastIndexOf(".") + 1) : "";
        if (ext.isEmpty())
        {
            IModel ret = null;
            for (final String ext1 : ModelFactory.knownExtension)
            {
                final IFactory<?> factory = ModelFactory.modelFactories.get(ext1);
                final ResourceLocation model1 = ResourceLocation.fromNamespaceAndPath(location.getNamespace(),
                        path + "." + ext1);
                if (ThutCore.conf.debug_models) ThutCore.LOGGER.debug("Checking {}", model1);
                ret = factory.create(model1, callback);
                ext = ext1;
                if (ret != null && ret.isValid()) break;
            }
            if (ret == null) ret = new X3dModel(location, callback).init(callback);
            if (!ret.isValid())
            {
                if (ThutCore.conf.debug_models) ThutCore.LOGGER.error("No Model found for {}", location);
            }
            else
            {
                if (ThutCore.conf.debug_models) ThutCore.LOGGER.debug("Successfully loaded model for {}", location);
                model.extension = ext;
            }
            return ret;
        }
        else
        {
            final IFactory<?> factory = ModelFactory.modelFactories.get(ext);
            if (factory == null)
            {
                System.out.println("No Model factory for " + location);
                ThutCore.LOGGER.error("No Model factory for {}, {}", ext, location);
                return null;
            }
            model.extension = ext;
            return factory.create(location, callback);
        }
    }

    public static IModel create(final ModelHolder model, final IModelCallback callback)
    {
        IModel made = ModelFactory.create(model.model, model, callback);
        if (!made.isValid()) for (final ResourceLocation loc : model.backupModels)
        {
            made = ModelFactory.create(loc, model, callback);
            if (made.isValid()) return made;
        }
        return made;
    }

    public static IModel create(final ModelHolder model)
    {
        return ModelFactory.create(model, m -> AnimationLoader.parse(model, m, null));
    }

    public static IModel createWithRenderer(final ModelHolder model, IModelRenderer<?> renderer)
    {
        return ModelFactory.create(model, m -> AnimationLoader.parse(model, m, renderer));
    }

    public static IModel createScaled(final ModelHolder model)
    {
        return ModelFactory.create(model, m -> {
            AnimationLoader.parse(model, m, null);
            for (IExtendedModelPart p : m.getParts().values())
            {
                if (p.getParent() == null)
                {
                    p.setPreScale(model.getLoadedScale());
                    p.setPreTranslations(model.getLoadedOffset());
                }
            }
        });
    }

    public static void registerIModel(final String extension, final IFactory<?> clazz)
    {
        registerIModel(extension, clazz, true);
    }

    public static void registerIModel(final String extension, final IFactory<?> clazz, boolean addFirst)
    {
        ModelFactory.modelFactories.put(extension, clazz);
        // Add new ones ahead of existing ones, as they are likely to be a replacement for rendering
        if (!ModelFactory.knownExtension.contains(extension))
        {
            if (addFirst) ModelFactory.knownExtension.addFirst(extension);
            else ModelFactory.knownExtension.add(extension);
        }
    }
}
