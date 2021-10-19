package thut.bot.entity;

import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;
import com.mojang.authlib.GameProfile;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import pokecube.core.utils.EntityTools;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.entity.CopyCaps;
import thut.api.terrain.StructureManager;
import thut.api.terrain.StructureManager.StructureInfo;

public class BotPlayer extends ServerPlayer
{
    private PathfinderMob mob = null;

    public BotPlayer(final ServerLevel world, final GameProfile profile)
    {
        super(world.getServer(), world, profile);
        this.connection = new FakePlayerNetHandler(world.getServer(), this);
    }

    @Override
    public void tick()
    {
        if (ForgeHooks.onLivingUpdate(this)) return;

        if (!(this.level instanceof final ServerLevel world)) return;

        final PathfinderMob old = this.mob;
        if (this.mob == null)
        {
            this.mob = new Villager(EntityType.VILLAGER, this.level);
            CopyCaps.get(this).setCopiedMob(this.mob);
        }
        final boolean init = this.mob != old;
        if (init)
        {
            this.mob.setInvulnerable(true);
            this.mob.setInvisible(true);
            EntityTools.copyPositions(this.mob, this);
            EntityTools.copyRotations(this.mob, this);
            EntityTools.copyEntityTransforms(this.mob, this);
        }

        Vec3 targ = this.position;

        if (this.getPersistentData().contains("targ_pos"))
        {
            final BlockPos village = NbtUtils.readBlockPos(this.getPersistentData().getCompound("targ_pos"));
            targ = new Vec3(village.getX(), village.getY(), village.getZ());
            if (targ.distanceToSqr(this.position) < 9) this.getPersistentData().remove("targ_pos");
        }
        else
        {
            final ResourceLocation location = new ResourceLocation("pokecube:village");
            final IForgeRegistry<StructureFeature<?>> reg = ForgeRegistries.STRUCTURE_FEATURES;
            final StructureFeature<?> structure = reg.getValue(location);

            if (reg.containsKey(location))
            {
                BlockPos village = world.findNearestMapFeature(structure, BlockPos.ZERO, 50, true);
                if (village != null)
                {
                    village = new BlockPos(village.getX(), world.getHeight(Types.WORLD_SURFACE, village.getX(), village
                            .getZ()), village.getZ());
                    targ = new Vec3(village.getX(), village.getY(), village.getZ());
                    this.getPersistentData().put("targ_pos", NbtUtils.writeBlockPos(village));
                }
            }
        }

        BlockPos origin = this.getOnPos();

        BlockPos hmap = new BlockPos(this.getOnPos());
        hmap = world.getHeightmapPos(Types.WORLD_SURFACE, hmap);
        final double dy = hmap.getY() - this.getOnPos().getY();

        if (dy > 0) origin = origin.above();

        final Set<StructureInfo> inside = StructureManager.getFor(this.level.dimension(), origin);
        if (inside.isEmpty())
        {

            final Iterable<BlockPos> iter = BlockPos.betweenClosed(origin.north().east(), origin.south().west());
            iter.forEach(pos ->
            {
                pos = pos.immutable();
                BlockState on = this.level.getBlockState(pos);

                final Function<BlockPos, BlockState> sides_above = p ->
                {
                    final FluidState fluid = world.getFluidState(p);
                    if (fluid.is(FluidTags.WATER)) return Blocks.OAK_PLANKS.defaultBlockState();
                    else if (fluid.is(FluidTags.LAVA)) return Blocks.COBBLESTONE.defaultBlockState();
                    return Blocks.AIR.defaultBlockState();
                };

                final Function<BlockPos, BlockState> below = p ->
                {
                    final FluidState fluid = world.getFluidState(p);
                    if (fluid.is(FluidTags.WATER)) return Blocks.OAK_PLANKS.defaultBlockState();
                    else if (fluid.is(FluidTags.LAVA)) return Blocks.COBBLESTONE.defaultBlockState();
                    else if (world.getBlockState(p).isAir()) return Blocks.OAK_PLANKS.defaultBlockState();
                    else if (PokecubeTerrainChecker.isLeaves(world.getBlockState(p))) return Blocks.AIR
                            .defaultBlockState();
                    return Blocks.COARSE_DIRT.defaultBlockState();
                };

                final Function<BlockState, Boolean> valid = s -> PokecubeTerrainChecker.isTerrain(s)
                        || PokecubeTerrainChecker.isLeaves(s) || PokecubeTerrainChecker.isRock(s) || s
                                .getBlock() == Blocks.WATER;

                if (valid.apply(on)) this.level.setBlock(pos, below.apply(pos), 3);

                pos = pos.below();
                on = this.level.getBlockState(pos);
                if (valid.apply(on)) this.level.setBlock(pos, below.apply(pos), 3);

                pos = pos.above(2);
                BlockState up = this.level.getBlockState(pos);
                if (valid.apply(up)) this.level.setBlock(pos, sides_above.apply(pos), 3);
                pos = pos.above();
                up = this.level.getBlockState(pos);
                if (valid.apply(up)) this.level.setBlock(pos, sides_above.apply(pos), 3);
                pos = pos.above();
                up = this.level.getBlockState(pos);
                if (valid.apply(up)) this.level.setBlock(pos, sides_above.apply(pos), 3);
            });
        }

        final int d1 = 9;
        final int d2 = 32;
        final double dist = targ.distanceToSqr(this.position);

        final PathNavigation navi = this.mob.getNavigation();
        if (!navi.isInProgress() && dist > d1)
        {
            BlockPos pos = new BlockPos(targ);
            if (Math.sqrt(dist) < 256)
            {
                pos = world.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, pos);
                this.getPersistentData().put("targ_pos", NbtUtils.writeBlockPos(pos));
            }
            final Path p = navi.createPath(pos, 1, 16);
            if (p != null) navi.moveTo(p, 1);
            if (p != null)
            {
                boolean rem = false;
                if (rem = (p.getNodeCount() < 3 && p.getEndNode().asBlockPos().distManhattan(navi.getTargetPos()) < d2))
                    this.getPersistentData().remove("targ_pos");
                System.out.println(p.getNodeCount() + " " + targ + " " + targ.distanceTo(this.position) + " " + p
                        .getEndNode().asBlockPos().distManhattan(navi.getTargetPos()) + " " + rem);
            }
        }

