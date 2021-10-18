package thut.bot.entity;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.mojang.authlib.GameProfile;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

public class BotPlayer extends ServerPlayer
{

    public BotPlayer(final ServerLevel world, final GameProfile profile)
    {
        super(world.getServer(), world, profile);
        this.connection = new FakePlayerNetHandler(world.getServer(), this);
    }

    @Override
    public void baseTick()
    {
        super.baseTick();
    }

    @Override
    public void doTick()
    {
        super.doTick();
    }

    @Override
    public void tick()
    {
        final Vec3 v = this.getDeltaMovement();
        super.tick();
        this.setDeltaMovement(0.0, v.y - 0.1, 0.0);
        this.getAbilities().flying = false;
        this.getAbilities().mayfly = false;
        this.move(MoverType.SELF, this.deltaMovement);
    }

    @ParametersAreNonnullByDefault
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
