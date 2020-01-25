package thut.core.client.render.tabula.old;

import java.io.InputStream;
import java.io.InputStreamReader;

import thut.core.client.render.tabula.json.TblJson;

/**
 * Class for parsing json files to containers.
 *
 * @author iLexiconn
 * @since 0.1.0
 */
public class JsonHelper
{
    public static TblJson parseTabulaModel(final InputStream stream)
    {
        return JsonFactory.getGson().fromJson(new InputStreamReader(stream), TblJson.class);
    }
}
