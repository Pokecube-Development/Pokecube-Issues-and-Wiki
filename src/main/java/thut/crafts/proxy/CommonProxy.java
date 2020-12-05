package thut.crafts.proxy;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import thut.api.entity.blockentity.BlockEntityBase;
import thut.api.entity.blockentity.IBlockEntity;
import thut.api.maths.Vector3;
import thut.core.common.Proxy;
import thut.core.common.network.EntityUpdate;
import thut.crafts.ThutCrafts;
import thut.crafts.entity.EntityCraft;

public class CommonProxy implements Proxy
{
    @SubscribeEvent
    public void interactRightClickBlock(final PlayerInteractEvent.RightClickBlock evt)
    {
        if (evt.getHand() == Hand.OFF_HAND || evt.getWorld().isRemote || evt.getItemStack().isEmpty() || !evt
                .getPlayer().isSneaking() || evt.getItemStack().getItem() != ThutCrafts.CRAFTMAKER) return;
        final ItemStack itemstack = evt.getItemStack();
        final PlayerEntity playerIn = evt.getPlayer();
        final World worldIn = evt.getWorld();
        final BlockPos pos = evt.getPos();
        if (itemstack.hasTag() && playerIn.isSneaking() && itemstack.getTag().contains("min"))
        {
            final CompoundNBT minTag = itemstack.getTag().getCompound("min");
            BlockPos min = pos;
            BlockPos max = Vector3.readFromNBT(minTag, "").getPos();
            final AxisAlignedBB box = new AxisAlignedBB(min, max);
            min = new BlockPos(box.minX, box.minY, box.minZ);
            max = new BlockPos(box.maxX, box.maxY, box.maxZ);
            final BlockPos mid = min;
            min = min.subtract(mid);
            max = max.subtract(mid);
            final int dw = Math.max(max.getX() - min.getX(), max.getZ() - min.getZ());
            if (max.getY() - min.getY() > 10 || dw > 2 * 5 + 1)
            {
                final String message = "msg.craft.toobig";
                if (!worldIn.isRemote) playerIn.sendMessage(new TranslationTextComponent(message), Util.DUMMY_UUID);
                return;
            }
            if (!worldIn.isRemote)
            {
                final EntityCraft craft = IBlockEntity.BlockEntityFormer.makeBlockEntity(evt.getWorld(), min, max, mid,
                        EntityCraft.CRAFTTYPE);
                final String message = craft != null ? "msg.craft.create" : "msg.craft.fail";
                playerIn.sendMessage(new TranslationTextComponent(message), Util.DUMMY_UUID);
            }
            itemstack.getTag().remove("min");
            evt.setCanceled(true);
        }
        else
        {
            if (!itemstack.hasTag()) itemstack.setTag(new CompoundNBT());
            final CompoundNBT min = new CompoundNBT();
            Vector3.getNewVector().set(pos).writeToNBT(min, "");
            itemstack.getTag().put("min", min);
            final String message = "msg.craft.setcorner";
            if (!worldIn.isRemote) playerIn.sendMessage(new TranslationTextComponent(message, pos), Util.DUMMY_UUID);
            evt.setCanceled(true);
            itemstack.getTag().putLong("time", worldIn.getGameTime());
        }
    }

    @SubscribeEvent
    public void interactRightClickBlock(final PlayerInteractEvent.RightClickItem evt)
    {
        if (evt.getHand() == Hand.OFF_HAND || evt.getWorld().isRemote || evt.getItemStack().isEmpty() || !evt
                .getPlayer().isSneaking() || evt.getItemStack().getItem() != ThutCrafts.CRAFTMAKER) return;
        final ItemStack itemstack = evt.getItemStack();
        final PlayerEntity playerIn = evt.getPlayer();
        final World worldIn = evt.getWorld();
        if (itemstack.hasTag() && playerIn.isSneaking() && itemstack.getTag().contains("min") && itemstack.getTag()
                .getLong("time") != worldIn.getGameTime())
        {
            final CompoundNBT minTag = itemstack.getTag().getCompound("min");
            final Vector3d loc = playerIn.getPositionVec().add(0, playerIn.getEyeHeight(), 0).add(playerIn
                    .getLookVec().scale(2));
            final BlockPos pos = new BlockPos(loc);
            BlockPos min = pos;
            BlockPos max = Vector3.readFromNBT(minTag, "").getPos();
            final AxisAlignedBB box = new AxisAlignedBB(min, max);
            min = new BlockPos(box.minX, box.minY, box.minZ);
            max = new BlockPos(box.maxX, box.maxY, box.maxZ);
            final BlockPos mid = min;
            min = min.subtract(mid);
            max = max.subtract(mid);
            final int dw = Math.max(max.getX() - min.getX(), max.getZ() - min.getZ());
            if (max.getY() - min.getY() > 15 || dw > 2 * 10 + 1)
            {
                final String message = "msg.craft.toobig";
                if (!worldIn.isRemote) playerIn.sendMessage(new TranslationTextComponent(message), Util.DUMMY_UUID);
                return;
            }
            if (!worldIn.isRemote)
            {
                final EntityCraft craft = IBlockEntity.BlockEntityFormer.makeBlockEntity(evt.getWorld(), min, max, mid,
                        EntityCraft.CRAFTTYPE);
                final String message = craft != null ? "msg.craft.create" : "msg.craft.fail";
                playerIn.sendMessage(new TranslationTextComponent(message), Util.DUMMY_UUID);
            }
            itemstack.getTag().remove("min");
        }
    }

    @SubscribeEvent
    public void logout(final PlayerLoggedOutEvent event)
    {
        if (event.getPlayer().isPassenger() && event.getPlayer().getLowestRidingEntity() instanceof EntityCraft) event
                .getPlayer().stopRiding();
    }

    @Override
    public void setup(final FMLCommonSetupEvent event)
    {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(ThutCrafts.class);
    }

    @SubscribeEvent
    /**
     * Sends update packet to the mob.
     *
     * @param evt
     */
    public void startTracking(final StartTracking evt)
    {
        if (evt.getTarget() instanceof IEntityAdditionalSpawnData && evt.getEntity() instanceof BlockEntityBase)
            EntityUpdate.sendEntityUpdate(evt.getEntity());
    }
}
