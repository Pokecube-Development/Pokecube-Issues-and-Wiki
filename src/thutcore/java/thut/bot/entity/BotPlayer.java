package thut.bot.entity;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundBlockEntityTagQueryPacket;
import net.minecraft.network.protocol.game.ServerboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundChatAckPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundChatSessionUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundEntityTagQueryPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundJigsawGeneratePacket;
import net.minecraft.network.protocol.game.ServerboundLockDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemPacket;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookChangeSettingsPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookSeenRecipePacket;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandMinecartPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSetJigsawBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetStructureBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.event.ServerChatEvent;
import org.jetbrains.annotations.Nullable;
import thut.api.Tracker;
import thut.api.util.PermNodes;
import thut.api.util.PermNodes.DefaultPermissionLevel;
import thut.bot.ThutBot;
import thut.bot.ThutBot.BotEntry;
import thut.bot.entity.ai.IBotAI;
import thut.core.common.ThutCore;
import thut.core.common.network.EntityUpdate;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BotPlayer extends ServerPlayer implements Npc
{

    public static final String PERMBOTORDER = "thutbot.perm.orderbot";

    public static final Pattern STARTORDER = Pattern.compile("(start)(\\s)(\\w+:\\w+)");

    private IBotAI routine;

    private final BotEntry entry;

    private final List<Pair<Long, String>> chat_queue = new ArrayList<>();

    public BotPlayer(final ServerLevel world, final GameProfile profile)
    {
        super(world.getServer(), world, profile, ClientInformation.createDefault());
        this.connection = new FakePlayerNetHandler(world.getServer(), this);
        entry = ThutBot.BOT_MAP.get(profile.getId());
        try
        {
            if (entry.getFile().exists()) this.getPersistentData().merge(NbtIo.read(entry.getFile().toPath()));
        }
        catch (Exception e)
        {
            ThutBot.LOGGER.error("Error loading saved tag for {}", entry.name);
            ThutBot.LOGGER.error(e);
        }

        if (this.getPersistentData().contains("_last_pos_"))
        {
            BlockPos pos = NbtUtils.readBlockPos(this.getPersistentData(), "_last_pos_").get();
            this.setPos(pos.getX(), pos.getY(), pos.getZ());
        }
    }

    @Override
    public void tick()
    {
        ChunkPos cpos = this.chunkPosition();
        ServerLevel level = (ServerLevel) this.level();

        chat_queue.removeIf(pair -> {
            var tick = pair.getFirst();
            if (tick > Tracker.instance().getTick()) return false;
            String message = pair.getSecond();
            PlayerChatMessage message2 = PlayerChatMessage.system(message);
            this.server.getPlayerList().broadcastChatMessage(message2, this, ChatType.bind(ChatType.CHAT, this));
            return true;
        });

        if (routine != null)
        {
            this.routine.tick();
            if (routine.isCompleted())
            {
                routine.end(null);
                this.getPersistentData().remove("ai_task");
                routine = null;
            }
        }
        else if (this.getPersistentData().contains("ai_task"))
        {
            String key = this.getPersistentData().getString("ai_task");
            IBotAI.Factory<?> factory = IBotAI.REGISTRY.get(key);
            if (factory != null)
            {
                this.routine = factory.create(this);
                this.routine.setKey(key);
                this.routine.start(null);
            }
        }
        else
        {
            this.setHealth(this.getMaxHealth());
            this.dead = false;
            if (this.tickCount % 20 == 0) EntityUpdate.sendEntityUpdate(this);

            this.setDeltaMovement(0, this.getDeltaMovement().y, 0);

            if (this.isInWater()) this.setDeltaMovement(this.getDeltaMovement().add(0, 0.05, 0));
            else this.setDeltaMovement(this.getDeltaMovement().add(0, -0.08, 0));

            this.move(MoverType.SELF, this.getDeltaMovement());
        }

        if (cpos != this.chunkPosition())
        {
            level.getChunkSource().move(this);
            this.getPersistentData().put("_last_pos_", NbtUtils.writeBlockPos(getOnPos()));
        }

        if (this.tickCount % 60 == 0)
        {
            try
            {
                entry.updateDimension(level.dimension());
                NbtIo.write(getPersistentData(), entry.getFile().toPath());
                ThutBot.saveBots();
            }
            catch (Exception e)
            {
                ThutBot.LOGGER.error("Error saving tag for {}", entry.name);
                ThutBot.LOGGER.error(e);
            }
        }
    }

    public void onChat(ServerChatEvent event)
    {
        ServerPlayer talker = event.getPlayer();
        if (talker instanceof BotPlayer) return;

        String cmd = event.getMessage().getString();

        boolean isOrder = cmd.contains(this.getName().getString());

        // Decide if we want to say something back?
        if (!isOrder) return;

        PermNodes.registerBooleanNode(ThutCore.MODID, PERMBOTORDER, DefaultPermissionLevel.OP,
                "Allowed to give orders to thutbots");
        String s1 = "I Am A Bot";
        chat(s1);

        if (!PermNodes.getBooleanPerm(talker, PERMBOTORDER)) return;

        if (cmd.toLowerCase(Locale.ROOT).contains("where are you?"))
        {
            chat("I am at " + this.getOnPos());
            return;
        }

        if (cmd.toLowerCase(Locale.ROOT).contains("what are you doing?"))
        {
            if (routine == null) chat("I am idle");
            else chat("I am doing: " + routine.getKey());
            return;
        }

        Matcher startOrder = STARTORDER.matcher(cmd);

        boolean had = startOrder.find();

        if (!had)
        {
            startOrder = Pattern.compile("(build)(\\s)(\\w+:\\w+)").matcher(cmd);
            had = startOrder.find();
        }
        if (had)
        {
            String key = startOrder.group(3);
            IBotAI.Factory<?> factory = IBotAI.REGISTRY.get(key);
            if (factory != null)
            {
                s1 = "Starting " + key;
                this.getPersistentData().putString("ai_task", key);
                if (this.routine != null) this.routine.end(talker);
                this.routine = factory.create(this);
                routine.setKey(key);
                boolean valid = false;

                try
                {
                    valid = routine.init(cmd);
                }
                catch (Exception e)
                {
                    ThutBot.LOGGER.error(e);
                }

                if (!valid)
                {
                    chat("Invalid argument!");
                    this.getPersistentData().remove("ai_task");
                    this.routine = null;
                    return;
                }
                this.routine.start(talker);
                chat(s1);
            }
            else
            {
                s1 = "I don't know how to do that!";
                if (this.routine != null)
                {
                    this.routine.end(talker);
                    this.getPersistentData().remove("ai_task");
                    this.routine = null;
                }
                chat(s1);
                s1 = "What I know how to do:";
                chat(s1);
                for (String s : IBotAI.REGISTRY.keySet())
                {
                    chat(s);
                }
            }
        }
        else if (cmd.contains("reset"))
        {
            if (this.routine != null) this.routine.end(talker);
            List<String> tags = new ArrayList<>(this.getPersistentData().getAllKeys());
            tags.forEach(s -> getPersistentData().remove(s));
            this.routine = null;
        }
    }

    public void chat(String message)
    {
        chat_queue.add(Pair.of(Tracker.instance().getTick() + 1, message));
    }

    @Override
    public boolean isFakePlayer()
    {
        return true;
    }

    @ParametersAreNonnullByDefault
    private static class FakePlayerNetHandler extends ServerGamePacketListenerImpl
    {
        private static final Connection DUMMY_CONNECTION = new BotPlayer.FakeConnection();

        public FakePlayerNetHandler(MinecraftServer server, ServerPlayer player)
        {
            super(server, DUMMY_CONNECTION, player, CommonListenerCookie.createInitial(player.getGameProfile(), false));
        }

        @Override
        public void tick() {}

        @Override
        public void resetPosition() {}

        @Override
        public void disconnect(Component message) {}

        @Override
        public void handlePlayerInput(ServerboundPlayerInputPacket packet) {}

        @Override
        public void handleMoveVehicle(ServerboundMoveVehiclePacket packet) {}

        @Override
        public void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket packet) {}

        @Override
        public void handleRecipeBookSeenRecipePacket(ServerboundRecipeBookSeenRecipePacket packet) {}

        @Override
        public void handleRecipeBookChangeSettingsPacket(ServerboundRecipeBookChangeSettingsPacket packet) {}

        @Override
        public void handleSeenAdvancements(ServerboundSeenAdvancementsPacket packet) {}

        @Override
        public void handleCustomCommandSuggestions(ServerboundCommandSuggestionPacket packet) {}

        @Override
        public void handleSetCommandBlock(ServerboundSetCommandBlockPacket packet) {}

        @Override
        public void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket packet) {}

        @Override
        public void handlePickItem(ServerboundPickItemPacket packet) {}

        @Override
        public void handleRenameItem(ServerboundRenameItemPacket packet) {}

        @Override
        public void handleSetBeaconPacket(ServerboundSetBeaconPacket packet) {}

        @Override
        public void handleSetStructureBlock(ServerboundSetStructureBlockPacket packet) {}

        @Override
        public void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket packet) {}

        @Override
        public void handleJigsawGenerate(ServerboundJigsawGeneratePacket packet) {}

        @Override
        public void handleSelectTrade(ServerboundSelectTradePacket packet) {}

        @Override
        public void handleEditBook(ServerboundEditBookPacket packet) {}

        @Override
        public void handleEntityTagQuery(ServerboundEntityTagQueryPacket packet) {}

        @Override
        public void handleBlockEntityTagQuery(ServerboundBlockEntityTagQueryPacket packet) {}

        @Override
        public void handleMovePlayer(ServerboundMovePlayerPacket packet) {}

        @Override
        public void teleport(double x, double y, double z, float yaw, float pitch) {}

        @Override
        public void handlePlayerAction(ServerboundPlayerActionPacket packet) {}

        @Override
        public void handleUseItemOn(ServerboundUseItemOnPacket packet) {}

        @Override
        public void handleUseItem(ServerboundUseItemPacket packet) {}

        @Override
        public void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket packet) {}

        @Override
        public void handleResourcePackResponse(ServerboundResourcePackPacket p_295695_) {}

        @Override
        public void handlePaddleBoat(ServerboundPaddleBoatPacket packet) {}

        @Override
        public void onDisconnect(DisconnectionDetails details) {}

        @Override
        public void send(Packet<?> packet) {}

        @Override
        public void send(Packet<?> packet, @Nullable PacketSendListener sendListener) {}

        @Override
        public void handleSetCarriedItem(ServerboundSetCarriedItemPacket packet) {}

        @Override
        public void handleChat(ServerboundChatPacket packet) {}

        @Override
        public void handleAnimate(ServerboundSwingPacket packet) {}

        @Override
        public void handlePlayerCommand(ServerboundPlayerCommandPacket packet) {}

        @Override
        public void handleInteract(ServerboundInteractPacket packet) {}

        @Override
        public void handleClientCommand(ServerboundClientCommandPacket packet) {}

        @Override
        public void handleContainerClose(ServerboundContainerClosePacket packet) {}

        @Override
        public void handleContainerClick(ServerboundContainerClickPacket packet) {}

        @Override
        public void handlePlaceRecipe(ServerboundPlaceRecipePacket packet) {}

        @Override
        public void handleContainerButtonClick(ServerboundContainerButtonClickPacket packet) {}

        @Override
        public void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket packet) {}

        @Override
        public void handleSignUpdate(ServerboundSignUpdatePacket packet) {}

        @Override
        public void handleKeepAlive(ServerboundKeepAlivePacket p_294627_) {}

        @Override
        public void handleCustomPayload(ServerboundCustomPayloadPacket p_294276_) {}

        @Override
        public void handleClientInformation(ServerboundClientInformationPacket p_301979_) {}

        @Override
        public void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket packet) {}

        @Override
        public void handleChangeDifficulty(ServerboundChangeDifficultyPacket packet) {}

        @Override
        public void handleLockDifficulty(ServerboundLockDifficultyPacket packet) {}

        @Override
        public void teleport(double x, double y, double z, float yaw, float pitch, Set<RelativeMovement> relativeSet) {}

        @Override
        public void ackBlockChangesUpTo(int sequence) {}

        @Override
        public void handleChatCommand(ServerboundChatCommandPacket packet) {}

        @Override
        public void handleChatAck(ServerboundChatAckPacket packet) {}

        @Override
        public void addPendingMessage(PlayerChatMessage message) {}

        @Override
        public void sendPlayerChatMessage(PlayerChatMessage message, ChatType.Bound boundChatType) {}

        @Override
        public void sendDisguisedChatMessage(Component content, ChatType.Bound boundChatType) {}

        @Override
        public void handleChatSessionUpdate(ServerboundChatSessionUpdatePacket packet) {}

        @Override
        public boolean hasChannel(ResourceLocation payloadId)
        {
            return false;
        }
    }

    private static final class FakeConnection extends Connection
    {
        public FakeConnection()
        {
            super(PacketFlow.SERVERBOUND);
        }

        @Override
        public void send(Packet<?> packet)
        {

        }

        @Override
        public void tick()
        {

        }

        @Override
        protected void tickSecond()
        {

        }

        @Override
        public void disconnect(DisconnectionDetails disconnectionDetails)
        {

        }

        @Override
        public void setListenerForServerboundHandshake(PacketListener listener) {}
    }
}
