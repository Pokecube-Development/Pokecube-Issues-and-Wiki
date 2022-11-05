package thut.core.init;

import java.util.Locale;
import java.util.stream.Stream;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import thut.api.TickHandler;
import thut.api.Tracker;
import thut.api.entity.blockentity.BlockEntityBase;
import thut.api.entity.blockentity.IBlockEntity;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeType;
import thut.api.terrain.StructureManager;
import thut.api.terrain.TerrainManager;
import thut.api.util.PermNodes;
import thut.api.util.PermNodes.DefaultPermissionLevel;
import thut.core.common.ThutCore;
import thut.core.common.ThutCore.MobEvents;
import thut.core.common.network.EntityUpdate;
import thut.core.common.world.mobs.data.SyncHandler;
import thut.crafts.ThutCrafts;
import thut.crafts.entity.EntityCraft;
import thut.lib.TComponent;

@Mod.EventBusSubscriber(bus = Bus.FORGE)
public class CommonInit
{
    public static final String SET_SUBBIOME = "thutcore.subbiome.set";

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = ThutCore.MODID)
    public static class RegistryEvents
    {
        @SubscribeEvent
        public static void setup(final FMLCommonSetupEvent event)
        {
            // Setup terrain manager
            TerrainManager.getInstance();

            MinecraftForge.EVENT_BUS.register(StructureManager.class);
            MinecraftForge.EVENT_BUS.register(TickHandler.class);
            MinecraftForge.EVENT_BUS.register(MobEvents.class);
            MinecraftForge.EVENT_BUS.register(SyncHandler.class);

            TerrainManager.init();

            PermNodes.registerBooleanNode(CommonInit.SET_SUBBIOME, DefaultPermissionLevel.OP,
                    "Able to set subbiomes via items");
        }
    }

    static BiomeType getSubbiome(final ServerPlayer player, final ItemStack held)
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

    protected static boolean isSubbiomeEditor(final ServerPlayer player, final ItemStack held)
    {
        return CommonInit.getSubbiome(player, held) != null;
    }

    private static void trySubbiomeEditor(final PlayerInteractEvent.RightClickBlock evt)
    {
        if (evt.getHand() == InteractionHand.OFF_HAND || !(evt.getPlayer() instanceof ServerPlayer player)
                || evt.getItemStack().isEmpty() || !evt.getPlayer().isShiftKeyDown()
                || !CommonInit.isSubbiomeEditor(player, evt.getItemStack()))
            return;
        final ItemStack itemstack = evt.getItemStack();
        final Player playerIn = evt.getPlayer();
        final Level worldIn = evt.getWorld();
        final BlockPos pos = evt.getPos();
        if (itemstack.hasTag() && playerIn.isShiftKeyDown() && itemstack.getTag().contains("min"))
        {
            final CompoundTag minTag = itemstack.getTag().getCompound("min");
            final BlockPos min = pos;
            final BlockPos max = Vector3.readFromNBT(minTag, "").getPos();
            if (!worldIn.isClientSide)
            {
                final BiomeType subbiome = CommonInit.getSubbiome(player, itemstack);
                final BoundingBox box = BoundingBox.fromCorners(min, max);
                final Stream<BlockPos> poses = BlockPos.betweenClosedStream(box.minX, box.minY, box.minZ, box.maxX,
                        box.maxY, box.maxZ);
                poses.forEach((p) -> {
                    TerrainManager.getInstance().getTerrain(worldIn, p).setBiome(p, subbiome);
                });
                final String message = "msg.subbiome.set";
                thut.lib.ChatHelper.sendSystemMessage(playerIn, TComponent.translatable(message, subbiome.name));
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
            final String message = "msg.subbiome.setcorner";
            if (!worldIn.isClientSide)
                thut.lib.ChatHelper.sendSystemMessage(playerIn, TComponent.translatable(message, pos));
            evt.setCanceled(true);
            itemstack.getTag().putLong("time", Tracker.instance().getTick());
        }
    }

    private static void trySubbiomeEditor(final PlayerInteractEvent.RightClickItem evt)
    {
        if (evt.getHand() == InteractionHand.OFF_HAND || !(evt.getPlayer() instanceof ServerPlayer player)
                || evt.getItemStack().isEmpty() || !evt.getPlayer().isShiftKeyDown()
                || !CommonInit.isSubbiomeEditor(player, evt.getItemStack()))
            return;
        final ItemStack itemstack = evt.getItemStack();
        final Player playerIn = evt.getPlayer();
        final Level worldIn = evt.getWorld();
        final long now = Tracker.instance().getTick();
        if (itemstack.hasTag() && playerIn.isShiftKeyDown() && itemstack.getTag().contains("min")
                && itemstack.getTag().getLong("time") != now)
        {
            final CompoundTag minTag = itemstack.getTag().getCompound("min");
            final Vec3 loc = playerIn.position().add(0, playerIn.getEyeHeight(), 0)
                    .add(playerIn.getLookAngle().scale(2));
            final BlockPos pos = new BlockPos(loc);
            final BlockPos min = pos;
            final BlockPos max = Vector3.readFromNBT(minTag, "").getPos();
            if (!worldIn.isClientSide)
            {
                final BiomeType subbiome = CommonInit.getSubbiome(player, itemstack);
                final BoundingBox box = BoundingBox.fromCorners(min, max);
                final Stream<BlockPos> poses = BlockPos.betweenClosedStream(box.minX, box.minY, box.minZ, box.maxX,
                        box.maxY, box.maxZ);
                poses.forEach((p) -> {
                    TerrainManager.getInstance().getTerrain(worldIn, p).setBiome(p, subbiome);
                });
                final String message = "msg.subbiome.set";
                thut.lib.ChatHelper.sendSystemMessage(playerIn, TComponent.translatable(message, subbiome.name));
            }
            itemstack.getTag().remove("min");
        }
    }

    private static void tryCraftMaker(final PlayerInteractEvent.RightClickBlock evt)
    {
        if (evt.getHand() == InteractionHand.OFF_HAND || evt.getWorld().isClientSide || evt.getItemStack().isEmpty()
                || !evt.getPlayer().isShiftKeyDown() || evt.getItemStack().getItem() != ThutCrafts.CRAFTMAKER.get())
            return;
        final ItemStack itemstack = evt.getItemStack();
        final Player playerIn = evt.getPlayer();
        final Level worldIn = evt.getWorld();
        final BlockPos pos = evt.getPos();
        if (itemstack.hasTag() && playerIn.isShiftKeyDown() && itemstack.getTag().contains("min"))
        {
            final CompoundTag minTag = itemstack.getTag().getCompound("min");
            BlockPos min = pos;
            BlockPos max = Vector3.readFromNBT(minTag, "").getPos();
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
                if (!worldIn.isClientSide)
                    thut.lib.ChatHelper.sendSystemMessage(playerIn, TComponent.translatable(message));
                return;
            }
            if (!worldIn.isClientSide)
            {
                final EntityCraft craft = IBlockEntity.BlockEntityFormer.makeBlockEntity(evt.getWorld(), min, max, mid,
                        ThutCrafts.CRAFTTYPE.get());
                final String message = craft != null ? "msg.craft.create" : "msg.craft.fail";
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
            final String message = "msg.craft.setcorner";
            if (!worldIn.isClientSide)
                thut.lib.ChatHelper.sendSystemMessage(playerIn, TComponent.translatable(message, pos));
            evt.setCanceled(true);
            itemstack.getTag().putLong("time", Tracker.instance().getTick());
        }
    }

    private static void tryCraftMaker(final PlayerInteractEvent.RightClickItem evt)
    {
        if (evt.getHand() == InteractionHand.OFF_HAND || evt.getWorld().isClientSide || evt.getItemStack().isEmpty()
                || !evt.getPlayer().isShiftKeyDown() || evt.getItemStack().getItem() != ThutCrafts.CRAFTMAKER.get())
            return;
        final ItemStack itemstack = evt.getItemStack();
        final Player playerIn = evt.getPlayer();
        final Level worldIn = evt.getWorld();
        final long now = Tracker.instance().getTick();
        if (itemstack.hasTag() && playerIn.isShiftKeyDown() && itemstack.getTag().contains("min")
                && itemstack.getTag().getLong("time") != now)
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
            if (max.getY() - min.getY() > 30 || dw > 2 * 20 + 1)
            {
                final String message = "msg.craft.toobig";
                if (!worldIn.isClientSide)
                    thut.lib.ChatHelper.sendSystemMessage(playerIn, TComponent.translatable(message));
                return;
            }
            if (!worldIn.isClientSide)
            {
                final EntityCraft craft = IBlockEntity.BlockEntityFormer.makeBlockEntity(evt.getWorld(), min, max, mid,
                        ThutCrafts.CRAFTTYPE.get());
                final String message = craft != null ? "msg.craft.create" : "msg.craft.fail";
                thut.lib.ChatHelper.sendSystemMessage(playerIn, TComponent.translatable(message));
            }
            itemstack.getTag().remove("min");
        }
    }

    @SubscribeEvent
    public static void interactRightClickBlock(final PlayerInteractEvent.RightClickBlock evt)
    {
        trySubbiomeEditor(evt);
        tryCraftMaker(evt);
    }

    @SubscribeEvent
    public static void interactRightClickBlock(final PlayerInteractEvent.RightClickItem evt)
    {
        trySubbiomeEditor(evt);
        tryCraftMaker(evt);
    }

    @SubscribeEvent
    public static void logout(final PlayerLoggedOutEvent event)
    {
        if (event.getPlayer().isPassenger() && event.getPlayer().getRootVehicle() instanceof EntityCraft)
            event.getPlayer().stopRiding();
    }

    @SubscribeEvent
    /**
     * Sends update packet to the mob.
     *
     * @param evt
     */
    public static void startTracking(final StartTracking evt)
    {
        if (evt.getTarget() instanceof IEntityAdditionalSpawnData && evt.getTarget() instanceof BlockEntityBase)
            EntityUpdate.sendEntityUpdate(evt.getTarget());
    }
}
