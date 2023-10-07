package thut.core.common.network.bigpacket;

import java.io.UnsupportedEncodingException;

import net.minecraft.network.FriendlyByteBuf;
import thut.api.util.JsonUtil;

public abstract class JsonPacket extends BigPacket
{

    public JsonPacket()
    {
        super();
    }

    public JsonPacket(final FriendlyByteBuf buffer)
    {
        super(buffer);
    }

    public JsonPacket(Object o)
    {
        super();
        String json = JsonUtil.smol_gson.toJson(o);
        try
        {
            this.setData(json.getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
    }

    public JsonPacket(String data)
    {
        super();
        try
        {
            this.setData(data.getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
    }
}
