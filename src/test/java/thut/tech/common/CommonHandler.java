package thut.tech.common;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import thut.api.entity.blockentity.IBlockEntity;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;
import thut.lib.TComponent;
import thut.tech.Reference;
import thut.tech.common.entity.EntityLift;
import thut.tech.common.network.PacketLift;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Reference.MOD_ID)
public class CommonHandler
{
    public static class InteractionHelper
    {

        @SubscribeEvent
        public static void interactRightClickBlock(final PlayerInteractEvent.RightClickBlock evt)
        {
            if (evt.getHand() == InteractionHand.OFF_HAND || evt.getLevel().isClientSide || evt.getItemStack().isEmpty()
                    || evt.getItemStack().getItem() != TechCore.LIFT.get())
                return;

            final ItemStack itemstack = evt.getItemStack();
            final Player playerIn = evt.getEntity();
            final Level worldIn = evt.getLevel();
            if (!evt.getEntity().isShiftKeyDown())
            {
                if (itemstack.hasTag())
                {
                    itemstack.getTag().remove("min");
                    itemstack.getTag().remove("time");
                    if (itemstack.getTag().isEmpty()) itemstack.setTag(null);
                    final String message = "msg.lift.reset";
                    if (!worldIn.isClientSide)
                        thut.lib.ChatHelper.sendSystemMessage(playerIn, TComponent.translatable(message));
                    evt.setCanceled(true);
                }
                return;
            }

            final BlockPos pos = evt.getPos();
            if (itemstack.hasTag() && playerIn.isShiftKeyDown() && itemstack.getTag().contains("min"))
            {
                final CompoundTag minTag = itemstack.getTag().getCompound("min");
                itemstack.getTag().putLong("time", worldIn.getGameTime());
                BlockPos min = pos;
                BlockPos max = Vector3.readFromNBT(minTag, "").getPos();
                final AABB box = new AABB(min, max);
                min = new BlockPos(box.minX, box.minY, box.minZ);
                max = new BlockPos(box.maxX, box.maxY, box.maxZ);
                final BlockPos mid = min;
                min = min.subtract(mid);
                max = max.subtract(mid);
                final int dw = Math.max(max.getX() - min.getX(), max.getZ() - min.getZ());
                if (max.getY() - min.getY() > TechCore.config.maxHeight || dw > 2 * TechCore.config.maxRadius + 1)
                {
                    final String message = "msg.lift.toobig";
                    if (!worldIn.isClientSide)
                        thut.lib.ChatHelper.sendSystemMessage(playerIn, TComponent.translatable(message));
                    return;
                }
                final int num = (dw + 1) * (max.getY() - min.getY() + 1);
                int count = 0;
                for (final ItemStack item : playerIn.getInventory().items)
                    if (item.getItem() == TechCore.LIFT.get()) count += item.getCount();
                if (!playerIn.getAbilities().instabuild && count < num)
                {
                    final String message = "msg.lift.noblock";
                    if (!worldIn.isClientSide)
                        thut.lib.ChatHelper.sendSystemMessage(playerIn, TComponent.translatable(message, num));
                    return;
                }
                else if (!playerIn.getAbilities().instabuild)
                    playerIn.getInventory().clearOrCountMatchingItems(b -> b.getItem() == TechCore.LIFT.get(), num,
                            playerIn.inventoryMenu.getCraftSlots());
                if (!worldIn.isClientSide)
                {
                    final EntityLift lift = IBlockEntity.BlockEntityFormer.makeBlockEntity(worldIn, min, max, mid,
                            TechCore.LIFTTYPE.get());
                    if (lift != null) lift.owner = playerIn.getUUID();
                    final String message = lift != null ? "msg.lift.create" : "msg.lift.fail";
                    thut.lib.ChatHelper.sendSystemMessage(playerIn, TComponent.translatable(message));
                }
                itemstack.getTag().remove("min");
                evt.setCanceled(true);
            }
            else
            {
                if (!itemstack.hasTag()) itemstack.setTag(new CompoundTag());
                final CompoundTag min = new CompoundTag();
                new Vector3().set(pos).writeToNBT(min, "");
                itemstack.getTag().put("min", min);
                final String message = "msg.lift.setcorner";
                if (!worldIn.isClientSide)
                    thut.lib.ChatHelper.sendSystemMessage(playerIn, TComponent.translatable(message, pos));
                evt.setCanceled(true);
                itemstack.getTag().putLong("time", worldIn.getGameTime());
            }
        }

