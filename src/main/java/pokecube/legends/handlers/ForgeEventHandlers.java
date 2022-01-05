package pokecube.legends.handlers;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.ISpecialCaptureCondition;
import pokecube.core.database.stats.SpecialCaseRegister;
import pokecube.core.events.MeteorEvent;
import pokecube.core.handlers.events.EventsHandler;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.conditions.AbstractCondition;
import thut.api.item.ItemList;
import thut.api.maths.Vector3;
import thut.api.terrain.StructureManager;
import thut.api.terrain.StructureManager.StructureInfo;

public class ForgeEventHandlers
{
    private static final ResourceLocation ZMOVECAP = new ResourceLocation("pokecube_legends:zmove_check");

    private static final ResourceLocation WHILTELISTED = new ResourceLocation("pokecube_legends:arceus_approved");

    public static Supplier<BlockState> DUST = () -> Blocks.AIR.defaultBlockState();
    public static Supplier<BlockState> MOLTEN = () -> Blocks.AIR.defaultBlockState();

    private boolean protectTemple(@Nullable final ServerPlayer player, @Nonnull final ServerLevel world,
            @Nullable final BlockState newState, final BlockPos pos)
    {
        final BlockState state = world.getBlockState(pos);
        if (ItemList.is(ForgeEventHandlers.WHILTELISTED, state)) return false;
        if (newState != null && ItemList.is(ForgeEventHandlers.WHILTELISTED, newState)) return false;
        if (player != null && player.isCreative()) return false;
        final Set<StructureInfo> set = StructureManager.getFor(world.dimension(), pos);
        for (final StructureInfo info : set)
        {
            String name = info.name;
            if (!name.contains(":")) name = "minecraft:" + name;
            if (PokecubeLegends.config.PROTECTED_STRUCTURES.contains(name))
            {
                if (player == null) return true;
                // Now we do some specifc checks for the player, to see if we
                // might actually allow breaking here.
                final List<PokedexEntry> valid = PokecubeLegends.config.STRUCTURE_ENTRIES.get(name);
                if (valid == null) return true;

                boolean canEdit = false;
                for (final PokedexEntry entry : valid)
                {
                    final ISpecialCaptureCondition capt = SpecialCaseRegister.getCaptureCondition(entry);
                    if (!(capt instanceof AbstractCondition)) continue;
                    final AbstractCondition condition = (AbstractCondition) capt;
                    if (condition.canCapture(player, false) && condition.isRelevant(state))
                    {
                        canEdit = true;
                        break;
                    }
                }
                return !canEdit;
            }
        }
        return false;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void capabilityEntities(final AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getCapabilities().containsKey(EventsHandler.POKEMOBCAP))
            event.addCapability(ForgeEventHandlers.ZMOVECAP, new ZPowerHandler());
    }

    @SubscribeEvent
    public void detonate(final ExplosionEvent.Detonate evt)
    {
        if (!(evt.getWorld() instanceof ServerLevel level) || !PokecubeLegends.config.protectTemples) return;
        final List<BlockPos> toRemove = Lists.newArrayList();

        ServerPlayer player = evt.getExplosion().getSourceMob() instanceof ServerPlayer
                ? (ServerPlayer) evt.getExplosion().getSourceMob()
                : null;

        for (final BlockPos pos : evt.getAffectedBlocks())
            if (this.protectTemple(player, level, null, pos)) toRemove.add(pos);

        evt.getAffectedBlocks().removeAll(toRemove);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void placeBlocks(final EntityPlaceEvent evt)
    {
        if (!(evt.getEntity() instanceof ServerPlayer player) || !PokecubeLegends.config.protectTemples) return;
        final ServerLevel world = (ServerLevel) player.getLevel();
        if (this.protectTemple(player, world, evt.getPlacedBlock(), evt.getPos()))
        {
            evt.setCanceled(true);
            player.displayClientMessage(new TranslatableComponent("msg.cannot_defile_temple"), true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void BreakBlock(final BreakEvent evt)
    {
        if (!(evt.getPlayer() instanceof ServerPlayer player) || !PokecubeLegends.config.protectTemples) return;

        final ServerLevel world = (ServerLevel) player.getLevel();
        if (this.protectTemple(player, world, null, evt.getPos()))
        {
            evt.setCanceled(true);
            player.displayClientMessage(new TranslatableComponent("msg.cannot_defile_temple"), true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void bucket(final FillBucketEvent evt)
    {
        if (!(evt.getPlayer() instanceof ServerPlayer player) || !PokecubeLegends.config.protectTemples) return;
        final ServerLevel world = (ServerLevel) player.getLevel();
        BlockPos pos = player.blockPosition();
        if (evt.getTarget() instanceof BlockHitResult && evt.getTarget().getType() != Type.MISS)
        {
            final BlockHitResult trace = (BlockHitResult) evt.getTarget();
            pos = trace.getBlockPos().relative(trace.getDirection());
        }
        if (this.protectTemple(player, world, null, pos))
        {
            evt.setCanceled(true);
            player.displayClientMessage(new TranslatableComponent("msg.cannot_defile_temple"), true);
        }
    }

    @SubscribeEvent
    public void MeteorDestructionEvent(final MeteorEvent event)
    {
        final ServerLevel level = event.getBoom().level;

        if (event.getPower() > PokecubeLegends.config.meteorPowerThreshold / 50
                && level.getRandom().nextDouble() < PokecubeLegends.config.meteorChanceForAny)
        {
            BlockPos pos = event.getPos();
            BlockState set = Blocks.AIR.defaultBlockState();

            if (event.getPower() > PokecubeLegends.config.meteorPowerThreshold)
            {
                set = MOLTEN.get();
            }
            else
            {
                boolean noDust;
                Vec3 diff = event.getBoom().getPosition().subtract(pos.getX(), pos.getY(), pos.getZ()).normalize();
                double dot = diff.dot(new Vec3(0, 1, 0));
                noDust = dot > 0 || dot < -0.95;
                if (noDust) return;
                set = DUST.get();
            }

            if (!set.isAir())
            {
                int n = 20;
                Vector3 hit = Vector3.getNextSurfacePoint(level, Vector3.getNewVector().set(pos), Vector3.secondAxis,
                        20);
                while (hit != null && n-- > 0)
                {
                    hit = Vector3.getNextSurfacePoint(level, Vector3.getNewVector().set(pos), Vector3.secondAxis, 20);
                }
                pos = pos.above(n);
                level.setBlock(pos, set, 3);
                level.scheduleTick(pos, set.getBlock(), 2);
            }
        }
    }
}
