package pokecube.core.database.tags;

import java.lang.reflect.Type;

import javax.json.JsonException;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import pokecube.core.database.tags.StringTag.StringValue;

public class StringValueAdaptor implements JsonDeserializer<StringValue<?>>
{
    public static final StringValueAdaptor INSTANCE = new StringValueAdaptor();

    @Override
    public StringValue<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException
    {
        if (json.isJsonPrimitive())
        {
            String value = json.getAsString();
            String name = value;
            if (name.contains(";"))
            {
                var args = name.split(";");
                // Here we manually add in support for values, later we can
                // generalise this!
                name = args[0];
                value = args[1];

                try
                {
                    Float var = Float.parseFloat(value);
                    return new StringValue<>(name).setValue(var);
                }
                catch (NumberFormatException e)
                {
                    e.printStackTrace();
                }
            }
            return new StringValue<>(name);
        }
        throw new JsonException("Error. currently only supports strings here");
    }

}
