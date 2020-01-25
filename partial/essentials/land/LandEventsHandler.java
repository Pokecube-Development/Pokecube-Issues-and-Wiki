package thut.essentials.land;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemFood;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.Ticket;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.FarmlandTrampleEvent;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import thut.essentials.Essentials;
import thut.essentials.ThutEssentials;
import thut.essentials.events.DenyItemUseEvent;
import thut.essentials.events.DenyItemUseEvent.UseType;
import thut.essentials.land.LandManager.LandTeam;
import thut.essentials.util.CompatWrapper;
import thut.essentials.util.Coordinate;

public class LandEventsHandler
{

    public static Set<String> itemUseWhitelist    = Sets.newHashSet();
    public static Set<String> blockUseWhiteList   = Sets.newHashSet();
    public static Set<String> blockBreakWhiteList = Sets.newHashSet();
    public static Set<String> blockPlaceWhiteList = Sets.newHashSet();

    public static void init()
    {
        MinecraftForge.EVENT_BUS.unregister(ThutEssentials.instance.teams);
        MinecraftForge.EVENT_BUS.unregister(ThutEssentials.instance.teams.interact_handler);
        MinecraftForge.EVENT_BUS.unregister(ThutEssentials.instance.teams.entity_handler);
        MinecraftForge.EVENT_BUS.unregister(ThutEssentials.instance.teams.block_handler);
        itemUseWhitelist.clear();
        for (String s : Essentials.config.itemUseWhitelist)
        {
            itemUseWhitelist.add(s);
        }
        blockUseWhiteList.clear();
        for (String s : Essentials.config.blockUseWhitelist)
        {
            blockUseWhiteList.add(s);
        }
        blockBreakWhiteList.clear();
        for (String s : Essentials.config.blockBreakWhitelist)
        {
            blockBreakWhiteList.add(s);
        }
        blockPlaceWhiteList.clear();
        for (String s : Essentials.config.blockPlaceWhitelist)
        {
            blockPlaceWhiteList.add(s);
        }
        MinecraftForge.EVENT_BUS.register(ThutEssentials.instance.teams);
        MinecraftForge.EVENT_BUS.register(ThutEssentials.instance.teams.interact_handler);
        MinecraftForge.EVENT_BUS.register(ThutEssentials.instance.teams.entity_handler);
        MinecraftForge.EVENT_BUS.register(ThutEssentials.instance.teams.block_handler);
    }

    public static class BlockEventHandler
    {
        public void checkPlace(BlockEvent evt, PlayerEntity player)
        {
            if (!(player instanceof ServerPlayerEntity)) return;
            // check whitelist first.
            String name = evt.getWorld().getBlockState(evt.getPos()).getBlock().getRegistryName().toString();
            if (blockPlaceWhiteList.contains(name)) { return; }
            // Chunk Coordinate
            Coordinate c = Coordinate.getChunkCoordFromWorldCoord(evt.getPos(),
                    player.getEntityWorld().provider.getDimension());
            // Block coordinate
            Coordinate b = new Coordinate(evt.getPos(), player.getEntityWorld().provider.getDimension());
            LandTeam team = LandManager.getInstance().getLandOwner(c);

            // Check permission for breaking wilderness, then return.
            if (team == null)
            {
                if (PermissionAPI.hasPermission(player, PERMPLACEWILD)) { return; }
                // TODO better message.
                player.sendMessage(new StringTextComponent("Cannot place that."));
                evt.setCanceled(true);
                ((ServerPlayerEntity) player).sendAllContents(player.inventoryContainer,
                        player.inventoryContainer.inventoryItemStacks);
                return;

            }
            // Check if the team allows fakeplayers
            if (team.fakePlayers && player instanceof FakePlayer)
            {

            }
            else // Otherwise check normal behaviour
            {
                // Treat relation place perm as owning the land.
                boolean owns = team.canPlaceBlock(player.getUniqueID(), b);
                if (owns && !PermissionAPI.hasPermission(player, PERMPLACEOWN))
                {
                    sendMessage(player, team, DENY);
                    evt.setCanceled(true);
                    ((ServerPlayerEntity) player).sendAllContents(player.inventoryContainer,
                            player.inventoryContainer.inventoryItemStacks);
                    return;
                }
                if (!owns && !PermissionAPI.hasPermission(player, PERMPLACEOTHER))
                {
                    sendMessage(player, team, DENY);
                    evt.setCanceled(true);
                    ((ServerPlayerEntity) player).sendAllContents(player.inventoryContainer,
                            player.inventoryContainer.inventoryItemStacks);
                    return;
                }
            }
        }

