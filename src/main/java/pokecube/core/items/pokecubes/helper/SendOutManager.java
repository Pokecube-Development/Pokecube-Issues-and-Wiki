package pokecube.core.items.pokecubes.helper;

import java.util.UUID;
import java.util.stream.Stream;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.server.permission.IPermissionHandler;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.context.PlayerContext;
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
import pokecube.core.utils.Permissions;
import pokecube.core.utils.PokemobTracker;
import pokecube.core.utils.TagNames;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;
import thut.core.common.commands.CommandTools;

public class SendOutManager
{
    public static Vector3 getFreeSpot(final Entity mob, final ServerWorld world, final Vector3 pos,
            final boolean respectRoom)
    {
        AxisAlignedBB box = mob.getBoundingBox();
        final PartEntity<?>[] parts = mob.getParts();
        if (parts != null) for (final PartEntity<?> part : parts)
            box = box.union(part.getBoundingBox());
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
            if (SendOutManager.valid(box.offset(r.x, r.y, r.z), world)) return rAbs;
        }
        return respectRoom ? null : pos.copy();
    }

    public static boolean valid(final AxisAlignedBB box, final ServerWorld world)
    {
        final Stream<VoxelShape> colliding = world.getCollisionShapes(null, box);
        final long num = colliding.count();
        return num == 0;
    }

    public static LivingEntity sendOut(final EntityPokecubeBase cube, final boolean summon)
    {
        return SendOutManager.sendOut(cube, summon, true);
    }

    public static LivingEntity sendOut(final EntityPokecubeBase cube, final boolean summon, final boolean respectRoom)
    {
        if (cube.getEntityWorld().isRemote || cube.isReleasing()) return null;
        final ServerWorld world = (ServerWorld) cube.getEntityWorld();
        final Entity mob = PokecubeManager.itemToMob(cube.getItem(), cube.getEntityWorld());

        if (mob == null) return null;

        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        final Config config = PokecubeCore.getConfig();

        // Next check some conditions for whether the sendout can occur.

        final boolean hasMob = mob != null;
        final boolean hasPokemob = pokemob != null;
        final boolean isPlayers = cube.shootingEntity instanceof ServerPlayerEntity;
        final ServerPlayerEntity user = isPlayers ? (ServerPlayerEntity) cube.shootingEntity : null;
        final boolean checkPerms = config.permsSendOut && isPlayers;
        boolean hasPerms = true;

        // Check permissions
        if (checkPerms)
        {
            final IPermissionHandler handler = PermissionAPI.getPermissionHandler();
            final PlayerContext context = new PlayerContext(user);
            hasPerms = handler.hasPermission(user.getGameProfile(), Permissions.SENDOUTPOKEMOB, context);
        }

        // No mob or no perms?, then just refund the item and exit
        if (!hasMob || !hasPerms)
        {
            if (isPlayers && cube.shootingEntity.isAlive())
            {
                Tools.giveItem((PlayerEntity) cube.shootingEntity, cube.getItem());
                user.sendMessage(new TranslationTextComponent("pokecube.sendout.fail.noperms.general"),
                        Util.DUMMY_UUID);
                cube.remove();
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
            final BlockState state = v.getBlockState(cube.getEntityWorld());
            final VoxelShape s = state.getCollisionShape(world, v.getPos());
            if (!s.isEmpty()) v.y += s.getEnd(Axis.Y);
            // Ensure the mob's position is initialized properly first
            v.moveEntity(mob);
            v = SendOutManager.getFreeSpot(mob, world, v, respectRoom);
            // Let npcs send out their mobs wherever they want to...
            if (v == null && isPlayers)
            {
                Tools.giveItem((PlayerEntity) cube.shootingEntity, cube.getItem());
                user.sendMessage(new TranslationTextComponent("pokecube.noroom"), Util.DUMMY_UUID);
                cube.remove();
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
                final IPermissionHandler handler = PermissionAPI.getPermissionHandler();
                final PlayerContext context = new PlayerContext(user);
                final boolean denied = !handler.hasPermission(user.getGameProfile(), Permissions.SENDOUTSPECIFIC.get(
                        entry), context);
                if (denied)
                {
                    Tools.giveItem(user, cube.getItem());
                    user.sendMessage(new TranslationTextComponent("pokecube.sendout.fail.noperms.specific", pokemob
                            .getDisplayName()), Util.DUMMY_UUID);
                    cube.remove();
                    return null;
                }
            }

            final SendOut evt = new SendOut.Pre(pokemob.getPokedexEntry(), v, cube.getEntityWorld(), pokemob);
            if (PokecubeCore.POKEMOB_BUS.post(evt))
            {
                if (isPlayers)
                {
                    Tools.giveItem(user, cube.getItem());
                    user.sendMessage(new TranslationTextComponent("pokecube.sendout.fail.cancelled", pokemob
                            .getDisplayName()), Util.DUMMY_UUID);
                    cube.remove();
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
            pokemob.initAI();

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
            cube.entityDropItem(cube.getItem(), 0.5f);
            cube.remove();
        }
        if (pokemob == null) return null;
        return pokemob.getEntity();
    }

    private static void make(final ServerWorld world, final Entity mob, final Vector3 v, final IPokemob pokemob,
            final boolean summon)
    {
        if (summon) world.summonEntity(mob);
        if (pokemob != null)
        {
            pokemob.onSendOut();
            pokemob.setGeneralState(GeneralStates.TAMED, true);
            pokemob.setGeneralState(GeneralStates.EXITINGCUBE, true);
            pokemob.setEvolutionTicks(50 + PokecubeCore.getConfig().exitCubeDuration);
            final Entity owner = pokemob.getOwner();
            if (owner instanceof PlayerEntity)
            {
                final ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.action.sendout", "green",
                        pokemob.getDisplayName());
                pokemob.displayMessageToOwner(mess);
            }
            final SendOut evt = new SendOut.Post(pokemob.getPokedexEntry(), v, world, pokemob);
            PokecubeCore.POKEMOB_BUS.post(evt);
        }
    }

    private static void apply(final ServerWorld world, final Entity mob, final Vector3 v, final IPokemob pokemob,
            final boolean summon)
    {
        final Entity test = world.getEntityByUuid(mob.getUniqueID());
        final Vector3 vec = v.copy();
        final UUID id = mob.getUniqueID();
        if (test == null) SendOutManager.make(world, mob, vec, pokemob, summon);
        else
        {
            PokecubeCore.LOGGER.warn("Replacing errored UUID mob! {}", mob);
            mob.getPersistentData().putUniqueId("old_uuid", id);
            mob.setUniqueId(UUID.randomUUID());
            SendOutManager.make(world, mob, vec, pokemob, summon);
            final IRunnable task = w ->
            {
                // Ensure the chunk is loaded here.
                w.getChunk(vec.getPos());
                final Entity original = world.getEntityByUuid(id);
                // The mob already exists in the world, remove it
                if (original != null) world.removeEntity(original, false);
                PokemobTracker.removePokemob(pokemob);
                mob.setUniqueId(id);
                PokemobTracker.addPokemob(pokemob);
                return true;
            };
            EventsHandler.Schedule(world, task);
        }
    }
}
