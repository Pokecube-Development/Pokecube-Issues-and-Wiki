package thut.test;

import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import thut.test.scripting.CmdListener;

@Mod(value = "testmod")
public class Tests
{
    public Tests()
    {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(() -> "testmod", (incoming, isNetwork) -> true));
        CmdListener.init();
    }
}
