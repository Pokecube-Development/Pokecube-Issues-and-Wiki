package thut.core.client.render.tabula.old;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author iLexiconn
 * @since 0.1.0
 */
public class JsonFactory
{
    private static Gson gson       = new Gson();
    private static Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();

    public static Gson getGson()
    {
        return JsonFactory.gson;
    }

    public static Gson getPrettyGson()
    {
        return JsonFactory.prettyGson;
    }
}