        public void checkBreak(BlockEvent evt, PlayerEntity player)
        {
            if (Essentials.config.landEnabled && player != null)
            {
                // check whitelist first.
                String name = evt.getWorld().getBlockState(evt.getPos()).getBlock().getRegistryName().toString();
                if (blockBreakWhiteList.contains(name)) { return; }
                // Chunk Coordinate
                Coordinate c = Coordinate.getChunkCoordFromWorldCoord(evt.getPos(),
                        player.getEntityWorld().provider.getDimension());
                // Block coordinate
                Coordinate b = new Coordinate(evt.getPos(), player.getEntityWorld().provider.getDimension());
                LandTeam team = LandManager.getInstance().getLandOwner(c);

                // Check permission for breaking wilderness, then return.
                if (team == null)
                {
                    if (PermissionAPI.hasPermission(player, PERMBREAKWILD)) { return; }
                    // TODO better message.
                    player.sendMessage(new StringTextComponent("Cannot break that."));
                    evt.setCanceled(true);
                    return;

                }
                // Check if the team allows fakeplayers
                if (team.fakePlayers && player instanceof FakePlayer)
                {

                }
                else // Otherwise check normal behaviour
                {
                    // Treat relation break perm as owning the land.
                    boolean owns = team.canBreakBlock(player.getUniqueID(), b);
                    if (owns && !PermissionAPI.hasPermission(player, PERMBREAKOWN))
                    {
                        sendMessage(player, team, DENY);
                        evt.setCanceled(true);
                        return;
                    }
                    if (!owns && !PermissionAPI.hasPermission(player, PERMBREAKOTHER))
                    {
                        sendMessage(player, team, DENY);
                        evt.setCanceled(true);
                        return;
                    }
                }
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public void trample(FarmlandTrampleEvent evt)
        {
            if (evt.getEntity().getEntityWorld().isRemote) return;
            if (!Essentials.config.landEnabled) return;
            Coordinate c = Coordinate.getChunkCoordFromWorldCoord(evt.getPos(), evt.getWorld().provider.getDimension());
            Entity trampler = evt.getEntity();
            LandTeam team = LandManager.getInstance().getLandOwner(c);
            if (team == null) return;
            PlayerEntity player = null;
            if (trampler instanceof PlayerEntity) player = (PlayerEntity) trampler;
            if (trampler instanceof IEntityOwnable && ((IEntityOwnable) trampler).getOwner() instanceof PlayerEntity)
                player = (PlayerEntity) ((IEntityOwnable) trampler).getOwner();
            checkBreak(evt, player);
            if (!evt.isCanceled() && ThutEssentials.instance.config.log_interactions)
            {
                ThutEssentials.logger.log(Level.FINER,
                        c + " trample " + evt.getPos() + " " + trampler.getUniqueID() + " " + trampler.getName());
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public void placeBlocks(PlaceEvent evt)
        {
            if (evt.getPlayer().getEntityWorld().isRemote) return;
            if (!Essentials.config.landEnabled) return;
            checkPlace(evt, evt.getPlayer());
            if (!evt.isCanceled() && Essentials.config.log_interactions)
            {
                Coordinate c = Coordinate.getChunkCoordFromWorldCoord(evt.getPos(),
                        evt.getWorld().provider.getDimension());
                ThutEssentials.logger.log(Level.FINER, c + " place " + evt.getPos() + " " + evt.getPlacedAgainst() + " "
                        + evt.getPlacedBlock() + " " + evt.getPlayer().getUniqueID() + " " + evt.getPlayer().getName());
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public void BreakBlock(BreakEvent evt)
        {
            if (evt.getPlayer().getEntityWorld().isRemote) return;
            if (!Essentials.config.landEnabled) return;
            PlayerEntity player = evt.getPlayer();
            checkBreak(evt, player);
            if (!evt.isCanceled() && Essentials.config.log_interactions)
            {
                Coordinate c = Coordinate.getChunkCoordFromWorldCoord(evt.getPos(),
                        evt.getWorld().provider.getDimension());
                ThutEssentials.logger.log(Level.FINER, c + " break " + evt.getPos() + " " + evt.getState() + " "
                        + evt.getPlayer().getUniqueID() + " " + evt.getPlayer().getName());
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public void bucket(FillBucketEvent event)
        {
            if (event.getEntityPlayer().getEntityWorld().isRemote) return;
            if (!Essentials.config.landEnabled) return;
            BlockPos pos = event.getEntityPlayer().getPosition();
            if (event.getTarget() != null)
            {
                pos = event.getTarget().getBlockPos().offset(event.getTarget().sideHit);
            }
            PlayerEntity player = event.getEntityPlayer();
            BlockEvent evt = new BreakEvent(event.getWorld(), pos, event.getWorld().getBlockState(pos), player);
            checkPlace(evt, player);
            checkBreak(evt, player);
            if (evt.isCanceled()) event.setCanceled(true);
            else if (Essentials.config.log_interactions)
            {
                Coordinate c = Coordinate.getChunkCoordFromWorldCoord(evt.getPos(),
                        evt.getWorld().provider.getDimension());
                ThutEssentials.logger.log(Level.FINER, c + " bucket " + evt.getPos() + " " + player.getUniqueID() + " "
                        + player.getName() + " " + event.getFilledBucket() + " " + event.getEmptyBucket());
            }
        }
    }

    public static class EntityEventHandler
    {
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public void death(LivingDeathEvent evt)
        {
            if (evt.getEntity().getEntityWorld().isRemote) return;
            if (!Essentials.config.landEnabled) return;

            // Cleanup the entity from protected mobs.
            UUID id = evt.getEntity().getUniqueID();
            if (LandManager.getInstance()._protected_mobs.containsKey(id))
            {
                LandTeam team = LandManager.getInstance()._protected_mobs.remove(id);
                team.protected_mobs.remove(id);
                LandSaveHandler.saveTeam(team.teamName);
            }

            // Cleanup the entity from public mobs.
            if (LandManager.getInstance()._public_mobs.containsKey(id))
            {
                LandTeam team = LandManager.getInstance()._public_mobs.remove(id);
                team.public_mobs.remove(id);
                LandSaveHandler.saveTeam(team.teamName);
            }
        }

        @SubscribeEvent
        public void update(LivingUpdateEvent evt)
        {
            if (evt.getEntity().getEntityWorld().isRemote) return;
            if (!Essentials.config.landEnabled) return;
            if (evt.getEntityLiving() instanceof ServerPlayerEntity && evt.getEntityLiving().ticksExisted > 10)
            {
                ServerPlayerEntity player = (ServerPlayerEntity) evt.getEntityLiving();
                BlockPos here;
                BlockPos old;
                here = new BlockPos(player.chasingPosX, player.chasingPosY, player.chasingPosZ);
                old = new BlockPos(player.prevChasingPosX, player.prevChasingPosY, player.prevChasingPosZ);
                Coordinate newChunk = Coordinate.getChunkCoordFromWorldCoord(here,
                        player.getEntityWorld().provider.getDimension());
                Coordinate oldChunk = Coordinate.getChunkCoordFromWorldCoord(old,
                        player.getEntityWorld().provider.getDimension());
                if (newChunk.equals(oldChunk) || !Essentials.config.landEnabled) return;
                boolean isNewOwned = LandManager.getInstance().isOwned(newChunk);
                boolean isOldOwned = LandManager.getInstance().isOwned(oldChunk);

                if (isNewOwned || isOldOwned)
                {
                    LandTeam team = LandManager.getInstance().getLandOwner(newChunk);
                    LandTeam team1 = LandManager.getInstance().getLandOwner(oldChunk);
                    if (!lastLeaveMessage.containsKey(evt.getEntity().getUniqueID()))
                        lastLeaveMessage.put(evt.getEntity().getUniqueID(), System.currentTimeMillis() - 1);
                    if (!lastEnterMessage.containsKey(evt.getEntity().getUniqueID()))
                        lastEnterMessage.put(evt.getEntity().getUniqueID(), System.currentTimeMillis() - 1);

                    if (!isNewOwned && !PermissionAPI.hasPermission(player, PERMENTERWILD))
                    {
                        player.connection.setPlayerLocation(old.getX() + 0.5, old.getY(), old.getZ() + 0.5,
                                player.rotationYaw, player.rotationPitch);
                        // TODO better message.
                        evt.getEntity().sendMessage(new StringTextComponent("You may not enter there."));
                        return;
                    }
                    boolean owns = team != null && team.isMember(player);
                    if (isNewOwned && owns && !PermissionAPI.hasPermission(player, PERMENTEROWN))
                    {
                        player.connection.setPlayerLocation(old.getX() + 0.5, old.getY(), old.getZ() + 0.5,
                                player.rotationYaw, player.rotationPitch);
                        // TODO better message.
                        evt.getEntity().sendMessage(new StringTextComponent("You may not enter there."));
                        return;
                    }
                    else if (isNewOwned && !owns && !PermissionAPI.hasPermission(player, PERMENTEROTHER))
                    {
                        player.connection.setPlayerLocation(old.getX() + 0.5, old.getY(), old.getZ() + 0.5,
                                player.rotationYaw, player.rotationPitch);
                        // TODO better message.
                        evt.getEntity().sendMessage(new StringTextComponent("You may not enter there."));
                        return;
                    }

                    messages:
                    {
                        if (team != null)
                        {
                            if (team.equals(team1)) break messages;
                            if (team1 != null)
                            {
                                long last = lastLeaveMessage.get(evt.getEntity().getUniqueID());
                                if (last < System.currentTimeMillis())
                                {
                                    sendMessage(player, team1, EXIT);
                                    lastLeaveMessage.put(evt.getEntity().getUniqueID(),
                                            System.currentTimeMillis() + 100);
                                }
                            }
                            long last = lastEnterMessage.get(evt.getEntity().getUniqueID());
                            if (last < System.currentTimeMillis())
                            {
                                sendMessage(player, team, ENTER);
                                lastLeaveMessage.put(evt.getEntity().getUniqueID(), System.currentTimeMillis() + 100);
                            }
                        }
                        else
                        {
                            long last = lastLeaveMessage.get(evt.getEntity().getUniqueID());
                            if (last < System.currentTimeMillis())
                            {
                                sendMessage(player, team1, EXIT);
                                lastLeaveMessage.put(evt.getEntity().getUniqueID(), System.currentTimeMillis() + 100);
                            }
                        }
                    }
                }
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public void attack(AttackEntityEvent evt)
        {
            if (evt.getEntity().getEntityWorld().isRemote || !Essentials.config.landEnabled) return;
            Coordinate c = Coordinate.getChunkCoordFromWorldCoord(evt.getTarget().getPosition(),
                    evt.getEntityPlayer().getEntityWorld().provider.getDimension());
            LandTeam owner = LandManager.getInstance().getLandOwner(c);

            // TODO possible perms for attacking things in unclaimed land?
            if (owner == null) return;

            // No player damage allowed here.
            if (evt.getTarget() instanceof PlayerEntity && owner.noPlayerDamage)
            {
                evt.setCanceled(true);
                return;
            }
            PlayerEntity attacker = evt.getEntityPlayer();

            // Check if the team allows fakeplayers
            if (owner.fakePlayers && evt.getEntityPlayer() instanceof FakePlayer) return;

            BlockPos pos = evt.getTarget().getPosition();
            Coordinate b = Coordinate.getChunkCoordFromWorldCoord(pos.getX(), pos.getY(), pos.getZ(),
                    attacker.dimension);

            // Check if item frame
            if (evt.getTarget() instanceof EntityItemFrame && !owner.canBreakBlock(attacker.getUniqueID(), b))
            {
                evt.setCanceled(true);
                return;
            }

            // If mob is protected, do not allow the attack, even if by owner.
            if (owner.protected_mobs.contains(evt.getTarget().getUniqueID()))
            {
                evt.setCanceled(true);
                return;
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public void attack(LivingAttackEvent evt)
        {
            if (evt.getEntity().getEntityWorld().isRemote) return;
            if (!Essentials.config.landEnabled) return;
            Coordinate c = Coordinate.getChunkCoordFromWorldCoord(evt.getEntity().getPosition(),
                    evt.getEntity().getEntityWorld().provider.getDimension());
            LandTeam owner = LandManager.getInstance().getLandOwner(c);

            // TODO maybe add a perm for combat in non-claimed land?
            if (owner == null) return;

            if (evt.getEntity() instanceof PlayerEntity)
            {
                LandTeam players = LandManager.getTeam(evt.getEntity());
                // Check if player is protected via friendly fire settings.
                if (!players.friendlyFire)
                {
                    Entity damageSource = evt.getSource().getTrueSource();
                    if (damageSource instanceof PlayerEntity && sameTeam(damageSource, evt.getEntity()))
                    {
                        evt.setCanceled(true);
                        return;
                    }
                }

                // Check if player is protected by team settings
                if (owner.noPlayerDamage)
                {
                    evt.setCanceled(true);
                    return;
                }
            }

            // Check if the team allows fakeplayers
            if (owner.fakePlayers && evt.getSource().getTrueSource() instanceof FakePlayer) return;

            // check if entity is protected by team
            if (owner.protected_mobs.contains(evt.getEntity().getUniqueID()))
            {
                evt.setCanceled(true);
                return;
            }
        }

        @SubscribeEvent
        public void projectileImpact(ProjectileImpactEvent evt)
        {
            if (evt.getEntity().getEntityWorld().isRemote) return;
            if (!Essentials.config.shopsEnabled) return;
            if (evt.getRayTraceResult() == null) return;
            if (evt.getRayTraceResult().entityHit == null) return;

            Entity target = evt.getRayTraceResult().entityHit;

            Coordinate c = Coordinate.getChunkCoordFromWorldCoord(evt.getEntity().getPosition(),
                    evt.getEntity().getEntityWorld().provider.getDimension());
            LandTeam owner = LandManager.getInstance().getLandOwner(c);

            // TODO maybe add a perm for combat in non-claimed land?
            if (owner == null) return;

            // Check if player is protected by team settings.
            if (owner.noPlayerDamage && target instanceof PlayerEntity)
            {
                evt.setCanceled(true);
                return;
            }

            // Protect item frames from projectiles regardless.
            if (target instanceof EntityItemFrame && owner.protectFrames)
            {
                evt.setCanceled(true);
                return;
            }

            // check if entity is protected by team
            if (owner.protected_mobs.contains(target.getUniqueID()))
            {
                evt.setCanceled(true);
                return;
            }

        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public void attack(LivingHurtEvent evt)
        {
            if (evt.getEntity().getEntityWorld().isRemote) return;
            if (!Essentials.config.landEnabled) return;
            Coordinate c = Coordinate.getChunkCoordFromWorldCoord(evt.getEntity().getPosition(),
                    evt.getEntity().getEntityWorld().provider.getDimension());
            LandTeam owner = LandManager.getInstance().getLandOwner(c);

            // TODO maybe add a perm for combat in non-claimed land?
            if (owner == null) return;

            // Check if player is protected by team settings.
            if (owner.noPlayerDamage && evt.getEntity() instanceof PlayerEntity)
            {
                evt.setCanceled(true);
                return;
            }

            // check if entity is protected by team
            if (owner.protected_mobs.contains(evt.getEntity().getUniqueID()))
            {
                evt.setCanceled(true);
                return;
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public void spawn(LivingSpawnEvent.SpecialSpawn evt)
        {
            if (!Essentials.config.landEnabled) return;
            if (evt.getEntity().getEntityWorld().isRemote) return;
            Coordinate c = Coordinate.getChunkCoordFromWorldCoord(evt.getEntity().getPosition(),
                    evt.getEntity().getEntityWorld().provider.getDimension());
            LandTeam owner = LandManager.getInstance().getLandOwner(c);
            if (owner == null) return;
            if (owner.noMobSpawn)
            {
                evt.setResult(Result.DENY);
                return;
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public void spawn(LivingSpawnEvent.CheckSpawn evt)
        {
            if (!Essentials.config.landEnabled) return;
            if (evt.getEntity().getEntityWorld().isRemote) return;
            Coordinate c = Coordinate.getChunkCoordFromWorldCoord(evt.getEntity().getPosition(),
                    evt.getEntity().getEntityWorld().provider.getDimension());
            LandTeam owner = LandManager.getInstance().getLandOwner(c);
            if (owner == null) return;
            if (owner.noMobSpawn)
            {
                evt.setResult(Result.DENY);
                return;
            }
        }

    }

    public static class InteractEventHandler
    {
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public void interact(PlayerInteractEvent.LeftClickBlock evt)
        {
            if (evt.getEntity().getEntityWorld().isRemote) return;
            if (!Essentials.config.landEnabled) return;
            // Chunk Coordinate
            Coordinate c = Coordinate.getChunkCoordFromWorldCoord(evt.getPos(),
                    evt.getEntityPlayer().getEntityWorld().provider.getDimension());
            // Block coordinate
            Coordinate b = new Coordinate(evt.getPos(), evt.getEntityPlayer().getEntityWorld().provider.getDimension());
            LandTeam owner = LandManager.getInstance().getLandOwner(c);

            // TODO potentially have perms for unowned use here?
            if (owner == null) return;

            // Check if the team allows fakeplayers
            if (owner.fakePlayers && evt.getEntityPlayer() instanceof FakePlayer) return;

            // check if this is in the global whitelist.
            Block block = evt.getWorld().getBlockState(evt.getPos()).getBlock();
            String name = block.getRegistryName().toString();
            if (blockBreakWhiteList.contains(name)) { return; }

            // Check if we own this, or we have team relation permissions for
            // this.
            if (owner.canUseStuff(evt.getEntityPlayer().getUniqueID(), b)) { return; }

            // Check if this is a public location
            Coordinate blockLoc = new Coordinate(evt.getPos(),
                    evt.getEntityPlayer().getEntityWorld().provider.getDimension());
            if (!LandManager.getInstance().isPublic(blockLoc, owner))
            {
                evt.setUseBlock(Result.DENY);
                evt.setCanceled(true);
                if (!evt.getWorld().isRemote) sendMessage(evt.getEntity(), owner, DENY);
                if (Essentials.config.log_interactions)
                    ThutEssentials.logger.log(Level.FINER, "Cancelled interact due to not allowed to left click that."
                            + c + " " + evt.getEntityPlayer().getUniqueID() + " " + evt.getEntityPlayer().getName());
            }
            evt.setUseItem(Result.DENY);
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public void interact(PlayerInteractEvent.EntityInteract evt)
        {
            if (evt.getSide() == Side.CLIENT) return;
            if (!Essentials.config.landEnabled) return;
            // Chunk Coordinate
            Coordinate c = Coordinate.getChunkCoordFromWorldCoord(evt.getPos(),
                    evt.getEntityPlayer().getEntityWorld().provider.getDimension());
            // Block coordinate
            Coordinate b = new Coordinate(evt.getPos(), evt.getEntityPlayer().getEntityWorld().provider.getDimension());
            LandTeam owner = LandManager.getInstance().getLandOwner(c);
            if (owner == null) return;

            // Check if the team allows fakeplayers
            if (owner.fakePlayers && evt.getEntityPlayer() instanceof FakePlayer) return;

            // If the player owns it, they can toggle whether the entity is
            // protected or not, Only team admins can do this.
            if (owner.canUseStuff(evt.getEntityPlayer().getUniqueID(), b) && owner.isAdmin(evt.getEntityPlayer()))
            {
                // No protecting players.
                if (evt.getTarget() instanceof PlayerEntity) return;

                // check if player is holding a public toggle.
                if (!evt.getWorld().isRemote && evt.getItemStack() != null
                        && evt.getItemStack().getDisplayName().equals("Public Toggle")
                        && evt.getEntityPlayer().isSneaking())
                {
                    // If so, toggle whether the entity is public.
                    if (owner.public_mobs.contains(evt.getTarget().getUniqueID()))
                    {
                        evt.getEntityPlayer().sendMessage(
                                new StringTextComponent("Removed from public: " + evt.getTarget().getName()));
                        LandManager.getInstance().toggleMobPublic(evt.getTarget().getUniqueID(), owner);
                    }
                    else
                    {
                        evt.getEntityPlayer()
                                .sendMessage(new StringTextComponent("Added to Public: " + evt.getTarget().getName()));
                        LandManager.getInstance().toggleMobPublic(evt.getTarget().getUniqueID(), owner);
                    }
                    evt.setCanceled(true);
                    if (Essentials.config.log_interactions)
                        ThutEssentials.logger.log(Level.FINER, "Cancelled interact due to toggling public mob." + c
                                + " " + evt.getEntityPlayer().getUniqueID() + " " + evt.getEntityPlayer().getName());
                    return;
                }
                // check if player is holding a protect toggle.
                if (!evt.getWorld().isRemote && evt.getItemStack() != null
                        && evt.getItemStack().getDisplayName().equals("Protect Toggle")
                        && evt.getEntityPlayer().isSneaking()
                        && PermissionAPI.hasPermission(evt.getEntityPlayer(), PERMPROTECTMOB))
                {
                    // If so, toggle whether the entity is protected.
                    if (owner.protected_mobs.contains(evt.getTarget().getUniqueID()))
                    {
                        evt.getEntityPlayer().sendMessage(
                                new StringTextComponent("Removed from protected: " + evt.getTarget().getName()));
                        LandManager.getInstance().toggleMobProtect(evt.getTarget().getUniqueID(), owner);
                    }
                    else
                    {
                        evt.getEntityPlayer().sendMessage(
                                new StringTextComponent("Added to protected: " + evt.getTarget().getName()));
                        LandManager.getInstance().toggleMobProtect(evt.getTarget().getUniqueID(), owner);
                    }
                    evt.setCanceled(true);
                    if (Essentials.config.log_interactions)
                        ThutEssentials.logger.log(Level.FINER, "Cancelled interact due to toggling protected mob." + c
                                + " " + evt.getEntityPlayer().getUniqueID() + " " + evt.getEntityPlayer().getName());
                    return;
                }
            }

            // If all public, don't bother checking things below.
            if (owner.allPublic) return;

            // Check the teams relations settings
            if (owner.canUseStuff(evt.getEntityPlayer().getUniqueID(), b)) return;

            // If not public, no use of mob.
            if (!owner.public_mobs.contains(evt.getTarget().getUniqueID()))
            {
                evt.setCanceled(true);
                if (Essentials.config.log_interactions)
                    ThutEssentials.logger.log(Level.FINER, "Cancelled interact due to not public mob." + c + " "
                            + evt.getEntityPlayer().getUniqueID() + " " + evt.getEntityPlayer().getName());
                return;
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public void interact(PlayerInteractEvent.RightClickItem evt)
        {
            if (evt.getSide() == Side.CLIENT) return;
            if (evt.getItemStack().getItem() instanceof ItemFood || evt.getItemStack().getItem() == Items.WRITTEN_BOOK
                    || evt.getItemStack().getItem() == Items.WRITABLE_BOOK || !Essentials.config.landEnabled
                    || evt.getEntity().world.isRemote)
                return;

            PlayerEntity player = evt.getEntityPlayer();
            String name = evt.getItemStack().getItem().getRegistryName().toString();

            // Check global config for whitelisted items.
            if (itemUseWhitelist.contains(name)) { return; }
            // Check if any mods decide that the item should be whitelisted
            // regardless of team.
            if (MinecraftForge.EVENT_BUS.post(
                    new DenyItemUseEvent(evt.getEntity(), evt.getItemStack(), UseType.RIGHTCLICKBLOCK))) { return; }
            // Chunk Coordinate
            Coordinate c = Coordinate.getChunkCoordFromWorldCoord(evt.getPos(),
                    player.getEntityWorld().provider.getDimension());
            // Block coordinate
            Coordinate b = new Coordinate(evt.getPos(), player.getEntityWorld().provider.getDimension());
            boolean ownedLand = LandManager.getInstance().isOwned(c);
            if (!ownedLand)
            {
                if (PermissionAPI.hasPermission(player, PERMUSEITEMWILD)) { return; }
                // TODO better message.
                player.sendMessage(new StringTextComponent("Cannot use that."));
                evt.setCanceled(true);
                ((ServerPlayerEntity) player).sendAllContents(player.inventoryContainer,
                        player.inventoryContainer.inventoryItemStacks);
                return;

            }
            LandTeam team = LandManager.getInstance().getLandOwner(c);

            // If all public, don't bother checking things below.
            if (team.allPublic) return;

            // Check if the team allows fakeplayers
            if (team.fakePlayers && evt.getEntityPlayer() instanceof FakePlayer) return;

            // Treat the relations settings as whether the player owns this.
            boolean owns = team.canUseStuff(player.getUniqueID(), b);

            // check permission
            String perm = owns ? PERMUSEITEMOWN : PERMUSEITEMOTHER;
            boolean permission = PermissionAPI.hasPermission(player, perm);
            if (!permission)
            {
                sendMessage(player, team, DENY);
                evt.setResult(Result.DENY);
                evt.setCanceled(true);
                ((ServerPlayerEntity) player).sendAllContents(player.inventoryContainer,
                        player.inventoryContainer.inventoryItemStacks);
                return;
            }
            // Return here if we own this.
            else if (owns) return;

            // Allow use if public block.
            Coordinate blockLoc = new Coordinate(evt.getPos(), player.getEntityWorld().provider.getDimension());
            if (LandManager.getInstance().isPublic(blockLoc, team))
            {
                evt.setResult(Result.DENY);
                return;
            }

            // If we got to here, deny the use.
            sendMessage(player, team, DENY);
            evt.setResult(Result.DENY);
            evt.setCanceled(true);
            ((ServerPlayerEntity) player).sendAllContents(player.inventoryContainer,
                    player.inventoryContainer.inventoryItemStacks);

        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public void interact(PlayerInteractEvent.RightClickBlock evt)
        {
            if (evt.getSide() == Side.CLIENT) return;
            if (!Essentials.config.landEnabled) return;
            // Chunk Coordinate
            Coordinate c = Coordinate.getChunkCoordFromWorldCoord(evt.getPos(),
                    evt.getEntityPlayer().getEntityWorld().provider.getDimension());
            // Block coordinate
            Coordinate b = new Coordinate(evt.getPos(), evt.getEntityPlayer().getEntityWorld().provider.getDimension());
            LandTeam owner = LandManager.getInstance().getLandOwner(c);

            PlayerEntity player = evt.getEntityPlayer();
            if (owner == null)
            {
                if (!PermissionAPI.hasPermission(player, PERMUSEBLOCKWILD))
                {
                    // TODO better message.
                    player.sendMessage(new StringTextComponent("Cannot use that."));
                    evt.setCanceled(true);
                    evt.setUseBlock(Result.DENY);
                    evt.setUseItem(Result.DENY);
                    if (player.inventoryContainer != null && player.inventory != null) ((ServerPlayerEntity) player)
                            .sendAllContents(player.inventoryContainer, player.inventoryContainer.inventoryItemStacks);
                    if (Essentials.config.log_interactions)
                        ThutEssentials.logger.log(Level.FINER, "Cancelled interact due to not allowed to use wild." + c
                                + " " + evt.getEntityPlayer().getUniqueID() + " " + evt.getEntityPlayer().getName());
                    return;
                }
                return;
            }
            // If all public, don't bother checking things below.
            if (owner.allPublic) return;

            Block block = null;
            IBlockState state = evt.getWorld().getBlockState(evt.getPos());
            block = state.getBlock();
            String name = block.getRegistryName().toString();
            if (blockUseWhiteList.contains(name)) { return; }
            boolean shouldPass = true;

            // Check if the team allows fakeplayers
            if (owner.fakePlayers && evt.getEntityPlayer() instanceof FakePlayer) return;

            // Check permission, Treat relation public perm as if we own this
            // for this check.
            boolean owns = owner.canUseStuff(player.getUniqueID(), b);

            // Check if the block is public.
            Coordinate blockLoc = new Coordinate(evt.getPos(),
                    evt.getEntityPlayer().getEntityWorld().provider.getDimension());
            boolean freeuse = LandManager.getInstance().isPublic(blockLoc, owner);
            owns = owns || freeuse;

            String perm = owns ? PERMUSEBLOCKOWN : PERMUSEBLOCKOTHER;
            boolean permission = PermissionAPI.hasPermission(player, perm);

            if (!permission)
            {
                sendMessage(player, owner, DENY);
                evt.setCanceled(true);
                evt.setUseBlock(Result.DENY);
                evt.setUseItem(Result.DENY);
                ((ServerPlayerEntity) player).sendAllContents(player.inventoryContainer,
                        player.inventoryContainer.inventoryItemStacks);
                if (Essentials.config.log_interactions) ThutEssentials.logger.log(Level.FINER,
                        "Cancelled interact due to not allowed to use. owns?: " + owns + ", " + c + " "
                                + evt.getEntityPlayer().getUniqueID() + " " + evt.getEntityPlayer().getName());
                return;
            }
            // If we own this, we can return here, first check public toggle
            // though.
            if (owns)
            {
                // Do stuff for toggling public
                if (!evt.getWorld().isRemote && evt.getItemStack() != null
                        && evt.getItemStack().getDisplayName().equals("Public Toggle")
                        && evt.getEntityPlayer().isSneaking() && !owner.allPublic
                        && LandManager.getInstance().isAdmin(evt.getEntityPlayer().getUniqueID()))
                {
                    blockLoc = new Coordinate(evt.getPos(),
                            evt.getEntityPlayer().getEntityWorld().provider.getDimension());
                    if (LandManager.getInstance().isPublic(blockLoc, owner))
                    {
                        evt.getEntityPlayer().sendMessage(new StringTextComponent("Set Block to Team Only"));
                        LandManager.getInstance().unsetPublic(blockLoc, owner);
                    }
                    else
                    {
                        evt.getEntityPlayer().sendMessage(new StringTextComponent("Set Block to Public Use"));
                        LandManager.getInstance().setPublic(blockLoc, owner);
                    }
                    evt.setCanceled(true);
                    if (Essentials.config.log_interactions)
                        ThutEssentials.logger.log(Level.FINER, "Cancelled interact due to public toggling. " + c + " "
                                + evt.getEntityPlayer().getUniqueID() + " " + evt.getEntityPlayer().getName());
                }
                // Do stuff for toggling break
                if (!evt.getWorld().isRemote && evt.getItemStack() != null
                        && evt.getItemStack().getDisplayName().equals("Break Toggle")
                        && evt.getEntityPlayer().isSneaking()
                        && LandManager.getInstance().isAdmin(evt.getEntityPlayer().getUniqueID()))
                {
                    blockLoc = new Coordinate(evt.getPos(),
                            evt.getEntityPlayer().getEntityWorld().provider.getDimension());
                    if (owner.anyBreakSet.contains(blockLoc))
                    {
                        evt.getEntityPlayer().sendMessage(new StringTextComponent("Set Block to Team Breaking Only"));
                        owner.anyBreakSet.remove(blockLoc);
                    }
                    else
                    {
                        evt.getEntityPlayer().sendMessage(new StringTextComponent("Set Block to Public Breaking"));
                        owner.anyBreakSet.add(blockLoc);
                    }
                    LandSaveHandler.saveTeam(owner.teamName);
                    evt.setCanceled(true);
                    if (Essentials.config.log_interactions)
                        ThutEssentials.logger.log(Level.FINER, "Cancelled interact due to break toggling. " + c + " "
                                + evt.getEntityPlayer().getUniqueID() + " " + evt.getEntityPlayer().getName());
                }
                // Do stuff for toggling place
                if (!evt.getWorld().isRemote && evt.getItemStack() != null
                        && evt.getItemStack().getDisplayName().equals("Place Toggle")
                        && evt.getEntityPlayer().isSneaking()
                        && LandManager.getInstance().isAdmin(evt.getEntityPlayer().getUniqueID()))
                {
                    blockLoc = new Coordinate(evt.getPos(),
                            evt.getEntityPlayer().getEntityWorld().provider.getDimension());
                    if (owner.anyPlaceSet.contains(blockLoc))
                    {
                        evt.getEntityPlayer().sendMessage(new StringTextComponent("Set Block to Team Placing Only"));
                        owner.anyPlaceSet.remove(blockLoc);
                    }
                    else
                    {
                        evt.getEntityPlayer().sendMessage(new StringTextComponent("Set Block to Public Placing"));
                        owner.anyPlaceSet.add(blockLoc);
                    }
                    LandSaveHandler.saveTeam(owner.teamName);
                    evt.setCanceled(true);
                    if (Essentials.config.log_interactions)
                        ThutEssentials.logger.log(Level.FINER, "Cancelled interact due to place toggling. " + c + " "
                                + evt.getEntityPlayer().getUniqueID() + " " + evt.getEntityPlayer().getName());
                }
                return;
            }

            // Check if the block has some custom interaction event, if so, move
            // on
            // through to that if the block is whitelisted.
            if (!(block.hasTileEntity(state)) && !evt.getWorld().isRemote)
            {
                shouldPass = MinecraftForge.EVENT_BUS
                        .post(new DenyItemUseEvent(evt.getEntity(), evt.getItemStack(), UseType.RIGHTCLICKBLOCK));
                name = evt.getItemStack().getItem().getRegistryName().toString();
                shouldPass = shouldPass || itemUseWhitelist.contains(name);
                boolean interact = true;
                if (shouldPass)
                {
                    BlockPos pos = evt.getPos();
                    Vec3d vec = evt.getHitVec();
                    if (vec == null) vec = new Vec3d(0, 0, 0);
                    interact = CompatWrapper.interactWithBlock(block, evt.getWorld(), pos, state, evt.getEntityPlayer(),
                            evt.getHand(), null, evt.getFace(), (float) vec.x, (float) vec.y, (float) vec.z);
                }
                if (!interact && shouldPass) return;
            }

            // If we got here, then nothing allows use.
            sendMessage(player, owner, DENY);
            evt.setCanceled(true);
            evt.setUseBlock(Result.DENY);
            evt.setUseItem(Result.DENY);
            ((ServerPlayerEntity) player).sendAllContents(player.inventoryContainer,
                    player.inventoryContainer.inventoryItemStacks);
            if (Essentials.config.log_interactions)
                ThutEssentials.logger.log(Level.FINER, "Cancelled interact due to not allowed to use block." + c + " "
                        + evt.getEntityPlayer().getUniqueID() + " " + evt.getEntityPlayer().getName());
        }
    }

    public static class ChunkLoadHandler
    {
        public static HashMap<Coordinate, Ticket> chunks = Maps.newHashMap();

        public static boolean removeChunks(Coordinate location)
        {
            Ticket ticket = chunks.remove(location);
            if (ticket != null)
            {
                ForgeChunkManager.releaseTicket(ticket);
                return true;
            }
            return false;
        }

        public static boolean addChunks(World world, Coordinate location, UUID placer)
        {
            if (!Essentials.config.chunkLoading) return false;

            boolean found = chunks.containsKey(location);
            try
            {
                if (!found)
                {
                    Ticket ticket = ForgeChunkManager.requestPlayerTicket(ThutEssentials.instance, placer.toString(),
                            world, ForgeChunkManager.Type.NORMAL);
                    int[] loc = { location.x, location.z };
                    ticket.getModData().setIntArray("pos", loc);
                    ChunkPos chunk = new ChunkPos(location.x, location.z);
                    ThutEssentials.logger.log(Level.FINER, "Forcing Chunk at " + location);
                    ForgeChunkManager.forceChunk(ticket, chunk);
                    chunks.put(location, ticket);
                    return true;
                }
            }
            catch (Throwable e)
            {
                ThutEssentials.logger.log(Level.FINER, "Error adding chunks to load.", new Exception(e));
            }
            return false;
        }
    }

    public static boolean sameTeam(Entity a, Entity b)
    {
        return LandManager.getTeam(a) == LandManager.getTeam(b);
    }

    public static final String     PERMBREAKWILD        = "thutessentials.land.break.unowned";
    public static final String     PERMBREAKOWN         = "thutessentials.land.break.owned.self";
    public static final String     PERMBREAKOTHER       = "thutessentials.land.break.owned.other";

    public static final String     PERMPLACEWILD        = "thutessentials.land.place.unowned";
    public static final String     PERMPLACEOWN         = "thutessentials.land.place.owned.self";
    public static final String     PERMPLACEOTHER       = "thutessentials.land.place.owned.other";

    public static final String     PERMUSEITEMWILD      = "thutessentials.land.useitem.unowned";
    public static final String     PERMUSEITEMOWN       = "thutessentials.land.useitem.owned.self";
    public static final String     PERMUSEITEMOTHER     = "thutessentials.land.useitem.owned.other";

    public static final String     PERMUSEBLOCKWILD     = "thutessentials.land.useblock.unowned";
    public static final String     PERMUSEBLOCKOWN      = "thutessentials.land.useblock.owned.self";
    public static final String     PERMUSEBLOCKOTHER    = "thutessentials.land.useblock.owned.other";

    public static final String     PERMENTERWILD        = "thutessentials.land.enter.unowned";
    public static final String     PERMENTEROWN         = "thutessentials.land.enter.owned.self";
    public static final String     PERMENTEROTHER       = "thutessentials.land.enter.owned.other";

    public static final String     PERMCREATETEAM       = "thutessentials.teams.create";
    public static final String     PERMJOINTEAMINVITED  = "thutessentials.teams.join.invite";
    public static final String     PERMJOINTEAMNOINVITE = "thutessentials.teams.join.force";

    public static final String     PERMPROTECTMOB       = "thutessentials.teams.protect.mob";

    public static final String     PERMUNCLAIMOTHER     = "thutessentials.land.unclaim.owned.other";

    static Map<UUID, Long>         lastLeaveMessage     = Maps.newHashMap();
    static Map<UUID, Long>         lastEnterMessage     = Maps.newHashMap();

    private boolean                registered           = false;
    protected InteractEventHandler interact_handler     = new InteractEventHandler();
    protected EntityEventHandler   entity_handler       = new EntityEventHandler();
    protected BlockEventHandler    block_handler        = new BlockEventHandler();

    public Set<UUID>               checked              = Sets.newHashSet();
    public List<GameProfile>       toCheck              = Lists.newArrayList();

    public LandEventsHandler()
    {
    }

    public void registerPerms()
    {
        if (registered) return;
        registered = true;
        PermissionAPI.registerNode(PERMBREAKWILD, DefaultPermissionLevel.ALL,
                "Can the player break blocks in unowned land.");
        PermissionAPI.registerNode(PERMBREAKOWN, DefaultPermissionLevel.ALL,
                "Can the player break blocks in their own land.");
        PermissionAPI.registerNode(PERMBREAKOTHER, DefaultPermissionLevel.OP,
                "Can the player break blocks in other player's land.");

        PermissionAPI.registerNode(PERMPLACEWILD, DefaultPermissionLevel.ALL,
                "Can the player place blocks in unowned land.");
        PermissionAPI.registerNode(PERMPLACEOWN, DefaultPermissionLevel.ALL,
                "Can the player place blocks in their own land.");
        PermissionAPI.registerNode(PERMPLACEOTHER, DefaultPermissionLevel.OP,
                "Can the player place blocks in other player's land.");

        PermissionAPI.registerNode(PERMUSEITEMWILD, DefaultPermissionLevel.ALL,
                "Can the player use items in unowned land.");
        PermissionAPI.registerNode(PERMUSEITEMOWN, DefaultPermissionLevel.ALL,
                "Can the player use items in their own land.");
        PermissionAPI.registerNode(PERMUSEITEMOTHER, DefaultPermissionLevel.OP,
                "Can the player use items in other player's land.");

        PermissionAPI.registerNode(PERMUSEBLOCKWILD, DefaultPermissionLevel.ALL,
                "Can the player use items in unowned land.");
        PermissionAPI.registerNode(PERMUSEBLOCKOWN, DefaultPermissionLevel.ALL,
                "Can the player use items in their own land.");
        PermissionAPI.registerNode(PERMUSEBLOCKOTHER, DefaultPermissionLevel.OP,
                "Can the player use items in other player's land.");

        PermissionAPI.registerNode(PERMENTERWILD, DefaultPermissionLevel.ALL, "Can the player enter unowned land.");
        PermissionAPI.registerNode(PERMENTEROWN, DefaultPermissionLevel.ALL, "Can the player enter their own land.");
        PermissionAPI.registerNode(PERMENTEROTHER, DefaultPermissionLevel.ALL,
                "Can the player enter other player's land.");

        PermissionAPI.registerNode(PERMCREATETEAM, DefaultPermissionLevel.ALL, "Can the player create a team.");
        PermissionAPI.registerNode(PERMJOINTEAMINVITED, DefaultPermissionLevel.ALL,
                "Can the player join a team with an invite.");
        PermissionAPI.registerNode(PERMJOINTEAMNOINVITE, DefaultPermissionLevel.OP,
                "Can the player join a team without an invite.");

        PermissionAPI.registerNode(PERMPROTECTMOB, DefaultPermissionLevel.ALL,
                "Can the player protect mobs in their team's land.");

        PermissionAPI.registerNode(PERMUNCLAIMOTHER, DefaultPermissionLevel.OP, "Can the player unclaim any land.");

    }

    public void queueUpdate(GameProfile profile)
    {
        if (profile.getId() == null) return;
        if (checked.contains(profile.getId())) return;
        toCheck.add(profile);
    }

    @SubscribeEvent
    public void tick(ServerTickEvent event)
    {
        if (toCheck.isEmpty() || event.phase != Phase.END) return;
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server.getEntityWorld().getTotalWorldTime() % 5 != 0) return;
        GameProfile profile = toCheck.get(0);
        try
        {
            profile = server.getMinecraftSessionService().fillProfileProperties(profile, true);
            if (profile.getName() == null || profile.getId() == null) return;
            server.getPlayerProfileCache().addEntry(profile);
        }
        catch (Exception e)
        {
            return;
        }
        toCheck.remove(0);
        if (profile.getId() != null) checked.add(profile.getId());
    }

    @SubscribeEvent
    public void login(PlayerLoggedInEvent evt)
    {
        PlayerEntity entityPlayer = evt.getPlayer();
        LandTeam team = LandManager.getTeam(entityPlayer);
        team.lastSeen = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getTotalWorldTime();
    }

    @SubscribeEvent
    public void detonate(ExplosionEvent.Detonate evt)
    {
        if (evt.getWorld().isRemote) return;
        List<BlockPos> toRemove = Lists.newArrayList();
        boolean denyBlasts = Essentials.config.denyExplosions;
        if (Essentials.config.landEnabled)
        {
            int dimension = evt.getWorld().provider.getDimension();
            for (BlockPos pos : evt.getAffectedBlocks())
            {
                Coordinate c = Coordinate.getChunkCoordFromWorldCoord(pos, dimension);
                LandTeam owner = LandManager.getInstance().getLandOwner(c);
                boolean deny = denyBlasts;
                if (owner == null) continue;
                deny = deny || owner.noExplosions;
                if (!deny) continue;
                toRemove.add(pos);
            }
        }
        evt.getAffectedBlocks().removeAll(toRemove);
    }

    public void onServerStarted()
    {
        LandSaveHandler.loadGlobalData();
    }

    public void onServerStopped()
    {
        LandManager.clearInstance();
    }

    private static final byte DENY  = 0;
    private static final byte ENTER = 1;
    private static final byte EXIT  = 2;

    private static void sendMessage(Entity player, LandTeam team, byte index)
    {
        ITextComponent message = null;
        switch (index)
        {
        case DENY:
            message = getDenyMessage(team);
            break;
        case ENTER:
            message = getEnterMessage(team);
            break;
        case EXIT:
            message = getExitMessage(team);
            break;
        }
        if (message != null) player.sendMessage(message);
    }

    private static ITextComponent getDenyMessage(LandTeam team)
    {
        if (team != null && !team.denyMessage.isEmpty()) { return new StringTextComponent(team.denyMessage); }
        if (!Essentials.config.defaultMessages) return null;
        return new TranslationTextComponent("msg.team.deny", team.teamName);
    }

    private static ITextComponent getEnterMessage(LandTeam team)
    {
        if (team != null && !team.enterMessage.isEmpty()) { return new StringTextComponent(team.enterMessage); }
        if (!Essentials.config.defaultMessages) return null;
        return new TranslationTextComponent("msg.team.enterLand", team.teamName);
    }

    private static ITextComponent getExitMessage(LandTeam team)
    {
        if (team != null && !team.exitMessage.isEmpty()) { return new StringTextComponent(team.exitMessage); }
        if (!Essentials.config.defaultMessages) return null;
        return new TranslationTextComponent("msg.team.exitLand", team.teamName);
    }
}
