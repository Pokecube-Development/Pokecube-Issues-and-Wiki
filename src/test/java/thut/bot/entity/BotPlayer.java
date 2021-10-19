package thut.bot.entity;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
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
import pokecube.core.world.gen.jigsaw.CustomJigsawStructure;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.Tracker;
import thut.api.entity.CopyCaps;
import thut.api.terrain.StructureManager;
import thut.api.terrain.StructureManager.StructureInfo;
import thut.bot.entity.map.Edge;
import thut.bot.entity.map.Node;
import thut.bot.entity.map.Tree;
import thut.core.common.ThutCore;

public class BotPlayer extends ServerPlayer
{
    private PathfinderMob mob = null;

    int stuckTicks = 0;

    Node targetNode = null;

    Tree map = null;

    public BotPlayer(final ServerLevel world, final GameProfile profile)
    {
        super(world.getServer(), world, profile);
        this.connection = new FakePlayerNetHandler(world.getServer(), this);
    }

    private void buildRoute(final BlockPos origin)
    {
        final Set<StructureInfo> inside = StructureManager.getNear(this.level.dimension(), origin, 16);

        inside.removeIf(i -> !(i.start.getFeature() instanceof CustomJigsawStructure));

        // System.out.println(inside);
        // inside.clear();

        final List<BlockState> paths = Lists.newArrayList(
        // @formatter:off
                Blocks.COARSE_DIRT.defaultBlockState(),
                Blocks.COBBLESTONE.defaultBlockState(),
                Blocks.DIRT_PATH.defaultBlockState(),
                Blocks.COARSE_DIRT.defaultBlockState(),
                Blocks.COBBLESTONE.defaultBlockState(),
                Blocks.DIRT_PATH.defaultBlockState(),
                Blocks.COARSE_DIRT.defaultBlockState(),
                Blocks.COBBLESTONE.defaultBlockState(),
                Blocks.DIRT_PATH.defaultBlockState(),
                Blocks.COARSE_DIRT.defaultBlockState(),
                Blocks.COBBLESTONE.defaultBlockState(),
                Blocks.DIRT_PATH.defaultBlockState(),
                Blocks.STRUCTURE_VOID.defaultBlockState(),
                Blocks.STRUCTURE_VOID.defaultBlockState(),
                Blocks.STRUCTURE_VOID.defaultBlockState());
                // @formatter:on

        if (inside.isEmpty())
        {
            final BlockPos onPos = this.getOnPos();
            final BlockState onState = this.getBlockStateOn();
            final BlockState torch = Blocks.TORCH.defaultBlockState();
            onState.getBlock();
            if (Block.canSupportCenter(this.level, onPos, Direction.UP)) this.level.setBlock(onPos.above(), torch, 3);

            final Iterable<BlockPos> iter = BlockPos.betweenClosed(origin.north().east(), origin.south().west());
            iter.forEach(pos ->
            {
                pos = pos.immutable();
                BlockState on = this.level.getBlockState(pos);

                final Function<BlockPos, BlockState> sides_above = p ->
                {
                    final FluidState fluid = this.level.getFluidState(p);
                    if (fluid.is(FluidTags.WATER)) return Blocks.OAK_PLANKS.defaultBlockState();
                    else if (fluid.is(FluidTags.LAVA)) return Blocks.COBBLESTONE.defaultBlockState();
                    return Blocks.AIR.defaultBlockState();
                };

                final Function<BlockPos, BlockState> below = p ->
                {
                    final FluidState fluid = this.level.getFluidState(p);
                    final BlockState b = this.level.getBlockState(p);
                    if (fluid.is(FluidTags.WATER)) return Blocks.OAK_PLANKS.defaultBlockState();
                    else if (fluid.is(FluidTags.LAVA)) return Blocks.COBBLESTONE.defaultBlockState();
                    else if (b.isAir()) return Blocks.OAK_PLANKS.defaultBlockState();
                    else if (PokecubeTerrainChecker.isLeaves(b)) return Blocks.AIR.defaultBlockState();
                    for (final BlockState block : paths)
                        if (block.getBlock() == b.getBlock()) return Blocks.STRUCTURE_VOID.defaultBlockState();
                    return paths.get(this.getRandom().nextInt(paths.size()));
                };

                final BiFunction<BlockState, BlockPos, Boolean> valid = (s, p) ->
                {
                    final BlockState up = this.level.getBlockState(p.above());
                    if (up.getBlock() == Blocks.TORCH) return false;
                    return PokecubeTerrainChecker.isTerrain(s) || PokecubeTerrainChecker.isLeaves(s)
                            || PokecubeTerrainChecker.isRock(s) || s.getBlock() == Blocks.WATER
                            || PokecubeTerrainChecker.isWood(s) || s.getBlock() == Blocks.DIRT_PATH;
                };

                BlockState place = below.apply(pos);
                if (valid.apply(on, pos) && place.getBlock() != Blocks.STRUCTURE_VOID) this.level.setBlock(pos, place,
                        2);

                pos = pos.below();
                on = this.level.getBlockState(pos);
                place = below.apply(pos);
                if (valid.apply(on, pos) && place.getBlock() != Blocks.STRUCTURE_VOID) this.level.setBlock(pos, place,
                        2);

                pos = pos.above(2);
                BlockState up = this.level.getBlockState(pos);
                if (valid.apply(up, pos)) this.level.setBlock(pos, sides_above.apply(pos), 2);
                pos = pos.above();
                up = this.level.getBlockState(pos);
                if (valid.apply(up, pos)) this.level.setBlock(pos, sides_above.apply(pos), 2);
                pos = pos.above();
                up = this.level.getBlockState(pos);
                if (valid.apply(up, pos)) this.level.setBlock(pos, sides_above.apply(pos), 2);
            });
        }
    }

