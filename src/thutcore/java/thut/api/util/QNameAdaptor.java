package thut.api.util;

import java.io.IOException;

import javax.xml.namespace.QName;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class QNameAdaptor extends TypeAdapter<QName>
{
    public static final QNameAdaptor INSTANCE = new QNameAdaptor();

    @Override
    public QName read(final JsonReader in) throws IOException
    {
        return new QName(in.nextString());
    }

    @Override
    public void write(final JsonWriter out, final QName value) throws IOException
    {
        out.value(value.toString());
    }

}
