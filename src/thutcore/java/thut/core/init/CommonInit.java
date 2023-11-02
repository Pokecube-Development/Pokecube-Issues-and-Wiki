package thut.core.init;

import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
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
import thut.api.level.structures.StructureManager;
import thut.api.level.structures.StructureStickApplier;
import thut.api.level.terrain.BiomeType;
import thut.api.level.terrain.SubbiomeStickApplier;
import thut.api.level.terrain.TerrainManager;
import thut.api.maths.Vector3;
import thut.api.util.PermNodes;
import thut.api.util.PermNodes.DefaultPermissionLevel;
import thut.core.common.ThutCore;
import thut.core.common.ThutCore.MobEvents;
import thut.core.common.network.EntityUpdate;
import thut.core.common.world.mobs.data.SyncHandler;
import thut.crafts.entity.EntityCraft;
import thut.lib.TComponent;

@Mod.EventBusSubscriber(bus = Bus.FORGE)
public class CommonInit
{
    public static final String SET_SUBBIOME = "thutcore.subbiome.set";
    public static final String SET_STRUCTURE = "thutcore.structure.edit";

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = ThutCore.MODID)
    public static class RegistryEvents
    {
        @SubscribeEvent
        public static void setup(final FMLCommonSetupEvent event)
        {
            // Setup terrain manager
            TerrainManager.getInstance();

            ThutCore.FORGE_BUS.register(StructureManager.class);
            ThutCore.FORGE_BUS.register(TickHandler.class);
            ThutCore.FORGE_BUS.register(MobEvents.class);
            ThutCore.FORGE_BUS.register(SyncHandler.class);

            TerrainManager.init();

            PermNodes.registerBooleanNode(ThutCore.MODID, CommonInit.SET_SUBBIOME, DefaultPermissionLevel.OP,
                    "Able to set subbiomes via items");
            PermNodes.registerBooleanNode(ThutCore.MODID, CommonInit.SET_STRUCTURE, DefaultPermissionLevel.OP,
                    "Able to set structures via items");
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

    public static interface ICustomStickHandler
    {
        boolean isItem(final ServerPlayer player, final ItemStack held);

        void apply(ServerPlayer player, ServerLevel level, ItemStack held, BlockPos min, BlockPos max);

        default boolean checkValid(ServerPlayer player, Level level, ItemStack held, BlockPos min, BlockPos max)
        {
            return true;
        }

        String getCornerMessage();
    }

    public static List<ICustomStickHandler> HANDLERS = Lists.newArrayList();

    static
    {
        HANDLERS.add(new SubbiomeStickApplier());
        HANDLERS.add(new StructureStickApplier());
    }

    protected static boolean isSubbiomeEditor(final ServerPlayer player, final ItemStack held)
    {
        return CommonInit.getSubbiome(player, held) != null;
    }

    @SubscribeEvent
    public static void interactRightClickBlock(final PlayerInteractEvent.RightClickBlock evt)
    {
        if (evt.getHand() == InteractionHand.OFF_HAND || !(evt.getPlayer() instanceof ServerPlayer player)
                || evt.getItemStack().isEmpty() || !evt.getPlayer().isShiftKeyDown())
            return;
        final ItemStack itemstack = evt.getItemStack();

        ICustomStickHandler handler = null;
        for (var h : HANDLERS)
        {
            if (h.isItem(player, itemstack))
            {
                handler = h;
                break;
            }
        }
        if (handler == null) return;

        final Player playerIn = evt.getPlayer();
        final Level worldIn = evt.getWorld();
        final BlockPos pos = evt.getPos();
        if (itemstack.hasTag() && playerIn.isShiftKeyDown() && itemstack.getTag().contains("min"))
        {
            final CompoundTag minTag = itemstack.getTag().getCompound("min");
            final BlockPos min = pos;
            final BlockPos max = Vector3.readFromNBT(minTag, "").getPos();
            if (!handler.checkValid(player, worldIn, itemstack, min, max)) return;
            if (!worldIn.isClientSide && worldIn instanceof ServerLevel level)
                handler.apply(player, level, itemstack, min, max);
            itemstack.getTag().remove("min");
            evt.setCanceled(true);
        }
        else
        {
            if (!itemstack.hasTag()) itemstack.setTag(new CompoundTag());
            final CompoundTag min = new CompoundTag();
            new Vector3().set(pos).writeToNBT(min, "");
            itemstack.getTag().put("min", min);
            final String message = handler.getCornerMessage();
            if (!worldIn.isClientSide)
                thut.lib.ChatHelper.sendSystemMessage(playerIn, TComponent.translatable(message, pos));
            evt.setCanceled(true);
            itemstack.getTag().putLong("time", Tracker.instance().getTick());
        }
    }

    @SubscribeEvent
    public static void interactRightClickBlock(final PlayerInteractEvent.RightClickItem evt)
    {
        if (evt.getHand() == InteractionHand.OFF_HAND || !(evt.getPlayer() instanceof ServerPlayer player)
                || evt.getItemStack().isEmpty() || !evt.getPlayer().isShiftKeyDown())
            return;
        final ItemStack itemstack = evt.getItemStack();

        ICustomStickHandler handler = null;
        for (var h : HANDLERS)
        {
            if (h.isItem(player, itemstack))
            {
                handler = h;
                break;
            }
        }
        if (handler == null) return;

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
            if (!worldIn.isClientSide && worldIn instanceof ServerLevel level)
                handler.apply(player, level, itemstack, min, max);
            itemstack.getTag().remove("min");
        }
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
