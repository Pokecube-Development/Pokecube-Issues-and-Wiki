package thut.api.util;

import javax.xml.namespace.QName;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonUtil
{
    public static final Gson gson;
    public static final Gson smol_gson;
    static
    {
        gson = new GsonBuilder().registerTypeAdapter(QName.class, QNameAdaptor.INSTANCE).setPrettyPrinting()
                .disableHtmlEscaping().setLenient().setExclusionStrategies(UnderscoreIgnore.INSTANCE).create();
        smol_gson = new GsonBuilder().registerTypeAdapter(QName.class, QNameAdaptor.INSTANCE).setLenient()
                .disableHtmlEscaping().setExclusionStrategies(UnderscoreIgnore.INSTANCE).create();
    }
}
