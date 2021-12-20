package thut.api.util;

import javax.xml.namespace.QName;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import pokecube.core.database.pokedex.PokedexEntryLoader;
import pokecube.core.database.pokedex.PokedexEntryLoader.StatsNode;

public class JsonUtil
{
    public static final Gson gson;
    static
    {
        gson = new GsonBuilder().registerTypeAdapter(QName.class, QNameAdaptor.INSTANCE).setPrettyPrinting()
                .disableHtmlEscaping().setExclusionStrategies(UnderscoreIgnore.INSTANCE).create();
        PokedexEntryLoader.missingno.stats = new StatsNode();
    }
}
