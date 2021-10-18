package thut.bot.entity;

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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import pokecube.core.utils.EntityTools;
import pokecube.core.world.terrain.PokecubeTerrainChecker;

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
        if (!(this.level instanceof final ServerLevel world)) return;

        if (this.mob == null)
        {
            this.mob = new Villager(EntityType.VILLAGER, this.level);
            EntityTools.copyPositions(this.mob, this);
            EntityTools.copyRotations(this.mob, this);
            EntityTools.copyEntityTransforms(this.mob, this);
        }

        final Iterable<BlockPos> iter = BlockPos.betweenClosed(this.getOnPos().north().east(), this.getOnPos().south()
                .west());
        iter.forEach(pos ->
        {
            pos = pos.immutable();
            final BlockState on = this.level.getBlockState(pos);
            final BlockState path = Blocks.COARSE_DIRT.defaultBlockState();
            if (PokecubeTerrainChecker.isGround(on)) this.level.setBlock(pos, path, 3);
            pos = pos.above();
            BlockState up = this.level.getBlockState(pos);
            if (PokecubeTerrainChecker.isGround(on) || PokecubeTerrainChecker.isLeaves(up)) this.level.setBlock(pos,
                    Blocks.AIR.defaultBlockState(), 3);
            pos = pos.above();
            up = this.level.getBlockState(pos);
            if (PokecubeTerrainChecker.isGround(on) || PokecubeTerrainChecker.isLeaves(up)) this.level.setBlock(pos,
                    Blocks.AIR.defaultBlockState(), 3);
            pos = pos.above();
            up = this.level.getBlockState(pos);
            if (PokecubeTerrainChecker.isGround(on) || PokecubeTerrainChecker.isLeaves(up)) this.level.setBlock(pos,
                    Blocks.AIR.defaultBlockState(), 3);
        });

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

        final PathNavigation navi = this.mob.getNavigation();
        if (!navi.isInProgress() && targ.distanceToSqr(this.position) > 9)
        {
            BlockPos pos = new BlockPos(targ);
            if (targ.distanceTo(this.position) < 256) pos = world.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, pos);
            final Path p = navi.createPath(pos, 1, 16);
            if (p != null) navi.moveTo(p, 1);
            if (p != null) if (p.getNodeCount() == 1 && navi.getPath().getEndNode().asBlockPos().equals(navi
                    .getTargetPos())) this.getPersistentData().remove("targ_pos");
        }

        // Prevent the mob from having its own ideas of what to do
        this.mob.getBrain().removeAllBehaviors();
        if (this.mob.getMoveControl().hasWanted())
        {
            targ = new Vec3(this.mob.getMoveControl().getWantedX(), this.mob.getMoveControl().getWantedY(), this.mob
                    .getMoveControl().getWantedZ());
            this.lookAt(Anchor.EYES, targ);
        }
        else this.mob.setDeltaMovement(this.mob.getDeltaMovement().multiply(0.8, 1, 0.8));
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