        @SubscribeEvent
        public static void interactRightClickBlock(final PlayerInteractEvent.RightClickItem evt)
        {
            if (evt.getHand() == InteractionHand.OFF_HAND || evt.getLevel().isClientSide || evt.getItemStack().isEmpty()
                    || evt.getItemStack().getItem() != TechCore.LIFT.get())
                return;
            final ItemStack itemstack = evt.getItemStack();
            final Player playerIn = evt.getEntity();
            final Level worldIn = evt.getLevel();

            if (!evt.getEntity().isShiftKeyDown())
            {
                if (itemstack.hasTag())
                {
                    itemstack.getTag().remove("min");
                    itemstack.getTag().remove("time");
                    if (itemstack.getTag().isEmpty()) itemstack.setTag(null);
                }
                final String message = "msg.lift.reset";
                if (!worldIn.isClientSide)
                    thut.lib.ChatHelper.sendSystemMessage(playerIn, TComponent.translatable(message));
                return;
            }

            final boolean validTag = itemstack.hasTag() && itemstack.getTag().contains("min");
            final boolean alreadyUsed = validTag && itemstack.getTag().getLong("time") - worldIn.getGameTime() == 0;
            if (validTag && !alreadyUsed)
            {
                final CompoundTag minTag = itemstack.getTag().getCompound("min");
                final Vec3 loc = playerIn.position().add(0, playerIn.getEyeHeight(), 0)
                        .add(playerIn.getLookAngle().scale(2));
                final BlockPos pos = new BlockPos(loc);
                BlockPos min = pos;
                BlockPos max = Vector3.readFromNBT(minTag, "").getPos();
                final AABB box = new AABB(min, max);
                min = new BlockPos(box.minX, box.minY, box.minZ);
                max = new BlockPos(box.maxX, box.maxY, box.maxZ);
                final BlockPos mid = min;
                min = min.subtract(mid);
                max = max.subtract(mid);
                final int dw = Math.max(max.getX() - min.getX(), max.getZ() - min.getZ());
                if (max.getY() - min.getY() > TechCore.config.maxHeight || dw > 2 * TechCore.config.maxRadius + 1)
                {
                    final String message = "msg.lift.toobig";
                    if (!worldIn.isClientSide)
                        thut.lib.ChatHelper.sendSystemMessage(playerIn, TComponent.translatable(message));
                    return;
                }
                final int num = (dw + 1) * (max.getY() - min.getY() + 1);
                int count = 0;
                for (final ItemStack item : playerIn.getInventory().items)
                    if (item.getItem() == TechCore.LIFT.get()) count += item.getCount();
                if (!playerIn.getAbilities().instabuild && count < num)
                {
                    final String message = "msg.lift.noblock";
                    if (!worldIn.isClientSide)
                        thut.lib.ChatHelper.sendSystemMessage(playerIn, TComponent.translatable(message, num));
                    return;
                }
                else if (!playerIn.getAbilities().instabuild)
                    playerIn.getInventory().clearOrCountMatchingItems(i -> i.getItem() == TechCore.LIFT.get(), num,
                            playerIn.inventoryMenu.getCraftSlots());
                if (!worldIn.isClientSide)
                {
                    final EntityLift lift = IBlockEntity.BlockEntityFormer.makeBlockEntity(worldIn, min, max, mid,
                            TechCore.LIFTTYPE.get());
                    if (lift != null) lift.owner = playerIn.getUUID();
                    final String message = lift != null ? "msg.lift.create" : "msg.lift.fail";
                    thut.lib.ChatHelper.sendSystemMessage(playerIn, TComponent.translatable(message));
                }
                itemstack.getTag().remove("min");
            }
        }
    }

    @SubscribeEvent
    public static void setup(final FMLCommonSetupEvent event)
    {
        TechCore.packets.registerMessage(PacketLift.class, PacketLift::new);
        MinecraftForge.EVENT_BUS.register(InteractionHelper.class);
        ThutCore.THUTICON = new ItemStack(TechCore.LINKER.get());
    }
}
