package thut.api.level.terrain;

import java.util.Locale;
import java.util.stream.Stream;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import thut.api.util.PermNodes;
import thut.core.init.CommonInit;
import thut.core.init.CommonInit.ICustomStickHandler;
import thut.lib.TComponent;

public class SubbiomeStickApplier implements ICustomStickHandler
{
    private BiomeType getSubbiome(final ServerPlayer player, final ItemStack held)
    {
        if (!PermNodes.getBooleanPerm(player, CommonInit.SET_SUBBIOME)) return null;
        if (held.getHoverName().getString().toLowerCase(Locale.ROOT).startsWith("subbiome->"))
        {
            final String[] args = held.getHoverName().getString().split("->");
            if (args.length != 2) return null;
            return BiomeType.getBiome(args[1].trim());
        }
        return null;
    }

    @Override
    public boolean isItem(ServerPlayer player, ItemStack held)
    {
        return getSubbiome(player, held) != null;
    }

    @Override
    public void apply(ServerPlayer player, ServerLevel level, ItemStack held, BlockPos min, BlockPos max)
    {
        final BiomeType subbiome = this.getSubbiome(player, held);
        final BoundingBox box = BoundingBox.fromCorners(min, max);
        final Stream<BlockPos> poses = BlockPos.betweenClosedStream(box.minX, box.minY, box.minZ, box.maxX, box.maxY,
                box.maxZ);
        poses.forEach((p) -> {
            TerrainManager.getInstance().getTerrain(level, p).setBiome(p, subbiome);
        });
        final String message = "msg.subbiome.set";
        thut.lib.ChatHelper.sendSystemMessage(player, TComponent.translatable(message, subbiome.name));
    }

    @Override
    public String getCornerMessage()
    {
        return "msg.subbiome.setcorner";
    }
}
