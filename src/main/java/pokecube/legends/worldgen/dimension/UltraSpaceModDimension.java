package pokecube.legends.worldgen.dimension;

/*import java.util.function.BiFunction;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import pokecube.legends.init.DimensionInit;
import thut.api.entity.ThutTeleporter;
import thut.api.entity.ThutTeleporter.TeleDest;
import thut.api.maths.Vector3;

public class UltraSpaceModDimension extends Dimensions
{

    @Override
    public BiFunction<World, DimensionType, ? extends Dimension> getFactory()
    {
        return UltraSpaceConfig.UltraSpaceDimension::new;
    }

    public static void sentToUltraspace(final ServerPlayerEntity player)
    {
        final RegistryKey<World> targetDim = DimensionInit.DIMENSION_TYPE;
        final BlockPos pos = DimensionInit.getTransferPoint(player, player.getServer(), targetDim);
        final Vector3 v = Vector3.getNewVector().set(pos).addTo(0.5, 0, 0.5);
        ThutTeleporter.transferTo(player, new TeleDest().setLoc(GlobalPos.getPosition(targetDim, pos), v), true);
    }

    public static void sendToOverworld(final ServerPlayerEntity player)
    {
        final RegistryKey<World> targetDim = World.OVERWORLD;
        final BlockPos pos = DimensionInit.getTransferPoint(player, player.getServer(), targetDim);
        final Vector3 v = Vector3.getNewVector().set(pos).addTo(0.5, 0, 0.5);
        ThutTeleporter.transferTo(player, new TeleDest().setLoc(GlobalPos.getPosition(targetDim, pos), v), true);
    }
}*/
