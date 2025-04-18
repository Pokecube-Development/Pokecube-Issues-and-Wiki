package thut.test;

import net.neoforged.fml.common.Mod;
import thut.test.scripting.CmdListener;

@Mod(value = "testmod")
public class Tests
{
    public Tests()
    {
        CmdListener.init();
    }
}
