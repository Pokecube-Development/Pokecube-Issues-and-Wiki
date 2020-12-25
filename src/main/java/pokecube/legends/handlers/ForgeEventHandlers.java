package pokecube.legends.handlers;

import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.AttachCapabilitiesEvent;
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
import pokecube.legends.init.BlockInit;
import thut.api.item.ItemList;
import thut.api.terrain.StructureManager;
import thut.api.terrain.StructureManager.StructureInfo;

public class ForgeEventHandlers
{
    private static final ResourceLocation ZMOVECAP = new ResourceLocation("pokecube_legends:zmove_check");

    private static final ResourceLocation WHILTELISTED = new ResourceLocation("pokecube_legends:arceus_approved");

    /*
     * @SubscribeEvent
     * public void onDimensionRegistry(final RegisterDimensionsEvent event)
     * {
     * DimensionInit.DIMENSION_TYPE =
     * DimensionManager.registerOrGetDimension(DimensionInit.DIMENSION_ID,
     * DimensionInit.DIMENSION, null, false);
     * if (DimensionInit.DIMENSION_TYPE.getRegistryName() == null)
     * DimensionInit.DIMENSION_TYPE.setRegistryName(
     * DimensionInit.DIMENSION_ID);
     * }
     */

    private boolean protectTemple(@Nullable final ServerPlayerEntity player, @Nonnull final ServerWorld world,
            @Nullable final BlockState newState, final BlockPos pos)
    {
        final BlockState state = world.getBlockState(pos);
        if (ItemList.is(ForgeEventHandlers.WHILTELISTED, state)) return false;
        if (newState != null && ItemList.is(ForgeEventHandlers.WHILTELISTED, newState)) return false;
        final Set<StructureInfo> set = StructureManager.getFor(world.getDimensionKey(), pos);
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
        if (event.getCapabilities().containsKey(EventsHandler.POKEMOBCAP)) event.addCapability(
                ForgeEventHandlers.ZMOVECAP, new ZPowerHandler());
    }

    @SubscribeEvent
    public void detonate(final ExplosionEvent.Detonate evt)
    {
        if (!(evt.getWorld() instanceof ServerWorld) || !PokecubeLegends.config.protectTemples) return;
        final List<BlockPos> toRemove = Lists.newArrayList();
        {
            for (final BlockPos pos : evt.getAffectedBlocks())
                if (this.protectTemple(null, (ServerWorld) evt.getWorld(), null, pos)) toRemove.add(pos);
        }
        evt.getAffectedBlocks().removeAll(toRemove);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void placeBlocks(final EntityPlaceEvent evt)
    {
        if (!(evt.getEntity() instanceof ServerPlayerEntity) || !PokecubeLegends.config.protectTemples) return;

        final ServerPlayerEntity player = (ServerPlayerEntity) evt.getEntity();
        final ServerWorld world = (ServerWorld) player.getEntityWorld();
        if (this.protectTemple(player, world, evt.getPlacedBlock(), evt.getPos()))
        {
            evt.setCanceled(true);
            player.sendMessage(new TranslationTextComponent("msg.cannot_defile_temple"), Util.DUMMY_UUID);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void BreakBlock(final BreakEvent evt)
    {
        if (!(evt.getPlayer() instanceof ServerPlayerEntity) || !PokecubeLegends.config.protectTemples) return;

        final ServerPlayerEntity player = (ServerPlayerEntity) evt.getPlayer();
        final ServerWorld world = (ServerWorld) player.getEntityWorld();
        if (this.protectTemple(player, world, null, evt.getPos()))
        {
            evt.setCanceled(true);
            player.sendMessage(new TranslationTextComponent("msg.cannot_defile_temple"), Util.DUMMY_UUID);
        }
    }

    @SubscribeEvent
    public void MeteorDestructionEvent(final MeteorEvent event)
    {
        final World worldIn = event.getBoom().world;
        final BlockPos pos = event.getPos();
        if (event.getPower() > PokecubeLegends.config.meteorPowerThreshold && worldIn.getRandom()
                .nextDouble() < PokecubeLegends.config.meteorChanceForAny && !worldIn.isRemote)
        {
            final BlockState block = worldIn.getRandom().nextDouble() > PokecubeLegends.config.meteorChanceForDust
                    ? BlockInit.METEOR_BLOCK.get().getDefaultState()
                    : BlockInit.COSMIC_DUST_ORE.get().getDefaultState();
            final FallingBlockEntity entity = new FallingBlockEntity(worldIn, pos.getX() + 0.5D, pos.getY(), pos.getZ()
                    + 0.5D, block);
            entity.fallTime = 1;
            worldIn.addEntity(entity);
        }
    }
}
