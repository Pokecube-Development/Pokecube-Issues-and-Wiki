package pokecube.legends.worldgen;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.server.ServerWorld;
import pokecube.legends.init.FeaturesInit;
import thut.api.entity.ThutTeleporter;
import thut.api.entity.ThutTeleporter.TeleDest;
import thut.api.maths.Vector3;

public class DimensionTranserHelper
{

    public static void sentToDistorted(final ServerPlayerEntity player)
    {
        final RegistryKey<World> targetDim = FeaturesInit.DISTORTEDWORLD_KEY;
        final BlockPos pos = DimensionTranserHelper.getTransferPoint(player, player.getServer(), targetDim);
        final Vector3 v = Vector3.getNewVector().set(pos).addTo(0.5, 0, 0.5);
        ThutTeleporter.transferTo(player, new TeleDest().setLoc(GlobalPos.getPosition(targetDim, pos), v), true);
    }

    public static void sentToUltraspace(final ServerPlayerEntity player)
    {
        final RegistryKey<World> targetDim = FeaturesInit.ULTRASPACE_KEY;
        final BlockPos pos = DimensionTranserHelper.getTransferPoint(player, player.getServer(), targetDim);
        final Vector3 v = Vector3.getNewVector().set(pos).addTo(0.5, 0, 0.5);
        ThutTeleporter.transferTo(player, new TeleDest().setLoc(GlobalPos.getPosition(targetDim, pos), v), true);
    }

    public static void sendToOverworld(final ServerPlayerEntity player)
    {
        final RegistryKey<World> targetDim = World.OVERWORLD;
        final BlockPos pos = DimensionTranserHelper.getTransferPoint(player, player.getServer(), targetDim);
        final Vector3 v = Vector3.getNewVector().set(pos).addTo(0.5, 0, 0.5);
        ThutTeleporter.transferTo(player, new TeleDest().setLoc(GlobalPos.getPosition(targetDim, pos), v), true);
    }

    public static BlockPos getTransferPoint(final ServerPlayerEntity player, final MinecraftServer server,
            final RegistryKey<World> targetDim)
    {
        final ServerWorld world = server.getWorld(targetDim);
        // Load the chunk before checking height.
        world.getChunk(player.getPosition());
        // Find height
        BlockPos top = world.getHeight(Type.MOTION_BLOCKING, player.getPosition());

        // We need to make a platform here!
        if (top.getY() <= 2)
        {
            top = player.getPosition();
            for (int i = -2; i <= 2; i++)
                for (int j = -2; j <= 2; j++)
                {
                    final BlockPos pos = new BlockPos(top.getX() + i, player.getPosY() - 1, top.getZ() + j);
                    world.setBlockState(pos, Blocks.OBSIDIAN.getDefaultState());
                }
        }
        return top;
    }
}
