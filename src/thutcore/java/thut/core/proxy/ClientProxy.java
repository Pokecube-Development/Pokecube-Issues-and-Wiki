package thut.core.proxy;

import net.minecraft.core.RegistryAccess;

public class ClientProxy extends CommonProxy
{
    @Override
    public RegistryAccess getRegistries()
    {
        final net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        // This is null on single player, so we have an integrated server
        if (mc.getCurrentServer() == null) return super.getRegistries();
        if (mc.level == null) return null;
        return mc.level.registryAccess();
    }
}
