package pokecube.legends.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import pokecube.legends.init.FeaturesInit;
import thut.api.entity.ThutTeleporter;
import thut.api.entity.ThutTeleporter.TeleDest;
import thut.api.maths.Vector3;

public class DimensionTranserHelper
{

    public static void sentToDistorted(final ServerPlayer player)
    {
        final ResourceKey<Level> targetDim = FeaturesInit.DISTORTEDWORLD_KEY;
        final BlockPos pos = DimensionTranserHelper.getTransferPoint(player, player.getServer(), targetDim);
        final Vector3 v = new Vector3().set(pos).addTo(0.5, 0, 0.5);
        ThutTeleporter.transferTo(player, new TeleDest().setLoc(GlobalPos.of(targetDim, pos), v), true);
    }

    public static void sentToUltraspace(final ServerPlayer player)
    {
        final ResourceKey<Level> targetDim = FeaturesInit.ULTRASPACE_KEY;
        final BlockPos pos = DimensionTranserHelper.getTransferPoint(player, player.getServer(), targetDim);
        final Vector3 v = new Vector3().set(pos).addTo(0.5, 0, 0.5);
        ThutTeleporter.transferTo(player, new TeleDest().setLoc(GlobalPos.of(targetDim, pos), v), true);
    }

    public static void sendToOverworld(final ServerPlayer player)
    {
        final ResourceKey<Level> targetDim = Level.OVERWORLD;
        final BlockPos pos = DimensionTranserHelper.getTransferPoint(player, player.getServer(), targetDim);
        final Vector3 v = new Vector3().set(pos).addTo(0.5, 0, 0.5);
        ThutTeleporter.transferTo(player, new TeleDest().setLoc(GlobalPos.of(targetDim, pos), v), true);
    }

    public static BlockPos getTransferPoint(final ServerPlayer player, final MinecraftServer server,
            final ResourceKey<Level> targetDim)
    {
        final ServerLevel world = server.getLevel(targetDim);
        // Load the chunk before checking height.
        world.getChunk(player.blockPosition());
        // Find height
        BlockPos top = world.getHeightmapPos(Types.MOTION_BLOCKING, player.blockPosition());

        // We need to make a platform here!
        if (top.getY() <= 2)
        {
            top = player.blockPosition();
            for (int i = -2; i <= 2; i++)
                for (int j = -2; j <= 2; j++)
                {
                    final BlockPos pos = new BlockPos(top.getX() + i, player.getY() - 1, top.getZ() + j);
                    world.setBlockAndUpdate(pos, Blocks.OBSIDIAN.defaultBlockState());
                }
        }
        return top;
    }
}
