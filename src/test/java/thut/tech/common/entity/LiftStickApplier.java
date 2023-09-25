package thut.tech.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import thut.api.entity.blockentity.IBlockEntity;
import thut.core.init.CommonInit.ICustomStickHandler;
import thut.lib.TComponent;
import thut.tech.common.TechCore;

public class LiftStickApplier implements ICustomStickHandler
{

    @Override
    public boolean isItem(ServerPlayer player, ItemStack held)
    {
        return held.getItem() == TechCore.LIFT.get();
    }

    @Override
    public void apply(ServerPlayer player, ServerLevel level, ItemStack held, BlockPos min, BlockPos max)
    {
        final AABB box = new AABB(min, max);
        min = new BlockPos((int) box.minX, (int) box.minY, (int) box.minZ);
        max = new BlockPos((int) box.maxX, (int) box.maxY, (int) box.maxZ);
        final BlockPos mid = min;
        min = min.subtract(mid);
        max = max.subtract(mid);
        final EntityLift lift = IBlockEntity.BlockEntityFormer.makeBlockEntity(level, min, max, mid,
                TechCore.LIFTTYPE.get());
        final String message = lift != null ? "msg.lift.create" : "msg.lift.fail";
        thut.lib.ChatHelper.sendSystemMessage(player, TComponent.translatable(message));
        if (lift != null)
        {
            lift.owner = player.getUUID();
            if (!player.getAbilities().instabuild)
            {
                final int dw = Math.max(max.getX() - min.getX(), max.getZ() - min.getZ());
                final int num = (dw + 1) * (max.getY() - min.getY() + 1);
                player.getInventory().clearOrCountMatchingItems(b -> b.getItem() == TechCore.LIFT.get(), num,
                        player.inventoryMenu.getCraftSlots());
            }
        }
    }

    @Override
    public boolean checkValid(ServerPlayer player, Level level, ItemStack held, BlockPos min, BlockPos max)
    {
        final AABB box = new AABB(min, max);
        min = new BlockPos((int) box.minX, (int) box.minY, (int) box.minZ);
        max = new BlockPos((int) box.maxX, (int) box.maxY, (int) box.maxZ);
        final BlockPos mid = min;
        min = min.subtract(mid);
        max = max.subtract(mid);
        final int dw = Math.max(max.getX() - min.getX(), max.getZ() - min.getZ());
        if (max.getY() - min.getY() > TechCore.config.maxHeight || dw > 2 * TechCore.config.maxRadius + 1)
        {
            final String message = "msg.lift.toobig";
            if (!level.isClientSide) thut.lib.ChatHelper.sendSystemMessage(player, TComponent.translatable(message));
            return false;
        }
        final int num = (dw + 1) * (max.getY() - min.getY() + 1);
        int count = 0;
        for (final ItemStack item : player.getInventory().items)
            if (item.getItem() == TechCore.LIFT.get()) count += item.getCount();
        if (!player.getAbilities().instabuild && count < num)
        {
            final String message = "msg.lift.noblock";
            if (!level.isClientSide)
                thut.lib.ChatHelper.sendSystemMessage(player, TComponent.translatable(message, num));
            return false;
        }
        return true;
    }

    @Override
    public String getCornerMessage()
    {
        return "msg.lift.setcorner";
    }
}
