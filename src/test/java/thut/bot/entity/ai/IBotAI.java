package thut.bot.entity.ai;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.server.level.ServerPlayer;

public interface IBotAI
{
    static Map<String, Factory> REGISTRY = Maps.newHashMap();

    void tick();

    public static interface Factory
    {
        IBotAI create(ServerPlayer owner);

        public static Factory get(final String key)
        {
            return IBotAI.REGISTRY.get(key);
        }
    }
}
