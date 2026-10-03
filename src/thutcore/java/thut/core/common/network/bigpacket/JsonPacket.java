package thut.core.common.network.bigpacket;

import java.nio.charset.StandardCharsets;

import thut.api.util.JsonUtil;

public abstract class JsonPacket extends BigPacket
{

    public JsonPacket()
    {
        super();
    }

    public JsonPacket(Object o)
    {
        super();
        String json = JsonUtil.smol_gson.toJson(o);
        this.setData(json.getBytes(StandardCharsets.UTF_8));
    }

    public JsonPacket(String data)
    {
        super();
        this.setData(data.getBytes(StandardCharsets.UTF_8));
    }
}
