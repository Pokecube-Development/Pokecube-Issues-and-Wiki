package pokecube.core.items.pokecubes.helper;

import java.util.UUID;

import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.entity.PartEntity;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.tasks.IRunnable;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.pokemob.SpawnEvent.SendOut;
import pokecube.core.handlers.Config;
import pokecube.core.handlers.events.EventsHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.items.pokecubes.EntityPokecubeBase;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.PermNodes;
import pokecube.core.utils.Permissions;
import pokecube.core.utils.PokemobTracker;
import pokecube.core.utils.TagNames;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;
import thut.core.common.commands.CommandTools;

public class SendOutManager
{
    public static Vector3 getFreeSpot(final Entity mob, final ServerLevel world, final Vector3 pos,
            final boolean respectRoom)
    {
        AABB box = mob.getBoundingBox();
        if (mob.isMultipartEntity())
        {
            final PartEntity<?>[] parts = mob.getParts();
            box = null;
            // If it has parts, use that for the bounds instead.
            for (final PartEntity<?> part : parts) if (box == null) box = part.getBoundingBox();
            else box = box.minmax(part.getBoundingBox());
            if (box == null) box = mob.getBoundingBox();
            else
            {
                box = box.move(mob.position());
            }
        }
        if (SendOutManager.valid(box, world)) return pos.copy();
        final int size = 10;
        final Vector3 r = Vector3.getNewVector(), rAbs = Vector3.getNewVector(), rHat = Vector3.getNewVector();
        final long start = System.nanoTime();
        // Starts at 1, as 0 is pos
        for (int i = 1; i < size * size * size; i++)
        {
            final byte[] p = Tools.indexArr[i];
            r.set(p);
            rHat.set(r).norm();
            r.scalarMultBy(0.25);
            rAbs.set(r).addTo(pos);
            final long diff = System.nanoTime() - start;
            if (diff > 2e6) break;
            if (!Vector3.isVisibleRange(world, pos, rHat, r.mag())) continue;
            if (SendOutManager.valid(box.move(r.x, r.y, r.z), world)) return rAbs;
        }
        return respectRoom ? null : pos.copy();
    }

    public static boolean valid(final AABB box, final ServerLevel world)
    {
        final Iterable<VoxelShape> colliding = world.getBlockCollisions(null, box);
        return !colliding.iterator().hasNext();
    }

    public static LivingEntity sendOut(final EntityPokecubeBase cube, final boolean summon)
    {
        return SendOutManager.sendOut(cube, summon, true);
    }

