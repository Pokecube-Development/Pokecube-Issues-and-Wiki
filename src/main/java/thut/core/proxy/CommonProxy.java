package thut.core.proxy;

import java.util.Locale;
import java.util.stream.Stream;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import thut.api.LinkableCaps;
import thut.api.TickHandler;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeType;
import thut.api.terrain.StructureManager;
import thut.api.terrain.TerrainManager;
import thut.core.common.Proxy;
import thut.core.common.ThutCore.MobEvents;
import thut.core.common.world.mobs.data.SyncHandler;

public class CommonProxy implements Proxy
{
    public static final String SET_SUBBIOME = "thutcore.subbiome.set";

    @Override
    public void setup(final FMLCommonSetupEvent event)
    {
        Proxy.super.setup(event);

        // Setup terrain manager
        TerrainManager.getInstance();

        MinecraftForge.EVENT_BUS.register(LinkableCaps.class);
        MinecraftForge.EVENT_BUS.register(TerrainManager.class);
        MinecraftForge.EVENT_BUS.register(StructureManager.class);
        MinecraftForge.EVENT_BUS.register(TickHandler.class);
        MinecraftForge.EVENT_BUS.register(MobEvents.class);
        MinecraftForge.EVENT_BUS.register(SyncHandler.class);

        PermissionAPI.registerNode(CommonProxy.SET_SUBBIOME, DefaultPermissionLevel.OP,
                "Able to set subbiomes via items");
    }

    BiomeType getSubbiome(final ServerPlayerEntity player, final ItemStack held)
    {
        if (!PermissionAPI.hasPermission(player, CommonProxy.SET_SUBBIOME)) return null;
        if (held.getDisplayName().getString().toLowerCase(Locale.ROOT).startsWith("subbiome->"))
        {
            final String[] args = held.getDisplayName().getString().split("->");
            if (args.length != 2) return null;
            return BiomeType.getBiome(args[1].trim());
        }
        return null;
    }

    protected boolean isSubbiomeEditor(final ServerPlayerEntity player, final ItemStack held)
    {
        return this.getSubbiome(player, held) != null;
    }

    @SubscribeEvent
    public void interactRightClickBlock(final PlayerInteractEvent.RightClickBlock evt)
    {
        if (evt.getHand() == Hand.OFF_HAND || !(evt.getPlayer() instanceof ServerPlayerEntity) || evt.getItemStack()
                .isEmpty() || !evt.getPlayer().isSneaking() || !this.isSubbiomeEditor((ServerPlayerEntity) evt
                        .getPlayer(), evt.getItemStack())) return;
        final ItemStack itemstack = evt.getItemStack();
        final PlayerEntity playerIn = evt.getPlayer();
        final World worldIn = evt.getWorld();
        final BlockPos pos = evt.getPos();
        if (itemstack.hasTag() && playerIn.isSneaking() && itemstack.getTag().contains("min"))
        {
            final CompoundNBT minTag = itemstack.getTag().getCompound("min");
            final BlockPos min = pos;
            final BlockPos max = Vector3.readFromNBT(minTag, "").getPos();
            if (!worldIn.isRemote)
            {
                final BiomeType subbiome = this.getSubbiome((ServerPlayerEntity) evt.getPlayer(), itemstack);
                final MutableBoundingBox box = new MutableBoundingBox(min, max);
                final Stream<BlockPos> poses = BlockPos.getAllInBox(box.minX, box.minY, box.minZ, box.maxX, box.maxY,
                        box.maxZ);
                poses.forEach((p) ->
                {
                    TerrainManager.getInstance().getTerrain(worldIn, p).setBiome(p, subbiome.getType());
                });
                final String message = "msg.subbiome.set";
                playerIn.sendMessage(new TranslationTextComponent(message));
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
            final String message = "msg.subbiome.setcorner";
            if (!worldIn.isRemote) playerIn.sendMessage(new TranslationTextComponent(message, pos));
            evt.setCanceled(true);
            itemstack.getTag().putLong("time", worldIn.getGameTime());
        }
    }

    @SubscribeEvent
    public void interactRightClickBlock(final PlayerInteractEvent.RightClickItem evt)
    {
        if (evt.getHand() == Hand.OFF_HAND || !(evt.getPlayer() instanceof ServerPlayerEntity) || evt.getItemStack()
                .isEmpty() || !evt.getPlayer().isSneaking() || !this.isSubbiomeEditor((ServerPlayerEntity) evt
                        .getPlayer(), evt.getItemStack())) return;
        final ItemStack itemstack = evt.getItemStack();
        final PlayerEntity playerIn = evt.getPlayer();
        final World worldIn = evt.getWorld();
        if (itemstack.hasTag() && playerIn.isSneaking() && itemstack.getTag().contains("min") && itemstack.getTag()
                .getLong("time") != worldIn.getGameTime())
        {
            final CompoundNBT minTag = itemstack.getTag().getCompound("min");
            final Vec3d loc = playerIn.getPositionVec().add(0, playerIn.getEyeHeight(), 0).add(playerIn.getLookVec()
                    .scale(2));
            final BlockPos pos = new BlockPos(loc);
            final BlockPos min = pos;
            final BlockPos max = Vector3.readFromNBT(minTag, "").getPos();
            if (!worldIn.isRemote)
            {
                final BiomeType subbiome = this.getSubbiome((ServerPlayerEntity) evt.getPlayer(), itemstack);
                final MutableBoundingBox box = new MutableBoundingBox(min, max);
                final Stream<BlockPos> poses = BlockPos.getAllInBox(box.minX, box.minY, box.minZ, box.maxX, box.maxY,
                        box.maxZ);
                poses.forEach((p) ->
                {
                    TerrainManager.getInstance().getTerrain(worldIn, p).setBiome(p, subbiome.getType());
                });
                final String message = "msg.subbiome.set";
                playerIn.sendMessage(new TranslationTextComponent(message));
            }
            itemstack.getTag().remove("min");
        }
    }
}
