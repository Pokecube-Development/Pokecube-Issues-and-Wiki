package thut.test;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import thut.test.worldgen.WorldgenTickTests;

@Mod(value = "testmod")
public class Tests
{
    public static boolean WORLGENTICKTEST = false;

    public Tests()
    {
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(
                () -> FMLNetworkConstants.IGNORESERVERONLY, (in, net) -> true));

        MinecraftForge.EVENT_BUS.addListener(WorldgenTickTests::onWorldTick);
    }
}
