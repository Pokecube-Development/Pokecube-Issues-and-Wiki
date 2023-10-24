package thut.core.client.render.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

public class ModelFactory
{
    public static interface IFactory<T extends IModel>
    {
        T create(ResourceLocation model);
    }

    private static final Map<String, IFactory<?>> modelFactories = Maps.newHashMap();
    public static final List<String> knownExtension = Lists.newArrayList();

    static
    {
        ModelFactory.registerIModel("bbmodel", BBModel::new);
        ModelFactory.registerIModel("json", JsonModel::new);
        ModelFactory.registerIModel("x3d", X3dModel::new);
    }

    public static IModel create(final ResourceLocation location, final ModelHolder model, final IModelCallback callback)
    {
        final String path = location.getPath();
        String ext = path.contains(".") ? path.substring(path.lastIndexOf(".") + 1, path.length()) : "";
        if (ext.isEmpty())
        {
            IModel ret = null;
            for (final String ext1 : ModelFactory.knownExtension)
            {
                final IFactory<?> factory = ModelFactory.modelFactories.get(ext1);
                final ResourceLocation model1 = new ResourceLocation(location.getNamespace(), path + "." + ext1);
                if (ThutCore.conf.debug_models) ThutCore.LOGGER.debug("Checking " + model1);
                ret = factory.create(model1);
                ext = ext1;
                if (ret != null && ret.isValid()) break;
            }
            if (ret == null) ret = new X3dModel();
            if (!ret.isValid())
            {
                if (ThutCore.conf.debug_models) ThutCore.LOGGER.error("No Model found for " + location);
            }
            else
            {
                if (ThutCore.conf.debug_models) ThutCore.LOGGER.debug("Successfully loaded model for " + location);
                model.extension = ext;
            }
            return ret.init(callback);
        }
        else
        {
            final IFactory<?> factory = ModelFactory.modelFactories.get(ext);
            model.extension = ext;
            return factory.create(location).init(callback);
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
        return ModelFactory.create(model, m -> {
            AnimationLoader.parse(model, m, null);
        });
    }

    public static IModel createWithRenderer(final ModelHolder model, IModelRenderer<?> renderer)
    {
        return ModelFactory.create(model, m -> {
            AnimationLoader.parse(model, m, renderer);
        });
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

    public static Set<String> getValidExtensions()
    {
        return ModelFactory.modelFactories.keySet();
    }

    public static void registerIModel(final String extension, final IFactory<?> clazz)
    {
        ModelFactory.modelFactories.put(extension, clazz);
        if (!ModelFactory.knownExtension.contains(extension)) ModelFactory.knownExtension.add(extension);
    }
}