        if (navi.isInProgress())
        {
            final BlockPos next = navi.getPath().getNextNodePos();
            final BlockPos here = this.getOnPos();
            int diff = 1;
            if (next.getY() < here.getY()) diff = 4;
            if (here.closerThan(next, diff)) navi.getPath().advance();
            if (targ.distanceTo(this.position) > 0)
            {
                targ = targ.normalize().scale(0.05);
                targ = targ.add(0, Math.signum(dy) * 0.25 - 0.1, 0);
                this.mob.setDeltaMovement(targ);
                this.mob.setOnGround(true);
            }
        }
        if (this.tickCount % 20 == 0) System.out.println(dist);
        this.mob.setSilent(true);
        this.mob.maxUpStep = 1.25f;

        // Prevent the mob from having its own ideas of what to do
        this.mob.getBrain().removeAllBehaviors();
        this.lookAt(Anchor.EYES, targ);

        this.mob.setOldPosAndRot();
        this.mob.tickCount = this.tickCount;
        this.mob.tick();

        if (this.mob.isInWater()) this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(0, 0.05, 0));

        this.setGameMode(GameType.CREATIVE);
        this.setInvulnerable(true);

        EntityTools.copyEntityTransforms(this, this.mob);
        EntityTools.copyPositions(this, this.mob);
        EntityTools.copyRotations(this, this.mob);

    }

    private static class FakePlayerNetHandler extends ServerGamePacketListenerImpl
    {
        private static final Connection DUMMY_CONNECTION = new Connection(PacketFlow.CLIENTBOUND);

        public FakePlayerNetHandler(final MinecraftServer server, final ServerPlayer player)
        {
            super(server, FakePlayerNetHandler.DUMMY_CONNECTION, player);
        }

        //@formatter:off
//        @Override public void tick() { }
//        @Override public void resetPosition() { }
        @Override public void disconnect(final Component message) { }
//        @Override public void handlePlayerInput(final ServerboundPlayerInputPacket packet) { }
//        @Override public void handleMoveVehicle(final ServerboundMoveVehiclePacket packet) { }
//        @Override public void handleAcceptTeleportPacket(final ServerboundAcceptTeleportationPacket packet) { }
//        @Override public void handleRecipeBookSeenRecipePacket(final ServerboundRecipeBookSeenRecipePacket packet) { }
//        @Override public void handleRecipeBookChangeSettingsPacket(final ServerboundRecipeBookChangeSettingsPacket packet) { }
//        @Override public void handleSeenAdvancements(final ServerboundSeenAdvancementsPacket packet) { }
//        @Override public void handleCustomCommandSuggestions(final ServerboundCommandSuggestionPacket packet) { }
//        @Override public void handleSetCommandBlock(final ServerboundSetCommandBlockPacket packet) { }
//        @Override public void handleSetCommandMinecart(final ServerboundSetCommandMinecartPacket packet) { }
//        @Override public void handlePickItem(final ServerboundPickItemPacket packet) { }
//        @Override public void handleRenameItem(final ServerboundRenameItemPacket packet) { }
//        @Override public void handleSetBeaconPacket(final ServerboundSetBeaconPacket packet) { }
//        @Override public void handleSetStructureBlock(final ServerboundSetStructureBlockPacket packet) { }
//        @Override public void handleSetJigsawBlock(final ServerboundSetJigsawBlockPacket packet) { }
//        @Override public void handleJigsawGenerate(final ServerboundJigsawGeneratePacket packet) { }
//        @Override public void handleSelectTrade(final ServerboundSelectTradePacket packet) { }
//        @Override public void handleEditBook(final ServerboundEditBookPacket packet) { }
//        @Override public void handleEntityTagQuery(final ServerboundEntityTagQuery packet) { }
//        @Override public void handleBlockEntityTagQuery(final ServerboundBlockEntityTagQuery packet) { }
//        @Override public void handleMovePlayer(final ServerboundMovePlayerPacket packet) { }
//        @Override public void teleport(final double x, final double y, final double z, final float yaw, final float pitch) { }
//        @Override public void teleport(final double x, final double y, final double z, final float yaw, final float pitch, final Set<ClientboundPlayerPositionPacket.RelativeArgument> flags) { }
//        @Override public void handlePlayerAction(final ServerboundPlayerActionPacket packet) { }
//        @Override public void handleUseItemOn(final ServerboundUseItemOnPacket packet) { }
//        @Override public void handleUseItem(final ServerboundUseItemPacket packet) { }
//        @Override public void handleTeleportToEntityPacket(final ServerboundTeleportToEntityPacket packet) { }
//        @Override public void handleResourcePackResponse(final ServerboundResourcePackPacket packet) { }
//        @Override public void handlePaddleBoat(final ServerboundPaddleBoatPacket packet) { }
//        @Override public void onDisconnect(final Component message) { }
        @Override public void send(final Packet<?> packet) { }
        @Override public void send(final Packet<?> packet, @Nullable final GenericFutureListener<? extends Future<? super Void>> listener) { }
//        @Override public void handleSetCarriedItem(final ServerboundSetCarriedItemPacket packet) { }
//        @Override public void handleChat(final ServerboundChatPacket packet) { }
//        @Override public void handleAnimate(final ServerboundSwingPacket packet) { }
//        @Override public void handlePlayerCommand(final ServerboundPlayerCommandPacket packet) { }
//        @Override public void handleInteract(final ServerboundInteractPacket packet) { }
//        @Override public void handleClientCommand(final ServerboundClientCommandPacket packet) { }
//        @Override public void handleContainerClose(final ServerboundContainerClosePacket packet) { }
//        @Override public void handleContainerClick(final ServerboundContainerClickPacket packet) { }
//        @Override public void handlePlaceRecipe(final ServerboundPlaceRecipePacket packet) { }
//        @Override public void handleContainerButtonClick(final ServerboundContainerButtonClickPacket packet) { }
//        @Override public void handleSetCreativeModeSlot(final ServerboundSetCreativeModeSlotPacket packet) { }
//        @Override public void handleSignUpdate(final ServerboundSignUpdatePacket packet) { }
//        @Override public void handleKeepAlive(final ServerboundKeepAlivePacket packet) { }
//        @Override public void handlePlayerAbilities(final ServerboundPlayerAbilitiesPacket packet) { }
//        @Override public void handleClientInformation(final ServerboundClientInformationPacket packet) { }
//        @Override public void handleCustomPayload(final ServerboundCustomPayloadPacket packet) { }
//        @Override public void handleChangeDifficulty(final ServerboundChangeDifficultyPacket packet) { }
//        @Override public void handleLockDifficulty(final ServerboundLockDifficultyPacket packet) { }
        //@formatter:on
    }
}
