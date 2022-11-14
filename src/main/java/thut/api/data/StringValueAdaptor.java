package thut.api.data;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import net.minecraft.resources.ResourceLocation;
import thut.api.data.StringTag.StringValue;
import thut.core.common.ThutCore;

public class StringValueAdaptor implements JsonDeserializer<StringValue<?>>
{
    final Class<?> type;

    public StringValueAdaptor(Class<?> type)
    {
        this.type = type;
    }

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

                if (this.type != null)
                {
                    if (type == Float.class || type == Double.class)
                    {
                        try
                        {
                            float var = Float.parseFloat(value);
                            return new StringValue<>(name).setValue(var);
                        }
                        catch (NumberFormatException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    else if (type == String.class)
                    {
                        return new StringValue<>(name).setValue(value);
                    }
                    else if (type == ResourceLocation.class)
                    {
                        return new StringValue<>(name).setValue(new ResourceLocation(value));
                    }
                    else
                    {
                        throw new JsonParseException("Error. unsupported sub class type!");
                    }
                }
                else
                {
                    ThutCore.LOGGER.error("Warning, got argument {} when no class set!", value);
                    return new StringValue<>(name);
                }
            }
            return new StringValue<>(name);
        }
        else
        {

        }
        throw new JsonParseException("Error. unsupported format!");
    }

}
