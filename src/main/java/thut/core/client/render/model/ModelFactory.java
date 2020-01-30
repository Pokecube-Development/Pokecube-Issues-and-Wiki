package thut.core.client.render.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.util.ResourceLocation;
import thut.core.client.render.animation.ModelHolder;
import thut.core.client.render.mca.McaModel;
import thut.core.client.render.obj.ObjModel;
import thut.core.client.render.x3d.X3dModel;
import thut.core.common.ThutCore;

public class ModelFactory
{
    public static interface IFactory<T extends IModel>
    {
        T create(ResourceLocation model);
    }

    private static final Map<String, IFactory<?>> modelFactories = Maps.newHashMap();
    public static final List<String>              knownExtension = Lists.newArrayList();

    static
    {
        ModelFactory.registerIModel("x3d", X3dModel::new);
        ModelFactory.registerIModel("mca", McaModel::new);
        ModelFactory.registerIModel("obj", ObjModel::new);
        // ModelFactory.registerIModel("tbl", TabulaModelParser::load);
        // ModelFactory.registerIModel("tbl", TblModel::new);
    }

    public static IModel create(final ModelHolder model)
    {
        final String path = model.model.getPath();
        String ext = path.contains(".") ? path.substring(path.lastIndexOf(".") + 1, path.length()) : "";
        if (ext.isEmpty())
        {
            IModel ret = null;
            for (final String ext1 : ModelFactory.knownExtension)
            {
                final IFactory<?> factory = ModelFactory.modelFactories.get(ext1);
                final ResourceLocation model1 = new ResourceLocation(model.model.getNamespace(), path + "." + ext1);
                ThutCore.LOGGER.debug("Checking " + model1);
                ret = factory.create(model1);
                ext = ext1;
                if (ret != null && ret.isValid()) break;
            }
            if (ret == null) ret = new X3dModel();
            if (!ret.isValid()) ThutCore.LOGGER.error("No Model found for " + model.model);
            else
            {
                ThutCore.LOGGER.debug("Successfully loaded model for " + model.model);
                model.extension = ext;
            }
            return ret;
        }
        else
        {
            final IFactory<?> factory = ModelFactory.modelFactories.get(ext);
            model.extension = ext;
            return factory.create(model.model);
        }
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