    public static LivingEntity sendOut(final EntityPokecubeBase cube, final boolean summon, final boolean respectRoom)
    {
        if (cube.getCommandSenderWorld().isClientSide || cube.isReleasing()) return null;
        final ServerLevel world = (ServerLevel) cube.getCommandSenderWorld();
        final Entity mob = PokecubeManager.itemToMob(cube.getItem(), cube.getCommandSenderWorld());

        if (mob == null) return null;

        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        final Config config = PokecubeCore.getConfig();

        // Next check some conditions for whether the sendout can occur.

        final boolean hasMob = mob != null;
        final boolean hasPokemob = pokemob != null;
        final boolean isPlayers = cube.shootingEntity instanceof ServerPlayer
                && !(cube.shootingEntity instanceof FakePlayer);
        final ServerPlayer user = isPlayers ? (ServerPlayer) cube.shootingEntity : null;
        final boolean checkPerms = config.permsSendOut && isPlayers;
        boolean hasPerms = true;

        // Check permissions
        if (checkPerms)
        {
            hasPerms = PermNodes.getBooleanPerm(user, Permissions.SENDOUTPOKEMOB);
        }

        // No mob or no perms?, then just refund the item and exit
        if (!hasMob || !hasPerms)
        {
            if (isPlayers && cube.shootingEntity.isAlive())
            {
                Tools.giveItem((Player) cube.shootingEntity, cube.getItem());
                user.displayClientMessage(new TranslatableComponent("pokecube.sendout.fail.noperms.general"), true);
                cube.discard();
            }
            return null;
        }

        // Fix the mob's position.
        Vector3 v = cube.v0.set(cube);

        // If we are breaking out from capture, directly set to old spot
        if (cube.isCapturing) v.set(cube.capturePos);
        // Otherwise look for free room, etc
        else
        {
            // Initialise size gene for the pokemob
            if (pokemob != null) pokemob.onGenesChanged();

            v.set(v.intX() + 0.5, v.intY(), v.intZ() + 0.5);
            final BlockState state = v.getBlockState(cube.getCommandSenderWorld());
            final VoxelShape s = state.getCollisionShape(world, v.getPos());
            if (!s.isEmpty()) v.y += s.max(Axis.Y);
            // Ensure the mob's position is initialized properly first
            v.moveEntity(mob);
            v = SendOutManager.getFreeSpot(mob, world, v, respectRoom);
            // Let npcs send out their mobs wherever they want to...
            if (v == null && isPlayers)
            {
                Tools.giveItem((Player) cube.shootingEntity, cube.getItem());
                user.displayClientMessage(new TranslatableComponent("pokecube.noroom"), true);
                cube.discard();
                return null;
            }
            else if (v == null) v = cube.v0.set(cube);
        }
        mob.fallDistance = 0;
        v.moveEntity(mob);
        if (hasPokemob)
        {
            // Check permissions
            if (config.permsSendOutSpecific && isPlayers)
            {
                final PokedexEntry entry = pokemob.getPokedexEntry();
                final boolean denied = !PermNodes.getBooleanPerm(user, Permissions.SENDOUTSPECIFIC.get(entry));
                if (denied)
                {
                    Tools.giveItem(user, cube.getItem());
                    user.displayClientMessage(new TranslatableComponent("pokecube.sendout.fail.noperms.specific",
                            pokemob.getDisplayName()), true);
                    cube.discard();
                    return null;
                }
            }

            final SendOut evt = new SendOut.Pre(pokemob);
            if (PokecubeCore.POKEMOB_BUS.post(evt))
            {
                if (isPlayers)
                {
                    Tools.giveItem(user, cube.getItem());
                    user.displayClientMessage(
                            new TranslatableComponent("pokecube.sendout.fail.cancelled", pokemob.getDisplayName()),
                            true);
                    cube.discard();
                }
                return null;
            }
            ItemStack cubeStack = pokemob.getPokecube();
            if (cubeStack.isEmpty())
            {
                cubeStack = cube.getItem().copy();
                cubeStack.getTag().remove(TagNames.POKEMOB);
                pokemob.setPokecube(cubeStack);
            }

            cube.setReleased(mob);

            // Ensure AI is initialized
//            pokemob.postInitAI();FIXME

            SendOutManager.apply(world, mob, v, pokemob, summon);
            cube.setItem(pokemob.getPokecube());

        }
        else if (mob instanceof LivingEntity)
        {
            cube.getItem().getTag().remove(TagNames.MOBID);
            cube.getItem().getTag().remove(TagNames.POKEMOB);
            cube.setReleased(mob);
            SendOutManager.apply(world, mob, v, pokemob, summon);
            return (LivingEntity) mob;
        }
        else
        {
            cube.spawnAtLocation(cube.getItem(), 0.5f);
            cube.discard();
        }
        if (pokemob == null) return null;
        return pokemob.getEntity();
    }

    private static void make(final ServerLevel world, final Entity mob, final Vector3 v, final IPokemob pokemob,
            final boolean summon)
    {
        if (summon) world.addWithUUID(mob);
        if (pokemob != null)
        {
            pokemob.onSendOut();
            pokemob.setGeneralState(GeneralStates.TAMED, true);
            pokemob.setGeneralState(GeneralStates.EXITINGCUBE, true);
            pokemob.setEvolutionTicks(50 + PokecubeCore.getConfig().exitCubeDuration);
            final Entity owner = pokemob.getOwner();
            if (owner instanceof Player)
            {
                final Component mess = CommandTools.makeTranslatedMessage("pokemob.action.sendout", "green",
                        pokemob.getDisplayName());
                pokemob.displayMessageToOwner(mess);
            }
            final SendOut evt = new SendOut.Post(pokemob);
            PokecubeCore.POKEMOB_BUS.post(evt);
        }
    }

    private static void apply(final ServerLevel world, final Entity mob, final Vector3 v, final IPokemob pokemob,
            final boolean summon)
    {
        final Entity test = world.getEntity(mob.getUUID());
        final Vector3 vec = v.copy();
        final UUID id = mob.getUUID();
        if (test == null) SendOutManager.make(world, mob, vec, pokemob, summon);
        else
        {
            PokecubeCore.LOGGER.warn("Replacing errored UUID mob! {}", mob);
            mob.getPersistentData().putUUID("old_uuid", id);
            mob.setUUID(UUID.randomUUID());
            SendOutManager.make(world, mob, vec, pokemob, summon);
            final IRunnable task = w -> {
                // Ensure the chunk is loaded here.
                w.getChunk(vec.getPos());
                final Entity original = world.getEntity(id);
                // The mob already exists in the world, remove it
                if (original != null) world.removeEntity(original, false);
                PokemobTracker.removePokemob(pokemob);
                mob.setUUID(id);
                PokemobTracker.addPokemob(pokemob);
                return true;
            };
            EventsHandler.Schedule(world, task);
        }
    }
}