    private void updateTargetMemory(final BlockPos target, CompoundTag tag)
    {
        if (tag == null) tag = NbtUtils.writeBlockPos(target);
        double dx, dz, dh2;
        for (final Node n : this.getMap().allRooms)
        {
            final BlockPos mid = n.getCenter();
            dx = mid.getX() - target.getX();
            dz = mid.getZ() - target.getZ();

            dh2 = dx * dx + dz * dz;
            if (dh2 < n.size)
            {
                n.setCenter(target, n.size);
                for (final Edge e : n.edges)
                    e.setEnds(e.node1.getCenter(), e.node2.getCenter());
            }
        }
        tag = this.getMap().serializeNBT();
        this.getPersistentData().put("tree_map", tag);
    }

    private void endTarget()
    {
        CompoundTag tag = this.getPersistentData().getCompound("targ_pos");
        final Node n = this.getTargetNode();
        if (n != null) tag = NbtUtils.writeBlockPos(n.getCenter());
        if (tag.isEmpty() || this.getTargetNode() == null) return;
        this.updateTargetMemory(NbtUtils.readBlockPos(tag), tag);
        n.started = true;
        n.dig_done = Tracker.instance().getTick();
        this.getPersistentData().remove("targ_pos");
        this.targetNode = null;
        tag = this.getMap().serializeNBT();
        this.getPersistentData().put("tree_map", tag);
    }

    private BlockPos getTarget()
    {
        final CompoundTag tag = this.getPersistentData().getCompound("targ_pos");
        if (!tag.isEmpty()) return NbtUtils.readBlockPos(tag);
        return null;
    }

    private Tree getMap()
    {
        if (this.map == null)
        {
            final CompoundTag tag = this.getPersistentData().getCompound("tree_map");
            this.map = new Tree();
            this.map.deserializeNBT(tag);
        }
        if (this.map.allRooms.isEmpty())
        {
            final ServerLevel world = (ServerLevel) this.level;
            final ResourceLocation location = new ResourceLocation("pokecube:village");
            final IForgeRegistry<StructureFeature<?>> reg = ForgeRegistries.STRUCTURE_FEATURES;
            final StructureFeature<?> structure = reg.getValue(location);
            final BlockPos village = world.findNearestMapFeature(structure, world.getSharedSpawnPos(), 50, false);
            if (village != null)
            {
                int size = 32;
                final Set<StructureInfo> near = StructureManager.getNear(world.dimension(), village, 0);
                infos:
                for (final StructureInfo i : near)
                    if (i.start.getFeature() == structure)
                    {
                        size = Math.max(i.start.getBoundingBox().getXSpan(), i.start.getBoundingBox().getZSpan());
                        break infos;
                    }
                final Node n = new Node();
                n.started = true;
                n.dig_done = Tracker.instance().getTick();
                n.setCenter(village, size);
                this.map.add(n);
                final CompoundTag tag = this.getMap().serializeNBT();
                this.getPersistentData().put("tree_map", tag);
                this.getPersistentData().put("targ_pos", NbtUtils.writeBlockPos(n.getCenter()));
            }
        }
        return this.map;
    }

    private Node nearest(final BlockPos targ)
    {
        final List<Node> nodes = this.getMap().allRooms;
        for (final Node n : nodes)
            if (targ.closerThan(n.getCenter(), n.size)) return n;
        return nodes.get(0);
    }

