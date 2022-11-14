package thut.crafts.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import thut.api.entity.blockentity.IBlockEntity;
import thut.core.init.CommonInit.ICustomStickHandler;
import thut.crafts.ThutCrafts;
import thut.lib.TComponent;

public class CraftStickApplier implements ICustomStickHandler
{

    @Override
    public boolean isItem(ServerPlayer player, ItemStack held)
    {
        return held.getItem() == ThutCrafts.CRAFTMAKER.get();
    }

    @Override
    public void apply(ServerPlayer player, ServerLevel level, ItemStack held, BlockPos min, BlockPos max)
    {
        final AABB box = new AABB(min, max);
        min = new BlockPos(box.minX, box.minY, box.minZ);
        max = new BlockPos(box.maxX, box.maxY, box.maxZ);
        final BlockPos mid = min;
        min = min.subtract(mid);
        max = max.subtract(mid);
        final EntityCraft craft = IBlockEntity.BlockEntityFormer.makeBlockEntity(level, min, max, mid,
                ThutCrafts.CRAFTTYPE.get());
        final String message = craft != null ? "msg.craft.create" : "msg.craft.fail";
        thut.lib.ChatHelper.sendSystemMessage(player, TComponent.translatable(message));
    }

    @Override
    public boolean checkValid(ServerPlayer player, Level level, ItemStack held, BlockPos min, BlockPos max)
    {
        final AABB box = new AABB(min, max);
        min = new BlockPos(box.minX, box.minY, box.minZ);
        max = new BlockPos(box.maxX, box.maxY, box.maxZ);
        final BlockPos mid = min;
        min = min.subtract(mid);
        max = max.subtract(mid);
        final int dw = Math.max(max.getX() - min.getX(), max.getZ() - min.getZ());
        if (max.getY() - min.getY() > 30 || dw > 2 * 20 + 1)
        {
            final String message = "msg.craft.toobig";
            thut.lib.ChatHelper.sendSystemMessage(player, TComponent.translatable(message));
            return false;
        }
        return true;
    }

    @Override
    public String getCornerMessage()
    {
        return "msg.craft.setcorner";
    }
}
