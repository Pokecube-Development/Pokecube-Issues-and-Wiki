package thut.api.util;

import javax.xml.namespace.QName;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonUtil
{
    public static final Gson gson;
    static
    {
        gson = new GsonBuilder().registerTypeAdapter(QName.class, QNameAdaptor.INSTANCE).setPrettyPrinting()
                .disableHtmlEscaping().setExclusionStrategies(UnderscoreIgnore.INSTANCE).create();
    }
}