    private Node getTargetNode()
    {
        if (this.targetNode != null) return this.targetNode;
        final BlockPos targ = this.getTarget();
        if (targ != null) return this.targetNode = this.nearest(targ);
        else
        {
            final ResourceLocation location = new ResourceLocation("pokecube:village");
            final IForgeRegistry<StructureFeature<?>> reg = ForgeRegistries.STRUCTURE_FEATURES;
            final StructureFeature<?> structure = reg.getValue(location);
            final ServerLevel world = (ServerLevel) this.level;
            final BlockPos village = world.findNearestMapFeature(structure, BlockPos.ZERO, 50, true);
            if (village != null)
            {
                this.targetNode = this.addNode(village);
                final List<Edge> edges = Lists.newArrayList(this.targetNode.edges);
                Collections.shuffle(edges);
                Node prev = null;
                for (final Edge e : edges)
                {
                    final Node other = e.node1 == this.targetNode ? e.node2 : e.node1;
                    if (!other.started) continue;
                    prev = other;
                }
                if (prev != null)
                {
                    final int y = prev.getCenter().getY();
                    BlockPos p = prev.getCenter();
                    world.getChunk(prev.getCenter());
                    p = new BlockPos(p.getX(), world.getHeight(Types.WORLD_SURFACE, p.getX(), p.getZ()), p.getZ());
                    if (p.getY() != y) this.updateTargetMemory(p, null);
                }
                if (prev == null || prev.getCenter().getY() == 0)
                {
                    ThutCore.LOGGER.error("Warning, no started node connected to us!");
                    if (this.targetNode != null) this.getPersistentData().put("targ_pos", NbtUtils.writeBlockPos(
                            this.targetNode.getCenter()));
                    return this.targetNode;
                }
                final BlockPos tpTo = prev.getCenter();
                // Move us to the nearest village to the target.
                this.teleportTo(tpTo.getX(), tpTo.getY(), tpTo.getZ());
                for (final ServerPlayer player : world.players())
                    if (player.getCamera() == this)
                    {
                        player.setCamera(player);
                        player.teleportTo(tpTo.getX(), tpTo.getY(), tpTo.getZ());
                        player.setCamera(this);
                    }

                System.out.println("Maybe here? " + tpTo);

                EntityTools.copyPositions(this.mob, this);
                EntityTools.copyRotations(this.mob, this);
                EntityTools.copyEntityTransforms(this.mob, this);
            }
        }
        if (this.targetNode != null) this.getPersistentData().put("targ_pos", NbtUtils.writeBlockPos(this.targetNode
                .getCenter()));
        return this.targetNode;
    }

    private Node addNode(final BlockPos next)
    {
        final ServerLevel world = (ServerLevel) this.level;
        final List<Node> nodes = Lists.newArrayList(this.getMap().allRooms);
        final BlockPos o0 = next;
        nodes.sort((o1, o2) -> o1.getCenter().subtract(o0).compareTo(o2.getCenter().subtract(o0)));

        int size = 32;
        final Set<StructureInfo> near = StructureManager.getNear(world.dimension(), next, 0);
        final ResourceLocation location = new ResourceLocation("pokecube:village");
        final IForgeRegistry<StructureFeature<?>> reg = ForgeRegistries.STRUCTURE_FEATURES;
        final StructureFeature<?> structure = reg.getValue(location);
        infos:
        for (final StructureInfo i : near)
            if (i.start.getFeature() == structure)
            {
                size = Math.max(i.start.getBoundingBox().getXSpan(), i.start.getBoundingBox().getZSpan());
                break infos;
            }
        final Node n1 = new Node();
        n1.setCenter(next, size);

        for (int i = 0; i < Math.min(nodes.size(), 3); i++)
        {
            final Node n2 = nodes.get(i);
            final Edge e = new Edge();
            e.node1 = n1;
            e.node2 = n2;
            e.setEnds(n1.getCenter(), n2.getCenter());
            n1.edges.add(e);
            n2.edges.add(e);
            this.getMap().allEdges.add(e);
        }
        this.getMap().add(n1);
        final CompoundTag tag = this.getMap().serializeNBT();
        this.getPersistentData().put("tree_map", tag);
        return n1;
    }

