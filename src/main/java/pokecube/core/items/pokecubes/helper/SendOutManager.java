package pokecube.core.items.pokecubes.helper;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.server.permission.IPermissionHandler;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.context.PlayerContext;
import pokecube.core.PokecubeCore;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.pokemob.SpawnEvent.SendOut;
import pokecube.core.handlers.Config;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.items.pokecubes.EntityPokecubeBase;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.Permissions;
import pokecube.core.utils.TagNames;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;
import thut.core.common.commands.CommandTools;

public class SendOutManager
{
    public static LivingEntity sendOut(final EntityPokecubeBase cube, final boolean summon)
    {
        if (cube.getEntityWorld().isRemote || cube.isReleasing()) return null;
        cube.setTime(20);
        final ServerWorld world = (ServerWorld) cube.getEntityWorld();
        final Entity mob = PokecubeManager.itemToMob(cube.getItem(), cube.getEntityWorld());
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        final Config config = PokecubeCore.getConfig();
        // Check permissions
        if (config.permsSendOut && cube.shootingEntity instanceof PlayerEntity)
        {
            final PlayerEntity player = (PlayerEntity) cube.shootingEntity;
            final IPermissionHandler handler = PermissionAPI.getPermissionHandler();
            final PlayerContext context = new PlayerContext(player);
            boolean denied = false;
            if (!handler.hasPermission(player.getGameProfile(), Permissions.SENDOUTPOKEMOB, context)) denied = true;
            if (denied)
            {
                Tools.giveItem((PlayerEntity) cube.shootingEntity, cube.getItem());
                cube.remove();
                return null;
            }
        }

        // TODO use what spawn code uses, to check if the mob fits here, if not,
        // cancel the send out.

        // Fix the mob's position.
        final Vector3 v = cube.v0.set(cube);
        if (mob != null)
        {
            v.set(v.intX() + 0.5, v.y, v.intZ() + 0.5);
            final BlockState state = v.getBlockState(cube.getEntityWorld());
            if (state.getMaterial().isSolid()) v.y = Math.ceil(v.y);
            mob.fallDistance = 0;
            v.moveEntity(mob);
        }

        if (pokemob != null)
        {
            // Check permissions
            if (config.permsSendOutSpecific && cube.shootingEntity instanceof PlayerEntity)
            {
                final PokedexEntry entry = pokemob.getPokedexEntry();
                final PlayerEntity player = (PlayerEntity) cube.shootingEntity;
                final IPermissionHandler handler = PermissionAPI.getPermissionHandler();
                final PlayerContext context = new PlayerContext(player);
                boolean denied = false;
                if (!handler.hasPermission(player.getGameProfile(), Permissions.SENDOUTSPECIFIC.get(entry), context))
                    denied = true;
                if (denied)
                {
                    Tools.giveItem((PlayerEntity) cube.shootingEntity, cube.getItem());
                    cube.remove();
                    return null;
                }
            }

            SendOut evt = new SendOut.Pre(pokemob.getPokedexEntry(), v, cube.getEntityWorld(), pokemob);
            if (PokecubeCore.POKEMOB_BUS.post(evt))
            {
                if (cube.shootingEntity != null && cube.shootingEntity instanceof PlayerEntity)
                {
                    Tools.giveItem((PlayerEntity) cube.shootingEntity, cube.getItem());
                    cube.remove();
                }
                return null;
            }
            if (summon) world.summonEntity(mob);
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
            cube.setReleased(mob);
            cube.setMotion(0, 0, 0);
            cube.setTime(20);
            cube.setReleasing(true);
            cube.setItem(pokemob.getPokecube());
            evt = new SendOut.Post(pokemob.getPokedexEntry(), v, cube.getEntityWorld(), pokemob);
            PokecubeCore.POKEMOB_BUS.post(evt);
        }
        else if (mob instanceof LivingEntity)
        {
            cube.getItem().getTag().remove(TagNames.MOBID);
            cube.getItem().getTag().remove(TagNames.POKEMOB);
            cube.setReleased(mob);
            cube.setMotion(0, 0, 0);
            cube.setTime(20);
            cube.setReleasing(true);
            if (summon) world.summonEntity(mob);
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
}
