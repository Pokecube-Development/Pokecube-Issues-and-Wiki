package pokecube.compat.cct;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import pokecube.adventures.blocks.commander.CommanderTile;
import pokecube.adventures.blocks.genetics.extractor.ExtractorTile;
import pokecube.adventures.blocks.genetics.splicer.SplicerTile;
import pokecube.adventures.blocks.siphon.SiphonTile;
import pokecube.adventures.blocks.warp_pad.WarpPadTile;
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

    public static void register()
    {
        PokecubeCore.LOGGER.info("Registering Pokecube CC Peripherals.");
        ComputerCraftAPI.registerPeripheralProvider(new PokecubePeripherals());
    }

    public static class PokecubePeripherals implements IPeripheralProvider
    {
        public IPeripheral getPeri(final Level world, final BlockPos pos, final Direction side)
        {
            final BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof CommanderTile) return new Commander((CommanderTile) tile);
            if (tile instanceof TMTile) return new TM((TMTile) tile);
            if (tile instanceof SplicerTile) return new Splicer((SplicerTile) tile);
            if (tile instanceof ExtractorTile) return new Extractor((ExtractorTile) tile);
            if (tile instanceof WarpPadTile) return new Warppad((WarpPadTile) tile);
            if (tile instanceof SiphonTile) return new Siphon((SiphonTile) tile);
            return null;
        }

        @Override
        public LazyOptional<IPeripheral> getPeripheral(final Level world, final BlockPos pos, final Direction side)
        {
            final IPeripheral peri = this.getPeri(world, pos, side);
            return peri == null ? LazyOptional.empty() : LazyOptional.of(() -> peri);
        }
    }
}
