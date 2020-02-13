package pokecube.compat.cct;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pokecube.adventures.blocks.commander.CommanderTile;
import pokecube.adventures.blocks.genetics.extractor.ExtractorTile;
import pokecube.adventures.blocks.genetics.splicer.SplicerTile;
import pokecube.adventures.blocks.siphon.SiphonTile;
import pokecube.adventures.blocks.warppad.WarppadTile;
import pokecube.compat.cct.modules.Commander;
import pokecube.compat.cct.modules.Extractor;
import pokecube.compat.cct.modules.Siphon;
import pokecube.compat.cct.modules.Splicer;
import pokecube.compat.cct.modules.TM;
import pokecube.compat.cct.modules.Warppad;
import pokecube.core.PokecubeCore;
import pokecube.core.blocks.tms.TMTile;

public class Impl
{

    private static boolean reged = false;

    public static void register()
    {
        if (!Impl.reged)
        {
            Impl.reged = true;
            PokecubeCore.LOGGER.info("Registering Pokecube CC Peripherals.");
            ComputerCraftAPI.registerPeripheralProvider(new PokecubePeripherals());
        }
    }

    public static class PokecubePeripherals implements IPeripheralProvider
    {
        @Override
        public IPeripheral getPeripheral(final World world, final BlockPos pos, final Direction side)
        {
            final TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof CommanderTile) return new Commander((CommanderTile) tile);
            if (tile instanceof TMTile) return new TM((TMTile) tile);
            if (tile instanceof SplicerTile) return new Splicer((SplicerTile) tile);
            if (tile instanceof ExtractorTile) return new Extractor((ExtractorTile) tile);
            if (tile instanceof WarppadTile) return new Warppad((WarppadTile) tile);
            if (tile instanceof SiphonTile) return new Siphon((SiphonTile) tile);
            return null;
        }
    }
}