    @Override
    public void tick()
    {
        this.setGameMode(GameType.CREATIVE);
        this.setInvulnerable(true);
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

        final Node targetNode = this.getTargetNode();

        if (targetNode == null)
        {
            if (this.tickCount % 20 == 0) System.out.println("No Target, I am idle!");
            return;
        }

        targ = targetNode.mid;

        BlockPos origin = this.getOnPos();

        BlockPos hmap = new BlockPos(this.getOnPos());
        hmap = world.getHeightmapPos(Types.WORLD_SURFACE, hmap);
        final double dy = hmap.getY() - this.getOnPos().getY();

        if (dy > 0) origin = origin.above();

        final int d1 = 9;
        final int d2 = 32;
        final double rr = targ.distanceToSqr(this.position);

        if (rr < d1) this.endTarget();

        final PathNavigation navi = this.mob.getNavigation();
        if (!navi.isInProgress() && rr > d1)
        {
            final BlockPos pos = new BlockPos(targ);
            final Path p = navi.createPath(pos, 1, 16);
            if (p != null) navi.moveTo(p, 1);
            if (p != null)
            {
                boolean rem = false;
                for (int i = 0; i < p.getNodeCount(); i++)
                {
                    final Vec3 vec = p.getEntityPosAtNode(this.mob, i);
                    BlockPos p2 = new BlockPos(vec);
                    this.buildRoute(p2);
                    if (i < p.getNodeCount() - 1)
                    {
                        Vec3 next = p.getEntityPosAtNode(this.mob, i + 1);
                        Vec3 dir = next.subtract(vec);
                        final double dr = dir.length();
                        dir = dir.normalize();
                        for (int j = 0; j < dr; j++)
                        {
                            next = vec.add(dir.scale(j));
                            p2 = new BlockPos(next);
                            this.buildRoute(p2);
                        }
                    }
                }
                if (rem = (p.getNodeCount() < 3 && p.getEndNode().asBlockPos().distManhattan(navi.getTargetPos()) < d2))
                    this.endTarget();
                if (this.tickCount % 20 == 0) System.out.println(p.getNodeCount() + " " + targ + " " + targ.distanceTo(
                        this.position) + " " + p.getEndNode().asBlockPos().distManhattan(navi.getTargetPos()) + " "
                        + rem);
            }
        }
        this.lookAt(Anchor.EYES, targ);

        boolean naviing = false;
        final int diff = 1;
        if (navi.isInProgress())
        {
            naviing = true;
            if (targ.y == 0 && rr < 256 * 256)
            {
                BlockPos village = new BlockPos(targ);
                world.getChunk(village);
                village = new BlockPos(village.getX(), world.getHeight(Types.WORLD_SURFACE, village.getX(), village
                        .getZ()), village.getZ());
                targ = new Vec3(village.getX(), village.getY(), village.getZ());
                this.updateTargetMemory(village, null);
            }

            final BlockPos next = navi.getPath().getNextNodePos();
            final BlockPos here = this.getOnPos();
            if (next.getY() <= here.getY() && here.closerThan(next, diff)) navi.getPath().advance();

            if (this.tickCount % 20 == 0) System.out.println("To Next: " + diff + " " + navi.getPath()
                    .getNextNodeIndex() + "/" + navi.getPath().getNodeCount() + " " + next.distManhattan(here));
        }
        else this.buildRoute(origin);

        this.mob.setSilent(true);
        this.mob.maxUpStep = 1.25f;

        // Prevent the mob from having its own ideas of what to do
        this.mob.getBrain().removeAllBehaviors();

        this.mob.setOldPosAndRot();
        this.mob.tickCount = this.tickCount;
        this.mob.tick();

        if (this.tickCount % 20 == 0) System.out.println("remaining: " + Math.sqrt(rr) + " " + this.position() + " "
                + targ);

        final double dh = this.mob.position().subtract(this.position()).horizontalDistance();

        if (dh < 0.25) this.stuckTicks++;
        else this.stuckTicks = 0;

        if (this.stuckTicks > 60)
        {
            if (this.stuckTicks > 100 && naviing && rr > diff)
            {
                if (this.tickCount % 20 == 0) System.out.println("Move Up?");
                targ = targ.normalize().scale(0.015);
                double y = Math.signum(dy);
                if (y > 1) y *= 0.25;
                else y *= 0.5;
                targ = targ.add(0, y - 0.1, 0);
                this.mob.setDeltaMovement(targ);
                this.mob.setOnGround(true);
            }
            if (this.stuckTicks > 2000) this.stuckTicks = 0;
            if (this.stuckTicks % 100 == 99)
            {
                this.mob.getNavigation().stop();
                final int y = world.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, this.mob.getBlockX(), this.mob
                        .getBlockZ());

                final Vec3 pos = LandRandomPos.getPos(this.mob, 5, 5);
                if (pos != null) this.mob.setPos(pos.x, pos.y, pos.z);
                else this.mob.setPos(this.mob.getX(), y, this.mob.getZ());
            }
            if (this.tickCount % 20 == 0) System.out.println("Stuck!!! " + this.stuckTicks + " " + dh);
        }

        if (this.mob.isInWater()) this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(0, 0.05, 0));
        else this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(0, -0.08, 0));

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
